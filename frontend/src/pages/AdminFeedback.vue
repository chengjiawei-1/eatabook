<template>
  <div class="space-y-6">
    <!-- 顶部标题栏 -->
    <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-2">
      <div>
        <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
          问题反馈管理 <span class="text-amber-500 text-base ml-2">User Voices</span>
        </h1>
        <p class="text-slate-500 text-sm mt-1">
          当前共收到 <span class="text-indigo-500 font-bold">{{ feedbacks.length }}</span> 条反馈，请及时处理。
        </p>
      </div>
    </div>

    <!-- 内容主体 -->
    <!-- 空状态处理 -->
    <div v-if="feedbacks.length === 0" 
         class="py-20 text-center bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-xl shadow-slate-200/50">
      <div class="w-20 h-20 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
        <i class="fa-solid fa-comment-slash text-3xl text-slate-300"></i>
      </div>
      <p class="text-slate-500 font-bold">暂无用户反馈数据</p>
    </div>

    <!-- 反馈列表 -->
    <div v-else class="grid grid-cols-1 gap-6">
      <div
        v-for="feedback in feedbacks"
        :key="feedback.id"
        class="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none transition-all hover:-translate-y-1 group"
      >
        <div class="flex flex-col md:flex-row gap-6">
          <!-- 左侧：用户信息块 -->
          <div class="md:w-64 flex-shrink-0">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-900/30 flex items-center justify-center text-indigo-600 shadow-sm">
                <i class="fa-solid fa-user-tie text-xl"></i>
              </div>
              <div>
                <p class="text-xs font-black text-slate-400 uppercase tracking-widest">用户名</p>
                <h4 class="text-slate-800 dark:text-white font-black truncate">
                  {{ feedback.userInfo?.username || feedback.username || '匿名用户' }}
                </h4>
              </div>
            </div>

            <div class="space-y-2">
              <div class="flex items-center gap-2 text-sm">
                <i class="fa-solid fa-id-badge w-5 text-slate-400"></i>
                <span class="text-slate-500">用户ID:</span>
                <span class="font-bold text-slate-700 dark:text-slate-300">{{ feedback.userId || '--' }}</span>
              </div>
              <div v-if="feedback.userInfo?.phone || feedback.phone" class="flex items-center gap-2 text-sm">
                <i class="fa-solid fa-phone w-5 text-emerald-500"></i>
                <span class="text-slate-700 dark:text-slate-300 truncate">
                  {{ feedback.userInfo?.phone || feedback.phone }}
                </span>
              </div>
              <div v-if="feedback.userInfo?.email || feedback.email" class="flex items-center gap-2 text-sm">
                <i class="fa-solid fa-envelope w-5 text-amber-500"></i>
                <span class="text-slate-700 dark:text-slate-300 truncate">
                  {{ feedback.userInfo?.email || feedback.email }}
                </span>
              </div>
            </div>
          </div>

          <!-- 右侧：反馈内容块 -->
          <div class="flex-1 flex flex-col justify-between">
            <div class="relative">
              <div class="absolute -left-4 top-0 bottom-0 w-1 bg-amber-400/20 rounded-full"></div>
              <p class="text-[11px] font-black text-amber-500 uppercase tracking-widest mb-2">反馈内容 Content</p>
              <div class="bg-slate-50 dark:bg-slate-800/50 rounded-2xl p-5 border border-slate-100 dark:border-slate-800">
                <p class="text-slate-700 dark:text-slate-200 leading-relaxed font-medium">
                  {{ feedback.content }}
                </p>
              </div>
            </div>

            <!-- 底部：时间 -->
            <div class="mt-4 flex justify-end items-center gap-2 text-[11px] font-bold text-slate-400">
              <i class="fa-regular fa-clock"></i>
              <span>提交于 {{ formatTime(feedback.submissionTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页组件样式统一 -->
    <div class="flex justify-center items-center mt-10 gap-3" v-if="totalPages > 1">
      <button
        @click="currentPage--"
        :disabled="currentPage === 1"
        class="w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 disabled:hover:bg-white transition-all shadow-sm"
      >
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      
      <div class="px-5 py-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
        <span class="text-sm font-black text-slate-800 dark:text-white">
          {{ currentPage }} <span class="text-slate-400 font-normal mx-1">/</span> {{ totalPages }}
        </span>
      </div>

      <button
        @click="currentPage++"
        :disabled="currentPage === totalPages"
        class="w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 disabled:hover:bg-white transition-all shadow-sm"
      >
        <i class="fa-solid fa-chevron-right"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import moment from 'moment';
import http from '@/utils/http';
import { getUserInfoAPI } from '@/api/user';

interface FeedbackItem {
 id: number;
 userId?: number;
 content: string;
 submissionTime: string;
 userInfo?: {
   username?: string;
   phone?: string;
   email?: string;
 };
}

const feedbacks = ref<FeedbackItem[]>([]);
const currentPage = ref(1);
const pageSize = 10;
const totalPages = ref(1);
const userInfoMap = ref<Map<number, any>>(new Map());

const fetchUserInfo = async (userId: number) => {
  if (userInfoMap.value.has(userId)) {
    return userInfoMap.value.get(userId);
  }
  
  try {
    const userInfo = await getUserInfoAPI(userId);  
    userInfoMap.value.set(userId, userInfo);
    return userInfo;
  } catch (error) {
    console.error('Error fetching user info:', error);
    return null;
  }
};

const fetchFeedback = async () => {
 try {
   const response: any = await http.get('/api/feedback', {
     params: {
       pageNo: currentPage.value,
       pageSize: pageSize,
     },
   });

   console.log('API返回的数据结构:', response); // 调试信息

   let records: any[] = [];
   let pages: number = 1;

   if (response && response.data && response.data.records) {
     records = response.data.records;
     pages = response.data.pages ?? pages;
   } else if (response && response.list) {
     records = response.list;
     pages = response.pages ?? pages;
   } else if (response && response.records) {
     records = response.records;
     pages = response.pages ?? pages;
   } else if (response && response.data && response.data.list) {
     records = response.data.list;
     pages = response.data.pages ?? pages;
   }

   console.log('处理后的记录:', records); // 调试信息
   
   // 为每个反馈记录查询用户信息
   const feedbacksWithUserInfo = await Promise.all(
     records.map(async (feedback: FeedbackItem) => {
       if (feedback.userId) {
         const userInfo = await fetchUserInfo(feedback.userId);
         return {
           ...feedback,
           userInfo: userInfo || {}
         };
       }
       return feedback;
     })
   );

   feedbacks.value = feedbacksWithUserInfo || [];
   totalPages.value = pages || 1;
 } catch (error) {
   console.error('Error fetching feedback:', error);
 }
};

const formatTime = (time: string) => {
 return moment(time).format('YYYY-MM-DD HH:mm:ss');
};

onMounted(() => {
 fetchFeedback();
});

watch(currentPage, () => {
 fetchFeedback();
});
</script>

<style scoped>
/* 保持与整体后台一致的过渡动画 */
.fade-slide-enter-active, .fade-slide-leave-active { 
  transition: all 0.3s ease; 
}
.fade-slide-enter-from { 
  opacity: 0; 
  transform: translateY(20px); 
}
</style>
