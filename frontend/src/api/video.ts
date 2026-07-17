import request from '@/utils/http';

export interface VideoItem {
  id: number;
  dishId: number;
  title: string;
  coverUrl: string;  
  videoUrl: string;  
  duration: string;
  author: string;
  price: number;
  isUnlocked: boolean;
}

export interface PointLogItem {
  id?: number;        
  userId: number;
  type: number;       // 1: 收入, 2: 支出
  amount: number;
  balance: number;    // 记录当时的剩余积分
  description: string;
  createTime?: string; 
}

export interface UserVideoUnlockItem {
  id?: number;           // 对应后端的id字段
  userId: number;        // 用户ID
  videoId: number;       // 视频ID
  unlockTime?: string;   // 解锁时间
}

export const getDishVideos = (dishId: number, userId?: number) => {
  return request.get<VideoItem[]>(`/api/dish-videos/${dishId}`, { 
    params: { userId } 
  });
};

export const unlockVideoApi = (data: { userId: number; videoId: number }) => {
  return request.post('/api/dish-videos/unlocks', data);
};

// 插入积分流水记录（记账）
export const insertPointLogApi = (data: PointLogItem) => {
  return request.post<number>('/api/dish-videos/point-logs', data);
};
