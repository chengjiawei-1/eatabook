<template>
  <div class="inbox-container">
    <!-- 收件箱图标（带未读数量） -->
    <div class="inbox-icon" @click="toggleInbox">
      <i class="fa-solid fa-envelope text-lg"></i>
      <span v-if="unreadCount > 0" class="unread-badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </div>

    <!-- 收件箱面板 -->
    <div v-if="isOpen" class="inbox-panel">
      <div class="inbox-header">
        <h3 class="text-lg font-semibold">收件箱</h3>
        <div class="header-actions">
          <button 
            v-if="unreadCount > 0"
            @click="markAllAsRead"
            class="text-sm text-blue-600 hover:text-blue-800"
          >
            全部已读
          </button>
          <button @click="refresh" class="ml-2">
            <i class="fa-solid fa-rotate" :class="{ 'fa-spin': isLoading }"></i>
          </button>
          <button @click="closeInbox" class="ml-2">
            <i class="fa-solid fa-times"></i>
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <div v-if="messages.length === 0 && !isLoading" class="empty-state">
          <i class="fa-regular fa-envelope-open text-4xl text-gray-400 mb-2"></i>
          <p class="text-gray-500">暂无消息</p>
        </div>

        <div v-else class="messages">
          <div 
            v-for="message in messages" 
            :key="message.id"
            :class="['message-item', { unread: !message.read }]"
            @click="handleMessageClick(message)"
          >
            <div class="message-header">
              <span class="message-type" :class="message.type">
                {{ getTypeLabel(message.type) }}
              </span>
              <span class="message-time">
                {{ formatTime(message.timestamp) }}
              </span>
            </div>
            <div class="message-title">
              {{ message.title }}
            </div>
            <div class="message-content">
              {{ message.content }}
            </div>
            <div v-if="message.senderName" class="message-sender">
              来自: {{ message.senderName }}
            </div>
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="hasMore" class="load-more">
          <button 
            @click="loadMore"
            :disabled="isLoading"
            class="load-more-btn"
          >
            {{ isLoading ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useInboxStore } from '@/stores/inbox';
import { initWebSocket, getWebSocket } from '@/utils/websocket';

const inboxStore = useInboxStore();
const isOpen = ref(false);
const wsService = ref<any>(null);

// 计算属性
const messages = ref(inboxStore.messages);
const unreadCount = ref(inboxStore.unreadCount);
const isLoading = ref(inboxStore.isLoading);
const hasMore = ref(inboxStore.hasMore);

// 初始化WebSocket
const initWebSocketConnection = () => {
  const wsBaseUrl = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8081';
  wsService.value = initWebSocket({
    url: `${wsBaseUrl}/ws/inbox`,
    onMessage: (message) => {
      inboxStore.addMessage(message);
    },
    onConnect: () => {
      console.log('WebSocket连接成功');
    },
    onDisconnect: () => {
      console.log('WebSocket连接断开');
    }
  });
};

// 切换收件箱显示
const toggleInbox = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    inboxStore.loadMessages(true);
  }
};

// 关闭收件箱
const closeInbox = () => {
  isOpen.value = false;
};

// 刷新消息
const refresh = async () => {
  await inboxStore.loadMessages(true);
};

// 加载更多消息
const loadMore = async () => {
  await inboxStore.loadMessages();
};

// 标记全部为已读
const markAllAsRead = async () => {
  await inboxStore.markAllAsRead();
};

// 处理消息点击
const handleMessageClick = async (message: any) => {
  if (!message.read) {
    await inboxStore.markAsRead(message.id);
  }
  
  // 根据消息类型处理点击事件
  switch (message.type) {
    case 'system':
      // 处理系统消息
      break;
    case 'notification':
      // 处理通知消息
      break;
    case 'chat':
      // 处理聊天消息
      break;
  }
};

// 格式化时间
const formatTime = (timestamp: number) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - timestamp;
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  
  return date.toLocaleDateString();
};

// 获取类型标签
const getTypeLabel = (type: string) => {
  const labels = {
    system: '系统',
    notification: '通知',
    chat: '聊天'
  };
  return labels[type as keyof typeof labels] || type;
};

// 点击外部关闭收件箱
const handleClickOutside = (event: Event) => {
  const target = event.target as HTMLElement;
  if (!target.closest('.inbox-container')) {
    closeInbox();
  }
};

onMounted(() => {
  // 初始化WebSocket连接
  initWebSocketConnection();
  
  // 加载初始消息
  inboxStore.loadMessages(true);
  
  // 添加点击外部关闭监听
  document.addEventListener('click', handleClickOutside);
});

onUnmounted(() => {
  // 清理WebSocket连接
  if (wsService.value) {
    wsService.value.disconnect();
  }
  
  // 移除事件监听
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
.inbox-container {
  position: relative;
  display: inline-block;
}

.inbox-icon {
  position: relative;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  transition: background-color 0.3s;
}

.inbox-icon:hover {
  background-color: rgba(0, 0, 0, 0.1);
}

.unread-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #ff4757;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  min-width: 16px;
  text-align: center;
}

.inbox-panel {
  position: absolute;
  top: 100%;
  right: 0;
  width: 400px;
  max-height: 500px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.inbox-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-list {
  max-height: 400px;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #64748b;
}

.message-item {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #f8fafc;
}

.message-item.unread {
  background-color: #f0f9ff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-type {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.message-type.system {
  background: #ffedd5;
  color: #9a3412;
}

.message-type.notification {
  background: #dbeafe;
  color: #1e40af;
}

.message-type.chat {
  background: #f0fdf4;
  color: #166534;
}

.message-time {
  font-size: 12px;
  color: #64748b;
}

.message-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: #1e293b;
}

.message-content {
  font-size: 14px;
  color: #475569;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-sender {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.load-more {
  padding: 16px;
  text-align: center;
}

.load-more-btn {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.load-more-btn:hover:not(:disabled) {
  background: #2563eb;
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
