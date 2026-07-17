<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <AdminNav />
    
    <main class="pt-24 pb-16">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 仅在根路径 /admin 显示 -->
        <div v-if="$route.path === '/admin'" class="space-y-6">
          
          <!-- 顶部欢迎栏 -->
          <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-2">
            <div>
              <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
                控制台概览 <span class="text-amber-500 text-base ml-2">Real-time</span>
              </h1>
              <p class="text-slate-500 text-sm mt-1">
                今天是 {{ currentDate }}，系统状态：<span class="text-emerald-500 font-bold">运行良好</span>
              </p>
            </div>
            <button 
              @click="refreshData" 
              :disabled="loading"
              class="px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-sm hover:shadow-md transition-all text-sm font-bold flex items-center gap-2 text-slate-700 dark:text-slate-200"
            >
              <i class="fa-solid fa-arrows-rotate" :class="{ 'fa-spin': loading }"></i>
              {{ loading ? '同步中...' : '同步实时数据' }}
            </button>
          </div>

          <!-- 加载/错误处理 -->
          <div v-if="loading" class="py-20 text-center bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-800">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500">正在调取实时数据...</p>
          </div>

          <div v-else-if="error" class="py-20 text-center bg-red-50 dark:bg-red-900/10 rounded-3xl border border-red-100 dark:border-red-900/20">
            <i class="fa-solid fa-triangle-exclamation text-red-500 text-3xl mb-4"></i>
            <p class="text-slate-600 dark:text-slate-300">{{ error }}</p>
            <button @click="loadDashboardData" class="mt-4 px-4 py-2 bg-white dark:bg-slate-800 rounded-lg text-sm font-bold shadow-sm">重试</button>
          </div>

          <!-- 四大核心指标卡片 -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div v-for="(stat, index) in stats" :key="stat.label" 
              class="bg-white dark:bg-slate-900 rounded-3xl p-5 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none transition-all hover:-translate-y-1 relative group overflow-hidden">
              
              <div class="flex justify-between relative z-10">
                <div>
                  <p class="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1">{{ stat.label }}</p>
                  <h3 class="text-2xl font-black text-slate-800 dark:text-white">{{ stat.value }}</h3>
                  <div :class="['text-[11px] mt-1 font-bold', stat.trend >= 0 ? 'text-emerald-500' : 'text-rose-500']">
                    {{ stat.trend >= 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}% 
                    <span class="text-slate-400 font-normal ml-1">昨日: {{ stat.yesterdayValue }}</span>
                  </div>
                </div>
                <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg', stat.bgClass, stat.colorClass]">
                  <i :class="['fa-solid', stat.icon, 'text-xl']"></i>
                </div>
              </div>

              <!-- 趋势微图：真实反映昨日到今日的跳变 -->
              <div class="h-12 w-full mt-2">
                <v-chart class="h-full w-full" :option="stat.chartOption" autoresize />
              </div>
            </div>
          </div>

          <!-- 中间大图表区域 -->
          <div v-if="!loading && !error" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- 业务今日概览 (柱状图) -->
            <div class="lg:col-span-2 bg-white dark:bg-slate-900 rounded-3xl p-6 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none">
              <h3 class="text-lg font-black text-slate-800 dark:text-white mb-6">当季食养推荐用户行为分析</h3>
              <div class="h-80 w-full">
                <v-chart :option="mainTrendOption" autoresize />
              </div>
              
          <!-- 统计信息显示 -->
          <div class="mt-4 flex items-center justify-center gap-6 text-sm text-slate-600 dark:text-slate-400">
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-indigo-500"></span>
              <span class="font-medium">总点击: {{ dashboardData.totalClicks }}次</span>
            </div>
            <div class="flex items-center gap-2">
              <span class="w-3 h-3 rounded-full bg-rose-500"></span>
              <span class="font-medium">总收藏: {{ dashboardData.totalFavorites }}次</span>
            </div>
            <div class="flex items-center gap-2 bg-indigo-50 dark:bg-indigo-900/20 px-3 py-1 rounded-lg">
              <span class="text-indigo-600 dark:text-indigo-300 font-semibold">
                转化率: {{ dashboardData.clickToFavoriteRate }}%
              </span>
            </div>
          </div>
         </div>

          <!-- 资源占比饼图 -->
          <div class="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none flex flex-col">
              <h3 class="text-lg font-black text-slate-800 dark:text-white mb-6">资源库配比</h3>
              <div class="flex-1 min-h-[350px]">
                <v-chart :option="contentPieOption" autoresize />
              </div>
            </div>
          </div>

          <!-- 底部快捷模块-->
          <div v-if="!loading && !error" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <RouterLink 
              v-for="link in quickLinks" :key="link.name" :to="link.to"
              class="group bg-white dark:bg-slate-900 p-5 rounded-3xl border border-transparent hover:border-amber-400 hover:shadow-xl hover:shadow-amber-500/10 transition-all duration-300 flex flex-col items-center text-center"
            >
              <div class="w-14 h-14 rounded-2xl bg-slate-50 dark:bg-slate-800 flex items-center justify-center mb-3 group-hover:bg-amber-500 group-hover:rotate-6 transition-all duration-300">
                <i :class="['fa-solid', link.icon, 'text-xl text-slate-400 group-hover:text-white transition-colors']"></i>
              </div>
              <span class="text-xs font-black text-slate-600 dark:text-slate-400 group-hover:text-slate-900 dark:group-hover:text-white">{{ link.name }}</span>
            </RouterLink>
          </div>
        </div>

        <!-- 子路由渲染 -->
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminNav from '@/components/AdminNav.vue'
import { getSeasonalRecommendationStats } from '@/api/analytics';

// API 引入 
import { getDashboardCards } from '@/api/databoard'
import { getAllUserAPI } from '@/api/user'
import { getAllBooks } from '@/api/ancientBooks'  
import { getAdminPostsAPI } from '@/api/forums'  
import { totalCountAPI, todayCountAPI, yesterdayCountAPI } from '@/api/constitution'
import { totalCheckinsApi, todayCheckinsApi, yesterdayCheckinsApi } from '@/api/checkin'

// ECharts 引入
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent ,TitleComponent} from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

