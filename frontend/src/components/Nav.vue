<template>
  <header
    class="fixed top-0 left-0 right-0 z-50 transition-all duration-500"
    :class="[
      isScrolled 
        ? 'bg-white/80 dark:bg-slate-900/80 backdrop-blur-lg shadow-lg py-2' 
        : 'bg-amber-50/95 dark:bg-slate-900/95 backdrop-blur-sm py-4 border-b border-amber-200/50 dark:border-slate-800'
    ]"
  >
    <div class="container mx-auto px-4 flex items-center justify-between">
      <!-- Logo: 中式印章感 -->
      <RouterLink to="/" class="flex items-center gap-3 group">
        <div class="relative">
          <div
            class="text-3xl font-bold text-amber-900 dark:text-amber-400 transition-transform duration-500 group-hover:scale-105"
            style="font-family: 'Ma Shan Zheng', cursive"
          >
            古籍食养
          </div>
          <div class="absolute -bottom-1 left-0 w-0 h-0.5 bg-amber-600 transition-all duration-500 group-hover:w-full"></div>
        </div>
        <div class="hidden lg:flex flex-col border-l border-amber-300 dark:border-slate-700 pl-3">
          <span class="text-[10px] uppercase tracking-[0.2em] text-amber-800/60 dark:text-amber-500/60 font-sans">Traditional</span>
          <span class="text-[10px] uppercase tracking-[0.2em] text-amber-800/60 dark:text-amber-500/60 font-sans">Dietary Therapy</span>
        </div>
      </RouterLink>

      <!-- PC端 导航菜单 -->
      <nav class="hidden md:flex items-center gap-8 lg:gap-12">
        <template v-for="link in navLinks" :key="link.to || link.label">
          <div v-if="link.children" class="relative group" @mouseenter="openDropdown(link.label)" @mouseleave="closeDropdown">
            <button
              class="relative py-2 text-sm font-medium transition-colors flex items-center gap-1.5"
              :class="[isActiveDropdown(link.label) ? 'text-amber-700 dark:text-amber-400' : 'text-slate-700 dark:text-slate-300 hover:text-amber-600 dark:hover:text-amber-400']"
              style="font-family: 'Noto Serif SC', serif"
            >
              <i :class="[link.icon, 'text-base opacity-70 group-hover:scale-110 transition-transform']"></i>
              {{ link.label }}
              <i class="fa-solid fa-chevron-down text-[10px] transition-transform duration-300" :class="{ 'rotate-180': activeDropdown === link.label }"></i>
              <span class="absolute bottom-0 left-1/2 -translate-x-1/2 w-0 h-0.5 bg-amber-600 transition-all duration-300 group-hover:w-full"></span>
            </button>
            <transition name="dropdown-slide">
              <div v-show="activeDropdown === link.label" class="absolute top-full left-1/2 -translate-x-1/2 mt-2 w-44 bg-white dark:bg-slate-800 rounded-xl shadow-2xl border border-amber-100 dark:border-slate-700 overflow-hidden z-50">
                <div class="py-2">
                  <RouterLink v-for="child in link.children" :key="child.to" :to="child.to" class="flex items-center gap-3 px-5 py-3 text-sm text-slate-700 dark:text-slate-300 hover:bg-amber-50 dark:hover:bg-slate-700 transition-all group/item" @click="closeDropdown">
                    <i :class="[child.icon, 'text-amber-600 dark:text-amber-500 transition-transform group-hover/item:translate-x-1']"></i>
                    <span style="font-family: 'Noto Serif SC', serif">{{ child.label }}</span>
                  </RouterLink>
                </div>
              </div>
            </transition>
          </div>
          <RouterLink v-else :to="link.to" class="relative py-2 text-sm font-medium transition-colors flex items-center gap-1.5 group" :class="[isActive(link.to) ? 'text-amber-700 dark:text-amber-400' : 'text-slate-700 dark:text-slate-300 hover:text-amber-600 dark:hover:text-amber-400']" style="font-family: 'Noto Serif SC', serif">
            <i :class="[link.icon, 'text-base opacity-70 group-hover:scale-110 transition-transform']"></i>
            {{ link.label }}
            <span class="absolute bottom-0 left-1/2 -translate-x-1/2 h-0.5 bg-amber-600 transition-all duration-300" :class="isActive(link.to) ? 'w-full' : 'w-0 group-hover:w-full'"></span>
          </RouterLink>
        </template>
      </nav>

      <!-- 右侧功能区 -->
      <div class="flex items-center gap-3">
        <!-- 主题切换 -->
        <button @click="toggleTheme" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-amber-100 dark:hover:bg-slate-800 transition-all active:scale-90">
          <i v-if="theme === 'dark'" class="fa-regular fa-sun text-amber-400 text-lg"></i>
          <i v-else class="fa-regular fa-moon text-amber-800 text-lg"></i>
        </button>
        <div class="h-4 w-px bg-slate-300 dark:bg-slate-700 mx-1"></div>
         <!-- 用户区域 -->
        <div v-if="isAuthenticated" class="flex items-center gap-2">
          <!-- 后台管理按钮 (仅管理员可见) -->
          <RouterLink 
            v-if="userRole === 'admin'"
            to="/admin" 
            class="hidden sm:flex items-center px-3 py-1.5 text-sm font-medium text-slate-600 dark:text-slate-300 hover:text-amber-600 dark:hover:text-amber-400 transition-colors group/admin"
          >
            <i class="fa-solid fa-user-shield mr-2 text-xs group-hover/admin:scale-110 transition-transform"></i>
            后台
          </RouterLink>
          
          <!-- 消息通知 -->
        <button @click="goToNotifications" class="relative w-10 h-10 flex items-center justify-center rounded-full text-amber-800 dark:text-amber-300 hover:bg-amber-100 dark:hover:bg-slate-800 transition-all group/notification">
          <div class="relative">
            <!-- 铃铛图标 -->
            <i class="fa-solid fa-bell text-lg transition-all duration-300 group-hover/notification:scale-110"></i>
            
            <!-- 未读消息徽章 -->
            <span v-if="unreadCount > 0" class="absolute -top-1.5 -right-1.5 min-w-[18px] h-[18px] bg-gradient-to-br from-red-500 to-amber-600 text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white dark:border-slate-900 shadow-md shadow-amber-900/30 animate-pulse">
              {{ unreadCount > 99 ? '99+' : unreadCount }}
            </span>
            
            <!-- 优雅的悬浮光环效果 -->
            <span v-if="unreadCount > 0" class="absolute -top-2 -right-2 w-6 h-6 rounded-full bg-red-400/20 animate-ping"></span>
          </div>
        </button>

          <!-- 用户头像下拉 (已补全所有选项) -->
          <div class="relative ml-1">
            <button @click="toggleAvatarDropdown" class="flex items-center gap-2 focus:outline-none p-1 rounded-full hover:bg-amber-100 dark:hover:bg-slate-800 transition-all" ref="avatarButton">
              <div class="w-9 h-9 rounded-full p-0.5 bg-gradient-to-tr from-amber-600 to-orange-300">
                <div class="w-full h-full rounded-full overflow-hidden border-2 border-white dark:border-slate-900">
                  <img :src="userAvatar || defaultAvatar" class="w-full h-full object-cover" @error="handleAvatarError" />
                </div>
              </div>
              <i class="fa-solid fa-caret-down text-[10px] text-amber-600 transition-transform duration-300" :class="{ 'rotate-180': showAvatarDropdown }"></i>
            </button>

            <transition name="pop-in">
              <div v-show="showAvatarDropdown" class="absolute right-0 mt-3 w-64 bg-white dark:bg-slate-800 rounded-2xl shadow-2xl border border-amber-100 dark:border-slate-700 overflow-hidden z-50" ref="avatarDropdown">
                <!-- 用户头部信息 -->
                <div class="p-5 bg-amber-50/50 dark:bg-slate-700/30 border-b border-amber-100 dark:border-slate-700">
                  <div class="flex items-center gap-3">
                    <img :src="userAvatar || defaultAvatar" class="w-12 h-12 rounded-full border-2 border-amber-200" />
                    <div class="overflow-hidden">
                      <p class="font-bold text-amber-900 dark:text-amber-300 truncate">{{ username }}</p>
                      <p class="text-[10px] text-amber-600/70 dark:text-amber-500/70 mt-1 uppercase tracking-tighter">
                        <i class="fa-solid fa-calendar-check mr-1"></i> {{ registerTime }}
                      </p>
                    </div>
                  </div>
                </div>
                
                <!-- 宫格导航区 (主要功能) -->
                <div class="p-2 grid grid-cols-2 gap-1">
                  <RouterLink to="/profile/sidebar?tab=constitution" @click="hideAvatarDropdown" class="flex flex-col items-center gap-1 p-3 rounded-xl hover:bg-amber-50 dark:hover:bg-slate-700 transition-colors group">
                    <i class="fa-solid fa-dna text-amber-600 group-hover:scale-110 transition-transform"></i>
                    <span class="text-[11px] font-medium text-slate-600 dark:text-slate-300">体质档案</span>
                  </RouterLink>
                  <RouterLink to="/profile/sidebar?tab=favorites" @click="hideAvatarDropdown" class="flex flex-col items-center gap-1 p-3 rounded-xl hover:bg-amber-50 dark:hover:bg-slate-700 transition-colors group">
                    <i class="fa-solid fa-heart text-red-400 group-hover:scale-110 transition-transform"></i>
                    <span class="text-[11px] font-medium text-slate-600 dark:text-slate-300">我的收藏</span>
                  </RouterLink>
                  <RouterLink to="/profile/sidebar?tab=posts" @click="hideAvatarDropdown" class="flex flex-col items-center gap-1 p-3 rounded-xl hover:bg-amber-50 dark:hover:bg-slate-700 transition-colors group">
                    <i class="fa-solid fa-pen-to-square text-blue-500 group-hover:scale-110 transition-transform"></i>
                    <span class="text-[11px] font-medium text-slate-600 dark:text-slate-300">我的帖子</span>
                  </RouterLink>
                  <RouterLink to="/profile/sidebar?tab=reports" @click="hideAvatarDropdown" class="flex flex-col items-center gap-1 p-3 rounded-xl hover:bg-amber-50 dark:hover:bg-slate-700 transition-colors group">
                    <i class="fa-solid fa-file-waveform text-emerald-500 group-hover:scale-110 transition-transform"></i>
                    <span class="text-[11px] font-medium text-slate-600 dark:text-slate-300">体质报告</span>
                  </RouterLink>
                </div>

                <!-- 列表操作区 (设置与工具) -->
                <div class="px-2 pb-2 space-y-0.5 border-t border-amber-100 dark:border-slate-700 pt-2">
                  <RouterLink to="/profile/edit" @click="hideAvatarDropdown" class="flex items-center gap-3 px-4 py-2 text-xs text-slate-600 dark:text-slate-300 hover:bg-amber-50 dark:hover:bg-slate-700 rounded-lg transition-colors">
                    <i class="fa-solid fa-user-gear text-slate-400"></i> 编辑资料
                  </RouterLink>
                  <RouterLink to="/physique-test" @click="hideAvatarDropdown" class="flex items-center gap-3 px-4 py-2 text-xs text-slate-600 dark:text-slate-300 hover:bg-amber-50 dark:hover:bg-slate-700 rounded-lg transition-colors">
                    <i class="fa-solid fa-user-gear text-slate-400"></i> 重新测试
                  </RouterLink>
                  <button @click="openFeedback" class="flex items-center gap-3 px-4 py-2 text-xs text-slate-600 dark:text-slate-300 hover:bg-amber-50 dark:hover:bg-slate-700 rounded-lg transition-colors w-full text-left">
                    <i class="fa-solid fa-comment-dots text-slate-400"></i> 平台反馈
                  </button>
                  <div class="border-t border-amber-100 dark:border-slate-700 my-1"></div>
                  <button @click="logout" class="w-full flex items-center gap-3 px-4 py-2 text-xs text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors">
                    <i class="fa-solid fa-power-off"></i> 退出登录
                  </button>
                </div>
              </div>
            </transition>
          </div>
        </div>
        
        <RouterLink v-else to="/login" class="px-6 py-1.5 rounded-full bg-amber-700 text-white hover:bg-amber-800 shadow-md shadow-amber-900/20 transition-all text-sm font-medium active:scale-95">登录</RouterLink>

        <!-- 移动端开关 -->
        <button @click="mobileMenuOpen = !mobileMenuOpen" class="md:hidden w-10 h-10 flex items-center justify-center rounded-full hover:bg-amber-100 dark:hover:bg-slate-800 transition-colors">
          <i :class="mobileMenuOpen ? 'fa-solid fa-xmark' : 'fa-solid fa-bars-staggered'" class="text-amber-900 dark:text-amber-400 text-xl"></i>
        </button>
      </div>
    </div>

    <!-- 移动端抽屉菜单 (已同步所有个人中心选项) -->
    <transition name="drawer">
      <div v-show="mobileMenuOpen" class="fixed inset-0 top-[60px] z-40 bg-white/95 dark:bg-slate-900/95 backdrop-blur-md md:hidden flex flex-col">
        <div class="flex-1 overflow-y-auto p-6 space-y-6">
          <!-- 导航链接 -->
          <div v-for="link in navLinks" :key="link.label">
            <h3 class="text-[10px] uppercase tracking-widest text-amber-600/60 mb-3 px-2 font-bold">{{ link.label }}</h3>
            <div v-if="link.children" class="grid grid-cols-2 gap-3">
              <RouterLink v-for="child in link.children" :key="child.to" :to="child.to" class="flex flex-col items-center gap-2 p-4 rounded-2xl bg-amber-50 dark:bg-slate-800 border border-amber-100/50 dark:border-slate-700" @click="mobileMenuOpen = false">
                <i :class="child.icon" class="text-amber-700 dark:text-amber-400 text-lg"></i>
                <span class="text-xs font-serif font-medium">{{ child.label }}</span>
              </RouterLink>
            </div>
            <RouterLink v-else :to="link.to" class="flex items-center gap-4 p-4 rounded-2xl bg-amber-50 dark:bg-slate-800 border border-amber-100/50 dark:border-slate-700" @click="mobileMenuOpen = false">
              <i :class="link.icon" class="text-amber-700 dark:text-amber-400"></i>
              <span class="text-sm font-serif font-medium">{{ link.label }}</span>
            </RouterLink>
          </div>

          <!-- 已登录用户的额外操作 -->
          <div v-if="isAuthenticated" class="pt-4 border-t border-amber-100">
            <h3 class="text-[10px] uppercase tracking-widest text-amber-600/60 mb-3 px-2 font-bold">个人中心</h3>
            <div class="grid grid-cols-2 gap-3">
              <RouterLink to="/profile/sidebar?tab=constitution" @click="mobileMenuOpen = false" class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800 text-xs text-slate-700 dark:text-slate-300">
                <i class="fa-solid fa-dna text-amber-600"></i> 体质档案
              </RouterLink>
              <RouterLink to="/profile/sidebar?tab=favorites" @click="mobileMenuOpen = false" class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800 text-xs text-slate-700 dark:text-slate-300">
                <i class="fa-solid fa-heart text-red-400"></i> 我的收藏
              </RouterLink>
              <RouterLink to="/profile/sidebar?tab=posts" @click="mobileMenuOpen = false" class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800 text-xs text-slate-700 dark:text-slate-300">
                <i class="fa-solid fa-pen-to-square text-blue-500"></i> 我的帖子
              </RouterLink>
              <RouterLink to="/profile/sidebar?tab=reports" @click="mobileMenuOpen = false" class="flex items-center gap-3 p-3 rounded-xl bg-slate-50 dark:bg-slate-800 text-xs text-slate-700 dark:text-slate-300">
                <i class="fa-solid fa-file-waveform text-emerald-500"></i> 体质报告
              </RouterLink>
            </div>
          </div>
        </div>
        
        <!-- 移动端底部 -->
        <div v-if="isAuthenticated" class="p-6 border-t border-amber-100 dark:border-slate-800 bg-amber-50/50 dark:bg-slate-800/50">
           <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <img :src="userAvatar || defaultAvatar" class="w-10 h-10 rounded-full border-2 border-amber-300" />
                <div class="overflow-hidden">
                  <p class="font-bold text-amber-900 dark:text-amber-300 truncate">{{ username }}</p>
                  <button @click="goToTest" class="text-[10px] text-amber-600">重新测试</button>
                </div>
              </div>
              <button @click="logoutMobile" class="text-red-500 text-sm font-bold">退出登录</button>
           </div>
        </div>
      </div>
    </transition>
  </header>

  <!-- Feedback Modal -->
  <FeedbackModal :is-open="isFeedbackModalOpen" @close="closeFeedbackModal" @submit="handleFeedbackSubmit" />
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from "vue";
import { useTheme } from "@/composables/useTheme";
import { useAuthStore } from "@/stores/auth";
import { useRouter, useRoute } from "vue-router";
import { getUserInfoAPI } from "@/api/user";
import { useInboxStore } from "@/stores/inbox";
import { initWebSocket, closeWebSocket } from "@/utils/websocket";
import FeedbackModal from "./FeedbackModal.vue";
import { submitFeedback } from "@/api/feedback";

