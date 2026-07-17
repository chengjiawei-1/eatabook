<template>
  <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center">
    <!-- 背景遮罩 -->
    <div
      class="absolute inset-0 bg-black/40 backdrop-blur-md transition-opacity"
      @click="handleClose"
    ></div>

    <!-- 弹窗主体 -->
    <div
      class="relative w-[90%] max-w-md mx-4
             rounded-2xl p-8
             bg-white/80 dark:bg-slate-800/80
             backdrop-blur-xl
             shadow-[0_30px_80px_-20px_rgba(0,0,0,0.35)]
             animate-dialog"
    >
      <!-- 顶部装饰光晕 -->
      <div
        class="absolute -top-16 left-1/2 -translate-x-1/2
               w-64 h-64 rounded-full
               bg-amber-400/30 blur-3xl pointer-events-none"
      ></div>

      <!-- 标题 -->
      <h3
        class="relative text-xl font-bold text-center
               text-amber-700 dark:text-amber-300 mb-4"
      >
        🌿 体质测试提醒
      </h3>

      <!-- 文案 -->
      <p
        class="relative text-center leading-relaxed
               text-amber-800/80 dark:text-amber-200/80
               mb-10"
      >
        {{ message }}
      </p>

      <!-- 按钮组 -->
      <div class="relative flex justify-center gap-5">
        <!-- 主按钮 -->
        <button
          @click="handleStartTest"
          class="relative px-7 py-3
                 rounded-full font-bold text-white
                 bg-gradient-to-r from-amber-600 to-amber-700
                 shadow-[0_12px_28px_-8px_rgba(217,119,6,0.6)]
                 transition-all duration-300
                 hover:from-amber-500 hover:to-amber-600
                 hover:-translate-y-1 active:scale-95
                 animate-cta"
        >
          开始体质测试
        </button>

        <!-- 次按钮 -->
        <button
          @click="handleClose"
          class="px-6 py-3 rounded-full
                 font-medium
                 text-amber-700 dark:text-amber-200
                 bg-amber-100/60 dark:bg-slate-700/60
                 backdrop-blur
                 transition-all duration-300
                 hover:bg-amber-200/70 dark:hover:bg-slate-600/70
                 hover:-translate-y-0.5 active:scale-95"
        >
          知道了
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: '您近7天还未进行体质测试哟~请先进行体质测试'
  }
})

const emit = defineEmits(['close', 'go-to-test'])

const handleStartTest = () => {
  emit('go-to-test')
  router.push('/physique-test')
}

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
/* 弹窗进场动画 */
@keyframes dialog {
  0% {
    opacity: 0;
    transform: scale(0.92) translateY(30px);
  }
  60% {
    opacity: 1;
    transform: scale(1.03) translateY(-4px);
  }
  100% {
    transform: scale(1) translateY(0);
  }
}

.animate-dialog {
  animation: dialog 0.45s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* CTA 呼吸感 */
@keyframes ctaPulse {
  0%, 100% {
    box-shadow: 0 12px 28px -8px rgba(217, 119, 6, 0.6);
  }
  50% {
    box-shadow: 0 18px 40px -8px rgba(217, 119, 6, 0.85);
  }
}

.animate-cta {
  animation: ctaPulse 1.8s ease-in-out infinite;
}
</style>
