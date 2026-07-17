<template>
  <div class="min-h-screen bg-gray-50 dark:bg-slate-900 transition-colors duration-300">
    <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题 -->
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
          <div>
            <h1 class="text-3xl font-bold text-slate-800 dark:text-amber-50">消息通知管理</h1>
            <p class="text-slate-500 dark:text-slate-400 mt-1">
              查看系统通知记录，监控消息触达情况
            </p>
          </div>
          <button 
            @click="showSendMessageModal = true"
            class="px-5 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-xl shadow-sm hover:shadow-md transition-all flex items-center gap-2"
          >
            <i class="fa-solid fa-paper-plane"></i>
            <span>发送通知</span>
          </button>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div v-for="(stat, index) in statsCards" :key="index" class="bg-white dark:bg-slate-800 rounded-xl p-5 shadow-sm border border-gray-100 dark:border-slate-700 flex items-center justify-between group hover:shadow-md transition-all">
            <div>
              <p class="text-sm text-slate-500 dark:text-slate-400">{{ stat.label }}</p>
              <p class="text-2xl font-bold text-slate-800 dark:text-slate-100 mt-1">{{ stat.value }}</p>
            </div>
            <div :class="['w-12 h-12 rounded-full flex items-center justify-center text-xl transition-transform group-hover:scale-110', stat.bgClass, stat.textClass]">
              <i :class="['fa-solid', stat.icon]"></i>
            </div>
          </div>
        </div>

        <!-- 筛选与搜索 -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-100 dark:border-slate-700 p-5 mb-6">
          <div class="flex flex-col lg:flex-row gap-4">
            <!-- 搜索框 -->
            <div class="flex-1 relative">
              <i class="fa-solid fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索标题、内容..."
                class="w-full pl-10 pr-4 py-2.5 bg-gray-50 dark:bg-slate-700/50 border border-gray-200 dark:border-slate-600 rounded-lg text-slate-700 dark:text-slate-200 focus:outline-none focus:ring-2 focus:ring-amber-500/50 focus:border-amber-500 transition-all"
              />
            </div>
            
            <!-- 筛选下拉框 -->
            <div class="flex flex-wrap gap-3">
              <select v-model="typeFilter" class="filter-select">
                <option value="all">所有类型</option>
                <option value="system">系统公告</option>
                <option value="reminder">提醒通知</option>
                <option value="reply">回复互动</option>
                <option value="like">点赞通知</option>
              </select>
              
              <select v-model="statusFilter" class="filter-select">
                <option value="all">所有状态</option>
                <option value="read">已读</option>
                <option value="unread">未读</option>
              </select>

              <select v-model="sortBy" class="filter-select">
                <option value="createdAt">最新发布</option>
                <option value="type">按类型</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 消息列表表格 -->
        <div class="bg-white dark:bg-slate-800 rounded-xl shadow-sm border border-gray-100 dark:border-slate-700 overflow-hidden">
          <!-- Loading -->
          <div v-if="loading" class="py-20 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500 dark:text-slate-400">正在加载消息记录...</p>
          </div>

          <!-- Empty -->
          <div v-else-if="filteredMessages.length === 0" class="py-20 text-center">
            <div class="bg-gray-50 dark:bg-slate-700/50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4">
              <i class="fa-regular fa-bell-slash text-3xl text-slate-300 dark:text-slate-500"></i>
            </div>
            <p class="text-slate-500 dark:text-slate-400">暂无相关消息</p>
          </div>

          <!-- Table -->
          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr class="bg-gray-50/80 dark:bg-slate-700/50 border-b border-gray-200 dark:border-slate-600 text-slate-500 dark:text-slate-400 text-xs uppercase tracking-wider">
                  <th class="px-6 py-4 font-semibold w-1/3">消息内容</th>
                  <th class="px-6 py-4 font-semibold">接收用户</th>
                  <th class="px-6 py-4 font-semibold">类型/状态</th>
                  <th class="px-6 py-4 font-semibold">发送时间</th>
                  <th class="px-6 py-4 font-semibold text-right">操作</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-100 dark:divide-slate-700">
                <tr 
                  v-for="msg in paginatedMessages" 
                  :key="msg.id"
                  class="group hover:bg-amber-50/50 dark:hover:bg-slate-700/30 transition-colors"
                >
                  <!-- 消息内容 -->
                  <td class="px-6 py-4">
                    <div class="flex items-start gap-3">
                      <div :class="['w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-1', getIconStyle(msg.type).bg]">
                        <i :class="[getIconStyle(msg.type).icon, getIconStyle(msg.type).text]"></i>
                      </div>
                      <div>
                        <div class="font-bold text-slate-800 dark:text-slate-200 mb-1 text-sm line-clamp-1" :title="msg.title">
                          {{ msg.title }}
                        </div>
                        <div class="text-xs text-slate-500 dark:text-slate-400 line-clamp-2" :title="msg.content">
                          {{ msg.content }}
                        </div>
                      </div>
                    </div>
                  </td>

                  <!-- 接收人 -->
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-300">
                      <span class="bg-slate-100 dark:bg-slate-700 px-2 py-0.5 rounded text-xs">ID: {{ msg.userId }}</span>
                    </div>
                  </td>

                  <!-- 类型与状态 -->
                  <td class="px-6 py-4">
                    <div class="flex flex-col gap-1.5 items-start">
                      <span :class="['px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide border', getIconStyle(msg.type).badge]">
                        {{ getTypeName(msg.type) }}
                      </span>
                      <div class="flex items-center text-xs">
                        <span :class="['w-1.5 h-1.5 rounded-full mr-1.5', msg.readStatus ? 'bg-green-500' : 'bg-red-500']"></span>
                        <span :class="msg.readStatus ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'">
                          {{ msg.readStatus ? '已读' : '未读' }}
                        </span>
                      </div>
                    </div>
                  </td>

                  <!-- 时间 -->
                  <td class="px-6 py-4">
                    <div class="text-xs text-slate-500 dark:text-slate-400 font-mono">
                      {{ new Date(msg.createdAt).toLocaleString() }}
                    </div>
                  </td>

                  <!-- 操作 -->
                  <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-2 opacity-80 group-hover:opacity-100 transition-opacity">
                      <button 
                        @click="viewMessageDetail(msg)"
                        class="action-btn text-blue-600 bg-blue-50 hover:bg-blue-100 dark:bg-blue-900/20 dark:hover:bg-blue-900/40"
                        title="详情"
                      >
                        <i class="fa-solid fa-eye"></i>
                      </button>
                      <button 
                        v-if="!msg.readStatus"
                        @click="handleMarkRead(msg.id)"
                        class="action-btn text-green-600 bg-green-50 hover:bg-green-100 dark:bg-green-900/20 dark:hover:bg-green-900/40"
                        title="标记已读"
                      >
                        <i class="fa-solid fa-check-double"></i>
                      </button>
                      <button 
                        @click="handleDelete(msg.id)"
                        class="action-btn text-red-600 bg-red-50 hover:bg-red-100 dark:bg-red-900/20 dark:hover:bg-red-900/40"
                        title="删除"
                      >
                        <i class="fa-solid fa-trash-can"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="px-6 py-4 border-t border-gray-100 dark:border-slate-700 bg-gray-50/50 dark:bg-slate-800 flex items-center justify-between">
            <span class="text-sm text-slate-500 dark:text-slate-400">
              共 {{ filteredMessages.length }} 条记录
            </span>
            <div class="flex gap-2">
              <button 
                @click="prevPage"
                :disabled="currentPage === 1"
                class="pagination-btn"
                :class="{'opacity-50 cursor-not-allowed': currentPage === 1}"
              >
                <i class="fa-solid fa-chevron-left mr-1"></i> 上一页
              </button>
              <button 
                @click="nextPage"
                :disabled="currentPage * pageSize >= filteredMessages.length"
                class="pagination-btn"
                :class="{'opacity-50 cursor-not-allowed': currentPage * pageSize >= filteredMessages.length}"
              >
                下一页 <i class="fa-solid fa-chevron-right ml-1"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import { getNotifications, getUnreadCount, markAsRead, deleteNotification } from '@/api/notification'
