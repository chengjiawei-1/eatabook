<template>
  <div class="min-h-screen bg-[#fdfaf7] dark:bg-slate-900 pb-12">
    <!-- 返回按钮 -->
    <div class="max-w-6xl mx-auto px-4 pt-4">
      <button 
        @click="goBack"
        class="inline-flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-800 border border-[#f2ece4] dark:border-slate-700 rounded-xl text-[#8a6d51] hover:text-[#d4a373] hover:border-[#d4a373]/40 transition-all shadow-sm"
      >
        <i class="fa-solid fa-arrow-left text-sm"></i>
        <span class="font-bold text-sm">返回</span>
      </button>
    </div>

    <!-- 顶部用户信息卡片 -->
    <div class="bg-white dark:bg-slate-800 border-b border-[#f2ece4] dark:border-slate-700 mt-4">
      <div class="max-w-6xl mx-auto px-4 py-8">
        <div class="flex flex-col md:flex-row items-center md:items-start gap-8">
          <!-- 用户头像 -->
          <div class="relative">
            <img 
              :src="userData.avatarUrl || defaultAvatar" 
              class="w-32 h-32 rounded-full object-cover border-4 border-white dark:border-slate-700 shadow-xl"
              alt="用户头像"
              @error="handleImageError"
            />
          </div>

          <!-- 用户基本信息 -->
          <div class="flex-1 text-center md:text-left">
            <div class="flex flex-col md:flex-row md:items-center gap-4 mb-6">
              <h1 class="font-serif text-3xl font-black text-[#6d4c3d] dark:text-[#deb887]">
                {{ userData.username || userData.name || userData.nickname || `用户${userId}` }}
              </h1>
              
              <!-- 关注按钮（非本人显示） -->
              <button 
                v-if="!isCurrentUser && userId"
                @click="toggleFollowUser"
                :disabled="followLoading"
                class="px-6 py-2.5 rounded-xl font-bold transition-all shadow-sm active:scale-95 flex items-center gap-2 justify-center md:justify-start"
                :class="isFollowing 
                  ? 'bg-[#f2ece4] text-[#a67c52] border border-[#e3d5ca] hover:bg-[#e8dfd1]' 
                  : 'bg-[#8a6d51] text-white hover:bg-[#6d4c3d]'"
              >
                <i :class="followLoading ? 'fa-solid fa-spinner fa-spin' : (isFollowing ? 'fa-solid fa-user-check' : 'fa-solid fa-user-plus')"></i>
                {{ isFollowing ? '已关注' : '+ 关注' }}
              </button>
            </div>

            <!-- 统计数据 -->
            <div class="flex justify-center md:justify-start gap-8">
              <div class="text-center">
                <div class="text-2xl font-serif font-black text-[#8a6d51] dark:text-[#deb887]">
                  {{ stats.postCount || 0 }}
                </div>
                <div class="text-xs text-[#c4a484] font-bold tracking-widest uppercase mt-1">帖子</div>
              </div>
              
              <!-- 粉丝数 - 只有当前用户可点击查看 -->
              <div 
                @click="isCurrentUser ? showFans = true : null"
                class="text-center transition-all"
                :class="isCurrentUser ? 'cursor-pointer hover:opacity-80' : 'cursor-default'"
              >
                <div class="text-2xl font-serif font-black text-[#8a6d51] dark:text-[#deb887]">
                  {{ stats.followerCount || 0 }}
                </div>
                <div class="text-xs text-[#c4a484] font-bold tracking-widest uppercase mt-1">粉丝</div>
              </div>
              
              <!-- 关注数 - 只有当前用户可点击查看 -->
              <div 
                @click="isCurrentUser ? showFollowing = true : null"
                class="text-center transition-all"
                :class="isCurrentUser ? 'cursor-pointer hover:opacity-80' : 'cursor-default'"
              >
                <div class="text-2xl font-serif font-black text-[#8a6d51] dark:text-[#deb887]">
                  {{ stats.followingCount || 0 }}
                </div>
                <div class="text-xs text-[#c4a484] font-bold tracking-widest uppercase mt-1">关注</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="max-w-6xl mx-auto px-4 py-8">
      <!-- 帖子筛选和排序 -->
      <div class="mb-8 bg-white dark:bg-slate-800 rounded-2xl border border-[#f2ece4] dark:border-slate-700 p-6 shadow-sm">
        <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h2 class="font-serif text-xl font-black text-[#6d4c3d] dark:text-[#deb887] mb-2">
              {{ userData.username || userData.name || userData.nickname || `用户${userId}` }} 发布的帖子
            </h2>
            <div class="flex items-center gap-4">
              <p class="text-[#c4a484] text-sm">
                共 {{ posts.length }} 篇已发布帖子
              </p>
              <span class="text-xs bg-[#f8f5f1] dark:bg-slate-700 text-[#8a6d51] px-2 py-1 rounded">
                按最新发布排序
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-16">
        <i class="fa-solid fa-yin-yang fa-spin text-4xl text-[#d4a373] mb-4"></i>
        <p class="text-[#8a6d51] font-serif italic">正在加载用户帖子...</p>
      </div>

      <!-- 无帖子状态 -->
      <div v-else-if="posts.length === 0" class="text-center py-16 bg-white dark:bg-slate-800/50 rounded-2xl border-2 border-dashed border-[#f2ece4]">
        <div class="text-[#e3d5ca] mb-4">
          <i class="fa-solid fa-scroll text-6xl"></i>
        </div>
        <p class="text-[#a67c52] font-serif mb-6 text-lg">
          {{ userData.username || userData.name || userData.nickname || `用户${userId}` }} 还没有发布过帖子
        </p>
        <button 
          v-if="isCurrentUser"
          @click="router.push('/forum')"
          class="px-6 py-3 bg-[#d4a373] text-white rounded-xl font-bold hover:bg-[#c4a484] transition-all shadow-lg"
        >
          <i class="fa-solid fa-pen-to-square mr-2"></i>开始撰写第一篇帖子
        </button>
      </div>

      <!-- 帖子列表 -->
      <div v-else class="grid gap-6">
        <div 
          v-for="post in posts" 
          :key="post.postId"
          class="group relative bg-white dark:bg-slate-800 p-6 rounded-2xl border-2 border-[#f2ece4] dark:border-slate-700 hover:border-[#d4a373]/40 transition-all hover:shadow-xl"
        >
          <!-- 帖子内容 -->
          <div @click="router.push(`/forum-post-detail/${post.postId}`)" class="cursor-pointer">
            <h3 class="font-serif text-xl font-black text-[#6d4c3d] dark:text-[#deb887] mb-3 group-hover:text-[#8a6d51] line-clamp-1">
              {{ post.title }}
            </h3>
            
            <!-- 帖子摘要 -->
            <p class="text-[#8a6d51]/80 dark:text-gray-400 text-sm font-serif line-clamp-3 mb-4 leading-relaxed">
              {{ post.content }}
            </p>

            <!-- 帖子元数据 -->
            <div class="flex flex-wrap items-center gap-4 text-xs text-[#c4a484]">
              <span v-if="post.categoryName" class="px-3 py-1 bg-[#f8f5f1] dark:bg-slate-700/50 rounded-lg">
                #{{ post.categoryName }}
              </span>
              <span class="flex items-center gap-1">
                <i class="fa-regular fa-eye text-[#d4a373]"></i>
                <span class="font-bold">{{ post.viewCount || 0 }}</span>
              </span>
            </div>
          </div>

          <!-- 帖子底部信息 -->
          <div class="flex items-center justify-between text-[11px] font-bold text-[#c4a484] pt-4 mt-4 border-t border-[#f2ece4] dark:border-slate-700/50">
            <div class="flex items-center gap-5 tracking-wider uppercase">
              <span><i class="fa-regular fa-calendar-check mr-1.5 text-[#d4a373]"></i>{{ formatDate(post.createTime) }}</span>
              <span><i class="fa-regular fa-heart mr-1.5 text-[#d4a373]"></i>{{ post.likeCount || 0 }}</span>
              <span><i class="fa-regular fa-comment mr-1.5 text-[#d4a373]"></i>{{ post.commentCount || 0 }}</span>
              <span><i class="fa-regular fa-star mr-1.5 text-[#d4a373]"></i>{{ post.favoriteCount || 0 }}</span>
            </div>
            
            <!-- 操作按钮（仅当前用户可见） -->
            <div v-if="isCurrentUser" class="flex gap-4 opacity-0 group-hover:opacity-100 transition-all transform translate-x-2 group-hover:translate-x-0">
              <button 
                @click.stop="handleDeletePost(post.postId)"
                class="text-red-400 hover:text-red-600 flex items-center gap-1 font-bold"
              >
                <i class="fa-solid fa-trash-can"></i> 删除
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 粉丝列表弹窗（仅当前用户可见） -->
    <div v-if="showFans && isCurrentUser" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white dark:bg-slate-800 rounded-2xl w-full max-w-md max-h-[80vh] overflow-hidden border border-[#f2ece4] dark:border-slate-700">
        <div class="p-6 border-b border-[#f2ece4] dark:border-slate-700 flex justify-between items-center">
          <h3 class="font-serif text-xl font-black text-[#6d4c3d] dark:text-[#deb887]">粉丝列表</h3>
          <button @click="showFans = false" class="text-[#c4a484] hover:text-[#8a6d51] text-xl">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
        <div class="p-4 overflow-y-auto max-h-[60vh]">
          <UserList 
            v-if="showFans && isCurrentUser"
            :user-id="userId"
            type="fans"
            @close="showFans = false"
          />
        </div>
      </div>
    </div>

    <!-- 关注列表弹窗（仅当前用户可见） -->
    <div v-if="showFollowing && isCurrentUser" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white dark:bg-slate-800 rounded-2xl w-full max-w-md max-h-[80vh] overflow-hidden border border-[#f2ece4] dark:border-slate-700">
        <div class="p-6 border-b border-[#f2ece4] dark:border-slate-700 flex justify-between items-center">
          <h3 class="font-serif text-xl font-black text-[#6d4c3d] dark:text-[#deb887]">关注列表</h3>
          <button @click="showFollowing = false" class="text-[#c4a484] hover:text-[#8a6d51] text-xl">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
        <div class="p-4 overflow-y-auto max-h-[60vh]">
          <UserList 
            v-if="showFollowing && isCurrentUser"
            :user-id="userId"
            type="following"
            @close="showFollowing = false"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUserById } from '@/api/forums';
import { getUserStatsAPI, followUserAPI, unfollowUserAPI, getFollowListAPI, getFansListAPI } from '@/api/user';
import { getPostsByUsername, deletePostAPI } from '@/api/forums';
import { useAuthStore } from '@/stores/auth';
import UserList from '@/components/UserList.vue';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 从路由参数获取用户ID，确保类型正确
const userId = computed(() => {
  const id = Number(route.params.userId);
  console.log('路由参数userId:', route.params.userId, '转换后:', id);
  return id || 0;
});

const userData = ref<any>({});
const stats = ref({ postCount: 0, followingCount: 0, followerCount: 0 });
const posts = ref<any[]>([]);
const loading = ref(false);
const followLoading = ref(false);
const showFans = ref(false);
const showFollowing = ref(false);

// 默认头像SVG数据
const defaultAvatar = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23cbd5e1'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E";

// 检查是否是当前用户
const isCurrentUser = computed(() => {
  const currentUserId = authStore.user?.userId || authStore.user?.id || 0;
  console.log('当前用户ID:', currentUserId, '目标用户ID:', userId.value);
  return currentUserId === userId.value;
});

const isFollowing = ref(false);

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 处理图片加载错误
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = defaultAvatar;
};

