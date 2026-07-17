import httpInstance from "@/utils/http";

/**
 * 定义生图请求的数据结构
 */
export interface CulinaryRequest {
  stepsText: string; // Dify 生成的“造法”全文
}

/**
 * 定义生图响应的数据结构
 */
export interface CulinaryResponse {
  success: boolean;
  imageUrls: string[];
  message: string;
}

/**
 * 调用后端接口：根据步骤文本生成图片列表
 * @param stepsText 包含 1. 2. 3. 步骤的字符串
 */
export async function generateCulinaryImages(data: {
  dishName: string;
  stepsText: string[];
}): Promise<CulinaryResponse> {
  try {
    const res = await httpInstance.post<CulinaryResponse>(
      '/api/culinary/step-images',
      {
        dishName: data.dishName,
        stepsText: data.stepsText
      },
      {
        timeout: 120000 // 2分钟超时
      }
    );

    const result = (res as any).data !== undefined ? (res as any).data : res;

    return result;
  } catch (error: any) {
    if (error.code === 'ECONNABORTED') {
      console.error('生图请求超时');
      return {
        success: false,
        imageUrls: [],
        message: '画卷描绘时间较长，服务器响应超时，请重试'
      };
    }

    console.error('generateCulinaryImages error:', error);
    return {
      success: false,
      imageUrls: [],
      message: '网络请求失败，请检查后端服务是否启动'
    };
  }
}
