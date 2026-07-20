<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-slate-900">
    <Nav />

    <main class="flex-grow pt-24 pb-16"> <!-- 还原原有间距 -->
      <div class="container mx-auto px-4">
        <div class="space-y-10"> <!-- 还原原有间距 -->
          
          <!-- ==================== 1. 测试引导页 ==================== -->
          <div
            v-if="step === 1"
            v-motion="{
              initial: { opacity: 0, y: 26, scale: 0.97 },
              enter: { opacity: 1, y: 0, scale: 1, transition: { type: 'spring', stiffness: 170, damping: 16 } },
            }"
            class="text-center"
          >
            <h1 class="text-3xl md:text-4xl font-serif-title font-black text-amber-900 dark:text-amber-200 mb-4">
              中医九种体质辨识
            </h1>
            <p class="font-sans-body text-slate-600 dark:text-slate-400 max-w-2xl mx-auto">
              格物致知，调和阴阳。根据您最近一个月的真实感受回答，我们将为您开启个性化古籍食养之旅
            </p>
            <div class="w-24 h-1 bg-gradient-to-r from-transparent via-amber-400 to-transparent mx-auto mt-8 opacity-60"></div>
          </div>

          <div
            v-if="step === 1"
            v-motion="{
              initial: { opacity: 0, y: 24, scale: 0.97 },
              enter: { opacity: 1, y: 0, scale: 1, transition: { type: 'spring', stiffness: 170, damping: 18 } },
            }"
            class="bg-white dark:bg-slate-800 rounded-xl shadow-md p-6 border border-amber-100 dark:border-slate-700"
          >
            <h2 class="text-xl font-serif-title font-bold text-amber-800 dark:text-amber-100 mb-4 flex items-center">
              <i class="fa-solid fa-circle-info mr-2 text-amber-600"></i>
              测试须知
            </h2>
            <p class="font-sans-body text-slate-700 dark:text-slate-300 mb-6 leading-relaxed">
                 本卷共 <span class="font-bold text-amber-600">55</span> 题，涵盖 9 种中医体质类型。请在平静状态下填写。请注意：这并非医学诊断，而是一种辅助了解自我身体特质的智慧工具。
            </p>

            <!-- 还原九宫格布局 -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
              <div
                v-for="type in constitutionTypes"
                :key="type.id"
                class="bg-amber-50/50 dark:bg-slate-700 p-4 rounded-lg border border-amber-100 dark:border-slate-600"
              >
                <h3 class="font-serif-title font-bold text-amber-900 dark:text-amber-200">{{ type.name }}</h3>
                <p class="text-xs text-slate-500 dark:text-slate-400 mt-1 font-sans-body">{{ type.description }}</p>
              </div>
            </div>

            <div class="text-center">
              <button class="bg-amber-800 hover:bg-amber-700 text-white font-bold py-3 px-10 rounded-lg transition-all shadow-lg active:scale-95" @click="startTest">
                开始测试
                <i class="fa-solid fa-arrow-right ml-2"></i>
              </button>
            </div>
          </div>

          <!-- ==================== 2. 答题过程中 ==================== -->
          <div
            v-else-if="typeof step === 'number' && step > 1"
            v-motion="{ initial: { opacity: 0, y: 20 }, enter: { opacity: 1, y: 0 } }"
            class="bg-white dark:bg-slate-800 rounded-xl shadow-lg p-8 border border-amber-100 dark:border-slate-700"
          >
            <div class="mb-8">
              <div class="flex justify-between items-center mb-2 font-sans-body">
                <span class="text-amber-800 dark:text-amber-200 font-bold">
                  问题 {{ step - 1 }} / {{ allQuestions.length }}
                </span>
                <span class="text-slate-400 text-sm">
                  {{ Math.floor(((step - 1) / allQuestions.length) * 100) }}%
                </span>
              </div>
              <div class="w-full bg-slate-100 dark:bg-slate-700 rounded-full h-2 shadow-inner">
                <div
                  class="bg-amber-500 h-2 rounded-full transition-all duration-300"
                  :style="{ width: `${((step - 1) / allQuestions.length) * 100}%` }"
                ></div>
              </div>
            </div>

            <div class="mb-8 min-h-[200px]">
              <h2 class="text-2xl font-serif-title font-bold text-amber-950 dark:text-amber-100 mb-6 leading-relaxed">
                {{ currentQuestion?.question }}
              </h2>
              <div v-if="currentQuestion?.reverse" class="mb-4 inline-block px-3 py-1 bg-amber-100 text-amber-800 text-[10px] font-bold rounded-full uppercase tracking-widest">
                <i class="fa-solid fa-scale-balanced mr-1"></i>反向计分题
              </div>
              <div class="space-y-3">
                <div
                  v-for="option in scoreOptions"
                  :key="option.score"
                  class="p-4 border-2 rounded-lg cursor-pointer transition-all flex items-center group"
                  :class="
                    answers[currentQuestion!.id] === option.score
                      ? 'border-amber-600 bg-amber-50/50 dark:bg-slate-700'
                      : 'border-slate-50 dark:border-slate-600 hover:border-amber-200 hover:bg-amber-50/10'
                  "
                  @click="selectAnswer(currentQuestion!.id, option.score)"
                >
                  <div
                    class="w-5 h-5 rounded-full border-2 flex items-center justify-center mr-3 transition-colors"
                    :class="
                      answers[currentQuestion!.id] === option.score
                        ? 'border-amber-600 bg-amber-600'
                        : 'border-slate-300 group-hover:border-amber-400'
                    "
                  >
                    <i v-if="answers[currentQuestion!.id] === option.score" class="fa-solid fa-check text-white text-[10px]"></i>
                  </div>
                  <div class="font-sans-body">
                    <span class="font-bold text-slate-700 dark:text-slate-200">{{ option.label }}</span>
                    <span class="text-slate-400 dark:text-slate-400 text-sm ml-2">({{ option.description }})</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex justify-between pt-6 border-t border-slate-50 dark:border-slate-700 font-sans-body">
              <button
                class="px-6 py-2 border border-slate-200 text-slate-400 rounded-lg hover:text-amber-700 transition-colors disabled:opacity-50"
                @click="prev"
                :disabled="step === 2"
              >
                上一题
              </button>
              <button 
                class="px-8 py-2 bg-amber-800 hover:bg-amber-700 text-white font-bold rounded-lg transition-colors disabled:opacity-30 active:scale-95"
                @click="next"
                :disabled="!answers[currentQuestion!.id]"
              >
                {{ step - 1 === allQuestions.length ? "提交测试" : "下一题" }}
              </button>
            </div>
          </div>

