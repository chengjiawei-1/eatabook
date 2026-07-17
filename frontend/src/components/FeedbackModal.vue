<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-slate-800 rounded-lg shadow-xl p-6 w-full max-w-md mx-auto">
      <h2 class="text-xl font-semibold mb-4 text-amber-800 dark:text-amber-300">问题反馈与建议</h2>
      <form @submit.prevent="submitFeedbackForm">
        <div class="mb-4">
          <textarea
            id="content"
            v-model="feedback.content"
            rows="6"
            class="w-full p-2 border border-amber-300 dark:border-slate-600 rounded-md bg-amber-50 dark:bg-amber-700 text-amber-900 dark:text-amber-100"
            placeholder="请填写您的宝贵意见或建议..."
            required
          ></textarea>
        </div>
        <div class="flex justify-end space-x-4">
          <button
            type="button"
            @click="$emit('close')"
            class="px-4 py-2 bg-gray-300 dark:bg-slate-600 text-gray-800 dark:text-white rounded-md hover:bg-gray-400 dark:hover:bg-slate-500 transition-colors"
          >
            取消
          </button>
          <button
            type="submit"
            class="px-4 py-2 bg-amber-500 dark:bg-amber-600 text-white rounded-md hover:bg-amber-600 dark:hover:bg-amber-700 transition-colors"
          >
            提交
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { submitFeedback } from '../api/feedback';
import { useAuthStore } from '@/stores/auth';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits(['close']);
const authStore = useAuthStore();

const feedback = ref({
  content: '',
});

const submitFeedbackForm = async () => {
  if (!feedback.value.content) {
    alert('反馈内容不能为空！');
    return;
  }
  try {
    const feedbackData = {
      content: feedback.value.content,
      userId: authStore.user?.userId || 0
    };
    
    await submitFeedback(feedbackData);
    alert('反馈提交成功！');
    feedback.value = { content: '' }; // Reset form
    emit('close');
  } catch (error) {
    console.error('提交反馈失败:', error);
    alert('提交反馈失败，请稍后再试。');
  }
};
</script>

<style scoped>
/* You can add specific styles for the modal here if needed */
</style>