// 在AdminPage.vue中添加缓存逻辑
const CACHE_KEY = 'admin_dashboard_cache'
const CACHE_EXPIRY = 24 * 60 * 60 * 1000 // 24小时

// 获取缓存数据
const getCachedData = () => {
  try {
    const cached = localStorage.getItem(CACHE_KEY)
    if (!cached) return null
    
    const data = JSON.parse(cached)
    if (Date.now() - data.timestamp > CACHE_EXPIRY) {
      localStorage.removeItem(CACHE_KEY)
      return null
    }
    return data
  } catch {
    return null
  }
}

// 保存缓存数据
const saveCachedData = (todayActive: number, totalUsers: number) => {
  try {
    const cacheData = {
      timestamp: Date.now(),
      yesterdayActive: todayActive, // 今天的活跃数就是明天的"昨日活跃"
      yesterdayTotal: totalUsers,
      date: new Date().toDateString()
    }
    localStorage.setItem(CACHE_KEY, JSON.stringify(cacheData))
  } catch (error) {
    console.error('保存缓存失败:', error)
  }
}

const $route = useRoute()
const $router = useRouter()

// 响应式状态
const loading = ref(true)
const error = ref('')
const dashboardData = ref<any>({
  recommendationClicks: [0, 0, 0, 0, 0, 0, 0], // 近7日点击查看详情数量
  dishFavorites: [0, 0, 0, 0, 0, 0, 0], // 近7日收藏菜品数量
  clickToFavoriteRate: 0, // 点击后收藏转化率
  totalClicks: 0, // 总点击次数
  totalFavorites: 0 // 总收藏次数
})
const yesterdayActiveUsers = ref(0)
const yesterdayTotalUsers = ref(0)

const constitutionTestStats = ref({ totalCount: 0, todayCount: 0, yesterdayCount: 0 })
const checkinStats = ref({ totalCount: 0, todayCount: 0, yesterdayCount: 0 })

const currentDate = computed(() => new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }))