// 获取用户数据
const fetchUserData = async () => {
  if (!userId.value || userId.value === 0) {
    console.error('用户ID无效');
    return;
  }
  
  try {
    console.log('正在获取用户数据，用户ID:', userId.value);
    const res = await getUserById(userId.value);
    console.log('用户数据API响应:', res);
    
    // 尝试不同的数据结构
    let userDataRaw = null;
    
    if (res.data && res.data.data) {
      // 格式: { code: 200, data: { ... } }
      userDataRaw = res.data.data;
    } else if (res.data) {
      // 格式: { ... } 直接就是用户数据
      userDataRaw = res.data;
    } else if (res) {
      // 直接返回的就是用户数据
      userDataRaw = res;
    }
    
    console.log('原始用户数据:', userDataRaw);
    
    if (userDataRaw && typeof userDataRaw === 'object') {
      userData.value = userDataRaw;
      console.log('设置的用户数据:', userData.value);
      
      // 确保有username字段（尝试多个可能的字段名）
      if (!userData.value.username) {
        const possibleNameFields = ['username', 'userName', 'name', 'nickname', 'nickName', 'realName'];
        for (const field of possibleNameFields) {
          if (userData.value[field]) {
            userData.value.username = userData.value[field];
            console.log(`使用字段 ${field} 作为用户名: ${userData.value[field]}`);
            break;
          }
        }
        
        // 如果还没有用户名，使用默认值
        if (!userData.value.username) {
          userData.value.username = `用户${userId.value}`;
        }
      }
    } else {
      console.error('无法解析用户数据，使用默认值');
      userData.value = { username: `用户${userId.value}` };
    }
    
  } catch (error) {
    console.error('获取用户数据失败:', error);
    // 设置默认数据
    userData.value = { username: `用户${userId.value}` };
  }
};

