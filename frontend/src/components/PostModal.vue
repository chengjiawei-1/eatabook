<template>
  <div
    class="fixed inset-0 bg-stone-900/60 backdrop-blur-md flex items-center justify-center z-[60] p-4"
    @click.self="close"
  >
    <div class="bg-[#fcfaf2] dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-2xl max-h-[85vh] overflow-hidden border border-amber-100 dark:border-slate-800 flex flex-col animate-in fade-in zoom-in-95 duration-300">
      
      <!-- 顶部艺术装饰 -->
      <div class="h-2 bg-gradient-to-r from-amber-200 via-amber-600 to-amber-200"></div>

      <!-- 头部固定在顶部 -->
      <div class="p-6 border-b border-amber-100 dark:border-slate-800">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 bg-amber-700 text-white rounded-2xl flex items-center justify-center shadow-lg shadow-amber-900/20">
              <i class="fa-solid fa-pen-nib text-lg"></i>
            </div>
            <div>
              <h2 class="text-xl font-serif font-black text-slate-800 dark:text-white">撰写食养心得</h2>
              <p class="text-[9px] uppercase tracking-widest text-amber-600/60 font-bold mt-1">Share your dietary wisdom</p>
            </div>
          </div>

          <button @click="close" class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-amber-100 dark:hover:bg-slate-800 text-slate-400 transition-colors">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      </div>

      <!-- 表单内容区域，可滚动 -->
      <div class="flex-1 overflow-y-auto custom-scrollbar p-6">
        <form @submit.prevent="publishPost" class="space-y-4">
          <!-- 标题和分类在一行 -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="md:col-span-2">
              <label class="custom-label">心得标题</label>
              <input v-model="newPost.title" class="custom-input text-sm" placeholder="起一个典雅的标题..." required />
            </div>

            <div>
              <label class="custom-label">所属分类</label>
              <div class="relative">
                <select v-model="newPost.categoryId" class="custom-input text-sm appearance-none" required>
                  <option value="" disabled>请选择分类</option>
                  <option v-for="c in categories" :key="c.categoryId" :value="c.categoryId">{{ c.categoryName }}</option>
                </select>
                <i class="fa-solid fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-amber-600 text-[9px] pointer-events-none"></i>
              </div>
            </div>
          </div>

          <!-- 正文内容，高度减小 -->
          <div>
            <label class="custom-label">正文内容</label>
            <textarea
              v-model="newPost.content"
              rows="4"
              class="custom-input text-sm resize-none"
              placeholder="分享您的食疗秘方或古籍见解..."
              required
            />
          </div>

          <!-- 图片上传区域，简化布局 -->
          <div>
            <label class="custom-label">配图</label>
            <div class="grid grid-cols-3 gap-3 mt-2">
              <!-- 已选图片预览 -->
              <div v-for="(img, i) in previewImages" :key="i" class="relative group aspect-square rounded-xl overflow-hidden shadow-md">
                <img :src="img" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300" />
                <button @click="removeImage(i)" class="absolute top-1 right-1 w-5 h-5 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center text-xs">
                  <i class="fa-solid fa-xmark"></i>
                </button>
              </div>

              <!-- 上传按钮 -->
              <label v-if="previewImages.length < 4" class="aspect-square rounded-xl border-2 border-dashed border-amber-200 dark:border-slate-700 flex flex-col items-center justify-center cursor-pointer hover:bg-amber-50 dark:hover:bg-slate-800 transition-all group">
                <input type="file" multiple accept="image/*" class="hidden" @change="handleImageUpload" />
                <i class="fa-solid fa-plus text-amber-600 text-sm group-hover:scale-125 transition-transform"></i>
                <span class="text-[9px] text-amber-600/60 font-bold mt-1">添加图片</span>
              </label>
            </div>
          </div>
        </form>
      </div>

      <!-- 操作按钮固定在底部 -->
      <div class="p-6 border-t border-amber-100 dark:border-slate-800 bg-white/50 dark:bg-slate-900/50">
        <div class="flex items-center justify-end gap-3">
          <button type="button" @click="close" class="px-6 py-2 rounded-full text-slate-400 text-sm font-bold hover:text-slate-600 transition-colors">取消</button>
          <button 
            type="submit" 
            @click="publishPost"
            class="px-8 py-2 rounded-full bg-gradient-to-r from-amber-700 to-orange-600 text-white font-serif font-bold shadow-lg shadow-amber-900/20 hover:shadow-amber-900/40 hover:-translate-y-0.5 active:scale-95 transition-all disabled:opacity-50 text-sm"
            :disabled="uploading"
          >
            <i v-if="uploading" class="fa-solid fa-spinner animate-spin mr-1"></i>
            {{ uploading ? '正在上传...' : '确认上传' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { publishForumPost, uploadPostImages, getForumCategories } from '@/api/forums'
import { getForumImageUrl } from '@/utils/imageUtils'  

const emit = defineEmits(['close', 'success'])

// 响应式数据
const newPost = ref({ title: '', content: '', categoryId: '' })
const files = ref<File[]>([])
const previewImages = ref<string[]>([])
const uploading = ref(false)
const categories = ref<any[]>([]) // 存储分类数据

// 获取分类数据
const fetchCategories = async () => {
  try {
    const res = await getForumCategories()
    console.log('PostModal 获取分类数据:', res)

    // 解析分类数据
    let categoriesArray = []
    if (res.data && typeof res.data === 'object' && res.data.data !== undefined) {
      // 标准响应格式：{ code: 200, data: [...] }
      categoriesArray = res.data.data || []
    } else if (res.data && Array.isArray(res.data)) {
      // 直接返回数组格式
      categoriesArray = res.data
    }

    categories.value = categoriesArray
    console.log('PostModal 分类数据解析完成:', categories.value)
  } catch (error) {
    console.error('获取分类数据失败:', error)
    alert('获取分类数据失败，请稍后重试')
  }
}

// 图片上传处理
const handleImageUpload = (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files) return

  for (const file of Array.from(input.files)) {
    files.value.push(file)
    previewImages.value.push(URL.createObjectURL(file))
  }

  input.value = ''
}

const removeImage = (index: number) => {
  files.value.splice(index, 1)
  previewImages.value.splice(index, 1)
}

// 检查文件大小和类型
const validateFiles = () => {
  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
  
  for (const file of files.value) {
    // 检查文件大小
    if (file.size > MAX_FILE_SIZE) {
      throw new Error(`文件 "${file.name}" 大小超过限制 (最大10MB)`);
    }
    
    // 检查文件类型
    if (!ALLOWED_TYPES.includes(file.type)) {
      throw new Error(`文件 "${file.name}" 类型不支持，请上传 JPEG、PNG、GIF 或 WebP 格式`);
    }
  }
  
  // 检查总文件数量
  if (files.value.length > 10) {
    throw new Error('最多只能上传10张图片');
  }
};

// 分片上传图片（避免网络拥堵）
const uploadImagesSequentially = async (postId: number) => {
  const results = [];
  
  for (let i = 0; i < files.value.length; i++) {
    const file = files.value[i];
    console.log(`上传第 ${i + 1}/${files.value.length} 张图片: ${file.name}`);
    
    try {
      // 单张图片上传，避免并发问题
      const uploadResult = await uploadPostImages(postId, [file]);
      
      if (uploadResult && uploadResult.data) {
        const processedImages = uploadResult.data.map((img: string) => getForumImageUrl(img));
        results.push(...processedImages);
        console.log(`第 ${i + 1} 张图片上传成功`);
      }
      
      // 添加延迟，避免服务器压力过大
      if (i < files.value.length - 1) {
        await new Promise(resolve => setTimeout(resolve, 500));
      }
      
    } catch (error: any) {
      console.error(`第 ${i + 1} 张图片上传失败:`, error);
      throw new Error(`图片 "${file.name}" 上传失败: ${error.response?.data?.message || '网络错误'}`);
    }
  }
  
  return results;
};

// 发布帖子
const publishPost = async () => {
  if (uploading.value) return;
  uploading.value = true;

  try {
    //验证文件
    if (files.value.length > 0) {
      validateFiles();
    }

    // 发布帖子
    const res = await publishForumPost({
      title: newPost.value.title,
      content: newPost.value.content,
      categoryId: Number(newPost.value.categoryId)
    });

    console.log('发布帖子响应结果:', res);

    // 获取帖子 ID
    const postId = res.postId || res.data?.postId || res.data?.data?.postId || res.data?.data;
    if (!postId) {
      console.error('无法获取帖子ID，返回结果为:', res);
      alert('帖子上传异常：未能获取到帖子ID');
      return;
    }

    // 上传图片（分片上传）
    if (files.value.length > 0) {
      console.log(`准备为帖子 ${postId} 上传 ${files.value.length} 张图片`);
      
      // 显示上传进度
      alert(`开始上传 ${files.value.length} 张图片，请勿关闭页面...`);
      
      const uploadResult = await uploadImagesSequentially(postId);
      console.log('所有图片上传完成，处理后的URL:', uploadResult);
    }

    console.log("postId 类型:", typeof postId);
    console.log("postId 值:", postId);

    alert('上传成功，请耐心等待管理员审核！');
    emit('success');
    close();
  } catch (error: any) {
    console.error('上传失败:', error);
    const errorMsg = error.message || error.response?.data?.message || '上传失败，请重试';
    alert(errorMsg);
  } finally {
    uploading.value = false;
  }
};

const close = () => {
  emit('close')
}

// 组件挂载时获取分类数据
onMounted(() => {
  fetchCategories()
})
</script>


<style scoped>
.font-serif { font-family: 'Noto Serif SC', serif; }

.custom-label {
  @apply block mb-1 text-[10px] uppercase tracking-widest text-amber-800/50 dark:text-amber-500/50 font-black px-2;
}

.custom-input {
  @apply w-full px-4 py-2.5 rounded-xl border-none bg-white dark:bg-slate-800 shadow-inner ring-1 ring-amber-100 dark:ring-slate-700 focus:ring-2 focus:ring-amber-500 transition-all outline-none text-slate-700 dark:text-slate-200 font-serif;
}

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { @apply bg-amber-200/50 rounded-full; }
</style>