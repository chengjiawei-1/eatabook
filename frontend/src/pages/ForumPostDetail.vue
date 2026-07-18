<template>
  <div class="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-900 transition-colors duration-300">
    <Nav />

    <main class="flex-grow pt-24 pb-16 container mx-auto px-4 sm:px-6 max-w-4xl">
      
      <!-- 顶部返回栏 -->
      <div class="mb-6 flex items-center justify-between">
        <button 
          @click="router.back()" 
          class="flex items-center gap-2 text-slate-500 hover:text-amber-600 dark:text-slate-400 dark:hover:text-amber-400 transition-colors font-medium"
        >
          <div class="w-8 h-8 rounded-full bg-white dark:bg-slate-800 shadow-sm flex items-center justify-center">
            <i class="fa-solid fa-arrow-left text-sm"></i>
          </div>
          <span>返回列表</span>
        </button>
      </div>

      <!-- Loading 状态 -->
      <div v-if="loading" class="flex flex-col items-center justify-center py-20 opacity-70">
        <div class="animate-spin rounded-full h-12 w-12 border-4 border-slate-200 border-t-amber-500"></div>
        <p class="mt-4 text-slate-500 font-medium">加载详情中...</p>
      </div>

      <!-- 文章详情卡片 -->
      <article
        v-else-if="post"
        class="bg-white dark:bg-slate-800 rounded-2xl shadow-xl overflow-hidden border border-slate-100 dark:border-slate-700 relative"
      >
        <!-- 顶部装饰条 -->
        <div class="h-2 bg-gradient-to-r from-amber-400 to-orange-500"></div>

        <div class="p-8 sm:p-10">
          <!-- 文章头部信息 -->
          <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-6 mb-8 pb-8 border-b border-slate-100 dark:border-slate-700">
            <div class="flex items-center gap-4">
              <div class="relative cursor-pointer" @click="router.push(`/user/${post.userId}`)">
                <div class="w-14 h-14 rounded-full overflow-hidden border-2 border-slate-100 dark:border-slate-600 shadow-sm">
                  <img 
                    :src="getAvatarUrl(post.userAvatar || post.avatarUrl)" 
                    class="w-full h-full object-cover"
                    alt="用户头像"
                    @error="handleImageError"
                  />
                </div>
                <!-- 楼主标识 -->
                <div class="absolute -bottom-1 -right-1 bg-amber-500 text-white text-[10px] px-1.5 py-0.5 rounded-full border-2 border-white dark:border-slate-800">
                  楼主
                </div>
              </div>
              
              <div>
                <h3 class="font-bold text-slate-800 dark:text-slate-100 text-lg cursor-pointer hover:text-amber-600 transition-colors" @click="router.push(`/user/${post.userId}`)">
                  {{ post.username }}
                </h3>
                <div class="flex items-center gap-3 text-sm text-slate-500 dark:text-slate-400 mt-1">
                  <span>{{ formatTime(post.createTime) }}</span>
                  <span v-if="post.categoryName" class="px-2 py-0.5 bg-slate-100 dark:bg-slate-700 rounded text-xs">
                    {{ post.categoryName }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 右侧操作区：关注按钮 + 点赞按钮 -->
            <div class="flex items-center gap-3">
              <!-- 【新增】关注按钮 (不显示给自己) -->
              <button 
                v-if="isLogin && currentUser?.userId !== post.userId"
                @click="handleFollow"
                :disabled="followLoading"
                class="px-4 py-2 rounded-xl text-sm font-medium transition-all shadow-sm flex items-center gap-1.5"
                :class="isFollowing 
                  ? 'bg-slate-100 text-slate-500 hover:bg-slate-200 dark:bg-slate-700 dark:text-slate-400' 
                  : 'bg-amber-500 text-white hover:bg-amber-600 hover:-translate-y-0.5'">
                <i v-if="followLoading" class="fa-solid fa-spinner fa-spin"></i>
                <template v-else>
                  <i :class="isFollowing ? 'fa-solid fa-check' : 'fa-solid fa-plus'"></i>
                  {{ isFollowing ? '已关注' : '关注' }}
                </template>
              </button>
              <!-- 点赞按钮 -->
              <button 
                @click="likePost"
                class="group flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-300 border"
                :class="post.liked 
                  ? 'bg-red-50 border-red-200 text-red-500 dark:bg-red-900/20 dark:border-red-900/50' 
                  : 'bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100 dark:bg-slate-700/50 dark:border-slate-600 dark:text-slate-300'"
              >
                <i :class="['text-lg transition-transform duration-200 group-active:scale-75', post.liked ? 'fa-solid fa-heart' : 'fa-regular fa-heart']"></i>
                <span class="font-medium">{{ post.likeCount || 0 }}</span>
              </button>

              <!-- 收藏按钮 -->
              <button 
                @click="favoritePost()"
                class="group flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-300 border"
                :class="post.favorited 
                  ? 'bg-amber-50 border-amber-200 text-amber-500 dark:bg-amber-900/20 dark:border-amber-900/50' 
                  : 'bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100 dark:bg-slate-700/50 dark:border-slate-600 dark:text-slate-300'"
              >
                <i :class="['text-lg transition-transform duration-200 group-active:scale-75', post.favorited ? 'fa-solid fa-star text-yellow-500' : 'fa-regular fa-star text-slate-400']"></i>
                <span class="font-medium">{{ post.favoriteCount || 0 }}</span>
              </button>
            </div>
          </header>

          <!-- 文章标题 -->
          <h1 class="text-3xl sm:text-4xl font-extrabold text-slate-800 dark:text-slate-100 mb-8 leading-tight">
            {{ post.title }}
          </h1>

          <!-- Markdown 内容区 -->
          <div
            class="prose prose-lg prose-slate dark:prose-invert max-w-none 
                   prose-a:text-amber-600 hover:prose-a:text-amber-500 
                   prose-img:rounded-xl prose-img:shadow-md
                   prose-headings:text-slate-800 dark:prose-headings:text-slate-100"
            v-html="renderedContent"
          />

           <!-- 帖子所有图片展示区域（轮播图模式） -->
           <div v-if="allImages && allImages.length > 0" class="mt-8">
             <!-- 轮播图容器 -->
             <div class="relative group">
               <!-- 图片显示区域 -->
               <div class="relative overflow-hidden rounded-xl shadow-lg bg-slate-100 dark:bg-slate-900">
                 <div class="relative h-96 w-full flex items-center justify-center">
                   <img
                     :src="allImages[currentImageIndex]"
                     class="max-w-full max-h-full object-contain cursor-zoom-in"
                     :alt="`帖子图片 ${currentImageIndex + 1}`"
                     @click="openImageModal(allImages[currentImageIndex])"
                   />
          
                    <!-- 图片计数 -->
                    <div class="absolute bottom-4 left-1/2 transform -translate-x-1/2 bg-black/50 text-white px-3 py-1 rounded-full text-sm backdrop-blur-sm">
                      {{ currentImageIndex + 1 }} / {{ allImages.length }}
                    </div>
                  </div>
                  
                  <!-- 左右切换按钮 -->
                  <button 
                    v-if="allImages.length > 1"
                    @click.stop="prevImage"
                    class="absolute left-4 top-1/2 transform -translate-y-1/2 bg-black/30 hover:bg-black/60 text-white w-10 h-10 flex items-center justify-center rounded-full opacity-0 group-hover:opacity-100 transition-all duration-300 backdrop-blur-sm"
                  >
                    <i class="fa-solid fa-chevron-left"></i>
                  </button>
                  <button 
                    v-if="allImages.length > 1"
                    @click.stop="nextImage"
                    class="absolute right-4 top-1/2 transform -translate-y-1/2 bg-black/30 hover:bg-black/60 text-white w-10 h-10 flex items-center justify-center rounded-full opacity-0 group-hover:opacity-100 transition-all duration-300 backdrop-blur-sm"
                  >
                    <i class="fa-solid fa-chevron-right"></i>
                  </button>
                </div>
                
                <!-- 缩略图导航 -->
                <div v-if="allImages.length > 1" class="mt-4 flex overflow-x-auto gap-2 py-2 hide-scrollbar">
                  <div
                    v-for="(img, index) in allImages"
                    :key="index"
                    :class="[
                      'flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden cursor-pointer transition-all duration-300 border-2',
                      currentImageIndex === index ? 'border-amber-500 scale-105 ring-2 ring-amber-200 dark:ring-amber-900' : 'border-transparent opacity-70 hover:opacity-100'
                    ]"
                    @click="goToImage(index)"
                  >
                    <img :src="img" class="w-full h-full object-cover" :alt="`缩略图 ${index + 1}`" />
                  </div>
               </div>
             </div>
           </div>

        </div>

        <!-- 评论区容器 -->
        <div class="bg-slate-50 dark:bg-slate-800/50 border-t border-slate-100 dark:border-slate-700 p-8 sm:p-10">
          <section class="max-w-3xl mx-auto">
            <div class="flex items-center gap-3 mb-8">
              <h2 class="text-xl font-bold text-slate-800 dark:text-slate-100">
                全部评论
              </h2>
              <span class="px-2 py-1 bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400 text-xs font-bold rounded-full">
                {{ comments.length }}
              </span>
            </div>

            <!-- 发表评论框 -->
            <div class="bg-white dark:bg-slate-800 p-1 rounded-2xl shadow-sm border border-slate-200 dark:border-slate-600 mb-10 focus-within:ring-2 focus-within:ring-amber-500/50 focus-within:border-amber-500 transition-all">
              <form v-if="isLogin" @submit.prevent="addComment">
                <textarea
                  v-model="newComment"
                  rows="3"
                  placeholder="说点什么吧..."
                  class="w-full px-4 py-3 bg-transparent border-none focus:ring-0 text-slate-700 dark:text-slate-200 resize-none outline-none"
                  required
                />
                <div class="flex justify-between items-center px-4 pb-2 pt-2 border-t border-slate-100 dark:border-slate-700">
                  <div class="text-xs text-slate-400">支持 Markdown</div>
                  <button 
                    class="px-6 py-2 bg-gradient-to-r from-amber-500 to-orange-500 text-white font-medium rounded-xl shadow-md hover:shadow-lg hover:-translate-y-0.5 transition-all duration-200">
                    发送
                  </button>
                </div>
              </form>
              <div v-else class="p-8 text-center bg-slate-50 dark:bg-slate-800 rounded-xl">
                <p class="text-slate-500 dark:text-slate-400">登录后参与讨论</p>
                <button @click="router.push('/login')" class="mt-2 text-amber-600 font-medium hover:underline">去登录</button>
              </div>
            </div>

            <!-- 评论列表 -->
            <div class="space-y-8">
              <div v-if="comments.length === 0" class="text-center py-10">
                <div class="text-4xl mb-3">🛋️</div>
                <p class="text-slate-500 dark:text-slate-400">还没有人评论，快来抢沙发！</p>
              </div>

              <div v-for="c in comments" :key="c.commentId" class="flex gap-4 group">
                <!-- 评论者头像 -->
                <div class="flex-shrink-0 cursor-pointer" @click="router.push(`/user/${c.userId}`)">
                  <img 
                    :src="getAvatarUrl(c.userAvatar || c.avatarUrl)" 
                    class="w-10 h-10 rounded-full bg-white shadow-sm object-cover"
                    alt="评论者头像"
                    @error="handleImageError"
                  />
                </div>
                
                <div class="flex-1 min-w-0">
                  <!-- 评论气泡 -->
                  <div class="bg-white dark:bg-slate-800 rounded-2xl rounded-tl-none p-4 shadow-sm border border-slate-100 dark:border-slate-700 hover:shadow-md transition-shadow">
                    <div class="flex justify-between items-center mb-2">
                      <div class="flex items-center gap-2">
                        <span class="font-bold text-slate-700 dark:text-slate-200 text-sm cursor-pointer hover:text-amber-600" @click="router.push(`/user/${c.userId}`)">
                          {{ c.username }}
                        </span>
                        <span v-if="c.username === post.username" class="px-1.5 py-0.5 bg-amber-100 dark:bg-amber-900 text-amber-700 dark:text-amber-400 text-[10px] rounded border border-amber-200 dark:border-amber-800">
                          楼主
                        </span>
                      </div>
                      <span class="text-xs text-slate-400">{{ formatTime(c.createTime) }}</span>
                    </div>
                    
                    <p class="text-slate-600 dark:text-slate-300 text-sm leading-relaxed whitespace-pre-wrap">{{ c.content }}</p>

                    <div class="mt-3 flex justify-end gap-2">
                      <!-- 评论点赞按钮 -->
                      <button
                        v-if="isLogin"
                        @click="likeComment(c)"
                        class="text-xs font-medium flex items-center gap-1 transition-colors"
                        :class="c.liked 
                          ? 'text-red-500 hover:text-red-600' 
                          : 'text-slate-400 hover:text-amber-600'">
                        <i :class="['text-xs', c.liked ? 'fa-solid fa-heart' : 'fa-regular fa-heart']"></i>
                        {{ c.likeCount || 0 }}
                      </button>

                      <button
                        v-if="isLogin"
                        @click="toggleReply(c.commentId)"
                        class="text-xs font-medium text-slate-400 hover:text-amber-600 flex items-center gap-1 transition-colors"
                      >
                        <i class="fa-solid fa-reply"></i> 回复
                      </button>
                      <button
                        v-if="isLogin && currentUser && String(c.userId) === String(currentUser.userId)"
                        @click="deleteComment(c.commentId)"
                        class="text-xs font-medium text-red-400 hover:text-red-600 flex items-center gap-1 transition-colors"
                      >
                        <i class="fa-solid fa-trash"></i> 删除
                      </button>
                    </div>
                  </div>

                  <!-- 回复输入框 -->
                  <div v-if="replyTo === c.commentId" class="mt-4 ml-2 animate-in fade-in slide-in-from-top-2 duration-200">
                    <form @submit.prevent="submitReply(c.commentId)" class="flex gap-2">
                      <input
                        v-model="replyContent"
                        type="text"
                        :placeholder="`回复 @${c.username}...`"
                        class="flex-1 px-4 py-2 bg-white dark:bg-slate-800 border border-amber-300 dark:border-amber-700 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500/50 dark:text-white"
                        required
                        autoFocus
                      />
                      <button class="px-4 py-2 bg-amber-600 text-white text-sm rounded-lg hover:bg-amber-700 whitespace-nowrap">
                        回复
                      </button>
                    </form>
                  </div>

                  <!-- 子回复 -->
                  <div v-if="c.replies?.length" class="mt-4 space-y-3 pl-4 border-l-2 border-slate-200 dark:border-slate-700 ml-2">
                    <div v-for="r in c.replies" :key="r.commentId" class="bg-slate-100/50 dark:bg-slate-800/50 p-3 rounded-lg group/reply">
                      <div class="flex items-center justify-between mb-1">
                        <div class="flex items-center gap-2">
                          <span class="font-bold text-slate-700 dark:text-slate-300 text-xs cursor-pointer hover:text-amber-600" @click="router.push(`/user/${r.userId}`)">{{ r.username }}</span>
                          <span class="text-slate-400 text-xs transform scale-90">回复</span>
                          <span class="font-bold text-amber-600 dark:text-amber-500 text-xs">
                            @{{ r.replyUsername || c.username }}
                          </span>
                        </div>
                        <button 
                          v-if="isLogin"
                          @click="toggleReply(r.commentId)" 
                          class="opacity-0 group-hover/reply:opacity-100 transition-opacity text-xs text-slate-400 hover:text-amber-600"
                        >
                          回复
                        </button>
                      </div>
                      <p class="text-slate-600 dark:text-slate-400 text-sm">{{ r.content }}</p>
                      <div class="mt-2 flex justify-end gap-2">
                        <!-- 子回复点赞按钮 -->
                        <button
                          v-if="isLogin"
                          @click="likeComment(r)"
                          class="text-xs font-medium flex items-center gap-1 transition-colors"
                          :class="r.liked 
                            ? 'text-red-500 hover:text-red-600' 
                            : 'text-slate-400 hover:text-amber-600'">
                          <i :class="['fa-solid text-xs', r.liked ? 'fa-heart' : 'fa-heart']"></i>
                          {{ r.likeCount || 0 }}
                        </button>

                        <button
                          v-if="isLogin && currentUser && String(r.userId) === String(currentUser.userId)"
                          @click="deleteComment(r.commentId)"
                          class="text-xs font-medium text-red-400 hover:text-red-600 flex items-center gap-1 transition-colors"
                        >
                          <i class="fa-solid fa-trash"></i> 删除
                        </button>
                      </div>
                      <div v-if="replyTo === r.commentId" class="mt-2">
                        <form @submit.prevent="submitReply(c.commentId, r.username)" class="flex gap-2">
                          <input
                            v-model="replyContent"
                            type="text"
                            :placeholder="`回复 @${r.username}...`"
                            class="flex-1 px-3 py-1.5 bg-white dark:bg-slate-900 border border-amber-300 rounded text-xs focus:outline-none dark:text-white"
                            required
                            autoFocus
                          />
                          <button class="px-3 py-1.5 bg-amber-600 text-white text-xs rounded hover:bg-amber-700">发送</button>
                        </form>
                      </div>
                    </div>
                  </div>

                </div>
              </div>

            </div>
          </section>
        </div>
      </article>

      <div v-else class="text-center py-20 bg-white dark:bg-slate-800 rounded-2xl shadow">
        <div class="text-6xl mb-4">🤔</div>
        <h3 class="text-xl text-slate-700 dark:text-slate-300 font-medium">帖子走丢了</h3>
        <p class="text-slate-500 mt-2">它可能已被删除或从未存在过。</p>
        <button @click="router.back()" class="mt-6 text-amber-600 font-medium hover:underline">返回列表</button>
      </div>
    </main>

    <Footer />

    <!-- 图片大图查看 Modal -->
    <div 
      class="image-modal" 
      :class="{ 'active': imageModal }" 
      @click="imageModal = false"
    >
      <div class="image-modal-content relative" @click.stop>
        <button 
          @click="imageModal = false"
          class="absolute -top-10 right-0 text-white hover:text-amber-500 transition-colors"
        >
          <i class="fa-solid fa-xmark text-3xl"></i>
        </button>
        <img :src="currentImage" alt="Large view" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { findPostStatus } from '@/api/forums'
