import httpInstance from "@/utils/http";

export interface CulinaryRequest {
  dishName: string;
  sourceText?: string;
  stepsText?: string[];
  maxImages?: number;
}

export interface CulinaryResponse {
  success: boolean;
  imageUrls: string[];
  message: string;
}

export async function generateCulinaryImages(data: CulinaryRequest): Promise<CulinaryResponse> {
  try {
    const res = await httpInstance.post<CulinaryResponse>(
      "/api/culinary/step-images",
      data,
      {
        timeout: 180000
      }
    );

    return (res as any).data !== undefined ? (res as any).data : res;
  } catch (error: any) {
    if (error.code === "ECONNABORTED") {
      console.error("Culinary image request timed out");
      return {
        success: false,
        imageUrls: [],
        message: "画卷描绘时间较长，服务器响应超时，请重试"
      };
    }

    console.error("generateCulinaryImages error:", error);
    return {
      success: false,
      imageUrls: [],
      message: "网络请求失败，请检查后端服务是否启动"
    };
  }
}
