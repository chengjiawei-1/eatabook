<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-slate-900">
    <!-- 导航 -->
    <Nav />

    <main class="flex-grow pt-24 pb-16">
      <div class="container mx-auto px-4">
        <!-- 标题区域 -->
        <div
          v-motion="{ initial: { opacity: 0, y: -20 }, enter: { opacity: 1, y: 0 } }"
          class="mb-12 text-center"
        >
          <h1
            class="text-3xl md:text-4xl font-bold text-amber-800 dark:text-amber-300 mb-4"
            style="font-family: 'Noto Serif SC', serif"
          >
            食疗方库
          </h1>
          <p class="text-amber-600 dark:text-amber-400 max-w-2xl mx-auto">
            探索古籍中的食疗方，根据体质选择适合自己的健康饮食方案
          </p>
        </div>

        <!-- 搜索与筛选 -->
        <div class="mb-10">
          <div class="relative mb-6">
            <input
              v-model="searchTerm"
              type="text"
              placeholder="搜索食疗方名称、功效或食材..."
              @keyup.enter="handleSearch"
              class="w-full px-4 py-3 pl-10 bg-amber-50 dark:bg-slate-800 rounded-xl border border-amber-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-amber-400 dark:focus:ring-amber-600 transition-colors"
            />
            <i class="fa-solid fa-search absolute left-3 top-1/2 -translate-y-1/2 text-amber-500 dark:text-amber-400"></i>
          </div>

          <div class="space-y-4">
            <!-- 类型筛选 -->
            <div>
              <h3 class="text-sm font-medium text-amber-700 dark:text-amber-300 mb-2">按类型筛选</h3>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="category in categories"
                  :key="category"
                  @click="handleCategoryChange(category)"
                  class="px-4 py-2 rounded-full text-sm font-medium transition-colors"
                  :class="selectedCategory === category
                    ? 'bg-amber-600 text-white'
                    : 'bg-amber-100 dark:bg-slate-800 text-amber-800 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-slate-700'"
                >
                  {{ category }}
                </button>
              </div>
            </div>

            <!-- 季节筛选 -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h3 class="text-sm font-medium text-amber-700 dark:text-amber-300 mb-2">按季节筛选</h3>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="season in seasons"
                    :key="season"
                    @click="handleSeasonChange(season)"
                    class="px-4 py-2 rounded-full text-sm font-medium transition-colors"
                    :class="selectedSeason === season
                      ? 'bg-amber-600 text-white'
                      : 'bg-amber-100 dark:bg-slate-800 text-amber-800 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-slate-700'"
                  >
                    {{ season }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 食疗方列表 -->
        <section>
          <div class="flex justify-between items-center mb-6">
            <h2 class="text-2xl font-bold text-amber-800 dark:text-amber-300" style="font-family: 'Noto Serif SC', serif">
              {{ isDefaultFilter ? "全部食疗方" : "搜索结果" }}
            </h2>
            <span class="text-sm text-amber-600 dark:text-amber-400">共找到 {{ filteredRecipes.length }} 个食疗方</span>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="text-center py-12 bg-amber-50 dark:bg-slate-800 rounded-xl">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-400 mb-4"></i>
            <p class="text-amber-700 dark:text-amber-300">正在加载食疗方数据...</p>
          </div>

          <!-- 无数据 -->
          <div v-else-if="filteredRecipes.length === 0" class="text-center py-12 bg-amber-50 dark:bg-slate-800 rounded-xl">
            <i class="fa-solid fa-search text-4xl text-amber-400 mb-4"></i>
            <p class="text-amber-700 dark:text-amber-300">未找到匹配的食疗方</p>
            <button @click="resetFilters" class="mt-4 px-4 py-2 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors">
              重置筛选条件
            </button>
          </div>

          <!-- 食疗方卡片列表 -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div
              v-for="(recipe, index) in filteredRecipes"
              :key="recipe.id"
              v-motion="{
                initial: { opacity: 0, y: 22, scale: 0.98 },
                enter: { opacity: 1, y: 0, scale: 1, transition: { delay: index * 0.06, type: 'spring', stiffness: 170, damping: 18 } }
              }"
              class="bg-white dark:bg-slate-800 rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-all duration-300 border border-amber-100 dark:border-slate-700 cursor-pointer"
              @click="openDetail(recipe)"
            >
              <div class="relative h-48 overflow-hidden">
                <img :src="recipe.image" :alt="recipe.name" class="w-full h-full object-cover transition-transform duration-500 hover:scale-110" />
                <div class="absolute top-3 left-3 bg-amber-600/80 text-white text-xs px-2 py-1 rounded-full">{{ recipe.source }}</div>
                <!-- 收藏按钮 -->
                <button
                  @click="toggleCollection(recipe.id, $event)"
                  class="absolute top-3 right-3 w-8 h-8 rounded-full flex items-center justify-center transition-all duration-300"
                  :class="isCollected(recipe.id) 
                    ? 'bg-red-500 text-white hover:bg-red-600' 
                    : 'bg-white/80 text-gray-600 hover:bg-white hover:text-red-500'"
                >
                  <i :class="isCollected(recipe.id) ? 'fa-solid fa-heart' : 'fa-regular fa-heart'"></i>
                </button>
              </div>
              <div class="p-5">
                <h3 class="text-lg font-semibold text-amber-800 dark:text-amber-200 mb-2" style="font-family: 'Noto Serif SC', serif">{{ recipe.name }}</h3>
                <p class="text-amber-600 dark:text-amber-400 text-sm mb-4 line-clamp-2">{{ recipe.description }}</p>
                <div class="flex flex-wrap gap-2 mb-4">
                  <span
                    v-for="(tag, tagIndex) in recipe.tags.slice(0, 3)"
                    :key="tagIndex"
                    class="bg-amber-100 dark:bg-slate-700 text-amber-700 dark:text-amber-300 text-xs px-2 py-1 rounded-full"
                  >
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页控件 -->
          <div class="flex justify-center items-center gap-2 mt-6">
            <button
              @click="goToPage(pageNo - 1)"
              :disabled="pageNo === 1"
              class="px-3 py-1 rounded bg-amber-100 dark:bg-slate-800 text-amber-700 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-slate-700 disabled:opacity-50"
            >
              上一页
            </button>
            <span class="px-2 text-sm text-amber-600 dark:text-amber-400">
              第 {{ pageNo }} 页 / 共 {{ totalPages }} 页
            </span>
            <button
              @click="goToPage(pageNo + 1)"
              :disabled="pageNo === totalPages"
              class="px-3 py-1 rounded bg-amber-100 dark:bg-slate-800 text-amber-700 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-slate-700 disabled:opacity-50"
            >
              下一页
            </button>
          </div>
        </section>
      </div>
    </main>

