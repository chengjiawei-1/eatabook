<template>
  <div class="w-full p-5 space-y-6">
    <!-- 社区公告卡片 -->
    <section class="relative overflow-hidden group">
      <div class="flex items-center justify-between mb-3 px-1">
        <h3 class="flex items-center gap-2 text-sm font-bold text-slate-800 dark:text-slate-200">
          <span class="flex items-center justify-center w-6 h-6 rounded-lg bg-amber-100 dark:bg-amber-900/50 text-amber-600">
            <i class="fa-solid fa-bullhorn text-[10px]"></i>
          </span>
          社区公告
        </h3>
        <span class="flex h-2 w-2">
        </span>
      </div>

      <div class="space-y-2">
        <!-- 公告项  -->
        <div class="notice-item group/item">
          <div class="flex gap-3 p-3 rounded-xl bg-white/50 dark:bg-slate-800/50 border border-white dark:border-slate-700 shadow-sm transition-all hover:shadow-md hover:bg-white dark:hover:bg-slate-800">
            <div class="mt-1 w-1.5 h-1.5 rounded-full bg-amber-400 group-hover/item:scale-150 transition-transform"></div>
            <div class="flex-1">
              <p class="text-xs font-medium text-slate-700 dark:text-slate-300 leading-relaxed">
                请遵守论坛社区规范，共建绿色养生环境。
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 分割线 - 减小高度 -->
    <div class="h-px bg-gradient-to-r from-transparent via-amber-200/30 dark:via-slate-600 to-transparent my-2"></div>

    <!-- 词云图卡片 -->
    <section>
      <div class="flex items-center justify-between mb-3 px-1">
        <h3 class="flex items-center gap-2 text-sm font-bold text-slate-800 dark:text-slate-200">
          <span class="flex items-center justify-center w-6 h-6 rounded-lg bg-orange-100 dark:bg-orange-900/50 text-orange-600">
            <i class="fa-solid fa-fire-flame-curved text-[10px]"></i>
          </span>
          探索热词
        </h3>
        <button @click="refreshWordCloud" class="text-[10px] text-slate-400 hover:text-amber-600 transition-colors">
          <i class="fa-solid fa-rotate-right mr-1" :class="{'animate-spin': isRefreshing}"></i>换一批
        </button>
      </div>

      <div class="relative bg-white/40 dark:bg-slate-800/40 rounded-3xl border border-white/60 dark:border-slate-700/50 p-2 overflow-hidden backdrop-blur-sm">
        <div ref="wordRef" class="h-64 w-full"></div>
        
        <!-- 装饰性光晕 -->
        <div class="absolute -bottom-10 -right-10 w-32 h-32 bg-amber-500/5 blur-3xl rounded-full"></div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, nextTick, onUnmounted } from "vue";
import * as echarts from "echarts";
import 'echarts-wordcloud';
import { getTopKeywords } from "@/api/databoard";

const wordRef = ref()
let wordChart: any
let refreshTimer: any = null
const isRefreshing = ref(false)
const keywords = ref<Array<{ text: string; value: number }>>([])
const REFRESH_INTERVAL = 30000

const normalizeKeywords = (data: any) => {
  if (!Array.isArray(data)) return [];
  return data
    .map((item: any) => ({
      text: String(item.text || item.keyword || '').trim(),
      value: Number(item.value || item.searchCount || 0)
    }))
    .filter(item => item.text)
    .slice(0, 20);
};

const loadWordCloud = async () => {
  try {
    const data = await getTopKeywords();
    keywords.value = normalizeKeywords(data);
    nextTick(() => {
      if (!wordRef.value) return;
      if (!wordChart) {
        wordChart = echarts.init(wordRef.value);
      }

      const option = {
        series: [{
          type: 'wordCloud',
          shape: 'rect',
          keepAspect: false,
          sizeRange: [14, 34], // 侧边栏词云字号
          rotationRange: [0, 0], // 不旋转，方便阅读
          gridSize: 4,
          drawOutOfBound: false,
          textStyle: {
            fontFamily: 'sans-serif',
            fontWeight: 600,
            color: function () {
              const colors = ['#d97706', '#f59e0b', '#ea580c', '#f97316', '#78350f'];
              return colors[Math.floor(Math.random() * colors.length)];
            }
          },
          emphasis: {
            textStyle: {
              color: '#92400e',
              textShadowBlur: 8,
              textShadowColor: 'rgba(0,0,0,0.1)'
            }
          },
          data: keywords.value.map((item: any) => ({
            name: item.text,
            value: item.value
          }))
        }]
      };
      wordChart.setOption(option, true);
    });
  } catch (error) {
    console.error('获取词云数据失败:', error);
  }
};

const refreshWordCloud = async () => {
  isRefreshing.value = true;
  await loadWordCloud();
  setTimeout(() => isRefreshing.value = false, 500);
};

const handleResize = () => {
  wordChart?.resize();
};

const handleKeywordsUpdated = () => {
  refreshWordCloud();
};

onMounted(async () => {
  await loadWordCloud();
  refreshTimer = setInterval(loadWordCloud, REFRESH_INTERVAL);
  window.addEventListener("resize", handleResize);
  window.addEventListener("forum-search-keywords-updated", handleKeywordsUpdated);
})

onUnmounted(() => {
  window.removeEventListener("resize", handleResize);
  window.removeEventListener("forum-search-keywords-updated", handleKeywordsUpdated);
  if (refreshTimer) clearInterval(refreshTimer);
  if (wordChart) {
    wordChart.dispose()
    wordChart = null
  }
})
</script>

<style scoped>
.notice-item {
  transition: transform 0.3s ease;
}
.notice-item:hover {
  transform: translateX(4px);
}

/* 文字渐变 */
.font-serif {
  font-family: 'Noto Serif SC', serif;
}
</style>
