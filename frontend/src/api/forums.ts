import httpInstance from "@/utils/http"

// 帖子类型（匹配后端ForumPost）
interface ForumPost {
  postId: number; // 改为postId，匹配后端字段
  title: string;
  content: string;
  userId: number;
  categoryId: number;
  status: number; // 0: 审核中, 1: 已发布, 2: 未通过
  likeCount: number;
  commentCount: number;
  favoriteCount: number;
  createTime: string;
  updateTime: string;
  rejectReason?: string;
  username?: string;
  userAvatar?: string;
  categoryName?: string;
  liked?: boolean;
  favorited?: boolean; // 收藏状态
  coverImage?: string;
  images?: string[];
}

// 评论类型
interface ForumComment {
  commentId: number; 
  postId: number;
  content: string;
  userId: number;
  parentId?: number;
  replyUserId?: number;
  createTime?: string;
  username?: string;
  userAvatar?: string;
  likeCount?: number;
  liked?: boolean;
  replyUsername?: string;
  replies?: ForumComment[];
}

// 分类类型
interface ForumCategory {
  categoryId: number;
  categoryCode: string;
  categoryName: string;
  intro?: string;
  sort?: number;
  count?: number;
}

// 搜索记录类型
interface ForumSearchRecord {
  searchId?: number;
  id?: number;
  search_id?: number;
  userId: number;
  searchContent: string;
  searchTime?: string;
  updateTime?: string;
}

// 统一响应格式
interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data?: T;
}

// 工具函数：获取Token
const getAuthHeader = () => {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

// 用户信息接口
export const getUserById = (id: number) => {
  return httpInstance.get<ApiResponse>(`/api/users/${id}`, {
    headers: getAuthHeader()
  });
};

//分类相关
export const getForumCategories = () => {
  return httpInstance.get<ApiResponse<ForumCategory[]>>('/api/forum/categories');
};

export const getCategoryById = async (categoryId: number) => {
  try {
    const allCategories = await getForumCategories();
    console.log('所有分类数据:', allCategories);
    
    // 修复：正确解析分类数据
    let categoriesArray = [];
    if (allCategories.data && typeof allCategories.data === 'object' && allCategories.data.data !== undefined) {
      // 标准响应格式：{ code: 200, data: [...] }
      categoriesArray = allCategories.data.data || [];
    } else if (allCategories.data && Array.isArray(allCategories.data)) {
      // 直接返回数组格式
      categoriesArray = allCategories.data;
    } else {
      // 其他格式
      categoriesArray = [];
    }
    
    const category = categoriesArray.find((cat: ForumCategory) => cat.categoryId === categoryId);
    return category || null;
  } catch (error) {
    console.error('获取分类信息失败:', error);
    return null;
  }
};

// 帖子相关
export const getForumPosts = (params: {
  categoryId?: number;
  keyword?: string;
}) => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts', {
    params,
    headers: getAuthHeader()
  });
};

export const getMyPostsAPI = (status?: number) => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts/me', {
    params: { status },
    headers: getAuthHeader()
  });
};

export const publishForumPost = (data: {
  title: string
  content: string
  categoryId: number
  relatedType?: string
  relatedId?: number
}) => {
  return httpInstance.post<ApiResponse<{postId: number}>>(
    '/api/forum/posts', 
    data,
    { headers: getAuthHeader() }
  );
};

export const getPostDetail = (id: string | number) => {
  return httpInstance.get<ApiResponse<ForumPost>>(`/api/forum/posts/${id}`, {
    headers: getAuthHeader()
  });
};

export const deletePostAPI = (id: string | number) => {
  return httpInstance.delete<ApiResponse>(`/api/forum/posts/${id}`, {
    headers: getAuthHeader()
  });
};

export const togglePostLike = (id: string | number) => {
  return httpInstance.post<ApiResponse>(`/api/forum/posts/${id}/likes`, {}, {
    headers: getAuthHeader()
  });
};

export const togglePostFavorite = (id: string | number) => {
  return httpInstance.post<ApiResponse>(`/api/forum/posts/${id}/favorites`, {}, {
    headers: getAuthHeader()
  });
};

// 根据用户名获取帖子列表
export const getPostsByUsername = (username: string) => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts/by-username', {
    params: { username },
    headers: getAuthHeader()
  });
};

// 根据关注列表查询帖子
export const getPostsByFollowing = () => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts/following', {
    headers: getAuthHeader()
  });
};

// 获取当前用户已发布（审核通过）的帖子
export const getMyPublishedPostsAPI = () => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts/my-published', {
    headers: getAuthHeader()
  });
};

