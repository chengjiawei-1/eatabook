<template>
  <div class="max-w-4xl mx-auto px-3 sm:px-6 pb-6 antialiased">
    
    <!-- 顶部标签切换 -->
    <div class="mb-8 flex justify-center">
      <div class="inline-flex bg-[#f2ece4] dark:bg-slate-800 rounded-2xl p-1.5 border-2 border-[#e3d5ca]">
        <button
          @click="activeTab = 'dishes'"
          :class="[
            'px-6 py-2.5 rounded-xl text-sm font-serif font-bold transition-all duration-300 flex items-center gap-2',
            activeTab === 'dishes'
              ? 'bg-[#8a6d51] text-white shadow-lg'
              : 'text-[#8a6d51] hover:bg-[#e3d5ca]/50'
          ]"
        >
          <i class="fa-solid fa-utensils"></i>
          食疗良方
          <span v-if="dishFavorites.length > 0" class="ml-1 text-xs bg-white/20 px-2 py-0.5 rounded-full">
            {{ dishFavorites.length }}
          </span>
        </button>
        <button
          @click="activeTab = 'posts'"
          :class="[
            'px-6 py-2.5 rounded-xl text-sm font-serif font-bold transition-all duration-300 flex items-center gap-2',
            activeTab === 'posts'
              ? 'bg-[#8a6d51] text-white shadow-lg'
              : 'text-[#8a6d51] hover:bg-[#e3d5ca]/50'
          ]"
        >
          <i class="fa-solid fa-scroll"></i>
          典藏帖子
          <span v-if="postFavorites.length > 0" class="ml-1 text-xs bg-white/20 px-2 py-0.5 rounded-full">
            {{ postFavorites.length }}
          </span>
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-24">
      <i class="fa-solid fa-yin-yang fa-spin text-4xl text-[#d4a373] mb-6"></i>
      <p class="text-lg text-[#8a6d51] font-serif tracking-widest animate-pulse">正在翻阅您的私人档案...</p>
    </div>

    <!-- 食疗良方标签页 -->
    <div v-else-if="activeTab === 'dishes'">
      <!-- 空状态 -->
      <div v-if="dishFavorites.length === 0" class="text-center py-20 bg-white dark:bg-slate-800 border-2 border-[#f2ece4] rounded-3xl">
        <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-[#fdfaf7] flex items-center justify-center border-2 border-dashed border-[#d4a373]/30">
          <i class="fa-regular fa-folder-open text-2xl text-[#8a6d51]"></i>
        </div>
        <h3 class="text-lg font-serif font-bold text-[#6d4c3d] mb-2">匣中尚无良方</h3>
        <p class="text-[#a67c52] mb-6">您还没有收藏任何食疗内容</p>
        <router-link
          to="/recipes"
          class="px-6 py-3 bg-[#8a6d51] text-white rounded-2xl hover:bg-[#6d4c3d] transition-all font-bold shadow-lg shadow-[#8a6d51]/20 inline-block"
        >
          去食谱页看看
        </router-link>
      </div>

      <!-- 菜品收藏网格 -->
      <div v-else>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div
            v-for="item in paginatedDishFavorites"
            :key="item.id"
            class="group relative bg-white dark:bg-slate-800 border-2 border-[#f2ece4] dark:border-slate-700 rounded-3xl overflow-hidden hover:shadow-2xl hover:border-[#d4a373] transition-all cursor-pointer"
            @click="goToDishDetail(item)"
          >
            <div class="relative h-52 overflow-hidden bg-gray-50">
              <img
                :src="item.image"
                :alt="item.name"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                @error="(e) => (e.target as HTMLImageElement).src = '/placeholder-dish.png'"
              />
              <div class="absolute top-4 left-4">
                <span class="px-3 py-1 bg-white/90 backdrop-blur-sm text-[#8a6d51] text-xs font-bold rounded-lg border border-[#f2ece4] shadow-sm">
                  {{ item.category || '食谱' }}
                </span>
              </div>
              <button
                @click.stop="removeDishFavorite(item.id, item.collectionId)"
                class="absolute top-4 right-4 w-9 h-9 rounded-xl bg-white/90 backdrop-blur-sm flex items-center justify-center text-red-400 hover:text-red-600 hover:bg-white transition-all shadow-sm"
              >
                <i class="fa-solid fa-heart"></i>
              </button>
            </div>

            <div class="p-5">
              <h3 class="font-serif font-black text-xl text-[#6d4c3d] dark:text-[#deb887] mb-2 line-clamp-1">
                {{ item.name }}
              </h3>
              <p class="text-sm text-[#a67c52] dark:text-gray-400 mb-4 line-clamp-2 leading-relaxed">
                {{ item.description }}
              </p>
              <div class="flex items-center justify-between pt-3 border-t border-dashed border-[#f2ece4]">
                <span class="text-[11px] text-[#c4a484] flex items-center gap-1 font-bold uppercase tracking-wider">
                  <i class="fa-regular fa-clock"></i> 收藏于 {{ item.collectionTime }}
                </span>
                <i class="fa-solid fa-arrow-right-long text-[#e3d5ca] group-hover:text-[#d4a373] group-hover:translate-x-1 transition-all"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="dishTotalPages > 1" class="mt-10 flex items-center justify-center gap-6">
          <button
            @click="dishCurrentPage--"
            :disabled="dishCurrentPage === 1"
            class="flex items-center gap-2 px-5 py-2 border-2 border-[#f2ece4] rounded-xl text-[#8a6d51] font-serif font-bold hover:border-[#d4a373] hover:bg-[#fdfaf7] disabled:opacity-30 disabled:pointer-events-none transition-all"
          >
            <i class="fa-solid fa-chevron-left"></i> 上一页
          </button>
          
          <div class="text-[#8a6d51] font-serif font-bold bg-[#fdfaf7] px-4 py-2 rounded-lg border border-[#f2ece4] min-w-[80px] text-center">
            {{ dishCurrentPage }} / {{ dishTotalPages }}
          </div>
          
          <button
            @click="dishCurrentPage++"
            :disabled="dishCurrentPage === dishTotalPages"
            class="flex items-center gap-2 px-5 py-2 border-2 border-[#f2ece4] rounded-xl text-[#8a6d51] font-serif font-bold hover:border-[#d4a373] hover:bg-[#fdfaf7] disabled:opacity-30 disabled:pointer-events-none transition-all"
          >
            下一页 <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 典藏帖子标签页 -->
    <div v-else-if="activeTab === 'posts'">
      <!-- 空状态 -->
      <div v-if="postFavorites.length === 0" class="text-center py-20 bg-white dark:bg-slate-800 border-2 border-[#f2ece4] rounded-3xl">
        <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-[#fdfaf7] flex items-center justify-center border-2 border-dashed border-[#d4a373]/30">
          <i class="fa-regular fa-comments text-2xl text-[#8a6d51]"></i>
        </div>
        <h3 class="text-lg font-serif font-bold text-[#6d4c3d] mb-2">暂无典藏帖子</h3>
        <p class="text-[#a67c52] mb-6">您还没有收藏任何论坛帖子</p>
        <router-link
          to="/forum"
          class="px-6 py-3 bg-[#8a6d51] text-white rounded-2xl hover:bg-[#6d4c3d] transition-all font-bold shadow-lg shadow-[#8a6d51]/20 inline-block"
        >
          去论坛看看
        </router-link>
      </div>

      <!-- 帖子收藏条状列表 -->
      <div v-else class="space-y-4">
        <div
          v-for="item in paginatedPostFavorites"
          :key="item.postId"
          class="group bg-white dark:bg-slate-800 border-2 border-[#f2ece4] dark:border-slate-700 rounded-2xl p-4 hover:shadow-xl hover:border-[#d4a373] transition-all cursor-pointer"
          @click="goToPostDetail(item)"
        >
          <div class="flex items-center gap-4">
            <!-- 用户头像 -->
            <div class="relative flex-shrink-0">
              <div class="w-14 h-14 rounded-full overflow-hidden border-2 border-[#f2ece4] dark:border-slate-600 shadow-md">
                <img
                  :src="item.userAvatar || defaultAvatar"
                  :alt="item.username"
                  class="w-full h-full object-cover"
                  @error="(e) => (e.target as HTMLImageElement).src = defaultAvatar"
                />
              </div>
              <div class="absolute -bottom-1 -right-1 w-5 h-5 bg-[#8a6d51] rounded-full flex items-center justify-center border-2 border-white dark:border-slate-800">
                <i class="fa-solid fa-user text-[10px] text-white"></i>
              </div>
            </div>

            <!-- 内容区域 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-serif font-bold text-[#6d4c3d] dark:text-[#deb887] text-base">
                  {{ item.username }}
                </span>
                <span class="text-xs text-[#a67c52] px-2 py-0.5 bg-[#f2ece4] dark:bg-slate-700 rounded-full">
                  {{ item.categoryName || '论坛' }}
                </span>
              </div>
              <h3 class="font-serif font-bold text-lg text-[#4a3728] dark:text-slate-200 line-clamp-1 group-hover:text-[#8a6d51] transition-colors">
                {{ item.title }}
              </h3>
              <p class="text-sm text-[#8a6d51] dark:text-gray-400 line-clamp-1 mt-1">
                {{ item.content }}
              </p>
            </div>

            <!-- 右侧操作区 -->
            <div class="flex flex-col items-end gap-2 flex-shrink-0">
              <button
                @click.stop="removePostFavorite(item.postId)"
                class="w-8 h-8 rounded-full bg-[#fdfaf7] dark:bg-slate-700 flex items-center justify-center text-red-400 hover:text-red-600 hover:bg-red-50 transition-all"
                title="取消收藏"
              >
                <i class="fa-solid fa-star text-sm"></i>
              </button>
              <span class="text-[11px] text-[#c4a484] font-serif">
                {{ item.collectionTime }}
              </span>
            </div>
          </div>
        </div>

        <!-- 帖子分页 -->
        <div v-if="postTotalPages > 1" class="mt-10 flex items-center justify-center gap-6">
          <button
            @click="postCurrentPage--"
            :disabled="postCurrentPage === 1"
            class="flex items-center gap-2 px-5 py-2 border-2 border-[#f2ece4] rounded-xl text-[#8a6d51] font-serif font-bold hover:border-[#d4a373] hover:bg-[#fdfaf7] disabled:opacity-30 disabled:pointer-events-none transition-all"
          >
            <i class="fa-solid fa-chevron-left"></i> 上一页
          </button>
          
          <div class="text-[#8a6d51] font-serif font-bold bg-[#fdfaf7] px-4 py-2 rounded-lg border border-[#f2ece4] min-w-[80px] text-center">
            {{ postCurrentPage }} / {{ postTotalPages }}
          </div>
          
          <button
            @click="postCurrentPage++"
            :disabled="postCurrentPage === postTotalPages"
            class="flex items-center gap-2 px-5 py-2 border-2 border-[#f2ece4] rounded-xl text-[#8a6d51] font-serif font-bold hover:border-[#d4a373] hover:bg-[#fdfaf7] disabled:opacity-30 disabled:pointer-events-none transition-all"
          >
            下一页 <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { collectionApi } from "@/api/collection"
// 添加论坛收藏相关的API
import { getFavoritePosts, togglePostFavorite } from "@/api/forums"
import { useAuthStore } from "@/stores/auth"
import {getUserInfoAPI} from "@/api/user"

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)

// 标签页状态
const activeTab = ref<'dishes' | 'posts'>('dishes')

// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iODAiIHI9IjQwIiBmaWxsPSIjZTllOWU5Ii8+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iMTUwIiByPSIzMCIgZmlsbD0iI2U5ZTllOSIvPgo8L3N2Zz4=';

//菜品收藏相关
const dishFavorites = ref<any[]>([])
const dishCurrentPage = ref(1)
const dishPageSize = 6

const dishTotalPages = computed(() => {
  return Math.ceil(dishFavorites.value.length / dishPageSize) || 1
})

const paginatedDishFavorites = computed(() => {
  const start = (dishCurrentPage.value - 1) * dishPageSize
  const end = start + dishPageSize
  return dishFavorites.value.slice(start, end)
})

//帖子收藏相关
interface PostFavorite {
  postId: number
  favoriteId?: number
  title: string
  content: string
  username: string
  userAvatar: string
  userId: number
  categoryName?: string
  collectionTime: string
  coverImage?: string
}

const postFavorites = ref<PostFavorite[]>([])
const postCurrentPage = ref(1)
const postPageSize = 10

const postTotalPages = computed(() => {
  return Math.ceil(postFavorites.value.length / postPageSize) || 1
})

const paginatedPostFavorites = computed(() => {
  const start = (postCurrentPage.value - 1) * postPageSize
  const end = start + postPageSize
  return postFavorites.value.slice(start, end)
})

//图片处理
const getImageUrl = (url: string) => {
  if (!url) return '/placeholder-dish.png';
  if (url.includes('src/assets')) {
    const cleanPath = url.replace('src/assets/', '../../assets/');
    return new URL(cleanPath, import.meta.url).href;
  }
  return url;
}



//数据加载
const loadFavorites = async () => {
  if (!auth.user?.userId) return
  loading.value = true
  
  try {
    // 并行加载菜品收藏和帖子收藏
    await Promise.all([
      loadDishFavorites(),
      loadPostFavorites()
    ])
  } catch (error) {
    console.error('加载收藏失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载菜品收藏（原有逻辑）
const loadDishFavorites = async () => {
  try {
    const response = await collectionApi.getUserFavoriteDishes(auth.user!.userId)
    dishFavorites.value = response.map(item => ({
      id: item.dishId,
      collectionId: item.collectionId,
      name: item.name || '未命名食疗方',
      image: getImageUrl(item.image), 
      description: item.effect || '暂无详细说明',
      category: '食谱',
      collectionTime: item.collectionTime ? new Date(item.collectionTime).toLocaleDateString() : '近期'
    }))
  } catch (error) {
    console.error('加载菜品收藏失败:', error)
    dishFavorites.value = []
  }
}


// 加载帖子收藏
const loadPostFavorites = async () => {
  try {
    // 调用论坛收藏帖子接口
    const response = await getFavoritePosts();
    console.log('收藏帖子API原始响应:', response);

    // 兼容多种返回结构（res / res.data / res.data.data）
    let list: any[] = [];
    if (Array.isArray(response)) {
      list = response;
    } else if (response && Array.isArray((response as any).data)) {
      list = (response as any).data;
    } else if (response && Array.isArray((response as any).data?.data)) {
      list = (response as any).data.data;
    } else if (response && Array.isArray((response as any).data?.list)) {
      list = (response as any).data.list;
    }
const usernameMap = new Map<number, string>();
const imageMap = new Map<number, string>();
    if (list.length > 0) {
      // 收集所有需要查询的userId
      const userIds = list.map(item => item.userId).filter(Boolean);
      if (userIds.length > 0) {
        // 批量查询用户名
        await Promise.all(
          userIds.map(async (userId) => {
            if (!usernameMap.has(userId)) {
              try {
                const data1 = await getUserInfoAPI(userId);
                console.log("33333333333333333")
                console.log(data1)
                const image = data1.avatarUrl;
                usernameMap.set(userId, data1.username || '匿名用户');
                imageMap.set(userId, image || defaultAvatar);
              } catch (e) {
                usernameMap.set(userId, '匿名用户');
                imageMap.set(userId, defaultAvatar);
              }
            }
          })
        );
      }}

console.log(usernameMap)
      console.log(imageMap)

    if (list.length > 0) {
      const posts = list.map((item: any) => ({
        postId: item.postId || item.id,
        favoriteId: 0, // 暂时不需要收藏记录ID
        title: item.title || '无标题',
        content: item.content ? item.content.substring(0, 100) + '...' : '暂无内容',
        username: usernameMap.get(item.userId) || '匿名用户',
        userAvatar: imageMap.get(item.userId) || defaultAvatar,
        userId: item.userId,
        categoryName: item.categoryName || '论坛',
        collectionTime: item.updateTime 
  ? new Date(item.updateTime).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false // 强制24小时制，避免出现上午/下午
    }).replace(/\//g, '-').replace(/\s+/, ' ') // 替换分隔符，保证格式统一
  : '近期',
        coverImage: item.coverImage || item.images?.[0]
      } as PostFavorite));
      
      postFavorites.value = posts;
      
      // 按创建时间倒序排列
      postFavorites.value.sort((a, b) => {
        try {
          const timeA = a.collectionTime === '近期' ? new Date(0) : new Date(a.collectionTime);
          const timeB = b.collectionTime === '近期' ? new Date(0) : new Date(b.collectionTime);
          return timeB.getTime() - timeA.getTime();
        } catch (e) {
          return 0;
        }
      });
    } else {
      console.warn('收藏帖子API没有返回列表数据，使用空列表');
      postFavorites.value = [];
    }
  } catch (error) {
    console.error('加载帖子收藏失败:', error);
    postFavorites.value = [];
  }
};

// 取消菜品收藏
const removeDishFavorite = async (dishId: number, collectionId: number) => {
  if (!auth.user?.userId) return
  try {
    const res = await collectionApi.cancelCollection(auth.user.userId, collectionId)
    if (res.code === 200) {
      dishFavorites.value = dishFavorites.value.filter(item => item.id !== dishId)
      if (paginatedDishFavorites.value.length === 0 && dishCurrentPage.value > 1) {
        dishCurrentPage.value--
      }
    }
  } catch (error) {
    console.error('取消菜品收藏失败:', error)
  }
}

// 取消帖子收藏
const removePostFavorite = async (postId: number) => {
  if (!auth.user?.userId) return;
  try {
    // 调用论坛的收藏/取消收藏接口
    const res = await togglePostFavorite(postId);
    if (res.code === 200) {
      // 从列表中移除该帖子
      postFavorites.value = postFavorites.value.filter(item => item.postId !== postId);
      
      if (paginatedPostFavorites.value.length === 0 && postCurrentPage.value > 1) {
        postCurrentPage.value--;
      }
      alert(res.msg || '已取消收藏');
    }
  } catch (error) {
    console.error('取消帖子收藏失败:', error);
    alert('取消收藏失败，请重试');
  }
};

// 跳转到菜品详情
const goToDishDetail = (item: any) => {
  router.push(`/dishes/${item.id}`)
}

// 跳转到帖子详情
const goToPostDetail = (item: PostFavorite) => {
  router.push(`/forum-post-detail/${item.postId}`)
}

// 刷新收藏
const refreshFavorites = () => {
  dishCurrentPage.value = 1
  postCurrentPage.value = 1
  loadFavorites()
}

const averImage =() =>{

}

onMounted(() => loadFavorites())
</script>

<style scoped>
/* 文字截断 */
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f2ece4;
}

::-webkit-scrollbar-thumb {
  background: #d4a373;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #8a6d51;
}
</style>