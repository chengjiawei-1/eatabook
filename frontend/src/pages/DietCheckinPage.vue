<template>
  <!-- 减小整体页面的背景内边距 -->
  <div class="min-h-screen bg-[#FDFBF8] dark:bg-slate-900 flex flex-col text-[#4A3728]">
    <Nav />

    <!-- 缩减 pt-28 -> pt-20, pb-12 -> pb-4, gap-8 -> gap-4 -->
     
    <main class="flex-grow pt-24 pb-16 container mx-auto px-4 max-w-6xl flex flex-col justify-center">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-5 items-stretch">
        
        <div class="lg:col-span-5 flex flex-col space-y-4">
          <!-- 减小 p-9 -> p-6 -->
          <div class="bg-gradient-to-br from-[#e3d7c5] to-[#d2b48c] rounded-[2rem] p-6 text-[#5c2e14] shadow-sm relative overflow-hidden flex-shrink-0 border border-white">
            <div class="relative z-10">
              <p class="text-[#8b4513] text-[10px] font-bold tracking-[0.2em] opacity-70">RHYTHM OF NATURE</p>
              <div class="flex items-baseline gap-2 mt-2">
                <!-- 缩小 text-8xl -> text-6xl -->
                <span class="text-7xl font-light tracking-tighter leading-none font-serif">{{ streakDays }}</span>
                <span class="text-sm font-bold text-[#8b4513] tracking-widest">DAYS</span>
              </div>
              <div class="mt-4"> <!-- mt-8 -> mt-4 -->
                <div class="flex justify-between text-[10px] mb-2 text-[#8b4513] font-bold tracking-widest">
                  <span class="text-[12px]">七日食疗挑战</span>
                  <span class="font-mono">{{ Math.min(streakDays, 7) }} / 7</span>
                </div>
                <div class="w-full bg-white/50 rounded-full h-1.5 overflow-hidden shadow-inner">
                  <div class="bg-[#8b4513] h-full rounded-full transition-all duration-1000" :style="{ width: `${(Math.min(streakDays, 7) / 7) * 100}%` }"></div>
                </div>
              </div>
            </div>
            <i class="fa-solid fa-wheat-awn absolute -right-6 -bottom-6 text-[#8b4513]/5 text-[10rem] rotate-12"></i>
          </div>

          <!-- 减小 min-h-[550px] -> min-h-[420px], p-10 -> p-6 -->
          <div class="bg-white dark:bg-slate-800 rounded-[2rem] shadow-sm border border-stone-200 overflow-hidden flex flex-col flex-grow min-h-[420px]">
            <transition name="card-flip" mode="out-in">
              <div v-if="activeTab === 'info'" :key="selectedDateStr + 'info'" class="p-7 flex-grow space-y-4">
                <div class="flex justify-between items-start">
                  <div>
                    <p class="text-[#9e7a5c] text-[10px] font-mono mb-1 tracking-widest font-bold uppercase">{{ displayDateTitle }}</p>
                    <!-- text-6xl -> text-4xl -->
                    <h2 class="text-5xl font-bold text-[#5c2e14] tracking-tight font-serif">{{ currentDayData.solarTerm }}</h2>
                    <p class="text-lg text-[#9e7a5c] mt-1 font-medium italic opacity-90">{{ currentDayData.lunarDate }}</p>
                  </div>
                  <div class="bg-stone-50 dark:bg-slate-700 p-3 rounded-[1.5rem] text-center border border-stone-100">
                    <i :class="['fa-solid mb-1 text-base text-[#c19a6b]', isWeatherLoading ? 'fa-spinner fa-spin' : 'fa-temperature-three-quarters']"></i>
                    <p class="text-2xl font-light text-[#5c2e14] dark:text-white">{{ weather.temp }}°</p>
                    <p class="text-[9px] text-stone-400 font-bold uppercase">{{ weather.text }}</p>
                  </div>
                </div>
                
                <div class="pt-4 border-t border-stone-100 flex-grow flex flex-col">
                  <div class="flex items-center gap-2 text-[#8b4513] font-bold mb-3 text-xs tracking-[0.15em]">
                    <span class="w-2 h-2 bg-[#c19a6b] rounded-full"></span> 今日调理指南
                  </div>
                  <div class="relative flex-grow bg-[#FDFBF8] p-5 rounded-[1.5rem] border border-stone-100 flex items-center">
                    <p class="text-[15px] text-[#4A3728] leading-[1.6] font-medium font-serif italic text-justify">
                      {{ currentDayData.suggest.includes('】') ? currentDayData.suggest.split('】')[1] : currentDayData.suggest }}
                    </p>
                  </div>
                </div>
              </div>

              <div v-else :key="selectedDateStr + 'meals'" class="p-7 flex-grow space-y-3 bg-white flex flex-col justify-center">
                <h3 class="text-[10px] font-bold text-[#9e7a5c] tracking-widest flex items-center gap-2 mb-1 uppercase">
                  Diet Plan · 膳食配伍
                </h3>

                <div v-if="isAiLoading" class="flex-grow flex flex-col items-center justify-center py-6 text-center space-y-4">
                  <div class="relative">
                    <div class="w-16 h-16 border-4 border-stone-100 border-t-[#c19a6b] rounded-full animate-spin"></div>
                    <i class="fa-solid fa-wand-magic-sparkles absolute inset-0 flex items-center justify-center text-[#c19a6b] text-xl animate-pulse"></i>
                  </div>
                  <div class="space-y-1">
                    <h4 class="text-lg font-bold text-[#5c2e14] font-serif">正在为您定制专属食谱</h4>
                    <p class="text-[9px] text-stone-400 tracking-widest font-bold uppercase italic">Crafting your personalized wisdom...</p>
                  </div>
                </div>

                <template v-else>
                  <div v-for="(meal, type) in currentDayData.meals" :key="type" 
                      @click="viewRecipe(meal)"
                      class="group flex gap-4 p-3 rounded-[1.2rem] hover:bg-stone-50 transition-all cursor-pointer border border-transparent hover:border-stone-100">
                    <div class="w-14 h-14 rounded-xl overflow-hidden shadow-sm bg-stone-100 flex-shrink-0">
                      <img :src="meal.image" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" />
                    </div>
                    <div class="flex-grow py-0.5">
                      <span class="text-[9px] font-bold text-[#c19a6b] uppercase tracking-widest opacity-80">{{ meal.label }}</span>
                      <h4 class="font-bold text-[#5c2e14] text-base mt-0">{{ meal.name }}</h4>
                      <p class="text-[11px] text-stone-500 line-clamp-1">{{ meal.effect }}</p>
                    </div>
                  </div>
                </template>
              </div>
            </transition>
            <!-- 缩减按钮高度 py-6 -> py-4 -->
            <button @click="handleTabChange" 
                    :class="[
                      'w-full py-4 transition-all border-t border-stone-100 uppercase font-bold text-[10px] tracking-[0.3em]',
                      !myBodyStatus ? 'bg-stone-50 text-stone-300 cursor-not-allowed' : 'bg-stone-50 hover:bg-stone-100 text-[#9e7a5c]'
                    ]">
              <i v-if="!myBodyStatus && activeTab === 'info'" class="fa-solid fa-lock mr-2"></i>
              {{ activeTab === 'info' ? 'Explore Recipes 浏览食谱' : 'Back to Wisdom 返回指南' }}
            </button>
          </div>
        </div>

        <!-- 缩小 p-10 -> p-8 -->
        <div class="lg:col-span-7 bg-white dark:bg-slate-800 rounded-[2rem] p-8 shadow-sm border border-stone-200/60 flex flex-col justify-between overflow-hidden">
          <div>
            <!-- 缩减 mb-12 -> mb-6 -->
            <div class="flex items-center justify-between mb-6 px-2">
              <div>
                <h3 class="text-3xl font-bold text-[#5c2e14] tracking-tight font-serif">{{ currentYear }}年</h3>
                <p class="text-stone-400 font-mono text-[10px] uppercase tracking-[0.4em] font-bold mt-1">{{ currentMonth + 1 }}月 · CALENDAR</p>
              </div>
              <div class="flex items-center bg-stone-50 rounded-xl p-1.5 border border-stone-100">
                <button @click="changeMonth(-1)" :disabled="isMonthPrevDisabled" class="w-8 h-8 hover:bg-white rounded-lg text-stone-500 transition-all flex items-center justify-center"><i class="fa-solid fa-chevron-left text-[10px]"></i></button>
                <button @click="goToday" class="px-4 text-[10px] font-bold text-stone-500 tracking-widest uppercase hover:text-[#8b4513]">Today</button>
                <button @click="changeMonth(1)" :disabled="isMonthNextDisabled" class="w-8 h-8 hover:bg-white rounded-lg text-stone-500 transition-all flex items-center justify-center"><i class="fa-solid fa-chevron-right text-[10px]"></i></button>
              </div>
            </div>

            <!-- 减小 gap-5 -> gap-3 -->
            <div class="grid grid-cols-7 gap-3">
              <div v-for="d in ['SUN','MON','TUE','WED','THU','FRI','SAT']" :key="d" class="text-center text-[10px] font-bold text-stone-400 mb-2 tracking-widest uppercase">{{ d }}</div>
              
              <div v-for="(date, index) in calendarDays" :key="index" class="aspect-square flex items-center justify-center">
                <div v-if="date.day"
                  @click="selectDate(date)"
                  :class="[
                    'w-12 h-12 rounded-[1.2rem] flex flex-col items-center justify-center transition-all relative group',
                    !isSelectable(date.fullDate) ? 'text-stone-200 cursor-not-allowed' : 'text-[#4A3728] cursor-pointer hover:bg-stone-50'
                  ]"
                >
                  <div v-if="isCheckIn(date.fullDate)" 
                       class="absolute inset-0.5 border-2 border-[#c19a6b]/80 rounded-full scale-105 pointer-events-none stamp-pop">
                  </div>
                  
                  <div v-if="isSelected(date.fullDate) && !isCheckIn(date.fullDate)" 
                       class="absolute inset-0.5 bg-[#c19a6b] rounded-full scale-105 pointer-events-none transition-transform duration-300">
                  </div>
                  
                  <span :class="[
                    'text-base font-bold z-10 transition-colors', 
                    isSelected(date.fullDate) && !isCheckIn(date.fullDate) ? 'text-white' : (isCheckIn(date.fullDate) ? 'text-[#8b4513]' : 'text-[#4A3728]')
                  ]">
                    {{ date.day }}
                  </span>
                  
                  <span :class="[
                    'text-[9px] mt-0 font-bold z-10 transition-colors', 
                    isSelected(date.fullDate) && !isCheckIn(date.fullDate) ? 'text-white/90' : (isCheckIn(date.fullDate) ? 'text-[#8b4513]' : 'text-[#8B7E74]')
                  ]">
                    {{ date.lunar }}
                  </span>
                  
                  <div v-if="date.isToday" :class="[
                    'absolute -top-1 right-1 w-2 h-2 rounded-full border-2 shadow-sm z-20',
                    isSelected(date.fullDate) ? 'bg-white border-[#c19a6b]' : 'bg-[#c19a6b] border-white'
                  ]"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 缩小 mt-12 -> mt-6, px-20 -> px-12 -->
          <div class="mt-6 flex flex-col items-center gap-4">
            <button 
              @click="doCheckIn"
              :disabled="!isTodaySelected || isTodayChecked"
              :class="[
                'group relative overflow-hidden px-12 py-4 rounded-xl font-bold text-sm tracking-[0.3em] transition-all shadow-lg active:scale-95',
                !myBodyStatus ? 'bg-stone-300 cursor-not-allowed opacity-80' : 'bg-[#8b4513] hover:brightness-110 text-white shadow-orange-950/10'
              ]"
            >
              <div class="flex items-center justify-center gap-3">
                <i :class="!myBodyStatus ? 'fa-solid fa-lock' : (isTodayChecked ? 'fa-solid fa-circle-check' : 'fa-solid fa-stamp')"></i>
                
                <template v-if="!myBodyStatus">
                  请先完成体质测试
                </template>
                <template v-else>
                  {{ isTodayChecked ? '今日签到已完成' : (isTodaySelected ? '确认今日食养签到' : '请先在日历中选定今日') }}
                </template>
              </div>
            </button>
            <p class="text-[9px] text-stone-400 tracking-[0.3em] font-bold uppercase italic text-center">
              The Way of Health: Follow the Seasons
            </p>
          </div>
        </div>
      </div>
    </main>

    <Footer />
    <transition name="sharp-modal">
      <div v-if="activeRecipe" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        
        <div class="absolute inset-0 bg-[#4A3728]/40" @click="activeRecipe = null"></div>
        
        <div class="relative bg-white dark:bg-slate-900 w-full max-w-lg rounded-[2.5rem] overflow-hidden shadow-[0_20px_60px_rgba(0,0,0,0.2)] transform-gpu">
          
          <div class="relative h-64 overflow-hidden">
            <img :src="activeRecipe.image" class="w-full h-full object-cover shadow-inner" />
            <div class="absolute top-4 right-4 bg-white/80 backdrop-blur-md px-3 py-1 rounded-full text-[10px] font-bold text-[#c19a6b]">
              RECOMMENDED
            </div>
          </div>
          
          <div class="p-8">
            <div class="mb-6">
              <span class="text-[#c19a6b] text-[11px] font-bold uppercase tracking-[0.2em] block mb-1">
                {{ activeRecipe.label }}
              </span>
              <h3 class="text-3xl font-bold text-[#5c2e14] font-serif leading-tight">
                {{ activeRecipe.name }}
              </h3>
            </div>
            
            <div class="bg-stone-50 rounded-2xl p-5 mb-8 border border-stone-100/50">
              <p class="text-base text-[#7c6a5a] leading-relaxed italic font-serif">
                “{{ activeRecipe.steps || activeRecipe.effect }}”
              </p>
            </div>
            
            <div class="flex gap-4">
              <button @click="goToDetail(activeRecipe.id)"
                      class="flex-grow py-4 bg-[#c19a6b] hover:bg-[#b38b5d] text-white rounded-xl font-bold text-sm tracking-[0.2em] transition-all shadow-md active:scale-95 flex items-center justify-center gap-2">
                <i class="fa-solid fa-arrow-right-long text-xs"></i> 详情做法
              </button>
              
              <button @click="activeRecipe = null" 
                      class="px-8 py-4 bg-stone-100 text-stone-400 rounded-xl font-bold text-xs tracking-widest hover:bg-stone-200 transition-colors">
                返回
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <transition name="reward-pop">
      <div v-if="showRewardModal" class="fixed inset-0 z-[200] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-[#4A3728]/30 backdrop-blur-md" @click="showRewardModal = false"></div>
        <div class="relative bg-white w-full max-w-sm rounded-[3.5rem] overflow-hidden shadow-[0_40px_80px_rgba(92,46,20,0.2)] border border-stone-100 text-center p-12">
          <div class="relative z-10">
            <div class="w-24 h-24 bg-gradient-to-tr from-[#9e7a5c] to-[#c19a6b] rounded-full mx-auto flex items-center justify-center shadow-xl mb-8">
              <i class="fa-solid fa-award text-4xl text-white"></i>
            </div>
            <h3 class="text-2xl font-bold text-[#5c2e14] mb-3 tracking-widest font-serif">七日食养 · 圆满</h3>
            <p class="text-stone-500 text-sm mb-10 leading-relaxed italic">“日日行，不怕千万里”<br/>恭喜获得食养挑战奖励</p>
            <div class="bg-stone-50 rounded-[2.2rem] py-8 mb-10 border border-stone-100">
              <span class="text-[10px] text-stone-400 font-bold uppercase tracking-[0.4em] block mb-3">INTEGRAL REWARD</span>
              <span class="text-5xl font-light text-[#5c2e14]">+1,000</span>
            </div>
            <button @click="showRewardModal = false" class="w-full py-5 bg-[#5c2e14] text-white rounded-2xl font-bold text-lg tracking-[0.4em] hover:bg-[#3d1f0d] transition-all">收下奖励</button>
          </div>
        </div>
      </div>
    </transition>
    
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@600;700;900&display=swap');

