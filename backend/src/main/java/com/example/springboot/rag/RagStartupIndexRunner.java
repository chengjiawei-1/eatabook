package com.example.springboot.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RagStartupIndexRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RagStartupIndexRunner.class);

    private final RagIndexService ragIndexService;

    @Value("${app.rag.index-on-startup:false}")
    private boolean indexOnStartup;

    @Value("${app.rag.fail-on-index-error:false}")
    private boolean failOnIndexError;

    public RagStartupIndexRunner(RagIndexService ragIndexService) {
        this.ragIndexService = ragIndexService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!indexOnStartup) {
            return;
        }

        try {
            RagIndexService.RagIndexResult result = ragIndexService.index();
            log.info("RAG indexing finished: resources={}, documents={}, chunks={}, sources={}",
                    result.resourceCount(),
                    result.documentCount(),
                    result.chunkCount(),
                    result.sources());
        } catch (RuntimeException e) {
            if (failOnIndexError) {
                throw e;
            }
            log.error("RAG indexing failed during startup. Application will continue running.", e);
        }
    }
}
