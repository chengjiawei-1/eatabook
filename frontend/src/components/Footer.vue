<template>
  <footer class="bg-amber-50 dark:bg-slate-900 border-t border-amber-200 dark:border-slate-800 py-8">
    <div class="container mx-auto px-4">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <!-- 关于古籍食养 -->
        <div class="text-center">
          <h3 class="font-serif-title text-xl font-bold mb-4 text-amber-800 dark:text-amber-300">
            古籍食养
          </h3>
          <p class="text-sm text-amber-700 dark:text-amber-200 leading-relaxed mx-auto max-w-xs">
            我们希望让古籍中的食养智慧，
            变成你每天都用得上的吃法建议。
          </p>
        </div>

        <!-- 快速链接 -->
        <div class="text-center">
          <h3 class="font-serif-title text-lg font-bold mb-4 text-amber-800 dark:text-amber-300">
            快速链接
          </h3>
          <ul class="space-y-2 mx-auto inline-block">
            <li>
              <RouterLink
                to="/"
                class="text-sm text-amber-700 dark:text-amber-200 hover:text-amber-500 dark:hover:text-amber-400 transition-colors"
              >
                首页
              </RouterLink>
            </li>
            <li>
              <RouterLink
                to="/books"
                class="text-sm text-amber-700 dark:text-amber-200 hover:text-amber-500 dark:hover:text-amber-400 transition-colors"
              >
                古籍库
              </RouterLink>
            </li>
            <li>
              <RouterLink
                to="/physique-test"
                class="text-sm text-amber-700 dark:text-amber-200 hover:text-amber-500 dark:hover:text-amber-400 transition-colors"
              >
                体质测试
              </RouterLink>
            </li>
            <li>
              <RouterLink
                to="/forum"
                class="text-sm text-amber-700 dark:text-amber-200 hover:text-amber-500 dark:hover:text-amber-400 transition-colors"
              >
                用户论坛
              </RouterLink>
            </li>
          </ul>
        </div>

        <!-- 问题反馈 -->
        <div class="text-center">
          <h3
            class="text-lg font-semibold mb-4 text-amber-800 dark:text-amber-300"
            style="font-family: 'Noto Serif SC', serif"
          >
            问题反馈
          </h3>
          <p class="text-sm text-amber-700 dark:text-amber-200 leading-relaxed mx-auto max-w-xs mb-4">
            遇到问题或有建议？欢迎随时告诉我们。
          </p>
          <button
            @click="openFeedbackModal"
            class="text-sm text-amber-700 dark:text-amber-200 hover:text-amber-500 dark:hover:text-amber-400 transition-colors underline underline-offset-2"
          >
            提交反馈
          </button>
        </div>
      </div>

<!-- 下半部分：备案信息（关键修改点：增加了 border-t 和颜色加深） -->
      <div class="border-t border-amber-200/60 dark:border-slate-800 pt-6">
        <div class="flex flex-col sm:flex-row justify-center items-center gap-y-2 sm:gap-x-4 text-[13px]">
        <!-- 版权 -->
        <span class="text-amber-950/60 dark:text-slate-400">
          © {{ currentYear }} 古籍食养 版权所有
        </span>
        
        <span class="hidden sm:inline w-px h-3 bg-amber-300 dark:bg-slate-700"></span>
        
        <!-- ICP备案号 -->
        <a 
          href="https://beian.miit.gov.cn/" 
          target="_blank" 
          rel="noopener"
          class="text-amber-950/80 dark:text-amber-400 font-medium hover:text-amber-600 transition-colors"
        >
          鄂ICP备2026014861号-1
        </a>

        <span class="hidden sm:inline w-px h-3 bg-amber-300 dark:bg-slate-700"></span>

        <!-- 公安备案号 -->
        <a 
          href="https://www.beian.gov.cn/portal/registerSystemInfo?recordcode=42128102000344号"
          target="_blank" 
          rel="noopener"
          class="inline-flex items-center gap-1 text-amber-950/80 dark:text-amber-400 font-medium hover:text-amber-600 transition-colors"
        >
          <img 
            :src="beianIconUrl" 
            alt="公安备案图标" 
            class="inline-block"
            style="width: 14px; height: auto;"
          />
          <span>鄂公网安备42128102000344号</span>
        </a>
      </div>

        <p class="text-center text-[11px] text-amber-900/40 dark:text-slate-500 mt-2 mb-0">
          免责声明：本网站提供的信息仅供参考，不构成任何医疗建议或诊疗依据。平台部分图片、视频等素材来源于公开网络，仅用于学习与展示，如涉及版权问题，请联系删除。
        </p>
      </div>
    </div>
  </footer>

  <!-- Feedback Modal -->
  <FeedbackModal :is-open="isFeedbackModalOpen" @close="closeFeedbackModal" @submit="handleFeedbackSubmit" />
</template>

<script setup lang="ts">
import { RouterLink } from "vue-router";
import { ref } from 'vue';
import FeedbackModal from './FeedbackModal.vue';
import { submitFeedback } from '../api/feedback';

const currentYear = new Date().getFullYear();
const beianIconUrl = "/images/备案图标.png";

const isFeedbackModalOpen = ref(false);

const openFeedbackModal = () => {
  isFeedbackModalOpen.value = true;
};

const closeFeedbackModal = () => {
  isFeedbackModalOpen.value = false;
};

const handleFeedbackSubmit = async (feedbackData: any) => {
  try {
    await submitFeedback(feedbackData);
    alert('感谢您的反馈！');
  } catch (error) {
    console.error('Error submitting feedback:', error);
    alert('反馈提交失败，请稍后再试。');
  }
};
</script>