import { useRoute, useRouter } from 'vue-router'
import Nav from '@/components/Nav.vue'
import Footer from '@/components/Footer.vue'
import { getPostLikeState, updatePostLikeState, getCommentLikeState, updateCommentLikeState } from '@/utils/likeState';
import {
  getPostDetail,
  getPostComments,
  publishComment,
  togglePostLike,
  togglePostFavorite,
  toggleCommentLike,
  deleteCommentAPI,
  getUserById,
  getCategoryById
} from '@/api/forums'
// 引入关注相关的 API
import { checkFollowStatusAPI, followUserAPI, unfollowUserAPI } from '@/api/user';
import { useAuthStore } from '@/stores/auth'
import { marked } from 'marked'
import { getAvatarUrl, getForumImageUrl } from '@/utils/imageUtils';

const route = useRoute()
const router = useRouter()
const postId = Number(route.params.id)

const ColumnValues = ref([])

const authStore = useAuthStore()
const isLogin = computed(() => authStore.isAuthenticated)
const currentUser = computed(() => authStore.user)
const LIKE_STATE_KEY = 'forum_like_state'
const FAVORITE_STATE_KEY = 'forum_favorite_state'

const loading = ref(true)
const post = ref<any>(null)
const comments = ref<any[]>([])

const newComment = ref('')
const replyContent = ref('')
const replyTo = ref<number | null>(null)

