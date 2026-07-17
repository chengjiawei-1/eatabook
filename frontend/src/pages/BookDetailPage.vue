<template>
  <div class="min-h-screen flex flex-col bg-[#fdfcf8] dark:bg-slate-950 font-sans selection:bg-amber-100">
    <Nav />

    <main class="flex-grow pt-24 pb-16">
      <div v-if="loading" class="container mx-auto px-4 flex-grow flex items-center justify-center">
        <div class="text-center">
          <i class="fa-solid fa-book-open text-3xl text-amber-600 mb-4 animate-pulse"></i>
          <p class="text-amber-800 dark:text-amber-300 font-medium">加载中...</p>
        </div>
      </div>

      <div v-else class="container mx-auto px-4 max-w-7xl">
        <div
          v-if="book"
          v-motion="{ initial: { opacity: 0, y: 20 }, enter: { opacity: 1, y: 0 } }"
          class="bg-white dark:bg-slate-900 rounded-3xl shadow-sm border-2 border-amber-200/60 dark:border-slate-800 overflow-hidden mb-8"
        >
          <div class="flex flex-col lg:flex-row">
            <div class="lg:w-1/3 p-8 lg:p-12 bg-stone-50/50 dark:bg-slate-900 flex justify-center items-center border-b lg:border-b-0 lg:border-r-2 border-amber-100/80 dark:border-slate-800">
              <div class="relative group w-full max-w-[280px]">
                <div class="absolute -inset-2 bg-gradient-to-r from-amber-200 to-amber-500 rounded-xl blur opacity-10 group-hover:opacity-30 transition duration-1000"></div>
                <div class="aspect-[2/3] relative rounded-lg overflow-hidden shadow-2xl border border-white dark:border-slate-700 bg-white">
                   <div v-show="imageLoading" class="absolute inset-0 bg-amber-50 animate-pulse"></div>
                   <img :src="normalizeCover(book.bookCoverUrl)" :alt="book.bookName" class="w-full h-full object-contain" @load="imageLoading = false" />
                </div>
              </div>
            </div>

            <div class="lg:w-2/3 p-8 lg:p-10">
              <div class="flex flex-col h-full">
                <div class="flex justify-between items-start mb-6">
                  <div>
                    <RouterLink to="/books" class="text-amber-700 dark:text-amber-500 text-xs font-semibold hover:underline mb-3 flex items-center">
                      <i class="fa-solid fa-chevron-left mr-2"></i> 返回列表
                    </RouterLink>
                    <h1 class="text-3xl font-bold text-amber-950 dark:text-amber-100 font-serif mb-4 tracking-tight">{{ book.bookName }}</h1>
                  </div>
                  <div class="hidden sm:block">
                    <span class="px-4 py-1.5 bg-amber-700 text-white text-xs font-bold rounded-full uppercase tracking-widest border border-amber-600 shadow-sm">
                      {{ mapCategory(book.bookCategory) }}
                    </span>
                  </div>
                </div>

                <div class="flex flex-wrap gap-3 mb-6">
                  <div class="flex items-center text-amber-900/80 dark:text-amber-400 text-xs bg-amber-50 dark:bg-slate-800/50 px-3 py-1.5 rounded-lg border border-amber-100/50 dark:border-slate-700">
                    <i class="fa-solid fa-user-pen mr-2 text-amber-600"></i>{{ book.author || '佚名' }}
                  </div>
                  <div class="flex items-center text-amber-900/80 dark:text-amber-400 text-xs bg-amber-50 dark:bg-slate-800/50 px-3 py-1.5 rounded-lg border border-amber-100/50 dark:border-slate-700">
                    <i class="fa-solid fa-landmark mr-2 text-amber-600"></i>{{ book.dynasty || '未知' }}
                  </div>
                  <div class="flex items-center text-amber-900/80 dark:text-amber-400 text-xs bg-amber-50 dark:bg-slate-800/50 px-3 py-1.5 rounded-lg border border-amber-100/50 dark:border-slate-700">
                    <i class="fa-solid fa-list-check mr-2 text-amber-600"></i>{{ book.nourishCount }} 条目
                  </div>
                </div>

                <div class="grid grid-cols-1 xl:grid-cols-5 gap-8">
                  <div class="xl:col-span-3">
                    <h3 class="text-xs font-bold text-amber-800 dark:text-amber-500 mb-3 uppercase tracking-widest flex items-center">
                      <span class="w-4 h-[1px] bg-amber-300 mr-2"></span> 古籍概要
                    </h3>
                    <p class="text-amber-950/90 dark:text-slate-200 leading-relaxed text-base italic font-serif bg-stone-50/60 dark:bg-slate-800/30 p-5 rounded-xl border border-amber-100/40 dark:border-slate-800">
                      “{{ book.intro || '暂无古籍简介' }}”
                    </p>
                  </div>
                  <div class="xl:col-span-2 bg-amber-50/30 dark:bg-slate-900/50 rounded-2xl p-5 border border-amber-100 dark:border-slate-800">
                    <h3 class="text-[11px] font-bold text-amber-800 dark:text-amber-600 mb-3 text-center uppercase tracking-[0.15em]">数据分布</h3>
                    <div class="h-32 w-full" ref="chartRef"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex flex-col md:flex-row gap-4 mb-8">
          <div class="relative flex-grow">
            <input
              v-model="searchTerm"
              type="text"
              placeholder="在墨香中检索食材、方剂、典故..."
              class="w-full px-6 py-4 pl-12 bg-white dark:bg-slate-900 rounded-2xl shadow-md border-2 border-amber-100 dark:border-slate-800 focus:border-amber-500 outline-none transition-all dark:text-white text-sm"
            />
            <i class="fa-solid fa-magnifying-glass absolute left-5 top-1/2 -translate-y-1/2 text-amber-500/60 text-sm"></i>
          </div>
          <button
            @click="showAnnotation = !showAnnotation"
            class="px-8 py-4 rounded-2xl font-bold text-xs transition-all flex items-center justify-center gap-2 border-2 shadow-md active:scale-95"
            :class="showAnnotation 
              ? 'bg-amber-700 text-white border-amber-700' 
              : 'bg-white text-amber-800 border-amber-100 dark:bg-slate-900 dark:border-slate-800 dark:text-amber-200'"
          >
            <i :class="['fa-solid', showAnnotation ? 'fa-eye' : 'fa-eye-slash']"></i>
            {{ showAnnotation ? "隐藏注解" : "显示注解" }}
          </button>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
          <div class="lg:col-span-3">
            <div class="bg-white dark:bg-slate-900 rounded-2xl border-2 border-amber-100 dark:border-slate-800 p-5 sticky top-24 shadow-md dark:shadow-slate-950/50">
              <h3 class="text-base font-bold text-amber-950 dark:text-amber-200 mb-4 font-serif border-b-2 border-amber-50 dark:border-slate-800 pb-3 flex items-center gap-2">
                <i class="fa-solid fa-scroll text-amber-600 text-xs"></i> 目录索引
              </h3>
              <nav class="space-y-1.5 max-h-[50vh] overflow-y-auto custom-scrollbar pr-2">
                <button
                  v-for="chapter in chapters" :key="chapter.chapterId"
                  @click="selectChapter(chapter)"
                  class="w-full text-left px-4 py-3 rounded-xl transition-all flex justify-between items-center group relative overflow-hidden"
                  :class="selectedChapter?.chapterId === chapter.chapterId 
                    ? 'bg-amber-700 text-white shadow-md' 
                    : 'hover:bg-amber-50 dark:hover:bg-slate-800 text-amber-900 dark:text-slate-400'"
                >
                  <div v-if="selectedChapter?.chapterId === chapter.chapterId" class="absolute left-0 top-0 bottom-0 w-1 bg-amber-400"></div>
                  <span class="truncate font-serif text-sm pl-1">{{ chapter.chapterTitle }}</span>
                  <span class="text-[9px] font-mono opacity-60 px-1.5 py-0.5 rounded-full border border-current">
                    {{ (chapter.ingredientCount ?? 0) + (chapter.dishCount ?? 0) }}
                  </span>
                </button>
              </nav>
            </div>
          </div>

          <div class="lg:col-span-9">
            <div class="bg-white dark:bg-slate-900 rounded-3xl border-2 border-amber-100 dark:border-slate-800 shadow-sm p-8 lg:p-12 min-h-[600px] relative">
              <div class="mb-10 border-b border-amber-50 dark:border-slate-800 pb-6 relative">
                <h2 class="text-2xl font-bold text-amber-950 dark:text-amber-100 font-serif">{{ selectedChapter?.chapterTitle }}</h2>
                <p class="text-amber-600 dark:text-amber-500 text-xs mt-4 flex items-center font-bold tracking-[0.2em] uppercase">
                    <span class="w-8 h-[1px] bg-amber-400 mr-3"></span>
                    {{ mapCategory(book?.bookCategory || '') === '菜品专著' ? '精选菜肴 · DISHES' : '四气五味 · FLAVORS' }}
                </p>
                <i class="fa-solid fa-feather absolute right-0 top-0 text-amber-50 dark:text-slate-800 text-4xl -z-0 opacity-30"></i>
              </div>

              <transition mode="out-in" enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0 translate-y-4" leave-to-class="opacity-0 -translate-y-4">
                <div :key="currentPage + (selectedChapter?.chapterId || '')" class="space-y-10 relative z-10">
                  <div v-for="item in displayItems" :key="item.id" class="group relative">
                    <div class="flex flex-wrap items-baseline gap-3 mb-2">
                      <h4 class="text-xl font-bold text-amber-950 dark:text-amber-100 font-serif group-hover:text-amber-700 transition-colors">{{ item.name }}</h4>
                      <span v-if="item.nature" class="px-2.5 py-0.5 text-[11px] font-bold rounded border border-amber-200 dark:border-amber-800 text-amber-700 dark:text-amber-400 uppercase bg-amber-50/50 dark:bg-slate-800 shadow-sm">
                        {{ item.nature }}
                      </span>
                    </div>
                    
                    <p class="text-amber-900/80 dark:text-slate-300 leading-relaxed mb-4 text-base font-serif pl-4 border-l-2 border-amber-100 dark:border-slate-800">
                      {{ item.effect }}
                    </p>
                    
                    <transition enter-active-class="duration-500 ease-out" enter-from-class="max-h-0 opacity-0" enter-to-class="max-h-60 opacity-100">
                      <div v-show="showAnnotation" class="overflow-hidden bg-[#faf7ed] dark:bg-slate-800/50 rounded-xl p-4 border-l-4 border-amber-400 border border-amber-100 dark:border-slate-700 mt-2">
                        <p class="text-[13px] text-amber-900/70 dark:text-slate-400 italic leading-relaxed flex gap-2">
                          <i class="fa-solid fa-quote-left text-base opacity-20 text-amber-800"></i>
                          <span>{{ item.evidence || '暂无详细古籍记载' }}</span>
                        </p>
                      </div>
                    </transition>

                    <div v-if="mapCategory(book?.bookCategory || '') === '菜品专著'" class="mt-4">
                      <RouterLink :to="`/dishes/${item.id}`" class="inline-flex items-center gap-2 text-[10px] font-bold text-amber-700 hover:text-amber-900 transition-all bg-amber-50 dark:bg-slate-800 px-3 py-1.5 rounded-lg border border-amber-100 dark:border-slate-700">
                        查看烹饪典籍 <i class="fa-solid fa-arrow-right-long transition-transform group-hover:translate-x-1"></i>
                      </RouterLink>
                    </div>
                  </div>

                  <div v-if="displayItems.length === 0" class="text-center py-20 text-stone-400">
                    <i class="fa-solid fa-wind mb-4 text-2xl opacity-20"></i>
                    <p class="font-serif">此章节暂未发现相关记载</p>
                  </div>
                </div>
              </transition>

              <div v-if="totalPages > 1" class="mt-12 flex justify-center items-center gap-6 border-t border-amber-50 dark:border-slate-800 pt-8">
                <button 
                  @click="currentPage--" 
                  :disabled="currentPage <= 1" 
                  class="w-9 h-9 rounded-full border border-amber-200 text-amber-600 flex items-center justify-center disabled:opacity-10 hover:bg-amber-50 transition-all shadow-sm"
                >
                  <i class="fa-solid fa-chevron-left text-xs"></i>
                </button>
                
                <div class="font-serif text-lg text-amber-950 dark:text-amber-200 flex items-baseline gap-1">
                  <span class="font-bold underline decoration-amber-300 underline-offset-4">{{ currentPage }}</span> 
                  <span class="opacity-20 mx-1 text-sm">/</span> 
                  <span class="opacity-40 text-xs">{{ totalPages }}</span>
                </div>
                
                <button 
                  @click="currentPage++" 
                  :disabled="currentPage >= totalPages" 
                  class="w-9 h-9 rounded-full border border-amber-200 text-amber-600 flex items-center justify-center disabled:opacity-10 hover:bg-amber-50 transition-all shadow-sm"
                >
                  <i class="fa-solid fa-chevron-right text-xs"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<style scoped>
