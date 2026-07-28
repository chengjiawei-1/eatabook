<template>
  <div class="space-y-8 antialiased">

    <div class="bg-[#fdfcf8] dark:bg-slate-800 rounded-3xl shadow-xl border-2 border-[#f2ece4] dark:border-slate-700 overflow-hidden">
      <div class="p-6 sm:p-8">
        
        <div class="bg-white dark:bg-slate-900 rounded-2xl p-6 border-2 border-[#f2ece4] dark:border-slate-700">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-xl font-serif font-bold text-[#6d4c3d] dark:text-[#deb887] flex items-center">
              <i class="fa-solid fa-chart-line mr-3 text-[#d4a373]"></i>九种体质变化趋势
            </h3>
            <div class="text-xs text-[#c4a484] font-bold tracking-widest uppercase bg-[#fdfaf7] px-3 py-1 rounded-lg border border-[#f2ece4]">
              Recent 6 Tests
            </div>
          </div>
          <div ref="lineRef" class="h-96 w-full bg-white dark:bg-slate-800 rounded-xl"></div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mt-8">
          
          <div class="lg:col-span-2 bg-[#fdfaf7] dark:bg-slate-900/50 rounded-2xl p-6 border-2 border-[#f2ece4] dark:border-slate-700 relative overflow-hidden">
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-6">
              <h3 class="text-xl font-serif font-bold text-[#6d4c3d] dark:text-[#deb887] flex items-center">
                <span class="w-1.5 h-4 bg-[#d4a373] mr-2 rounded-full"></span>
                体质深度分析
              </h3>
              <div class="flex items-center gap-2">
                <button
                  @click="toggleAnalysisSpeech"
                  :disabled="!analysisSpeechText"
                  class="tool-btn"
                  :title="isSpeakingAnalysis ? '停止朗读' : '朗读体质分析'"
                >
                  <i :class="isSpeakingAnalysis ? 'fa-solid fa-volume-xmark' : 'fa-solid fa-volume-high'"></i>
                  <span>{{ isSpeakingAnalysis ? '停止' : '朗读' }}</span>
                </button>
              </div>
            </div>
            <p v-if="cardStatus" class="mb-4 text-xs text-[#8a6d51] bg-white/70 border border-[#f2ece4] rounded-lg px-3 py-2">
              {{ cardStatus }}
            </p>
            <div class="space-y-4 relative z-10">
            <div class="text-lg text-gray-700 dark:text-gray-300 leading-[1.8] font-serif min-h-24">
              <template v-if="aiAnalyzing && !aiAnalysisBuffer">
                <div class="flex items-center gap-3 text-[#8a6d51] animate-pulse">
                  <i class="fa-solid fa-yin-yang fa-spin text-2xl"></i>
                  正在为您分析体质变化...
                </div>
              </template>
            <div 
              v-if="aiAnalysisBuffer || !aiAnalyzing" 
              class="whitespace-pre-wrap break-words"
              style="white-space: pre-wrap !important; display: block;"
            >
              {{ aiAnalysisBuffer || aiAnalysis.overallAnalysis }}
            </div>
            </div>
            </div>
            <i class="fa-solid fa-stamp absolute -bottom-4 -right-4 text-[#d4a373]/5 text-7xl"></i>
          </div>

          <div class="space-y-8">
            <div class="bg-white dark:bg-slate-900 rounded-2xl p-6 border-2 border-[#d4a373]/20 dark:border-slate-700 shadow-sm">
              <div class="flex justify-between items-center mb-6">
                <h3 class="text-lg font-serif font-bold text-[#8a6d51] dark:text-[#deb887]">最新体质分数</h3>
                <div class="text-[10px] font-bold py-1 px-2 bg-[#fdfaf7] border border-[#f2ece4] text-[#c4a484] rounded-full shadow-sm">
                  {{ recentTest.date || '暂无数据' }}
                </div>
              </div>
              
              <div class="space-y-5" v-if="topThreeConstitutions.length > 0">
                <div v-for="(constitution, index) in topThreeConstitutions" :key="constitution.name" class="space-y-2 group">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center">
                      <div class="w-3 h-3 rounded-full mr-3 shadow-inner" :style="{ backgroundColor: getConstitutionColor(constitution.name) }"></div>
                      <span class="text-sm font-bold text-[#6d4c3d] dark:text-gray-300">{{ constitution.name }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-serif font-black text-[#8a6d51]">{{ constitution.score }}分</span>
                      <div class="w-5 h-5 flex items-center justify-center text-[10px] text-white rounded shadow-sm" 
                           :class="[index === 0 ? 'bg-[#d4a373]' : index === 1 ? 'bg-[#c4a484]' : 'bg-[#e3d5ca]']">
                        {{ index + 1 }}
                      </div>
                    </div>
                  </div>
                  <div class="w-full bg-[#f2ece4] dark:bg-slate-700 rounded-full h-2 overflow-hidden shadow-inner">
                    <div 
                      class="h-full rounded-full transition-all duration-700 ease-out" 
                      :style="{ width: `${constitution.score}%`, backgroundColor: getConstitutionColor(constitution.name) }"
                    ></div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center text-[#c4a484] py-8 text-sm italic font-serif">暂无测评历史记录</div>
            </div>

            <div class="bg-[#fdfaf7] dark:bg-slate-900 rounded-2xl p-6 border-2 border-[#8a6d51]/20 dark:border-slate-700 relative overflow-hidden">
              <h3 class="text-lg font-serif font-bold text-[#8a6d51] dark:text-[#deb887] mb-4 flex items-center gap-2">
                <i class="fa-solid fa-bell text-[#d4a373]"></i> 下次复测提醒
              </h3>
              <div class="mb-5">
                <p class="text-xs text-[#a67c52] dark:text-amber-400 mb-2 leading-relaxed italic">
                  “凡事预则立”，建议您每15天测试一次，以便根据体质变化调整养生方针。
                </p>
              </div>
              <div class="flex items-center justify-between bg-white dark:bg-slate-800 p-4 rounded-xl border border-[#f2ece4]">
                <div class="text-[#8a6d51]">
                  <span class="text-2xl font-serif font-black">{{ nextTestReminder.daysLeft }}</span>
                  <span class="text-xs ml-1 font-bold opacity-60">天后</span>
                </div>
                <button
                  @click="showDatePicker = !showDatePicker"
                  class="bg-[#8a6d51] hover:bg-[#6d4c3d] text-white text-xs px-4 py-2 rounded-lg transition-all shadow-md active:scale-95 font-bold"
                >
                  <i class="fa-solid fa-gear mr-1"></i> 设置
                </button>
              </div>
              
              <div v-if="showDatePicker" class="mt-4 p-4 bg-white dark:bg-slate-800 rounded-xl border-2 border-[#d4a373] shadow-2xl relative z-10">
                <div class="space-y-3">
                  <input 
                    type="date" 
                    v-model="nextTestDate"
                    :min="minDate"
                    class="w-full text-sm px-3 py-2 rounded-lg border border-[#f2ece4] focus:border-[#d4a373] outline-none bg-white text-[#6d4c3d] font-bold shadow-sm"
                  />
                  <input 
                    type="time" 
                    v-model="nextTestTime"
                    class="w-full text-sm px-3 py-2 rounded-lg border border-[#f2ece4] focus:border-[#d4a373] outline-none bg-white text-[#6d4c3d] font-bold shadow-sm"
                  />
                  <div class="flex gap-2 mt-4">
                    <button 
                      @click="saveReminder"
                      class="flex-1 py-2 bg-[#8a6d51] text-white text-xs rounded-lg font-bold hover:bg-[#6d4c3d] transition-colors"
                    >
                      确认
                    </button>
                    <button 
                      @click="showDatePicker = false"
                      class="flex-1 py-2 bg-[#f2ece4] text-[#8a6d51] text-xs rounded-lg font-bold hover:bg-gray-200 transition-colors"
                    >
                      取消
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 核心宋体字体 */
.font-serif {
  font-family: "Source Han Serif CN", serif;
}

/* 宣纸滚动条 */
::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #e3d5ca;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #d4a373;
}

/* 进度条动画增强 */
.h-full.rounded-full {
  box-shadow: 0 0 10px rgba(212, 163, 115, 0.15);
}

/* 输入框微调 */
input[type="date"], input[type="time"] {
  accent-color: #8a6d51;
}

.tool-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid #eadbca;
  background: #fffaf4;
  color: #8a6d51;
  font-size: 12px;
  font-weight: 700;
  transition: all 0.2s ease;
}

.tool-btn:hover:not(:disabled) {
  background: #8a6d51;
  color: #fff;
}

.tool-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* 响应式微调 */
@media (max-width: 640px) {
  .text-3xl { font-size: 1.5rem; }
  .p-6 { padding: 1rem; }
}
</style>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import { useAuthStore } from '@/stores/auth';
import { getHistoryAPI } from '@/api/constitution';
import { chatStream } from '@/api/ai';
import { setReminderAPI } from '@/api/user';
import { getAnalysisCache, saveAnalysisCache } from '@/api/analysisCache';

const AI_FAILURE_MARKERS = [
  "AI 服务请求过于频繁",
  "AI 服务调用失败",
  "Too Many Requests",
  "rate limiting",
  "System is too busy",
  "Request was rejected",
  "50609",
  "429"
];

const isAiFailureText = (text: string) => {
  return !!text && AI_FAILURE_MARKERS.some(marker => text.includes(marker));
};

const router = useRouter();
const auth = useAuthStore();

//图表逻辑
const lineRef = ref<HTMLElement | null>(null);
let lineChart: echarts.ECharts | null = null;

const physiqueHistory = ref<any[]>([]);
const aiAnalysis = ref({
  overallAnalysis: '正在等待体质数据分析...',
  dietAdvice: []
});
const topThreeConstitutions = ref<any[]>([]);
const recentTest = ref({ date: '' });
const cardStatus = ref('');
const isSpeakingAnalysis = ref(false);

const analysisSpeechText = computed(() => {
  const text = normalizeAnalysisText(aiAnalysisBuffer.value || aiAnalysis.value.overallAnalysis || '');
  return text.startsWith('正在') ? '' : text;
});

// 下次测试提醒
const nextTestReminder = ref({ daysLeft: 15 });
const showDatePicker = ref(false);
const nextTestDate = ref('');
const nextTestTime = ref('09:00'); // 添加时间选择变量，默认上午9点
const minDate = new Date().toISOString().split('T')[0];

// 体质颜色配置
const constitutionColors: Record<string, string> = {
  '平和质': '#1dd1a1',
  '气虚质': '#45b7d1',
  '阴虚质': '#4ecdc4',
  '痰湿质': '#54a0ff',
  '阳虚质': '#ff6b6b',
  '湿热质': '#feca57',
  '血瘀质': '#5f27cd',
  '气郁质': '#ff9ff3',
  '特禀质': '#ff9f43'
};

const currentUserId = computed(() => auth.user?.userId);

const buildAIMessage = (recent6: any[]) => {
  return `
数据说明：
testTime 表示测试时间。
mainConstitution 表示主体质。
constitutionConvertScores 表示九种体质转化分，范围0至100。
数据按时间正序排列，最后一条为第6次审计数据。

最近 6 次测试数据如下（JSON）：
${JSON.stringify(recent6, null, 2)}
`;
};

const normalizeAnalysisText = (text: string) => {
  return String(text || '')
    .replace(/\r\n?/g, '\n')
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n[ \t]+/g, '\n')
    .replace(/([^\n])([一二三四五六七八九十]、)/g, '$1\n$2')
    .replace(/\n{3,}/g, '\n\n')
    .trim();
};

// 新增 AI 分析内容流式显示变量
const aiAnalysisBuffer = ref(""); // 当前已经展示的内容
const aiAnalyzing = ref(false);

// 流式调用 AI 分析
const callAIAnalysis = async (recent6: any[]) => {
  if (!currentUserId.value || recent6.length === 0) return;

  aiAnalyzing.value = true;
  aiAnalysisBuffer.value = "";

  const latestTest = recent6[recent6.length - 1];
  
// 去掉毫秒零头，确保前后端匹配更稳
  const latestTimeMillis = Math.floor(new Date(latestTest.testTime).getTime() / 1000) * 1000;

  try {
    // 调用接口时传入数字类型，此时 TS 不再报错
    const cachedData = await getAnalysisCache(currentUserId.value, latestTimeMillis);

    if (cachedData && cachedData.trim() !== "") {
// 命中缓存：直接展示
      const processedContent = normalizeAnalysisText(cachedData);
      aiAnalysisBuffer.value = processedContent;
      aiAnalysis.value.overallAnalysis = processedContent; 
      aiAnalyzing.value = false;
      return;
    }

    // 执行原有的 AI 流式调用逻辑
    console.log("%c[缓存未命中] 正在请求 AI 生成分析...", "color: #d4a373; font-weight: bold;");
    const message = buildAIMessage(recent6);
    await chatStream(
      {
        agent: "changeanalysis",
        prompt: message
      },
      (chunk: string) => {
        aiAnalysisBuffer.value += chunk;
      },
async () => {
        console.log("%c[AI 原始输出]", "color: #d4a373; font-weight: bold;", aiAnalysisBuffer.value);

        const rawContent = aiAnalysisBuffer.value;
        const processedContent = normalizeAnalysisText(rawContent);

        // 最终赋值
        if (isAiFailureText(processedContent)) {
          aiAnalysisBuffer.value = "AI 分析暂时没有生成成功，请稍后刷新后再试。本次失败不会写入缓存。";
          aiAnalysis.value.overallAnalysis = aiAnalysisBuffer.value;
          aiAnalyzing.value = false;
          return;
        }

        aiAnalysisBuffer.value = processedContent;
        aiAnalysis.value.overallAnalysis = processedContent;
        aiAnalyzing.value = false;
        
        //新增：AI 生成完毕后，立即保存到数据库
        const latestTest = physiqueHistory.value[physiqueHistory.value.length - 1];
        if (latestTest) {
          const ts = Math.floor(new Date(latestTest.testTime).getTime() / 1000) * 1000;
          console.log("准备保存缓存，时间戳为:", ts);
          const isSaved = await saveAnalysisCache(currentUserId.value, processedContent, ts);
          console.log(isSaved ? "✅ 分析已存入缓存" : "❌ 缓存保存失败");
        }

        //核心调试日志
        console.group("排版结果检查");
        console.log("最终字符串(带转义):", JSON.stringify(processedContent));
        if (processedContent.includes('\n')) {
          console.log("%c成功识别并插入了换行符！", "color: #1dd1a1; font-weight: bold;");
        } else {
          console.warn("未能识别到关键词，未插入任何换行。请检查 AI 返回的内容是否包含 '一、' 等字样");
        }
        console.groupEnd();
      }
    );
  } catch (error) {
    console.error("AI 流式分析失败:", error);
    aiAnalysis.value.overallAnalysis = "AI 分析失败，请稍后重试";
    aiAnalyzing.value = false;
  }
};

// 加载体质历史数据
const loadPhysique = async () => {
  if (!currentUserId.value) return;
  
  try {
    const res = await getHistoryAPI(currentUserId.value);
    const rawHistory = Array.isArray(res) ? res : (res as any).data || [];
    
    if (rawHistory.length === 0) {
      aiAnalysis.value.overallAnalysis = '您还没有进行过体质测试，快去测试一下吧！';
      return;
    }

    // 按时间正序排列
    const sortedHistory = rawHistory.sort((a: any, b: any) =>
      new Date(a.testTime).getTime() - new Date(b.testTime).getTime()
    );
    
    // 取最近6次数据
    const recent6 = sortedHistory.slice(-6);
    physiqueHistory.value = recent6;
    
    // 更新最新信息
    const latest = recent6[recent6.length - 1];
    updateLatestInfo(latest);
    
    // 初始化图表
    nextTick(() => {
      setTimeout(() => {
        if (lineRef.value) {
          if (!lineChart) {
            lineChart = echarts.init(lineRef.value);
          }
          updateLineChart(recent6);
        }
      }, 100); // 100ms 缓冲区
    });
    
    // 调用AI分析
    callAIAnalysis(recent6);
  } catch (error) {
    console.error('加载体质历史失败', error);
    aiAnalysis.value.overallAnalysis = '数据加载失败，请稍后重试';
  }
};

// 优化 window resize 逻辑 
let resizeObserver: ResizeObserver | null = null;

// 更新最新体质信息
const updateLatestInfo = (latestData: any) => {
  recentTest.value.date = latestData.testTime.split('T')[0];
  
  try {
    const scores = JSON.parse(latestData.constitutionConvertScores);
    const typeMap: any = {
      balanced: '平和质', qiDeficiency: '气虚质', yinDeficiency: '阴虚质',
      phlegmDampness: '痰湿质', yangDeficiency: '阳虚质', dampHeat: '湿热质',
      bloodStasis: '血瘀质', qiStagnation: '气郁质', specialEndowment: '特禀质'
    };
    
    const parsedScores: any[] = [];
    for (const key in scores) {
      if (typeMap[key]) {
        parsedScores.push({ name: typeMap[key], score: scores[key] });
      }
    }
    
    parsedScores.sort((a, b) => b.score - a.score);
    topThreeConstitutions.value = parsedScores.slice(0, 3);
  } catch (e) {
    console.error('解析分数失败', e);
    topThreeConstitutions.value = [];
  }
};

// 更新折线图 - 修复乱码问题
const updateLineChart = (data: any[]) => {
  if (!lineChart || !lineRef.value) return;

  // 获取容器实际宽度，如果宽度为0则延迟重试
  if (lineRef.value.clientWidth === 0) {
    setTimeout(() => updateLineChart(data), 100);
    return;
  }
  
  const dates = data.map(item => {
    const date = new Date(item.testTime);
    return `${date.getMonth() + 1}-${date.getDate()}`;
  });
  
  const typeMapReverse: any = {
    balanced: '平和质', qiDeficiency: '气虚质', yinDeficiency: '阴虚质',
    phlegmDampness: '痰湿质', yangDeficiency: '阳虚质', dampHeat: '湿热质',
    bloodStasis: '血瘀质', qiStagnation: '气郁质', specialEndowment: '特禀质'
  };
  
  const seriesList = Object.keys(typeMapReverse).map(engKey => {
    const cnName = typeMapReverse[engKey];
    return {
      name: cnName,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: data.map(item => {
        try {
          const s = JSON.parse(item.constitutionConvertScores);
          return s[engKey] || 0;
        } catch { 
          return 0; 
        }
      }),
      itemStyle: { 
        color: constitutionColors[cnName],
        borderWidth: 2,
        borderColor: '#fff'
      },
      lineStyle: { 
        width: 3,
        shadowColor: 'rgba(0,0,0,0.1)',
        shadowBlur: 5,
        shadowOffsetY: 3
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          borderWidth: 3,
          shadowBlur: 10,
          shadowColor: 'rgba(0,0,0,0.2)'
        }
      }
    };
  });
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#f5deb3',
      borderWidth: 1,
      textStyle: { 
        color: '#333',
        fontSize: 12
      },
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: '#c19a6b',
          width: 1,
          type: 'dashed'
        }
      },
      formatter: function(params: any[]) {
        let result = `<div style="font-weight: bold; margin-bottom: 5px;">${params[0].axisValue}</div>`;
        params.forEach(item => {
          result += `<div style="display: flex; align-items: center; margin: 3px 0;">
            <span style="display: inline-block; width: 10px; height: 10px; background: ${item.color}; border-radius: 50%; margin-right: 5px;"></span>
            <span style="flex: 1;">${item.seriesName}:</span>
            <span style="font-weight: bold;">${item.value}分</span>
          </div>`;
        });
        return result;
      }
    },
    legend: {
      data: Object.values(typeMapReverse),
      bottom: 0,
      textStyle: { 
        color: '#666',
        fontSize: 11
      },
      itemGap: 10,
      itemWidth: 20,
      itemHeight: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '18%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { 
        lineStyle: { 
          color: '#c19a6b',
          width: 1
        } 
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        interval: 0
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      max: 100,
      min: 0,
      interval: 20,
      axisLine: { 
        show: false 
      },
      axisLabel: {
        color: '#666',
        fontSize: 12,
        formatter: '{value}分'
      },
      splitLine: { 
        lineStyle: { 
          type: 'dashed', 
          color: '#eee',
          width: 1
        } 
      }
    },
    series: seriesList
  };
  
  // 清除之前的配置并重绘
  lineChart.resize();
  lineChart.clear();
  lineChart.setOption(option, true);
};

