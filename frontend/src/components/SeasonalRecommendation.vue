<template>
  <section class="py-16 bg-white dark:bg-slate-900">
    <div class="container mx-auto px-4">
      <div class="mb-10">
        <div class="flex items-center justify-between mb-4">
          <h2
            class="text-2xl md:text-3xl font-bold text-amber-800 dark:text-amber-300"
            style="font-family: 'Noto Serif SC', serif"
          >
            个性化食养推荐
          </h2>

          <div class="flex items-center gap-3">
            <!-- 换一换按钮 -->
            <button
              @click="refreshDishes"
              class="flex items-center gap-1 text-sm text-amber-700 dark:text-amber-300 hover:text-amber-900 dark:hover:text-amber-200 transition-colors"
              title="换一批推荐菜品"
            >
              <i class="fa-solid fa-rotate-right"></i>
              换一换
            </button>

            <div ref="calendarWrapper" class="relative flex items-center gap-2">
              <button
                @click.stop="toggleCalendar"
                class="flex items-center gap-2 bg-amber-100 dark:bg-amber-900/30 px-3 py-1 rounded-full cursor-pointer hover:bg-amber-200 dark:hover:bg-amber-800 transition-colors"
                title="查看节气日历"
              >
                <i class="fa-solid fa-sun text-amber-600 dark:text-amber-400"></i>
                <span class="text-sm font-medium text-amber-700 dark:text-amber-300">{{ currentSolarTerm.name }}</span>
              </button>

              <!-- 日历弹窗 -->
              <div
                v-if="showCalendar"
                class="absolute top-full right-0 mt-2 w-56 bg-white dark:bg-slate-800 rounded-xl shadow-lg border border-amber-200 dark:border-slate-700 p-4 z-50"
              >
                <div class="text-center">
                  <p class="text-sm text-amber-600 dark:text-amber-400">{{ today.weekday }}</p>
                  <p class="text-2xl font-bold text-amber-800 dark:text-amber-300 my-1">{{ today.day }}</p>
                  <p class="text-sm text-amber-700 dark:text-amber-400">{{ today.year }} 年 {{ today.month }} 月</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <p class="text-amber-600 dark:text-amber-400 text-sm md:text-base max-w-3xl">
          {{ currentSolarTerm.description }}
        </p>
      </div>

      <!-- 展示推荐菜品 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div
          v-for="(dish, index) in displayedDishes"
          :key="dish.id"
          class="bg-amber-50 dark:bg-slate-800 rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-all duration-300 group relative"
        >
          <div class="relative h-48 overflow-hidden">
            <img :src="dish.coverImage" :alt="dish.name" class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
            <div class="absolute top-3 left-3 bg-amber-600/80 text-white text-xs px-2 py-1 rounded-full">
              {{ dish.sourceBook }}
            </div>
          </div>

          <div class="p-5">
            <h3
              class="text-lg font-semibold text-amber-800 dark:text-amber-200 mb-2"
              style="font-family: 'Noto Serif SC', serif"
            >
              {{ dish.name }}
            </h3>

          <!-- 标签和匹配度展示 -->
          <div class="flex flex-wrap gap-2 mb-4 items-center">
            <!-- 左侧标签 -->
            <span
              v-for="(tag, tagIndex) in dish.suitabilityTags"
              :key="tagIndex"
              class="bg-amber-100 dark:bg-slate-700 text-amber-700 dark:text-amber-300 text-xs px-2 py-1 rounded-full"
            >
              {{ tag }}
            </span>

            <!-- 右侧匹配度 -->
            <div class="ml-auto text-sm text-amber-700 dark:text-amber-300">
              <span class="font-medium">匹配度：</span>
              <span class="ml-1">{{ dish.matchScore }}</span>
            </div>
          </div>

            <div class="flex justify-end">
              <a
                href="javascript:void(0)"
                @click.prevent="handleClickView(dish.id)"
                class="inline-flex items-center gap-1 text-amber-600 ..."
              >
                查看详情
                <i class="fa-solid fa-arrow-right text-xs group-hover:translate-x-1 transition-transform"></i>
              </a>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-10 text-center">
        <RouterLink
          to="/recipes"
          class="inline-flex items-center gap-2 bg-amber-100 hover:bg-amber-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-amber-800 dark:text-amber-300 px-5 py-2 rounded-full transition-colors"
        >
          查看更多食养推荐
          <i class="fa-solid fa-chevron-right text-xs"></i>
        </RouterLink>
      </div>
    </div>
  </section>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { getRecommendedDishes } from '@/api/dishes' // 使用新接口
import { useAuthStore } from '@/stores/auth' // 假设使用 Pinia 管理用户状态
import { logClickView, logFavoriteDish } from '@/api/analytics';
import { RouterLink, useRouter } from 'vue-router'  // 添加useRouter

const router = useRouter();  
const userId = ref(0); 

