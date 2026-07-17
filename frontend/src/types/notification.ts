// types/notification.ts
export interface Notification {
  id: number;
  userId: number;
  type: 'system' | 'reply' | 'like' | 'favorite' | 'follow' | 'reminder'; // 对应后端的 type
  title: string;
  content: string;
  link?: string;
  readStatus: boolean; 
  relatedId?: number;
  createdAt: string;
  readAt?: string;
  // 前端辅助字段
  senderName?: string; 
  senderId?: number;
}