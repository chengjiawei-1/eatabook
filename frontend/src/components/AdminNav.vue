<template>
  <header 
    class="fixed top-0 left-0 right-0 z-50 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md shadow-sm border-b border-gray-200 dark:border-slate-800 transition-colors duration-300"
  >
    <div class="container mx-auto px-4 h-16 flex items-center justify-between gap-4">
      
      <!-- 1. Logo与标题区域 -->
      <RouterLink 
        to="/admin" 
        class="flex items-center gap-3 flex-shrink-0 group hover:opacity-90 transition-opacity"
      >
        <div class="w-9 h-9 bg-gradient-to-br from-amber-500 to-amber-700 rounded-lg flex items-center justify-center shadow-md group-hover:scale-105 transition-transform">
          <i class="fa-solid fa-shield-halved text-white text-sm"></i>
        </div>
        <div class="hidden md:block">
          <span class="block text-lg font-bold text-slate-800 dark:text-slate-100 leading-tight">管理员中心</span>
          <span class="block text-[10px] text-slate-500 dark:text-slate-400 font-mono tracking-wider">ADMIN CONSOLE</span>
        </div>
      </RouterLink>

     <!-- 2. 中间导航菜单 -->
    <nav class="flex-1 flex items-center justify-center mx-4 overflow-visible">
      <div class="flex items-center gap-1 p-1 bg-slate-100/50 dark:bg-slate-800/50 rounded-xl border border-slate-200/50 dark:border-slate-700/50">
        
        <template v-for="item in navConfig" :key="item.name">
          <!-- A. 普通链接 -->
          <RouterLink
            v-if="!item.children"
            :to="item.to"
            :active-class="activeClass"
            :class="[baseLinkClass, $route.path === item.to ? '' : inactiveClass]"
            class="whitespace-nowrap"
          >
            <i :class="['fa-solid', item.icon, 'mr-2 text-xs']"></i>
            {{ item.name }}
          </RouterLink>

          <!-- B. 下拉菜单 -->
          <!-- 修改点：确保 relative 容器 z-index 正确 -->
          <div v-else class="relative group z-20">
            <button
              :class="[
                baseLinkClass, 
                isChildActive(item.children) ? activeClass : inactiveClass
              ]"
              class="whitespace-nowrap flex items-center"
            >
              <i :class="['fa-solid', item.icon, 'mr-2 text-xs']"></i>
              {{ item.name }}
              <i class="fa-solid fa-chevron-down ml-1.5 text-[10px] opacity-50 group-hover:rotate-180 transition-transform"></i>
            </button>

            <!-- 下拉浮层 -->
            <!-- 修改点：top 设为 120% 并增加 py-2，增强触发稳定性 -->
            <div class="absolute top-[110%] left-1/2 -translate-x-1/2 w-44 p-1 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-200 dark:border-slate-700 opacity-0 translate-y-2 pointer-events-none group-hover:opacity-100 group-hover:translate-y-0 group-hover:pointer-events-auto transition-all duration-200">
              <!-- 为了防止鼠标移出间隙导致菜单消失，增加一个透明遮罩层 -->
              <div class="absolute -top-4 left-0 right-0 h-4 bg-transparent"></div>
              
              <RouterLink
                v-for="child in item.children"
                :key="child.to"
                :to="child.to"
                class="flex items-center px-3 py-2.5 rounded-lg text-sm transition-colors mb-px last:mb-0"
                :class="$route.path === child.to 
                  ? 'bg-amber-50 dark:bg-amber-900/20 text-amber-700 dark:text-amber-400 font-semibold' 
                  : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700/50 hover:text-slate-900 dark:hover:text-slate-100'"
              >
                <i :class="['fa-solid', child.icon, 'mr-3 w-4 text-center text-xs opacity-80']"></i>
                {{ child.name }}
              </RouterLink>
            </div>
          </div>
        </template>

      </div>
    </nav>

      <!-- 3. 右侧操作区 -->
      <div class="flex items-center gap-3 flex-shrink-0">
        <!-- 主题切换 -->
        <button
          @click="toggleTheme"
          class="w-9 h-9 flex items-center justify-center rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 transition-colors"
          :title="theme === 'dark' ? '切换亮色' : '切换暗色'"
        >
          <i v-if="theme === 'dark'" class="fa-regular fa-sun text-amber-400"></i>
          <i v-else class="fa-regular fa-moon"></i>
        </button>

        <div class="h-4 w-px bg-slate-300 dark:bg-slate-700 mx-1"></div>

        <!-- 返回前台 -->
        <RouterLink
          to="/"
          class="hidden sm:flex items-center px-3 py-1.5 text-sm font-medium text-slate-600 dark:text-slate-300 hover:text-amber-600 dark:hover:text-amber-400 transition-colors"
        >
          <i class="fa-solid fa-house-chimney mr-2"></i>
          前台
        </RouterLink>

        <!-- 退出登录 -->
        <button
          @click="logout"
          class="flex items-center px-4 py-1.5 rounded-lg bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 hover:bg-red-100 dark:hover:bg-red-900/30 border border-red-200 dark:border-red-900/50 transition-all text-sm font-medium shadow-sm hover:shadow"
        >
          <i class="fa-solid fa-power-off sm:mr-2"></i>
          <span class="hidden sm:inline">退出</span>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useTheme } from "@/composables/useTheme";
import { useAuthStore } from "@/stores/auth";

const $route = useRoute()
const router = useRouter()
const { theme, toggleTheme } = useTheme();
const auth = useAuthStore();

// 样式配置
const baseLinkClass = 'px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-200 flex items-center'
const activeClass = 'bg-white dark:bg-slate-700 text-amber-700 dark:text-amber-400 shadow-sm ring-1 ring-black/5 dark:ring-white/10'
const inactiveClass = 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200 hover:bg-slate-200/50 dark:hover:bg-slate-700/50'

// 重新组织的导航配置
const navConfig = ref([
  {
    name: '控制台',
    to: '/admin',
    icon: 'fa-gauge-high'
  },
  {
    name: '数据大屏',
    to: '/admin/dashboard',
    icon: 'fa-chart-line'
  },
  {
    name: '用户管理',
    to: '/admin/users',
    icon: 'fa-users'
  },
  {
    name: '内容管理', 
    icon: 'fa-box-archive',
    children: [
      { name: '古籍管理', to: '/admin/books', icon: 'fa-book-bookmark' },
      { name: '菜品管理', to: '/admin/dishes', icon: 'fa-utensils' },
      { name: '食材管理', to: '/admin/ingredients', icon: 'fa-leaf' },
      { name: '关联管理', to: '/admin/dish-ingredient-rel', icon: 'fa-link' },
    ]
  },
  {
    name: '论坛管理',
    to: '/admin/forum',
    icon: 'fa-comments'
  },
  // {
  //   name: '系统消息',
  //   to: '/admin/messages',
  //   icon: 'fa-envelope'
  // },
  {
    name: '问题反馈',
    to: '/admin/feedback',
    icon: 'fa-lightbulb' 
  }
])

// 判断子路由是否激活
const isChildActive = (children: any[]) => {
  return children.some(child => $route.path === child.to)
}

const logout = () => {
  if(confirm('确定要退出管理控制台吗？')) {
    auth.logout();
    router.push('/login');
  }
};
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>