/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e9d8a6;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #ca8a04;
}

/* 适配暗黑模式滚动条 */
.dark .custom-scrollbar::-webkit-scrollbar-track {
  background: #1e293b;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background: #475569;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}

/* 图片样式优化 */
img {
  display: block;
  min-height: 100%;
  background-color: #f5f5f5;
  background-image: linear-gradient(45deg, #eee 25%, transparent 25%, transparent 75%, #eee 75%, #eee), 
                    linear-gradient(45deg, #eee 25%, transparent 25%, transparent 75%, #eee 75%, #eee);
  background-size: 6px 6px;
  background-position: 0 0, 3px 3px;
  transition: opacity 0.3s ease;
}

.dark img {
  background-color: #2d3748;
  background-image: linear-gradient(45deg, #4a5568 25%, transparent 25%, transparent 75%, #4a5568 75%, #4a5568), 
                    linear-gradient(45deg, #4a5568 25%, transparent 25%, transparent 75%, #4a5568 75%, #4a5568);
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.chart-container-bg {
  position: relative;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.05) 0%, transparent 70%);
}
</style>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch, computed, nextTick } from "vue";
import { useRoute, RouterLink } from "vue-router";
import * as echarts from "echarts";
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";

// 导入 API 接口
import { getBookById, updateBookViewCount, type AncientBook } from "@/api/ancientBooks";
import { getBookChaptersByBookId, type BookChapters } from "@/api/bookChapters";
// 导入包含全量查询功能的接口
import { getIngredientsByChapterAll } from "@/api/foodIngredients"; 
import { getDishesByChapterAll } from "@/api/dishes";

// 路由与基础状态
const route = useRoute();
const book = ref<AncientBook | null>(null);
const loading = ref(true);
const error = ref<string | null>(null);

// 页面交互状态
const showAnnotation = ref(false);
const searchTerm = ref("");
const imageLoading = ref(true);

// 数据存储 (全量与分页)
const allChapterItems = ref<any[]>([]); // 存储当前章节的原始全量数据
const currentPage = ref(1);
const pageSize = 3;

// 图表相关
const chartRef = ref<HTMLElement | null>(null);
let chartInstance: echarts.ECharts | null = null;

// 章节相关
const chapters = ref<BookChapters[]>([]);
const selectedChapter = ref<BookChapters | null>(null);


// 过滤逻辑：基于全量数据和搜索词
const filteredItems = computed(() => {
  const keyword = searchTerm.value.trim().toLowerCase();
  
  // 如果搜索框为空，返回该章节全量数据
  if (!keyword) return allChapterItems.value;

  // 仅匹配 item.name 字段
  return allChapterItems.value.filter(item => {
    return item.name.toLowerCase().includes(keyword);
  });
});

// 分页切片：从过滤后的结果中截取当前页
const displayItems = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  const end = start + pageSize;
  return filteredItems.value.slice(start, end);
});

// 总页数计算
const totalPages = computed(() => {
  const pages = Math.ceil(filteredItems.value.length / pageSize);
  return pages > 0 ? pages : 1;
});

// 工具方法 

const normalizeCover = (url?: string) => {
  if (!url) return "";
  if (/^https?:\/\//.test(url)) return url;
  if (url.startsWith("/")) return url;
  return "/" + url;
};

const mapCategory = (apiCategory: string): string => {
  const map: Record<string, string> = {
    FOOD_THERAPY: "菜品专著",
    DIETARY: "菜品专著",
    COOKING: "菜品专著",
    YINSHAN: "菜品专著",
    SUISHI: "菜品专著",
    PHARMACY: "食材专著",
    HERBAL: "食材专著",
    BENCAO: "食材专著",
    FOOD_MATERIAL: "食材专著",
  };
  return map[apiCategory] || apiCategory;
};

// 数据加载逻辑
const fetchBookDetail = async () => {
  loading.value = true;
  error.value = '';
  try {
    const bookId = route.params.id as string;
    const bookData = await getBookById(bookId);
    if (bookData) {
      book.value = bookData;
      try {
        console.log('开始更新访问量，书籍ID:', bookId);
        const success = await updateBookViewCount(bookId);
        if (success) {
          console.log('访问量更新成功');
          // 重新获取书籍数据以获取最新的访问量
          const updatedBookData = await getBookById(bookId);
          if (updatedBookData) {
            book.value = updatedBookData;
          }
        } else {
          console.warn('访问量更新返回false');
        }
      } catch (err) {
        console.error('更新访问量失败:', err);
      }
      const chaptersData = await getBookChaptersByBookId(bookId);
      chapters.value = chaptersData.sort((a, b) => a.chapterSeq - b.chapterSeq);

      if (chapters.value.length > 0) {
        selectedChapter.value = chapters.value[0];
        await loadChapterContent();
        // 确保 DOM 渲染后初始化图表
        nextTick(() => {
          renderChart();
        });
      }
    } else {
      error.value = '古籍不存在';
    }
  } catch (err) {
    error.value = '加载失败';
  } finally {
    loading.value = false;
  }
};



//加载章节内容
const loadChapterContent = async () => {
  if (!selectedChapter.value || !book.value) return;

  const chapterId = selectedChapter.value.chapterId;
  const category = mapCategory(book.value.bookCategory);

  try {
    let rawList = [];
    if (category === "食材专著") {
      // 调用食材全量接口
      const res = await getIngredientsByChapterAll(chapterId);
      rawList = res.map(item => ({
        id: String(item.ingredientId),
        name: item.ingredientName,
        nature: item.property || "",
        effect: item.ancientEffect || "",
        evidence: item.ancientOriginText || "",
      }));
    } else {
      // 调用菜品全量接口
      const res = await getDishesByChapterAll(chapterId);
      rawList = res.map(dish => ({
        id: String(dish.dishId),
        name: dish.dishName,
        nature: dish.kind || "",
        effect: [
          dish.effectTag1, dish.effectTag2, dish.effectTag3, dish.effectTag4, dish.effectTag5
        ].filter(Boolean).join("、"),
        evidence: dish.ancientCitation || "",
      }));
    }

    // 存入全量数组，触发计算属性联动
    allChapterItems.value = rawList;
    currentPage.value = 1; // 切换章节重置页码
  } catch (err) {
    console.error("加载章节条目失败:", err);
    allChapterItems.value = [];
  }
};

const selectChapter = (chapter: BookChapters) => {
  selectedChapter.value = chapter;
  // watch 会触发 loadChapterContent
};

// 图表渲染

const renderChart = () => {
  if (!chartRef.value || !chapters.value.length) return;
  if (!chartInstance) chartInstance = echarts.init(chartRef.value);

  const chartData = chapters.value
    .map(ch => ({
      name: ch.chapterTitle,
      value: (ch.ingredientCount ?? 0) + (ch.dishCount ?? 0)
    }))
    .filter(item => item.value > 0);

  const option = {
    tooltip: {
      trigger: 'item',
      padding: [10, 15],
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderRadius: 8,
      textStyle: { color: '#78350f' },
      borderWidth: 1,
      borderColor: '#fcd34d',
      formatter: '{b}<br/><span style="font-weight:bold; font-size:16px;">{c}</span> 条目'
    },
    series: [
      {
        type: 'pie',
        radius: ['55%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2,
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.05)'
        },
        label: { show: false },
        color: ['#b45309', '#d97706', '#f59e0b', '#fbbf24', '#d4d4d8'],
        data: chartData,
        emphasis: {
          scale: true,
          scaleSize: 5,
          itemStyle: {
            shadowBlur: 20,
            shadowColor: 'rgba(180, 83, 9, 0.2)'
          }
        }
      },
      {
        type: 'pie',
        radius: ['48%', '49%'],
        center: ['50%', '50%'],
        silent: true,
        label: { show: false },
        itemStyle: { color: '#e5e7eb' },
        data: [{ value: 1 }]
      }
    ],
    graphic: {
      elements: [
        {
          type: 'text',
          left: 'center',
          top: '42%',
          style: {
            text: '典籍分布',
            fill: '#92400e',
            fontSize: 12,
            fontFamily: 'Noto Serif SC',
            letterSpacing: 2
          }
        },
        {
          type: 'text',
          left: 'center',
          top: '52%',
          style: {
            text: 'CHAPTERS',
            fill: '#d4d4d8',
            fontSize: 10,
            fontWeight: 'bold'
          }
        }
      ]
    }
  };

  chartInstance.setOption(option);
};

// 生命周期

onMounted(() => {
  fetchBookDetail();
  window.addEventListener("resize", () => chartInstance?.resize());
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", () => chartInstance?.resize());
  chartInstance?.dispose();
});

// 监听器
watch(() => route.params.id, () => {
  chartInstance?.clear();
  fetchBookDetail();
});

// 监听章节切换
watch(selectedChapter, () => {
  loadChapterContent();
});

// 监听搜索词，搜索时自动跳回第一页
watch(searchTerm, () => {
  currentPage.value = 1;
});

// 章节数据变化后重绘图表
watch(chapters, (newChapters) => {
  if (newChapters.length > 0) {
    setTimeout(renderChart, 300);
  }
}, { deep: true });

</script>
