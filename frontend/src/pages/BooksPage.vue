<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-slate-900">
    <Nav />

    <main class="flex-grow pt-24 pb-16">
      <div class="container mx-auto px-4">
        <div
          v-motion="{
            initial: { opacity: 0, y: -20 },
            enter: { opacity: 1, y: 0 },
          }"
          class="mb-4 text-center"
        >
          <h1
            class="text-3xl md:text-4xl font-bold text-amber-800 dark:text-amber-300 mb-4"
            style="font-family: 'Noto Serif SC', serif"
          >
            古籍库
          </h1>
          <p class="text-amber-600 dark:text-amber-400 max-w-2xl mx-auto">
            探索中国传统医学与饮食文化的经典古籍，获取千年智慧的食养经验
          </p>
        </div>

        <div class="mb-4">
          <div class="flex flex-col md:flex-row gap-4 items-center">
            <div class="relative flex-grow">
              <input
                v-model="searchTerm"
                type="text"
                placeholder="搜索古籍名称、作者或内容..."
                class="w-full px-4 py-3 pl-10 bg-amber-50 dark:bg-slate-800 rounded-xl border border-amber-200 dark:border-slate-700 focus:outline-none focus:ring-2 focus:ring-amber-400 dark:focus:ring-amber-600 transition-colors"
              />
              <i class="fa-solid fa-search absolute left-3 top-1/2 -translate-y-1/2 text-amber-500 dark:text-amber-400"></i>
            </div>

            <div class="flex flex-wrap gap-2">
              <button
                v-for="category in categories"
                :key="category"
                @click="selectedCategory = category"
                class="px-4 py-2 rounded-full text-sm font-medium transition-colors"
                :class="
                  selectedCategory === category
                    ? 'bg-amber-600 text-white'
                    : 'bg-amber-100 dark:bg-slate-800 text-amber-800 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-slate-700'
                "
              >
                {{ category }}
              </button>
            </div>
          </div>
        </div>

        <!-- 排序选项 -->
        <div class="mb-6 flex justify-between items-center">
          <h2
            class="text-2xl font-bold text-amber-800 dark:text-amber-300"
            style="font-family: 'Noto Serif SC', serif"
          >
            {{ showFeatured ? "全部古籍" : "搜索结果" }}
          </h2>
          
          <div class="flex items-center gap-2">
            <span class="text-sm text-amber-600 dark:text-amber-400">排序方式：</span>
            <select
              v-model="sortBy"
              class="px-3 py-2 bg-amber-50 dark:bg-slate-800 rounded-lg border border-amber-200 dark:border-slate-700 text-amber-700 dark:text-amber-300 focus:outline-none focus:ring-2 focus:ring-amber-400 dark:focus:ring-amber-600 transition-colors"
            >
              <option value="default">默认排序</option>
              <option value="views-desc">访问量从高到低</option>
              <option value="views-asc">访问量从低到高</option>
              <option value="title">按书名排序</option>
            </select>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="text-center py-12 bg-amber-50 dark:bg-slate-800 rounded-xl">
          <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-400 mb-4"></i>
          <p class="text-amber-700 dark:text-amber-300">正在加载古籍数据...</p>
        </div>

        <div
          v-else-if="filteredBooks.length === 0"
          class="text-center py-12 bg-amber-50 dark:bg-slate-800 rounded-xl"
        >
          <i class="fa-solid fa-search text-4xl text-amber-400 mb-4"></i>
          <p class="text-amber-700 dark:text-amber-300">未找到匹配的古籍</p>
          <button
            @click="resetFilters"
            class="mt-4 px-4 py-2 bg-amber-600 text-white rounded-lg hover:bg-amber-700 transition-colors"
          >
            重置搜索
          </button>
        </div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="(book, index) in sortedAndFilteredBooks"
            :key="book.id"
            v-motion="{
              initial: { opacity: 0, y: 18, scale: 0.98 },
              enter: { opacity: 1, y: 0, scale: 1, transition: { delay: index * 0.05, type: 'spring', stiffness: 170, damping: 20 } },
            }"
            class="bg-white dark:bg-slate-800 rounded-xl overflow-hidden shadow-md hover:shadow-xl transition-all duration-300 border border-amber-100 dark:border-slate-700 flex relative"
          >
            <!-- 访问量显示在右上角 -->
            <div class="absolute top-2 right-2 bg-amber-500 text-white text-xs px-2 py-1 rounded-full z-10">
              {{ book.viewCount || 0 }} 访问
            </div>
            <div class="w-1/3">
              <img
                :src="bookCoverSrc(book)"
                :alt="book.title"
                class="w-full h-full object-cover"
                @error="useBookCoverFallback($event, book)"
              />
            </div>
            <div class="w-2/3 p-4">
              <h3
                class="text-lg font-semibold text-amber-800 dark:text-amber-200 mb-1"
                style="font-family: 'Noto Serif SC', serif"
              >
                {{ book.title }}
              </h3>
              <p class="text-sm text-amber-600 dark:text-amber-400 mb-2">{{ book.author }} · {{ book.era }}</p>
              <div class="flex flex-wrap gap-2 mb-3">
                <span class="text-xs bg-amber-100 dark:bg-slate-700 text-amber-700 dark:text-amber-300 px-2 py-1 rounded-full">
                  {{ book.category }}
                </span>
                <span class="text-xs bg-amber-100 dark:bg-slate-700 text-amber-700 dark:text-amber-300 px-2 py-1 rounded-full">
                  {{ book.entryCount }} 条目
                </span>
              </div>
              <p class="text-sm text-amber-700 dark:text-amber-300 mb-4 line-clamp-2">
                {{ book.description }}
              </p>
              <RouterLink
                :to="`/books/${book.id}`"
                class="inline-flex items-center gap-1 text-amber-600 dark:text-amber-400 hover:text-amber-800 dark:hover:text-amber-300 text-sm font-medium transition-colors"
              >
                查看详情
                <i class="fa-solid fa-arrow-right text-xs group-hover:translate-x-1 transition-transform"></i>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>
    </main>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from "vue";
