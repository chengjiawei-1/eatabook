<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl space-y-8">
        
        <!-- 页面标题 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-2">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              数据大屏 <span class="text-amber-500 text-base ml-2">Analytics</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              实时监控平台健康数据与用户行为趋势，当前状态：<span class="text-emerald-500 font-bold">同步中</span>
            </p>
          </div>
          <div class="text-[11px] font-black text-slate-400 bg-white dark:bg-slate-900 px-5 py-2.5 rounded-2xl shadow-xl shadow-slate-200/50 border border-white dark:border-slate-800 uppercase tracking-widest">
            Last Updated：{{ updateTime }}
          </div>
        </div>

        <!-- 核心指标卡片 (统一控制台风格) -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div
            v-for="(card, index) in cards"
            :key="card.title"
            class="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none transition-all hover:-translate-y-1 relative group overflow-hidden"
          >
            <!-- 背景装饰图标 -->
            <div class="absolute -right-4 -bottom-4 opacity-[0.03] group-hover:opacity-[0.08] transition-all duration-500">
                <i :class="['fa-solid', card.icon, 'text-8xl rotate-12']"></i>
            </div>
            
            <div class="relative z-10 flex items-center justify-between">
              <div>
                <p class="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1">{{ card.title }}</p>
                <h3 class="text-3xl font-black text-slate-800 dark:text-white">{{ card.value }}</h3>
              </div>
              <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg', card.bgClass, card.textClass]">
                <i :class="['fa-solid', card.icon, 'text-xl']"></i>
              </div>
            </div>
            <!-- 进度条装饰 -->
            <div class="mt-6 h-1.5 w-full bg-slate-50 dark:bg-slate-800 rounded-full overflow-hidden">
              <div class="h-full rounded-full transition-all duration-1000 ease-out" :class="card.barColor" :style="{ width: card.percent + '%' }"></div>
            </div>
          </div>
        </div>

        <!-- 主图表区域 (统一圆角与头部) -->
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
          
          <!-- 左侧：全国地图 -->
          <div class="xl:col-span-2 bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden flex flex-col h-[650px]">
            <div class="px-8 py-5 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/30 dark:bg-slate-800/30">
              <h3 class="text-sm font-black text-slate-700 dark:text-slate-200 uppercase tracking-widest flex items-center gap-2">
                <i class="fa-solid fa-map-location-dot text-amber-500"></i>
                地域体质分布监控
              </h3>
              
              <div class="relative">
                <select v-model="selectedRegion" @change="loadMap" class="filter-select">
                  <option value="all">全国视图 View</option>
                  <option v-for="r in regions" :key="r" :value="r">{{ r }}</option>
                </select>
              </div>
            </div>
            <div class="flex-1 relative p-6">
              <div ref="mapRef" class="w-full h-full"></div>
            </div>
            <!-- 地图来源标注 -->
            <div class="px-6 py-4 border-t border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/30 text-center">
              <p class="text-xs text-slate-500 dark:text-slate-400">
                审图号：GS（2024）0650号<br>
                地图来源：国家地理信息公共服务平台<br>
                (https://www.tianditu.gov.cn/)
              </p>
            </div>
          </div>

          <!-- 右侧：多维分析 -->
          <div class="xl:col-span-1 bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden flex flex-col h-[650px]">
            <!-- 选项卡头部 -->
            <div class="flex p-2 gap-1 border-b border-slate-50 dark:border-slate-800 bg-slate-50/30">
              <button
                v-for="t in tabs"
                :key="t.key"
                @click="switchTab(t.key)"
                class="flex-1 py-3 rounded-2xl text-[10px] font-black uppercase tracking-tighter transition-all"
                :class="activeTab === t.key 
                    ? 'bg-white dark:bg-slate-800 text-amber-600 shadow-md ring-1 ring-slate-100 dark:ring-slate-700' 
                    : 'text-slate-400 hover:text-slate-600'"
              >
                <i :class="['fa-solid', t.icon, 'mb-1 block text-base']"></i>
                {{ t.label }}
              </button>
            </div>

            <!-- 图表内容区 -->
            <div class="flex-1 p-8 relative overflow-hidden">
              <div v-show="activeTab === 'constitution'" class="h-full flex flex-col">
                <div class="mb-4 flex items-center gap-2">
                    <div class="w-1 h-4 bg-amber-500 rounded-full"></div>
                    <h4 class="text-xs font-black text-slate-800 dark:text-slate-200 uppercase tracking-widest">
                        {{ selectedRegion === 'all' ? '全国' : selectedRegion }} · 占比概况
                    </h4>
                </div>
                <div ref="pieRef" class="flex-1"></div>
              </div>
              <div v-show="activeTab === 'dish'" class="h-full flex flex-col">
                <div class="mb-4 flex items-center gap-2">
                    <div class="w-1 h-4 bg-indigo-500 rounded-full"></div>
                    <h4 class="text-xs font-black text-slate-800 dark:text-slate-200 uppercase tracking-widest">热门食疗 Top 排行</h4>
                </div>
                <div ref="barRef" class="flex-1"></div>
              </div>
              <div v-show="activeTab === 'relation'" class="h-full flex flex-col">
                <div class="mb-4 flex items-center gap-2">
                    <div class="w-1 h-4 bg-emerald-500 rounded-full"></div>
                    <h4 class="text-xs font-black text-slate-800 dark:text-slate-200 uppercase tracking-widest">体质-食物关联强度</h4>
                </div>
                <div ref="radarRef" class="flex-1"></div>
              </div>
              <div v-show="activeTab === 'keywords'" class="h-full flex flex-col">
                <div class="mb-4 flex items-center gap-2">
                    <div class="w-1 h-4 bg-rose-500 rounded-full"></div>
                    <h4 class="text-xs font-black text-slate-800 dark:text-slate-200 uppercase tracking-widest">搜索热词云分布</h4>
                </div>
                <div ref="wordRef" class="flex-1"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </div>
  </div>
</template>

<style scoped>
.filter-select {
  @apply bg-white dark:bg-slate-800 border-none rounded-2xl px-5 py-2.5 text-xs font-black text-slate-700 dark:text-slate-200 shadow-xl shadow-slate-200/50 focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-all pr-10 appearance-none;
}

/* 自定义滚动条风格 */
::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>

<script setup lang="ts">
/* 脚本逻辑部分 100% 保持您提供的代码，不作任何修改 */
import { onMounted, onBeforeUnmount, ref, watch, nextTick, computed } from "vue";
import * as echarts from "echarts";
import 'echarts-wordcloud';
import { 
  tabs, regions, constitutionColorMap,
  getDashboardCards, getTop8DishRank, getAllConstitution, 
  getConstitutionByProvince, getRadarDishes, getAllMapData, getTopKeywords 
} from '@/api/databoard';

const mapReady = ref(false);
const initChinaMap = async () => {
  const res = await fetch('/maps/中国.geojson');
  const chinaJson = await res.json();

  echarts.registerMap('china', chinaJson);

  await nextTick();
  await new Promise(r => setTimeout(r, 50));

  mapReady.value = true;   

  loadMap();
};

const cards = ref<any[]>([]);
const activeTab = ref<"constitution" | "dish" | "relation" | "keywords">("constitution");
const selectedRegion = ref<string>("all");
const updateTime = computed(() => new Date().toLocaleString());
const mapDataCache = ref<any[]>([]);
const pieRef = ref();
const barRef = ref();
const radarRef = ref();
const mapRef = ref();
const wordRef = ref();
let charts: Record<string, any> = { pie: null, bar: null, radar: null, map: null, word: null };

const insights = [
  { title: '体质分布趋势', icon: 'fa-user-group', content: '平和质用户占比最高(35%)，但近年来气虚质和阴虚质用户呈现上升趋势，可能与现代生活压力增大有关。' },
  { title: '食疗偏好分析', icon: 'fa-bowl-food', content: '粥类和羹类食疗方案最受欢迎，用户倾向于选择制作简单、易于坚持的调理方式，尤其是早餐场景。' },
  { title: '季节性变化', icon: 'fa-cloud-sun', content: '夏季清热祛湿类菜品需求显著增加，冬季则以温补类菜品为主，体现了"天人合一"的养生理念。' },
  { title: '用户行为特点', icon: 'fa-hand-pointer', content: '完成体质测试的用户中，有78%会继续查看个性化食疗建议，65%的用户会收藏至少一个食疗方案。' }
];

const getSeason = () => {
  const month = new Date().getMonth() + 1; 
  if (month >= 3 && month <= 5) return 'spring'; 
  if (month >= 6 && month <= 8) return 'summer'; 
  if (month >= 9 && month <= 11) return 'autumn'; 
  return 'winter'; 
};

const loadCards = async () => {
  const rawCards = await getDashboardCards();
  const styles = [
    { icon: 'fa-users', colorClass: 'text-blue-500', bgClass: 'bg-blue-50 dark:bg-blue-900/20', textClass: 'text-blue-600 dark:text-blue-400', barColor: 'bg-blue-500', percent: 70 },
    { icon: 'fa-book-open', colorClass: 'text-purple-500', bgClass: 'bg-purple-50 dark:bg-purple-900/20', textClass: 'text-purple-600 dark:text-purple-400', barColor: 'bg-purple-500', percent: 45 },
    { icon: 'fa-utensils', colorClass: 'text-green-500', bgClass: 'bg-green-50 dark:bg-green-900/20', textClass: 'text-green-600 dark:text-green-400', barColor: 'bg-green-500', percent: 60 },
    { icon: 'fa-eye', colorClass: 'text-amber-500', bgClass: 'bg-amber-50 dark:bg-amber-900/20', textClass: 'text-amber-600 dark:text-amber-400', barColor: 'bg-amber-500', percent: 85 }
  ];
  cards.value = rawCards.map((card: any, index: number) => ({ ...card, ...styles[index % styles.length] }));
};

const loadPie = async () => {
  const data = selectedRegion.value === 'all' ? await getAllConstitution() : await getConstitutionByProvince(selectedRegion.value);
  if (!charts.pie && pieRef.value) charts.pie = echarts.init(pieRef.value);
  const centerTitle = selectedRegion.value === 'all' ? '全国' : selectedRegion.value;
  charts.pie?.setOption({
    title: { text: centerTitle, subtext: '分布概况', left: 'center', top: '45%', textStyle: { fontSize: 18, fontWeight: 'bold', color: '#8b4513' }, subtextStyle: { fontSize: 12, color: '#94a3b8' } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: '2%', left: 'center', icon: 'circle', textStyle: { fontSize: 11 } },
    series: [{ name: '体质分布', type: 'pie', radius: ['50%', '75%'], center: ['50%', '48%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 }, label: { show: false }, emphasis: { label: { show: false } }, data: data.map((d: any) => ({ name: d.name, value: d.value, itemStyle: { color: constitutionColorMap[d.name] } })) }]
  }, true);
};

const loadBar = async () => {
  const data = await getTop8DishRank();
  if (!charts.bar && barRef.value) charts.bar = echarts.init(barRef.value);
  const sortedData = [...data].sort((a: any, b: any) => a.value - b.value);
  charts.bar?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '3%', containLabel: true },
    xAxis: { type: 'value', show: false },
    yAxis: { type: 'category', data: sortedData.map((i: any) => i.name), axisTick: { show: false }, axisLine: { show: false } },
    series: [{ type: 'bar', data: sortedData.map((i: any) => ({ value: i.value, itemStyle: { color: i.color || '#f59e0b', borderRadius: [0, 4, 4, 0] } })), label: { show: true, position: 'right' }, barWidth: '50%' }]
  });
};

const loadRadar = async () => {
  try {
    const data = await getRadarDishes(getSeason());
    if (!charts.radar && radarRef.value) charts.radar = echarts.init(radarRef.value);
    const radarData = data.length > 0 ? data : [{ subject: '平和', A: 80 }, { subject: '气虚', A: 50 }, { subject: '阳虚', A: 40 }, { subject: '阴虚', A: 60 }, { subject: '痰湿', A: 30 }, { subject: '湿热', A: 40 }];
    charts.radar?.setOption({
      radar: { indicator: radarData.map((d: any) => ({ name: d.subject, max: 100 })), radius: '65%', splitArea: { areaStyle: { color: ['rgba(255,255,255,0)', 'rgba(200,200,200,0.1)'] } } },
      series: [{ type: 'radar', data: [{ value: radarData.map((d: any) => d.A), name: '关联度', areaStyle: { color: 'rgba(245, 158, 11, 0.2)' }, lineStyle: { color: '#f59e0b', width: 2 }, symbol: 'circle', symbolSize: 6, itemStyle: { color: '#f59e0b' } }] }]
    });
  } catch(e) { console.error(e) }
};

const loadWordCloud = async () => {
  try {
    const data = await getTopKeywords();
    if (!charts.word && wordRef.value) charts.word = echarts.init(wordRef.value);
    charts.word?.setOption({
      series: [{ type: 'wordCloud', shape: 'circle', sizeRange: [12, 50], rotationRange: [-45, 45], gridSize: 8, textStyle: { fontFamily: 'sans-serif', fontWeight: 'bold', color: () => `rgb(${[Math.round(Math.random() * 160), Math.round(Math.random() * 160), Math.round(Math.random() * 160)].join(',')})` }, data: data.map((i: any) => ({ name: i.text, value: i.value })) }]
    });
  } catch(e) { console.error(e) }
};

const loadMap = async () => {
  // 防止地图未初始化
  if (!mapReady.value || !mapRef.value) return;

  // 获取数据
  if (mapDataCache.value.length === 0) {
    try {
      mapDataCache.value = await getAllMapData();
    } catch (e) {
      console.error('地图数据加载失败:', e);
      return;
    }
  }

  const normalizeName = (name: string) =>
    name ? name.replace(/省|市|自治区|特别行政区|壮族|回族|维吾尔/g, "") : "";

  // 确保 echarts 实例只初始化一次
  if (!charts.map) {
    charts.map = echarts.init(mapRef.value);
  }

  // 等 DOM + canvas 稳定
  await new Promise(requestAnimationFrame);

  // 数据处理
  const processedData = mapDataCache.value.map((d: any) => {
    const isSelected =
      selectedRegion.value !== "all" &&
      normalizeName(d.name) === normalizeName(selectedRegion.value);

    return {
      name: d.name,
      value: d.constitution,
      constitution: d.constitution,
      itemStyle: {
        areaColor: isSelected
          ? constitutionColorMap[d.constitution] || "#f3f4f6"
          : selectedRegion.value === "all"
          ? constitutionColorMap[d.constitution] || "#f3f4f6"
          : "#f3f4f6"
      },
      select: {
        itemStyle: {
          areaColor: "#f59e0b"
        }
      }
    };
  });

  //echarts 配置
  const option = {
    tooltip: {
      trigger: "item",
      formatter: (params: any) =>
        params.data
          ? `${params.name}<br/>主体质: <b>${params.data.constitution}</b>`
          : params.name
    },

    visualMap: {
      type: "piecewise",
      pieces: Object.entries(constitutionColorMap).map(([k, v]) => ({
        value: k,
        label: k,
        color: v
      })),
      left: "left",
      bottom: "5%",
      show: true,
      textStyle: { color: "#666" }
    },

    series: [
      {
        name: "地域体质",
        type: "map",
        map: "china",
        roam: true,
        zoom: 1.2,
        scaleLimit: { min: 0.8, max: 2.5 },
        selectedMode: "single",
        label: { show: false },
        itemStyle: {
          borderColor: "#fff",
          borderWidth: 0.5
        },
        emphasis: {
          itemStyle: { areaColor: "#fbbf24" },
          label: { show: true }
        },
        data: processedData
      }
    ]
  };

  charts.map.setOption(option, true);

  // 清理旧事件
  charts.map.off("click");

  // 点击交互
  charts.map.on("click", (params: any) => {
    if (!params.name) return;

    const clicked = normalizeName(params.name);
    const selected = normalizeName(selectedRegion.value);

    selectedRegion.value =
      clicked === selected ? "all" : params.name;
  });

  // 强制选中态修正
  if (selectedRegion.value !== "all") {
    const fullName = processedData.find(
      d => normalizeName(d.name) === normalizeName(selectedRegion.value)
    )?.name;

    if (fullName) {
      charts.map.dispatchAction({
        type: "mapSelect",
        name: fullName
      });
    }
  } else {
    charts.map.dispatchAction({
      type: "unselect",
      seriesIndex: 0
    });
  }
};

const switchTab = (key: any) => { activeTab.value = key; };
const handleResize = () => { Object.values(charts).forEach(chart => chart?.resize()); };

watch(activeTab, (newTab) => { nextTick(() => { if (newTab === 'constitution') loadPie(); if (newTab === 'dish') loadBar(); if (newTab === 'relation') loadRadar(); if (newTab === 'keywords') loadWordCloud(); }); });
watch(selectedRegion, async () => {
  if (!mapReady.value) return;
  await loadMap();

  if (activeTab.value === 'constitution') {
    await loadPie();
  }
});
onMounted(async () => {
  await loadCards();

  await initChinaMap(); 

  await loadPie();

  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); Object.values(charts).forEach(chart => chart?.dispose()); });
</script>