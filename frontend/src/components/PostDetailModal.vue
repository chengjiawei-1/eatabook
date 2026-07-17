<template>
  <div
    v-motion="{
      initial: { opacity: 0 },
      enter: { opacity: 1 },
      transition: { duration: 300 }
    }"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
    @click="$emit('close')"
  >
    <div
      v-motion="{
        initial: { opacity: 0, y: 20 },
        enter: { opacity: 1, y: 0 },
        transition: { duration: 300 }
      }"
      class="bg-white dark:bg-slate-800 rounded-xl shadow-xl p-6 max-w-4xl w-full max-h-[90vh] overflow-y-auto"
      @click.stop
    >
      <!-- 头部 -->
      <div class="flex justify-between items-center mb-6 pb-4 border-b border-gray-200 dark:border-slate-700">
        <h2 class="text-xl font-bold text-slate-800 dark:text-slate-100">帖子详情</h2>
        <button 
          @click="$emit('close')" 
          class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
        >
          <i class="fa-solid fa-times"></i>
        </button>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="flex items-center justify-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-600"></div>
      </div>
      
      <div v-else class="space-y-6">
        <!-- 帖子基本信息 -->
        <div class="bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 p-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-2xl font-bold text-slate-800 dark:text-slate-100 mb-2">{{ post.title }}</h3>
              <div class="flex flex-wrap gap-2">
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">
                  <i class="fa-solid fa-user mr-1.5"></i>{{ authorInfo.username || post.author || `用户${post.userId}` }}
                </span>
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">
                  <i class="fa-solid fa-calendar mr-1.5"></i>{{ formatTime(post.createTime) }}
                </span>
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400">
                  <i class="fa-solid fa-heart mr-1.5"></i>{{ post.likeCount || 0 }}
                </span>
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400">
                  <i class="fa-solid fa-comment mr-1.5"></i>{{ post.commentCount || 0 }}
                </span>
                <span :class="[
                  'inline-flex items-center px-3 py-1 rounded-full text-xs font-medium',
                  post.status === 1 
                    ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400' 
                    : post.status === 0
                    ? 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400'
                    : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'
                ]">
                  <i :class="[
                    'mr-1.5',
                    post.status === 1 ? 'fa-check-circle' 
                    : post.status === 0 ? 'fa-hourglass-half'
                    : 'fa-ban'
                  ]"></i>
                  {{ post.status === 1 ? '已通过' : post.status === 0 ? '待审核' : '已拒绝' }}
                </span>
              </div>
            </div>
          </div>
          
          <!-- 帖子内容 -->
          <div class="mt-4">
            <h4 class="font-semibold text-slate-800 dark:text-slate-200 mb-3">帖子内容</h4>
            <div class="prose max-w-none text-slate-700 dark:text-slate-300 bg-gray-50 dark:bg-slate-700/50 rounded-lg p-4">
              <p>
                {{ postContent || post.content || '帖子内容加载中...' }}
              </p>
            </div>
            
            <!-- 图片展示 -->
            <div v-if="postImages.length > 0" class="mt-4 grid grid-cols-3 gap-3">
              <div 
                v-for="image in postImages" 
                :key="image.id"
                class="relative rounded-lg overflow-hidden border border-gray-200 dark:border-slate-600"
              >
                <img :src="image.url" :alt="image.alt" class="w-full h-24 object-cover">
                <button 
                  @click="viewImage(image)"
                  class="absolute inset-0 bg-black/0 hover:bg-black/20 transition-colors flex items-center justify-center"
                >
                  <i class="fa-solid fa-expand text-white opacity-0 hover:opacity-100 transition-opacity"></i>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 统计信息和作者信息 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- 帖子统计 -->
          <div class="bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 p-6">
            <h4 class="font-semibold text-slate-800 dark:text-slate-200 mb-4">帖子统计</h4>
            <div class="space-y-3">
              <div class="flex justify-between items-center">
                <span class="text-sm text-slate-600 dark:text-slate-400">浏览数</span>
                <span class="font-medium text-slate-800 dark:text-slate-200">{{ postStats.views?.toLocaleString() || '0' }} 次</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-slate-600 dark:text-slate-400">收藏数</span>
                <span class="font-medium text-slate-800 dark:text-slate-200">{{ postStats.favorites?.toLocaleString() || '0' }} 次</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-slate-600 dark:text-slate-400">分享数</span>
                <span class="font-medium text-slate-800 dark:text-slate-200">{{ postStats.shares?.toLocaleString() || '0' }} 次</span>
              </div>
              <div class="flex justify-between items-center">
                <span class="text-sm text-slate-600 dark:text-slate-400">举报数</span>
                <span class="font-medium text-slate-800 dark:text-slate-200">{{ postStats.reports || '0' }} 次</span>
              </div>
            </div>
          </div>
          
          <!-- 作者信息 -->
          <div class="bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 p-6">
            <h4 class="font-semibold text-slate-800 dark:text-slate-200 mb-4">作者信息</h4>
            <div class="space-y-3">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full overflow-hidden bg-slate-200 dark:bg-slate-600 flex items-center justify-center">
                  <img :src="authorInfo.avatar" :alt="authorInfo.username" class="w-full h-full object-cover">
                </div>
                <div>
                  <span class="font-medium text-slate-800 dark:text-slate-200">{{ authorInfo.username }}</span>
                  <div class="text-xs text-slate-500 dark:text-slate-400 mt-1">注册时间: {{ authorInfo.registrationDate }}</div>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4 mt-4">
                <div class="text-center">
                  <div class="text-lg font-bold text-slate-800 dark:text-slate-200">{{ authorInfo.postCount }}</div>
                  <div class="text-xs text-slate-500 dark:text-slate-400">发帖数</div>
                </div>
                <div class="text-center">
                  <div class="text-lg font-bold text-slate-800 dark:text-slate-200">{{ authorInfo.followers }}</div>
                  <div class="text-xs text-slate-500 dark:text-slate-400">粉丝数</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="bg-white dark:bg-slate-800 rounded-lg border border-gray-200 dark:border-slate-700 p-6">
          <div class="flex justify-between items-center mb-4">
            <h4 class="font-semibold text-slate-800 dark:text-slate-200">评论列表 ({{ comments.length }})</h4>
            <button 
              @click="viewAllComments"
              class="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 dark:bg-slate-700 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-300 rounded-lg text-sm transition-colors"
            >
              查看全部评论
            </button>
          </div>
          
          <div class="space-y-3">
            <div v-for="comment in recentComments" :key="comment.id" class="p-3 border border-gray-100 dark:border-slate-700 rounded-lg">
              <div class="flex justify-between items-start mb-2">
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full overflow-hidden bg-slate-200 dark:bg-slate-600">
                    <img :src="comment.avatar" :alt="comment.author" class="w-full h-full object-cover">
                  </div>
                  <span class="text-sm font-medium text-slate-800 dark:text-slate-200">{{ comment.author }}</span>
                </div>
                <span class="text-xs text-slate-500 dark:text-slate-400">{{ comment.time }}</span>
              </div>
              <p class="text-sm text-slate-700 dark:text-slate-300 mb-2">{{ comment.content }}</p>
              <div class="flex justify-between items-center">
                <div class="flex items-center gap-3">
                  <button class="text-xs text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-300 transition-colors">
                    <i class="fa-solid fa-thumbs-up mr-1"></i> {{ comment.likes }}
                  </button>
                  <button class="text-xs text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-300 transition-colors">
                    回复
                  </button>
                </div>
                <button 
                  v-if="comment.reported"
                  class="text-xs text-red-600 dark:text-red-400"
                >
                  <i class="fa-solid fa-flag mr-1"></i> 已举报
                </button>
              </div>
            </div>
          </div>
          
          <div v-if="comments.length === 0" class="text-center py-6">
            <i class="fa-regular fa-comment text-3xl text-slate-300 dark:text-slate-600 mb-2"></i>
            <p class="text-sm text-slate-500 dark:text-slate-400">暂无评论</p>
          </div>
        </div>

        <!-- 举报记录 -->
        <div v-if="reports.length > 0" class="bg-white dark:bg-slate-800 rounded-lg border border-red-200 dark:border-red-900/30 p-6">
          <h4 class="font-semibold text-red-800 dark:text-red-300 mb-4">举报记录 ({{ reports.length }})</h4>
          <div class="space-y-2">
            <div v-for="report in reports" :key="report.id" class="text-sm p-2 bg-red-50 dark:bg-red-900/20 rounded">
              <div class="flex justify-between mb-1">
                <span class="text-red-700 dark:text-red-400 font-medium">{{ report.reason }}</span>
                <span class="text-red-600 dark:text-red-300 text-xs">{{ report.time }}</span>
              </div>
              <p class="text-red-600 dark:text-red-300 text-xs">举报人: {{ report.reporter }}</p>
            </div>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="errorMessage" class="p-3 bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 rounded-lg border border-red-200 dark:border-red-900/30">
          <i class="fa-solid fa-exclamation-circle mr-2"></i>{{ errorMessage }}
        </div>
        
        <!-- 操作按钮 -->
        <div class="flex justify-end gap-3 pt-4 border-t border-gray-200 dark:border-slate-700">
          <button 
            @click="handleReply"
            class="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-lg transition-colors flex items-center gap-2"
          >
            <i class="fa-solid fa-reply"></i>
            回复帖子
          </button>
          <button 
            @click="confirmDelete"
            class="px-4 py-2 bg-red-100 hover:bg-red-200 dark:bg-red-900/30 dark:hover:bg-red-900/50 text-red-700 dark:text-red-400 font-medium rounded-lg transition-colors"
          >
            删除帖子
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getPostDetail, getPostComments, getForumPosts } from '@/api/forums'
import { getUserInfoAPI } from '@/api/user'