<transition name="fade">
  <div
    v-if="showDetail && selectedRecipe"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm"
    @click="closeDetail"
  >
    <div
      class="bg-white dark:bg-slate-900 rounded-xl overflow-hidden shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
      @click.stop
    >
      <!-- 头部 -->
      <div class="sticky top-0 z-10 bg-white dark:bg-slate-900 border-b border-amber-100 dark:border-slate-700 px-6 py-4 flex justify-between items-center">
        <h2 class="text-xl font-bold text-amber-800 dark:text-amber-300" style="font-family: 'Noto Serif SC', serif">
          {{ selectedRecipe.name }}
        </h2>
        <div class="flex items-center gap-2">
          <!-- 收藏按钮 -->
          <button
            @click="toggleCollection(selectedRecipe.id, $event)"
            class="p-2 rounded-full transition-colors"
            :class="isCollected(selectedRecipe.id) 
              ? 'text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20' 
              : 'text-gray-400 hover:text-red-500 hover:bg-amber-100 dark:hover:bg-slate-800'"
          >
            <i :class="isCollected(selectedRecipe.id) ? 'fa-solid fa-heart text-xl' : 'fa-regular fa-heart text-xl'"></i>
          </button>
          <button
            @click="closeDetail"
            class="p-2 rounded-full hover:bg-amber-100 dark:hover:bg-slate-800 transition-colors"
            aria-label="Close"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      </div>

      <div class="p-6 space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 图片 -->
          <div class="rounded-xl overflow-hidden shadow-md">
            <img 
              :src="selectedRecipe.image" 
              :alt="selectedRecipe.name" 
              class="w-full max-h-[300px] object-cover rounded-lg"
            />
          </div>
          <!-- 信息 -->
          <div class="space-y-3">
            <div class="flex items-center gap-2 text-sm text-amber-700 dark:text-amber-300">
              <i class="fa-solid fa-book"></i>
              <span>来源：{{ selectedRecipe.source }}</span>
            </div>

            <!-- 功效标签 -->
            <div class="flex flex-wrap gap-2">
              <span
                v-for="(tag, idx) in selectedRecipe.tags"
                :key="idx"
                class="bg-amber-50 dark:bg-slate-800 text-amber-700 dark:text-amber-300 border border-amber-100 dark:border-slate-700 px-3 py-1 rounded-full text-xs"
              >
                {{ tag }}
              </span>
            </div>

            <!-- 适宜体质 -->
            <div class="bg-green-50 dark:bg-slate-800 p-3 rounded-lg border border-green-200 dark:border-green-700">
              <h3 class="text-sm font-semibold text-green-700 dark:text-green-300 mb-1">适宜体质</h3>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="(s, idx) in selectedRecipe.suitableFor"
                  :key="idx"
                  class="bg-green-100 dark:bg-green-800 text-green-700 dark:text-green-200 px-2 py-1 rounded-full text-xs"
                >
                  {{ s }}
                </span>
              </div>
            </div>

            <!-- 类型 -->
            <div class="text-sm text-amber-700 dark:text-amber-300">
              类型：{{ selectedRecipe.categories.join("、") }}
            </div>

            <!-- 季节 -->
            <div class="text-sm text-amber-700 dark:text-amber-300">
              季节：<span v-if="selectedRecipe.seasons">{{ selectedRecipe.seasons.join("、") }}</span>
            </div>
          </div>
        </div>

        <!-- 食材 / 制作方法 -->
        <div class="bg-amber-50 dark:bg-slate-800 rounded-xl p-5 border border-amber-100 dark:border-slate-700">
          <h3 class="text-lg font-semibold text-amber-800 dark:text-amber-300 mb-3">配料与制作</h3>
          <p class="text-gray-700 dark:text-gray-300 leading-relaxed whitespace-pre-line">
            {{ selectedRecipe.ancientCitation }}
          </p>
        </div>

        <!-- 底部按钮 -->
        <div class="flex justify-end gap-2">
          <RouterLink
            :to="`/dishes/${selectedRecipe.id}`"
            class="inline-flex items-center gap-2 text-amber-600 dark:text-amber-400 hover:text-amber-800 dark:hover:text-amber-300 text-sm font-medium transition-colors"
          >
            查看菜品页
            <i class="fa-solid fa-arrow-right text-xs"></i>
          </RouterLink>

          <button
            class="px-4 py-2 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors"
            @click="closeDetail"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </div>