// 获取用户统计数据
const fetchUserStats = async () => {
  if (!userId.value || userId.value === 0) return;
  
  try {
    console.log('正在获取用户统计，用户ID:', userId.value);
    const res = await getUserStatsAPI(userId.value);
    console.log('用户统计API响应:', res);
    
    // 尝试多种可能的响应格式
    let data = null;
    
    if (res.data && res.data.data) {
      // 格式: { code: 200, data: { ... } }
      data = res.data.data;
    } else if (res.data) {
      // 格式: { ... } 直接就是统计数据
      data = res.data;
    } else if (res) {
      // 直接返回的就是统计数据
      data = res;
    }
    
    console.log('统计原始数据:', data);
    
    if (data && typeof data === 'object') {
      // 尝试多个可能的字段名
      stats.value = {
        postCount: data.postCount || data.postCounts || data.posts || 0,
        followingCount: data.followingCount || data.following || 0,
        followerCount: data.followerCount || data.followers || 0,
      };
    } else {
      console.warn('统计数据格式异常，使用默认值');
      stats.value = { postCount: 0, followingCount: 0, followerCount: 0 };
    }
    
    console.log('设置的用户统计:', stats.value);
  } catch (error) {
    console.error('获取用户统计失败:', error);
    stats.value = { postCount: 0, followingCount: 0, followerCount: 0 };
  }
};