// 获取用户收藏的帖子（新增接口）
export const getFavoritePosts = () => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts/favorites', {
    headers: getAuthHeader()
  });
};

// ========== 评论相关 ==========
export const getPostComments = (params: {
  postId: string | number;
  pageNum: number;
  pageSize: number;
}) => {
  return httpInstance.get<ApiResponse<ForumComment[]>>(`/api/forum/posts/${params.postId}/comments`, {
    params: {
      pageNum: params.pageNum,
      pageSize: params.pageSize
    },
    headers: getAuthHeader()
  });
};

export const publishComment = (data: {
  postId: string | number;
  content: string;
  parentId?: number;
  replyUserId?: number;
}) => {
  return httpInstance.post<ApiResponse>('/api/forum/comments', data, {
    headers: getAuthHeader()
  });
};

export const toggleCommentLike = (id: string | number) => {
  return httpInstance.post<ApiResponse>(`/api/forum/comments/${id}/likes`, {}, {
    headers: getAuthHeader()
  });
};

export const deleteCommentAPI = (id: string | number) => {
  return httpInstance.delete<ApiResponse>(`/api/forum/comments/${id}`, {
    headers: getAuthHeader()
  });
};

// 图片上传
export function uploadPostImages(postId: number, files: File[]) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })

  return httpInstance({
    url: `/api/forum/posts/${postId}/images`,
    method: 'post',
    data: formData,
    headers: {
      ...getAuthHeader(),
      'Content-Type': 'multipart/form-data' // 添加上传文件的Content-Type
    }
  })
}

// 搜索记录相关（修正接口路径）
export const saveSearchRecord = (record: {
  userId: number;
  searchContent: string;
}) => {
  return httpInstance.post<ApiResponse>('/api/forum/search-records', record, {
    headers: getAuthHeader()
  });
};

export const getSearchRecords = (userId: number) => {
  return httpInstance.get<ApiResponse<ForumSearchRecord[]>>('/api/forum/search-records', {
    params: { userId },
    headers: getAuthHeader()
  });
};

// 搜索记录删除相关

// 删除用户全部搜索记录
export const deleteUserSearchHistory = (userId: number) => {
  return httpInstance.delete<ApiResponse<number>>(`/api/forum/users/${userId}/search-records`, {
    headers: getAuthHeader()
  });
};

// 删除特定搜索记录
export const deleteSearchRecordById = (searchId: number) => {
  return httpInstance.delete<ApiResponse<number>>(`/api/forum/search-records/${searchId}`, {
    headers: getAuthHeader()
  });
};

// 删除用户特定搜索内容记录
export const deleteSearchRecordByContent = (userId: number, searchContent: string) => {
  return httpInstance.delete<ApiResponse<number>>('/api/forum/search-records', {
    params: { userId, searchContent },
    headers: getAuthHeader()
  });
};

// 高赞帖子
export const getTopPosts = () => {
  return httpInstance.get<ApiResponse<ForumPost[]>>('/api/forum/posts/top');
};

// 帖子管理

// 1. 管理员获取全量帖子列表
export const getAdminPostsAPI = () => {
  return httpInstance.get('/api/forum/admin/posts', {
    headers: getAuthHeader() // 添加Authorization头
  })
}

// 审核通过
export const approvePostAPI = (postId: number) => {
  return httpInstance.put(`/api/forum/posts/${postId}/approve`, {}, {
    headers: getAuthHeader() // 添加Authorization头
  })
}

// 审核驳回
export const rejectPostAPI = (postId: number) => {
  return httpInstance.put(`/api/forum/posts/${postId}/reject`, {}, {
    headers: getAuthHeader() // 添加Authorization头
  })
}

// 状态展示
export const findPostStatus = (userId : number) => {
  return httpInstance.get(`/api/forum/users/${userId}/favorite-post-ids`, {
    headers: getAuthHeader() // 添加Authorization头
  })
}

// 推荐帖子
export const getRecommendPosts = (params?: {
  limit?: number
  userId?: number
  lastPostId?: number
}) => {
  return httpInstance.get<ApiResponse<{
    list: ForumPost[];
    hasMore: boolean;
  }>>(
    '/api/forum/recommend/posts',
    {
      params,
      headers: getAuthHeader()
    }
  )
}

// 推荐帖子缓存管理
export const clearRecommendCache = () => {
  return httpInstance.delete<ApiResponse>('/api/forum/recommend/cache', {
    headers: getAuthHeader()
  });
};

// 导出类型
export type {
  ForumPost,
  ForumComment,
  ForumCategory,
  ForumSearchRecord,
  ApiResponse
};
