<template>
  <div class="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900 transition-colors duration-300">
    <!-- 导航栏 -->
    <Nav class="shadow-sm z-50" @publish="openPublishModal" />
   
    <main class="flex-grow pt-24 pb-16 container mx-0 sm:mx-2 px-2 sm:px-4 max-w-full">
      <!-- 三栏布局结构 -->
      <div class="flex flex-col lg:flex-row gap-2 lg:gap-1">
        <!-- 左侧侧边栏 -->
        <div 
          :class="[
            'fixed left-0 top-24 bottom-0 overflow-y-auto p-6 border-r border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 z-40 transition-all duration-300',
            sidebarLeftVisible ? 'translate-x-0 w-64' : '-translate-x-full w-0'
          ]"
        >
          <div :class="sidebarLeftVisible ? 'w-full' : 'w-0 overflow-hidden'">
            <SideBarL @category-change="handleCategoryChange" />
          </div>
          
          <!-- 添加收起按钮 -->
          <button 
            @click="toggleSidebarLeft"
            class="absolute top-4 -right-3 bg-slate-200 dark:bg-slate-700 rounded-r-lg p-1 shadow-md hover:bg-slate-300 dark:hover:bg-slate-600"
          >
            <i :class="sidebarLeftVisible ? 'fa-solid fa-chevron-left' : 'fa-solid fa-chevron-right'"></i>
          </button>
        </div>
        
        <!-- 中间内容区 - 搜索框和帖子列表 -->
        <div 
          :class="[
            'flex-1 order-3 lg:order-2 transition-all duration-300',
            sidebarLeftVisible && sidebarRightVisible ? 'lg:ml-64 lg:mr-64' : 
            sidebarLeftVisible ? 'lg:ml-64' : 
            sidebarRightVisible ? 'lg:mr-64' : 'lg:mx-0'
          ]"
        >
          <!-- 按钮导航 -->
          <div class="mb-4 flex flex-wrap justify-between items-center gap-2">
            <!-- 左侧标签页按钮组 -->
            <div class="flex gap-0.5">
              <button
                @click="setActiveTab('recommend')"
                :class="[
                  'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center',
                  activeTab === 'recommend'
                    ? 'bg-amber-500 text-white shadow-sm'
                    : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/50 hover:text-slate-800 dark:hover:text-slate-200'
                ]"
              >
                推荐
              </button>
              <button
                @click="setActiveTab('following')"
                :class="[
                  'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center',
                  activeTab === 'following'
                    ? 'bg-amber-500 text-white shadow-sm'
                    : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/50 hover:text-slate-800 dark:hover:text-slate-200'
                ]"
              >
                关注
              </button>
              <button
                @click="setActiveTab('my')"
                :class="[
                  'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 flex items-center justify-center',
                  activeTab === 'my'
                    ? 'bg-amber-500 text-white shadow-sm'
                    : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/50 hover:text-slate-800 dark:hover:text-slate-200'
                ]"
              >
                我的
              </button>
            </div>
            
            <!-- 右侧操作按钮组 -->
            <div class="flex gap-0.5">
              <!-- 搜索按钮 -->
              <button
                @click="toggleSearchBox"
                class="p-2 rounded-lg transition-all duration-200 flex items-center justify-center"
                :class="showSearchBox 
                  ? 'bg-amber-500 text-white' 
                  : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/50 hover:text-slate-800 dark:hover:text-slate-200'"
                :title="showSearchBox ? '隐藏搜索' : '显示搜索'"
              >
                <i :class="showSearchBox ? 'fa-solid fa-times' : 'fa-solid fa-search'"></i>
              </button>
              
              <button
                @click="toggleBothSidebars"
                class="p-2 rounded-lg transition-all duration-200 flex items-center justify-center"
                :class="bothSidebarsCollapsed 
                  ? 'bg-amber-500 text-white' 
                  : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/50 hover:text-slate-800 dark:hover:text-slate-200'"
                :title="bothSidebarsCollapsed ? '展开侧边栏' : '收起侧边栏'"
              >
                <i :class="bothSidebarsCollapsed ? 'fa-solid fa-expand' : 'fa-solid fa-compress'"></i>
              </button>
            </div>
          </div>
          
          <!-- 搜索框区域 - 条件渲染 -->
          <div v-if="showSearchBox" class="mb-4 lg:mb-6">
            <div class="relative flex-1">
              <input
                v-model="searchQuery"
                @keyup.enter="searchPosts"
                @focus="showHistory = true; loadSearchHistory()"
                @blur="hideHistoryWithDelay()"
                type="text"
                placeholder="搜索帖子标或内容，搜索用户请以 @ 开头"
                class="w-full px-4 py-3 pl-12 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-200 shadow-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-transparent"
                />
              <div class="absolute left-4 top-1/2 transform -translate-y-1/2 text-slate-400">
                <i class="fa-solid fa-search"></i>
              </div>
              <button 
                v-if="searchQuery && !isSearching"
                @click="clearSearch"
                class="absolute right-12 top-1/2 transform -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-300"
              >
                <i class="fa-solid fa-times"></i>
              </button>
              <button
                @click="searchPosts"
                class="absolute right-4 top-1/2 transform -translate-y-1/2 px-4 py-1 bg-amber-500 text-white rounded-lg hover:bg-amber-600 transition"
                :disabled="isSearching"
              >
                <i v-if="isSearching" class="fa-solid fa-spinner animate-spin"></i>
                <i v-else class="fa-solid fa-search"></i>
              </button>

              <!-- 历史记录下拉框 -->
              <div 
                v-if="showHistory && !isSearching"  
                class="absolute z-10 mt-2 w-full bg-white/90 dark:bg-slate-800/90 rounded-xl shadow-lg border border-slate-200/50 dark:border-slate-700/50 max-h-60 overflow-y-auto"
              >
                <div class="p-2">
                  <div class="flex justify-between items-center mb-2 px-3 py-2">
                    <span class="text-sm font-medium text-slate-500 dark:text-slate-400">历史搜索</span>
                    <button 
                      v-if="searchHistory.length > 0"
                      @click="clearAllHistory"
                      class="text-xs text-amber-500 hover:text-amber-600 dark:hover:text-amber-400"
                    >
                      清空
                    </button>
                  </div>

                   <!-- 显示暂无搜索历史或搜索记录列表 -->
                   <div v-if="searchHistory.length === 0" class="px-3 py-4 text-center text-sm text-slate-500 dark:text-slate-400">
                     <i class="fa-solid fa-clock-rotate-left mr-2"></i>暂无搜索历史
                   </div>
          
                  <div 
                    v-for="(record, index) in searchHistory" 
                    :key="index"
                    class="flex items-center justify-between px-3 py-2 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg cursor-pointer"
                    @click="selectHistoryItem(record)"
                  >
                    <div class="flex items-center flex-1">
                      <i class="fa-solid fa-clock-rotate-left mr-3 text-slate-400 text-sm"></i>
                      <span class="text-slate-700 dark:text-slate-200 truncate">{{ record.searchContent }}</span>
                    </div>
                    <!-- 删除单条记录按钮 -->
                    <button
                      @click.stop="removeHistoryItem(index)"
                      class="ml-2 text-slate-400 hover:text-red-500"
                    >
                      <i class="fa-solid fa-times text-sm"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 搜索结果提示 -->
            <div v-if="searchQuery && searchResults.length > 0" class="mt-2 text-sm text-slate-500 dark:text-slate-400">
              找到 {{ searchResults.length }} 个相关帖子
            </div>
            <div v-else-if="searchQuery && searchResults.length === 0 && !isSearching && hasSearched" class="mt-2 text-sm text-amber-500 dark:text-amber-400">
              未找到相关帖子
            </div>

            <!-- 分类筛选提示 -->
            <div v-if="selectedCategory" class="mt-2 flex items-center justify-between text-sm">
              <div class="text-amber-500 dark:text-amber-400">
                当前筛选: <span class="font-medium">{{ selectedCategory.categoryName }}</span>
              </div>
              <button @click="resetCategory" class="text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200">
                <i class="fa-solid fa-times mr-1"></i> 清除筛选
              </button>
            </div>
          </div>

          <!-- 主内容区 - 帖子列表 (改为微博样式的单栏流) -->
          <div class="flex flex-col gap-4 mb-8">
            <!-- Loading 状态 -->
            <div v-if="loading" class="flex flex-col items-center justify-center py-20 opacity-70 bg-white dark:bg-slate-800 border border-slate-100 dark:border-slate-700 p-6 rounded-xl">
              <div class="animate-spin rounded-full h-12 w-12 border-4 border-slate-200 border-t-amber-500"></div>
              <p class="mt-4 text-slate-500 dark:text-slate-400 font-medium">正在加载精彩内容...</p>
            </div>

            <!-- 空状态处理 (保留原有逻辑) -->
            <div v-else-if="displayPosts.length === 0" class="text-center py-20 bg-white dark:bg-slate-800 border border-dashed border-slate-300 dark:border-slate-700 p-6 rounded-xl">
               <h3 class="text-xl font-medium text-slate-700 dark:text-slate-200">暂无帖子</h3>
               <p v-if="activeTab === 'my'" class="text-slate-500 dark:text-slate-400 mt-2">
                <button @click="openPublishModalAndScroll" class="text-blue-500 underline">现在去发一个</button>
               </p>
               <p v-else class="text-slate-500 dark:text-slate-400 mt-2">快来分享你的第一条动态吧！</p>
            </div>

            <!-- 帖子卡片 -->
            <div
              v-for="post in displayPosts"
              :key="post.postId"
              class="post-parallax bg-white dark:bg-slate-800/80 backdrop-blur-sm rounded-2xl shadow-sm border border-slate-100/70 dark:border-slate-700/50 p-5 transition-all duration-300 hover:shadow-md"
            >
              <!-- 帖子头部 (用户信息) -->
              <div class="flex justify-between items-start mb-4">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <img
                      :src="getAvatarUrl(post.userAvatar)"
                      class="w-12 h-12 rounded-full object-cover border-2 border-white dark:border-slate-700 shadow-sm"
                    />
                    <div v-if="isCurrentUserAuthor(post.userId)" class="absolute -bottom-1 -right-1 px-1.5 h-4 flex items-center justify-center text-[10px] font-bold rounded-full bg-amber-500 text-white border border-white dark:border-slate-800">
                      我
                    </div>
                  </div>
                  <div>
                    <h3 class="font-bold text-slate-800 dark:text-slate-100">{{ post.username }}</h3>
                    <div class="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                      <span>{{ formatTime(post.createTime) }}</span>
                      <span v-if="post.categoryName" class="text-amber-500 font-medium">#{{ post.categoryName }}</span>
                    </div>
                  </div>
                </div>

                

                <!-- 右侧按钮组 -->
                <div class="flex items-center gap-2">
                  <!-- 详情按钮 - 新增 -->
                  <button 
                    @click="router.push(`/forum-post-detail/${post.postId}`)" 
                    class="p-2 text-slate-400 hover:text-amber-500 hover:bg-amber-50 dark:hover:bg-amber-900/20 rounded-lg transition-colors"
                    title="查看详情"
                  >
                    <i class="fa-solid fa-arrow-right text-lg"></i>
                  </button>
                  
                  <!-- 删除按钮 (仅作者可见) -->
                  <button 
                    v-if="isCurrentUserAuthor(post.userId)" 
                    @click="deletePost(post.postId)" 
                    class="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors"
                    title="删除帖子"
                  >
                    <i class="fa-solid fa-trash-can"></i>
                  </button>
                </div>
              </div>

              <!-- 帖子内容 -->
              <div class="cursor-pointer mb-3" @click="router.push(`/forum-post-detail/${post.postId}`)">
                <h2 class="text-xl font-bold text-slate-800 dark:text-slate-100 mb-2 hover:text-amber-600 transition-colors">
                  {{ post.title }}
                </h2>
                <p class="text-slate-600 dark:text-slate-300 leading-relaxed line-clamp-3">
                  {{ post.content }}
                </p>
              </div>

              <!-- 图片展示 (微博九宫格样式) -->
              <div v-if="getPostAllImages(post).length > 0" class="mb-4">
                <div 
                  class="grid gap-1.5"
                  :class="getGridClass(getPostAllImages(post).length)"
                >
                  <div 
                    v-for="(img, index) in getPostAllImages(post).slice(0, 9)" 
                    :key="index"
                    class="relative aspect-square rounded-lg overflow-hidden bg-slate-100 dark:bg-slate-700 group/img cursor-pointer"
                    @click="router.push(`/forum-post-detail/${post.postId}`)"
                  >
                    <img :src="img" class="w-full h-full object-cover transition-transform duration-500 group-hover/img:scale-105" />
                    
                    <!-- 第九张图片显示遮罩 -->
                    <div 
                      v-if="index === 8 && getPostAllImages(post).length > 9"
                      class="absolute inset-0 bg-black/40 flex flex-col items-center justify-center text-white backdrop-blur-[2px]"
                    >
                      <i class="fa-solid fa-plus text-xl"></i>
                      <span class="text-xs font-bold mt-1">查看更多({{ getPostAllImages(post).length - 9 }})</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 底部操作栏 -->
              <div class="flex items-center justify-around border-t border-slate-100/50 dark:border-slate-700/50 pt-3">
                <button @click="router.push(`/forum-post-detail/${post.postId}`)" class="flex items-center gap-2 text-slate-500 hover:text-blue-500 transition-colors">
                  <i class="fa-regular fa-comment text-lg"></i>
                  <span class="text-sm">{{ post.commentCount || 0 }}</span>
                </button>
                <button @click="likePost(post)" class="flex items-center gap-2 transition-colors" :class="post.liked ? 'text-red-500' : 'text-slate-500 hover:text-red-500'">
                  <i :class="[post.liked ? 'fa-solid fa-heart' : 'fa-regular fa-heart', 'text-lg']"></i>
                  <span class="text-sm">{{ post.likeCount || 0 }}</span>
                </button>
                <button @click="favoritePost(post)" class="flex items-center gap-2 transition-colors" :class="post.favorited ? 'text-amber-500' : 'text-slate-500 hover:text-amber-500'">
                  <i :class="[post.favorited ? 'fa-solid fa-star' : 'fa-regular fa-star', 'text-lg']"></i>
                  <span class="text-sm">{{ post.favoriteCount || 0 }}</span>
                </button>
              </div>
            </div>
          </div>

          <div ref="loadMoreRef" class="h-10"></div>
          <div v-if="loadingMore" class="text-center py-6 text-slate-400">
            正在加载更多内容...
          </div>
          <div v-if="!hasMore" class="text-center py-6 text-slate-400">
            已经到底啦～
          </div>

        </div>

        <!-- 右侧侧边栏 -->
        <div 
          :class="[
            'fixed right-0 top-24 bottom-0 overflow-y-auto p-6 border-l border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 z-40 transition-all duration-300',
            sidebarRightVisible ? 'translate-x-0 w-64' : 'translate-x-full w-0'
          ]"
        >
          <div :class="sidebarRightVisible ? 'w-full' : 'w-0 overflow-hidden'">
            <SideBarR />
          </div>
          
          <!-- 添加收起按钮 -->
          <button 
            @click="toggleSidebarRight"
            class="absolute top-4 -left-3 bg-slate-200 dark:bg-slate-700 rounded-l-lg p-1 shadow-md hover:bg-slate-300 dark:hover:bg-slate-600"
          >
            <i :class="sidebarRightVisible ? 'fa-solid fa-chevron-right' : 'fa-solid fa-chevron-left'"></i>
          </button>
        </div>

      </div>
    </main>

    <!-- 发帖弹窗组件 -->
    <PostPublishModal
      v-if="showPublishModal"
      :categories="categories"
      @close="closePublishModal"
      @success="handlePublishSuccess"
    />

    <!-- 圆形加号发布按钮 -->
    <div 
      ref="publishButtonRef"
      class="fixed z-40 flex items-center justify-center cursor-move"
      :style="{
        right: `${buttonPosition.x}px`,
        bottom: `${buttonPosition.y}px`,
        width: '64px',
        height: '64px'
      }"
      @mousedown="startDrag"
      @touchstart="startDrag"
    >
      <button
        @click="openPublishModal"
        class="w-16 h-16 bg-gradient-to-r from-amber-500 to-orange-500 text-white rounded-full shadow-lg hover:shadow-xl hover:scale-110 transition-all duration-300 flex items-center justify-center"
        aria-label="发布新话题"
      >
        <i class="fa-solid fa-plus text-2xl"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { findPostStatus } from '@/api/forums'