const router = useRouter();
const route = useRoute();
const { theme, toggleTheme } = useTheme();
const auth = useAuthStore();
const inboxStore = useInboxStore();

// 滚动逻辑
const isScrolled = ref(false);
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

const navLinks = [
  { label: "首页", to: "/", icon: "fa-solid fa-house-chimney" },
  { label: "体质测试", to: "/physique-test", icon: "fa-solid fa-vial-circle-check" },
  { label: "食养计划", to: "/diet-checkin", icon: "fa-solid fa-calendar-check" },
  {
    label: "食养探索",
    icon: "fa-solid fa-leaf",
    children: [
      { label: "古籍典藏", to: "/books", icon: "fa-solid fa-scroll" },
      { label: "食疗方库", to: "/recipes", icon: "fa-solid fa-mortar-pestle" },
      { label: "知识图谱", to: "/knowledge-graph", icon: "fa-solid fa-circle-nodes" },
    ],
  },
  { label: "用户论坛", to: "/forum", icon: "fa-solid fa-feather-pointed" },
];

const isAuthenticated = computed(() => auth.isAuthenticated);
const unreadCount = computed(() => inboxStore.unreadCount);
const isActive = (path: string) => route.path === path;
const isActiveDropdown = (label: string) => {
  const link = navLinks.find((l) => l.label === label);
  return link?.children?.some((child) => route.path === child.to);
};

