<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-slate-900">
    <Nav />
    
    <main class="flex-grow pt-24 pb-16">
      <div class="container mx-auto px-4">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-[#8b4513] dark:text-[#deb887] font-serif">
            个人中心
          </h1>
          <p class="text-gray-600 dark:text-gray-400 mt-2">
            管理您的个人信息和内容
          </p>
        </div>

        <div class="flex flex-col lg:flex-row gap-8">
          <!-- 侧边栏导航 -->
          <aside class="lg:w-64 flex-shrink-0">
            <div class="bg-white dark:bg-slate-800 rounded-xl shadow-lg border border-[#f5deb3] dark:border-slate-700 p-6 sticky top-32">
              <!-- 用户摘要 -->
              <div class="flex items-center space-x-4 mb-8 pb-6 border-b border-[#f5deb3] dark:border-slate-700">
                <div class="w-16 h-16 rounded-full overflow-hidden border-2 border-[#e6cba8] dark:border-slate-600">
                  <img 
                    :src="profile?.avatarUrl || defaultAvatar" 
                    :alt="profile?.username || '用户'"
                    class="w-full h-full object-cover"
                  />
                </div>
                <div>
                  <h3 class="font-bold text-[#5c3a21] dark:text-[#deb887]">{{ profile?.username || '加载中...' }}</h3>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    <i class="fa-solid fa-calendar-alt mr-1"></i>
                    {{ profile?.registerTime || '未知时间' }}
                  </p>
                </div>
              </div>

              <!-- 导航菜单 -->
              <nav class="space-y-2">
                <router-link
                  v-for="item in navItems"
                  :key="item.key"
                  :to="`/profile/sidebar?tab=${item.key}`"
                  class="flex items-center space-x-3 px-4 py-3 rounded-lg transition-colors"
                  :class="{
                    'bg-[#f5deb3] dark:bg-slate-700 text-[#8b4513] dark:text-[#deb887]': activeTab === item.key,
                    'text-gray-700 dark:text-gray-300 hover:bg-[#f5deb3]/30 dark:hover:bg-slate-700/50': activeTab !== item.key
                  }"
                  @click="setActiveTab(item.key)"
                >
                  <div class="w-8 h-8 rounded-lg flex items-center justify-center"
                    :class="{
                      'bg-[#8b4513]/20 dark:bg-[#deb887]/20 text-[#8b4513] dark:text-[#deb887]': activeTab === item.key,
                      'bg-gray-100 dark:bg-slate-700 text-gray-600 dark:text-gray-400': activeTab !== item.key
                    }"
                  >
                    <i :class="['fa-solid', item.icon]"></i>
                  </div>
                  <span class="font-medium">{{ item.label }}</span>
                </router-link>

                <!-- 快捷操作 -->
                <div class="pt-6 border-t border-[#f5deb3] dark:border-slate-700">
                  <button
                    @click="goToEditProfile"
                    class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-[#f5deb3]/30 dark:hover:bg-slate-700/50 w-full transition-colors"
                  >
                    <div class="w-8 h-8 rounded-lg bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center">
                      <i class="fa-solid fa-pen-to-square text-amber-600 dark:text-amber-400"></i>
                    </div>
                    <span class="font-medium">编辑资料</span>
                  </button>

                  <button
                    @click="retakeTest"
                    class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-[#f5deb3]/30 dark:hover:bg-slate-700/50 w-full transition-colors mt-2"
                  >
                    <div class="w-8 h-8 rounded-lg bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
                      <i class="fa-solid fa-heart-pulse text-red-600 dark:text-red-400"></i>
                    </div>
                    <span class="font-medium">重新测试</span>
                  </button>
                </div>
              </nav>
            </div>
          </aside>

          <!-- 主要内容区域 -->
          <div class="flex-grow">
            <!-- 根据当前标签显示不同内容 -->
            <div v-show="activeTab === 'constitution'" class="animate-fade-in">
              <ConstitutionTab />
            </div>

            <div v-show="activeTab === 'favorites'" class="animate-fade-in">
              <FavoritesTab />
            </div>

            <div v-show="activeTab === 'reports'" class="animate-fade-in">
              <ReportsTab />
            </div>

            <div v-show="activeTab === 'posts'" class="animate-fade-in">
              <MySocialPost />
            </div>
          </div>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUserInfoAPI } from '@/api/user'
import Nav from '@/components/Nav.vue'
import Footer from '@/components/Footer.vue'
import ConstitutionTab from '@/components/profile/ConstitutionTab.vue'
import FavoritesTab from '@/components/profile/FavoritesTab.vue'
import ReportsTab from '@/components/profile/ReportsTab.vue'
import MySocialPost from '@/components/profile/MySocialPostTab.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

// 导航项
const navItems = [
  { key: 'constitution', label: '体质档案', icon: 'fa-chart-line' },
  { key: 'favorites', label: '我的收藏', icon: 'fa-heart' },
  { key: 'posts', label: '我的帖子', icon: 'fa-pen-to-square' },
  { key: 'reports', label: '体质报告', icon: 'fa-file-alt' }
]

// 活动标签
const activeTab = ref('constitution')

// 用户信息 - 使用可选链操作符避免undefined错误
const profile = ref({
  username: '加载中...',
  avatarUrl: '',
  registerTime: ''
})

const defaultAvatar = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23cccccc'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E"

// 判断是否已认证
const isAuthenticated = computed(() => auth.isAuthenticated)

// 从URL查询参数设置活动标签
const setActiveTabFromQuery = () => {
  const tab = route.query.tab as string
  if (tab && navItems.some(item => item.key === tab)) {
    activeTab.value = tab
  }
}

// 设置活动标签
const setActiveTab = (key: string) => {
  activeTab.value = key
  router.push({ query: { tab: key } })
}
const loadProfile = async () => {
  if (!auth.user?.userId) return;
  
  try {
    // 这里的 response 就是你日志里打印出来的那个对象
    const response = await getUserInfoAPI(auth.user.userId)
    console.log('用户信息API响应:', response)
    
    // 判断 response 是否包含 username 字段
    if (response && response.username) {
      // 使用 response 赋值
      profile.value = {
        username: response.username,
        avatarUrl: response.avatarUrl || defaultAvatar,
        // 处理时间格式
        registerTime: response.registerTime 
          ? response.registerTime.replace('T', ' ').split(' ')[0]
          : '未知时间'
      }
      console.log('成功填充 profile 对象:', profile.value)
    } else {
      console.warn('API 返回对象中不包含 username 字段')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 跳转到编辑资料
const goToEditProfile = () => {
  router.push('/profile/edit')
}

// 重新测试
const retakeTest = () => {
  router.push('/test')
}

// 监听路由变化
watch(() => route.query.tab, () => {
  setActiveTabFromQuery()
})

// 监听登录状态变化，自动加载用户信息（与Nav.vue保持一致）
watch(
  isAuthenticated,
  (newVal) => {
    if (newVal) {
      console.log('检测到用户已登录，加载侧边栏用户信息')
      loadProfile()
    } else {
      console.log('用户未登录，清空侧边栏用户信息')
      Object.assign(profile.value, {
        username: '用户',
        avatarUrl: defaultAvatar,
        registerTime: '未知时间'
      })
    }
  },
  { immediate: true } // 立即执行一次，确保初始化时也加载
)

// 初始化
onMounted(() => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }
  
  setActiveTabFromQuery()
})
</script>


<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>