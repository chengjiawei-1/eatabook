<template>
  <!-- 容器：确保背景色统一，高度最小为全屏 -->
  <div class="min-h-screen flex flex-col bg-[#fdfcf0] dark:bg-slate-900">
    <Nav />

    <!-- 增加 pt-32 解决导航栏遮挡，flex-grow 撑开空间 -->
    <main class="flex-grow pt-32 pb-8 flex items-center">
      <div class="container mx-auto px-4">
        
        <!-- 标题：保持原有的古风样式，缩小底部边距 -->
        <div 
          v-motion="{ initial: { opacity: 0, y: -20 }, enter: { opacity: 1, y: 0 } }"
          class="text-center mb-6"
        >
          <h1 class="text-3xl font-bold text-[#8b4513] dark:text-[#deb887]" style="font-family: 'Noto Serif SC', serif">
            编辑个人资料
          </h1>
          <div class="w-32 h-1 bg-[#c19a6b] mx-auto mt-3 rounded-full"></div>
        </div>

        <!-- 卡片主体：max-w-4xl 适中宽度 -->
        <div
          v-motion="{ initial: { opacity: 0, y: 20 }, enter: { opacity: 1, y: 0 } }"
          class="max-w-4xl mx-auto bg-white dark:bg-slate-800 rounded-xl shadow-lg overflow-hidden border border-[#f5deb3] dark:border-slate-700"
        >
          <div class="bg-gradient-to-r from-[#8b4513] to-[#c19a6b] h-1.5"></div>

          <!-- 左右分栏布局：利用空间，减少垂直滚动 -->
          <div class="p-6 md:p-10 flex flex-col md:flex-row gap-10">
            
            <!-- 左侧：头像与 ID 区域 (1/3) -->
            <div class="md:w-1/3 flex flex-col items-center justify-center border-b md:border-b-0 md:border-r border-[#f5deb3]/30 pb-6 md:pb-0 md:pr-10">
              <div class="relative group">
                <img 
                  :src="formData.avatarUrl || defaultAvatar" 
                  alt="用户头像" 
                  class="w-36 h-36 rounded-full object-cover border-4 border-[#f5deb3] shadow-md bg-gray-50"
                  @error="handleImageError"
                />
                <label class="absolute bottom-1 right-1 bg-[#8b4513] hover:bg-[#a52a2a] text-white p-2.5 rounded-full cursor-pointer shadow-lg transition-all">
                  <i class="fa-solid fa-camera"></i>
                  <input type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
                </label>
              </div>
              <p class="text-xs text-gray-500 mt-3">点击相机更换头像</p>

              <!-- 您的 User ID (只读) 移到头像下方，节省横向空间 -->
              <div class="mt-8 w-full bg-[#fdfcf0] dark:bg-slate-700/50 p-3 rounded-lg border border-[#f5deb3]/50 text-center">
                <label class="block text-[10px] uppercase tracking-wider text-[#8b4513]/60 mb-1">User ID</label>
                <span class="font-mono text-gray-600 dark:text-gray-300">#{{ formData.userId }}</span>
              </div>
            </div>

            <!-- 右侧：表单区域 (2/3) -->
            <div class="md:w-2/3">
              <form @submit.prevent="handleSubmit" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                
                <!-- Username (占据两列) -->
                <div class="md:col-span-2">
                  <label class="block text-sm font-bold text-[#8b4513] dark:text-[#deb887] mb-1.5">用户名</label>
                  <input 
                    v-model="formData.username"
                    type="text"
                    required
                    class="w-full px-4 py-2.5 bg-white dark:bg-slate-700 border border-[#f5deb3] dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-[#c19a6b] outline-none transition-all"
                  />
                </div>

                <!-- Password (占据两列) -->
                <div class="md:col-span-2">
                  <label class="block text-sm font-bold text-[#8b4513] dark:text-[#deb887] mb-1.5">
                    新密码 <span class="text-xs text-gray-400 font-normal">(留空则不修改)</span>
                  </label>
                  <div class="relative">
                    <input 
                      v-model="passwordInput"
                      :type="showPassword ? 'text' : 'password'"
                      placeholder="请输入新密码"
                      class="w-full px-4 py-2.5 bg-white dark:bg-slate-700 border border-[#f5deb3] dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-[#c19a6b] outline-none"
                    />
                    <button type="button" @click="showPassword = !showPassword" class="absolute right-3 top-2.5 text-[#8b4513] dark:text-[#deb887]">
                      <i :class="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"></i>
                    </button>
                  </div>
                </div>

                <!-- Phone -->
                <div>
                  <label class="block text-sm font-bold text-[#8b4513] dark:text-[#deb887] mb-1.5">手机号</label>
                  <input 
                    v-model="formData.phone"
                    type="tel"
                    required
                    class="w-full px-4 py-2.5 bg-white dark:bg-slate-700 border border-[#f5deb3] dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-[#c19a6b] outline-none"
                  />
                  <p v-if="!isPhoneValid" class="text-[10px] text-red-500 mt-1">请输入11位有效手机号</p>
                </div>

                <!-- Email -->
                <div>
                  <label class="block text-sm font-bold text-[#8b4513] dark:text-[#deb887] mb-1.5">邮箱</label>
                  <input 
                    v-model="formData.email"
                    type="email"
                    required
                    class="w-full px-4 py-2.5 bg-white dark:bg-slate-700 border border-[#f5deb3] dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-[#c19a6b] outline-none"
                  />
                </div>

                <!-- 新增：籍贯 -->
                <div>
                  <label class="block text-sm font-bold text-[#8b4513] dark:text-[#deb887] mb-1.5">籍贯</label>
                  <input 
                    v-model="formData.nativePlace"
                    type="text"
                    placeholder="请输入您的籍贯"
                    class="w-full px-4 py-2.5 bg-white dark:bg-slate-700 border border-[#f5deb3] dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-[#c19a6b] outline-none"
                  />
                </div>

                <!-- 城市 -->
                <div>
                  <label class="block text-sm font-bold text-[#8b4513] dark:text-[#deb887] mb-1.5">所在城市</label>
                  <input 
                    v-model="formData.city"
                    type="text"
                    placeholder="请输入您所在的城市"
                    class="w-full px-4 py-2.5 bg-white dark:bg-slate-700 border border-[#f5deb3] dark:border-slate-600 rounded-lg focus:ring-2 focus:ring-[#c19a6b] outline-none"
                  />
                </div>

                <!-- 按钮组 -->
                <div class="md:col-span-2 flex justify-end gap-4 pt-4">
                  <button 
                    type="button"
                    @click="handleCancel"
                    class="px-6 py-2.5 text-sm font-medium text-[#8b4513] bg-[#f5deb3] hover:bg-[#d2b48c] rounded-lg transition-colors"
                  >
                    取消
                  </button>
                  <button 
                    type="submit"
                    :disabled="!isFormValid || isSubmitting"
                    class="px-8 py-2.5 text-sm font-medium text-white bg-[#8b4513] hover:bg-[#a52a2a] disabled:bg-gray-300 rounded-lg transition-all flex items-center"
                  >
                    <i :class="isSubmitting ? 'fa-solid fa-spinner fa-spin' : 'fa-solid fa-save'" class="mr-2"></i>
                    {{ isSubmitting ? '保存中...' : '保存修改' }}
                  </button>
                </div>
              </form>
            </div>

          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import { getAvatarUrl } from '@/utils/imageUtils';