const username = ref("");
const userAvatar = ref("");
const registerTime = ref("");
const userRole = ref("user"); // 默认用户角色
const defaultAvatar = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23d97706'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E";

const loadUserInfo = async () => {
  if (!auth.user?.userId) return;
  try {
    const res = await getUserInfoAPI(auth.user.userId);
    if (res) {
      username.value = res.username || "杏林学子";
      userAvatar.value = res.avatarUrl || res.avatar || defaultAvatar;
      registerTime.value = res.registerTime?.split("T")[0] || "不久前";
      userRole.value = res.role || "user"; // 从后端获取角色
    }
  } catch (error) {
    console.error(error);
  }
};

const handleAvatarError = () => { userAvatar.value = defaultAvatar; };
const showAvatarDropdown = ref(false);
const avatarButton = ref<HTMLElement | null>(null);
const avatarDropdown = ref<HTMLElement | null>(null);

const toggleAvatarDropdown = () => { showAvatarDropdown.value = !showAvatarDropdown.value; };
const hideAvatarDropdown = () => { showAvatarDropdown.value = false; };
const goToNotifications = () => { hideAvatarDropdown(); router.push("/notifications"); };

const isFeedbackModalOpen = ref(false);

const openFeedback = () => {
  hideAvatarDropdown();
  isFeedbackModalOpen.value = true;
};