</transition>

    <!-- 页脚 -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import { searchDishesByPage, Dish, PageInfo } from "@/api/dishes";
import { collectionApi } from "@/api/collection";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();

// 收藏状态
const collectedIds = ref<Set<number>>(new Set());

// 加载用户收藏
async function loadUserCollections() {
  if (!auth.isAuthenticated || !auth.user?.userId) return;
  try {
    const res = await collectionApi.getUserCollectionsByType(auth.user.userId, 'dish');
    if (res.code === 200 && res.data) {
      collectedIds.value = new Set(res.data.map(item => item.targetId));
    }
  } catch (e) {
    console.error('加载收藏失败:', e);
  }
}

// 切换收藏
async function toggleCollection(recipeId: string, event: Event) {
  event.stopPropagation();
  
  if (!auth.isAuthenticated) {
    router.push('/login');
    return;
  }
  
  const dishId = parseInt(recipeId);
  try {
    const res = await collectionApi.toggleCollection(auth.user!.userId, 'dish', dishId);
    if (res.success) {
      if (res.isCollected) {
        collectedIds.value.add(dishId);
      } else {
        collectedIds.value.delete(dishId);
      }
      // 触发响应式更新
      collectedIds.value = new Set(collectedIds.value);
    }
  } catch (e) {
    console.error('收藏操作失败:', e);
  }
}

// 检查是否已收藏
function isCollected(recipeId: string): boolean {
  return collectedIds.value.has(parseInt(recipeId));
}

// 数据类型
type Recipe = {
  id: string;
  name: string;
  image: string;
  description: string;
  source: string;
  ingredients: string[];
  cookingMethod: string;
  efficacy: string;
  suitableFor: string[];
  preparationTime: number;
  difficulty: string;
  calories: number;
  categories: string[];
  tags: string[];
  seasons?: string[];         
  ancientCitation?: string;   
};

// 分页状态
const pageNo = ref(1);
const pageSize = ref(6);
const total = ref(0);
const recipes = ref<Recipe[]>([]);
const loading = ref(false);