div {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.font-serif {
  font-family: 'Noto Serif SC', serif;
}

/* 打卡圆圈弹性动画 */
.stamp-pop {
  animation: stamp-in 0.45s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes stamp-in {
  from { transform: scale(1.6); opacity: 0; }
  to { transform: scale(1.05); opacity: 1; }
}

.reward-pop-enter-active, .reward-pop-leave-active { transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1); }
.reward-pop-enter-from, .reward-pop-leave-to { opacity: 0; transform: scale(0.85) translateY(30px); }

.card-flip-enter-active, .card-flip-leave-active { transition: all 0.45s ease; }
.card-flip-enter-from { opacity: 0; transform: translateY(15px); }
.card-flip-leave-to { opacity: 0; transform: translateY(-15px); }

@keyframes soft-float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-10px); } }
.fa-award { animation: soft-float 3s ease-in-out infinite; }

/* 丝滑弹窗动画 */
.smooth-modal-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  will-change: transform, opacity;
}

.smooth-modal-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.smooth-modal-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}

.smooth-modal-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* 针对移动端优化，防止点击穿透导致的卡顿 */
.fixed {
  touch-action: none;
}
</style>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from "vue";
import { Solar } from "lunar-javascript";
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import { useAuthStore } from "@/stores/auth";
import { getUserInfoAPI, addUserPoints } from "@/api/user"; // 引入加分接口
import { selectByUserIdForArgentAPI } from "@/api/constitution";
import { chatStructured } from "@/api/ai";
import { getDishById } from "@/api/dishes";
import { useRouter } from 'vue-router';
import { getSeasonalWisdomAPI } from "@/api/seasonal";
import { searchCity, get7DayWeather } from "@/api/weather";
import { ElNotification } from 'element-plus'; // 建议引入提示组件
import { getValidMealPlan, saveMealPlan } from "@/api/plan";
// 导入你提供的打卡与流水接口
import { 
  insertCheckinApi, 
  findLatestCheckinApi, 
  getCheckinRangeApi, 
  countTodayCheckinApi 
} from "@/api/checkin";
import { insertPointLogApi } from "@/api/video";

