import httpInstance from "@/utils/http"

// 对应后端实体类
export interface SeasonalWisdom {
  id?: number
  tagName: string
  type: string
  title: string
  content: string
}

// 节气查询接口
export function getSeasonalWisdomAPI(tagName: string) {
  return httpInstance({
    url: `/api/seasonal-wisdom/${tagName}`,
    method: 'get'
  })
}
