import { useAuthStore } from "@/stores/auth";
import { useInboxStore } from "@/stores/inbox";
import type { Notification } from "@/types/notification";

export interface WebSocketConfig {
  url: string;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
  onMessage?: (message: any) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts: number;
  private readonly reconnectInterval: number;
  private readonly url: string;
  private manuallyClosed = false;
  private readonly onMessage?: (message: any) => void;
  private readonly onConnect?: () => void;
  private readonly onDisconnect?: () => void;

  constructor(config: WebSocketConfig) {
    this.url = config.url;
    this.reconnectInterval = config.reconnectInterval ?? 3000;
    this.maxReconnectAttempts = config.maxReconnectAttempts ?? 5;
    this.onMessage = config.onMessage;
    this.onConnect = config.onConnect;
    this.onDisconnect = config.onDisconnect;
  }

  public connect() {
    const authStore = useAuthStore();
    const token = authStore.token || localStorage.getItem("token");

    if (!token) {
      return;
    }

    if (this.ws && [WebSocket.OPEN, WebSocket.CONNECTING].includes(this.ws.readyState)) {
      return;
    }

    this.manuallyClosed = false;

    try {
      const cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;
      const separator = this.url.includes("?") ? "&" : "?";
      this.ws = new WebSocket(`${this.url}${separator}token=${encodeURIComponent(cleanToken)}`);
      this.initEventHandlers();
    } catch (error) {
      console.error("WebSocket: failed to create connection", error);
      this.handleReconnect();
    }
  }

  public disconnect() {
    this.manuallyClosed = true;
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  private initEventHandlers() {
    if (!this.ws) return;

    this.ws.onopen = () => {
      this.reconnectAttempts = 0;
      this.onConnect?.();
    };

    this.ws.onmessage = (event) => {
      this.handleMessage(event.data);
    };

    this.ws.onclose = () => {
      this.ws = null;
      this.onDisconnect?.();
      if (!this.manuallyClosed) {
        this.handleReconnect();
      }
    };

    this.ws.onerror = (error) => {
      console.error("WebSocket: network error", error);
    };
  }

  private handleMessage(messageData: string) {
    try {
      const data = JSON.parse(messageData);

      if (this.onMessage) {
        this.onMessage(data.notification ?? data);
        return;
      }

      if (data.type === "NEW_NOTIFICATION" && data.notification) {
        const inboxStore = useInboxStore();
        inboxStore.addMessage(data.notification as Notification);
      }
    } catch (error) {
      console.error("WebSocket: failed to parse message", error);
    }
  }

  private handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      return;
    }

    this.reconnectAttempts++;
    window.setTimeout(() => this.connect(), this.reconnectInterval);
  }
}

const wsInstances = new Map<string, WebSocketService>();

const trimTrailingSlash = (value: string) => value.replace(/\/+$/, "");

const getDefaultWsBaseUrl = () => {
  const configured = import.meta.env.VITE_WS_BASE_URL;
  if (configured) {
    return trimTrailingSlash(configured);
  }

  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}`;
};

export const initWebSocket = (config?: Partial<WebSocketConfig>) => {
  const url = config?.url ?? `${getDefaultWsBaseUrl()}/ws/notifications`;
  const existing = wsInstances.get(url);

  if (existing) {
    existing.connect();
    return existing;
  }

  const service = new WebSocketService({
    url,
    reconnectInterval: 5000,
    ...config,
  });
  wsInstances.set(url, service);
  service.connect();
  return service;
};

export const getWebSocket = (url?: string) => {
  if (url) {
    return wsInstances.get(url) ?? null;
  }
  return wsInstances.values().next().value ?? null;
};

export const closeWebSocket = (url?: string) => {
  if (url) {
    wsInstances.get(url)?.disconnect();
    wsInstances.delete(url);
    return;
  }

  wsInstances.forEach((service) => service.disconnect());
  wsInstances.clear();
};