// 关注相关状态
const isFollowing = ref(false);
const followLoading = ref(false);

// 检查关注状态
const checkFollowStatus = async () => {
  if (!isLogin.value || !post.value?.userId || !currentUser.value) return;
  // 不要关注自己
  if (currentUser.value.userId === post.value.userId) return;

  try {
    const res = await checkFollowStatusAPI(currentUser.value.userId, post.value.userId);
    // 处理可能的返回结构差异
    isFollowing.value = !!(res.data?.data || res.data); 
  } catch (e) {
    console.error('检查关注状态失败', e);
  }
};

// 关注/取关处理函数
const handleFollow = async () => {
  if (!isLogin.value) {
    router.push('/login');
    return;
  }
  
  if (followLoading.value) return;
  followLoading.value = true;
  
  try {
    if (isFollowing.value) {
      await unfollowUserAPI(post.value.userId);
      isFollowing.value = false;
    } else {
      await followUserAPI(post.value.userId);

      isFollowing.value = true;
    }
  } catch (e) {
    console.error('操作失败', e);
    alert('操作失败，请重试');
  } finally {
    followLoading.value = false;
  }
};

// 图片预览相关
const imageModal = ref(false)
const currentImage = ref('')
// 图片轮播索引
const currentImageIndex = ref(0)

