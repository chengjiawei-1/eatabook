<template>
  <section class="pt-24 pb-16 md:pt-32 md:pb-24 bg-gradient-to-b from-amber-50 to-amber-100 dark:from-slate-900 dark:to-slate-800">
    <div class="container mx-auto px-4">
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 items-center">
        <div class="relative">
          <div class="w-full h-[400px] md:h-[500px] flex items-center justify-center">
            <div class="absolute inset-0 flex items-center justify-center">
              <div class="w-[300px] h-[400px] rounded-lg bg-amber-200 dark:bg-slate-700 opacity-30 transform -rotate-6"></div>
              <div class="w-[300px] h-[400px] rounded-lg bg-amber-300 dark:bg-slate-600 opacity-20 transform rotate-4"></div>
            </div>

            <div
              v-motion="{
                initial: { opacity: 0, y: 30, scale: 0.95 },
                enter: { opacity: 1, y: 0, scale: 1, transition: { type: 'spring', stiffness: 160, damping: 18 } },
              }"
              :key="activeBookIndex"
              class="relative z-10 w-[280px] h-[400px] md:w-[320px] md:h-[480px] rounded-lg overflow-hidden shadow-xl transform transition-transform duration-500 hover:scale-105 hover:-rotate-2 cursor-pointer group"
            >
              <img :src="activeBook.coverImage" :alt="activeBook.title" class="w-full h-full object-cover" />
              <div
                class="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent flex flex-col justify-end p-6 text-white"
              >
                <h3 class="text-2xl font-bold mb-2">{{ activeBook.title }}</h3>
                <p class="text-sm mb-4 line-clamp-2 text-white/90 leading-relaxed">{{ truncateText(activeBook.description, 60) }}</p>
                <div class="flex justify-between items-center">
                  <span class="text-xs bg-white/20 px-2 py-1 rounded-full">
                    {{ activeBook.entryCount }} 条目
                  </span>
                  <RouterLink
                    :to="`/books/${activeBook.id}`"
                    class="text-xs bg-amber-600 hover:bg-amber-700 px-3 py-1 rounded-full transition-colors"
                  >
                    查看目录
                  </RouterLink>
                </div>
              </div>
              <div
                class="absolute bottom-0 right-0 w-20 h-20 bg-gradient-to-br from-white/40 to-transparent rounded-tl-full opacity-0 group-hover:opacity-100 transition-opacity duration-300"
              ></div>
            </div>
          </div>

            <button
            @click="prevBook"
              class="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-1/2 bg-amber-100 dark:bg-slate-800 hover:bg-amber-200 dark:hover:bg-slate-700 w-10 h-10 rounded-full flex items-center justify-center shadow-lg transition-all duration-200 hover:scale-110"
            aria-label="Previous book"
          >
            <i class="fa-solid fa-chevron-left text-amber-800 dark:text-amber-300"></i>
          </button>
            <button
            @click="nextBook"
              class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2 bg-amber-100 dark:bg-slate-800 hover:bg-amber-200 dark:hover:bg-slate-700 w-10 h-10 rounded-full flex items-center justify-center shadow-lg transition-all duration-200 hover:scale-110"
            aria-label="Next book"
          >
            <i class="fa-solid fa-chevron-right text-amber-800 dark:text-amber-300"></i>
          </button>

          <div class="flex justify-center gap-2 mt-6">
            <button
              v-for="(_, index) in books"
              :key="index"
              @click="setActive(index)"
              class="w-2 h-2 rounded-full transition-all"
              :class="index === activeBookIndex ? 'w-6 bg-amber-600 dark:bg-amber-400' : 'bg-amber-300 dark:bg-slate-600'"
              :aria-label="`Go to book ${index + 1}`"
            />
          </div>
        </div>

          <div class="relative group">
            <!-- 背景装饰光晕 - 增加氛围感 -->
            <div class="absolute -inset-1 bg-gradient-to-r from-amber-200 to-orange-100 rounded-2xl blur opacity-25 group-hover:opacity-50 transition duration-1000 group-hover:duration-200"></div>

            <div 
              v-motion="{ 
                initial: { opacity: 0, y: 20, scale: 0.95 }, 
                enter: { opacity: 1, y: 0, scale: 1, transition: { delay: 0.15, type: 'spring', stiffness: 100, damping: 20 } }, 
              }" 
              class="relative bg-white/80 dark:bg-slate-800/90 backdrop-blur-xl rounded-2xl shadow-2xl p-6 md:p-8 border border-amber-100/50 dark:border-slate-700/50 overflow-hidden"
            >
              <!-- 内部装饰元素 -->
              <div class="absolute top-0 right-0 -mt-8 -mr-8 w-32 h-32 bg-amber-100/30 dark:bg-amber-900/20 rounded-full blur-2xl"></div>
              <div class="absolute bottom-0 left-0 -mb-8 -ml-8 w-32 h-32 bg-orange-100/30 dark:bg-orange-900/20 rounded-full blur-2xl"></div>

              <div class="relative mb-8 text-center">
                <span class="inline-block px-3 py-1 mb-3 text-xs font-bold tracking-widest text-amber-600 uppercase bg-amber-50 dark:bg-amber-900/30 rounded-full">
                  Traditional Wisdom
                </span>
               <!-- 修改标题部分 -->
                <h2 class="text-3xl md:text-4xl font-serif-title font-extrabold text-amber-900 dark:text-amber-200 mb-4">
                  探索体质与食疗的奥秘
                </h2>
                <p class="font-sans-body text-amber-800/70 dark:text-amber-200/70 leading-relaxed">
                  通过传统中医体质测试，了解自己的特点，获取个性化的食养建议。
                </p>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <div
                  class="group/item flip-card flex p-4 bg-amber-50/50 dark:bg-slate-700/50 rounded-xl border border-transparent hover:border-amber-200 dark:hover:border-amber-700 hover:bg-white dark:hover:bg-slate-700 transition-all duration-300 hover:shadow-md hover:-translate-y-0.5"
                  >
                  <div class="flip-card-inner flex flex-col items-center text-center w-full">

                      <!-- 正面 -->
                      <div class="flip-card-front flex flex-col items-center w-full">
                        <div
                          class="mb-3 bg-amber-100 dark:bg-amber-900/50 p-3 rounded-xl text-amber-600 dark:text-amber-300 group-hover/item:scale-110 transition-transform duration-300"
                        >
                          <i class="fa-solid fa-book text-xl"></i>
                        </div>
                        <h3 class="text-base font-bold text-amber-900 dark:text-amber-100 mb-1">
                          古籍智慧
                        </h3>
                      </div>

                      <!-- 背面 -->
                      <div
                        class="flip-card-back flex items-center justify-center px-3"
                      >
                        <p class="text-base text-amber-700/70 dark:text-amber-300/70">
                          基于经典古籍的饮食养生智慧
                        </p>
                      </div>

                    </div>
                  </div>

                <div
                  class="group/item flip-card flex p-4 bg-amber-50/50 dark:bg-slate-700/50 rounded-xl border border-transparent hover:border-amber-200 dark:hover:border-amber-700 hover:bg-white dark:hover:bg-slate-700 transition-all duration-300 hover:shadow-md hover:-translate-y-0.5"
                >
                  <div class="flip-card-inner flex flex-col items-center text-center w-full">

                    <!-- 正面 -->
                    <div class="flip-card-front flex flex-col items-center w-full">
                      <div class="mb-3 bg-amber-100 dark:bg-amber-900/50 p-3 rounded-xl text-amber-600 dark:text-amber-300 group-hover/item:scale-110 transition-transform duration-300">
                        <i class="fa-solid fa-user-check text-xl"></i>
                      </div>
                      <h3 class="text-base font-bold text-amber-900 dark:text-amber-100 mb-1">
                        个性化建议
                      </h3>
                    </div>

                    <!-- 背面 -->
                    <div class="flip-card-back flex items-center justify-center px-3">
                      <p class="text-base text-amber-700/70 dark:text-amber-300/70">
                        根据个人体质特点定制专属食养方案
                      </p>
                    </div>

                  </div>
                </div>

                <div
                  class="group/item flip-card flex p-4 bg-amber-50/50 dark:bg-slate-700/50 rounded-xl border border-transparent hover:border-amber-200 dark:hover:border-amber-700 hover:bg-white dark:hover:bg-slate-700 transition-all duration-300 hover:shadow-md hover:-translate-y-0.5"
                >
                  <div class="flip-card-inner flex flex-col items-center text-center w-full">

                    <!-- 正面 -->
                    <div class="flip-card-front flex flex-col items-center w-full">
                      <div class="mb-3 bg-amber-100 dark:bg-amber-900/50 p-3 rounded-xl text-amber-600 dark:text-amber-300 group-hover/item:scale-110 transition-transform duration-300">
                        <i class="fa-solid fa-chart-simple text-xl"></i>
                      </div>
                      <h3 class="text-base font-bold text-amber-900 dark:text-amber-100 mb-1">
                        科学解读
                      </h3>
                    </div>

                    <!-- 背面 -->
                    <div class="flip-card-back flex items-center justify-center px-3">
                      <p class="text-base text-amber-700/70 dark:text-amber-300/70">
                        结合现代营养学的科学解读
                      </p>
                    </div>

                  </div>
                </div>

              </div>

              <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
                <RouterLink 
  to="/physique-test" 
  class="relative px-8 py-3 bg-gradient-to-r from-amber-600 to-amber-700
         text-white font-bold rounded-full text-sm
         shadow-[0_8px_16px_-8px_rgba(217,119,6,0.5)]
         hover:-translate-y-0.5 active:scale-95
         animate-breath-button overflow-visible"