// 核心数据加载逻辑
const loadDashboardData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [
      cardsRes, usersRes, booksRes, forumRes, 
      totalCountRes, todayCountRes, yesterdayCountRes, 
      totalCheckinsRes, todayCheckinsApiRes, yesterdayCheckinsApiRes, 
      behaviorRes
    ] = await Promise.all([
      getDashboardCards(), getAllUserAPI(), getAllBooks(), getAdminPostsAPI(),
      totalCountAPI(), todayCountAPI(), yesterdayCountAPI(),
      totalCheckinsApi(), todayCheckinsApi(), yesterdayCheckinsApi(),
      getSeasonalRecommendationStats() // 这里的行为分析数据是关键
    ])
    
    const parseNum = (res: any) => {
      if (typeof res === 'number') return res
      return res?.data?.data ?? res?.data ?? 0
    }

    // 更新体质测试与打卡数据
    constitutionTestStats.value = { 
      totalCount: parseNum(totalCountRes), 
      todayCount: parseNum(todayCountRes), 
      yesterdayCount: parseNum(yesterdayCountRes) 
    }
    checkinStats.value = { 
      totalCount: parseNum(totalCheckinsRes), 
      todayCount: parseNum(todayCheckinsApiRes), 
      yesterdayCount: parseNum(yesterdayCheckinsApiRes) 
    }

    // 处理用户增长逻辑
    const users = Array.isArray(usersRes) ? usersRes : (usersRes as any).data || []
    const today = new Date().setHours(0, 0, 0, 0)
    const yesterday = today - 24 * 60 * 60 * 1000

    const todayActive = users.filter((u: any) => u.lastLoginTime && new Date(u.lastLoginTime).setHours(0, 0, 0, 0) === today).length
    yesterdayActiveUsers.value = users.filter((u: any) => u.lastLoginTime && new Date(u.lastLoginTime).setHours(0, 0, 0, 0) === yesterday).length
    yesterdayTotalUsers.value = users.filter((u: any) => u.registerTime && new Date(u.registerTime).setHours(0, 0, 0, 0) < today).length

    // 处理书籍与论坛
    const books = Array.isArray(booksRes) ? booksRes : []
    // 论坛数据统一兼容多种返回格式：AxiosResponse<ApiResponse<T>> / 直接数组等
    const forumRaw =
      (forumRes as any)?.data?.data ??
      (forumRes as any)?.data ??
      forumRes
    const forumData = Array.isArray(forumRaw) ? forumRaw : []

    // 统计古籍种类分布
    const bookCategories: Record<string, number> = {}
    books.forEach((book: any) => {
      // 调试：查看每本书的数据结构
      console.log('单本书籍数据:', book);
      
      const category = book.bookCategory || book.category || book.type || '未知'
      bookCategories[category] = (bookCategories[category] || 0) + 1
    })

    // 调试：查看统计结果
    console.log('古籍种类统计结果:', bookCategories);

    // 基础面板数据填充
    dashboardData.value = {
      ...cardsRes?.data ?? {},
      totalUsers: users.length,
      todayActiveUsers: todayActive,
      yesterdayActiveUsers: yesterdayActiveUsers.value,
      totalBooks: books.length,
      totalPosts: forumData.length,
      bookCategories,
      totalDishes: cardsRes?.data?.totalDishes ?? 0,
      totalIngredients: cardsRes?.data?.totalIngredients ?? 0,
    }

    console.log('--- 行为分析原始响应对象 ---', behaviorRes); // 添加这一行

    if (behaviorRes?.data?.success || behaviorRes?.success) { // 兼容两种可能的解包方式
      const bData = behaviorRes.data?.data || behaviorRes.data || behaviorRes;
      console.log('分析接口处理后的详情:', bData);
      
      // 映射到 chartData 所需的字段
      // 假设后端 Service 已经按照我上一步建议的方式将 Map 转为了 dates, clickData, favoriteData 数组
      dashboardData.value.dates = bData.dates || []; 
      dashboardData.value.recommendationClicks = bData.clickData || [];
      dashboardData.value.dishFavorites = bData.favoriteData || [];
      dashboardData.value.totalClicks = bData.totalClicks || 0;
      dashboardData.value.totalFavorites = bData.totalFavorites || 0;
      dashboardData.value.clickToFavoriteRate = bData.conversionRate || 0;
    }

  } catch (err) {
    console.error('加载仪表盘数据失败:', err)
    error.value = '数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}


// 趋势计算 
const getTrend = (todayVal: number, yesterdayVal: number) => {
  if (!yesterdayVal) return 0
  return Math.round(((todayVal - yesterdayVal) / yesterdayVal) * 100)
}

// ECharts 配置生成

// 这里的微型图表只展示两个点：昨日和今日，保证 100% 真实
const getRealSparkline = (today: number, yesterday: number, color: string) => {
  const data = [yesterday, today];
  
  return {
    grid: { left: 0, right: 0, top: 10, bottom: 0 },
    xAxis: { type: 'category', show: false, boundaryGap: false },
    yAxis: { 
      type: 'value', 
      show: false, 
      // 让 Y 轴不从 0 开始，这样微小的变化也能看到起伏
      min: (value: any) => value.min * 0.95 
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: color },
        // 这里的data必须使用参数传入的值
        data: data,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: color + '44' }, // 顶部半透明
              { offset: 1, color: color + '00' }  // 底部全透明
            ]
          }
        }
      }
    ]
  }
}

