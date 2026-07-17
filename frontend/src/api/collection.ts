import axios from 'axios';
import { getDishById } from './dishes';

// API基础URL - 使用相对路径走 Vite 代理
const API_BASE_URL = '/api';

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    console.error('API请求错误:', error);
    return Promise.reject(error);
  }
);

// 类型定义
export interface UserCollections {
  collectionId: number;
  userId: number;
  targetType: 'dish' | 'book' | 'ingredient';
  targetId: number;
  collectionTime: string;
  updateTime: string;
}

// 菜品详情类型
export interface DishDetail {
  dishId: number;
  dishName: string;
  dishSource?: string;
  effectTag1?: string;
  effectTag2?: string;
  effectTag3?: string;
  effectTag4?: string;
  effectTag5?: string;
  suitableConstitution?: string;
  unsuitableConstitution?: string;
  ancientCitation?: string;
  kind?: string;
  dishImgUrl?: string;
  collectCount?: number;
  [key: string]: any;
}

export interface CollectionResponse<T = any> {
  code: number;
  msg: string;
  data?: T;
  count?: number;
  [key: string]: any;
}

// 收藏API接口
export const collectionApi = {
  /**
   * 添加收藏
   */
  addCollection: (userCollection: {
    userId: number;
    targetType: string;
    targetId: number;
  }): Promise<CollectionResponse<UserCollections>> => {
    // 设置默认时间
    const now = new Date().toISOString();
    const collectionData = {
      ...userCollection,
      collectionTime: now,
      updateTime: now
    };
    
    return api.post('/collections', collectionData);
  },

  /**
   * 取消收藏
   */
  cancelCollection: (userId: number, collectionId: number): Promise<CollectionResponse> => {
    return api.delete(`/collections/${collectionId}`, {
      params: { userId }
    });
  },

  /**
   * 获取用户所有收藏
   */
  getUserCollections: (userId: number): Promise<CollectionResponse<UserCollections[]>> => {
    return api.get(`/collections/users/${userId}`);
  },

  /**
   * 根据用户ID和收藏类型查询收藏记录
   */
  getUserCollectionsByType: (
    userId: number, 
    targetType: string
  ): Promise<CollectionResponse<UserCollections[]>> => {
    return api.get(`/collections/users/${userId}/types/${targetType}`);
  },

  /**
   * 切换收藏状态
   */
  toggleCollection: async (
    userId: number, 
    targetType: string, 
    targetId: number
  ): Promise<{success: boolean; isCollected?: boolean; collectionId?: number}> => {
    try {
      // 1. 先获取用户的所有收藏记录
      const response = await collectionApi.getUserCollectionsByType(userId, targetType);
      
      if (response.code === 200 && response.data) {
        // 2. 检查是否已经收藏
        const existingCollection = response.data.find(
          (item: UserCollections) => item.targetId === targetId
        );
        
        if (existingCollection) {
          // 3. 已收藏，执行取消收藏
          const cancelRes = await collectionApi.cancelCollection(userId, existingCollection.collectionId);
          if (cancelRes.code === 200) {
            return { success: true, isCollected: false };
          }
        } else {
          // 4. 未收藏，执行添加收藏
          const addRes = await collectionApi.addCollection({ 
            userId, 
            targetType, 
            targetId 
          });
          if (addRes.code === 200 && addRes.data) {
            return { 
              success: true, 
              isCollected: true, 
              collectionId: addRes.data.collectionId 
            };
          }
        }
      }
      
      return { success: false };
    } catch (error) {
      console.error('切换收藏状态失败:', error);
      return { success: false };
    }
  },


  /**
   * 获取用户的收藏菜品详情
   */
  getUserFavoriteDishes: async (
    userId: number
  ): Promise<Array<{
    collectionId: number;
    dishId: number;
    name: string;
    image: string;
    effect: string;
    source: string;
    collectionTime: string;
  }>> => {
    try {
      // 获取收藏记录
      const collectionsResponse = await collectionApi.getUserCollectionsByType(userId, 'dish');
      console.log('收藏记录响应:', collectionsResponse);
      
      if (collectionsResponse.code === 200 && collectionsResponse.data) {
        const collections = collectionsResponse.data;
        const result = [];
        
        // 为每个收藏记录获取菜品详情
        for (const collection of collections) {
          try {
            // 使用 dishes.ts 中的方法获取菜品详情
            const dish = await getDishById(collection.targetId);
            console.log('菜品详情:', dish);
            
            if (dish) {
              const effectTags = [
                dish.effectTag1,
                dish.effectTag2,
                dish.effectTag3
              ].filter(Boolean).join('、');
              
              result.push({
                collectionId: collection.collectionId,
                dishId: dish.dishId,
                name: dish.dishName,
                image: dish.dishImgUrl || '',
                effect: effectTags || '暂无描述',
                source: dish.dishSource || '',
                collectionTime: collection.collectionTime
              });
            }
          } catch (err) {
            console.error(`获取菜品 ${collection.targetId} 详情失败:`, err);
          }
        }
        
        return result;
      }
      
      return [];
    } catch (error) {
      console.error('获取用户收藏菜品失败:', error);
      return [];
    }
  }
};

export default collectionApi;