<div v-else-if="step === 'results'" v-motion-fade class="space-y-6 max-w-6xl mx-auto pb-0">
            
            <div class="relative bg-gradient-to-br from-[#fdfbf7] to-[#f5f1ea] dark:from-slate-800 dark:to-slate-900 rounded-[2rem] shadow-2xl shadow-orange-900/5 p-6 md:p-8 border border-orange-100/30 overflow-hidden">
              <div class="absolute top-0 right-0 opacity-[0.03] pointer-events-none">
                <i class="fa-solid fa-cloud-sun text-[15rem] -mr-16 -mt-10 text-[#8b4513]"></i>
              </div>
              <div class="relative z-10 flex flex-col md:flex-row gap-8 items-center">
                <div class="flex-shrink-0 text-center md:text-left">
                  <div class="inline-block relative">
                    <span class="absolute -top-3 -left-4 text-4xl text-red-600/10 font-serif">体质</span>
                    <h2 class="text-5xl md:text-6xl font-black text-[#7c4a35] dark:text-[#eecfad] tracking-tighter" style="font-family: 'Noto Serif SC', serif">
                      {{ result?.primaryType }}
                    </h2>
                    <div class="absolute -right-10 bottom-1 w-10 h-10 border-2 border-red-600/60 text-red-600/70 flex items-center justify-center font-bold text-[10px] rotate-12 rounded-sm">
                      经络<br/>调理
                    </div>
                  </div>
                </div>
                <div class="flex-grow space-y-3">
                  <div v-if="result?.secondaryTypes?.length" class="flex flex-wrap gap-2">
                    <span v-for="t in result.secondaryTypes" :key="t" class="px-3 py-0.5 bg-white/60 dark:bg-orange-900/20 text-[#8b4513] dark:text-orange-200 rounded-full text-xs font-medium border border-orange-100 shadow-sm">
                      兼有：{{ t }}
                    </span>
                  </div>
                  <p class="text-lg text-gray-700 dark:text-gray-300 leading-relaxed font-serif italic border-l-4 border-[#c19a6b]/40 pl-4">
                    “{{ result?.suggest }}”
                  </p>
                  <div class="pt-1 flex gap-4">
                    <button @click="reset" class="text-xs font-bold text-stone-400 hover:text-[#8b4513] transition-colors flex items-center gap-1">
                      <i class="fa-solid fa-rotate-right"></i> 重新测试
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
              <div class="lg:col-span-5 bg-white dark:bg-slate-800 rounded-3xl p-6 shadow-sm border border-stone-100">
                <div class="flex items-center justify-between mb-4">
                  <h3 class="text-base font-bold text-gray-800 dark:text-gray-200 flex items-center gap-2">
                    <span class="w-1 h-4 bg-[#c19a6b] rounded-full"></span>
                    体质平衡分析
                  </h3>
                </div>
                <div ref="radarRef" class="h-64 w-full"></div>
                <div class="mt-4 space-y-2">
                  <div v-for="score in result?.scores.filter((s: any) => s.value >= 40)" :key="score.key" 
                    class="flex items-center justify-between p-2.5 bg-stone-50/50 dark:bg-slate-700/50 rounded-xl">
                    <span class="text-xs font-medium">{{ score.name }}</span>
                    <div class="flex items-center gap-2">
                      <div class="w-20 h-1 bg-gray-200 rounded-full overflow-hidden">
                        <div class="h-full bg-[#c19a6b]" :style="{ width: score.value + '%' }"></div>
                      </div>
                      <span class="text-[10px] font-bold text-[#8b4513]">{{ score.value.toFixed(1) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="lg:col-span-7 h-full">
                <div class="bg-[#fdfbf8] dark:bg-slate-800 rounded-3xl shadow-xl border border-stone-200 overflow-hidden h-full flex flex-col min-h-[650px] relative">
                  <div class="bg-[#9e7a5c] p-6 md:p-8 text-white relative">
                    <div class="absolute top-0 left-1/2 -translate-x-1/2 w-16 h-1 bg-black/10 rounded-b-full"></div>
                    <div class="flex justify-between items-end">
                      <div class="space-y-1">
                        <h3 class="text-2xl font-bold tracking-[0.3em] font-serif">专属调养处方</h3>
                        <p class="text-[9px] text-stone-100/50 uppercase tracking-[0.2em] font-mono">Personalized Herbal Prescription</p>
                      </div>
                      <div class="text-right opacity-80 font-serif">
                        <p class="text-xs">日期：{{ new Date().toLocaleDateString() }}</p>
                      </div>
                    </div>
                  </div>

                  <div class="p-8 md:p-10 flex-grow flex flex-col relative">
                    <div class="absolute inset-y-0 left-12 w-px bg-stone-100 hidden md:block"></div>
                    <div v-if="isAiLoading" class="custom-markdown font-serif is-writing text-xl leading-relaxed">
                      {{ extractPlanStreaming(aiSuggestion) || '正在生成专属调养方案…' }}
                    </div>

                    <div v-else-if="formattedAiSuggestion" class="flex-grow flex flex-col justify-around space-y-6">
                      <section class="relative pl-4">
                        <span class="absolute -left-10 -top-3 text-stone-700/5 text-6xl font-serif italic">01</span>
                        <h4 class="text-[#9e7a5c] text-xs font-bold tracking-widest mb-3 flex items-center uppercase">
                          <i class="fa-solid fa-feather-pointed mr-2"></i> 调养总则 / Principle
                        </h4>
                        <p class="text-xl md:text-2xl text-stone-800 dark:text-gray-100 font-serif leading-relaxed font-bold mb-3">
                          {{ formattedAiSuggestion?.plan }}
                        </p>
                        <div class="flex flex-wrap gap-x-6 gap-y-2">
                          <div v-if="formattedAiSuggestion?.region" class="flex gap-2 text-xs text-stone-500 font-serif italic">
                            <span class="text-stone-400 font-bold not-italic">【地域】</span>{{ formattedAiSuggestion.region }}
                          </div>
                          <div v-if="formattedAiSuggestion?.ageFocus" class="flex gap-2 text-xs text-stone-500 font-serif italic">
                            <span class="text-stone-400 font-bold not-italic">【重点】</span>{{ formattedAiSuggestion.ageFocus }}
                          </div>
                        </div>
                      </section>

                      <section class="relative pl-4">
                        <span class="absolute -left-10 -top-3 text-stone-700/5 text-6xl font-serif italic">02</span>
                        <h4 class="text-[#9e7a5c] text-xs font-bold tracking-widest mb-4 flex items-center uppercase">
                          <i class="fa-solid fa-leaf mr-2"></i> 膳食配伍 / Dietary
                        </h4>
                        <div class="space-y-3">
                          <div v-for="(val, label) in { '主食根茎': 'foodStaple', '蔬果菌藻': 'foodVeg', '肉禽蛋奶': 'foodMeat' }" 
                               :key="label" class="flex items-center group">
                            <span class="w-24 text-stone-400 font-serif text-sm flex-shrink-0">【{{ label }}】</span>
                            <span class="text-lg text-stone-800 dark:text-gray-200 font-bold font-serif tracking-wide">
                              {{ formattedAiSuggestion?.[val as keyof typeof formattedAiSuggestion] }}
                            </span>
                          </div>
                        </div>
                      </section>

                      <section class="pt-4 border-t border-stone-100 pl-4">
                        <div class="flex items-start gap-3">
                           <div class="bg-[#c19a6b]/20 text-[#8b4513] text-[9px] px-1.5 py-3 rounded-sm [writing-mode:vertical-lr] tracking-widest font-serif font-bold">医嘱谨记</div>
                           <div class="space-y-1 flex-grow">
                             <p class="text-base text-stone-600 dark:text-gray-300 font-serif italic font-medium">
                               {{ formattedAiSuggestion?.daily }}
                             </p>
                             <div class="flex justify-between items-center mt-1">
                               <p class="text-[9px] text-stone-400 uppercase tracking-widest">Precautionary advice</p>
                               <span v-if="formattedAiSuggestion?.ageAvoid" class="text-xs px-2 py-0.5 bg-stone-100 text-stone-600 rounded-full border border-stone-200 italic">
                                 禁忌：{{ formattedAiSuggestion.ageAvoid }}
                               </span>
                             </div>
                           </div>
                        </div>
                      </section>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="relative overflow-hidden bg-gradient-to-r from-[#e3d7c5] to-[#d2b48c] dark:from-slate-800 dark:to-slate-700 rounded-[2rem] p-0.5 group transition-all duration-500">
              <div class="bg-white/90 dark:bg-slate-900/90 backdrop-blur-sm rounded-[1.9rem] p-6 md:p-8 flex flex-col md:flex-row items-center justify-between gap-6 relative overflow-hidden">
                <div class="relative z-10 flex flex-col md:flex-row items-center gap-6">
                  <div class="w-14 h-14 bg-gradient-to-br from-[#f5f1ea] to-[#e3d7c5] text-[#8b4513] rounded-2xl flex items-center justify-center text-2xl shadow-inner border border-white/50">
                    <i class="fa-solid fa-calendar-check opacity-80"></i>
                  </div>
                  <div class="text-center md:text-left">
                    <h3 class="text-2xl font-black text-[#7c4a35] dark:text-white mb-1" style="font-family: 'Noto Serif SC', serif">七日食养打卡计划</h3>
                    <p class="text-xs text-stone-500 dark:text-gray-400">基于 {{ result?.primaryType }} 开启专属调理</p>
                  </div>
                </div>
                <button @click="goToDietCheckin" 
                  class="relative z-10 px-8 py-3.5 bg-gradient-to-r from-[#9e7a5c] to-[#8b4513] text-white rounded-xl font-bold text-base hover:brightness-110 transform transition-all flex items-center gap-2">
                  开启打卡 <i class="fa-solid fa-chevron-right text-xs"></i>
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
/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #e3d7c5;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #c19a6b;
}

/* 字体与排版优化 */
.font-serif {
  font-family: 'Noto Serif SC', serif;
}

/* AI 内容排版优化 */
.custom-markdown {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  letter-spacing: 0.02em;
}

.custom-markdown :deep(b), 
.custom-markdown :deep(strong) {
  color: #7c4a35;
  background: rgba(124, 74, 53, 0.05);
  padding: 0 4px;
  border-radius: 4px;
}

/* 装饰性元素 */
.ai-content-wrapper {
  background: rgba(0, 0, 0, 0.02);
  padding: 1.5rem;
  border-radius: 1.5rem;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

/* 处方笺内部细节 */
:deep(.ai-content-styled b), 
:deep(.ai-content-styled strong) {
  color: #9e7a5c;
  background-color: rgba(158, 122, 92, 0.08);
  padding: 0 4px;
  border-radius: 4px;
}

/* 打字机光标动画 */
.is-writing::after {
  content: '|';
  animation: blink 1s infinite;
  color: #9e7a5c;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* 列表数字美化 */
:deep(.list-number) {
  display: inline-flex;
  width: 20px;
  height: 20px;
  background: #c19a6b;
  color: white;
  font-size: 10px;
  border-radius: 50%;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
  vertical-align: middle;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .tracking-[0.4em] {
    tracking: 0.2em;
  }
}
</style>

<script setup lang="ts">
import { ref, computed, nextTick } from "vue";
import * as echarts from "echarts";
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import { useAuthStore } from "@/stores/auth";
// 引入你刚刚定义的 API
import { submitTestAPI } from "@/api/constitution";
import { getUserInfoAPI } from "@/api/user";
import { chatStream  } from "@/api/ai";
import { useRouter } from "vue-router";

const getStructuredReport = (text: string) => {
  if (!text) return {};
  const extract = (tag: string) => {
    // 保持你原来的正则逻辑
    const regex = new RegExp(`\\[${tag}\\]\\{?(.*?)\\}?(?=\\s*\\[|$)`, 's');
    const match = text.match(regex);
    return match ? match[1].trim() : '';
  };

  return {
    plan: extract('方案'),
    region: extract('地域'),
    ageAvoid: extract('忌口'),
    ageFocus: extract('重点'),
    foodStaple: extract('主食'),
    foodVeg: extract('蔬果'),
    foodMeat: extract('肉类'),
    daily: extract('注意')
  };
};

const formattedAiSuggestion = computed(() => {
  return getStructuredReport(aiSuggestion.value);
});

const router = useRouter();

const goToDietCheckin = () => {
  router.push("/diet-checkin");
};

// 增加响应式变量存储 AI 建议
const aiSuggestion = ref(""); 
const isAiLoading = ref(false);

/**
 * 解析形如 [标题]内容 的结构化文本
 * 返回：{ 标题: 内容 }
 */
function extractPlanStreaming(text: string): Record<string, string> {
  const result: Record<string, string> = {};
  const regex = /\[([A-Za-z\u4e00-\u9fa5]+?)\]([\s\S]*?)(?=\[[A-Za-z\u4e00-\u9fa5]+?\]|$)/g;

  let match;
  while ((match = regex.exec(text)) !== null) {
    const title = match[1].trim();
    const content = match[2].trim();
    result[title] = content;
  }

  return result;
}

/**
 * 工具函数：根据出生日期计算年龄
 */
const getAge = (birthday: string | Date) => {
  const birthDate = new Date(birthday);
  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const m = today.getMonth() - birthDate.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) age--;
  return age;
};


const authStore = useAuthStore();

// 静态数据定义
// 体质元数据
const constitutionTypes = [
  { id: "balanced", name: "平和质", description: "体态适中、面色红润、精力充沛" },
  { id: "qiDeficiency", name: "气虚质", description: "容易疲乏、气短、自汗" },
  { id: "yinDeficiency", name: "阴虚质", description: "手足心热、口燥咽干" },
  { id: "phlegmDampness", name: "痰湿质", description: "体形肥胖、腹部肥满、口黏" },
  { id: "yangDeficiency", name: "阳虚质", description: "手脚发凉、畏寒怕冷" },
  { id: "dampHeat", name: "湿热质", description: "面垢油光、口苦、易生痤疮" },
  { id: "bloodStasis", name: "血瘀质", description: "肤色晦黯、易出现瘀斑" },
  { id: "qiStagnation", name: "气郁质", description: "神情抑郁、多愁善感" },
  { id: "specialEndowment", name: "特禀质", description: "容易过敏、喷嚏流涕" },
];

const scoreOptions = [
  { score: 1, label: "没有", description: "根本不/从不" },
  { score: 2, label: "很少", description: "有一点/偶尔" },
  { score: 3, label: "有时", description: "有些/少数时间" },
  { score: 4, label: "经常", description: "相当/多数时间" },
  { score: 5, label: "总是", description: "非常/每天" }
];

// 55道题目完整列表
const allQuestions = ref([
  // 平和质 (7题, 注意 reverse 标记)
 { id: 1, question: "您精力充沛吗？（指精神头足，乐于做事）", constitution: "balanced", reverse: false },
  { id: 2, question: "您容易疲乏吗？（指体力不佳，稍活动或劳动即感累）", constitution: "balanced", reverse: true },
  { id: 3, question: "您说话声音低弱无力吗？（指说话没力气）", constitution: "balanced", reverse: true },
  { id: 4, question: "您感到闷闷不乐、情绪低沉吗？", constitution: "balanced", reverse: true },
  { id: 5, question: "您比一般人耐受不了寒冷吗？", constitution: "balanced", reverse: true },
  { id: 6, question: "您容易失眠吗？", constitution: "balanced", reverse: true },
  { id: 7, question: "您容易忘事（健忘）吗？", constitution: "balanced", reverse: true },
  
  // 气虚质 (6题)
  { id: 8, question: "您容易气短（呼吸短促，接不上气）吗？", constitution: "qiDeficiency", reverse: false },
  { id: 9, question: "您容易心慌吗？", constitution: "qiDeficiency", reverse: false },
  { id: 10, question: "您容易头晕或站起时晕眩吗？", constitution: "qiDeficiency", reverse: false },
  { id: 11, question: "您比别人容易患感冒吗？", constitution: "qiDeficiency", reverse: false },
  { id: 12, question: "您喜欢安静、懒得说话吗？", constitution: "qiDeficiency", reverse: false },
  { id: 13, question: "您活动量稍大就容易出虚汗吗？", constitution: "qiDeficiency", reverse: false },
  
  // 阴虚质 (7题)
  { id: 14, question: "您感到手脚心发热吗？", constitution: "yinDeficiency", reverse: false },
  { id: 15, question: "您感觉身体、脸上发热吗？", constitution: "yinDeficiency", reverse: false },
  { id: 16, question: "您皮肤或口唇干吗？", constitution: "yinDeficiency", reverse: false },
  { id: 17, question: "您口唇的颜色比一般人红吗？", constitution: "yinDeficiency", reverse: false },
  { id: 18, question: "您容易便秘或大便干燥吗？", constitution: "yinDeficiency", reverse: false },
  { id: 19, question: "您感到眼睛干涩吗？", constitution: "yinDeficiency", reverse: false },
  { id: 20, question: "您感到口干咽燥、总想喝水吗？", constitution: "yinDeficiency", reverse: false },
  
  // 痰湿质 (7题)
  { id: 21, question: "您感到胸闷或腹部胀满吗？", constitution: "phlegmDampness", reverse: false },
  { id: 22, question: "您感到身体沉重不轻松或不爽快吗？", constitution: "phlegmDampness", reverse: false },
  { id: 23, question: "您腹部肥满松软吗？", constitution: "phlegmDampness", reverse: false },
  { id: 24, question: "您有额部油脂分泌多的现象吗？", constitution: "phlegmDampness", reverse: false },
  { id: 25, question: "您嘴里有黏黏的感觉吗？", constitution: "phlegmDampness", reverse: false },
  { id: 26, question: "您平时痰多，特别是感到咽喉部总有痰堵着吗？", constitution: "phlegmDampness", reverse: false },
  { id: 27, question: "您舌苔厚腻或有舌苔厚厚的感觉吗？", constitution: "phlegmDampness", reverse: false },
  
  // 阳虚质 (5题)
  { id: 28, question: "您手脚发凉吗？", constitution: "yangDeficiency", reverse: false },
  { id: 29, question: "您胃脘部、背部或腰膝部怕冷吗？", constitution: "yangDeficiency", reverse: false },
  { id: 30, question: "您感到怕冷、衣服比别人穿得多吗？", constitution: "yangDeficiency", reverse: false },
  { id: 31, question: "您吃（喝）凉的东西会感到不舒服或者怕吃（喝）凉的吗？", constitution: "yangDeficiency", reverse: false },
  { id: 32, question: "您受凉或吃（喝）凉的东西后，容易腹泻（拉肚子）吗？", constitution: "yangDeficiency", reverse: false },
  
  // 湿热质 (5题)
  { id: 33, question: "您面部或鼻部有油腻感或者油亮发光吗？", constitution: "dampHeat", reverse: false },
  { id: 34, question: "您脸上容易生痤疮或皮肤容易生疮疖吗？", constitution: "dampHeat", reverse: false },
  { id: 35, question: "您感到口苦或嘴里有异味吗？", constitution: "dampHeat", reverse: false },
  { id: 36, question: "您大便黏滞不爽、有解不尽的感觉吗？", constitution: "dampHeat", reverse: false },
  { id: 37, question: "您小便时尿道有发热感、尿色浓（深）吗？", constitution: "dampHeat", reverse: false },
  
  // 血瘀质 (6题)
  { id: 38, question: "您的皮肤在不知不觉中会出现青紫瘀斑（皮下出血）吗？", constitution: "bloodStasis", reverse: false },
  { id: 39, question: "您两颧部有细微红丝吗？", constitution: "bloodStasis", reverse: false },
  { id: 40, question: "您身体上有哪里疼痛吗？", constitution: "bloodStasis", reverse: false },
  { id: 41, question: "您面色晦暗或容易出现褐斑吗？", constitution: "bloodStasis", reverse: false },
  { id: 42, question: "您容易出现黑眼圈吗？", constitution: "bloodStasis", reverse: false },
  { id: 43, question: "您口唇颜色偏黯吗？", constitution: "bloodStasis", reverse: false },
  
  // 气郁质 (6题)
  { id: 44, question: "您感到闷闷不乐、情绪低沉吗？", constitution: "qiStagnation", reverse: false },
  { id: 45, question: "您容易精神紧张、焦虑不安吗？", constitution: "qiStagnation", reverse: false },
  { id: 46, question: "您多愁善感、感情脆弱吗？", constitution: "qiStagnation", reverse: false },
  { id: 47, question: "您容易感到害怕或受到惊吓吗？", constitution: "qiStagnation", reverse: false },
  { id: 48, question: "您无缘无故叹气吗？", constitution: "qiStagnation", reverse: false },
  { id: 49, question: "您感到孤独吗？", constitution: "qiStagnation", reverse: false },
  
  // 特禀质 (6题)
  { id: 50, question: "您没有感冒时也会打喷嚏、鼻塞、流鼻涕吗？", constitution: "specialEndowment", reverse: false },
  { id: 51, question: "您有因季节变化、温度变化或异味等原因而咳喘的现象吗？", constitution: "specialEndowment", reverse: false },
  { id: 52, question: "您容易过敏（对药物、食物、气味、花粉或在季节交替、气候变化时）吗？", constitution: "specialEndowment", reverse: false },
  { id: 53, question: "您的皮肤容易起荨麻疹吗？", constitution: "specialEndowment", reverse: false },
  { id: 54, question: "您的皮肤因过敏出现过紫红包瘀点、瘀斑吗？", constitution: "specialEndowment", reverse: false },
  { id: 55, question: "您的皮肤一抓就红，并出现抓痕吗？", constitution: "specialEndowment", reverse: false }
]);

// 响应式状态
const step = ref<number | "results">(1);
const answers = ref<Record<number, number>>({});
const result = ref<any>(null);

// 后端数据状态
const backendFoodRecs = ref<any[]>([]);
const loadingFood = ref(false);

const radarRef = ref<HTMLElement>();
let radarChart: echarts.ECharts | null = null;

// 计算属性 & 逻辑

const currentQuestion = computed(() => {
  if (typeof step.value === "number" && step.value > 1) {
    return allQuestions.value[step.value - 2];
  }
  return null;
});

const startTest = () => step.value = 2;

const selectAnswer = (id: number, score: number) => {
  answers.value[id] = score;
};

const next = () => {
  if (typeof step.value === "number") {
    if (step.value - 1 === allQuestions.value.length) {
      calculateResults();
    } else {
      step.value++;
    }
  }
};

const prev = () => {
  if (typeof step.value === "number" && step.value > 2) {
    step.value--;
  }
};

// 重置
const reset = () => {
  step.value = 1;
  answers.value = {};
  result.value = null;
  backendFoodRecs.value = [];
  if (radarChart) {
    radarChart.dispose();
    radarChart = null;
  }
};

// 核心逻辑：计算与保存

const calculateResults = async () => {
  const rawScoresMap: Record<string, { score: number, count: number }> = {};
  constitutionTypes.forEach(type => {
    rawScoresMap[type.id] = { score: 0, count: 0 };
  });

  allQuestions.value.forEach(q => {
    const s = answers.value[q.id];
    if (s !== undefined) {
      const finalS = q.reverse ? (6 - s) : s;
      rawScoresMap[q.constitution].score += finalS;
      rawScoresMap[q.constitution].count += 1;
    }
  });

  const scoresArray: any[] = [];
  const dbRawScores: Record<string, number> = {};
  const dbConvertScores: Record<string, number> = {};

  constitutionTypes.forEach(type => {
    const d = rawScoresMap[type.id];
    if (d.count > 0) {
      let val = ((d.score - d.count) / (d.count * 4)) * 100;
      val = Math.max(0, Math.min(100, val));
      scoresArray.push({ name: type.name, key: type.id, value: val });
      dbRawScores[type.id] = d.score;
      dbConvertScores[type.id] = parseFloat(val.toFixed(2));
    }
  });

  const balancedScore = scoresArray.find(s => s.key === 'balanced')?.value || 0;
  const others = scoresArray.filter(s => s.key !== 'balanced');
  const maxOther = Math.max(...others.map(s => s.value));

  let primaryType = "";
  if (balancedScore >= 60 && maxOther < 30) {
    primaryType = "平和质";
  } else {
    scoresArray.sort((a, b) => b.value - a.value);
    primaryType = scoresArray[0].name;
  }

  const secondaryTypes = scoresArray
    .filter(s => s.name !== primaryType && s.value >= 30)
    .map(s => s.name);

  const suggest = `根据测试，您的主体质为${primaryType}。${
    secondaryTypes.length ? '兼有' + secondaryTypes.join('、') + '倾向。' : '体质较为单一。'
  }`;

  result.value = { primaryType, secondaryTypes, scores: scoresArray, suggest };
  step.value = "results";

  nextTick(() => { initRadar(scoresArray); });

if (authStore.user?.userId) {
    loadingFood.value = true;
    isAiLoading.value = true;
    aiSuggestion.value = ""; 

    try {
      // 第一次提交：保存分数和基本信息
      const results = await Promise.allSettled([
        submitTestAPI({
          userId: Number(authStore.user.userId),
          testTime: new Date().toISOString(), // 建议加上时间
          constitutionScores: JSON.stringify(dbRawScores),
          constitutionConvertScores: JSON.stringify(dbConvertScores),
          mainConstitution: primaryType,
          subConstitution: secondaryTypes.join(','),
          suggest: suggest,
          needFoodRecommendation: true
        }),
        getUserInfoAPI(authStore.user.userId)
      ]);

      const saveRes = results[0].status === 'fulfilled' ? results[0].value : null;
      if (saveRes?.data?.foodRecommendations) {
        backendFoodRecs.value = saveRes.data.foodRecommendations;
      }

      const userRes = results[1].status === 'fulfilled' ? results[1].value : null;
      const userInfo = (userRes?.data || userRes) as any;
      const age = userInfo?.birthday ? getAge(userInfo.birthday) : "未知";
      const uRegion = userInfo?.nativePlace || "未知";

      const prompt = `
        用户基础信息：年龄 ${age} 岁，所在地区：${uRegion}。
        中医体质结果：主体质为【${primaryType}】，兼有体质为【${secondaryTypes.join('、') || '无'}】。
        请按照规范格式 [Plan方案]{...} [Region地域]{...} 等输出。
      `.trim();

      // 调用 AI 流式接口
      await chatStream(
        { agent: "suggester", prompt },
        (chunk) => {
          aiSuggestion.value += chunk; // 实时显示给用户看
        },
        async () => {
          // 当 AI 回复完全结束时
          isAiLoading.value = false;
          
          // 解析最终生成的完整文本
          const structuredData = getStructuredReport(aiSuggestion.value);
          
          console.log("AI 报告生成完毕，准备存入数据库:", structuredData);

          try {
            // 后端逻辑应该是根据 userId 和最近一次 testId 进行 update
            await submitTestAPI({
              userId: Number(authStore.user.userId),
              testTime: new Date().toISOString(),
              constitutionScores: JSON.stringify(dbRawScores),
              constitutionConvertScores: JSON.stringify(dbConvertScores),
              mainConstitution: primaryType,
              subConstitution: secondaryTypes.join(','),
              suggest: suggest,
              needFoodRecommendation: true,
              ...structuredData // 将 plan, region, foodMeat 等 8 个字段展开混入
            });
            console.log("AI 结构化报告已成功持久化到数据库");
          } catch (updateErr) {
            console.error("更新 AI 报告失败", updateErr);
          }
        }
      );

    } catch (err) {
      console.error("AI 调用失败", err);
      isAiLoading.value = false;
    } finally {
      loadingFood.value = false;
    }
  } else {
    console.warn("未登录，无法调用AI方案");
  }
};

const initRadar = (data: any[]) => {
  if (!radarRef.value) return;
  if (radarChart) radarChart.dispose();
  
  radarChart = echarts.init(radarRef.value);
  const option = {
    backgroundColor: 'transparent',
    // tooltip: { show: false },
    silent: true,
    radar: {
      indicator: data.map(i => ({ name: i.name, max: 100 })),
      splitArea: {
        areaStyle: {
          color: ['rgba(245, 222, 179, 0.2)', 'rgba(210, 180, 140, 0.2)']
        }
      },
      axisLine: { lineStyle: { color: '#8b4513' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: data.map(i => i.value),
        name: '体质得分',
        areaStyle: { color: 'rgba(139, 69, 19, 0.5)' },
        itemStyle: { color: '#8b4513' },
        lineStyle: { color: '#8b4513' }
      }]
    }]
  };
  radarChart.setOption(option);
  
  window.addEventListener('resize', () => radarChart?.resize());
};
</script>