const stats = computed(() => [
  {
    label: '总注册用户',
    value: dashboardData.value.totalUsers,
    yesterdayValue: yesterdayTotalUsers.value,
    icon: 'fa-users',
    trend: getTrend(dashboardData.value.totalUsers, yesterdayTotalUsers.value),
    colorClass: 'text-indigo-600',
    bgClass: 'bg-indigo-50 dark:bg-indigo-900/30',
    // 传入该卡片对应的真实数据
    chartOption: getRealSparkline(dashboardData.value.totalUsers, yesterdayTotalUsers.value, '#6366f1')
  },
  {
    label: '今日体质测试',
    value: constitutionTestStats.value.todayCount,
    yesterdayValue: constitutionTestStats.value.yesterdayCount,
    icon: 'fa-stethoscope',
    trend: getTrend(constitutionTestStats.value.todayCount, constitutionTestStats.value.yesterdayCount),
    colorClass: 'text-rose-600',
    bgClass: 'bg-rose-50 dark:bg-rose-900/30',
    chartOption: getRealSparkline(constitutionTestStats.value.todayCount, constitutionTestStats.value.yesterdayCount, '#f43f5e')
  },
  {
    label: '今日活跃用户',
    value: dashboardData.value.todayActiveUsers,
    yesterdayValue: yesterdayActiveUsers.value,
    icon: 'fa-bolt',
    trend: getTrend(dashboardData.value.todayActiveUsers, yesterdayActiveUsers.value),
    colorClass: 'text-amber-600',
    bgClass: 'bg-amber-50 dark:bg-amber-900/30',
    chartOption: getRealSparkline(dashboardData.value.todayActiveUsers, yesterdayActiveUsers.value, '#f59e0b')
  },
  {
    label: '今日打卡人数',
    value: checkinStats.value.todayCount,
    yesterdayValue: checkinStats.value.yesterdayCount,
    icon: 'fa-calendar-check',
    trend: getTrend(checkinStats.value.todayCount, checkinStats.value.yesterdayCount),
    colorClass: 'text-emerald-600',
    bgClass: 'bg-emerald-50 dark:bg-emerald-900/30',
    chartOption: getRealSparkline(checkinStats.value.todayCount, checkinStats.value.yesterdayCount, '#10b981')
  }
])