const nextImage = () => {
  if (allImages.value.length > 0) {
    currentImageIndex.value = (currentImageIndex.value + 1) % allImages.value.length
  }
}

const prevImage = () => {
  if (allImages.value.length > 0) {
    currentImageIndex.value = currentImageIndex.value === 0 
      ? allImages.value.length - 1 
      : currentImageIndex.value - 1
  }
}

const goToImage = (index: number) => {
  currentImageIndex.value = index
}

const openImageModal = (imgUrl: string) => {
  currentImage.value = imgUrl
  imageModal.value = true
}

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iODAiIHI9IjQwIiBmaWxsPSIjZTllOWU5Ii8+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iMTUwIiByPSIzMCIgZmlsbD0iI2U5ZTllOSIvPgo8L3N2Zz4='

const formatTime = (t: string) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const renderedContent = computed(() => {
  if (!post.value || !post.value.content) return ''
  return marked(post.value.content)
})

const allImages = computed(() => {
  if (!post.value) return []
  const result: string[] = []
  if (post.value.images && Array.isArray(post.value.images)) {
    result.push(...post.value.images)
  }
  return result
})

const buildTree = (list: any[]) => {
  const map = new Map<number, any>()
  const roots: any[] = []

  list.forEach(item => {
    item.replies = []
    map.set(item.commentId, item)
  })

  list.forEach(item => {
    if (item.parentId) {
      const parent = map.get(item.parentId)
      if (parent) parent.replies.push(item)
    } else {
      roots.push(item)
    }
  })

  return roots
}
const fetchPostDetail = async () => {
  try {
    console.log('=== 帖子详情调试开始 ===');
    console.log('帖子ID:', postId);
    
    const response = await getPostDetail(postId);
    console.log('API响应:', response);
    
    let postData = null;
    if (response.data && response.data.data) {
      postData = response.data.data;
    } else if (response.data) {
      postData = response.data;
    } else {
      postData = response;
    }
    
    console.log('原始帖子数据:', postData);
    console.log('用户ID:', postData.userId);
    console.log('用户名字段检查:');
    console.log('username:', postData.username);
    console.log('userName:', postData.userName);
    console.log('author:', postData.author);
    
    console.log('头像字段检查:');
    console.log('userAvatar:', postData.userAvatar);
    console.log('avatarUrl:', postData.avatarUrl);
    console.log('avatar_url:', postData.avatar_url);
    console.log('avatar:', postData.avatar);
    
    console.log('所有字段:', Object.keys(postData));
    
    // 设置帖子数据
    post.value = {
      ...postData,
      username: postData.username || postData.userName || postData.author || `用户${postData.userId}`,
      userAvatar: postData.userAvatar || postData.avatarUrl || postData.avatar_url || postData.avatar || defaultAvatar
    };
    
    console.log('处理后的帖子数据:', post.value);
    console.log('=== 帖子详情调试结束 ===');
    
  } catch (error) {
    console.error('获取帖子详情失败:', error);
  }
};