// 辅助函数
const getRankColor = (index: number) => {
  const colors = ['bg-yellow-500 text-white', 'bg-gray-400 text-white', 'bg-amber-700 text-white'];
  return colors[index] || 'bg-gray-300';
};

const getConstitutionColor = (name: string) => {
  return constitutionColors[name] || '#ccc';
};

let analysisSpeechStopRequested = false;

const stopAnalysisSpeech = () => {
  analysisSpeechStopRequested = true;
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
  isSpeakingAnalysis.value = false;
};

const toggleAnalysisSpeech = () => {
  if (isSpeakingAnalysis.value) {
    stopAnalysisSpeech();
    return;
  }
  if (!('speechSynthesis' in window)) {
    cardStatus.value = '当前浏览器不支持语音播报。';
    return;
  }

  const text = analysisSpeechText.value;
  if (!text) return;
  analysisSpeechStopRequested = false;
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = 'zh-CN';
  utterance.rate = 0.95;
  utterance.pitch = 1;
  utterance.onend = () => {
    isSpeakingAnalysis.value = false;
  };
  utterance.onerror = (event) => {
    isSpeakingAnalysis.value = false;
    if (analysisSpeechStopRequested || event.error === 'canceled' || event.error === 'interrupted') {
      return;
    }
    cardStatus.value = '语音播报失败，请检查浏览器权限或系统语音设置。';
  };
  window.speechSynthesis.cancel();
  window.speechSynthesis.speak(utterance);
  isSpeakingAnalysis.value = true;
};