import type { Notification } from '@/types/notification'


// 状态
const messages = ref<Notification[]>([])
const loading = ref(false)
const searchQuery = ref('')
const typeFilter = ref('all')
const statusFilter = ref('all')
const sortBy = ref('createdAt')
const currentPage = ref(1)
const pageSize = ref(10)
const showSendMessageModal = ref(false)

// 统计数据
const messageStats = ref({
  total: 0,
  unread: 0,
  today: 0,
  system: 0
  
})
// 监听筛选条件变化，自动重置分页到第一页
watch([searchQuery, statusFilter], () => {
  currentPage.value = 1 // 当任何筛选条件改变时，重置到第一页
})

// 计算属性：统计卡片
const statsCards = computed(() => [
  { label: '消息总数', value: messageStats.value.total, icon: 'fa-envelope', bgClass: 'bg-blue-100 dark:bg-blue-900/30', textClass: 'text-blue-600 dark:text-blue-400' },
  { label: '未读消息', value: messageStats.value.unread, icon: 'fa-envelope-open-text', bgClass: 'bg-red-100 dark:bg-red-900/30', textClass: 'text-red-600 dark:text-red-400' },
  { label: '今日新增', value: messageStats.value.today, icon: 'fa-bell', bgClass: 'bg-green-100 dark:bg-green-900/30', textClass: 'text-green-600 dark:text-green-400' },
  { label: '系统公告', value: messageStats.value.system, icon: 'fa-bullhorn', bgClass: 'bg-amber-100 dark:bg-amber-900/30', textClass: 'text-amber-600 dark:text-amber-400' },
])