// 状态定义
// 存储 7 天天气的字典：{ "2025-12-26": { text: '多云', temp: '8' } }
const weatherForecastMap = ref<Record<string, { text: string; temp: string }>>({});
const isWeatherLoading = ref(false);

// 天气初始化函数
const initWeather = async () => {
  isWeatherLoading.value = true;
  try {
    // 使用 .trim() 确保没有多余空格，并增加兜底字符串防止传 null 触发 400
    const rawLocation = myInfo.value?.city || myInfo.value?.nativePlace;
    const locationQuery = rawLocation.trim() ;

    console.log("准备查询天气，当前城市:", locationQuery);

    const cityInfo = await searchCity(locationQuery);
    
    if (cityInfo && cityInfo.id) {
      const dailyData = await get7DayWeather(cityInfo.id);
      const tempDict: Record<string, any> = {};
      dailyData.forEach((item: any) => {
        tempDict[item.fxDate] = {
          text: item.textDay,
          temp: item.tempMax
        };
      });
      weatherForecastMap.value = tempDict;
      syncWeatherByDate(selectedDateStr.value);
    }
  } catch (error) {
    console.error("天气初始化流程失败:", error);
  } finally {
    isWeatherLoading.value = false;
  }
};

// 日期与天气同步函数
const syncWeatherByDate = (dateStr: string) => {
  const data = weatherForecastMap.value[dateStr];
  if (data) {
    weather.value = { 
      text: data.text, 
      temp: parseInt(data.temp) 
    };
  } else {
    // 兜底逻辑：如果超出 7 天或加载失败
    weather.value = { text: "数据同步中", temp: 0 };
  }
};