// 获取用户帖子
const fetchUserPosts = async () => {
  if (!userData.value.username) return;
  
  loading.value = true;
  try {
    console.log('正在获取用户帖子，用户名:', userData.value.username);
    const res = await getPostsByUsername(userData.value.username);
    console.log('用户帖子API响应:', res);
    
    // 尝试多种可能的响应格式
    let data = null;
    
    if (res.data && res.data.data) {
      // 格式: { code: 200, data: [...] }
      data = res.data.data;
    } else if (res.data && Array.isArray(res.data)) {
      // 格式: { data: [...] }
      data = res.data;
    } else if (Array.isArray(res.data)) {
      // 格式: [...] 直接是数组
      data = res.data;
    } else if (res) {
      // 尝试直接使用res
      data = res;
    }
    
    console.log('帖子原始数据:', data);
    
    if (Array.isArray(data)) {
      // 只筛选已发布的帖子（状态为1）
      const publishedPosts = data.filter(post => Number(post.status) === 1);
      
      console.log('已发布的帖子:', publishedPosts);
      
      // 按发布时间倒序排序（最新发布在前）
      const sortedPosts = publishedPosts.sort((a, b) => 
        new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
      );
      
      posts.value = sortedPosts.map((post, index) => ({
        ...post,
        postId: post.postId || post.id || index,
        status: post.status !== undefined && post.status !== null ? Number(post.status) : 0
      }));
      
      // 更新帖子数量统计（只统计已发布的帖子）
      stats.value.postCount = posts.value.length;
      
      console.log('处理后的帖子列表:', posts.value);
    } else {
      console.warn('帖子数据不是数组:', data);
      posts.value = [];
    }
    
  } catch (error) {
    console.error('获取用户帖子失败:', error);
    posts.value = [];
  } finally {
    loading.value = false;
  }
};

