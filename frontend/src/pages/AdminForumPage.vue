<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              论坛内容管理 <span class="text-amber-500 text-base ml-2">Community</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              审核用户发帖与维护社区秩序，当前待办：<span class="text-rose-500 font-bold">{{ forumStats.pendingPosts }}</span> 篇待审核
            </p>
          </div>
          <button 
            @click="loadPosts"
            class="px-5 py-2.5 bg-white dark:bg-slate-900 border border-white dark:border-slate-800 text-slate-600 dark:text-slate-300 font-bold rounded-2xl shadow-xl shadow-slate-200/50 dark:shadow-none hover:bg-slate-50 transition-all flex items-center gap-2 group active:scale-95"
          >
            <i :class="['fa-solid fa-rotate-right group-hover:rotate-180 transition-transform duration-700', loading ? 'animate-spin' : '']"></i>
            <span>同步社区数据</span>
          </button>
        </div>

        <!-- 统计卡片 (统一控制台风格) -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div v-for="(stat, index) in statsCards" :key="index" 
            class="bg-white dark:bg-slate-900 rounded-3xl p-5 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none transition-all hover:-translate-y-1 relative group overflow-hidden">
            <div class="flex justify-between items-center relative z-10">
              <div>
                <p class="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1">{{ stat.label }}</p>
                <h3 class="text-2xl font-black text-slate-800 dark:text-white">{{ stat.value }}</h3>
              </div>
              <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg', stat.bgClass, stat.textClass]">
                <i :class="['fa-solid', stat.icon, 'text-xl']"></i>
              </div>
            </div>
            <div class="absolute -right-2 -bottom-2 opacity-[0.03] text-5xl rotate-12 group-hover:scale-125 transition-transform">
              <i :class="['fa-solid', stat.icon]"></i>
            </div>
          </div>
        </div>

        <!-- 筛选工具栏 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none p-6 border border-white dark:border-slate-800 mb-6">
          <div class="flex flex-col lg:flex-row gap-4 items-center">
            <div class="w-full lg:flex-1 relative">
              <i class="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索帖子标题、关键词内容..."
                class="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800/50 border-none rounded-2xl text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 transition-all font-bold placeholder:font-normal"
              />
            </div>
            
            <div class="flex flex-wrap gap-3 w-full lg:w-auto">
              <select v-model="categoryFilter" class="filter-select">
                <option value="">全部分类 Topic</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
              
              <select v-model="statusFilter" class="filter-select">
                <option value="all">全部状态 Status</option>
                <option value="pending">待审核 Pending</option>
                <option value="approved">已发布 Online</option>
                <option value="rejected">已拒绝 Banned</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 批量操作工具栏 (同步古籍管理风格) -->
        <Transition name="fade-slide">
          <div v-if="selectedPostIds.length > 0" class="bg-indigo-600 rounded-3xl shadow-xl p-4 mb-6 flex flex-col sm:flex-row items-center justify-between gap-4 animate-fade-in">
            <div class="flex items-center gap-3">
              <span class="px-3 py-1 bg-white/20 rounded-lg text-white text-[10px] font-black uppercase tracking-widest">
                已选 {{ selectedPostIds.length }} 项
              </span>
              <button 
                @click="batchApproveSelected"
                :disabled="batchLoading"
                class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 disabled:bg-emerald-300 text-white rounded-xl text-xs font-black shadow-lg transition-all flex items-center gap-2 active:scale-95"
              >
                <i :class="['fa-solid', batchLoading ? 'fa-spinner fa-spin' : 'fa-check']"></i>
                批量审核通过
              </button>
              
              <button 
                @click="clearSelection"
                class="px-3 py-2 text-white/70 hover:text-white text-[10px] font-black uppercase tracking-widest transition-colors"
              >
                清空选择
              </button>
            </div>
            <p class="text-white/80 text-[10px] font-bold italic tracking-wider uppercase">Content Review Operation Center</p>
          </div>
        </Transition>

        <!-- 帖子列表表格 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden">
          <div v-if="loading" class="py-24 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500 font-bold">正在调取社区卷宗...</p>
          </div>

          <div v-else-if="filteredPosts.length === 0" class="py-24 text-center">
            <div class="w-20 h-20 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
              <i class="fa-regular fa-folder-open text-3xl text-slate-200"></i>
            </div>
            <p class="text-slate-400 font-bold">暂无相关帖子记录</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[1000px]">
              <thead>
                <tr class="bg-slate-50/50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 text-slate-400 text-[11px] font-black uppercase tracking-widest">
                  <th class="px-6 py-4 w-[60px] text-center">
                    <input 
                      type="checkbox" 
                      :checked="isAllSelected" 
                      @change="toggleSelectAll"
                      class="rounded border-slate-300 text-amber-500 focus:ring-amber-500"
                    >
                  </th>
                  <th class="px-6 py-4 w-[30%]">帖子核心内容</th>
                  <th class="px-6 py-4 w-[15%]">发布作者</th>
                  <th class="px-6 py-4 w-[20%] text-center">分类话题</th>
                  <th class="px-6 py-4 w-[12%] text-center">当前状态</th>
                  <th class="px-6 py-4 w-[15%]">时间戳记</th>
                  <th class="px-8 py-4 w-[15%] text-right">操作管理</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50 dark:divide-slate-800">
                <tr v-for="post in paginatedPosts" :key="post.postId" class="group hover:bg-slate-50/80 dark:hover:bg-slate-800/50 transition-all">
                  <td class="px-6 py-4 text-center">
                    <input 
                      type="checkbox" 
                      :checked="isPostSelected(post.postId)"
                      @change="togglePostSelection(post.postId)"
                      :disabled="post.status !== 0"
                      class="rounded border-slate-300 text-amber-500 focus:ring-amber-500 disabled:opacity-30 disabled:cursor-not-allowed"
                    >
                  </td>
                  <td class="px-6 py-4">
                    <div class="flex flex-col gap-1">
                      <div @click="viewPostDetail(post)" class="font-black text-slate-800 dark:text-slate-200 text-sm line-clamp-1 hover:text-amber-500 cursor-pointer transition-colors">
                        {{ post.title }}
                      </div>
                      <div class="text-[11px] text-slate-400 font-bold line-clamp-2 leading-relaxed">{{ post.content }}</div>
                    </div>
                  </td>

                  <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                      <div class="w-9 h-9 rounded-2xl bg-slate-100 dark:bg-slate-800 border border-white dark:border-slate-700 flex items-center justify-center text-slate-400 overflow-hidden shadow-sm">
                        <img v-if="post.userAvatar" :src="post.userAvatar" class="w-full h-full object-cover" />
                        <i v-else class="fa-solid fa-user text-xs"></i>
                      </div>
                      <span class="text-xs font-bold text-slate-700 dark:text-slate-300 truncate max-w-[100px]">{{ post.username || '匿名用户' }}</span>
                    </div>
                  </td>

                  <td class="px-6 py-4 text-center">
                    <span :class="['px-3 py-1 rounded-xl text-[10px] font-black border uppercase tracking-tighter shadow-sm', getCategoryStyle(post.categoryId).badge]">
                      {{ getCategoryName(post.categoryId) }}
                    </span>
                  </td>

                  <td class="px-6 py-4 text-center">
                    <span :class="['inline-flex items-center px-2.5 py-1 rounded-lg text-[10px] font-black uppercase tracking-widest border shadow-sm', getStatusStyle(post.status).badge]">
                      <i :class="['mr-1.5', getStatusStyle(post.status).icon]"></i>
                      {{ getStatusStyle(post.status).text }}
                    </span>
                  </td>

                  <td class="px-6 py-4">
                    <div class="text-[10px] font-black text-slate-400 uppercase tracking-tighter">{{ formatTime(post.createTime) }}</div>
                  </td>

                  <td class="px-8 py-4 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button @click="viewPostDetail(post)" class="row-action-btn hover:bg-amber-500" title="详情"><i class="fa-solid fa-eye"></i></button>
                      <template v-if="post.status === 0">
                        <button @click="approvePost(post)" class="row-action-btn hover:bg-emerald-500" title="通过"><i class="fa-solid fa-check"></i></button>
                        <button @click="rejectPost(post)" class="row-action-btn hover:bg-orange-500" title="驳回"><i class="fa-solid fa-ban"></i></button>
                      </template>
                      <button @click="handleDelete(post)" class="row-action-btn hover:bg-rose-500" title="删除"><i class="fa-solid fa-trash"></i></button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页栏 -->
          <div class="px-8 py-6 bg-slate-50/50 dark:bg-slate-800/30 flex items-center justify-between">
            <span class="text-xs font-black text-slate-400 uppercase tracking-widest">
              Total {{ filteredPosts.length }} Records
            </span>
            <div class="flex items-center gap-3">
              <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <div class="px-5 py-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm text-sm font-black text-slate-800 dark:text-white">
                {{ currentPage }} <span class="text-slate-400 font-normal mx-1">/</span> {{ Math.ceil(filteredPosts.length / pageSize) }}
              </div>
              <button @click="nextPage" :disabled="currentPage * pageSize >= filteredPosts.length" class="pagination-btn">
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
      </div>
      <PostDetailModal v-if="showPostDetailModal" :post="selectedPost" @close="showPostDetailModal = false" />
  </div>