const router = useRouter();

const goToDetail = (id: number) => {
  if (!id) return;
  router.push(`/dishes/${id}`);
};

// 存储 AI 返回的原始 ID 列表
const aiIdPlan = ref<{
  breakfast: number[],
  lunch: number[],
  dinner: number[]
} | null>(null);

// 缓存已经加载过的菜品详情，避免重复请求
const dishCache = new Map<number, any>();

// 基础状态定义
const authStore = useAuthStore();
const activeTab = ref('info');
const today = new Date();
const currentYear = ref(today.getFullYear());
const currentMonth = ref(today.getMonth());
const selectedDateStr = ref(today.toISOString().split('T')[0]);
const activeRecipe = ref<any>(null);

const streakDays = ref(0); // 连续打卡天数由后端/逻辑计算
const checkInDays = ref<string[]>([]); // 存储日期字符串用于日历圈起
const isTodayChecked = ref(false); // 今日是否已打卡状态

const weather = ref<{ text: string; temp: string | number }>({ 
  text: "数据加载中", 
  temp: "-" 
});
// 默认的建议文字，等 AI 加载好了会换掉
const termSuggest = ref("正在同步您的体质数据并生成专属调理建议...");

// 三餐数据对象
const dailyMeals = reactive({
  breakfast: { id:0,label: "Morning 早餐", name: "加载中...", tags: "清淡", image: "https://via.placeholder.com/150", effect: "正在计算...", steps: "" },
  lunch: { id:0,label: "Noon 午餐", name: "加载中...", tags: "营养", image: "https://via.placeholder.com/150", effect: "正在计算...", steps: "" },
  dinner: { id:0,label: "Night 晚餐", name: "加载中...", tags: "易消化", image: "https://via.placeholder.com/150", effect: "正在计算...", steps: "" }
});

// 核心逻辑：获取用户信息并调用 Projecter 智能体
const isAiLoading = ref(false);
const myInfo = ref<any>(null); // 存用户信息
const myBodyStatus = ref<any>(null); // 存最新的体质
const planStartDate = ref<Date | null>(null);

// 一个简单的计算年龄函数
const getMyAge = (birthday: string) => {
  if (!birthday) return ""; // 如果没有生日，返回空字符串
  const birthDate = new Date(birthday);
  const age = today.getFullYear() - birthDate.getFullYear();
  return age;
};