const loadData = async () => {
  loading.value = true
  try {
    console.log('=== 帖子详情页调试开始 ===');
    console.log('帖子ID:', postId);
    
    // 获取帖子详情
    const postRes = await getPostDetail(postId)
    console.log('【调试】帖子详情API响应:', postRes);
    
    // 兼容处理：有些后端返回 res.data.data，有些直接返回 res.data
    let postDataResponse = postRes?.data?.data || postRes?.data || postRes;
    console.log('【调试】帖子详情数据:', postDataResponse);
    
    if (!postDataResponse || typeof postDataResponse !== 'object') {
      console.error('未能获取到帖子基础数据');
      return;
    }
    
    // 将postDataResponse赋值给postData变量，确保后续代码可以访问
    const postData = postDataResponse;
    
    // 调试用户信息字段
    console.log('【调试】用户ID:', postData.userId);
    console.log('【调试】用户名字段检查:');
    console.log('username:', postData.username);
    console.log('userName:', postData.userName);
    console.log('author:', postData.author);
    console.log('nickname:', postData.nickname);
    
    console.log('【调试】头像字段检查:');
    console.log('userAvatar:', postData.userAvatar);
    console.log('avatarUrl:', postData.avatarUrl);
    console.log('avatar_url:', postData.avatar_url);
    console.log('avatar:', postDataResponse.avatar);
    console.log('avatarUrl:', postDataResponse.avatarUrl);
    
    console.log('【调试】所有字段:', Object.keys(postDataResponse));
    // 初始赋值 
    post.value = {
      ...postDataResponse,
      username: postDataResponse.username || postDataResponse.userName || postDataResponse.author || postDataResponse.nickname || '匿名用户',
      userAvatar: postDataResponse.userAvatar || postDataResponse.avatarUrl || postDataResponse.avatar_url || postDataResponse.avatar || defaultAvatar,
      postId: postDataResponse.postId || postId,
      liked: !!postDataResponse.liked, 
      likeCount: postDataResponse.likeCount || 0,
      favorited: !!postDataResponse.favorited,
      favoriteCount: postDataResponse.favoriteCount || 0
    }
    
    console.log('【调试】初始设置后的帖子数据:', post.value);

    // 补全用户信息
    if (postDataResponse.userId) {
      console.log('【调试】开始查询用户信息，用户ID:', postDataResponse.userId);
      try {
        const userRes = await getUserById(postDataResponse.userId)
        console.log('【调试】用户信息API响应:', userRes);
        
        const userInfoData = userRes?.data?.data || userRes?.data || userRes;
        console.log('【调试】用户信息数据:', userInfoData);
        console.log('【调试】用户信息原始响应结构:', userRes);
        
        if (userInfoData && typeof userInfoData === 'object') {
          console.log('【调试】用户信息字段检查:');
          console.log('username:', userInfoData.username);
          console.log('userName:', userInfoData.userName);
          console.log('nickname:', userInfoData.nickname);
          console.log('avatarUrl:', userInfoData.avatarUrl);
          console.log('avatar:', userInfoData.avatar);
          console.log('userAvatar:', userInfoData.userAvatar);
          console.log('用户信息数据类型:', typeof userInfoData);
          
          // 只有当获取到新数据时才赋值，否则保留 postDataResponse 里的旧值
        post.value.username = userInfoData.username || userInfoData.userName || userInfoData.nickname || postDataResponse.username || '未知用户';
        
        // 兼容多种头像字段名，添加更严格的空值检查
        // 使用三元运算符确保返回的是实际的URL字符串，而不是布尔值
        post.value.userAvatar = getAvatarUrl(
          userInfoData.avatarUrl || userInfoData.userAvatar || userInfoData.avatar_url || userInfoData.avatar || postDataResponse.userAvatar || postDataResponse.avatarUrl || defaultAvatar
        );
          
          console.log('【调试】更新后的用户信息:');
          console.log('username:', post.value.username);
          console.log('userAvatar:', post.value.userAvatar);
        } else {
          console.warn('【调试】用户信息数据为空或无效');
          console.log('用户信息数据类型:', typeof userInfoData);
          console.log('用户信息数据值:', userInfoData);
        }
      } catch (error) {
        console.error('详情页获取用户信息失败:', error)
      }
    } else {
      console.warn('【调试】帖子数据中没有userId字段');
    }
    // 同步收藏状态 
    await loadStaus();
    // 检查关注状态
    if (isLogin.value && post.value.userId) {
      checkFollowStatus();
    }

    // 补全分类信息
    if (post.value.categoryId) {
      try {
        const categoryRes = await getCategoryById(post.value.categoryId)
        // 同样注意解析层级
        const categoryInfo = categoryRes?.data?.data || categoryRes?.data || categoryRes;
        if (categoryInfo) {
          post.value.categoryName = categoryInfo.categoryName || categoryInfo.name || '未知分类';
        }
      } catch (error) {
        console.error('获取分类信息失败', error)
      }
    }

    // 图片解析 
    if (typeof post.value.images === 'string') {
      try { post.value.images = JSON.parse(post.value.images) } catch { post.value.images = [] }
    }
    if (Array.isArray(post.value.images)) {
      post.value.images = post.value.images.map((img: string) => getForumImageUrl(img))
    }

    // 评论加载 
    const commentRes = await getPostComments({ postId, pageNum: 1, pageSize: 100 })
    const commentRawData = commentRes?.data?.data || commentRes?.data || [];
    const list = Array.isArray(commentRawData) ? commentRawData : (commentRawData.records || []);
    
    const commentsWithUserInfo = await Promise.all(list.map(async (comment: any) => {
      const cachedCommentLikeState = getCommentLikeState(comment.commentId);
      const commentWithDefaults = {
        ...comment,
        liked: cachedCommentLikeState.liked !== undefined ? cachedCommentLikeState.liked : (comment.liked || false),
        likeCount: cachedCommentLikeState.likeCount !== undefined ? cachedCommentLikeState.likeCount : (comment.likeCount || 0)
      }
      
      console.log('【调试】原始评论数据:', comment);
      console.log('【调试】评论用户ID:', comment.userId);
      console.log('【调试】评论用户名:', comment.username);
      console.log('【调试】评论头像:', comment.userAvatar);
      
      if (comment.userId) {
        try {
          const uRes = await getUserById(comment.userId)
          console.log('【调试】用户信息API响应:', uRes);
          
          // 兼容处理不同的响应结构
          const uInfo = uRes?.data?.data || uRes?.data || uRes;
          console.log('【调试】用户信息数据:', uInfo);
          
          if (uInfo && typeof uInfo === 'object') {
            commentWithDefaults.username = uInfo.username || uInfo.userName || uInfo.nickname || comment.username || '未知用户';
            commentWithDefaults.userAvatar = (uInfo.avatarUrl && uInfo.avatarUrl !== 'null') ? uInfo.avatarUrl : 
                                           (uInfo.avatar && uInfo.avatar !== 'null') ? uInfo.avatar : 
                                           (uInfo.userAvatar && uInfo.userAvatar !== 'null') ? uInfo.userAvatar : 
                                           (comment.userAvatar && comment.userAvatar !== 'null') ? comment.userAvatar : 
                                           defaultAvatar;
            
            console.log('【调试】更新后的评论用户信息:', {
              username: commentWithDefaults.username,
              userAvatar: commentWithDefaults.userAvatar
            });
          } else {
            console.warn('【调试】用户信息数据为空或无效');
            // 使用原始评论数据中的信息
            commentWithDefaults.username = comment.username || '未知用户';
            commentWithDefaults.userAvatar = (comment.userAvatar && comment.userAvatar !== 'null') ? comment.userAvatar : defaultAvatar;
          }
        } catch (error) {
          console.error('【调试】获取评论用户信息失败:', error);
          // 出错时使用原始数据
          commentWithDefaults.username = comment.username || '未知用户';
          commentWithDefaults.userAvatar = (comment.userAvatar && comment.userAvatar !== 'null') ? comment.userAvatar : defaultAvatar;
        }
      } else {
        console.warn('【调试】评论数据中没有userId字段');
        commentWithDefaults.username = comment.username || '未知用户';
        commentWithDefaults.userAvatar = (comment.userAvatar && comment.userAvatar !== 'null') ? comment.userAvatar : defaultAvatar;
      }
      
      console.log('【调试】最终评论数据:', commentWithDefaults);
      return commentWithDefaults
    }))
    comments.value = buildTree(commentsWithUserInfo)

  } catch (e) {
    console.error('loadData 发生严重错误:', e)
  } finally {
    loading.value = false
  }
}