</template>

<script setup lang="ts">
/* 脚本逻辑完全保留原始实现，无任何功能修改 */
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import PostDetailModal from '@/components/PostDetailModal.vue'
import { getAdminPostsAPI, approvePostAPI, rejectPostAPI, deletePostAPI, getUserById, getForumCategories } from '@/api/forums'
import { ElMessage, ElMessageBox } from 'element-plus'

const posts = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const searchQuery = ref('')
const categoryFilter = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedPostIds = ref<number[]>([])
const batchLoading = ref(false)
const showPostDetailModal = ref(false)
const selectedPost = ref<any>(null)
const forumStats = ref({ totalPosts: 0, pendingPosts: 0, approvedPosts: 0 })

watch([searchQuery, categoryFilter, statusFilter], () => { currentPage.value = 1 })

const statsCards = computed(() => [
  { label: '总帖子数 Total', value: forumStats.value.totalPosts, icon: 'fa-comments', bgClass: 'bg-indigo-50 dark:bg-indigo-900/30', textClass: 'text-indigo-600' },
  { label: '待审核 Pending', value: forumStats.value.pendingPosts, icon: 'fa-clock', bgClass: 'bg-amber-50 dark:bg-amber-900/30', textClass: 'text-amber-600' },
  { label: '已发布 Online', value: forumStats.value.approvedPosts, icon: 'fa-circle-check', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600' },
  { label: '被驳回 Banned', value: forumStats.value.totalPosts - forumStats.value.approvedPosts - forumStats.value.pendingPosts, icon: 'fa-ban', bgClass: 'bg-rose-50 dark:bg-rose-900/30', textClass: 'text-rose-600' },
])

const filteredPosts = computed(() => posts.value.filter(post => {
  const matchSearch = post.title.toLowerCase().includes(searchQuery.value.toLowerCase()) || post.content.toLowerCase().includes(searchQuery.value.toLowerCase())
  const matchCat = !categoryFilter.value || post.categoryId === Number(categoryFilter.value)
  const matchStatus = statusFilter.value === 'all' || (statusFilter.value === 'pending' && post.status === 0) || (statusFilter.value === 'approved' && post.status === 1) || (statusFilter.value === 'rejected' && post.status === 2)
  return matchSearch && matchCat && matchStatus
}))

const paginatedPosts = computed(() => filteredPosts.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

const isAllSelected = computed(() => {
  const currentPagePendingPosts = paginatedPosts.value.filter(post => post.status === 0)
  return currentPagePendingPosts.length > 0 && currentPagePendingPosts.every(post => selectedPostIds.value.includes(post.postId))
})

const getStatusStyle = (status: number) => {
  switch (status) {
    case 1: return { badge: 'bg-emerald-50 text-emerald-600 border-emerald-100 dark:bg-emerald-900/20 dark:text-emerald-400', icon: 'fa-check-circle', text: '已发布' }
    case 0: return { badge: 'bg-amber-50 text-amber-600 border-amber-100 dark:bg-amber-900/20 dark:text-amber-400', icon: 'fa-hourglass-half', text: '待审核' }
    case 2: return { badge: 'bg-rose-50 text-rose-600 border-rose-100 dark:bg-rose-900/20 dark:text-rose-400', icon: 'fa-ban', text: '已驳回' }
    default: return { badge: 'bg-gray-50 text-gray-500', icon: 'fa-question', text: '未知' }
  }
}

const getCategoryStyle = (id: number) => {
  const colors = ['bg-indigo-50 text-indigo-600 border-indigo-100', 'bg-emerald-50 text-emerald-600 border-emerald-100', 'bg-amber-50 text-amber-600 border-amber-100', 'bg-rose-50 text-rose-600 border-rose-100', 'bg-purple-50 text-purple-600 border-purple-100'];
  return { badge: colors[id % colors.length] || 'bg-slate-50 text-slate-500' }
}

const formatTime = (t: string) => t ? new Date(t).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : '-'

const isPostSelected = (postId: number) => selectedPostIds.value.includes(postId)
const togglePostSelection = (postId: number) => {
  const index = selectedPostIds.value.indexOf(postId)
  index > -1 ? selectedPostIds.value.splice(index, 1) : selectedPostIds.value.push(postId)
}
const toggleSelectAll = () => {
  const currentPagePostIds = paginatedPosts.value.filter(post => post.status === 0).map(post => post.postId)
  isAllSelected.value ? selectedPostIds.value = selectedPostIds.value.filter(id => !currentPagePostIds.includes(id)) : selectedPostIds.value = [...new Set([...selectedPostIds.value, ...currentPagePostIds])]
}
const clearSelection = () => { selectedPostIds.value = [] }

const batchApproveSelected = async () => {
  if (selectedPostIds.value.length === 0) return ElMessage.warning('请先选择要操作的帖子')
  try {
    await ElMessageBox.confirm(`确定要批量通过 ${selectedPostIds.value.length} 个帖子吗？`, '批量确认');
    batchLoading.value = true; let successCount = 0;
    for (const postId of selectedPostIds.value) {
      try { const res = await approvePostAPI(postId); if (res.code === 200) { successCount++; const idx = posts.value.findIndex(p => p.postId === postId); if (idx !== -1) posts.value[idx].status = 1 } } catch {}
    }
    calculateStats(); clearSelection(); ElMessage.success(`成功通过 ${successCount} 个帖子`);
  } catch {} finally { batchLoading.value = false }
}

const loadPosts = async () => {
  loading.value = true;
  try {
    const res = await getAdminPostsAPI();
    if (res.code !== 200) throw new Error(res.msg);
    await loadCategories();
    posts.value = await Promise.all(res.data.map(async (p: any) => {
      const postId = p.postId || p.id; const currentStatus = p.status !== undefined ? Number(p.status) : 0;
      let username = p.username; let avatarUrl = p.userAvatar;
      if (!username && p.userId) {
        try { const uRes = await getUserById(p.userId); const userData = uRes.data || uRes; username = userData.username || userData.data?.username; avatarUrl = userData.avatarUrl || userData.data?.avatarUrl; } catch {}
      }
      return { ...p, postId, status: currentStatus, username: username || `用户${p.userId}`, userAvatar: avatarUrl, categoryId: Number(p.categoryId) };
    }));
    calculateStats(); clearSelection();
  } catch (error) { ElMessage.error('加载失败'); } finally { loading.value = false; }
};

const approvePost = async (post: any) => {
  try {
    const res = await approvePostAPI(post.postId);
    if (res.code === 200) { ElMessage.success('已审核通过'); const idx = posts.value.findIndex(p => p.postId === post.postId); if (idx !== -1) { posts.value[idx].status = 1; calculateStats(); } }
  } catch {}
}

const rejectPost = async (post: any) => {
  try {
    await ElMessageBox.confirm('确定要驳回该帖子吗？', '提示');
    const res = await rejectPostAPI(post.postId);
    if (res.code === 200) { ElMessage.warning('帖子已驳回'); const idx = posts.value.findIndex(p => p.postId === post.postId); if (idx !== -1) { posts.value[idx].status = 2; calculateStats(); } }
  } catch {}
}

const loadCategories = async () => {
  try { const res = await getForumCategories(); const raw = res.data?.data || res.data || []; categories.value = raw.map((cat: any) => ({ id: Number(cat.categoryId), name: cat.categoryName || cat.name })); } catch { categories.value = []; }
};

const getCategoryName = (id: number) => { const cat = categories.value.find(c => c.id === id); return cat ? cat.name : (id === 0 ? '默认话题' : `话题${id}`); };
const calculateStats = () => { forumStats.value = { totalPosts: posts.value.length, pendingPosts: posts.value.filter(p => p.status === 0).length, approvedPosts: posts.value.filter(p => p.status === 1).length } }
const handleDelete = async (post: any) => {
  try {
    await ElMessageBox.confirm('确定删除吗？', '警告');
    const res = await deletePostAPI(post.postId);
    if (res.code !== 200) {
      ElMessage.error(res.msg || '删除失败');
      return;
    }
    posts.value = posts.value.filter(item => item.postId !== post.postId);
    calculateStats();
    ElMessage.success('已删除');
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除失败');
    }
  }
}
const viewPostDetail = (post: any) => { selectedPost.value = post; showPostDetailModal.value = true; }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value * pageSize.value < filteredPosts.value.length) currentPage.value++ }

onMounted(loadPosts)
</script>

<style scoped>
.filter-select {
  @apply bg-slate-50 dark:bg-slate-800 border-none rounded-2xl px-5 py-3 text-sm font-bold text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-all;
}

.row-action-btn {
  @apply w-8 h-8 rounded-xl bg-slate-50 dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-white hover:rotate-6 transition-all duration-300;
}

.pagination-btn {
  @apply w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 transition-all shadow-sm;
}

.fade-enter-active, .fade-slide-enter-active { transition: all 0.3s ease; }
.fade-enter-from, .fade-slide-enter-from { opacity: 0; transform: translateY(10px); }

::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }
</style>