// 筛选条件
const seasons = ["全部", "春季", "夏季", "秋季", "冬季"];
const categories = ["全部", "汤类", "粥类", "糊类", "羹类", "主食面点类", "主菜类", "小菜类", "蔬菜类", "点心类", "茶水类", "其他"];
const searchTerm = ref("");
const submittedSearchTerm = ref("");
const selectedCategory = ref("全部");
const selectedSeason = ref("全部");
const selectedEfficacy = ref("全部");

// 计算总页数
const totalPages = computed(() => Math.ceil(total.value / pageSize.value));

//工具方法
const normalizeCover = (url?: string) => {
  if (!url) return "";
  if (/^https?:\/\//.test(url)) return url;
  if (url.startsWith("/")) url = url;
  else url = "/" + url;

  // 补上 .png
  if (!url.endsWith(".png") && !url.endsWith(".jpg") && !url.endsWith(".jpeg")) {
    url += ".png";
  }
  return url;
};

// 获取数据
async function fetchRecipes() {
  loading.value = true;
  try {
    const res: PageInfo<Dish> | null = await searchDishesByPage({
      keyword: submittedSearchTerm.value || undefined,
      kind: selectedCategory.value === "全部" ? undefined : selectedCategory.value,
      season: selectedSeason.value === "全部" ? undefined : selectedSeason.value,
      pageNo: pageNo.value,
      pageSize: pageSize.value,
    });

    if (!res) {
      recipes.value = [];
      total.value = 0;
      return;
    }

    recipes.value = res.list.map(dish => {
        const effectTags: string[] = [
        dish.effectTag1,
        dish.effectTag2,
        dish.effectTag3,
        dish.effectTag4,
        dish.effectTag5
      ].filter(Boolean) as string[];
        // 处理季节
        const seasons: string[] = [];
        if (dish.seasonSpring) seasons.push("春季");
        if (dish.seasonSummer) seasons.push("夏季");
        if (dish.seasonAutumn) seasons.push("秋季");
        if (dish.seasonWinter) seasons.push("冬季");
      return {
        id: dish.dishId.toString(),
        name: dish.dishName,
        image: normalizeCover(dish.dishImgUrl) || "",
        description: `源自《${dish.dishSource}》的经典食疗方，${effectTags.join("、")}`,
        source: dish.dishSource,
        ingredients: dish.suitableConstitution ? dish.suitableConstitution.split(",") : [],
        cookingMethod: "",
        efficacy: effectTags.join("、"),
        suitableFor: dish.suitableConstitution ? dish.suitableConstitution.split(",") : [],
        preparationTime: 30,
        difficulty: "简单",
        calories: 200,
        categories: dish.kind ? [dish.kind] : ["其他"],
        tags: effectTags,
        seasons,                  
        ancientCitation: dish.ancientCitation, 
      };
    });
    total.value = res.total;
  } catch (err) {
    console.error(err);
    recipes.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 计算属性
const filteredRecipes = computed(() => {
  if (!submittedSearchTerm.value) return recipes.value;
  return recipes.value.filter(r =>
    r.name.includes(submittedSearchTerm.value) ||
    r.description.includes(submittedSearchTerm.value) ||
    r.efficacy.includes(submittedSearchTerm.value)
  );
});
const isDefaultFilter = computed(() => 
    selectedCategory.value === "全部" && 
    selectedSeason.value === "全部" && 
    selectedEfficacy.value === "全部" && 
    submittedSearchTerm.value === "");

// 筛选操作
function handleCategoryChange(category: string) {
  selectedCategory.value = category;
  pageNo.value = 1;
  fetchRecipes();
}
function handleSeasonChange(season: string) {
  selectedSeason.value = season;
  pageNo.value = 1;
  fetchRecipes();
}
function handleSearch() {
  submittedSearchTerm.value = searchTerm.value;
  pageNo.value = 1;
  fetchRecipes();
}

function resetFilters() {
  searchTerm.value = "";
  selectedCategory.value = "全部";
  selectedSeason.value = "全部";
  selectedEfficacy.value = "全部";
  pageNo.value = 1;
  fetchRecipes();
}

// 分页操作
function goToPage(page: number) {
  if (page < 1 || page > totalPages.value) return;
  pageNo.value = page;
  fetchRecipes();
}

// 详情弹窗
const showDetail = ref(false);
const selectedRecipe = ref<Recipe | null>(null);
const router = useRouter();
function openDetail(recipe: Recipe) {
  selectedRecipe.value = recipe;
  showDetail.value = true;
}
function closeDetail() {
  selectedRecipe.value = null;
  showDetail.value = false;
}

// 页面加载
onMounted(() => {
  fetchRecipes();
  loadUserCollections();
});

</script>
