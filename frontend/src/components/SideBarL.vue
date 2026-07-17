<template>
  <div class="w-full p-5 space-y-8">
    <!-- 社区概览卡片 -->
    <section class="relative group">
      <div class="mb-4 px-1 flex items-center justify-between">
        <h3 class="flex items-center gap-2 text-sm font-bold text-slate-800 dark:text-slate-200">
          <span class="flex items-center justify-center w-6 h-6 rounded-lg bg-amber-100 dark:bg-amber-900/50 text-amber-600">
            <i class="fa-solid fa-seedling text-[10px]"></i>
          </span>
          社群雅集
        </h3>
        <div class="flex items-center gap-1.5 px-2 py-0.5 rounded-full bg-green-50 dark:bg-green-900/20 border border-green-100 dark:border-green-800/30">
          <span class="relative flex h-1.5 w-1.5">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-1.5 w-1.5 bg-green-500"></span>
          </span>
        </div>
      </div>

      <!-- 统计网格 -->
      <div class="grid grid-cols-3 gap-3">
        <div class="stat-card group/card">
          <div class="stat-value text-amber-600">{{ totalPosts }}</div>
          <div class="stat-label">心得</div>
          <div class="stat-bg-icon"><i class="fa-solid fa-feather"></i></div>
        </div>
        <div class="stat-card group/card">
          <div class="stat-value text-orange-600">{{ totalUsers }}</div>
          <div class="stat-label">同道</div>
          <div class="stat-bg-icon"><i class="fa-solid fa-users"></i></div>
        </div>
        <div class="stat-card group/card">
          <div class="stat-value text-emerald-600">{{ totalCategories }}</div>
          <div class="stat-label">宗门</div>
          <div class="stat-bg-icon"><i class="fa-solid fa-leaf"></i></div>
        </div>
      </div>
    </section>

    <!-- 分割线 -->
    <div class="h-px bg-gradient-to-r from-transparent via-amber-200/50 dark:via-slate-700 to-transparent"></div>

    <!-- 板块分类 -->
    <section>
      <div class="mb-4 px-1">
        <h3 class="flex items-center gap-2 text-sm font-bold text-slate-800 dark:text-slate-200">
          食养板块
        </h3>
      </div>

      <div class="space-y-1.5">
        <button
          v-for="c in categories"
          :key="c.categoryId"
          @click="handleCategoryClick(c)"
          :class="[
            'w-full flex items-center justify-between px-4 py-3 rounded-2xl transition-all duration-300 group relative overflow-hidden',
            activeCategoryId === c.categoryId 
              ? 'bg-amber-500 text-white shadow-lg shadow-amber-500/30' 
              : 'hover:bg-white dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400 hover:shadow-sm'
          ]"
        >
          <div class="flex items-center gap-3 z-10">
            <span class="font-medium text-sm">{{ c.categoryName }}</span>
          </div>
          
          <div class="flex items-center gap-2 z-10">
            <span :class="[
              'text-[10px] px-2 py-0.5 rounded-full font-bold',
              activeCategoryId === c.categoryId ? 'bg-white/20 text-white' : 'bg-slate-100 dark:bg-slate-700 text-slate-500'
            ]">
              {{ c.count }}
            </span>
            <i class="fa-solid fa-chevron-right text-[10px] opacity-0 group-hover:opacity-100 transition-all translate-x-[-4px] group-hover:translate-x-0"></i>
          </div>

          <!-- 激活状态下的背景光晕 -->
          <div v-if="activeCategoryId === c.categoryId" class="absolute inset-0 bg-gradient-to-r from-amber-600 to-orange-500 -z-0"></div>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getForumCategories } from '@/api/forums'
import { getSidebarData, getCategoryCounts } from '@/api/sidebar'

interface Category {
  categoryId: number
  categoryName: string
  icon: string
  count: number
}

const emit = defineEmits(['category-change'])

const categories = ref<Category[]>([])
const activeCategoryId = ref<number | null>(null)
const totalPosts = ref<number>(0)
const totalUsers = ref<number>(0)
const totalCategories = ref<number>(0)

const loadSidebarData = async () => {
  try {
    const [sidebarStats, categoryCountsData, categoriesData] = await Promise.all([
      getSidebarData(),
      getCategoryCounts(),
      getForumCategories()
    ])
    
    totalPosts.value = sidebarStats.totalPosts || 0
    totalUsers.value = sidebarStats.totalUsers || 0
    totalCategories.value = sidebarStats.totalCategories || 0
    
    const categoriesArray = Array.isArray(categoriesData) ? categoriesData : (categoriesData.data || [])
    const categoryCountsMap = new Map((categoryCountsData as any[]).map(item => [item.category_id, item.post_count]))
    
    categories.value = categoriesArray.map((cat: any) => ({
      categoryId: cat.categoryId,
      categoryName: cat.categoryName,
      icon: cat.icon || 'fa-leaf',
      count: categoryCountsMap.get(cat.categoryId) || 0
    }))
  } catch (error) {
    console.error('加载侧边栏失败:', error)
  }
}

onMounted(loadSidebarData)

const handleCategoryClick = (item: Category) => {
  console.log('点击分类:', item); // 添加调试
  
  // 如果点击的是当前已选中的分类，则取消选中
  if (activeCategoryId.value === item.categoryId) {
    activeCategoryId.value = null;
    emit('category-change', null);
  } else {
    // 否则选中新的分类
    activeCategoryId.value = item.categoryId;
    emit('category-change', item);
  }
}
</script>

<style scoped>
.stat-card {
  @apply relative overflow-hidden flex flex-col items-center justify-center p-3 rounded-2xl 
         bg-white/50 dark:bg-slate-800/50 border border-white dark:border-slate-700 
         transition-all duration-300 hover:shadow-lg hover:shadow-amber-500/5 hover:-translate-y-1;
}

.stat-value {
  @apply text-lg font-black z-10;
}

.stat-label {
  @apply text-[10px] text-slate-500 dark:text-slate-400 font-medium z-10;
}

.stat-bg-icon {
  @apply absolute -bottom-1 -right-1 text-2xl opacity-[0.03] dark:opacity-[0.05] 
         transition-transform duration-500 group-hover/card:scale-125 group-hover/card:-rotate-12;
}

/* 隐藏侧边栏滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  display: none;
}
</style>