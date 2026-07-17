import axios from "axios";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

const httpInstance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '', 
    timeout: 15000
})

// 在请求发送前，自动把 Token 塞到 Header 里
httpInstance.interceptors.request.use(config => {
    // 获取 Store 中的 Token
    const authStore = useAuthStore();
    const token = authStore.token;

    // 如果 Token 存在，就添加到请求头 Authorization 中
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config
}, e => Promise.reject(e))

// 处理后端返回的数据，以及统一处理 401 Token 过期
httpInstance.interceptors.response.use(
    res => res.data,
    e => {
        // 处理 401 错误 
        if (e.response && e.response.status === 401) {
            const authStore = useAuthStore();
            
            // 清理用户信息
            authStore.logout();
            
            // 强制跳转回登录页
            router.push('/login');
        }
        
        return Promise.reject(e)
    }
)

export default httpInstance