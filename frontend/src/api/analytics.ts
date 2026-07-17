import httpInstance from "@/utils/http";

// 记录点击查看详情行为
export async function logClickView(userId: number, dishId: number) {
  return httpInstance.post('/api/analytics/events/dish-views', {
    userId,
    dishId
  });
}

// 记录收藏菜品行为
export async function logFavoriteDish(userId: number, dishId: number, refererSessionId?: string) {
  return httpInstance.post('/api/analytics/events/dish-favorites', {
    userId,
    dishId,
    refererSessionId
  });
}

// 获取首页推荐用户行为分析
export async function getSeasonalRecommendationStats() {
  return httpInstance.get('/api/analytics/stats/seasonal-recommendations');
}

// 获取点击转化率分析
// 获取热门菜品推荐效果
// 获取用户行为小时分布