// 显示日历弹窗状态
const showCalendar = ref(false)
// 日历区域引用
const calendarWrapper = ref<HTMLElement | null>(null)
// 推荐菜品数据
const recommendedDishes = ref<any[]>([])
const displayedDishes = ref<any[]>([])

// 今日日期
const today = computed(() => {
  const now = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']

  return {
    year: now.getFullYear(),
    month: now.getMonth() + 1,
    day: now.getDate(),
    weekday: weekdays[now.getDay()],
    time: now.toLocaleTimeString()
  }
})

// 季节描述
const solarTermInfo = {
  spring: {
    name: "春季",
    description: "春季是万物生发的季节，此时阳气渐升，宜疏肝解郁，调达气血。"
  },
  summer: {
    name: "夏季",
    description: "夏季是阳气最旺的时节，此时宜清热解暑，养心护阳，兼顾祛湿。"
  },
  autumn: {
    name: "秋季",
    description: "秋季是收敛肃降的季节，此时宜润燥养肺，滋阴补水，平衡气机。"
  },
  winter: {
    name: "冬季",
    description: "冬季是封藏养精的时节，此时宜温补肾阳，蓄养精气，以待来年。"
  }
}

// 关闭日历功能
const handleClickOutside = (e: MouseEvent) => {
  if (
    showCalendar.value &&
    calendarWrapper.value &&
    !calendarWrapper.value.contains(e.target as Node)
  ) {
    showCalendar.value = false
  }
}

// 日历显示/隐藏
const toggleCalendar = () => {
  showCalendar.value = !showCalendar.value
}

// 获取当前季节
const getSeason = () => {
  const month = new Date().getMonth() + 1
  if (month >= 3 && month <= 5) return 'spring'
  if (month >= 6 && month <= 8) return 'summer'
  if (month >= 9 && month <= 11) return 'autumn'
  return 'winter'
}

// 当前季节计算属性
const currentSolarTerm = computed(() => {
  return solarTermInfo[getSeason()]
})

// 加载推荐菜品
const loadRecommendedDishes = async () => {
  try {
    // 从状态管理中获取用户 Token
    const authStore = useAuthStore();
    const token = authStore.token;

    if (!token) {
      console.warn('用户未登录或 Token 不存在');
      return;
    }

    // 调用接口获取推荐菜品
    const response = await getRecommendedDishes(token);
    console.log('【推荐菜品接口返回】', response);

    // 从 response.data 中提取菜品列表
    const dishes = response?.data || []; // 假设后端返回的数据在 data 字段中

    // 映射后端字段 → 前端展示结构
    const allDishes = dishes.map((dish: any) => ({
      id: dish.dish_id,
      name: dish.dish_name,
      coverImage: dish.dish_img_url, // 默认图片占位符
      sourceBook: dish.kind,
      shortReason: dish.effect_tag1 || '应季养生推荐',
      suitabilityTags: [
        dish.effect_tag1,
        dish.effect_tag2,
        dish.effect_tag3,
        dish.effect_tag4,
        dish.effect_tag5
      ].filter(Boolean),
      matchScore: dish.match_score // 匹配度字段
    }));

    // 保存所有推荐菜品数据
    recommendedDishes.value = allDishes;

    // 只展示3个菜品
    refreshDishes();
  } catch (error) {
    console.error('加载推荐菜品失败:', error);
  }
}

// 刷新菜品展示
const refreshDishes = () => {
  if (recommendedDishes.value.length <= 3) {
    // 如果总数量小于等于3个，则显示所有
    displayedDishes.value = [...recommendedDishes.value]
  } else {
    // 随机选取3个不重复的菜品
    const shuffled = [...recommendedDishes.value].sort(() => 0.5 - Math.random())
    displayedDishes.value = shuffled.slice(0, 3)
  }
}

// 点击查看详情
const handleClickView = async (dishId: number) => {
  console.log("正在记录点击行为...", dishId, userId.value);
  try {
    const res = await logClickView(userId.value, dishId);
    
    // 打印出到底返回了什么，看看结构
    console.log("API 返回的原始结果:", res);

    // 适配不同的 Axios 拦截器返回结构
    const responseData = res?.data || res; 
    
    if (responseData && (responseData.success || responseData.code === 200)) {
      const sid = responseData.sessionId || responseData.data?.sessionId;
      if (sid) {
        localStorage.setItem(`click_session_${dishId}`, sid);
        console.log("✅ 成功存入 localStorage, Key:", `click_session_${dishId}`, "Value:", sid);
      } else {
        console.warn("⚠️ API 成功但未找到 sessionId");
      }
    }
  } catch (error) {
    console.error('❌ API请求失败:', error);
  } finally {
    // 无论如何都会执行跳转
    router.push({
      path: `/dishes/${dishId}`,
      query: { from: 'home' }
    });
  }
}

// 生命周期钩子
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  loadRecommendedDishes() // 调用新接口加载推荐菜品
  const authStore = useAuthStore();
  userId.value = authStore.user?.userId || 0;
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>