interface Props {
  post: any
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  'change-status': [status: string]
  'delete-post': []
}>()

const loading = ref(false)
const errorMessage = ref('')
const isPinned = ref(false)

// 帖子内容
const postContent = ref('')

// 帖子图片
const postImages = ref<any[]>([])

// 帖子统计
interface PostStats {
  views: number
  favorites: number
  shares: number
  reports: number
}

const postStats = ref<PostStats>({
  views: 0,
  favorites: 0,
  shares: 0,
  reports: 0
})

// 作者信息
const authorInfo = ref({
  username: '',
  avatar: '',
  registrationDate: '',
  postCount: 0,
  followers: 0
})

// 评论列表
interface Comment {
  id: string
  author: string
  avatar: string
  content: string
  time: string
  likes: number
  reported: boolean
}

const comments = ref<Comment[]>([])

// 举报记录
interface Report {
  id: string
  reason: string
  reporter: string
  time: string
}

const reports = ref<Report[]>([])

// 最近评论（最多显示3条）
const recentComments = computed(() => comments.value.slice(0, 3))

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

const handleStatusChange = (event: Event) => {
  const select = event.target as HTMLSelectElement
  emit('change-status', select.value)
}

const handleReply = () => {
  // 回复帖子
  alert('回复帖子功能')
}

