<template>
  <div class="min-h-screen flex flex-col bg-[#FDFCF9] text-slate-900 font-sans selection:bg-amber-100">
    <Nav />

  <main class="flex-grow pt-24 pb-16">
      <div class="container mx-auto px-4 max-w-6xl" v-if="dish">
        <!-- 返回按钮 - 根据来源页面动态返回 -->
        <button
          @click="handleBack"
          class="inline-flex items-center text-amber-800 hover:text-amber-600 transition-colors mb-8 text-sm"
        >
          <i class="fa-solid fa-chevron-left mr-2"></i>
          {{ getBackButtonText() }}
        </button>

        <div 
          v-motion="{ initial: { opacity: 0, y: 10 }, enter: { opacity: 1, y: 0 } }"
          class="bg-white rounded-2xl shadow-sm border-2 border-amber-200 overflow-hidden flex flex-col md:flex-row mb-8"
        >
          <div class="md:w-1/4 h-60 md:h-auto overflow-hidden bg-stone-100 border-r-2 border-amber-100/50">
            <img
              :src="dish.image"
              :alt="dish.name"
              class="w-full h-full object-cover transition-transform duration-700 hover:scale-105"
            />
          </div>

          <div class="md:w-3/4 p-10 flex flex-col justify-center">
            <div class="flex justify-between items-start mb-6">
              <div>
                <h1 class="text-[32px] font-semibold tracking-wide text-amber-900 mb-3 font-serif">
                  {{ dish.name }}
                </h1>
                <div class="flex gap-2">
                  <span class="text-xs bg-amber-50 text-amber-700 px-3 py-1 rounded-full border border-amber-100 font-medium">
                    {{ dish.source }}
                  </span>
                  <span class="text-xs bg-stone-100 text-stone-600 px-3 py-1 rounded-full">
                    {{ dish.chapter }}
                  </span>
                </div>
              </div>

              <button
                @click="toggleFavorite"
                class="w-10 h-10 rounded-full flex items-center justify-center transition-all border border-amber-50"
                :class="isFavorite ? 'bg-red-50 text-red-500' : 'bg-amber-50 text-amber-600 hover:bg-amber-100'"
              >
                <i :class="isFavorite ? 'fa-solid fa-heart' : 'fa-regular fa-heart'"></i>
              </button>
            </div>

            <div class="space-y-8">
              <div>
                <span class="text-[11px] uppercase tracking-widest text-stone-400 font-bold block mb-3">
                  功效主治
                </span>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="eff in dish.effects"
                    :key="eff"
                    class="text-sm text-amber-900 px-2 py-0.5 bg-amber-50/70 rounded border border-amber-100"
                  >
                    {{ eff }}
                  </span>
                </div>
              </div>

              <div class="grid grid-cols-2 gap-10">
                <div>
                  <span class="text-[11px] uppercase tracking-widest text-green-600/70 font-bold block mb-3">
                    宜
                  </span>
                  <div class="flex flex-wrap gap-2">
                    <span v-for="c in dish.suitable" :key="c" class="text-xs text-green-700 bg-green-50 px-2 py-0.5 rounded border border-green-100">
                      {{ c }}
                    </span>
                  </div>
                </div>
                <div>
                  <span class="text-[11px] uppercase tracking-widest text-red-400 font-bold block mb-3">
                    忌
                  </span>
                  <div class="flex flex-wrap gap-2">
                    <span v-for="c in dish.unsuitable" :key="c" class="text-xs text-red-600 bg-red-50 px-2 py-0.5 rounded border border-red-100">
                      {{ c }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-2xl border-2 border-amber-200 p-10 mb-12 shadow-sm">
          <div class="flex justify-between items-center mb-8">
            <h3 class="text-lg font-semibold text-amber-900 flex items-center font-serif">
              <span class="w-1 h-5 bg-amber-500 mr-3 rounded-full"></span>
              古籍依据
            </h3>
            <button
              @click="handleTranslate"
              :disabled="isTranslating"
              class="text-sm px-5 py-2 rounded-full font-medium transition-all shadow-sm"
              :class="showTranslation ? 'bg-amber-100 text-amber-800' : 'bg-amber-600 text-white'"
            >
              <i class="fa-solid mr-2" :class="isTranslating ? 'fa-spinner animate-spin' : 'fa-wand-magic-sparkles'"></i>
              {{ isTranslating ? '正在校对...' : showTranslation ? '隐藏解语' : '现代解语' }}
            </button>
          </div>

          <div class="relative">
            <div class="absolute -left-4 top-1 bottom-1 w-[3px] bg-amber-200 rounded-full"></div>
            <p class="text-[17px] leading-loose text-stone-700 pl-6 font-serif tracking-[0.03em]">
              「 {{ dish.nutrition }} 」
            </p>
          </div>

          <transition enter-active-class="transition duration-700 ease-out" enter-from-class="opacity-0 translate-y-4" enter-to-class="opacity-100 translate-y-0">
            <div v-if="showTranslation" class="mt-8 relative">
              <div class="absolute inset-0 bg-stone-50 rounded-xl border border-stone-200/60 shadow-inner opacity-80 pointer-events-none"></div>
              <div class="relative p-6 md:p-8 space-y-8">
                <div v-if="splitTranslation.summary" class="flex items-start gap-4">
                  <div class="flex-shrink-0 mt-1">
                    <span class="inline-block w-8 h-8 rounded-lg bg-amber-600 text-white flex items-center justify-center shadow-md">
                      <i class="fa-solid fa-quote-left text-sm"></i>
                    </span>
                  </div>
                  <div>
                    <h4 class="text-amber-900 font-bold text-base mb-2">现代解析</h4>
                    <p class="text-stone-800 font-serif text-[15px] leading-8 text-justify">{{ splitTranslation.summary }}</p>
                  </div>
                </div>

                <div class="h-px bg-gradient-to-r from-transparent via-amber-200 to-transparent w-full opacity-60"></div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-0 md:divide-x divide-stone-200 border border-stone-100 rounded-xl bg-white overflow-hidden shadow-sm">
                  <div v-if="splitTranslation.ingredients" class="p-6">
                    <h4 class="text-stone-500 text-xs font-bold tracking-widest uppercase mb-4 flex items-center gap-2">
                      <span class="w-1.5 h-1.5 bg-green-500 rounded-full"></span> Ingredients
                    </h4>
                    <p class="text-stone-700 text-sm font-medium leading-loose whitespace-pre-line font-mono">{{ splitTranslation.ingredients }}</p>
                  </div>
                  <div v-if="splitTranslation.theory" class="p-6">
                    <h4 class="text-stone-500 text-xs font-bold tracking-widest uppercase mb-4 flex items-center gap-2">
                      <span class="w-1.5 h-1.5 bg-blue-500 rounded-full"></span> Theory
                    </h4>
                    <p class="text-stone-600 text-[13px] leading-7 italic text-justify border-l-2 border-stone-200 pl-3">“{{ splitTranslation.theory }}”</p>
                  </div>
                </div>

                <div v-if="splitTranslation.method">
                  <h4 class="text-amber-900 font-bold text-base mb-6 flex items-center">
                    <i class="fa-solid fa-utensils text-amber-500 mr-2 text-sm"></i> 古法新做
                  </h4>
                  <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12">
                    <div class="lg:col-span-8 space-y-0">
                      <div 
                      v-for="(step, index) in splitTranslation.method
                        .split(/(?=步骤\d+：)/)
                        .filter(s => s.trim())" 
                      :key="index" 
                      class="group flex gap-4 relative pb-6 last:pb-0"
                    >
                      <div class="absolute left-[11px] top-8 bottom-0 w-[2px] bg-stone-200 group-last:hidden"></div>

                      <div class="flex-shrink-0 relative z-10">
                        <span class="flex items-center justify-center w-6 h-6 rounded-full bg-white text-amber-700 text-xs font-bold border-2 border-amber-100 shadow-sm">
                          {{ index + 1 }}
                        </span>
                      </div>

                      <p class="text-stone-700 text-[14px] leading-7">
                        {{ step.replace(/^步骤\d+：/, '').trim() }}
                      </p>
                    </div>
                    </div>
                    <div class="lg:col-span-4">
                      <div class="bg-stone-50/90 rounded-2xl border border-stone-200/80 p-7 h-full shadow-sm">
                        <h5 class="text-xs font-bold text-amber-900 uppercase tracking-widest mb-6 flex items-center justify-center">
                          烹饪贴士
                        </h5>
                        <div class="space-y-6">
                          <div class="flex items-center justify-between">
                            <span class="text-[12px] text-stone-500 font-bold">难度指数</span>
                            <div class="flex text-amber-500 text-[10px] gap-1">
                              <i v-for="n in splitTranslation.difficulty" :key="n" class="fa-solid fa-star"></i>
                            </div>
                          </div>
                          <div class="bg-white p-4 rounded-xl border border-stone-200 text-[13px] text-stone-600 italic leading-relaxed">
                            {{ splitTranslation.tips }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
<div class="mt-10 pt-8 border-t border-dashed border-amber-200">
  <div class="flex items-center justify-between mb-6">
    <div>
      <h4 class="text-amber-900 font-bold text-base flex items-center">
        <i class="fa-solid fa-wand-magic-sparkles text-amber-500 mr-2"></i>
        古法步骤图
      </h4>
      <p class="text-xs text-stone-400 mt-1">根据古法步骤生成视觉参考图（支持左右滑动）</p>
    </div>

    <button 
      @click="handleGenerateStepImages"
      :disabled="isGeneratingImages || isTranslating"
      class="text-xs px-5 py-2 bg-amber-50 text-amber-700 rounded-full border border-amber-200 hover:bg-amber-100 transition-all flex items-center gap-2 disabled:opacity-50"
    >
      <i class="fa-solid" :class="isGeneratingImages ? 'fa-spinner animate-spin' : 'fa-palette'"></i>
      {{ isGeneratingImages ? '正在挥毫泼墨...' : '生成步骤图' }}
    </button>
  </div>

  <div 
    v-if="stepImages.length > 0" 
    class="flex gap-4 overflow-x-auto pb-4 custom-horizontal-scrollbar snap-x"
  >
    <div 
      v-for="(url, index) in stepImages" 
      :key="index" 
      class="flex-shrink-0 w-[260px] sm:w-[300px] group relative aspect-square rounded-xl overflow-hidden bg-stone-100 border border-amber-100 shadow-sm snap-center"
    >
      <el-image 
        :src="url" 
        :preview-src-list="stepImages"
        :initial-index="index"
        fit="cover"
        class="w-full h-full transition-transform duration-500 group-hover:scale-110"
      >
        <template #placeholder>
          <div class="flex items-center justify-center h-full bg-stone-50 text-stone-400 text-xs">加载中...</div>
        </template>
      </el-image>
      <div class="absolute top-2 left-2 px-2 py-0.5 bg-black/30 backdrop-blur-sm text-white text-[10px] rounded border border-white/20">
        步骤 {{ index + 1 }}
      </div>
    </div>
  </div>

  <div v-else-if="isGeneratingImages" class="flex gap-4 overflow-x-hidden pb-4">
    <div v-for="n in 3" :key="n" class="flex-shrink-0 w-[260px] sm:w-[300px] aspect-square rounded-xl bg-stone-50 animate-pulse flex flex-col items-center justify-center border border-stone-100">
      <i class="fa-solid fa-image text-stone-200 text-2xl mb-2"></i>
      <span class="text-[10px] text-stone-300">正在绘制分镜 {{ n }}...</span>
    </div>
  </div>
</div>
                </div>
              </div>
            </div>
          </transition>
        </div>
        <div v-if="dish.videos && dish.videos.length > 0" class="mb-12">
          <div class="bg-white rounded-2xl shadow-sm border-2 border-amber-200 p-8">
            <h3 class="text-lg font-bold text-amber-900 flex items-center mb-6 font-serif">
              <span class="w-1 h-5 bg-amber-500 mr-3 rounded-full"></span> 烹饪教程
            </h3>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              <div v-for="video in dish.videos" :key="video.id" class="group cursor-pointer" @click="handleVideoClick(video)">
                <div class="relative aspect-video rounded-xl overflow-hidden mb-3 shadow-md bg-stone-100 border border-amber-100/30">
                  <img :src="video.cover" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                  <div class="absolute inset-0 bg-black/20 flex items-center justify-center">
                    <div class="w-12 h-12 bg-white/90 rounded-full flex items-center justify-center text-amber-600 shadow-lg">
                      <i class="fa-solid" :class="video.isUnlocked ? 'fa-play' : 'fa-lock'"></i>
                    </div>
                  </div>
                </div>
                <h4 class="font-bold text-stone-800 group-hover:text-amber-700 transition-colors line-clamp-1">{{ video.title }}</h4>
              </div>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          <div class="lg:col-span-7 bg-white rounded-2xl p-8 border-2 border-amber-200 shadow-sm min-h-[420px]">
            <h3 class="text-lg font-bold text-amber-900 mb-6 font-serif">食材与性味</h3>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div
                v-for="ing in dish.ingredients"
                :key="ing.name"
                @click="activeIngredient = ing"
                class="p-4 rounded-xl border transition-all cursor-pointer flex flex-col gap-2"
                :class="activeIngredient?.name === ing.name ? 'border-amber-500 bg-amber-50/30 shadow-inner' : 'border-stone-100 hover:border-amber-200'"
              >
                <div class="flex justify-between items-center">
                  <span class="font-bold text-stone-800">{{ ing.name }}</span>
                  <i v-if="activeIngredient?.name === ing.name" class="fa-solid fa-circle-check text-amber-500 text-xs"></i>
                </div>
                <div class="flex gap-2">
                  <span class="text-[10px] px-2 py-0.5 bg-stone-100 text-stone-500 rounded font-medium">{{ ing.nature }}</span>
                  <span class="text-[10px] px-2 py-0.5 bg-stone-100 text-stone-500 rounded font-medium">{{ ing.flavor }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="lg:col-span-5">
            <div class="bg-white text-stone-900 rounded-2xl p-8 border-2 border-amber-200 shadow-md w-full flex flex-col relative overflow-hidden min-h-[420px] max-h-[600px]">
              <div class="absolute right-0 bottom-0 opacity-[0.03] pointer-events-none text-amber-900">
                <i class="fa-solid fa-seedling text-[120px] translate-x-10 translate-y-10"></i>
              </div>
              <div v-if="activeIngredient" class="relative z-10 flex flex-col h-full overflow-hidden">
                <span class="text-[10px] text-amber-600 font-bold tracking-widest uppercase">Ingredient Analysis</span>
                <h4 class="text-2xl font-bold mt-2 mb-4 text-amber-900 font-serif">{{ activeIngredient.name }}</h4>
                
                <div class="flex-grow overflow-y-auto pr-2 custom-scrollbar space-y-6">
                  <div>
                    <span class="text-[11px] text-stone-400 font-bold block mb-2 uppercase tracking-tight">药性归经</span>
                    <p class="text-sm font-medium text-stone-700 flex gap-2">
                      <span class="bg-amber-50 px-2 py-0.5 rounded border border-amber-100/50">{{ activeIngredient.nature }}</span>
                      <span class="bg-amber-50 px-2 py-0.5 rounded border border-amber-100/50">{{ activeIngredient.flavor }}</span>
                    </p>
                  </div>
                  <div>
                    <span class="text-[11px] text-stone-400 font-bold block mb-2 uppercase tracking-tight">功效详解</span>
                    <p class="text-base leading-relaxed text-stone-600 font-serif">{{ activeIngredient.effect }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="flex-grow flex items-center justify-center text-stone-400 italic text-sm text-center">
                <div>
                  <i class="fa-solid fa-hand-pointer block mb-2 opacity-20 text-2xl"></i>
                  点击左侧食材查看详细药性
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    <Footer />

    <Transition name="fade">
      <div v-if="showVideoModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm" @click="closeVideoModal">
        <div class="bg-black w-full max-w-4xl aspect-video rounded-xl overflow-hidden shadow-2xl relative" @click.stop>
          <button @click="closeVideoModal" class="absolute top-4 right-4 text-white hover:text-amber-400 z-10 text-2xl">
            <i class="fa-solid fa-xmark"></i>
          </button>
          <iframe v-if="currentVideo?.url" :src="currentVideo.url" class="w-full h-full" frameborder="0" allowfullscreen></iframe>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* 自定义横向滚动条样式 */
.custom-horizontal-scrollbar::-webkit-scrollbar {
  height: 6px; /* 滚动条高度 */
}

.custom-horizontal-scrollbar::-webkit-scrollbar-track {
  background: #fdfcf9;
  border-radius: 10px;
}

.custom-horizontal-scrollbar::-webkit-scrollbar-thumb {
  background: #e9e4d9; /* 靠近 amber-100 的颜色 */
  border-radius: 10px;
}

.custom-horizontal-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #d4cfc1;
}

/* 兼容 Firefox */
.custom-horizontal-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: #e9e4d9 #fdfcf9;
}

/* 磁贴感：滑动停止时自动对齐图片 */
.snap-x {
  scroll-snap-type: x mandatory;
}
.snap-center {
  scroll-snap-align: center;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #f1f1f1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #e2e2e2;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, RouterLink, useRouter } from "vue-router";
import { chatStream } from "@/api/dify";
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import { getDishById } from "@/api/dishes";
import { findIngredientsByDishId, type FoodIngredients } from "@/api/foodIngredients";
import { collectionApi } from "@/api/collection";
import { useAuthStore } from "@/stores/auth";
import { logFavoriteDish } from "@/api/analytics";
import { generateCulinaryImages } from '@/api/culinaryImage';
import { ElMessage } from 'element-plus'; // 如果没用 ElementPlus，可以用原生 alert 或你自己的通知组件

// 引入视频API和类型
import { getDishVideos, unlockVideoApi, type VideoItem } from "@/api/video";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

// 当前菜品详情
const dish = ref<any>(null);

// 视频相关状态
const showVideoModal = ref(false);
const currentVideo = ref<VideoItem | null>(null);

// 收藏状态
const isFavorite = ref(false);

// 选中食材
const activeIngredient = ref<any>(null);

// AI 翻译状态
const isTranslating = ref(false);
const showTranslation = ref(false);
const translationResult = ref("");

const splitTranslation = ref({
  summary: '',     // + 现代解析
  ingredients: '', // + 现代配比
  method: '',      // + 古法新做
  theory: '',       // + 辨证理法
  difficulty: 0,   // 默认3星
  keyFocus: '文火慢炖', // 默认占位
  tips: '暂无额外提示'   // 默认占位
});

// 动态返回逻辑
const getBackButtonText = () => {
  const fromPage = route.query.from as string;
  switch (fromPage) {
    case 'books':
      return '返回古籍食谱';
    case 'recipes':
      return '返回食谱推荐';
    case 'home':
      return '返回首页';
    case 'forum':
      return '返回论坛';
    case 'search':
      return '返回搜索结果';
    case 'favorites':
      return '返回收藏列表';
    default:
      return '返回上一页';
  }
};

const handleBack = () => {
  const fromPage = route.query.from as string;
  
  if (fromPage) {
    // 根据来源页面返回
    switch (fromPage) {
      case 'books':
        router.push('/books');
        break;
      case 'recipes':
        router.push('/recipes');
        break;
      case 'home':
        router.push('/');
        break;
      case 'forum':
        router.push('/forum');
        break;
      case 'search':
        // 如果是搜索结果页面，返回时保留搜索参数
        const searchQuery = route.query.searchQuery as string;
        if (searchQuery) {
          router.push(`/search?q=${encodeURIComponent(searchQuery)}`);
        } else {
          router.push('/search');
        }
        break;
      case 'favorites':
        router.push('/favorites');
        break;
      default:
        router.back();
    }
  } else {
    // 如果没有来源页面参数，使用默认返回逻辑
    router.back();
  }
};

// 视频交互逻辑
const openVideoModal = (video: VideoItem) => {
  currentVideo.value = video;
  showVideoModal.value = true;
};

const closeVideoModal = () => {
  showVideoModal.value = false;
  currentVideo.value = null;
};

// 处理视频点击
const handleVideoClick = async (video: VideoItem) => {
  // 如果已解锁，直接播放
  if (video.isUnlocked) {
    if (!video.videoUrl) {
      alert("视频地址获取异常");
      return;
    }
    openVideoModal(video);
    return;
  }

  // 鉴权
  if (!auth.isAuthenticated) {
    router.push('/login');
    return;
  }

  const confirmed = window.confirm(`该视频是名家亲传教程，解锁需消耗 ${video.price} 积分。\n\n确认解锁观看吗？`);
  
  if (confirmed) {
    try {
      // 调用解锁接口
      const res = await unlockVideoApi({
        userId: auth.user.userId,
        videoId: video.id
      });
      
      if ((res as any).code === 200 || (res as any).success === true) {
        
        // 解锁成功后，必须重新获取视频列表！
        // 因为后端出于安全考虑，未解锁时不返回 url，解锁后重新请求才能拿到 url
        await fetchVideoList(dish.value.id);
        
        // 自动打开刚才点击的视频
        const updatedVideo = dish.value.videos.find((v: VideoItem) => v.id === video.id);
        if (updatedVideo) {
          openVideoModal(updatedVideo);
        }
      } else {
        alert((res as any).msg || "解锁失败，请检查积分是否充足");
      }
    } catch (error: any) {
      console.error("解锁异常:", error);
      alert(error.msg || "系统繁忙，请稍后再试");
    }
  }
};

const fetchVideoList = async (dishId: number) => {
  try {
    const currentUserId = auth.user?.userId;
    console.log("--- [视频列表请求] ---");
    console.log("菜品ID:", dishId, "用户ID:", currentUserId); 
    
    const res = await getDishVideos(dishId, currentUserId);

    // 提取原始数组数据
    const rawVideos = Array.isArray(res) ? res : (res as any).data || [];
    console.log("API 返回的原始视频数据 (rawVideos):", JSON.parse(JSON.stringify(rawVideos)));

    // 核心：加工处理
    const processedVideos = rawVideos.map((v: VideoItem) => {
      const finalUrl = v.videoUrl ? normalizeCover(v.videoUrl) : "";
      const finalCover = v.coverUrl ? normalizeCover(v.coverUrl) : "";
      
      // 这里打印每一个视频的转换结果
      console.log(`视频[${v.title}] 转换日志:`);
      console.log(`  > 原始 URL: ${v.videoUrl} -> 转换后: ${finalUrl}`);
      console.log(`  > 原始封面: ${v.coverUrl} -> 转换后: ${finalCover}`);
      console.log("版本标记：2026-05-04-v1");
      
      return {
        ...v,
        url: normalizeCover(finalUrl), 
        cover: normalizeCover(finalCover)
      };
    });

    if (dish.value) {
      dish.value.videos = processedVideos;
    }
    
    return processedVideos;
  } catch (e) {
    console.error("获取视频列表失败", e);
    console.log("版本标记：2026-05-04-v1");
    return [];
  }
};

// 图片地址兜底
const normalizeCover = (url?: string) => {
  if (!url) return "";
  if (/^https?:\/\//.test(url)) return url;
  return url.startsWith("/") ? url : "/" + url;
};


// AI 翻译逻辑
const handleTranslate = async () => {
  if (!dish.value?.nutrition) return;
  if (translationResult.value && !isTranslating.value) {
    showTranslation.value = !showTranslation.value;
    return;
  }
  
  showTranslation.value = true;
  translationResult.value = "";
  isTranslating.value = true;

  // 每次翻译前先清空旧内容
  splitTranslation.value = { summary: '', ingredients: '', method: '', theory: '', difficulty: 0, keyFocus: '文火慢炖', tips: '暂无额外提示' };

  // 给 AI 的提示词，强制它按格式输出，方便前端拆
 const prompt = `你是一位精通中医养生专家。请将古籍内容解析为现代白话，必须严格按下述格式输出：
【解析】内容...
【食材】内容...
【造法】内容...
【理法】内容...
【要点】
难度|3
关键|文火慢炖
提示|...内容...
古籍内容：\n${dish.value.nutrition}`;

  try {
    // 使用流式接口，一边返回一边显示
    // 在 handleTranslate 的 onMessage 回调里
    await chatStream(
      {
        agent: "translator",
        prompt
      },
      (chunk) => {
        translationResult.value += chunk;
        const text = translationResult.value;

      const extract = (regex: RegExp) => {
        const match = text.match(regex);
        return match ? match[1].trim() : "";
      };

      splitTranslation.value.summary = extract(/【解析】([\s\S]*?)(?=【|$)/);
      splitTranslation.value.ingredients = extract(/【食材】([\s\S]*?)(?=【|$)/);
      splitTranslation.value.method = extract(/【造法】([\s\S]*?)(?=【|$)/);
      splitTranslation.value.theory = extract(/【理法】([\s\S]*?)(?=【|$)/);

      const highlights = extract(/【要点】([\s\S]*?)(?=【|$)/);
      if (highlights) {
        const diffMatch = highlights.match(/难度\|(\d)/);
        if (diffMatch) splitTranslation.value.difficulty = parseInt(diffMatch[1]);

        // 使用 [^提示\n]* 表示匹配不包含“提示”和“换行”的任意字符
        const keyMatch = highlights.match(/关键\|(.*?)(?=提示|难度|\n|$)/);
        if (keyMatch) {
          splitTranslation.value.keyFocus = keyMatch[1].trim().replace(/[|]$|提示$/, ''); 
        }

        const tipMatch = highlights.match(/提示\|([\s\S]*)/);
        if (tipMatch) {
          splitTranslation.value.tips = tipMatch[1].trim();
        }
      }
    });
  } catch (error) {
    console.error("翻译出错:", error);
  } finally {
    isTranslating.value = false;
  }
};

// 生图相关的响应式变量
const isGeneratingImages = ref(false);
const stepImages = ref<string[]>([]); // 存储生成的图片 URL 列表

// 提取造法并请求后端
const handleGenerateStepImages = async () => {
  if (!splitTranslation.value.method) return;

  const stepsArray = splitTranslation.value.method
    .split(/(?=步骤\d+：)/)
    .filter(s => s.trim());
const payload = {
  dishName: dish.value?.name,
  stepsText: stepsArray
};

  isGeneratingImages.value = true;
  stepImages.value = []; // 清空之前的图片

  try {
    // 调用 ts 文件中的接口
    const res = await generateCulinaryImages(payload);
    
    if (res.success) {
      stepImages.value = res.imageUrls;
      ElMessage.success('画卷已绘成');
    } else {
      ElMessage.error(res.message || '生成失败');
    }
  } catch (error) {
    console.error("生图异常:", error);
    console.log("版本标记：2026-05-04-v1");
    ElMessage.error('网络异常，无法连接生图服务器');
  } finally {
    isGeneratingImages.value = false;
  }
};

// 加载菜品详情
const loadDishDetail = async () => {
  const dishId = Number(route.params.id);
  if (!dishId) return;

  const d = await getDishById(dishId);
  if (!d) return;

  // 并行加载食材
  let ings: FoodIngredients[] = await findIngredientsByDishId(d.dishId);
  const uniqueMap = new Map();
  ings.forEach(i => {
    if (!uniqueMap.has(i.ingredientName)) uniqueMap.set(i.ingredientName, i);
  });

  // 基础数据组装
  dish.value = {
    id: d.dishId,
    name: d.dishName,
    source: d.dishSource,
    chapter: d.kind,
    image: normalizeCover(d.dishImgUrl),
    effects: [d.effectTag1, d.effectTag2, d.effectTag3, d.effectTag4, d.effectTag5].filter(Boolean),
    suitable: d.suitableConstitution ? d.suitableConstitution.split("，") : [],
    unsuitable: d.unsuitableConstitution ? d.unsuitableConstitution.split("，") : [],
    nutrition: d.ancientCitation || "暂无古籍记载",
    videos: [], // 先置空，稍后获取
    ingredients: Array.from(uniqueMap.values()).map((i: any) => ({
      name: i.ingredientName,
      nature: i.property || "不详",
      flavor: i.category || "不详",
      effect: i.ancientEffect || "暂无功效记载",
    })),
  };

  await fetchVideoList(d.dishId);

  checkFavoriteStatus();
};

// 收藏逻辑
const checkFavoriteStatus = async () => {
  if (!auth.isAuthenticated || !auth.user?.userId || !dish.value) return;

  const res = await collectionApi.getUserCollectionsByType(
    auth.user.userId,
    'dish'
  )as any; // 关键点：添加 as any

  // + 判断当前菜品是否在收藏列表里
  if (res.code === 200) {
    isFavorite.value = res.data.some((item: any) => item.targetId === dish.value.id);
  }
};

// DishDetail.vue 

const toggleFavorite = async () => {
  if (!auth.isAuthenticated) return router.push('/login');
  
  const res = await collectionApi.toggleCollection(auth.user!.userId, 'dish', dish.value.id);
  
  if (res.success) {
    const wasCollected = res.isCollected ?? false;
    isFavorite.value = wasCollected;

    if (wasCollected) {
      const uId = auth.user?.userId || 0;
      const dId = dish.value.id; 
      
      // 从 localStorage 读取
      const storageKey = `click_session_${dId}`;
      const refererId = localStorage.getItem(storageKey);
      
      console.log("准备记录收藏行为：");
      console.log("- 菜品ID:", dId);
      console.log("- 读取到的来源SessionId:", refererId);

      try {
        // 发送给后端 
        await logFavoriteDish(uId, dId, refererId || ""); 
        console.log("收藏行为记录成功！");
      } catch (error) {
        console.error("记录收藏行为失败", error);
      }
    }
  }
};

onMounted(loadDishDetail);
</script>
