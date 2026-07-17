// src/api/foodIngredients.ts
import httpInstance from "@/utils/http";

// 食材实体定义
export interface FoodIngredients {
  ingredientId: number;
  ingredientName: string;
  alias?: string;
  property?: string;
  category?: string;
  ancientEffect?: string;
  ancientOriginText?: string;
  updateTime?: string;
}

// 根据菜品 ID 查询食材
export async function findIngredientsByDishId(dishId: number): Promise<FoodIngredients[]> {
  try {
    // 使用 httpInstance.get
    const res = await httpInstance.get(`/api/food-ingredients/dishes/${dishId}`);
    return res.data || res;
  } catch (err) {
    console.error("findIngredientsByDishId error:", err);
    return [];
  }
}


// 导入食材（文件上传）
export async function importFoodIngredients(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // httpInstance 会自动根据 FormData 设置 Content-Type
    const res = await httpInstance.post(`/api/food-ingredients/imports`, formData);
    return res.data || res;
  } catch (error) {
    console.error('importFoodIngredients error:', error);
    return null;
  }
}

// 导入食材-章节关联
export async function importIngredientBookRel(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const res = await httpInstance.post(`/api/ingredient-book-relations/imports`, formData);
    return res.data || res;
  } catch (error) {
    console.error('importIngredientBookRel error:', error);
    return null;
  }
}

//查询所有食材
export async function findAllIngredients(): Promise<FoodIngredients[]> {
  try {
    const res = await httpInstance.get(`/api/food-ingredients`);
    return res.data || res;
  } catch (err) {
    console.error("findAllIngredients error:", err);
    return [];
  }
}

//删除食材
export async function deleteIngredientById(ingredientId: number): Promise<string | null> {
  try {
    const res = await httpInstance.delete(`/api/food-ingredients/${ingredientId}`);
    return res.data || res;
  } catch (err) {
    console.error("deleteIngredientById error:", err);
    return null;
  }
}


//根据ID查询单个食材详情
export async function getIngredientById(ingredientId: number): Promise<FoodIngredients | null> {
  try {
    const allIngredients = await findAllIngredients();
    return allIngredients.find(ing => ing.ingredientId === ingredientId) || null;
  } catch (err) {
    console.error("getIngredientById error:", err);
    return null;
  }
}

//更新食材信息
export async function updateIngredientById(
  ingredientId: number, 
  ingredientData: Partial<FoodIngredients>
): Promise<string | null> {
  try {
    // 确保不发送ingredientId字段，因为已经在URL中包含了
    const { ingredientId: _, ...dataToSend } = ingredientData;
    console.log('发送更新请求:', {
      ingredientId,
      data: dataToSend
    });
    
    const res = await httpInstance.put(`/api/food-ingredients/${ingredientId}`, dataToSend);
    
    console.log('更新响应:', res.data);
    return res.data || res;
  } catch (err: any) {
    console.error("updateIngredientById error:", err);
    let errorMessage = '更新食材失败';
    
    if (err.response) {
      const status = err.response.status;
      const data = err.response.data;
      
      if (status === 500) {
        errorMessage = `服务器内部错误 (500) - 请检查后端服务日志`;
      } else if (status === 400) {
        errorMessage = `请求参数错误 (400) - ${data || '请检查输入数据'}`;
      } else if (status === 404) {
        errorMessage = `食材不存在 (404) - 食材ID: ${ingredientId}`;
      } else {
        errorMessage = `服务器错误: ${status} - ${data || '未知错误'}`;
      }
    } else if (err.request) {
      errorMessage = '网络错误: 无法连接到服务器';
    } else {
      errorMessage = `请求错误: ${err.message}`;
    }
    throw new Error(errorMessage);
  }
}
//对应后端：@GetMapping("foodingredients/chapter_id/{chapter_id}")
export async function getIngredientsByChapterAll(chapterId: number): Promise<FoodIngredients[]> {
  try {
    const res = await httpInstance.get(`/api/food-ingredients/chapters/${chapterId}`);
    // 根据你的 httpInstance 封装，可能直接返回 res 或 res.data
    return res.data || res;
  } catch (err) {
    console.error("getIngredientsByChapterAll error:", err);
    return [];
  }
}
