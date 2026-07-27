package com.example.springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

@Service
public class AiRequestLimiter {

    private final Semaphore semaphore;
    private final Object pacingLock = new Object();
    private final long minIntervalMillis;
    private final int maxRetries;
    private final long initialBackoffMillis;
    private final long maxBackoffMillis;

    private long nextAllowedAt;

    public AiRequestLimiter(@Value("${app.ai.request.max-concurrent:1}") int maxConcurrent,
                            @Value("${app.ai.request.min-interval-millis:3000}") long minIntervalMillis,
                            @Value("${app.ai.request.max-retries:2}") int maxRetries,
                            @Value("${app.ai.request.initial-backoff-millis:8000}") long initialBackoffMillis,
                            @Value("${app.ai.request.max-backoff-millis:30000}") long maxBackoffMillis) {
        this.semaphore = new Semaphore(Math.max(1, maxConcurrent));
        this.minIntervalMillis = Math.max(0, minIntervalMillis);
        this.maxRetries = Math.max(0, maxRetries);
        this.initialBackoffMillis = Math.max(1000, initialBackoffMillis);
        this.maxBackoffMillis = Math.max(this.initialBackoffMillis, maxBackoffMillis);
    }

    public <T> T execute(Callable<T> action) {
        RuntimeException lastFailure = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                acquireSlot();
                try {
                    return action.call();
                } finally {
                    semaphore.release();
                }
            } catch (Exception error) {
                RuntimeException failure = toRuntimeException(error);
                if (!isRateLimitException(failure) || attempt >= maxRetries) {
                    throw failure;
                }
                lastFailure = failure;
                sleepQuietly(backoffMillis(attempt));
            }
        }
        throw lastFailure == null ? new IllegalStateException("AI request failed") : lastFailure;
    }

    public <T> Flux<T> executeFlux(Supplier<Flux<T>> supplier) {
        return Flux.defer(() -> {
            boolean acquired = false;
            try {
                acquireSlot();
                acquired = true;
                return supplier.get().doFinally(signalType -> semaphore.release());
            } catch (RuntimeException error) {
                if (acquired) {
                    semaphore.release();
                }
                return Flux.error(error);
            } catch (Exception error) {
                if (acquired) {
                    semaphore.release();
                }
                return Flux.error(toRuntimeException(error));
            }
        }).retryWhen(Retry.backoff(maxRetries, Duration.ofMillis(initialBackoffMillis))
                .maxBackoff(Duration.ofMillis(maxBackoffMillis))
                .filter(this::isRateLimitException));
    }

    private void acquireSlot() throws InterruptedException {
        semaphore.acquire();
        pace();
    }

    private void pace() throws InterruptedException {
        if (minIntervalMillis <= 0) {
            return;
        }
        synchronized (pacingLock) {
            long now = System.currentTimeMillis();
            long waitMillis = nextAllowedAt - now;
            if (waitMillis > 0) {
                Thread.sleep(waitMillis);
                now = System.currentTimeMillis();
            }
            nextAllowedAt = now + minIntervalMillis;
        }
    }

    private long backoffMillis(int attempt) {
        long multiplier = 1L << Math.min(attempt, 4);
        long delay = initialBackoffMillis * multiplier;
        return Math.min(delay, maxBackoffMillis);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry AI request", e);
        }
    }

    private RuntimeException toRuntimeException(Exception error) {
        if (error instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new IllegalStateException(error);
    }

    private boolean isRateLimitException(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientError
                    && webClientError.getStatusCode().value() == 429) {
                return true;
            }
            if (current instanceof ResponseStatusException responseStatusException
                    && responseStatusException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
