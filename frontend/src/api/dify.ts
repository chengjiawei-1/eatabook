//语义驱动，而不是 HTTP 方法驱动
import httpInstance from "@/utils/http";
const API_BASE_URL = "/api/ai";

// 通用类型
export interface ChatPayload {
  agent: string;            // 智能体标识，如 translator / constitution
  prompt: string;           // 完整 prompt
  meta?: Record<string, any>; // 可选扩展信息（schema / 场景 / tag）
}

//用途：问答 / 推荐 / 简单解释
//用途：体质分析 / 评估 / 固定格式输出
export async function chatStructured(agent: string, prompt: string) {
  try {
    const payload = {
      agent: agent,
      prompt: prompt
    };

    const res = await httpInstance.post('/api/ai/chat/structured', payload, {
      timeout: 60000 
    });
    console.log(res)

    // 返回 res.data 给前端使用
    return res;
  } catch (err) {
    console.error("[chatStructured] error:", err);
    throw err;
  }
}

//用途：翻译 / 长文生成 / 实时展示
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
    throw new Error(`Stream 请求失败: ${response.status}`);
  }

  if (!response.body) {
    throw new Error("Stream body 为空");
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
