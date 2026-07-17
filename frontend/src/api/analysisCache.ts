import httpInstance from "@/utils/http";

// 定义体质深度分析缓存的数据结构
export interface PhysiqueAnalysisCache {
  userId: number;
  analysisContent: string;   // AI生成的分析文本
  lastTestTime: string;      // 对应的最后一次测试时间 (YYYY-MM-DD HH:mm:ss)
  testCount: number;         // 分析参考的记录条数
  updateTime?: string;
}

// analysisCache.ts

/**
 * 获取体质深度分析结果
 * @param latestTestTime 
 */
export async function getAnalysisCache(userId: number, latestTestTime: number): Promise<string | null> {
  try {
    const res = await httpInstance.get<string>('/api/constitution-analysis', {
      params: { userId, latestTestTime }
    });

    const data = (res as any).data !== undefined ? (res as any).data : res;

    if (typeof data === 'string' && data.trim().length > 10) {
      return data;
    }
    return null;
  } catch (error) {
    console.error('getAnalysisCache error:', error);
    return null;
  }
}

/**
 * 保存或手动更新分析结果到数据库
 * @param userId 用户ID
 * @param analysisContent AI生成的完整深度分析文本
 * @param latestTestTimeMillis 对应测试时间的时间戳 (毫秒)
 */
export async function saveAnalysisCache(
  userId: number, 
  analysisContent: string, 
  latestTestTimeMillis: number
): Promise<boolean> {
  try {
    const res = await httpInstance.post('/api/constitution-analysis', {
      userId,
      analysisContent,
      latestTestTime: latestTestTimeMillis
    });
    
    const result = (res as any).data !== undefined ? (res as any).data : res;
    return result === 'success';
  } catch (error) {
    console.error('saveAnalysisCache error:', error);
    return false;
  }
}
