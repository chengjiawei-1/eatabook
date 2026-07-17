<template>
  <div class="min-h-screen bg-[#fdfbf7] dark:bg-slate-900 pt-24 pb-12 transition-colors duration-300">
    <div class="container mx-auto px-4 max-w-4xl">
      
      <!-- 返回按钮 -->
      <button 
        @click="goBack"
        class="group flex items-center text-gray-500 dark:text-gray-400 hover:text-amber-600 dark:hover:text-amber-400 mb-6 transition-colors text-sm font-medium"
      >
        <div class="w-8 h-8 rounded-full bg-white dark:bg-slate-800 border border-gray-200 dark:border-slate-700 flex items-center justify-center mr-2 group-hover:border-amber-300 dark:group-hover:border-amber-600 transition-colors shadow-sm">
          <i class="fa-solid fa-arrow-left transform group-hover:-translate-x-0.5 transition-transform"></i>
        </div>
        返回上一页
      </button>

      <!-- 页面头部 -->
      <div class="flex flex-col md:flex-row justify-between items-end md:items-center mb-8 gap-4">
        <div>
          <h1 class="text-3xl font-bold text-[#8b4513] dark:text-[#deb887] mb-2" style="font-family: 'Noto Serif SC', serif">
            消息中心
          </h1>
          <p class="text-gray-500 dark:text-gray-400 text-sm">
            您有 <span class="font-bold text-amber-600">{{ unreadCount }}</span> 条未读消息
          </p>
        </div>
        
        <!-- 操作按钮 -->
        <div class="flex gap-3">
          <button
            @click="markAllRead"
            :disabled="unreadCount === 0 || loading"
            class="px-4 py-2 bg-white dark:bg-slate-800 border border-amber-200 dark:border-slate-700 rounded-lg text-amber-800 dark:text-amber-300 text-sm hover:bg-amber-50 dark:hover:bg-slate-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 shadow-sm"
          >
            <i class="fa-solid fa-check-double"></i>
            全部已读
          </button>
        </div>
      </div>

      <!-- 标签筛选 -->
      <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-amber-100 dark:border-slate-700 p-2 mb-6 flex gap-2 overflow-x-auto">
        <button
          v-for="tab in filters"
          :key="tab.value"
          @click="currentFilter = tab.value"
          class="px-4 py-2 rounded-lg text-sm font-medium transition-all whitespace-nowrap"
          :class="currentFilter === tab.value 
            ? 'bg-amber-100 text-amber-900 dark:bg-amber-900/30 dark:text-amber-100' 
            : 'text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-slate-700'"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- 消息列表 -->
      <div class="space-y-4">
        <!-- 加载中 -->
        <div v-if="loading && messages.length === 0" class="py-12 text-center">
          <i class="fa-solid fa-spinner fa-spin text-3xl text-amber-400 mb-3"></i>
          <p class="text-gray-500">正在获取消息...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredMessages.length === 0" class="py-16 text-center bg-white dark:bg-slate-800 rounded-2xl border border-dashed border-amber-200 dark:border-slate-700">
          <div class="w-16 h-16 bg-amber-50 dark:bg-slate-700 rounded-full flex items-center justify-center mx-auto mb-4 text-amber-300">
            <i class="fa-regular fa-bell-slash text-3xl"></i>
          </div>
          <p class="text-gray-500 dark:text-gray-400">暂无相关消息</p>
        </div>

        <!-- 列表项 -->
        <transition-group name="list">
          <div
            v-for="msg in filteredMessages"
            :key="msg.id"
            class="group relative bg-white dark:bg-slate-800 rounded-xl p-5 border border-amber-100 dark:border-slate-700 shadow-sm hover:shadow-md transition-all duration-200"
            :class="{ 'bg-amber-50/30 dark:bg-slate-800/50': msg.readStatus }"
          >
            <!-- 左侧红点 (未读指示) -->
            <div v-if="!msg.readStatus" class="absolute left-0 top-6 w-1 h-8 bg-red-500 rounded-r-full"></div>

            <div class="flex items-start gap-4">
              <!-- 图标 -->
              <div 
                class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
                :class="getTypeStyle(msg.type)"
              >
                <i :class="getTypeIcon(msg.type)"></i>
              </div>

              <!-- 内容 -->
              <div class="flex-grow min-w-0">
                <div class="flex justify-between items-start mb-1">
                  <h3 
                    class="font-bold text-gray-800 dark:text-gray-200 truncate pr-4"
                    :class="{ 'text-gray-500 dark:text-gray-500 font-normal': msg.readStatus }"
                  >
                    {{ msg.title }}
                  </h3>
                  <span class="text-xs text-gray-400 flex-shrink-0 whitespace-nowrap ml-2">
                    {{ formatTime(msg.createdAt) }}
                  </span>
                </div>
                
                <p 
                  class="text-sm text-gray-600 dark:text-gray-400 leading-relaxed mb-3"
                  :class="{ 'line-through opacity-70': msg.readStatus && false }" 
                >
                  {{ msg.content }}
                </p>

                <!-- 底部操作栏 -->
                <div class="flex items-center justify-between pt-2 border-t border-gray-100 dark:border-slate-700/50">
                  <div class="flex gap-2">
                    <!-- 修改后的逻辑：只包含点赞、收藏、评论、回复 -->
                    <button 
                      v-if="['post_like','post_favorite', 'comment', 'comment_reply'].includes(msg.type)"
                      @click="handleNavigate(msg)"
                      class="text-xs text-amber-600 dark:text-amber-400 hover:underline flex items-center gap-1"
                    >
                      查看详情 <i class="fa-solid fa-arrow-right"></i>
                    </button>
                  </div>
                  
                  <div class="flex gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button 
                      v-if="!msg.readStatus"
                      @click="handleMarkRead(msg.id)"
                      class="text-xs text-amber-600 hover:text-amber-800 dark:text-amber-400"
                      title="标记已读"
                    >
                      <i class="fa-regular fa-envelope-open mr-1"></i> 已读
                    </button>
                    <button 
                      @click="handleDelete(msg.id)"
                      class="text-xs text-gray-400 hover:text-red-500 transition-colors"
                      title="删除"
                    >
                      <i class="fa-solid fa-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition-group>
        
        <!-- 加载更多 -->
        <div v-if="filteredMessages.length > 0 && !loading" class="text-center pt-4 pb-8">
          <p class="text-xs text-gray-400">仅显示最近消息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useInboxStore } from '@/stores/inbox';