const getAiPlan = async () => {
  // 基础校验
  if (!myInfo.value || !myBodyStatus.value) return;
  const userId = authStore.user?.userId; 
  const currentConst = myBodyStatus.value.mainConstitution;
  
  if (!userId) return;

  isAiLoading.value = true;
  try {
    // 尝试从后端数据库获取已有计划
    console.log("🔍 开始查询用户", userId, "的", currentConst, "计划");
    const res = await getValidMealPlan(userId, currentConst);

    if (res && res.planJson) {
      console.log("✅ 后端返回有效计划，准备计算执行锚点...");

      const lastRecord = await findLatestCheckinApi(userId);
      const todayStr = new Date().toISOString().split('T')[0];
      const dToday = new Date(todayStr + 'T00:00:00');

      if (lastRecord) {
        const dLast = new Date(lastRecord.checkinDate + 'T00:00:00');
        const diffDays = Math.round((dToday.getTime() - dLast.getTime()) / 86400000);
        const streak = lastRecord.currentStreak;

        // 判断是否处于“有效连签中” 
        const isContinuing = diffDays <= 1 && streak > 0 && streak <= 7;

        if (isContinuing) {
          const offsetDays = isTodayChecked.value ? (streak - 1) : streak;
          const anchor = new Date(dToday);
          anchor.setDate(anchor.getDate() - offsetDays);
          planStartDate.value = anchor;
          console.log(`🚀 连签中(第${streak}天)，确定计划 Day 1 为:`, anchor.toISOString().split('T')[0]);
        } else {
          // 断签了或者是新的一轮
          planStartDate.value = dToday;
          console.log("🚀 暂无有效连签或已断签，将今天设为计划 Day 1");
        }
      } else {
        // 历史上从未打过卡
        planStartDate.value = dToday;
        console.log("🚀 用户无打卡记录，将今天设为计划 Day 1");
      }

      // 解析计划内容
      let finalJson = res.planJson;
      aiIdPlan.value = (typeof finalJson === 'string') ? JSON.parse(finalJson) : finalJson;

      if (aiIdPlan.value) {
        // 同步当前选定日期的三餐显示
        await updateDailyMealsByDate(new Date(selectedDateStr.value));
        isAiLoading.value = false;
        return;
      }
    }

    // 缓存失效或不存在，请求 AI 生成新计划
    console.log("☁️ 缓存失效或体质变更，请求 AI 生成新计划...");
    const askData = {
      primary: currentConst,
      secondary: myBodyStatus.value.subConstitution,
      age: getMyAge(myInfo.value.birthday),
      location: myInfo.value.nativePlace || myInfo.value.city,
      preferences: myInfo.value.content,
      solarTerm: currentDayData.value.solarTerm
    };

    const response = await chatStructured('projecter', JSON.stringify(askData));
    if (!response) return;

    let finalData = response.data ? response.data : response;
    if (typeof finalData === 'string') finalData = JSON.parse(finalData);

    // 更新前端状态并同步到后端
    if (finalData && finalData.breakfast) {
      // 新生成的方案，锚点一定是今天
      const todayAnchor = new Date();
      todayAnchor.setHours(0,0,0,0);
      planStartDate.value = todayAnchor;
      
      aiIdPlan.value = finalData;
      await updateDailyMealsByDate(new Date(selectedDateStr.value));

      // 保存到后端，注意参数名要和后端 Controller 对应
      await saveMealPlan(userId, JSON.stringify(finalData), currentConst);
      console.log("✅ AI 新计划已生成并锁定今日为起始日");
    }

  } catch (error) {
    console.error("❌ 调理方案流程中断:", error);
  } finally {
    isAiLoading.value = false;
  }
};

const updateDailyMealsByDate = async (targetDate: Date) => {
  // 基础检查
  if (!aiIdPlan.value || !planStartDate.value) {
    console.warn("[Diet] 计划数据或起始日期尚未初始化");
    return;
  }

  // 统一时间精度，只保留年月日，消除时区导致的计算偏差
  const startOfPlan = new Date(planStartDate.value);
  startOfPlan.setHours(0, 0, 0, 0);
  
  const startOfTarget = new Date(targetDate);
  startOfTarget.setHours(0, 0, 0, 0);
  
  // 计算偏移索引 (0-6)
  const dayIndex = Math.round((startOfTarget.getTime() - startOfPlan.getTime()) / 86400000);
  
  // 严格索引拦截：如果不在这 7 天范围内，不更新菜谱内容
  if (dayIndex < 0 || dayIndex > 6) {
    console.log(`[Diet] 日期 ${dayIndex} 超出 7 天计划范围，跳过更新`);
    return;
  }

  console.log(`[Diet] 锁定起始日为 ${startOfPlan.toLocaleDateString()}，正在加载第 ${dayIndex + 1} 天菜谱`);

  // 从 AI 计划中提取菜品 ID
  const ids = {
    breakfast: aiIdPlan.value.breakfast[dayIndex],
    lunch: aiIdPlan.value.lunch[dayIndex],
    dinner: aiIdPlan.value.dinner[dayIndex]
  };

  // 定义内部详情抓取函数
  const fetchAndAssign = async (id: number | undefined, type: 'breakfast' | 'lunch' | 'dinner', label: string) => {
    // 防御：无效 ID 处理
    if (!id || isNaN(id)) {
      dailyMeals[type].name = "调理建议生成中...";
      return;
    }
    
    try {
      // 检查缓存
      let dishData = dishCache.get(id);
      if (!dishData) {
        dishData = await getDishById(id);
        if (dishData) dishCache.set(id, dishData);
      }

      if (dishData) {
        // 更新响应式对象
        dailyMeals[type] = {
          id: id, 
          label: label,
          name: dishData.dishName || "滋补药膳",
          tags: dishData.effectTag1 || "体质调理",
          // 替换掉之前报错的 via.placeholder.com
          image: dishData.dishImgUrl || "https://dummyimage.com/600x400/f5f5f5/8b4513&text=Diet+Meal", 
          effect: `${dishData.effectTag1 || ''} ${dishData.effectTag2 || ''}`.trim(),
          steps: dishData.ancientCitation || "详情请咨询医师"
        };
      } else {
        dailyMeals[type].name = "暂无相关菜品";
      }
    } catch (error) {
      console.error(`[Diet] 抓取菜品 ${id} 失败:`, error);
      dailyMeals[type].name = "数据读取异常";
    }
  };

  // 并发执行请求
  await Promise.all([
    fetchAndAssign(ids.breakfast, 'breakfast', 'Morning 早餐'),
    fetchAndAssign(ids.lunch, 'lunch', 'Noon 午餐'),
    fetchAndAssign(ids.dinner, 'dinner', 'Night 晚餐')
  ]);
};

