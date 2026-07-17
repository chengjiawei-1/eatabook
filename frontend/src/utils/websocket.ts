import { useAuthStore } from "@/stores/auth";
import { useInboxStore } from "@/stores/inbox";
import type { Notification } from "@/types/notification";

// 配置项接口
export interface WebSocketConfig {
  url: string;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
}

class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 3000;
  private url: string;
  private isConnected = false;
  private manuallyClosed = false; // 标记是否手动关闭

  constructor(config: WebSocketConfig) {
    this.url = config.url;
    if (config.reconnectInterval) this.reconnectInterval = config.reconnectInterval;
    if (config.maxReconnectAttempts) this.maxReconnectAttempts = config.maxReconnectAttempts;
  }

  // 建立连接
  public connect() {
    // 获取 Token 
    const authStore = useAuthStore();
    const token = authStore.token || localStorage.getItem('token'); 

    if (!token) {
      // console.warn
      return;
    }

    // 防止重复连接
    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return;
    }

    this.manuallyClosed = false;

    try {
      // 拼接 Token 
      let cleanToken = token;
      if (token.startsWith('Bearer ')) {
        cleanToken = token.substring(7);
      }

      const wsUrl = `${this.url}?token=${cleanToken}`;
      this.ws = new WebSocket(wsUrl);

      this.initEventHandlers();

    } catch (error) {
      console.error("WebSocket: 创建连接失败", error);
      this.handleReconnect();
    }
  }

  // 初始化事件
  private initEventHandlers() {
    if (!this.ws) return;

    this.ws.onopen = () => {
      console.log("WebSocket: 连接成功");
      this.isConnected = true;
      this.reconnectAttempts = 0;
    };

    this.ws.onmessage = (event) => {
      this.handleMessage(event.data);
    };

    this.ws.onclose = (event) => {
      this.isConnected = false;
      this.ws = null;
      // 只有非手动关闭时才重连
      if (!this.manuallyClosed) {
        console.log(`WebSocket: 连接断开 (Code: ${event.code})，准备重连...`);
        this.handleReconnect();
      }
    };

    this.ws.onerror = (error) => {
      console.error("WebSocket: 网络错误", error);
    };
  }

  // 处理消息
  private handleMessage(messageData: string) {
    try {
      const data = JSON.parse(messageData);
      
      // 匹配后端 NotificationWebSocketHandler 发送的结构
      if (data.type === 'NEW_NOTIFICATION' && data.notification) {
        const notification: Notification = data.notification;
        
        console.log("收到新消息:", notification.title);

        // 调用 Pinia Store 更新 UI
        const inboxStore = useInboxStore();
        inboxStore.addMessage(notification);
      }
    } catch (error) {
      console.error("WebSocket: 消息解析异常", error);
    }
  }

  // 重连机制
  private handleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.warn("WebSocket: 超过最大重连次数，放弃重连");
      return;
    }

    this.reconnectAttempts++;
    setTimeout(() => {
      this.connect();
    }, this.reconnectInterval);
  }

  // 手动断开
  public disconnect() {
    this.manuallyClosed = true;
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.isConnected = false;
  }
}

// 单例导出
let wsInstance: WebSocketService | null = null;

export const initWebSocket = () => {
  // 这里的 URL 请根据你的实际后端地址修改
  const WS_URL = `${import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8081'}/ws/notifications`;

  if (!wsInstance) {
    wsInstance = new WebSocketService({
      url: WS_URL,
      reconnectInterval: 5000
    });
  }
  wsInstance.connect();
};

export const closeWebSocket = () => {
  if (wsInstance) {
    wsInstance.disconnect();
    wsInstance = null;
  }
};
