<template>
  <div>
    <div v-if="loading" class="text-center py-8">
      <i class="fa-solid fa-yin-yang fa-spin text-xl text-[#d4a373]"></i>
    </div>
    
    <div v-else-if="userList.length === 0" class="text-center py-8">
      <p class="text-[#c4a484] font-serif italic">暂无数据</p>
    </div>
    
    <div v-else class="space-y-3">
      <div 
        v-for="user in userList" 
        :key="user.userId || user.id"
        class="flex items-center justify-between p-4 rounded-xl bg-[#f8f5f1] dark:bg-slate-700/30 hover:bg-white dark:hover:bg-slate-700 transition-all border border-transparent hover:border-[#f2ece4] dark:hover:border-slate-600"
      >
        <div class="flex items-center gap-3 cursor-pointer" @click="goToUserProfile(user)">
          <img 
            :src="user.avatarUrl || defaultAvatar" 
            class="w-10 h-10 rounded-full object-cover border border-[#f2ece4]"
            alt="用户头像"
          />
          <div>
            <div class="font-serif font-bold text-[#6d4c3d] dark:text-[#deb887] text-sm">
              {{ user.username }}
            </div>
            <div class="text-[10px] text-[#c4a484] font-bold uppercase tracking-widest">
              ID: {{ user.userId || user.id }}
            </div>
          </div>
        </div>
        
        <button
          v-if="!isCurrentUser(user) && type === 'fans'"
          @click="toggleFollow(user)"
          class="px-3 py-1.5 text-xs font-bold rounded-lg transition-all active:scale-95"
          :class="user.isFollowing 
            ? 'bg-[#f2ece4] text-[#a67c52] border border-[#e3d5ca]' 
            : 'bg-[#8a6d51] text-white hover:bg-[#6d4c3d]'"
        >
          {{ user.isFollowing ? '已关注' : '+ 关注' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getFollowListAPI, getFansListAPI, followUserAPI, unfollowUserAPI } from '@/api/user';
import { useAuthStore } from '@/stores/auth';

const props = defineProps<{
  userId: number;
  type: 'fans' | 'following';
}>();

const emit = defineEmits(['close']);

const router = useRouter();
const authStore = useAuthStore();
const userList = ref<any[]>([]);
const loading = ref(false);

const defaultAvatar = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23cbd5e1'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E";

const fetchList = async () => {
  if (!props.userId) return;
  
  loading.value = true;
  try {
    const res = props.type === 'following' 
      ? await getFollowListAPI(props.userId)
      : await getFansListAPI(props.userId);
    
    let list = res.data?.data || res.data || [];
    
    // 确保是数组
    if (!Array.isArray(list)) {
      list = [];
    }
    
    // 检查当前登录用户是否已关注这些用户
    const currentUserId = authStore.user?.userId || authStore.user?.id;
    if (currentUserId && props.type === 'fans') {
      // 获取当前用户的关注列表
      const followRes = await getFollowListAPI(currentUserId);
      const followingList = followRes.data?.data || followRes.data || [];
      
      userList.value = list.map((user: any) => ({
        ...user,
        userId: user.userId || user.id,
        isFollowing: followingList.some((followed: any) => 
          followed.userId === (user.userId || user.id)
        )
      }));
    } else {
      userList.value = list.map((user: any) => ({
        ...user,
        userId: user.userId || user.id,
        isFollowing: props.type === 'following' // 关注列表中的用户默认是已关注的
      }));
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
    userList.value = [];
  } finally {
    loading.value = false;
  }
};

const goToUserProfile = (user: any) => {
  const userId = user.userId || user.id;
  if (userId) {
    router.push(`/user/${userId}`);
    emit('close');
  }
};

const isCurrentUser = (user: any) => {
  const currentUserId = authStore.user?.userId || authStore.user?.id;
  const targetUserId = user.userId || user.id;
  return currentUserId === targetUserId;
};

const toggleFollow = async (user: any) => {
  const targetUserId = user.userId || user.id;
  if (!targetUserId) return;
  
  try {
    if (user.isFollowing) {
      await unfollowUserAPI(targetUserId);
      user.isFollowing = false;
    } else {
      await followUserAPI(targetUserId);
      user.isFollowing = true;
    }
  } catch (error) {
    console.error('关注操作失败:', error);
  }
};

onMounted(() => {
  fetchList();
});
</script>