// 生命周期：进页面就开始加载
onMounted(async () => {
  const userId = authStore.user?.userId;
  const todayStr = today.toISOString().split('T')[0];

  console.log(">>> 页面加载，用户信息:", userId);
  
  // 基础 UI 数据初始化 
  const currentTerm = currentDayData.value.solarTerm;
  await initWeather();
  
  fetchRandomWisdom(currentTerm).then(res => {
    if (res) termSuggest.value = res;
  });

  // 如果用户已登录，加载业务数据
if (userId) {
    try {
      await fetchMonthCheckins(currentYear.value, currentMonth.value);
      const todayCount = await countTodayCheckinApi({ userId, today: todayStr });
      isTodayChecked.value = todayCount > 0;
      const lastRecord = await findLatestCheckinApi(userId);

if (lastRecord) {
  const todayStr = new Date().toISOString().split('T')[0];
  const lastDateStr = lastRecord.checkinDate;
  const dToday = new Date(todayStr + 'T00:00:00');
  const dLast = new Date(lastDateStr + 'T00:00:00');
  const diffDays = Math.round((dToday.getTime() - dLast.getTime()) / 86400000);

  if (diffDays === 0) {
    if (lastRecord.currentStreak >= 7 && (lastRecord.isRewarded === 1 || lastRecord.isRewarded === true)) {
      streakDays.value = 0;
    } else {
      streakDays.value = lastRecord.currentStreak;
    }
  } 
  else if (diffDays === 1 && (lastRecord.isRewarded === 1 || lastRecord.isRewarded === true)) {
    streakDays.value = 0;
  } 
  else if (diffDays === 1) {
    streakDays.value = lastRecord.currentStreak;
  } 
  else {
    streakDays.value = 0;
  }
}

      // AI 调理方案初始化
      const userRes = await getUserInfoAPI(userId);
      myInfo.value = userRes.data ? userRes.data : userRes; 

      //拿到用户信息后再去查天气
      await initWeather();

      const conRes = await selectByUserIdForArgentAPI(userId);
      const list = conRes.data || conRes;
      if (list && list.length > 0) {
        myBodyStatus.value = list[list.length - 1];
        console.log("✅ 确认拿到的是否为最新体质:", myBodyStatus.value);
        await getAiPlan(); // AI 计划依赖用户信息和体质
      } else {
        termSuggest.value = "检测到您还没有进行体质测试，请先完成测试哦！";
      }

    } catch (err) {
      console.error("初始化业务数据出错了:", err);
    }
  }
});
    // 补上这个计算属性，用来显示日历上方的日期标题
    const displayDateTitle = computed(() => {
      const d = new Date(selectedDateStr.value);
      return `${d.getFullYear()} / ${String(d.getMonth() + 1).padStart(2, '0')} / ${String(d.getDate()).padStart(2, '0')}`;
    });

    // 日历及计算逻辑
    const currentDayData = computed(() => {
      const d = new Date(selectedDateStr.value);
      const solar = Solar.fromDate(d);
      const lunar = solar.getLunar();
      
      // 12个月传统别称映射
      const seasonalNames = [
        "孟春", "仲春", "季春", "孟夏", "仲夏", "季夏",
        "孟秋", "仲秋", "季秋", "孟冬", "仲冬", "季冬"
      ];

      // 24节气精准查询逻辑
      // lunar.getJie() 返回的是当前日期所属的那个节气
      let termDisplay = lunar.getJieQi() || lunar.getJie() || seasonalNames[lunar.getMonth() - 1];

      return {
        solarTerm: termDisplay, 
        lunarDate: `农历 ${lunar.getMonthInChinese()}月${lunar.getDayInChinese()}`,
        weather: weather.value,
        suggest: termSuggest.value,
        meals: dailyMeals
      };
    });

    const isSelectable = (dateStr: string) => {
      if (!dateStr || !planStartDate.value) return false;
      
      const targetDate = new Date(dateStr + 'T00:00:00');
      const startOfPlan = new Date(planStartDate.value.getFullYear(), planStartDate.value.getMonth(), planStartDate.value.getDate());
      
      // 计算目标日期相对于“Day 1”偏移了几天
      const offsetFromStart = Math.round((targetDate.getTime() - startOfPlan.getTime()) / 86400000);
      
      const now = new Date();
      now.setHours(0, 0, 0, 0);
      
      return offsetFromStart >= 0 && offsetFromStart < 7 && targetDate >= now;
    };

    const isMonthPrevDisabled = computed(() => {
      const minDate = new Date(today.getFullYear(), today.getMonth() - 1);
      return currentYear.value <= minDate.getFullYear() && currentMonth.value <= minDate.getMonth();
    });

    const isMonthNextDisabled = computed(() => {
      const maxDate = new Date(today.getFullYear(), today.getMonth() + 1);
      return currentYear.value >= maxDate.getFullYear() && currentMonth.value >= maxDate.getMonth();
    });

