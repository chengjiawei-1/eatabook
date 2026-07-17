// src/api/dishes.ts
import httpInstance from "@/utils/http";

// 菜品实体定义
export interface Dish {
  dishId: number;
  dishName: string;
  dishSource: string;

  effectTag1: string;
  effectTag2: string;
  effectTag3: string;
  effectTag4: string;
  effectTag5: string;

  suitableConstitution: string;
  unsuitableConstitution: string;
  ancientCitation: string;

  seasonSpring: number;
  seasonSummer: number;
  seasonAutumn: number;
  seasonWinter: number;

  kind: string;
  dishImgUrl: string;
  updateTime: Date;
}

// 分页结构
export interface PageInfo<T> {
  list: T[];
  pageNum: number;
  pageSize: number;
  total: number;
  pages: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

// 菜品-食材关联实体定义
export interface DishIngredientRel {
  relId: number;
  dishId: number;
  ingredientId: number;
  usageDetail?: string;
}

// 查询全部菜品
export async function getAllDishes(): Promise<Dish[]> {
  try {
    const data: Dish[] = await httpInstance.get('/api/dishes');
    return data;
  } catch (err) {
    console.error("getAllDishes error:", err);
    throw err;
  }
}

// 查询所有菜品-食材关联
export async function findAllDishIngredientRels(): Promise<DishIngredientRel[]> {
  try {
    const data: DishIngredientRel[] = await httpInstance.get('/api/dish-ingredient-relations');
    return data;
  } catch (err) {
    console.error("findAllDishIngredientRels error:", err);
    throw err;
  }
}

//根据搜索内容查询菜品
export async function searchDishesByPage(params: {
  keyword?: string;
  kind?: string;
  season?: string;
  pageNo?: number;
  pageSize?: number;
}): Promise<PageInfo<Dish> | null> {
  try {
    const queryParams = {
      keyword: params.keyword,
      kind: params.kind,
      season: params.season,
      pageNo: params.pageNo || 1,
      pageSize: params.pageSize || 6
    };
    
    const data: PageInfo<Dish> = await httpInstance.get('/api/dishes/search', { params: queryParams });
    return data;
  } catch (err) {
    console.error("searchDishesByPage error:", err);
    throw err;
  }
}

// 根据章节分页查询菜品
//根据菜品 ID 查询单个菜品
export async function getDishById(dishId: number): Promise<Dish | null> {
  try {
    const data: Dish = await httpInstance.get(`/api/dishes/${dishId}`);
    return data;
  } catch (err) {
    console.error("getDishById error:", err);
    throw err;
  }
}

//多标签菜品查询
// 导入菜品（文件上传接口）
export async function importDishes(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const data: string = await httpInstance.post('/api/dishes/imports', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('导入菜品成功:', data);
    return data;
  } catch (error) {
    console.error('importDishes error:', error);
    return null;
  }
}

// 删除菜品
export async function deleteDishById(dishId: number): Promise<string | null> {
  try {
    const data: string = await httpInstance.delete(`/api/dishes/${dishId}`);
    console.log('删除菜品成功:', data);
    return data;
  } catch (error) {
    console.error('deleteDishById error:', error);
    return null;
  }
}

// 更新菜品
export async function updateDishById(dishId: number, dishData: Partial<Dish>): Promise<string | null> {
  try {
    const data: string = await httpInstance.put(`/api/dishes/${dishId}`, dishData);
    console.log('更新菜品成功:', data);
    return data;
  } catch (error) {
    console.error('updateDishById error:', error);
    return null;
  }
}

// 导入菜品-章节关联（文件上传接口）
export async function importDishBookRel(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const data: string = await httpInstance.post('/api/dish-book-relations/imports', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('导入菜品-章节关联成功:', data);
    return data;
  } catch (error) {
    console.error('importDishBookRel error:', error);
    return null;
  }
}

// 导入菜品-食材关联（文件上传接口）
export async function importDishIngredientRel(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const data: string = await httpInstance.post('/api/dish-ingredient-relations/imports', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('导入菜品-食材关联成功:', data);
    return data;
  } catch (error) {
    console.error('importDishIngredientRel error:', error);
    return null;
  }
}

// 删除菜品-食材关联（根据关联ID）
export async function deleteDishIngredientRelById(relId: number): Promise<number | null> {
  try {
    const data: number = await httpInstance.delete(`/api/dish-ingredient-relations/${relId}`);
    console.log('删除菜品-食材关联成功:', data);
    return data;
  } catch (error) {
    console.error('deleteDishIngredientRelById error:', error);
    return null;
  }
}

// 根据章节 ID 获取该章节下所有菜品
export async function getDishesByChapterAll(chapterId: number): Promise<Dish[]> {
  try {
    // 对应后端 @GetMapping("dishes/chapterid/{chapter_id}")
    const data: Dish[] = await httpInstance.get(`/api/dishes/chapters/${chapterId}`);
    return data;
  } catch (err) {
    console.error("getDishesByChapterAll error:", err);
    throw err;
  }
}

// 菜品匹配度
export async function getRecommendedDishes(token: string): Promise<{ code: number; message: string; data: Array<Record<string, any>> } | null> {
  try {
    // 设置请求头，携带 Authorization Token
    const config = {
      headers: {
        Authorization: `Bearer ${token}`
      }
    };

    // 请求路径不需要用户 ID
    const response = await httpInstance.get(`/api/recommendations/dishes`, config);
    return response; // 返回完整响应对象
  } catch (err) {
    console.error("getRecommendedDishes error:", err);
    throw err;
  }
}