// 业务今日概览柱状图
// 业务今日概览柱状图/折线图
const mainTrendOption = computed(() => ({
  title: {
    text: '当季食养推荐：点击-收藏转化分析 (近7日)',
    left: 'center',
    textStyle: {
      color: '#374151',
      fontSize: 16,
      fontWeight: 'bold'
    }
  },
  tooltip: { 
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    formatter: function(params: any) {
      let result = `<div class="font-bold mb-1">${params[0].name}</div>`;
      params.forEach((item: any) => {
        result += `<div class="flex items-center justify-between gap-4">
                    <span>${item.marker}${item.seriesName}</span>
                    <span class="font-mono font-bold">${item.value}次</span>
                   </div>`;
      });
      
      // 动态计算该日期的转化率
      const clickItem = params.find((p: any) => p.seriesName === '点击查看详情');
      const favItem = params.find((p: any) => p.seriesName === '收藏菜品');
      if (clickItem && favItem && clickItem.value > 0) {
        const rate = ((favItem.value / clickItem.value) * 100).toFixed(1);
        result += `<div class="mt-2 pt-2 border-t border-gray-200 text-indigo-600 font-bold">
                    当日转化率: ${rate}%
                   </div>`;
      }
      return result;
    }
  },
  legend: {
    data: ['点击查看详情', '收藏菜品'],
    top: '10%',
    textStyle: { color: '#374151' }
  },
  grid: { 
    left: '3%', 
    right: '4%', 
    top: '22%', 
    bottom: '5%', 
    containLabel: true 
  },
  xAxis: { 
    type: 'category', 
    // 使用后端返回的真实日期序列
    data: dashboardData.value.dates && dashboardData.value.dates.length > 0 
          ? dashboardData.value.dates 
          : ['-', '-', '-', '-', '-', '-', '-'], 
    axisTick: { show: false },
    axisLine: { lineStyle: { color: '#e5e7eb' } },
    axisLabel: { color: '#6b7280', fontSize: 11 }
  },
  yAxis: { 
    type: 'value', 
    name: '次数',
    nameTextStyle: { color: '#9ca3af', padding: [0, 0, 0, -30] },
    splitLine: { lineStyle: { type: 'dashed', color: '#f3f4f6' } },
    axisLabel: { color: '#6b7280' }
  },
  series: [
    {
      name: '点击查看详情',
      type: 'line',
      smooth: true,
      showSymbol: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 4, color: '#6366f1', shadowColor: 'rgba(99, 102, 241, 0.2)', shadowBlur: 10 },
      itemStyle: { color: '#6366f1', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(99, 102, 241, 0.2)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0)' }
          ]
        }
      },
      // 绑定后端返回的点击量数组
      data: dashboardData.value.recommendationClicks || [0, 0, 0, 0, 0, 0, 0]
    },
    {
      name: '收藏菜品',
      type: 'line',
      smooth: true,
      showSymbol: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { width: 4, color: '#f43f5e', shadowColor: 'rgba(244, 63, 94, 0.2)', shadowBlur: 10 },
      itemStyle: { color: '#f43f5e', borderWidth: 2, borderColor: '#fff' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(244, 63, 94, 0.2)' },
            { offset: 1, color: 'rgba(244, 63, 94, 0)' }
          ]
        }
      },
      // 绑定后端返回的收藏量数组
      data: dashboardData.value.dishFavorites || [0, 0, 0, 0, 0, 0, 0]
    }
  ]
}))

// 资源占比真实饼图
const contentPieOption = computed(() => {
  const bookCategories = dashboardData.value.bookCategories || {}
  const categoryData = Object.entries(bookCategories).map(([name, count], index) => ({
    value: count as number,
    name: name === '未知' ? '未分类' : name,
    itemStyle: { 
       color: ['#6366f1', '#8b5cf6', '#a855f7', '#c084fc', '#d946ef', '#f59e0b', '#10b981'][index] || '#94a3b8' 
    }
  }))

  return {
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { 
      bottom: '0', 
      icon: 'circle', 
      type: 'scroll', 
      textStyle: { color: '#374151', fontSize: 12 }
    },
    series: [{
      name: '古籍种类',
      type: 'pie',
      radius: ['40%', '50%'], 
      center: ['50%', '45%'], 
      avoidLabelOverlap: true,
      itemStyle: { 
        borderRadius: 8, 
        borderColor: '#fff', 
        borderWidth: 2 
      },
      label: { 
        show: true,
        position: 'outside',
        formatter: '{b}\n{d}%', 
        fontSize: 11,
        fontWeight: 'bold',
        color: '#4b5563',
        // 禁用自动截断
        overflow: 'none', 
        // 缩短文字与引导线的距离
        distanceToLabelLine: 2 
      },
      // 缩短引导线长度，防止文字被推向边缘
      labelLine: {
        length: 8,    // 第一段线
        length2: 10,   // 第二段线
        smooth: true
      },
      // 强制布局调整，防止标签重叠或被切断
      labelLayout: {
        hideOverlap: false,
        moveOverlap: 'shiftY'
      },
      data: categoryData
    }]
  }
})

// 底部快捷链接
const quickLinks = [
  { name: '数据大屏', icon: 'fa-chart-pie', to: '/admin/dashboard' },
  { name: '用户管理', icon: 'fa-user-shield', to: '/admin/users' },
  { name: '古籍录入', icon: 'fa-book-medical', to: '/admin/books' },
  { name: '菜谱管理', icon: 'fa-utensils', to: '/admin/dishes' },
  { name: '论坛审核', icon: 'fa-comments', to: '/admin/forum' },
  { name: '消息推送', icon: 'fa-paper-plane', to: '/admin/messages' }
]

const refreshData = () => loadDashboardData()

onMounted(() => loadDashboardData())
</script>

<style scoped>
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-20px); }
</style>