import type { Notification } from '@/types/notification';
import { deleteNotification } from '@/api/notification';

const router = useRouter();
const store = useInboxStore();

// 返回逻辑
const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/'); // 如果没有历史记录，回首页
  }
};

// 状态
const currentFilter = ref('all');
const filters = [
  { label: '全部消息', value: 'all' },
  { label: '仅看未读', value: 'unread' }
];

// 计算属性
const loading = computed(() => store.isLoading);
const messages = computed(() => store.messages);
const unreadCount = computed(() => store.unreadCount);

// 过滤后的消息列表
const filteredMessages = computed(() => {
  let list = messages.value;
  
  if (currentFilter.value === 'unread') {
    list = list.filter(m => !m.readStatus);
  } else if (currentFilter.value !== 'all') {
    list = list.filter(m => m.type === currentFilter.value);
  }
  return list;
});

// 样式辅助
const getTypeStyle = (type: string) => {
  const styles: Record<string, string> = {
    'reminder': 'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400',
    'post_like': 'bg-red-100 text-red-500 dark:bg-red-900/30 dark:text-red-400',
    'post_favorite': 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/30 dark:text-yellow-400',
    'comment': 'bg-green-100 text-green-600 dark:bg-green-900/30 dark:text-green-400',
    'comment_reply': 'bg-teal-100 text-teal-600 dark:bg-teal-900/30 dark:text-teal-400',
    'follow': 'bg-purple-100 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400'
  };
  return styles[type] || 'bg-gray-100 text-gray-500';
};

const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    'reminder': 'fa-regular fa-clock',
    'post_like': 'fa-solid fa-heart',
    'post_favorite': 'fa-solid fa-star',
    'comment': 'fa-solid fa-comment-dots',
    'comment_reply': 'fa-solid fa-comments',
    'follow': 'fa-solid fa-user-plus'
  };
  return icons[type] || 'fa-solid fa-bell';
};

const formatTime = (timeStr: string) => {
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  if (diff < 3600 * 1000) {
    return Math.ceil(diff / 60000) + '分钟前';
  }
  if (diff < 24 * 3600 * 1000) {
    return Math.ceil(diff / (3600 * 1000)) + '小时前';
  }
  return date.toLocaleDateString() + ' ' + date.getHours().toString().padStart(2,'0') + ':' + date.getMinutes().toString().padStart(2,'0');
};

// 操作逻辑

onMounted(() => {
  store.loadMessages(true);
});

const handleMarkRead = async (id: number) => {
  await store.markAsRead(id);
};

const markAllRead = async () => {
  if (confirm('确定将所有消息标记为已读吗？')) {
    await store.markAllAsRead();
  }
};

const handleNavigate = (msg: Notification) => {
  // 标记已读
  if (!msg.readStatus) {
    store.markAsRead(msg.id);
  }
  if (msg.relatedId) {
    // 拼接
    router.push(`/forum-post-detail/${msg.relatedId}`);
  }
};


const handleDelete = async (id: number) => {
  if (!confirm('确定删除这条消息吗？')) return;
  try {
    await deleteNotification(id);
    const index = store.messages.findIndex(m => m.id === id);
    if (index > -1) {
      store.messages.splice(index, 1);
      store.loadMessages(true);
    }
  } catch (e) {
    console.error('删除失败', e);
  }
};
</script>

<style scoped>
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>