// 引入您的 Store 和 API
import { useAuthStore } from "@/stores/auth";
import { getUserInfoAPI, updateUserAPI, updatePasswordAPI, uploadAvatarAPI } from "@/api/user";

const router = useRouter();
const authStore = useAuthStore();
const currentUserId = computed(() => authStore.user?.userId);

const defaultAvatar = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IArs4c6QAAAAtJREFUGFdjYAACAAAFAAGq1chRAAAAAElFTkSuQmCC";

const formData = ref({
  userId: '',     
  username: '',   
  phone: '',      
  email: '',      
  avatarUrl: '',    
  nativePlace: '', 
  city: ''          
});

const passwordInput = ref('');
const showPassword = ref(false);
const isSubmitting = ref(false);

const isPhoneValid = computed(() => {
  const phoneRegex = /^1[3-9]\d{9}$/;
  return phoneRegex.test(formData.value.phone);
});

const isFormValid = computed(() => {
  return formData.value.username?.trim() !== '' && 
         isPhoneValid.value && 
         formData.value.email?.trim() !== '';
});

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement;
  target.src = "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
};

// 加载用户真实数据
const loadUserProfile = async () => {
  if (!currentUserId.value) {
    alert("未登录，请先登录");
    router.push("/login");
    return;
  }
  
  try {
    const res = await getUserInfoAPI(currentUserId.value);
    if (res && res.userId) {
      formData.value = {
        userId: res.userId.toString(),
        username: res.username,
        phone: res.phone || '',
        email: res.email || '',
        avatarUrl: res.avatarUrl || '',
        nativePlace: res.nativePlace || '',  
        city: res.city || ''                 
      };
    }
  } catch (error) {
    console.error("获取用户信息失败", error);
  }
};

// 处理头像上传
const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 检查文件大小（限制为5MB）
  if (file.size > 5 * 1024 * 1024) {
    alert("图片大小不能超过5MB");
    return;
  }

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    alert("请选择图片文件");
    return;
  }

  const uploadData = new FormData();
  uploadData.append('file', file);
  uploadData.append('userId', formData.value.userId);

  try {
    const res = await uploadAvatarAPI(uploadData);
    if (res && res.code === 200 && res.url) {
      formData.value.avatarUrl = getAvatarUrl(res.url);  // 使用统一的图片处理函数
      alert("头像上传成功");
    } else {
      alert(res?.msg || "头像上传失败");
    }
  } catch (error) {
    console.error("头像上传失败:", error);
    alert("头像上传失败，请检查文件格式或联系管理员");
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!isFormValid.value) return;
  isSubmitting.value = true;
  
  try {
    const updatePayload = {
      userId: parseInt(formData.value.userId),
      username: formData.value.username,
      phone: formData.value.phone,
      email: formData.value.email,
      avatarUrl: formData.value.avatarUrl,
      nativePlace: formData.value.nativePlace, // 新增：籍贯
      city: formData.value.city                 // 新增：城市
    };

    await updateUserAPI(updatePayload);

    if (passwordInput.value.trim()) {
      await updatePasswordAPI(formData.value.userId, passwordInput.value);
    }

    alert('资料修改成功！');
    router.push({ path: '/profile/sidebar', query: { tab: 'constitution' } });
  } catch (error) {
    console.error('保存失败:', error);
    alert('保存失败！');
  } finally {
    isSubmitting.value = false;
  }
};

const handleCancel = () => {
  router.push({ path: '/profile/sidebar', query: { tab: 'constitution' } });
};

onMounted(() => {
  loadUserProfile();
});
</script>

<style scoped>
/* 针对小屏幕高度的微调，确保不滚动 */
@media (max-height: 850px) {
  .pt-32 { padding-top: 6.5rem; }
  .mb-6 { margin-bottom: 0.75rem; }
  .p-10 { padding: 1.5rem; }
  .w-36 { width: 7rem; height: 7rem; }
}
</style>