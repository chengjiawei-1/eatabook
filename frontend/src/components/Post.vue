<template>
  <section class="top-posts">
    <div class="header">
      <div>
        <h2 class="font-serif-title text-3xl md:text-4xl text-amber-800 dark:text-amber-300 flex items-center gap-3">
          <i class="fa-solid fa-fire-flame-curved text-orange-500 text-2xl"></i>
          社区高赞分享
        </h2>
        <p class="subtitle font-sans-body mt-2 text-amber-700/60">基于社区真实数据精选</p>
      </div>

      <button class="more-btn font-sans-body hover:bg-amber-100 transition-colors" @click="goForum">
        看看大家都在说什么 →
      </button>
    </div>
    <!-- 加载态 -->
    <div v-if="loading" class="loading">
      正在加载高赞帖子...
    </div>

    <!-- 帖子卡片 -->
    <div v-else class="post-grid">
      <div
        v-for="post in (posts || []).slice(0, 3)"
        :key="post.postId"
        class="post-card"
        @click="goDetail(post.postId)"
      >
        <!-- 封面 -->
        <img
          v-if="post.coverImage"
          :src="post.coverImage"
          class="cover"
        />

        <h3 class="title font-serif-title group-hover:text-amber-700 transition-colors">{{ post.title }}</h3>

        <p class="content">
          {{ preview(post.content) }}
        </p>

        <div class="card-footer">
          <div class="user-info">
            <img 
              :src="post.userAvatar || post.avatarUrl || defaultAvatar" 
              class="user-avatar"
              alt="用户头像"
              @error="handleImageError"
            />
            <span class="username">{{ post.username || post.userName || '未知用户' }}</span>
          </div>
          
          <div class="meta">
            <span>💬 {{ post.commentCount || 0 }}</span>
            <span>❤️ {{ post.likeCount || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTopPosts, getUserById } from '@/api/forums'
import { useRouter } from 'vue-router'

const router = useRouter()

const posts = ref([])
const loading = ref(true)
// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iODAiIHI9IjQwIiBmaWxsPSIjZTllOWU5Ii8+CiAgPGNpcmNsZSBjeD0iMTAwIiBjeT0iMTUwIiByPSIzMCIgZmlsbD0iI2U5ZTllOSIvPgo8L3N2Zz4='

/* 获取点赞前三帖子 */
const fetchTop3 = async () => {
  try {
    const res = await getTopPosts()
    console.log('接口返回:', res)

    let postsData = []
    if (res && res.data && Array.isArray(res.data)) {
      postsData = res.data
    } else if (Array.isArray(res)) {
      postsData = res
    } else {
      postsData = []
    }

    // 获取每个帖子的用户信息
    const postsWithUserInfo = await Promise.all(postsData.map(async (post) => {
      try {
        // 如果帖子有userId字段，尝试获取用户信息
        if (post.userId) {
          const userRes = await getUserById(post.userId)
          console.log(`用户 ${post.userId} 的信息:`, userRes)
          
          // 根据实际API返回的字段名提取用户信息
          const userInfo = userRes.data || userRes
          
          return {
            ...post,
            username: userInfo.username || userInfo.userName || post.username || '未知用户',
            userAvatar: userInfo.avatarUrl || userInfo.avatar_url || userInfo.avatar || userInfo.userAvatar || post.userAvatar
          }
        }
        return post
      } catch (error) {
        console.error(`获取用户ID ${post.userId} 的信息失败:`, error)
        return post
      }
    }))

    posts.value = postsWithUserInfo
  } catch (e) {
    console.error('获取高赞帖子失败', e)
    posts.value = []
  } finally {
    loading.value = false
  }
}

// 添加错误处理
const handleImageError = (event) => {
  event.target.src = '/images/default-avatar.png';
};

onMounted(fetchTop3)

/* 内容预览（处理 LONGTEXT / 富文本） */
const preview = (content) => {
  if (!content) return ''
  return content
    .replace(/<[^>]+>/g, '') // 去 HTML
    .slice(0, 60) + '...'
}

/* 时间格式化 */
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').slice(0, 16)
}

const goDetail = (id) => {
  router.push(`/forum-post-detail/${id}`) // 根据您的路由配置调整路径
}

const goForum = () => {
  router.push('/forum')
}
</script>

<style scoped>
.top-posts {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
  min-height: 400px; /* 确保有最小高度 */
}

/* ===== 头部 ===== */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48px;
}

.header h2 {
  font-size: 36px;
  font-weight: 700;
}

.subtitle {
  color: #666;
  margin-top: 8px;
}

.more-btn {
  background: #f3f4f6;
  border: none;
  padding: 10px 18px;
  border-radius: 999px;
  cursor: pointer;
}

/* ===== 卡片 ===== */
.post-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 改为三列布局 */
  gap: 28px;
  position: relative; /* 确保层级正常 */
}

.post-card {
  background: #fff;
  border-radius: 20px;
  padding: 22px;
  cursor: pointer;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  height: 100%; /* 确保所有卡片高度一致 */
}

.post-card:hover {
  transform: translateY(-6px);
}

.cover {
  width: 100%;
  height: auto;
  object-fit: contain;
  border-radius: 14px;
  margin-bottom: 16px;
  max-height: 200px; /* 调整最大高度以适应三列布局 */
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.content {
  font-size: 14px;
  color: #444;
  line-height: 1.6;
  margin-bottom: auto; /* 让内容区域自动占据空间，将底部信息推到底部 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 底部 ===== */
.card-footer {
  display: flex;
  justify-content: space-between; /* 左右分布 */
  align-items: center;
  font-size: 13px;
  color: #666;
  margin-top: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  display: flex;
  gap: 12px;
}

.meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .post-grid {
    grid-template-columns: repeat(2, 1fr); /* 中等屏幕显示两列 */
  }
}

@media (max-width: 640px) {
  .post-grid {
    grid-template-columns: 1fr; /* 小屏幕显示单列 */
  }
  
  .cover {
    max-height: 250px; /* 小屏幕时图片可以更高一些 */
  }
}
/* 增加局部字体强制覆盖 */
.font-serif-title { font-family: 'Noto Serif SC', serif; }
.post-card { border: 1px solid rgba(217, 119, 6, 0.1); }
.post-card:hover { border-color: #d97706; }
</style>