import { RouterLink } from "vue-router";
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import { getAllBooks, getBooksByCategory, AncientBook } from "@/api/ancientBooks";

// 页面使用的书籍结构
interface Book {
  id: string;
  title: string;
  coverImage: string;
  description: string;
  entryCount: number;
  category: string;
  era: string;
  author: string;
  viewCount: number;
  status: string;
}

// 页面状态
const books = ref<Book[]>([]);
const loading = ref(false);
const searchTerm = ref("");
const selectedCategory = ref("全部");
const sortBy = ref("default"); // 排序方式

// 分类列表（直接用于请求后端）
const categories = ["全部", "菜品专著", "食材专著"];

// 页面加载时获取全部古籍
onMounted(async () => {
  loadAllBooks();
});

// 根据当前分类加载数据
watch(selectedCategory, async (val) => {
  if (val === "全部") {
    loadAllBooks();
  } else {
    loadBooksByCategory(val);
  }
});

// 获取全部古籍
const loadAllBooks = async () => {
  loading.value = true;
  const apiBooks = await getAllBooks();
  books.value = mapBooks(apiBooks);
  loading.value = false;
};

// 根据分类从后端获取古籍
const loadBooksByCategory = async (category: string) => {
  loading.value = true;
  const apiBooks = await getBooksByCategory(category);
  books.value = mapBooks(apiBooks);
  loading.value = false;
};

// 将后端数据转换为页面需要的格式
const mapBooks = (apiBooks: AncientBook[]): Book[] => {
  return apiBooks
    .filter(book => book.status === 1) // 只保留已发布的古籍
    .map((book) => ({
      id: book.bookId,
      title: book.bookName,
      coverImage: book.bookCoverUrl,
      description: book.intro,
      entryCount: book.nourishCount,
      category: book.bookCategory,
      era: book.dynasty,
      author: book.author,
      viewCount: book.viewCount || 0, // 添加访问量
      status: book.status === 1 ? 'published' : 'draft', // 添加状态字段
    }));
};

const normalizeAssetPath = (url?: string) => {
  if (!url) return "";
  if (/^https?:\/\//.test(url)) return url;
  return url.startsWith("/") ? url : `/${url}`;
};

const withCoverVersion = (url: string) => {
  if (!url) return "";
  const separator = url.includes("?") ? "&" : "?";
  return `${encodeURI(url)}${separator}v=book-cover-20260730`;
};

const bookCoverSrc = (book: Book) => {
  return withCoverVersion(normalizeAssetPath(book.coverImage || `/images/book/${book.title}.png`));
};

const useBookCoverFallback = (event: Event, book: Book) => {
  const img = event.target as HTMLImageElement;
  if (img.dataset.coverFallback === "1") {
    img.removeAttribute("src");
    return;
  }

  img.dataset.coverFallback = "1";
  img.src = withCoverVersion(`/images/book/${book.title}.png`);
};

// 前端搜索过滤
const filteredBooks = computed(() => {
  if (!searchTerm.value) return books.value;
  const keyword = searchTerm.value.trim();
  // 关键词前不能是汉字
  const reg = new RegExp(`(^|[^\\u4e00-\\u9fa5])${keyword}`);
  return books.value.filter((book) =>
    reg.test(book.title) ||
    reg.test(book.author) 
  );
});

// 排序后的书籍列表
const sortedAndFilteredBooks = computed(() => {
  let booksToSort = [...filteredBooks.value];
  
  switch (sortBy.value) {
    case "views-desc":
      // 访问量从高到低排序
      return booksToSort.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0));
    case "views-asc":
      // 访问量从低到高排序
      return booksToSort.sort((a, b) => (a.viewCount || 0) - (b.viewCount || 0));
    case "title":
      // 按书名排序
      return booksToSort.sort((a, b) => a.title.localeCompare(b.title, 'zh-CN'));
    case "default":
    default:
      // 默认排序
      return booksToSort;
  }
});

// 是否显示"全部古籍"
const showFeatured = computed(() => {
  return selectedCategory.value === "全部" && searchTerm.value === "";
});

// 重置搜索条件
const resetFilters = () => {
  searchTerm.value = "";
  selectedCategory.value = "全部";
  sortBy.value = "default";
};
</script>
