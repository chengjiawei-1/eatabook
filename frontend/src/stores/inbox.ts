import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Notification } from '@/types/notification';
import { 
  getNotifications, 
  markAsRead as apiMarkAsRead, 
  markAllAsRead as apiMarkAllAsRead,
  getUnreadCount as apiGetUnreadCount
} from '@/api/notification';

export const useInboxStore = defineStore('inbox', () => {
  const messages = ref<Notification[]>([]);
  const unreadCount = ref(0);
  const isLoading = ref(false);
  
  // 计算属性：未读消息列表
  const unreadMessages = computed(() => 
    messages.value.filter(msg => !msg.readStatus)
  );

  // 加载消息列表和未读数
  const loadMessages = async (isForce = false) => {
    // 简单的防抖，如果刚加载过且不是强制刷新，则跳过
    if (isLoading.value && !isForce) return;
    
    isLoading.value = true;
    try {
      // 并行请求：获取列表 + 获取未读数
      const [listRes, countRes] = await Promise.all([
        getNotifications(20),
        apiGetUnreadCount()
      ]);

      // 处理列表 
      messages.value = Array.isArray(listRes) ? listRes : (listRes as any).data || [];
      
      // 处理未读数
      unreadCount.value = countRes.unreadCount || 0;
      
    } catch (error) {
      console.error('加载消息中心失败:', error);
    } finally {
      isLoading.value = false;
    }
  };

  //接收 WebSocket 推送的新消息
  const addMessage = (message: Notification) => {
    // 防止重复添加
    const exists = messages.value.some(m => m.id === message.id);
    if (!exists) {
      messages.value.unshift(message); // 添加到队首
      if (!message.readStatus) {
        unreadCount.value++;
      }
    }
  };

  // 标记单条已读
  const markAsRead = async (id: number) => {
    const message = messages.value.find(msg => msg.id === id);
    if (message && message.readStatus) return; // 已经是已读

    try {
      await apiMarkAsRead(id);
      // 更新本地状态
      if (message) {
        message.readStatus = true;
        unreadCount.value = Math.max(0, unreadCount.value - 1);
      }
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  };

  // 全部已读
  const markAllAsRead = async () => {
    if (unreadCount.value === 0) return;
    
    try {
      await apiMarkAllAsRead();
      // 本地批量更新
      messages.value.forEach(msg => msg.readStatus = true);
      unreadCount.value = 0;
    } catch (error) {
      console.error('一键已读失败:', error);
    }
  };

  // 清空状态 (登出时调用)
  const clearMessages = () => {
    messages.value = [];
    unreadCount.value = 0;
  };

  return {
    messages,
    unreadCount,
    unreadMessages,
    isLoading,
    loadMessages,
    addMessage,
    markAsRead,
    markAllAsRead,
    clearMessages
  };
});