const closeFeedbackModal = () => {
  isFeedbackModalOpen.value = false;
};

const handleFeedbackSubmit = async (feedbackData: any) => {
  try {
    await submitFeedback(feedbackData);
    alert('感谢您的反馈！');
    closeFeedbackModal();
  } catch (error) {
    console.error('Error submitting feedback:', error);
    alert('反馈提交失败，请稍后再试。');
  }
};

const logout = () => {
  hideAvatarDropdown();
  inboxStore.clearMessages();
  closeWebSocket();
  auth.logout();
  router.push("/login");
};

const logoutMobile = () => { mobileMenuOpen.value = false; logout(); };

const handleClickOutside = (event: MouseEvent) => {
  if (avatarDropdown.value && !avatarDropdown.value.contains(event.target as Node) && 
      avatarButton.value && !avatarButton.value.contains(event.target as Node)) {
    hideAvatarDropdown();
  }
};

const activeDropdown = ref<string | null>(null);
const mobileMenuOpen = ref(false);
let dropdownTimeout: any = null;

const openDropdown = (label: string) => {
  if (dropdownTimeout) clearTimeout(dropdownTimeout);
  activeDropdown.value = label;
};
const closeDropdown = () => {
  dropdownTimeout = setTimeout(() => { activeDropdown.value = null; }, 150);
};