const calendarDays = computed(() => {
  const days = [];
  const startDay = new Date(currentYear.value, currentMonth.value, 1).getDay();
  const endDate = new Date(currentYear.value, currentMonth.value + 1, 0).getDate();
  for (let i = 0; i < startDay; i++) days.push({ day: null });
  for (let i = 1; i <= endDate; i++) {
    const d = new Date(currentYear.value, currentMonth.value, i);
    const dStr = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
    const solar = Solar.fromDate(d);
    days.push({ 
      day: i, 
      fullDate: dStr, 
      isToday: dStr === today.toISOString().split('T')[0],
      lunar: solar.getLunar().getJieQi() || solar.getLunar().getDayInChinese()
    });
  }
  return days;
});

// 检查是否已打卡
const isCheckIn = (dateStr: string) => checkInDays.value.includes(dateStr);
const isSelected = (dateStr: string) => selectedDateStr.value === dateStr;
const isTodaySelected = computed(() => selectedDateStr.value === today.toISOString().split('T')[0]);

// 定义一个方法来获取数据库中的活泼文案
const fetchRandomWisdom = async (tagName: string) => {
  console.log(`[接口调试] 🚀 准备请求数据库，tagName 为: "${tagName}"`);
  try {
    const list = await getSeasonalWisdomAPI(tagName);
    if (list && list.length > 0) {
      // 从返回的 List 中随机抽取一条
      const randomIndex = Math.floor(Math.random() * list.length);
      const wisdom = list[randomIndex];
      // 格式化输出：【标题】内容
      return `${wisdom.title} ${wisdom.content}`;
    }
  } catch (error) {
    console.error("获取数据库文案失败:", error);
  }
  return null;
};

// 修改selectDate 函数
const selectDate = async (date: any) => { 
  if (date.fullDate && isSelectable(date.fullDate)) {
    selectedDateStr.value = date.fullDate;

    // 同步天气显示 
    syncWeatherByDate(date.fullDate);

    // 更新三餐 
    await updateDailyMealsByDate(new Date(date.fullDate));

    // 更新节气文案 
    const tagName = currentDayData.value.solarTerm;
    const dbSuggest = await fetchRandomWisdom(tagName);
    if (dbSuggest) {
      termSuggest.value = dbSuggest;
    }
  }
};

const goToday = () => {
  currentYear.value = today.getFullYear();
  currentMonth.value = today.getMonth();
  selectedDateStr.value = today.toISOString().split('T')[0];
};

const changeMonth = async (step: number) => {
  let nextMonth = currentMonth.value + step;
  let nextYear = currentYear.value;
  
  if (nextMonth < 0) { 
    nextMonth = 11; 
    nextYear--; 
  } else if (nextMonth > 11) { 
    nextMonth = 0; 
    nextYear++; 
  }
  
  currentMonth.value = nextMonth;
  currentYear.value = nextYear;

  // 使用新函数刷新打卡数据
  await fetchMonthCheckins(nextYear, nextMonth);
};

// 先定义励志文案库 (建议放在 doCheckIn 函数上方)
const inspirationalTips: Record<number, { title: string, message: string }> = {
  1: { title: "万事开头难", message: "第一天达成！食养之路始于足下，坚持下去，还有6天即可领取1000积分大奖！" },
  2: { title: "渐入佳境", message: "连续两天了！身体会记住你的每一份自律，再接再厉，距离大奖更近一步。" },
  3: { title: "不负韶华", message: "打卡第3天。一半的距离已经走完，坚持食养，是对身体最好的回馈。" },
  4: { title: "志存高远", message: "已经坚持4天了！你是最棒的，离圆满只剩3天，1000积分在向你招手！" },
  5: { title: "持之以恒", message: "第5天达成！不仅是积分，更重要的是你建立的健康习惯。加油！" },
  6: { title: "黎明之前", message: "最后一天冲刺！明天就是圆满之日，1000积分奖励就在眼前，千万别断签哦！" }
};

