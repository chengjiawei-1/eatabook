import httpInstance from "@/utils/http";

// 类型定义

// 登录参数
interface LoginParams {
  username: string;  
  password: string;
}

// 注册参数
interface RegisterParams {
  username: string;
  email: string;
  password: string;
  nativePlace: string;
  phone: string;
  avatarUrl: string;
  birthday: Date;
  city:String
}

// 用户信息接口 
export interface UserInfo {
  userId: number;           
  username: string;
  nativePlace?: string;
  password: string;
  phone?: string; 
  email: string;
  birthday: Date; 
  registerTime?: string;
  avatarUrl?: string;
  updateTime?: string;
  role?: string;
  lastLoginTime?: string;
  nextTestTime?: string;
  content?: string;
  city?: string;
}  

// 提醒设置参数
interface ReminderParams {
  userId: number;
  time: string; // "yyyy-MM-dd HH:mm:ss" 格式
}

// 通用响应类型
export interface ApiResponse<T> {  
  code: number;
  msg: string;
  data: T;
}

//POST /api/users/login  
export const loginAPI = (data: LoginParams) => {
  return httpInstance.post('/api/users/login', data);
};

//POST /api/users/register 
export interface RegisterResponse {
  code: string;
  msg: string;
  data?:any;
}
export const registerAPI = (data: RegisterParams): Promise<RegisterResponse> => {
  return httpInstance.post('/api/users/register', data);
};

//GET /api/users
export const getAllUserAPI = (): Promise<ApiResponse<UserInfo[]>> => {
  return httpInstance.get('/api/users')
}


//GET /api/users/{id}
export const getUserInfoAPI = (userId: number | string) => {
  return httpInstance.get<ApiResponse<UserInfo>>(`/api/users/${userId}`);
}

//POST /api/users/avatars
export const uploadAvatarAPI = (formData: FormData) => {
  return httpInstance.post<any>('/api/users/avatars', formData);
}

//PUT /api/users/{id}
export const updateUserAPI = (data: Partial<UserInfo>) => {
  return httpInstance.put(`/api/users/${data.userId}`, data);
}

//PUT /api/users/{id}/password
export const updatePasswordAPI = (id: number | string, password: string) => {
  return httpInstance.put(`/api/users/${id}/password`, null, {
    params: {
      password
    }
  });
}

//PUT /api/users/{id}/last-login-time
export const updateLastLoginTimeAPI = (userId: number | string) => {
  return httpInstance.put(`/api/users/${userId}/last-login-time`);
}

//DELETE /api/users/{id}
export const deleteUserAPI = (id: number | string) => {
  return httpInstance.delete(`/api/users/${id}`);
};

//PUT /api/users/{userId}/test-reminder
export const setReminderAPI = (userId: number | string, time: string) => {
  return httpInstance.put<ApiResponse<any>>(`/api/users/${userId}/test-reminder`, { 
    time 
  });
};

/**
 * 增加用户积分
 * @param userId 用户ID
 * @param amount 积分数额
 */
export async function addUserPoints(userId: number, amount: number): Promise<number> {
  try {
    // 1. 防御性转换：确保传给后端的数值一定是数字类型，避免后端 500
    const cleanUserId = Number(userId);
    const cleanAmount = Number(amount);

    if (isNaN(cleanUserId)) throw new Error("Invalid UserID");

    const queryParams = {
      amount: cleanAmount 
    };

    console.log("🚀 正在请求积分接口...", queryParams);

    // 2. 保持 params 传参模式（对应后端的 @RequestParam）
    const data: number = await httpInstance.post(`/api/users/${cleanUserId}/points`, null, { 
      params: queryParams,
      timeout: 5000 // 增加 5 秒超时限制，防止接口卡死前端
    });

    return data;
  } catch (err) {
    // 3. 详细日志记录，方便排查到底是 Axios 报错还是业务报错
    console.error("❌ addUserPoints 接口调用彻底失败:", err);
    throw err;
  }
}

//社交关系与内容管理 API

//GET /api/users/{userId}/stats
export const getUserStatsAPI = (userId: number | string) => {
  return httpInstance.get<ApiResponse<any>>(`/api/users/${userId}/stats`);
}

//PUT /api/users/me/following/{targetUserId}
export const followUserAPI = (targetUserId: number) => {
  return httpInstance.put(`/api/users/me/following/${targetUserId}`);
};

//DELETE /api/users/me/following/{targetUserId}
export const unfollowUserAPI = (targetUserId: number) => {
  return httpInstance.delete(`/api/users/me/following/${targetUserId}`);
};

//GET /api/users/{currentUserId}/following/{targetUserId}
export const checkFollowStatusAPI = (currentUserId: number, targetUserId: number) => {
  return httpInstance.get(`/api/users/${currentUserId}/following/${targetUserId}`);
}

//GET /api/users/{userId}/following
export const getFollowListAPI = (userId: number | string, params?: any) => {
  return httpInstance.get(`/api/users/${userId}/following`, { params });
}

//GET /api/users/{userId}/followers
export const getFansListAPI = (userId: number | string, params?: any) => {
  return httpInstance.get(`/api/users/${userId}/followers`, { params });
}
