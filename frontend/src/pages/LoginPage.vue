<template>
<div class="min-h-screen flex flex-col bg-cover bg-center bg-no-repeat" :style="loginBackgroundStyle">
  <div class="flex-grow flex items-center justify-center p-4 pt-24">
    <div class="max-w-md w-full bg-white/95 dark:bg-slate-800/95 rounded-2xl shadow-xl p-8 border border-amber-100 dark:border-slate-700 backdrop-blur-sm">
      <!-- 标题部分 -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-amber-800 dark:text-amber-300 mb-2">登录</h1>
      </div>

      <!-- 欢迎回来提示 -->
      <div v-if="showWelcomeMessage" class="mb-6 p-4 bg-amber-50 dark:bg-amber-900/30 border border-amber-200 dark:border-amber-700 rounded-lg text-center">
        <div class="text-amber-700 dark:text-amber-300 text-lg font-medium mb-1">
          🎉 欢迎回来！
        </div>
        <div class="text-amber-600 dark:text-amber-400 text-sm">
          <span v-if="daysSinceLastLogin > 0">
            距离您上次登录已经过去了 <span class="font-bold">{{ daysSinceLastLogin }}</span> 天啦~
          </span>
          <span v-else>您今天已经来过啦，欢迎回来继续学习！</span>
        </div>
      </div>

      <!-- 表单部分 -->
      <div class="space-y-4 mb-6">
        <div>
          <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">账号</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            class="w-full px-4 py-2 bg-amber-50 dark:bg-slate-700 border border-amber-200 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-400"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">密码</label>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
            class="w-full px-4 py-2 bg-amber-50 dark:bg-slate-700 border border-amber-200 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-400 font-mono"
          />
        </div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="mb-4 p-3 bg-red-100 text-red-700 rounded-lg text-sm text-center">
        {{ error }}
      </div>

      <!-- 登录按钮 -->
      <button
        @click="handleLogin"
        :disabled="isLoading"
        class="w-full py-3 bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-lg transition-colors disabled:opacity-50"
      >
        {{ isLoading ? "登录中..." : "登录" }}
      </button>
      
      <div class="mt-4 text-center text-sm">
        <RouterLink to="/register" class="text-amber-600 hover:underline">没有账号？去注册</RouterLink>
      </div>
    </div>
  </div>
 <footer class="fixed bottom-4 w-full text-center px-4 z-10">
  <div class="flex flex-col sm:flex-row justify-center items-center gap-y-1 sm:gap-x-4 text-[12px] font-medium text-amber-900/80 dark:text-slate-400 drop-shadow-sm">
    <!-- 版权 -->
    <span>© 2026 古籍食养</span>
    
    <!-- 分割线 -->
    <span class="hidden sm:inline w-px h-2.5 bg-amber-900/40 dark:bg-slate-600"></span>
    
    <!-- ICP备案号 -->
    <a 
      href="https://beian.miit.gov.cn/" 
      target="_blank" 
      rel="noopener"
      class="text-amber-900 hover:text-amber-700 dark:text-amber-300 transition-colors underline-offset-2 hover:underline"
    >
      鄂ICP备2026014861号-1
    </a>

    <!-- 分割线 -->
    <span class="hidden sm:inline w-px h-2.5 bg-amber-900/40 dark:bg-slate-600"></span>

    <!-- 公安备案号（使用本地图标） -->
    <a 
      href="https://www.beian.gov.cn/portal/registerSystemInfo?recordcode=42128102000344号" 
      target="_blank" 
      rel="noopener"
      class="inline-flex items-center gap-1 text-amber-900 hover:text-amber-700 dark:text-amber-300 transition-colors underline-offset-2 hover:underline"
    >
      <img 
        src="/images/备案图标.png" 
        alt="公安备案图标" 
        class="w-4 h-4"
        style="width: 16px; height: 17px;"
      />
      <span>鄂公网安备42128102000344号</span>
    </a>
  </div>
</footer>
</div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import loginBackground from "@/assets/images/bg.png";

const router = useRouter();
const route = useRoute(); 
const auth = useAuthStore();

const loginBackgroundStyle = {
  backgroundImage: `url(${loginBackground})`,
};

const isLoading = ref(false);
const error = ref("");
const showWelcomeMessage = ref(false);

const form = reactive({
  username: "",
  password: "",
});

// 保存登录前的lastLoginTime
const previousLoginTime = ref<string | undefined>();

// 基于previousLoginTime计算天数
const daysSinceLastLogin = computed(() => {
  if (!previousLoginTime.value) return 0;
  const lastLogin = new Date(previousLoginTime.value);
  const now = new Date();
  const diffMs = now.getTime() - lastLogin.getTime();
  return Math.floor(diffMs / (1000 * 60 * 60 * 24));
});

const handleLogin = async () => {
  if (!form.username || !form.password) {
    error.value = "请输入账号和密码";
    return;
  }
  
  isLoading.value = true;
  error.value = "";
  try {
    const result = await auth.login(form.username, form.password);
    console.log("登录结果：", result);
    // 拿到刚才在 Action 里显式返回的 previousLoginTime
    previousLoginTime.value = result.previousLoginTime;
    
    // 如果有上次登录记录（即不是第一次注册登录的用户）
    if (previousLoginTime.value) {
      showWelcomeMessage.value = true;
      // 延迟跳转，让用户看一眼欢迎消息
      setTimeout(() => {
        navigateAfterLogin();
      }, 1500); // 增加到 1.5s，体感更好
    } else {
      navigateAfterLogin();
    }
  } catch (err: any) {
    error.value = err.message || "登录失败，请检查账号密码";
  } finally {
    isLoading.value = false;
  }
};

// 跳转逻辑
const navigateAfterLogin = () => {
  if (auth.isAdmin) {
    router.replace('/admin');
  } else {
    router.replace('/scroll-animation');
  }
};
</script>


<style scoped>
/* 让密码圆点变粗 */
input[type="password"] {
  font-weight: 900 !important;    /* 最粗的字重 */
  font-size: 18px !important;     /* 略微增大字体使圆点更饱满 */
  line-height: 1 !important;      /* 防止行高增加 */
}

/* 保持 placeholder 字体大小和粗细正常 */
input[type="password"]::placeholder {
  font-size: 1rem !important;
  font-weight: normal !important;
}
</style>