// 检查关注状态
const checkFollowStatus = async () => {
  if (isCurrentUser.value || !userId.value || userId.value === 0) return;
  
  try {
    const currentUserId = authStore.user?.userId || authStore.user?.id;
    if (!currentUserId) return;
    
    const res = await getFollowListAPI(currentUserId);
    const followingList = res.data?.data || res.data || [];
    isFollowing.value = followingList.some((user: any) => 
      (user.userId && user.userId === userId.value) || 
      (user.id && user.id === userId.value)
    );
    console.log('关注状态:', isFollowing.value);
  } catch (error) {
    console.error('检查关注状态失败:', error);
  }
};

// 切换关注状态
const toggleFollowUser = async () => {
  if (isCurrentUser.value || !userId.value || userId.value === 0) return;
  
  followLoading.value = true;
  try {
    if (isFollowing.value) {
      await unfollowUserAPI(userId.value);
      isFollowing.value = false;
      stats.value.followerCount = Math.max(0, stats.value.followerCount - 1);
    } else {
      await followUserAPI(userId.value);
      isFollowing.value = true;
      stats.value.followerCount += 1;
    }
  } catch (error) {
    console.error('关注操作失败:', error);
  } finally {
    followLoading.value = false;
  }
};

// 删除帖子
const handleDeletePost = async (postId: number) => {
  if (!confirm('确定要删除这条帖子吗？')) return;
  
  try {
    await deletePostAPI(postId);
    posts.value = posts.value.filter(p => p.postId !== postId);
    stats.value.postCount -= 1;
  } catch (error) {
    console.error('删除帖子失败:', error);
    alert('删除失败');
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '';
  
  try {
    const date = new Date(dateString);
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return dateString;
    }
    
    return date.toLocaleDateString('zh-CN', { 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-');
  } catch (error) {
    console.error('日期格式化错误:', error, dateString);
    return dateString;
  }
};

// 初始化数据
const initData = async () => {
  if (!userId.value || userId.value === 0) {
    console.error('无效的用户ID');
    return;
  }
  
  await fetchUserData();
  await fetchUserStats();
  await checkFollowStatus();
  
  // 用户数据加载后再获取帖子
  if (userData.value.username || userData.value.name || userData.value.nickname) {
    await fetchUserPosts();
  }
};

// 监听路由参数变化
watch(() => route.params.userId, (newUserId) => {
  const id = Number(newUserId);
  console.log('路由参数变化，新的用户ID:', id);
  
  if (id && id !== userId.value) {
    // 重新初始化数据
    initData();
  }
});

onMounted(() => {
  console.log('UserProfilePage 组件挂载');
  initData();
});
</script>

<style scoped>
.font-serif {
  font-family: "Source Han Serif CN", serif;
}

.line-clamp-1 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
}

.line-clamp-2 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.line-clamp-3 {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

/* 卡片淡入动画 */
.grid > div {
  animation: card-fade-in 0.5s ease-out forwards;
  animation-delay: calc(var(--item-index, 0) * 0.05s);
}

@keyframes card-fade-in {
  from { 
    opacity: 0; 
    transform: translateY(10px); 
  }
  to { 
    opacity: 1; 
    transform: translateY(0); 
  }
}
</style>