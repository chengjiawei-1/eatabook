interface LikeState {
  postLikes: Record<number, boolean>; // 帖子点赞状态
  postLikeCounts: Record<number, number>; // 帖子点赞数
  commentLikes: Record<number, boolean>; // 评论点赞状态
  commentLikeCounts: Record<number, number>; // 评论点赞数
}

const LIKE_STATE_KEY = 'forum_like_state';

export const getLikeState = (): LikeState => {
  const stored = localStorage.getItem(LIKE_STATE_KEY);
  if (stored) {
    try {
      const parsed = JSON.parse(stored);
      // 确保返回的对象包含所有必要的字段
      return {
        postLikes: parsed.postLikes || {},
        postLikeCounts: parsed.postLikeCounts || {},
        commentLikes: parsed.commentLikes || {},
        commentLikeCounts: parsed.commentLikeCounts || {}
      };
    } catch {
      return { postLikes: {}, postLikeCounts: {}, commentLikes: {}, commentLikeCounts: {} };
    }
  }
  return { postLikes: {}, postLikeCounts: {}, commentLikes: {}, commentLikeCounts: {} };
};

// 获取点赞状态
export const getPostLikeState = (postId: number) => {
  try {
    const state = localStorage.getItem(LIKE_STATE_KEY);
    if (!state) return { liked: false, likeCount: 0 };
    
    const parsed = JSON.parse(state);
    return {
      liked: parsed.postLikes?.[postId] || false,
      likeCount: parsed.postLikeCounts?.[postId] || 0
    };
  } catch (e) {
    console.error('获取点赞状态失败:', e);
    return { liked: false, likeCount: 0 };
  }
};

// 更新点赞状态
export const updatePostLikeState = (postId: number, liked: boolean, likeCount: number) => {
  try {
    const state = localStorage.getItem(LIKE_STATE_KEY);
    const parsed = state ? JSON.parse(state) : { postLikes: {}, postLikeCounts: {} };
    
    // 确保对象存在
    parsed.postLikes = parsed.postLikes || {};
    parsed.postLikeCounts = parsed.postLikeCounts || {};
    
    parsed.postLikes[postId] = liked;
    parsed.postLikeCounts[postId] = likeCount;
    
    localStorage.setItem(LIKE_STATE_KEY, JSON.stringify(parsed));
    
    // 触发存储事件，通知其他标签页更新
    window.dispatchEvent(new StorageEvent('storage', {
      key: LIKE_STATE_KEY,
      newValue: JSON.stringify(parsed)
    }));
  } catch (e) {
    console.error('更新点赞状态失败:', e);
  }
};

export const updateCommentLikeState = (commentId: number, liked: boolean, likeCount: number) => {
  const state = getLikeState();
  state.commentLikes[commentId] = liked;
  state.commentLikeCounts[commentId] = likeCount;
  localStorage.setItem(LIKE_STATE_KEY, JSON.stringify(state));
};

export const getCommentLikeState = (commentId: number): { liked: boolean; likeCount: number } => {
  const state = getLikeState();
  return {
    liked: state.commentLikes[commentId] || false,
    likeCount: state.commentLikeCounts[commentId] || 0
  };
};

export const clearLikeState = () => {
  localStorage.removeItem(LIKE_STATE_KEY);
};