const addComment = async () => {
  if (!newComment.value.trim()) return
  try {
    await publishComment({ postId, content: newComment.value, parentId: undefined })
    newComment.value = ''
    loadData()
  } catch (e) {
    console.error('评论失败', e)
  }
}

const toggleReply = (id: number) => {
  replyContent.value = ''
  replyTo.value = replyTo.value === id ? null : id
}

const submitReply = async (parentId: number) => {
  if (!replyContent.value.trim()) return
  try {
    await publishComment({ postId, content: replyContent.value, parentId })
    replyContent.value = ''
    replyTo.value = null
    loadData()
  } catch (e) {
    console.error('回复失败', e)
  }
}

const deleteComment = async (commentId: number) => {
  if (!confirm('确定删除这条评论吗？')) return
  try {
    await deleteCommentAPI(commentId)
    loadData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// 详情页点赞
const unwrapForumMutationResponse = (res: any) => {
  const body = res?.code !== undefined ? res : res?.data;
  return {
    code: body?.code,
    data: body?.data || {},
    msg: body?.msg
  };
};

const likePost = async () => {
  try {
    const res = await togglePostLike(post.value.postId);
    const { code, data } = unwrapForumMutationResponse(res);
    if (code === 200) {
      // 直接覆盖当前页面的唯一 post 对象
      post.value.liked = data.liked;
      post.value.likeCount = data.likeCount;
      
      updatePostLikeState(post.value.postId, post.value.liked, post.value.likeCount);
    }
  } catch (error) {
    alert('点赞失败');
  }
};

// 详情页收藏
const favoritePost = async () => {
  try {
    const res = await togglePostFavorite(post.value.postId);
    const { code, data } = unwrapForumMutationResponse(res);
    if (code === 200) {
      // 使用 Object.assign 确保Vue响应性更新
      Object.assign(post.value, {
        favorited: data.favorited,
        favoriteCount: data.favoriteCount
      });
      
      // 强制触发UI更新
      post.value = { ...post.value };
      
      // 更新本地存储状态
      updatePostFavoriteState(post.value.postId, post.value.favorited, post.value.favoriteCount);
      
      console.log('收藏状态更新:', {
        favorited: post.value.favorited,
        favoriteCount: post.value.favoriteCount
      });
    }
  } catch (error) {
    alert('收藏失败');
  }
};

// 更新收藏状态到本地存储
const updatePostFavoriteState = (postId: number, favorited: boolean, favoriteCount: number) => {
  try {
    const stateStr = localStorage.getItem(FAVORITE_STATE_KEY);
    const state: any = stateStr ? JSON.parse(stateStr) : { 
      postFavorites: {}, 
      postFavoriteCounts: {}
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

// 获取收藏状态
const getPostFavoriteState = (postId: number) => {
  try {
    const stateStr = localStorage.getItem(FAVORITE_STATE_KEY);
    const state: any = stateStr ? JSON.parse(stateStr) : { 
      postFavorites: {}, 
      postFavoriteCounts: {}
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

const likeComment = async (comment: any) => {
  if (!isLogin.value) {
    alert('请先登录');
    return;
  }
  
  if (!comment.commentId) return;
  
  const previousLiked = comment.liked;
  const previousLikeCount = comment.likeCount;
  
  try {
    comment.liked = !comment.liked;
    comment.likeCount = comment.liked 
      ? (comment.likeCount || 0) + 1 
      : Math.max(0, (comment.likeCount || 0) - 1);
    
    await toggleCommentLike(comment.commentId);
    // 更新评论点赞状态到缓存
    updateCommentLikeState(comment.commentId, comment.liked, comment.likeCount);
  } catch (error: any) {
    console.error('评论点赞失败:', error);
    comment.liked = previousLiked;
    comment.likeCount = previousLikeCount;
    if (error.response?.status === 401) {
      alert('登录已过期，请重新登录');
    }
  }
};

const handleImageError = (e: any) => {
  e.target.src = defaultAvatar
}

// 监听 storage 变化
const handleStorageChange = (e: StorageEvent) => {
  if (e.key === LIKE_STATE_KEY && e.newValue) {
    try {
      const newState = JSON.parse(e.newValue);
      
      // 更新帖子点赞状态
      if (post.value && newState.postLikes.hasOwnProperty(postId)) {
        post.value.liked = newState.postLikes[postId];
        post.value.likeCount = newState.postLikeCounts[postId];
      }
    } catch (err) {
      console.error('解析点赞状态失败:', err);
    }
  }

  if (e.key === FAVORITE_STATE_KEY && e.newValue) {
    try {
      const newState = JSON.parse(e.newValue);
      
      // 更新帖子收藏状态
      if (post.value && newState.postFavorites && newState.postFavorites.hasOwnProperty(postId)) {
        post.value.favorited = newState.postFavorites[postId];
        post.value.favoriteCount = newState.postFavoriteCounts[postId];
      }
    } catch (err) {
      console.error('解析收藏状态失败:', err);
    }
  }
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

const loadStaus = async () =>{
  try {
    const userId = getCurrentUserId();
    if (!userId) {
      console.warn('未找到当前用户ID，跳过状态加载');
      return;
    }
    
    console.log('开始加载帖子状态，用户ID:', userId);
    
    // 暂时使用空数组，避免影响其他功能
    ColumnValues.value = [];
    console.log('状态加载完成（使用空数组）');
    
  } catch (error) {
    console.error('加载帖子状态失败:', error);
    // 发生错误时设置为空数组，避免影响其他功能
    ColumnValues.value = [];
  }
}

const isStatus  = computed(() => {
  return (postId) => {
    if (!ColumnValues.value) return false;
    if (postId == null) return false;
    return ColumnValues.value.includes(postId);
  };
});

onMounted(() => {
  window.addEventListener('storage', handleStorageChange);
  // 先加载主要数据，状态加载放在后面避免阻塞
  loadData();
  // 延迟加载状态，避免API错误影响主要功能
  setTimeout(() => {
    loadStaus();
  }, 1000);
});

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
});
</script>

<style>
/* Markdown 样式 */
.prose h1, .prose h2, .prose h3 {
  margin-top: 1.5em;
  margin-bottom: 0.8em;
  font-weight: 700;
}
.prose p {
  margin-bottom: 1.25em;
  line-height: 1.8;
}
.prose ul {
  list-style-type: disc;
  padding-left: 1.5em;
}
.prose blockquote {
  border-left-color: #f59e0b; /* Amber-500 */
  background-color: #fffbeb; /* Amber-50 */
  padding: 1rem;
  border-radius: 0.5rem;
  font-style: italic;
}
.dark .prose blockquote {
  background-color: #451a03; /* Dark Amber */
  color: #fbbf24;
}

/* 图片查看 Modal 样式 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}

.image-modal.active {
  opacity: 1;
  visibility: visible;
}

.image-modal-content {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.image-modal-content img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.5);
}
</style>
