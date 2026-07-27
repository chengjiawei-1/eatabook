import httpInstance from "@/utils/http";

export interface SubmitTestParams {
  testId?: number;
  userId: number;
  testTime: string;                  // 测试时间
  constitutionScores: string;        // JSON string
  constitutionConvertScores: string; // JSON string
  mainConstitution: string;
  subConstitution: string;
  suggest: string;
  plan?: string;        // 方案总则
  region?: string;      // 地域建议
  ageAvoid?: string;    // 年龄忌口
  ageFocus?: string;    // 年龄重点
  foodStaple?: string;  // 主食
  foodVeg?: string;     // 蔬果
  foodMeat?: string;    // 肉类
  daily?: string;       // 注意事项
  needFoodRecommendation: boolean;
}

// 提交测试
export const submitTestAPI = (data: SubmitTestParams) => {
  return httpInstance.post('/api/constitution-tests', data);
};

export const generateConstitutionAdviceAPI = (data: {
  userId: number;
  primary: string;
  secondary?: string;
  age?: number;
  location?: string;
  season?: string;
}) => {
  return httpInstance.post('/api/constitution-tests/advice', data);
};

// 获取历史
export const getHistoryAPI = (userId: number) => {
  return httpInstance.get(`/api/constitution-tests/users/${userId}`);
};

/**
 * 查询用户测试分数及主体质（用于图表或简报展示）
 * 对应后端新增接口：/api/constitution/argent/{userId}
 */
export const selectByUserIdForArgentAPI = (userId: number) => {
  return httpInstance.get(`/api/constitution-tests/users/${userId}/scores`);
};
/**
 * 对应后端新增接口：分页查询测试结果
 * @param userId 用户ID
 * @param pageNo 当前页码 (默认 1)
 * @param pageSize 每页条数 (默认 6)
 */
/**
 * 对应后端接口：查询总体质测试次数
 */
 export const totalCountAPI = () => {
  return httpInstance.get('/api/constitution-tests/count');
};

/**
 * 对应后端接口：查询今日测试次数
 */
 export const todayCountAPI = () => {
  return httpInstance.get('/api/constitution-tests/today/count');
};
/**
 * 对应后端接口：查询昨日测试次数
 */
 export const yesterdayCountAPI = () => {
  return httpInstance.get('/api/constitution-tests/yesterday/count');
};
