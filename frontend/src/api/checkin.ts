import request from '@/utils/http';

// 用户打卡实体定义
export interface UserCheckin {
  id?: number;
  userId: number;
  checkinDate: string;    // 格式: "2025-12-27"
  isRewarded: number;     // 0-未领奖励, 1-已领奖励
  currentStreak: number;  // 当前连续打卡天数
  lastCheckinDate?: string | null;
}

// 写入打卡记录
export const insertCheckinApi = (data: UserCheckin) => {
  return request.post<number>('/api/checkins', data);
};

// 获取用户最新的一条记录（用于前端判定连签）
export const findLatestCheckinApi = (userId: number) => {
  return request.get<UserCheckin>(`/api/checkins/users/${userId}/latest`);
};

// 查询日期范围内的打卡日期（用于日历标注）
export const getCheckinRangeApi = (params: { userId: number; startDate: string; endDate: string }) => {
  return request.get<string[]>('/api/checkins', { params });
};

// 检查今天是否已打卡
export const countTodayCheckinApi = (params: { userId: number; today: string }) => {
  return request.get<number>('/api/checkins/today/count', { params });
};

// 打卡总数
export const totalCheckinsApi = () => {
  return request.get<number>('/api/checkins/count');
};

//今日打卡数
export const todayCheckinsApi = () => {
  return request.get<number>('/api/checkins/today/total');
};

//昨日打卡数
export const yesterdayCheckinsApi = () => {
  return request.get<number>('/api/checkins/yesterday/total');
};