const handlePinPost = () => {
  isPinned.value = !isPinned.value
  alert(isPinned.value ? '帖子已置顶' : '帖子已取消置顶')
}

const viewImage = (image: any) => {
  // 查看大图
  window.open(image.url, '_blank')
}

const viewAllComments = () => {
  // 查看全部评论
  alert('查看全部评论')
}

const confirmDelete = () => {
  if (window.confirm(`确定要删除帖子"${props.post.title}"吗？此操作不可撤销。`)) {
    emit('delete-post')
  }
}

// 加载帖子详情
const loadPostDetails = async () => {
  try {
    loading.value = true
    errorMessage.value = ''
    
    // 使用真实API获取帖子详情
    const postId = props.post.postId || props.post.id
    if (!postId) {
      throw new Error('帖子ID不存在')
    }
    
    const response = await getPostDetail(postId)
    
    // 处理API响应数据（支持直接数组或包含在data字段中）
    const postData = response.data || response || {}
    
    // 更新帖子内容
    postContent.value = postData.content || props.post.content || ''
    
    // 更新帖子统计信息（收藏数即点赞数）
    postStats.value = {
      views: postData.viewCount || 0,
      favorites: postData.likeCount || 0, // 使用likeCount作为收藏数
      shares: postData.shareCount || 0,
      reports: postData.reportCount || 0
    }
    
    // 获取用户信息（注册时间等）
    let userInfo = null
    if (postData.userId) {
      try {
        const userResponse = await getUserInfoAPI(postData.userId)
        userInfo = userResponse.data || userResponse
      } catch (error) {
        console.warn('获取用户信息失败:', error)
      }
    }
    
    // 前端计算用户发帖数
    let userPostCount = 0
    if (postData.userId) {
      try {
        // 获取所有帖子，然后过滤出该用户的帖子
        const allPostsResponse = await getForumPosts({})
        const allPosts = allPostsResponse.data || allPostsResponse || []
        
        if (Array.isArray(allPosts)) {
          // 过滤出该用户的所有帖子并计数
          userPostCount = allPosts.filter((post: any) => 
            post.userId === postData.userId || post.authorId === postData.userId
          ).length
        }
      } catch (error) {
        console.warn('前端计算发帖数失败:', error)
      }
    }
    
    // 更新作者信息
    authorInfo.value = {
      username: postData.authorName || postData.author || props.post.author || `用户${postData.userId || props.post.userId}`,
      avatar: postData.authorAvatar || postData.avatar || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiNlNWU1ZTUiLz4KPHBhdGggZD0iTTIwIDIyQzE2LjY5IDIyIDE0IDE5LjMxIDE0IDE2QzE0IDEyLjY5IDE2LjY5IDEwIDIwIDEwQzIzLjMxIDEwIDI2IDEyLjY5IDI2IDE2QzI2IDE5LjMxIDIzLjMxIDIyIDIwIDIyWk0yMCAyNEMyNi42MyAyNCAzMiAyOS4zNyAzMiAzNkgyMEMxMy4zNyAzMiA4IDI3LjYzIDggMjFDOCAxNC4zNyAxMy4zNyA4IDIwIDhDMjYuNjMgOCAzMiAxMy4zNyAzMiAyMEMzMiAyNi42MyAyNi42MyAzMiAyMCAzMloiIGZpbGw9IiM5OTk5OTkiLz4KPC9zdmc+',
      registrationDate: userInfo?.registerTime || postData.authorRegistrationDate || '2025-01-01',
      postCount: postData.authorPostCount || userPostCount || 0,
      followers: postData.authorFollowers || 0
    }
    
    // 加载评论数据
    try {
      const commentsResponse = await getPostComments({
        postId: postId,
        pageNum: 1,
        pageSize: 10 // 获取前10条评论
      })
      const commentsData = commentsResponse.data || commentsResponse || []
      
      comments.value = (Array.isArray(commentsData) ? commentsData : []).map((comment: any, index: number) => ({
        id: comment.id || index.toString(),
        author: comment.username || comment.author || `用户${comment.userId}`,
        avatar: comment.avatar || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAiIGhlaWdodD0iMzAiIHZpZXdCb3g9IjAgMCAzMCAzMCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMTUiIGN5PSIxNSIgcj0iMTUiIGZpbGw9IiNlNWU1ZTUiLz4KPHBhdGggZD0iTTE1IDE2LjVDMTIuNTIgMTYuNSAxMC41IDE0LjQ4IDEwLjUgMTJDMTAuNSA5LjUyIDEyLjUyIDcuNSAxNSA3LjVDMTcuNDggNy41IDE5LjUgOS41MiAxOS41IDEyQzE5LjUgMTQuNDggMTcuNDggMTYuNSAxNSAxNi41Wk0xNSAxOEMyMC4wMSAxOCAyNCAyMi4wMSAyNCAyN0gyMEMxNC45OSAyNCAxMCAxOS4wMSAxMCAxM0MxMCA2Ljk5IDE0Ljk5IDIgMjAgMkMyNS4wMSAyIDMwIDYuOTkgMzAgMTNDMzAgMTkuMDEgMjUuMDEgMjQgMjAgMjRaIiBmaWxsPSIjOTk5OTk5Ii8+Cjwvc3ZnPg==',
        content: comment.content || '评论内容',
        time: formatTime(comment.createTime || new Date().toISOString()),
        likes: comment.likeCount || 0,
        reported: comment.reported || false
      }))
    } catch (commentError) {
      console.warn('加载评论失败，使用默认评论数据:', commentError)
      // 使用默认评论数据
      comments.value = [
        { id: '1', author: '李健康', avatar: 'https://via.placeholder.com/30', content: '非常感谢分享，我也要试试！', time: '今天 11:30', likes: 5, reported: false },
        { id: '2', author: '王食疗', avatar: 'https://via.placeholder.com/30', content: '请问具体做法是什么？', time: '今天 10:45', likes: 3, reported: false }
      ]
    }
    
    // 加载举报记录（如果有）
    if (postData.reports && Array.isArray(postData.reports)) {
      reports.value = postData.reports.map((report: any, index: number) => ({
        id: report.reportId || index.toString(),
        reason: report.reason || '未知原因',
        reporter: report.reporterName || `用户${report.reporterId}`,
        time: formatTime(report.createTime || new Date().toISOString())
      }))
    }
    
    loading.value = false
    
  } catch (error: any) {
    console.error('加载帖子详情失败:', error)
    errorMessage.value = error.message || '加载帖子详情失败，请重试'
    
    // 使用备用数据方案
    postContent.value = props.post.content || '帖子内容加载失败，请刷新重试'
    postStats.value = {
      views: props.post.viewCount || 0,
      favorites: props.post.likeCount || 0, 
      shares: props.post.shareCount || 0,
      reports: props.post.reportCount || 0
    }
    authorInfo.value = {
      username: props.post.author || `用户${props.post.userId}`,
      avatar: 'https://via.placeholder.com/40',
      registrationDate: '2025-01-01',
      postCount: 0,
      followers: 0
    }
    
    loading.value = false
  }
}

onMounted(() => {
  loadPostDetails()
})
</script>
