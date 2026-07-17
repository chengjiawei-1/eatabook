import { defineStore } from "pinia";
import { loginAPI, registerAPI, type RegisterParams, updateLastLoginTimeAPI } from "@/api/user"; 
import { UserInfo } from "@/api/user";
// 请确保你的路由文件路径是 @/router 或者 @/router/index
import router from "@/router"; 

interface User extends UserInfo {
  id?: number;
  username: string;
  email?: string;
  avatarUrl?: string;
  role?: string;
  lastLoginTime?: string;
  [key: string]: any;
}

export const useAuthStore = defineStore("auth", {
  // State
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('userInfo') || 'null') as User | null,
    isAuthenticated: !!localStorage.getItem('token'),
  }),

  // Getters
  getters: {
    isAdmin(): boolean {
      return this.user?.role === 'admin';
    },
    formattedLastLoginTime(): string {
      if (!this.user?.lastLoginTime) return '从未登录';
      
      const lastLogin = new Date(this.user.lastLoginTime);
      const now = new Date();
      const diffMs = now.getTime() - lastLogin.getTime();
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
      
      if (diffDays === 0) {
        return '今天 ' + lastLogin.toLocaleTimeString('zh-CN', { 
          hour: '2-digit', 
          minute: '2-digit' 
        });
      } else if (diffDays === 1) {
        return '昨天 ' + lastLogin.toLocaleTimeString('zh-CN', { 
          hour: '2-digit', 
          minute: '2-digit' 
        });
      } else if (diffDays < 7) {
        return `${diffDays}天前`;
      } else {
        return lastLogin.toLocaleDateString('zh-CN');
      }
    }
  },
  
  // Actions
  actions: {
    async login(username: string, pass: string) {
      try {
        const res: any = await loginAPI({ username, password: pass });
        if (res.code !== 200) throw new Error(res.msg || '登录失败');

        const { token, userInfo } = res.data;

        // 这个时间是“上一次”的登录时间
        const oldLoginTime = userInfo.lastLoginTime; 

        // 先存 Token，防止后面请求 401
        this.token = token;
        localStorage.setItem('token', token);

        try {
          // 更新数据库里的时间
          await updateLastLoginTimeAPI(userInfo.userId);
        } catch (error) {
          console.warn('更新最后登录时间失败:', error);
        }

        // 更新本地状态
        this.user = { ...userInfo, lastLoginTime: new Date().toISOString() };
        this.isAuthenticated = true;
        localStorage.setItem('userInfo', JSON.stringify(this.user));

        // 必须返回一个包含 previousLoginTime 的对象
        return {
          ...userInfo,
          previousLoginTime: oldLoginTime // 将旧时间传给 LoginPage
        };
      } catch (error) {
        this.logout();
        throw error;
      }
    },

    async register(data: RegisterParams) {
      const res: any = await registerAPI(data);
      return res.msg || "注册成功";
    },

    logout() {
      // 清理状态
      this.token = '';
      this.user = null;
      this.isAuthenticated = false;
      
      // 清理缓存
      // 建议只清理相关的，避免误删其他数据，或者保留用 localStorage.clear() 也可以
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      
      // 直接使用导入的 router 实例进行跳转
      // 这样就不会报错 "inject() can only be used inside setup()" 了
      router.replace('/login');
    },
    
    updateUserInfo(userInfo: User) {
      this.user = { ...this.user, ...userInfo };
      localStorage.setItem('userInfo', JSON.stringify(this.user));
    }
  },
});