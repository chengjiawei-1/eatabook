import httpInstance from "@/utils/http";

// 定义膳食计划的数据结构
export interface UserMealPlan {
  userId: number;
  planJson: string;           // 存储七天计划的 JSON 字符串
  generateDate: string;       // 方案生成的起始日期 (YYYY-MM-DD)
  baseConstitution: string;   // 生成方案时参考的体质名称
  updateTime?: string;
}

/**
 * 获取用户现有的有效食疗计划
 * @param userId 用户ID
 * @param currentConstitution 当前最新的体质名称
 * @returns 返回包含 generateDate 和 planJson 的对象，或 null
 */
export async function getValidMealPlan(userId: number, currentConstitution: string): Promise<any | null> {
  try {
    const res = await httpInstance.get('/api/meal-plans/current', {
      params: {
        userId,
        currentConstitution 
      }
    });

    // 如果后端返回的是 Result 包装类，这里可能需要 return res.data;
    // 如果后端直接返回 Map，则直接 return res;
    return res || null;
  } catch (error) {
    console.error('getValidMealPlan error:', error);
    return null;
  }
}

/**
 * 保存或更新从 AI 生成的新计划
 * @param userId 用户ID
 * @param planJson 完整的 AI 生成 JSON 字符串
 * @param constitution 生成该计划时对应的体质
 */
export async function saveMealPlan(userId: number, planJson: string, constitution: string): Promise<boolean> {
  try {
    const response: string = await httpInstance.post('/api/meal-plans', {
      userId,
      planJson,
      constitution
    });
    console.log('保存计划成功:', response);
    return response === 'success';
  } catch (error) {
    console.error('saveMealPlan error:', error);
    return false;
  }
}

export async function generateMealPlan(params: {
  userId: number;
  prompt?: string;
  save?: boolean;
  topK?: number;
}): Promise<any> {
  return httpInstance.post('/api/meal-plans/generate', params);
}
