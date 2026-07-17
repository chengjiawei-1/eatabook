import httpInstance from '@/utils/http';
import type { Notification } from '@/types/notification';

export const getNotifications = async (limit: number = 20): Promise<Notification[]> => {
  // 后端接口：GET /api/notifications?limit=20
  return httpInstance.get('/api/notifications', {
    params: { limit }
  });
};

// 获取未读数量
export const getUnreadCount = async (): Promise<{ unreadCount: number }> => {
  try {
    return await httpInstance.get('/api/notifications/unread-count');
  } catch (error) {
    console.error('获取未读消息数量失败:', error);
    // 返回默认值，避免应用崩溃
    return { unreadCount: 0 };
  }
};

// 标记单条已读
export const markAsRead = async (id: number): Promise<void> => {
  await httpInstance.put(`/api/notifications/${id}/read`);
};

// 标记所有已读 
export const markAllAsRead = async (): Promise<void> => {
  await httpInstance.put('/api/notifications/read');
};

// 删除消息
export const deleteNotification = async (id: number): Promise<void> => {
  await httpInstance.delete(`/api/notifications/${id}`);
};
