<template>
  <div class="bg-[#fdfcf8] dark:bg-slate-800 rounded-2xl shadow-xl border-2 border-[#f2ece4] dark:border-slate-700 min-h-[600px] overflow-hidden antialiased">
    
    <div class="p-6 border-b-2 border-[#f2ece4] dark:border-slate-700 bg-[#fdfaf7] dark:bg-slate-700/30">
      <div class="flex items-center justify-around text-center">
        <div 
          class="cursor-pointer group p-3 rounded-xl transition-all hover:bg-white dark:hover:bg-slate-600"
          :class="{ 'bg-white dark:bg-slate-600 shadow-md border border-[#f2ece4]': activeTab === 'posts' }"
          @click="activeTab = 'posts'"
        >
          <div class="text-2xl font-serif font-black text-[#8a6d51] dark:text-[#deb887] group-hover:text-[#d4a373]">
            {{ stats.postCount }}
          </div>
          <div class="text-xs text-[#c4a484] font-bold mt-1 tracking-widest uppercase">帖子</div>
        </div>

        <div 
          class="cursor-pointer group p-3 rounded-xl transition-all hover:bg-white dark:hover:bg-slate-600"
          :class="{ 'bg-white dark:bg-slate-600 shadow-md border border-[#f2ece4]': activeTab === 'following' }"
          @click="activeTab = 'following'"
        >
          <div class="text-2xl font-serif font-black text-[#8a6d51] dark:text-[#deb887] group-hover:text-[#d4a373]">
            {{ stats.followingCount }}
          </div>
          <div class="text-xs text-[#c4a484] font-bold mt-1 tracking-widest uppercase">关注</div>
        </div>

        <div 
          class="cursor-pointer group p-3 rounded-xl transition-all hover:bg-white dark:hover:bg-slate-600"
          :class="{ 'bg-white dark:bg-slate-600 shadow-md border border-[#f2ece4]': activeTab === 'fans' }"
          @click="activeTab = 'fans'"
        >
          <div class="text-2xl font-serif font-black text-[#8a6d51] dark:text-[#deb887] group-hover:text-[#d4a373]">
            {{ stats.followerCount }}
          </div>
          <div class="text-xs text-[#c4a484] font-bold mt-1 tracking-widest uppercase">粉丝</div>
        </div>
      </div>
    </div>

    <div class="p-6">
      <div v-if="activeTab === 'posts'" class="space-y-6">
        <div class="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
          <button
            v-for="filter in postFilters"
            :key="filter.value"
            @click="changePostFilter(filter.value)"
            class="px-5 py-2 text-xs font-bold rounded-lg whitespace-nowrap transition-all border-2"
            :class="currentPostFilter === filter.value 
              ? 'bg-[#8a6d51] border-[#8a6d51] text-white shadow-md' 
              : 'bg-white dark:bg-slate-700 border-[#f2ece4] text-[#c4a484] hover:border-[#d4a373]'"
          >
            {{ filter.label }}
          </button>
        </div>

        <div v-if="loading" class="text-center py-12">
          <i class="fa-solid fa-yin-yang fa-spin text-3xl text-[#d4a373]"></i>
          <p class="mt-3 text-[#8a6d51] font-serif italic">正在翻阅文案...</p>
        </div>

        <div v-else-if="posts.length === 0" class="text-center py-16 bg-white dark:bg-slate-900/50 rounded-2xl border-2 border-dashed border-[#f2ece4]">
          <div class="text-[#e3d5ca] mb-4">
            <i class="fa-solid fa-pen-nib text-5xl"></i>
          </div>
          <p class="text-[#a67c52] font-serif mb-6">
            {{ `暂无${postFilters.find(f => f.value === currentPostFilter)?.label}的帖子记录` }}
          </p>
          <button 
            @click="router.push('/forum')"
            class="px-6 py-2.5 bg-[#d4a373] text-white rounded-xl font-bold hover:bg-[#c4a484] transition-all shadow-lg text-sm"
          >
            前往论坛撰写
          </button>
        </div>

        <div v-else class="grid gap-5">
          <div 
            v-for="post in posts" 
            :key="post.postId"
            class="group relative bg-white dark:bg-slate-800 p-6 rounded-2xl border-2 border-[#f2ece4] dark:border-slate-700 hover:border-[#d4a373]/40 transition-all hover:shadow-xl"
          >
            <div class="absolute top-6 right-6">
              <span class="px-3 py-1 rounded-lg text-[10px] font-black tracking-widest uppercase border" 
                    :style="{ color: getStatusStyle(post.status).color, borderColor: getStatusStyle(post.status).color + '40', backgroundColor: getStatusStyle(post.status).color + '10' }">
                {{ getStatusStyle(post.status).text }}
              </span>
            </div>

            <div @click="router.push(`/forum-post-detail/${post.postId}`)" class="cursor-pointer pr-20">
              <h3 class="font-serif text-xl font-black text-[#6d4c3d] dark:text-[#deb887] mb-3 truncate group-hover:text-[#8a6d51]">
                {{ post.title }}
              </h3>
              <p class="text-[#8a6d51]/80 dark:text-gray-400 text-sm font-serif italic line-clamp-2 mb-4 leading-relaxed">
                {{ post.content }}
              </p>
            </div>

            <div class="flex items-center justify-between text-[11px] font-bold text-[#c4a484] pt-4 border-t border-[#f2ece4] dark:border-slate-700/50">
              <div class="flex items-center gap-5 tracking-wider uppercase">
                <span><i class="fa-regular fa-calendar-check mr-1.5 text-[#d4a373]"></i>{{ formatDate(post.createTime) }}</span>
                <span><i class="fa-regular fa-heart mr-1.5 text-[#d4a373]"></i>{{ post.likeCount || 0 }}</span>
                <span><i class="fa-regular fa-comment mr-1.5 text-[#d4a373]"></i>{{ post.commentCount || 0 }}</span>
              </div>
              <div class="flex gap-4 opacity-0 group-hover:opacity-100 transition-all transform translate-x-2 group-hover:translate-x-0">
                <button @click.stop="handleDeletePost(post.postId)" class="text-red-400 hover:text-red-600 flex items-center gap-1 font-black">
                  <i class="fa-solid fa-trash-can"></i> 删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="space-y-6">
        <div v-if="loading" class="py-20 text-center">
          <i class="fa-solid fa-yin-yang fa-spin text-3xl text-[#d4a373]"></i>
        </div>
        <div v-else-if="socialList.length === 0" class="py-20 text-center bg-white dark:bg-slate-900 rounded-2xl border-2 border-dashed border-[#f2ece4]">
          <p class="text-[#c4a484] font-serif italic">暂无相关同修好友</p>
        </div>
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div 
            v-for="user in socialList" 
            :key="user.userId"
            class="flex items-center justify-between p-5 rounded-2xl bg-white dark:bg-slate-700/30 border-2 border-[#f2ece4] dark:border-slate-700 hover:border-[#d4a373]/30 transition-all shadow-sm"
          >
            <div class="flex items-center gap-4 cursor-pointer" @click="router.push(`/user/${user.userId}`)">
              <div class="relative">
                <img :src="user.avatarUrl || defaultAvatar" class="w-14 h-14 rounded-full object-cover border-2 border-[#f2ece4] shadow-sm" />
                <div class="absolute inset-0 rounded-full border border-black/5"></div>
              </div>
              <div class="min-w-0">
                <div class="font-serif font-black text-[#6d4c3d] dark:text-[#deb887] text-base truncate">{{ user.username }}</div>
                <div class="text-[10px] text-[#c4a484] font-bold uppercase tracking-widest mt-0.5">Community Member</div>
              </div>
            </div>
            <button
              v-if="user.userId !== currentUserId"
              @click="toggleFollow(user)"
              class="px-5 py-2 text-xs font-black rounded-xl transition-all shadow-sm active:scale-95"
              :class="user.isFollowing 
                ? 'bg-[#f2ece4] text-[#a67c52] border border-[#e3d5ca]' 
                : 'bg-[#8a6d51] text-white hover:bg-[#6d4c3d]'"
            >
              {{ user.isFollowing ? '已关注' : '+ 关注' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.font-serif {
  font-family: "Source Han Serif CN", serif;
}
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }

/* 列表淡入效果 */
.grid > div {
  animation: card-fade-in 0.5s ease-out forwards;
}

@keyframes card-fade-in {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { getUserStatsAPI, followUserAPI, unfollowUserAPI, getFollowListAPI, getFansListAPI } from '@/api/user';
import { deletePostAPI, getMyPostsAPI } from '@/api/forums';

const router = useRouter();
const authStore = useAuthStore();
const defaultAvatar = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23cbd5e1'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E";

const activeTab = ref('posts'); 
const loading = ref(false);
const stats = ref({ postCount: 0, followingCount: 0, followerCount: 0 });
const posts = ref<any[]>([]);
const socialList = ref<any[]>([]);

const currentUserId = computed(() => authStore.user?.userId || authStore.user?.id || 0);

const currentPostFilter = ref<number>(1); // 默认显示"已发布"
const postFilters = [
  { label: '已发布', value: 1 },
  { label: '审核中', value: 0 },
  { label: '未通过', value: 2 },
];

const getStatusStyle = (status: number) => {
  const s = Number(status);
  switch(s) {
    case 0: return { class: 'bg-amber-100 text-amber-700', text: '待审核' };
    case 1: return { class: 'bg-green-100 text-green-700', text: '已发布' };
    case 2: return { class: 'bg-red-100 text-red-700', text: '未通过' };
    default: return { class: 'bg-slate-100 text-slate-500', text: '处理中' };
  }
};

// 获取统计数据
const fetchStats = async () => {
  if (!currentUserId.value) return;
  try {
    const res = await getUserStatsAPI(currentUserId.value);
    console.log('统计API响应:', res);
    const data = res.data?.data || res.data || res;
    console.log('统计数据:', data);
    
    stats.value.followingCount = data.followingCount || 0;
    stats.value.followerCount = data.followerCount || 0;
    
    // 帖子总数只显示已发布的数量
    // 获取所有已发布的帖子数量
    try {
      const postsRes = await getMyPostsAPI(1); // 只获取已发布的帖子
      const publishedPosts = postsRes.data?.data || postsRes.data || postsRes;
      if (Array.isArray(publishedPosts)) {
        stats.value.postCount = publishedPosts.length;
        console.log('已发布帖子数量:', stats.value.postCount);
      } else {
        stats.value.postCount = 0;
      }
    } catch (error) {
      console.error('获取已发布帖子数量失败:', error);
      stats.value.postCount = 0;
    }
  } catch (error) {
    console.error('获取统计失败:', error);
  }
};

const fetchPosts = async () => {
  if (!currentUserId.value) return;
  loading.value = true;
  try {
    const res = await getMyPostsAPI(currentPostFilter.value);
    console.log('当前筛选状态:', currentPostFilter.value, '返回帖子数:', res.data?.data?.length);
    console.log('完整API响应:', res);
    
    const rawData = res.data?.data || res.data || res;
    console.log('原始数据:', rawData);
    
    if (Array.isArray(rawData)) {
      posts.value = rawData.map(post => ({
        ...post,
        postId: post.postId || post.id, 
        status: post.status !== undefined && post.status !== null ? Number(post.status) : 0 
      }));

      console.log('处理后的帖子列表:', posts.value);
    } else {
      console.log('返回数据不是数组:', rawData);
      posts.value = [];
    }
  } catch (error) {
    console.error('获取帖子失败:', error);
    posts.value = [];
  } finally {
    loading.value = false;
  }
};


// 获取社交列表
const fetchSocialList = async () => {
  if (!currentUserId.value) return;
  loading.value = true;
  try {
    const res = activeTab.value === 'following' 
      ? await getFollowListAPI(currentUserId.value)
      : await getFansListAPI(currentUserId.value);
    const list = res.data?.data || res.data || [];
    socialList.value = Array.isArray(list) ? list.map((u: any) => ({
      ...u,
      userId: u.userId || u.id,
      isFollowing: activeTab.value === 'following'
    })) : [];
  } catch (error) {
    socialList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleDeletePost = async (postId: number) => {
  if(!confirm('确定要删除这条帖子吗？')) return;
  try {
    await deletePostAPI(postId);
    posts.value = posts.value.filter(p => p.postId !== postId);
    // 删除帖子后重新获取已发布帖子数量
    await fetchStats();
  } catch (e) {
    alert('删除失败');
  }
};

const toggleFollow = async (user: any) => {
  try {
    if (user.isFollowing) {
      await unfollowUserAPI(user.userId);
      user.isFollowing = false;
      stats.value.followingCount--;
    } else {
      await followUserAPI(user.userId);
      user.isFollowing = true;
      stats.value.followingCount++;
    }
  } catch (e) {
    console.error(e);
  }
};

const changePostFilter = (val: any) => {
  currentPostFilter.value = val;
  fetchPosts();
};

const formatDate = (d: string) => d ? d.replace('T', ' ').substring(0, 16) : '';

watch(activeTab, (newVal) => {
  if (newVal === 'posts') fetchPosts();
  else fetchSocialList();
  fetchStats();
});

onMounted(() => {
  fetchStats();
  fetchPosts();
});
</script>