// 辅助样式
const getIconStyle = (type: string) => {
  switch (type) {
    case 'system': return { bg: 'bg-amber-100 dark:bg-amber-900/30', text: 'text-amber-600', icon: 'fa-solid fa-bullhorn', badge: 'bg-amber-50 text-amber-700 border-amber-200' }
    case 'reminder': return { bg: 'bg-blue-100 dark:bg-blue-900/30', text: 'text-blue-600', icon: 'fa-solid fa-clock', badge: 'bg-blue-50 text-blue-700 border-blue-200' }
    case 'reply': return { bg: 'bg-green-100 dark:bg-green-900/30', text: 'text-green-600', icon: 'fa-solid fa-comment-dots', badge: 'bg-green-50 text-green-700 border-green-200' }
    case 'like': return { bg: 'bg-pink-100 dark:bg-pink-900/30', text: 'text-pink-600', icon: 'fa-solid fa-heart', badge: 'bg-pink-50 text-pink-700 border-pink-200' }
    default: return { bg: 'bg-gray-100 dark:bg-gray-700', text: 'text-gray-500', icon: 'fa-solid fa-bell', badge: 'bg-gray-50 text-gray-600 border-gray-200' }
  }
}

const getTypeName = (type: string) => {
  const map: Record<string, string> = { system: '系统公告', reminder: '提醒', reply: '回复', like: '点赞', follow: '关注' }
  return map[type] || '通知'
}

// 数据加载
const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getNotifications(100) 
    messages.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 过滤与分页
const filteredMessages = computed(() => {
  let list = messages.value.filter(m => {
    const search = searchQuery.value.toLowerCase()
    const matchSearch = m.title.toLowerCase().includes(search) || m.content.toLowerCase().includes(search)
    const matchType = typeFilter.value === 'all' || m.type === typeFilter.value
    const matchStatus = statusFilter.value === 'all' || 
                        (statusFilter.value === 'read' ? m.readStatus : !m.readStatus)
    return matchSearch && matchType && matchStatus
  })
  
  if (sortBy.value === 'createdAt') {
    list.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  }
  return list
})

const paginatedMessages = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredMessages.value.slice(start, start + pageSize.value)
})

// 操作逻辑
const handleMarkRead = async (id: number) => {
  await markAsRead(id)
  const msg = messages.value.find(m => m.id === id)
  if (msg) msg.readStatus = true
}

const handleDelete = async (id: number) => {
  if (confirm('确定删除此消息吗？')) {
    await deleteNotification(id)
    messages.value = messages.value.filter(m => m.id !== id)
  }
}

const viewMessageDetail = (msg: Notification) => {
  alert(`【${msg.title}】\n${msg.content}`)
}

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value * pageSize.value < filteredMessages.value.length) currentPage.value++ }

onMounted(loadMessages)
</script>

<style scoped>
.filter-select {
  @apply bg-gray-50 dark:bg-slate-700/50 border border-gray-200 dark:border-slate-600 text-slate-700 dark:text-slate-200 rounded-lg px-4 py-2.5 focus:outline-none focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-colors;
}
.action-btn {
  @apply w-8 h-8 rounded-lg flex items-center justify-center transition-all duration-200;
}
.pagination-btn {
  @apply px-4 py-2 bg-white dark:bg-slate-700 border border-gray-200 dark:border-slate-600 text-slate-700 dark:text-slate-200 rounded-lg text-sm font-medium hover:bg-gray-50 dark:hover:bg-slate-600 transition-colors flex items-center;
}
.line-clamp-1 {
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden;
}
.line-clamp-2 {
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
/* 模态框过渡 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>