>
  开始体质测试

  <!-- 光晕 -->
  <span class="absolute inset-0 rounded-full animate-halo"></span>
</RouterLink>
                <RouterLink 
                  to="/books" 
                  class="px-8 py-3 bg-white dark:bg-slate-700 hover:bg-amber-50 dark:hover:bg-slate-600 text-amber-800 dark:text-amber-300 font-bold rounded-full text-center border-2 border-amber-100 dark:border-slate-600 transition-all hover:border-amber-200 dark:hover:border-slate-500 shadow-sm hover:shadow-md active:scale-95 text-sm"
                >
                  浏览古籍
                </RouterLink>
              </div>
            </div>
          </div>

      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from "vue";
import { RouterLink } from "vue-router";
import { getAllBooks } from "@/api/ancientBooks";

//文本截断工具函数
const truncateText = (text: string, maxLength: number): string => {
  if (!text) return "";
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + "...";
};

//轮播书籍列表
const books = ref([
  {
    id: "0",
    title: "加载中...",
    coverImage: "",
    description: "正在加载古籍数据...",
    entryCount: 0
  }
]);

// 当前显示的书籍索引
const activeBookIndex = ref(0);

//当前正在展示的书籍
const activeBook = computed(() => {
  return books.value[activeBookIndex.value];
});