// 保存提醒设置
const saveReminder = async () => {
  if (!nextTestDate.value) {
    alert('请选择日期！');
    return;
  }
  
  const selectedDate = new Date(nextTestDate.value);
  const today = new Date();
  const diffTime = selectedDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  // 格式化时间为 yyyy-MM-dd HH:mm:ss 格式
  const formattedTime = `${nextTestDate.value} ${nextTestTime.value}:00`;
  
  try {
    console.log('开始设置提醒，参数:', {
      userId: currentUserId.value,
      time: formattedTime
    });
    
    // 调用后端API
    const response = await setReminderAPI(currentUserId.value, formattedTime);
    console.log('API响应:', response);
    
    if (response.code === 200) {
      nextTestReminder.value.daysLeft = diffDays > 0 ? diffDays : 0;
      showDatePicker.value = false;
      alert(`提醒设置成功！将在 ${nextTestReminder.value.daysLeft} 天后（${formattedTime}）提醒您`);
    } else {
      alert(`设置失败：${response.message || '未知错误'}`);
    }
  } catch (error) {
    console.error('设置提醒失败:', error);
    alert('设置提醒失败，请检查网络连接或稍后重试');
  }
};

// 窗口大小变化处理
const handleResize = () => {
  if (lineChart) {
    lineChart.resize();
  }
};

// 错误处理
const handleError = (error: any) => {
  console.error('图表渲染错误:', error);
  aiAnalysis.value.overallAnalysis = '图表渲染出错，请刷新页面重试';
};

// 生命周期
onMounted(() => {
  if (!auth.isAuthenticated) {
    router.push('/login');
    return;
  }
  
  // 添加错误监听
  window.addEventListener('error', handleError);
  
  loadPhysique();
// 使用 ResizeObserver 替代 window resize 监听，能更精准监听容器大小变化
  if (lineRef.value) {
    resizeObserver = new ResizeObserver(() => {
      if (lineChart) {
        lineChart.resize();
      }
    });
    resizeObserver.observe(lineRef.value);
  }
});

onUnmounted(() => {
  stopAnalysisSpeech();
  if (lineChart) {
    lineChart.dispose();
    lineChart = null;
  }
  window.removeEventListener('resize', handleResize);
  window.removeEventListener('error', handleError);
});
</script>