// 完整的 doCheckIn 函数
const doCheckIn = async () => {
  const userId = authStore.user?.userId;
// 1. 基础登录拦截
  if (!userId) {
    ElNotification({
      title: '请先登录',
      message: '登录后即可记录您的健康打卡历程',
      type: 'warning'
    });
    return;
  }

  // 判断 myBodyStatus 是否为空（在 onMounted 中我们会尝试获取体质）
  if (!myBodyStatus.value) {
    ElNotification({
      title: '体质数据缺失',
      message: '为了提供精准的食疗建议，请先完成体质测试后再开启签到挑战。',
      type: 'error',
      duration: 5000,
      position: 'top-right',
      offset: 100,
    });
    // router.push('/constitution-test'); 
    return;
  }

  // 严格前端拦截：如果状态显示已打卡，直接退出
  if (!userId || isTodayChecked.value) {
    console.log("今日已完成打卡，拦截重复操作");
    return;
  }

  // 获取本地标准日期字符串 (YYYY-MM-DD)
  const now = new Date();
  const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
  
  const yesterdayDate = new Date();
  yesterdayDate.setDate(now.getDate() - 1);
  const yesterdayStr = `${yesterdayDate.getFullYear()}-${String(yesterdayDate.getMonth() + 1).padStart(2, '0')}-${String(yesterdayDate.getDate()).padStart(2, '0')}`;

  try {
    // 获取最后一次打卡记录以判定连签
    const lastRecord = await findLatestCheckinApi(userId);
    
    let newStreak = 1;
    /*
     * 如果没有历史记录（第一次打卡），dbLastCheckinDate 设置为当天日期 todayStr
     * 这样数据库中该字段就不会显示为 Null 或空，而是显示为打卡当天的日期
     */
    let dbLastCheckinDate = lastRecord ? lastRecord.checkinDate : todayStr; 

    if (lastRecord) {
      // 判定是否连续：对比 YYYY-MM-DD 字符串
      if (dbLastCheckinDate === yesterdayStr) {
        // 如果昨天打过卡了，检查是否需要重置周期
        if (lastRecord.isRewarded === 1 || lastRecord.currentStreak >= 7) {
          newStreak = 1; // 满7天领过奖后，新的一天重新开始
        } else {
          newStreak = lastRecord.currentStreak + 1;
        }
      } else if (dbLastCheckinDate === todayStr) {
        // 二次防御：数据库已有今日记录
        isTodayChecked.value = true;
        return;
      } else {
        // 断签，重置为 1
        newStreak = 1;
      }
    }

    // 奖励判定
    const isNowSevenDays = (newStreak === 7); 
    const isRewarded = isNowSevenDays ? 1 : 0;

    const checkinParam = {
      userId,
      checkinDate: todayStr,
      lastCheckinDate: dbLastCheckinDate,
      currentStreak: newStreak,
      isRewarded: isRewarded
    };

    console.log("提交打卡:", checkinParam);
    
    // 提交数据库
    await insertCheckinApi(checkinParam);

    // 更新 UI 基础状态
    if (!checkInDays.value.includes(todayStr)) {
      checkInDays.value.push(todayStr);
    }
    isTodayChecked.value = true;

    // 分支处理：满7天奖励 vs 1-6天励志提示
    if (isNowSevenDays) {
      try {
        // 调用加分接口
        await addUserPoints(Number(userId), 1000); 

        // 记录积分流水日志
        const logData = {
          userId: Number(userId),
          type: 1, 
          amount: 1000,
          balance: 0, 
          description: "七日连签奖励"
        };
        await insertPointLogApi(logData);

        // 触发大弹窗和顶部通知
        showRewardModal.value = true;
        ElNotification({
          title: '圆满达成！',
          message: '您已完成七日食疗挑战，1000 积分大奖已存入账户。',
          type: 'success',
          duration: 3000,
          position: 'top-right',
          offset: 100,
        });

        // UI 进度归零（表示新一轮开始）
        streakDays.value = 0; 
      } catch (rewardError) {
        console.error("奖励发放流程出错:", rewardError);
      }
    } else {
      streakDays.value = newStreak; // 更新实时进度显示

      // 根据当前天数匹配励志文案
      const tip = inspirationalTips[newStreak];
      if (tip) {
        ElNotification({
          title: tip.title,
          message: tip.message,
          type: 'info', // 使用 info 类型，与大奖的 success 区分开
          duration: 4000,
          position: 'top-right',
          offset: 100,
        });
      }
    }

  } catch (error) {
    console.error("打卡主流程失败:", error);
    alert("打卡失败，请检查网络后再试");
    // 回滚前端状态
    isTodayChecked.value = false;
  }
};

const fetchMonthCheckins = async (year: number, month: number) => {
  const userId = authStore.user?.userId;
  if (!userId) return;

  // 使用字符串拼接待代替换 toISOString，确保获取的是本地时间的 1 号和最后一天
  const firstDay = `${year}-${String(month + 1).padStart(2, '0')}-01`;
  
  // 获取本月最后一天：下个月的第 0 天即为本月最后一天
  const lastDateObj = new Date(year, month + 1, 0);
  const lastDay = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDateObj.getDate()).padStart(2, '0')}`;

  console.log(`[Checkin] 正在请求范围: ${firstDay} 至 ${lastDay}`);

  try {
    const range = await getCheckinRangeApi({ userId, startDate: firstDay, endDate: lastDay });
    if (range) {
      // 统一格式化为 YYYY-MM-DD，过滤掉后端可能带有的时间后缀
      checkInDays.value = range.map((d: string) => d.split(' ')[0]);
      console.log("[Checkin] 刷新成功，当前记录数:", checkInDays.value.length);
    }
  } catch (err) {
    console.error("[Checkin] 获取月度打卡记录失败:", err);
  }
};

const showRewardModal = ref(false); // 控制奖励弹窗显示

// 新增 Tab 切换拦截函数 
const handleTabChange = () => {
  // 如果当前在信息页，且想去菜谱页
  if (activeTab.value === 'info') {
    // 检查是否有体质数据
    if (!myBodyStatus.value) {
      ElNotification({
        title: '开启食养方案',
        message: '请先完成体质测试，以便 AI 为您生成专属的膳食配伍建议。',
        type: 'warning',
        duration: 4000,
        position: 'top-right',
        offset: 100,
      });
      return; // 拦截，不进行切换
    }
    activeTab.value = 'meals';
  } else {
    // 如果已经在菜谱页，随时可以返回信息页
    activeTab.value = 'info';
  }
};

// 查看食谱详情
const viewRecipe = (meal: any) => { 
  activeRecipe.value = meal; 
};
</script>