//切换到下一本书
const nextBook = () => {
  activeBookIndex.value =
    (activeBookIndex.value + 1) % books.value.length;
};

//切换到上一本书
const prevBook = () => {
  activeBookIndex.value =
    (activeBookIndex.value - 1 + books.value.length) % books.value.length;
};

//点击下方圆点，切换到指定书籍
const setActive = (index: number) => {
  activeBookIndex.value = index;
};

//页面加载完成后获取所有古籍数据
onMounted(async () => {
  try {
    const apiBooks = await getAllBooks();

    if (apiBooks && apiBooks.length > 0) {
      books.value = apiBooks.map(book => ({
        id: book.bookId,
        title: book.bookName,
        coverImage: book.bookCoverUrl ,
        description: book.intro ,
        entryCount: book.nourishCount
      }));
    }
  } catch (error) {
    console.error("轮播古籍数据加载失败：", error);
  }
});
</script>

<style scoped>
/* 简介最多显示两行，超出部分省略 */
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 3D 翻转基础 */
.flip-card {
  perspective: 1000px;
}

.flip-card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.4s cubic-bezier(0.455, 0.030, 0.515, 0.955);
  transform-style: preserve-3d;
}

/* hover 时翻转 */
.flip-card:hover .flip-card-inner {
  transform: rotateY(180deg);
}

.flip-card-front,
.flip-card-back {
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

/* 背面 */
.flip-card-back {
  position: absolute;
  inset: 0;
  transform: rotateY(180deg);
}
@keyframes breath {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
}

@keyframes halo {
  0% {
    opacity: 0.6;
    transform: scale(1);
  }
  100% {
    opacity: 0;
    transform: scale(1.5);
  }
}

.animate-breath-button {
  animation: breath 1.6s ease-in-out infinite;
}

.animate-halo {
  background: rgba(251, 191, 36, 0.35);
  filter: blur(20px);
  z-index: -1;
  animation: halo 1.6s ease-out infinite;
}

</style>