import { ref, onMounted, computed, watch, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { updatePostLikeState, getPostLikeState } from '@/utils/likeState';
import { 
  getForumPosts, 
  togglePostLike, 
  togglePostFavorite,
  getForumCategories, 
  deletePostAPI, 
  saveSearchRecord as saveSearchRecordAPI, 
  getSearchRecords,
  getUserById,
  deleteUserSearchHistory,
  deleteSearchRecordById,
  deleteSearchRecordByContent,
  getRecommendPosts,
  clearRecommendCache,
  getPostsByUsername,
  getMyPublishedPostsAPI,      
  getPostsByFollowing 

} from '@/api/forums';
import { getUserInfoAPI } from '@/api/user';
import { getAvatarUrl } from '@/utils/imageUtils';

// 导入缺失的组件
import Nav from '@/components/Nav.vue';
import SideBarL from '@/components/SideBarL.vue';
import SideBarR from '@/components/SideBarR.vue';
import Footer from '@/components/Footer.vue';
import PostPublishModal from '@/components/PostModal.vue';

// 类型定义
interface Comment {
  id: number;
  postId: number;
  username: string;
  userId?: number;
  content: string;
  createTime: string;
}

interface Post {
  postId: number;
  title: string;
  content: string;
  username: string;
  userId?: number;
  userAvatar?: string;
  createTime: string;
  liked: boolean;
  likeCount: number;
  favorited: boolean; 
  favoriteCount: number; 
  commentCount: number;
  comments: Comment[];
  categoryName?: string;
  categoryId?: number;
  coverImage?: string;
  images?: string[];
}

interface Category {
  categoryId: number;
  categoryName: string;
  icon?: string;
  count?: number;
}

//搜索记录
interface ForumSearchRecord {
  searchId?: number;
  userId: number;
  searchContent: string;
  searchTime?: string;
  updateTime?: string;
}

interface LikeState {
  postLikes: Record<number, boolean>; // 帖子点赞状态
  postLikeCounts: Record<number, number>; // 帖子点赞数
  postFavorites: Record<number, boolean>; // 帖子收藏状态
  postFavoriteCounts: Record<number, number>; // 帖子收藏数
}

const LIKE_STATE_KEY = 'forum_like_state';
const FAVORITE_STATE_KEY = 'forum_favorite_state';
const router = useRouter()

// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iODAiIHI9IjQwIiBmaWxsPSIjZTllOWU5Ii8+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iMTUwIiByPSIzMCIgZmlsbD0iI2U5ZTllOSIvPgo8L3N2Zz4=';

const ColumnValues = ref([])

// 响应式数据
const loading = ref<boolean>(true);
const posts = ref<Post[]>([]);
const categories = ref<Category[]>([]);
const showCommentId = ref<number | null>(null);
const showPublishModal = ref(false);
const searchQuery = ref<string>('');
const searchResults = ref<Post[]>([]);
const isSearching = ref<boolean>(false);
const hasSearched = ref<boolean>(false);
const selectedCategory = ref<Category | null>(null);
const userCache = new Map<number, any>();
const showSearchBox = ref<boolean>(false);

// 搜索历史相关
const searchHistory = ref<ForumSearchRecord[]>([]);
const showHistory = ref<boolean>(false);


// 添加可移动按钮相关变量
const publishButtonRef = ref<HTMLElement | null>(null);
const isDragging = ref(false);
const dragStart = ref({ x: 0, y: 0 });
const buttonPosition = ref({ x: 32, y: 200 }); 

//无限流相关
const pageSize = 50
const lastPostId = ref<number | null>(null)
const hasMore = ref(true)
const loadingMore = ref(false)
const loadMoreRef = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

// 添加侧边栏控制状态
const sidebarLeftVisible = ref(true);
const sidebarRightVisible = ref(true);
const bothSidebarsCollapsed = ref(false); // 记录两侧边栏是否都被收起

// 添加新的响应式数据
const activeTab = ref<'recommend' | 'following' | 'my'>('recommend');
const followingPosts = ref<Post[]>([]);
const myPosts = ref<Post[]>([]);
const tabLoading = ref({
  following: false,
  my: false,
  recommend: false
});

// 辅助函数：图片处理逻辑
const getPostAllImages = (post: Post) => {
  const imgs = [];
  if (post.coverImage) imgs.push(post.coverImage);
  if (post.images && post.images.length > 0) imgs.push(...post.images);
  // 去重处理
  return [...new Set(imgs)];
};

const getGridClass = (count: number) => {
  if (count === 1) return 'grid-cols-1 w-2/3 max-w-[400px]';
  if (count === 2 || count === 4) return 'grid-cols-2 w-2/3 max-w-[400px]';
  return 'grid-cols-3 max-w-[500px]';
};

// 添加切换搜索框显示的方法
const toggleSearchBox = () => {
  showSearchBox.value = !showSearchBox.value;
  
  // 如果关闭搜索框，同时清空搜索内容
  if (!showSearchBox.value) {
    searchQuery.value = '';
    searchResults.value = [];
    hasSearched.value = false;
  }
};

  // 拖拽开始
  const startDrag = (e: MouseEvent | TouchEvent) => {
    isDragging.value = true;
    
  // 获取触摸或鼠标位置
  const clientX = e instanceof TouchEvent ? e.touches[0].clientX : e.clientX;
  const clientY = e instanceof TouchEvent ? e.touches[0].clientY : e.clientY;
  
  // 获取按钮当前位置
  const rect = publishButtonRef.value?.getBoundingClientRect();
  if (rect) {
    dragStart.value = {
      x: clientX - rect.right, // 相对于按钮右边缘的偏移
      y: clientY - rect.bottom // 相对于按钮下边缘的偏移
    };
  }
  // 添加事件监听器
  document.addEventListener('mousemove', handleDrag);
  document.addEventListener('touchmove', handleDrag, { passive: false });
  document.addEventListener('mouseup', stopDrag);
  document.addEventListener('touchend', stopDrag);
};

// 拖拽处理
const handleDrag = (e: MouseEvent | TouchEvent) => {
  if (!isDragging.value) return;
  
  e.preventDefault();
  
  const clientX = e instanceof TouchEvent ? e.touches[0].clientX : e.clientX;
  const clientY = e instanceof TouchEvent ? e.touches[0].clientY : e.clientY;
  
  const newX = window.innerWidth - clientX + dragStart.value.x;
  const newY = window.innerHeight - clientY + dragStart.value.y;
  
  // 限制按钮在屏幕范围内
  const clampedX = Math.max(16, Math.min(newX, window.innerWidth - 80)); // 最小距离左边16px，最大距离右边80px
  const clampedY = Math.max(16, Math.min(newY, window.innerHeight - 80)); // 最小距离顶部16px，最大距离底部80px
  
  buttonPosition.value = {
    x: clampedX,
    y: clampedY
  };
};

// 拖拽结束
const stopDrag = () => {
  isDragging.value = false;
  
  // 移除事件监听器
  document.removeEventListener('mousemove', handleDrag);
  document.removeEventListener('touchmove', handleDrag);
  document.removeEventListener('mouseup', stopDrag);
  document.removeEventListener('touchend', stopDrag);
  
  // 保存位置到本地存储
  saveButtonPosition();
};

// 保存按钮位置到本地存储
const saveButtonPosition = () => {
  localStorage.setItem('publishButtonPosition', JSON.stringify(buttonPosition.value));
};

// 从本地存储加载按钮位置
const loadButtonPosition = () => {
  const savedPosition = localStorage.getItem('publishButtonPosition');
  if (savedPosition) {
    try {
      const position = JSON.parse(savedPosition);
      buttonPosition.value = {
        x: Math.max(16, Math.min(position.x, window.innerWidth - 80)),
        y: Math.max(16, Math.min(position.y, window.innerHeight - 80))
      };
    } catch (e) {
      console.error('加载按钮位置失败:', e);
    }
  }
};

// 帖子滚动轻微浮动视差
let parallaxTicking = false;

const handlePostParallax = () => {
  // 移动端不启用，避免影响性能
  if (window.innerWidth < 768) return;

  if (parallaxTicking) return;
  parallaxTicking = true;

  requestAnimationFrame(() => {
    const cards = document.querySelectorAll<HTMLElement>('.post-parallax');
    const viewportCenter = window.innerHeight / 2;

    cards.forEach((card, index) => {
      const rect = card.getBoundingClientRect();
      const cardCenter = rect.top + rect.height / 2;
      const distance = cardCenter - viewportCenter;

      // 控制浮动幅度
      const strength = (index % 3 + 1) * 0.015;
      const translateY = Math.max(-6, Math.min(6, -distance * strength));

      card.style.transform = `translateY(${translateY}px)`;
    });

    parallaxTicking = false;
  });
};


// 获取当前用户ID
const getCurrentUserId = (): number | null => {
  const userInfoStr = localStorage.getItem('userInfo');
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      return userInfo.userId || userInfo.id;
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
  return null;
};


// 检查当前用户是否是帖子作者
const isCurrentUserAuthor = (postUserId: number | undefined) => {
  const currentUserId = getCurrentUserId();
  return currentUserId && postUserId && currentUserId === postUserId;
};

// 在响应式数据定义后添加监听器
watch(searchQuery, (newQuery) => {
  if (!newQuery.trim()) {
    // 当搜索框为空时，立即清空搜索结果，显示全部帖子
    searchResults.value = [];
    hasSearched.value = false;
  }
});

// 当选中分类变化时，清空搜索结果
watch(selectedCategory, () => {
  searchQuery.value = '';
  searchResults.value = [];
  hasSearched.value = false;
});

// 根据分类筛选帖子
const filteredPostsByCategory = computed(() => {
  if (!selectedCategory.value) {
    return posts.value;
  }
  
  // 使用更严格的类型转换匹配
  const targetCategoryId = Number(selectedCategory.value.categoryId);
  
  return posts.value.filter(post => {
    const postCategoryId = post.categoryId ? Number(post.categoryId) : null;
    return postCategoryId === targetCategoryId;
  });
});


// 工具函数
const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  // 简单的相对时间显示
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff/60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff/3600000)}小时前`;
  
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 添加一个新的方法来打开发布模态框并滚动到按钮位置
const openPublishModalAndScroll = () => {
  // 触发发布按钮点击事件
  showPublishModal.value = true;
  
  // 平滑滚动到发布按钮位置
  nextTick(() => {
    const publishButton = publishButtonRef.value;
    if (publishButton) {
      const rect = publishButton.getBoundingClientRect();
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      const targetTop = rect.top + scrollTop - window.innerHeight / 2;
      
      window.scrollTo({
        top: targetTop,
        behavior: 'smooth'
      });
    }
  });
};

// 侧边栏控制函数
const toggleSidebarLeft = () => {
  sidebarLeftVisible.value = !sidebarLeftVisible.value;
  // 更新整体状态
  bothSidebarsCollapsed.value = !sidebarLeftVisible.value && !sidebarRightVisible.value;
  localStorage.setItem('bothSidebarsCollapsed', bothSidebarsCollapsed.value.toString());
};

const toggleSidebarRight = () => {
  sidebarRightVisible.value = !sidebarRightVisible.value;
  // 更新整体状态
  bothSidebarsCollapsed.value = !sidebarLeftVisible.value && !sidebarRightVisible.value;
  localStorage.setItem('bothSidebarsCollapsed', bothSidebarsCollapsed.value.toString());
};

// 一键收起两侧边栏
const collapseBothSidebars = () => {
  sidebarLeftVisible.value = false;
  sidebarRightVisible.value = false;
  bothSidebarsCollapsed.value = true;
  localStorage.setItem('bothSidebarsCollapsed', 'true');
};

// 一键展开两侧边栏
const expandBothSidebars = () => {
  sidebarLeftVisible.value = true;
  sidebarRightVisible.value = true;
  bothSidebarsCollapsed.value = false;
  localStorage.setItem('bothSidebarsCollapsed', 'false');
};

// 切换两侧边栏状态
const toggleBothSidebars = () => {
  if (bothSidebarsCollapsed.value) {
    expandBothSidebars();
  } else {
    collapseBothSidebars();
  }
};


// 搜索功能
const searchPosts = async () => {
  if (!searchQuery.value.trim()) {
    searchResults.value = [];
    hasSearched.value = false;
    return;
  }
  
  // 确保搜索框是可见的
  showSearchBox.value = true;
  
  isSearching.value = true;
  hasSearched.value = true;
  
  try {
    await new Promise(resolve => setTimeout(resolve, 300));
    
    const query = searchQuery.value.trim();
    
    // 判断是否是搜索用户名
    if (query.startsWith('@')) {
      // 按用户名搜索
      const username = query.substring(1); // 去掉 @ 符号
      const response = await getPostsByUsername(username);
      
      if (response.code === 200 && response.data) {
        // 处理返回的帖子数据
        const processedResults = await processPosts(
          response.data,
          categories.value
        );
        searchResults.value = processedResults;
      } else {
        searchResults.value = [];
        console.error('获取用户帖子失败:', response.msg);
      }
    } else {
      // 原有的标题/内容搜索
      const queryLower = query.toLowerCase();
      // 如果有选中分类，在分类筛选结果中搜索
      const searchBase = selectedCategory.value ? filteredPostsByCategory.value : posts.value;
      searchResults.value = searchBase.filter(post => 
        post.title.toLowerCase().includes(queryLower) || 
        post.content.toLowerCase().includes(queryLower)
      );
    }

    await saveSearchRecord(searchQuery.value.trim());

  } catch (error) {
    console.error('搜索失败:', error);
    searchResults.value = [];
  } finally {
    isSearching.value = false;
    // 这里不隐藏搜索框，让用户能看到搜索结果
  }
};

// 修改清除搜索功能
const clearSearch = () => {
  searchQuery.value = '';
  searchResults.value = [];
  hasSearched.value = false;
  // 不隐藏搜索框，让用户可以继续输入
};

// 加载搜索历史
const loadSearchHistory = async () => {
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      console.error('无法获取当前用户ID');
      return;
    }
    
    const records = await getSearchRecords(userId);
    console.log('搜索记录API返回:', records); // 添加调试日志
    
    // 正确处理API返回的数据结构
    let recordsData = [];
    if (records && records.data) {
      // 如果返回的是标准响应格式
      if (records.data.code === 200) {
        recordsData = records.data.data || [];
      } else {
        // 如果直接返回数组数据
        recordsData = Array.isArray(records.data) ? records.data : [];
      }
    } else if (Array.isArray(records)) {
      // 如果直接返回数组
      recordsData = records;
    }
    
    console.log('处理后的搜索记录:', recordsData); // 添加调试日志
    searchHistory.value = recordsData.slice(0, 5);
  } catch (error) {
    console.error('加载搜索历史失败:', error);
    // 如果API调用失败，使用本地存储作为备用方案
    loadSearchHistory();
  }
};

// 从本地存储加载搜索历史
const loadLocalSearchHistory = () => {
  try {
    const localHistory = localStorage.getItem('forum_search_history');
    if (localHistory) {
      const parsedHistory = JSON.parse(localHistory);
      searchHistory.value = parsedHistory.slice(0, 5);
    }
  } catch (error) {
    console.error('加载本地搜索历史失败:', error);
  }
};

// 保存搜索记录到本地存储
const saveLocalSearchHistory = (keyword: string) => {
  try {
    const userId = getCurrentUserId();
    if (!userId) return;
    
    const localHistory = localStorage.getItem('forum_search_history');
    let historyArray = localHistory ? JSON.parse(localHistory) : [];
    
    // 去重并限制数量
    historyArray = historyArray.filter(item => item.searchContent !== keyword);
    historyArray.unshift({
      userId,
      searchContent: keyword,
      searchTime: new Date().toISOString()
    });
    
    // 最多保存10条记录
    historyArray = historyArray.slice(0, 10);
    localStorage.setItem('forum_search_history', JSON.stringify(historyArray));
  } catch (error) {
    console.error('保存本地搜索历史失败:', error);
  }
};

// 保存搜索记录
const saveSearchRecord = async (keyword: string) => {
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      console.error('无法获取当前用户ID');
      return;
    }
    
    await saveSearchRecordAPI({ userId, searchContent: keyword });
    await loadSearchHistory();
  } catch (error) {
    console.error('保存搜索记录失败:', error);
    // API失败时使用本地存储
    saveLocalSearchHistory(keyword);
    loadSearchHistory();
  }
};


const hideHistoryWithDelay = () => {
  setTimeout(() => {
    showHistory.value = false;
  }, 200);
};

// 选择历史记录项
const selectHistoryItem = (record: ForumSearchRecord) => {
  searchQuery.value = record.searchContent;
  showHistory.value = false;
  searchPosts();
};

// 删除单条历史记录
const removeHistoryItem = async (index: number) => {
  const record = searchHistory.value[index];
  
  if (!record.searchId) {
    searchHistory.value.splice(index, 1);
    updateLocalSearchHistory();
    return;
  }

  if (confirm('确定要删除这条搜索记录吗？')) {
    try {
      // 调用后端API删除记录
      await deleteSearchRecordById(record.searchId);
      
      // 从数组中移除该记录
      searchHistory.value.splice(index, 1);
      
      // 更新本地存储
      updateLocalSearchHistory();
      
      console.log('搜索记录删除成功');
      // ElMessage.success('搜索记录删除成功');
    } catch (error) {
      console.error('删除搜索记录失败:', error);
      // 可选：显示错误消息
      // ElMessage.error('删除搜索记录失败，请稍后重试');
      alert('删除搜索记录失败，请稍后重试');
    }
  }
};

// 清空历史记录 - 实现真正清空
const clearAllHistory = async () => {
  const userId = getCurrentUserId();
  if (!userId) {
    console.error('无法获取当前用户ID');
    return;
  }

  if (confirm('确定要清空所有搜索历史吗？此操作不可恢复。')) {
    try {
      // 调用后端API清空用户的所有搜索记录
      await deleteUserSearchHistory(userId);
      
      // 清空本地存储
      localStorage.setItem('forum_search_history', JSON.stringify([]));
      
      // 清空当前显示历史记录
      searchHistory.value = [];
      
      // 关闭历史记录显示
      showHistory.value = false;
      
      console.log('搜索历史清空成功');
      // ElMessage.success('搜索历史已清空');
    } catch (error) {
      console.error('清空搜索历史失败:', error);
      
      // 如果API失败，询问用户是否继续清空本地记录
      if (confirm('清空搜索历史失败，是否清空本地记录？')) {
        localStorage.setItem('forum_search_history', JSON.stringify([]));
        searchHistory.value = [];
        showHistory.value = false;
      }
    }
  }
};

// 更新本地搜索历史存储函数
const updateLocalSearchHistory = () => {
  try {
    const userId = getCurrentUserId();
    if (!userId) return;
    
    // 将当前前端的搜索历史同步到本地存储
    const historyForStorage = searchHistory.value.map(record => ({
      ...record,
      userId: record.userId || userId
    }));
    
    localStorage.setItem('forum_search_history', JSON.stringify(historyForStorage));
  } catch (error) {
    console.error('更新本地搜索历史失败:', error);
  }
};

const openPublishModal = () => {
  showPublishModal.value = true
};

const closePublishModal = () => {
  showPublishModal.value = false
};

const handlePublishSuccess = async () => {
  showPublishModal.value = false;
  
  // 重新加载相关数据
  if (activeTab.value === 'my') {
    await loadMyPosts();
  } else if (activeTab.value === 'recommend') {
    await getPosts();
  }
  
};

// 分类筛选相关函数
const handleCategoryChange = (category: Category) => {
  selectedCategory.value = category;
};

const resetCategory = () => {
  selectedCategory.value = null;
};

// 更新点赞状态到本地存储
const updatePostLikeState = (postId: number, liked: boolean, likeCount: number) => {
  try {
    const stateStr = localStorage.getItem(LIKE_STATE_KEY);
    const state: LikeState = stateStr ? JSON.parse(stateStr) : { 
      postLikes: {}, 
      postLikeCounts: {}
    };
    
    state.postLikes[postId] = liked;
    state.postLikeCounts[postId] = likeCount;
    
    localStorage.setItem(LIKE_STATE_KEY, JSON.stringify(state));
    
    // 触发存储事件，用于跨标签页同步
    window.dispatchEvent(new StorageEvent('storage', {
      key: LIKE_STATE_KEY,
      newValue: JSON.stringify(state)
    }));
  } catch (e) {
    console.error('更新点赞状态失败:', e);
  }
};

// 获取点赞状态
const getPostLikeState = (postId: number) => {
  try {
    const stateStr = localStorage.getItem(LIKE_STATE_KEY);
    const state: LikeState = stateStr ? JSON.parse(stateStr) : { 
      postLikes: {}, 
      postLikeCounts: {}
    };
    
    return {
      liked: state.postLikes[postId] || false,
      likeCount: state.postLikeCounts[postId] || 0
    };
  } catch (e) {
    console.error('获取点赞状态失败:', e);
    return { liked: false, likeCount: 0 };
  }
};


// 添加设置活动标签页的方法
const setActiveTab = async (tab: 'recommend' | 'following' | 'my') => {
  if (activeTab.value === tab) return;
  
  activeTab.value = tab;
  
  // 根据标签页加载对应的数据
  switch (tab) {
    case 'recommend':
      if (posts.value.length === 0 && !tabLoading.value.recommend) {
        await getPosts();
      }
      break;
    case 'following':
      if (followingPosts.value.length === 0 && !tabLoading.value.following) {
        await loadFollowingPosts();
      }
      break;
    case 'my':
      if (myPosts.value.length === 0 && !tabLoading.value.my) {
        await loadMyPosts();
      }
      break;
  }
};

// 加载关注的帖子
const loadFollowingPosts = async () => {
  tabLoading.value.following = true;
  try {
    const response = await getPostsByFollowing();
    if (response.code === 200) {
      if (response.data && Array.isArray(response.data)) {
        // 如果返回的是帖子数组
        followingPosts.value = await processPosts(response.data, categories.value);
      } else if (response.data === null || response.data === undefined) {
        // 如果用户没有关注任何人，API可能返回 null
        followingPosts.value = [];
      } else {
        // 其他数据结构处理
        const postData = Array.isArray(response.data?.data) ? response.data.data : response.data;
        followingPosts.value = await processPosts(postData, categories.value);
      }
    } else {
      console.error('获取关注的帖子失败:', response.msg);
      followingPosts.value = [];
    }
  } catch (error) {
    console.error('获取关注的帖子时出错:', error);
    followingPosts.value = [];
  } finally {
    tabLoading.value.following = false;
  }
};

// 加载我的帖子
const loadMyPosts = async () => {
  tabLoading.value.my = true;
  try {
    const response = await getMyPublishedPostsAPI();
    if (response.code === 200 && response.data) {
      myPosts.value = await processPosts(response.data, categories.value);
    } else {
      console.error('获取我的帖子失败:', response.msg);
      myPosts.value = [];
    }
  } catch (error) {
    console.error('获取我的帖子时出错:', error);
    myPosts.value = [];
  } finally {
    tabLoading.value.my = false;
  }
};


// 获取初始帖子列表
const getPosts = async () => {
  tabLoading.value.recommend = true;
  loading.value = true;
  hasMore.value = true;
  lastPostId.value = null;

  try {
    const categoryRes = await getForumCategories();
    const categoriesData: Category[] = Array.isArray(categoryRes.data?.data)
      ? categoryRes.data.data
      : [];

    categories.value = categoriesData;

    console.log('开始请求推荐帖子', {
      limit: pageSize,
      userId: getCurrentUserId(),
      lastPostId: null
    });

    const res = await getRecommendPosts({
      limit: pageSize,
      userId: getCurrentUserId() || undefined,
      lastPostId: undefined
    });

    console.log('完整 res =', res);

    const recommendData = res.data || {};
    const rawData: any[] = Array.isArray(recommendData.list)
      ? recommendData.list
      : [];

    hasMore.value = Boolean(recommendData.hasMore);

    console.log('推荐原始数量:', rawData.length);
    console.log('hasMore:', hasMore.value);
    console.log('原始帖子数据样本:', rawData.slice(0, 3)); // 显示前3个帖子的原始数据

    const approvedList = rawData.filter(
      post => Number(post.status ?? 1) === 1
    );

    console.log('审核通过的帖子数量:', approvedList.length);
    console.log('审核通过的帖子样本:', approvedList.slice(0, 3));

    const processedPosts = await processPosts(
      approvedList,
      categoriesData
    );

    posts.value = processedPosts;

    if (processedPosts.length > 0) {
      lastPostId.value =
        processedPosts[processedPosts.length - 1].postId;
    }

    console.log('最终展示帖子数:', posts.value.length);
    console.log('最终帖子样本:', posts.value.slice(0, 3));
  } catch (error) {
    console.error('获取推荐帖子失败:', error);
    posts.value = [];
    hasMore.value = false;
  } finally {
    loading.value = false;
    tabLoading.value.recommend = false;
  }
};



//加载更多帖子
const loadMorePosts = async () => {
  if (!hasMore.value || loadingMore.value) return;
  loadingMore.value = true;

  try {
    const res = await getRecommendPosts({
      limit: pageSize,
      userId: getCurrentUserId() || undefined,
      lastPostId: lastPostId.value || undefined
    });

    const recommendData = res.data || {}; 
    const rawData: any[] = Array.isArray(recommendData.list)
      ? recommendData.list
      : [];

    hasMore.value = Boolean(recommendData.hasMore);

    if (rawData.length === 0) {
      console.warn('推荐接口返回空列表');
      return;
    }

    const newPosts = await processPosts(rawData, categories.value);
    posts.value.push(...newPosts);

    if (newPosts.length > 0) {
      lastPostId.value = newPosts[newPosts.length - 1].postId;
    }

    console.log(`加载更多：${newPosts.length}，hasMore=${hasMore.value}`);
  } catch (error) {
    console.error('加载更多帖子失败:', error);
  } finally {
    loadingMore.value = false;
  }
};





//统一处理帖子数据
const processPosts = async (rawPosts: any[], categoriesData: Category[]): Promise<Post[]> => {
  const result: Post[] = [];

  for (const post of rawPosts) {
    
    let userInfo = null;

    if (post.userId) {
      if (userCache.has(post.userId)) {
        userInfo = userCache.get(post.userId);
      } else {
        try {
          const userRes = await getUserById(post.userId);
          
          if (userRes) {
            userInfo = userRes;
          }
          
          if (userInfo) {
            userCache.set(post.userId, userInfo);
          }
        } catch (e) { 
        }
      }
    }

    // 增强分类匹配逻辑
    let categoryInfo = null;
    
    // 尝试多种匹配方式
    if (post.categoryId) {
      // 精确匹配
      categoryInfo = categoriesData.find(c => 
        c.categoryId === post.categoryId
      );
      
      // 字符串转换匹配
      if (!categoryInfo) {
        categoryInfo = categoriesData.find(c => 
          String(c.categoryId) === String(post.categoryId)
        );
      }
      
      // 数字转换匹配
      if (!categoryInfo) {
        categoryInfo = categoriesData.find(c => 
          Number(c.categoryId) === Number(post.categoryId)
        );
      }
    }
    

    // 在创建最终对象前再次确认用户名
    const finalUsername = userInfo?.username || userInfo?.name || post.username || post.author || '匿名用户';
    const finalUserAvatar = userInfo?.avatarUrl || userInfo?.avatar || post.userAvatar || post.avatar || defaultAvatar;
    
    // 添加分类名称获取逻辑
    let finalCategoryName = '';
    if (categoryInfo) {
      // 如果找到了匹配的分类
      finalCategoryName = categoryInfo.categoryName;
    } else if (post.categoryName) {
      // 如果帖子本身带有分类名称
      finalCategoryName = post.categoryName;
    } else if (post.categoryId) {
      // 如果有分类ID但没找到对应分类，查询数据库获取分类名称
      try {
        const categoryRes = await getForumCategories();
        let allCategories: Category[] = [];
        
        if (categoryRes.data && typeof categoryRes.data === 'object') {
          if (categoryRes.data.data !== undefined) {
            allCategories = Array.isArray(categoryRes.data.data) ? categoryRes.data.data : [];
          } else {
            allCategories = Array.isArray(categoryRes.data) ? categoryRes.data : [];
          }
        } else {
          allCategories = Array.isArray(categoryRes) ? categoryRes : [];
        }
        
        const exactCategory = allCategories.find(c => 
          c.categoryId === post.categoryId || 
          String(c.categoryId) === String(post.categoryId)
        );
        
        if (exactCategory) {
          finalCategoryName = exactCategory.categoryName;
        } else {
          finalCategoryName = `ID:${post.categoryId}`;
        }
      } catch (e) {
        console.error(`获取分类ID ${post.categoryId} 的名称失败`, e);
        finalCategoryName = `ID:${post.categoryId}`;
      }
    } else {
      finalCategoryName = '未分类';
    }
    
    // 处理收藏状态
    const favoriteState = getPostFavoriteState(post.postId || post.id);

    result.push({
      ...post,
      postId: post.postId || post.id,
      username: finalUsername,
      userAvatar: finalUserAvatar,
      categoryName: finalCategoryName,
      categoryId: post.categoryId || post.categoryId,
      liked: Boolean(post.liked),
      likeCount: post.likeCount || 0,
      favorited: post.favorited !== undefined ? Boolean(post.favorited) : Boolean(favoriteState.favorited),
      favoriteCount: post.favoriteCount || 0,
      status: Number(post.status || 1)
    });
  }

  console.log('处理完成的帖子数组:', result);
  
  // 调试：检查第一个帖子的点赞和收藏状态
  if (result.length > 0) {
    const firstPost = result[0];
    console.log('第一个帖子的状态:', {
      postId: firstPost.postId,
      liked: firstPost.liked,
      likeCount: firstPost.likeCount,
      favorited: firstPost.favorited,
      favoriteCount: firstPost.favoriteCount
    });
  }
  
  return result;
};


// 修改显示的帖子逻辑
const displayPosts = computed(() => {
  if (searchQuery.value && hasSearched.value) {
    return searchResults.value;
  }
  
  // 如果有分类筛选，返回筛选后的帖子
  if (selectedCategory.value) {
    let sourcePosts: Post[] = [];
    
    switch (activeTab.value) {
      case 'recommend':
        sourcePosts = posts.value;
        break;
      case 'following':
        sourcePosts = followingPosts.value;
        break;
      case 'my':
        sourcePosts = myPosts.value;
        break;
    }
    
    return sourcePosts.filter(post => 
      post.categoryId && 
      Number(post.categoryId) === Number(selectedCategory.value!.categoryId)
    );
  }

  // 根据当前标签页返回相应帖子
  switch (activeTab.value) {
    case 'recommend':
      return posts.value;
    case 'following':
      return followingPosts.value;
    case 'my':
      return myPosts.value;
    default:
      return posts.value;
  }
});

// 在 getCategories 函数中增强调试
const getCategories = async () => {
  try {
    const res = await getForumCategories();
    console.log('原始分类API响应:', res);
    
    let categoryRawData;
    if (res.data && typeof res.data === 'object') {
      if (res.data.data !== undefined) {
        categoryRawData = res.data.data; // 标准响应格式
      } else {
        categoryRawData = res.data; // 直接返回数据
      }
    } else {
      categoryRawData = res; // 直接返回整个对象
    }
    
    const categoryData = Array.isArray(categoryRawData) ? categoryRawData : [];
    categories.value = [...categoryData];
    
    // 添加分类数据的详细日志
    console.log('分类数据详情:', categories.value);
    categories.value.forEach(cat => {
      console.log(`分类ID: ${cat.categoryId}, 名称: ${cat.categoryName}`);
    });
  } catch (error) {
    console.error('获取分类失败:', error);
    categories.value = [];
  }
};

// 接收 post 参数，并用后端结果覆盖
const likePost = async (post: Post) => {
  try {
    const res = await togglePostLike(post.postId);
    if (res.data.code === 200) {
      // 使用 Object.assign 确保Vue响应性更新
      Object.assign(post, {
        liked: res.data.data.liked,
        likeCount: res.data.data.likeCount
      });
      
      // 强制触发UI更新
      const index = posts.value.findIndex(p => p.postId === post.postId);
      if (index !== -1) {
        posts.value[index] = { ...post };
      }
      
      // 更新缓存
      updatePostLikeState(post.postId, post.liked, post.likeCount);
      
      console.log('列表页点赞状态更新:', {
        postId: post.postId,
        liked: post.liked,
        likeCount: post.likeCount
      });
    }
  } catch (error) {
    alert('操作失败');
  }
};

// 列表页收藏
const favoritePost = async (post: Post) => {
  try {
    const res = await togglePostFavorite(post.postId);
    if (res.data.code === 200) {
      // 使用 Object.assign 确保Vue响应性更新
      Object.assign(post, {
        favorited: res.data.data.favorited,
        favoriteCount: res.data.data.favoriteCount
      });
      
      // 强制触发UI更新
      const index = posts.value.findIndex(p => p.postId === post.postId);
      if (index !== -1) {
        posts.value[index] = { ...post };
      }
      
      // 更新本地存储状态
      updatePostFavoriteState(post.postId, post.favorited, post.favoriteCount);
      
      console.log('列表页收藏状态更新:', {
        postId: post.postId,
        favorited: post.favorited,
        favoriteCount: post.favoriteCount
      });
    }
  } catch (error) {
    alert('操作失败');
  }
};

//更新收藏状态到本地存储
const updatePostFavoriteState = (postId: number, favorited: boolean, favoriteCount: number) => {
  try {
    const stateStr = localStorage.getItem(FAVORITE_STATE_KEY);
    const state: LikeState = stateStr ? JSON.parse(stateStr) : { 
      postFavorites: {}, 
      postFavoriteCounts: {},
      postLikes: {},
      postLikeCounts: {}
    };
    
    state.postFavorites[postId] = favorited;
    state.postFavoriteCounts[postId] = favoriteCount;
    
    localStorage.setItem(FAVORITE_STATE_KEY, JSON.stringify(state));
    
    // 触发存储事件，用于跨标签页同步
    window.dispatchEvent(new StorageEvent('storage', {
      key: FAVORITE_STATE_KEY,
      newValue: JSON.stringify(state)
    }));
  } catch (e) {
    console.error('更新收藏状态失败:', e);
  }
};

//获取收藏状态
const getPostFavoriteState = (postId: number) => {
  try {
    const stateStr = localStorage.getItem(FAVORITE_STATE_KEY);
    const state: LikeState = stateStr ? JSON.parse(stateStr) : { 
      postFavorites: {}, 
      postFavoriteCounts: {},
      postLikes: {},
      postLikeCounts: {}
    };
    
    return {
      favorited: state.postFavorites[postId] || false,
      favoriteCount: state.postFavoriteCounts[postId] || 0
    };
  } catch (e) {
    console.error('获取收藏状态失败:', e);
    return { favorited: false, favoriteCount: 0 };
  }
};

const deletePost = async (postId: number) => {
  if (!confirm('确定要删除这个帖子吗？')) {
    return;
  }
  
  try {
    await deletePostAPI(postId);
    posts.value = posts.value.filter(post => post.postId !== postId);
    alert('删除成功！');
  } catch (error: any) {
    console.error('删除帖子失败:', error);
    if (error.response?.status === 400) {
      alert('请求参数错误，请检查帖子ID格式');
    } else if (error.response?.status === 404) {
      alert('帖子不存在或已被删除');
    } else if (error.response?.status === 403) {
      alert('您没有权限删除这个帖子');
    } else {
      alert('删除帖子失败，请稍后重试');
    }
  }
};


// 监听 storage 变化
const handleStorageChange = (e: StorageEvent) => {
  if (e.key === LIKE_STATE_KEY && e.newValue) {
    try {
      const newState = JSON.parse(e.newValue);

      // 更新帖子点赞状态
      posts.value.forEach(post => {
        if (newState.postLikes.hasOwnProperty(post.postId)) {
          post.liked = newState.postLikes[post.postId];
          post.likeCount = newState.postLikeCounts[post.postId];
        }
      });
    } catch (err) {
      console.error('解析点赞状态失败:', err);
    }
  }

  if (e.key === FAVORITE_STATE_KEY && e.newValue) {
    try {
      const newState = JSON.parse(e.newValue);

      // 更新帖子收藏状态
      posts.value.forEach(post => {
        if (newState.postFavorites.hasOwnProperty(post.postId)) {
          post.favorited = newState.postFavorites[post.postId];
          post.favoriteCount = newState.postFavoriteCounts[post.postId];
        }
      });
    } catch (err) {
      console.error('解析收藏状态失败:', err);
    }
  }
};

// 更新本地帖子状态的函数 - 修正版本
const updateLocalPostStates = (newState: LikeState) => {
  // 更新帖子列表中的状态（仅适用于 ForumPage.vue）
  if (posts.value) {
    posts.value.forEach(post => {
      if (newState.postLikes.hasOwnProperty(post.postId)) {
        post.liked = newState.postLikes[post.postId];
      }
      if (newState.postLikeCounts.hasOwnProperty(post.postId)) {
        post.likeCount = newState.postLikeCounts[post.postId];
      }
    });
  }
};

// 更新本地收藏状态的函数
const updateLocalFavoriteStates = (newState: LikeState) => {
  // 更新帖子列表中的收藏状态（仅适用于 ForumPage.vue）
  if (posts.value) {
    posts.value.forEach(post => {
      if (newState.postFavorites.hasOwnProperty(post.postId)) {
        post.favorited = newState.postFavorites[post.postId];
      }
      if (newState.postFavoriteCounts.hasOwnProperty(post.postId)) {
        post.favoriteCount = newState.postFavoriteCounts[post.postId];
      }
    });
  }
};

const testDataLoading = async () => {
  console.log('=== 开始测试数据加载 ===');
  await getPosts();
  
  // 打印最原始的响应数据
  const rawPostRes = await getForumPosts({});
  const rawCategoryRes = await getForumCategories();
  
  console.log('帖子API原始响应:', rawPostRes);
  console.log('分类API原始响应:', rawCategoryRes);
  console.log('帖子API返回的data字段:', rawPostRes.data);
  console.log('分类API返回的data字段:', rawCategoryRes.data);
  
  console.log('=== 测试完成 ===');
  console.log('帖子数量:', posts.value.length);
  console.log('分类数量:', categories.value.length);
  console.log('显示帖子数量:', displayPosts.value.length);
  console.log('帖子数据详情:', JSON.stringify(posts.value));
  console.log('分类数据详情:', JSON.stringify(categories.value));
};


onUnmounted(() => {
  // ========== 清理事件 ==========
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener('scroll', handlePostParallax);

  // ========== 清理 observer ==========
  observer?.disconnect();
  observer = null;
});



const isStatus  = computed(() => {
  return (postId) => {
    // 判断ColumnValues.value是否存在
    if (!ColumnValues.value) return false;
    // 判断postId是否有效
    if (postId == null) return false;
    // 安全调用includes
    return ColumnValues.value.includes(postId);
  };
});

const loadStaus = async () =>{
    const userId = getCurrentUserId();
  const A  = await findPostStatus(userId);
  console.log("999999999999999999")
  console.log(A.data)
  ColumnValues.value = A.data
}

// 页面加载时获取数据
onMounted(async  () => {
  
  loadStaus()

   // 读取上次保存的侧边栏状态
  const savedCollapsed = localStorage.getItem('bothSidebarsCollapsed');
  if (savedCollapsed === 'true') {
    bothSidebarsCollapsed.value = true;
    sidebarLeftVisible.value = false;
    sidebarRightVisible.value = false;
  }
  // 先加载分类数据，再加载推荐帖子
  getCategories().then(async () => {
    await getPosts();
  });
  
  // 其他初始化
  loadSearchHistory();
  loadButtonPosition();
  
  // 添加事件监听
  window.addEventListener('storage', handleStorageChange);
  window.addEventListener('scroll', handlePostParallax, { passive: true });

  // 设置观察器
  observer = new IntersectionObserver(
    entries => {
      if (
        entries[0].isIntersecting &&
        hasMore.value &&
        !loading.value &&
        activeTab.value === 'recommend' // 只在推荐标签页加载更多
      ) {
        loadMorePosts();
      }
    },
    { threshold: 0.1 }
  );

  if (loadMoreRef.value) {
    observer.observe(loadMoreRef.value);
  }
});




</script>


<style scoped>
textarea::-webkit-scrollbar {
  width: 8px;
}
textarea::-webkit-scrollbar-track {
  background: transparent;
}
textarea::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}
dark textarea::-webkit-scrollbar-thumb {
  background-color: #475569;
}
/* 添加拖拽光标样式 */
.cursor-move {
  cursor: move;
}

/* 拖拽时的样式 */
.dragging {
  transition: none !important;
  transform: scale(1.1);
  box-shadow: 0 15px 30px rgba(255, 108, 50, 0.4) !important;
}

.group:hover {
  transform: translateY(-5px) scale(1.01);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* ================= 帖子滚动轻微浮动视差（保留原有样式） ================= */
.post-parallax {
  will-change: transform;
  transition: transform 0.25s ease-out;
}

/* 优化文字截断效果 */
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}

/* 微博九宫格网格适配样式 */
.grid-cols-1 .aspect-square {
  aspect-ratio: 16 / 9; /* 单图使用长方形 */
  max-height: 400px;
}

.aspect-square {
  aspect-ratio: 1 / 1; /* 多图使用正方形 */
}

/* 增加加载动画 */
@keyframes shimmer {
  0% { background-position: -1000px 0; }
  100% { background-position: 1000px 0; }
}

.shimmer-effect {
  background: linear-gradient(to right, #f0f0f0 0%, #e0e0e0 50%, #f0f0f0 100%);
  background-size: 1000px 100%;
  animation: shimmer 2s infinite linear;
}

/* 标签页按钮样式 */
.tab-navigation {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tab-button {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
}

.tab-button.active {
  background-color: #f59e0b; /* amber-500 */
  color: white;
}

.tab-button.inactive {
  background-color: #e2e8f0; /* slate-200 */
  color: #38bdf8; /* slate-700 */
}

.tab-button:hover:not(.active) {
  background-color: #cbd5e1; /* slate-300 */
}
</style>