import httpInstance from "@/utils/http";

const API_BASE_URL = "/api/ai";

export interface ChatPayload {
  agent?: string;
  scene?: string;
  prompt: string;
  meta?: Record<string, any>;
}

export async function chatStructured(agent: string, prompt: string) {
  const payload: ChatPayload = {
    agent,
    prompt
  };

  try {
    return await httpInstance.post(`${API_BASE_URL}/chat/structured`, payload, {
      timeout: 60000
    });
  } catch (error: any) {
    const message = error?.response?.data?.message
      || error?.response?.data?.error
      || error?.response?.data
      || error?.message
      || "AI request failed";
    throw new Error(String(message));
  }
}

export async function chatStream(
  payload: ChatPayload,
  onChunk: (text: string) => void,
  onDone?: () => void
): Promise<void> {
  const token = localStorage.getItem("token");
  const controller = new AbortController();
  const signal = controller.signal;

  const response = await fetch(`${API_BASE_URL}/chat/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": token ? `Bearer ${token}` : "",
      "Accept": "text/event-stream",
      "Cache-Control": "no-cache"
    },
    body: JSON.stringify(payload),
    signal
  });

  if (!response.ok) {
    const errorText = await response.text().catch(() => "");
    throw new Error(`Stream request failed: ${response.status}${errorText ? ` - ${errorText}` : ""}`);
  }

  if (!response.body) {
    throw new Error("Stream body is empty");
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder("utf-8");

  let buffer = "";
  let doneFlag = false;

  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split(/\r?\n\r?\n/);
      buffer = events.pop() || "";

      for (const event of events) {
        const lines = event.split(/\r?\n/);

        for (const line of lines) {
          if (!line.startsWith("data:")) continue;

          const content = line.slice(5).trim();
          if (!content) continue;

          if (content === "[DONE]") {
            if (!doneFlag) {
              doneFlag = true;
              onDone?.();
            }
            controller.abort();
            return;
          }

          onChunk(content);
        }
      }
    }
  } catch (err) {
    if ((err as any)?.name !== "AbortError") {
      throw err;
    }
  } finally {
    if (!doneFlag) {
      onDone?.();
    }
    reader.releaseLock();
  }
}