watch(isAuthenticated, (newVal) => {
  if (newVal) {
    loadUserInfo();
    inboxStore.loadMessages(true);
    initWebSocket();
  } else {
    closeWebSocket();
  }
}, { immediate: true });

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("scroll", handleScroll);
});
</script>

<style scoped>
/* 复古底纹效果 */
header {
  /* 纸张纹理效果 */
  background-image: 
    radial-gradient(rgba(139, 69, 19, 0.05) 1px, transparent 1px),
    radial-gradient(rgba(139, 69, 19, 0.05) 1px, transparent 1px),
    linear-gradient(to right, rgba(251, 191, 36, 0.03) 0%, rgba(251, 191, 36, 0.05) 100%),
    linear-gradient(to bottom, rgba(251, 191, 36, 0.03) 0%, rgba(251, 191, 36, 0.05) 100%);
  background-size: 
    50px 50px,
    50px 50px,
    100% 100%,
    100% 100%;
  background-position: 
    0 0,
    25px 25px,
    0 0,
    0 0;
  
  /* 叠加层 - 用于主题切换 */
  background-color: transparent;
}

/* 深色主题下的复古纹理 */
dark header {
  background-image: 
    radial-gradient(rgba(251, 191, 36, 0.1) 1px, transparent 1px),
    radial-gradient(rgba(251, 191, 36, 0.1) 1px, transparent 1px),
    linear-gradient(to right, rgba(51, 65, 85, 0.3) 0%, rgba(51, 65, 85, 0.5) 100%),
    linear-gradient(to bottom, rgba(51, 65, 85, 0.3) 0%, rgba(51, 65, 85, 0.5) 100%);
}

/* PC 下拉菜单动画 - 卷轴滑动 */
.dropdown-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}
.dropdown-slide-leave-active {
  transition: all 0.2s ease-in;
}
.dropdown-slide-enter-from {
  opacity: 0;
  transform: translate(-50%, -10px) scaleY(0);
  transform-origin: top;
}

/* 用户头像下拉动画 - 弹出式 */
.pop-in-enter-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.pop-in-leave-active {
  transition: all 0.2s ease-in;
}
.pop-in-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.9);
}

/* 移动端菜单动画 */
.drawer-enter-active, .drawer-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.drawer-enter-from, .drawer-leave-to {
  transform: translateY(-20px);
  opacity: 0;
}

/* 导航项字体 */
.font-serif {
  font-family: 'Noto Serif SC', serif;
}
</style>