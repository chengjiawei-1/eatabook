import httpInstance from "@/utils/http"

//卡片
export function getDashboardCards() {
  return httpInstance({
    url: "/api/databoard/cards",
    method: "get",
  })
}


//环形图-所有用户体质分布
export function getAllConstitution() {
  return httpInstance({
    url: "/api/databoard/constitution/all",
    method: "get",
  })
}

//环形图-指定地区用户体质分布
// @param province 省份名称
export function getConstitutionByProvince(province?: string) {
  return httpInstance({
    url: "/api/databoard/constitution/province",
    method: "get",
    params: {
      province,
    },
  })
}

//排行榜-菜品热度排行
export function getTop8DishRank() {
  return httpInstance({
    url: "/api/databoard/dish/rank",
    method: "get",
  })
}

//雷达图-体质-菜品
// @param season 季节，四个字段分别为：spring、summer、autumn、winter
export function getRadarDishes(season = null) {
  return httpInstance({
    url: "/api/databoard/radar/dishes",
    method: "get",
    params: {
      season: season
    },
  })
}

//地图-全国数据
export function getAllMapData() {
  return httpInstance({
    url: "/api/databoard/map/all",
    method: "get",
  })
}

//地图-指定地区数据
// @param region 地区名称
// 词云图-获取热搜前20关键词
export function getTopKeywords() {
  return httpInstance({
    url: "/api/databoard/keywords",
    method: "get",
  })
}


export const tabs = [
  { key: "constitution", label: "用户体质分布", icon: "fa-chart-pie" },
  { key: "dish", label: "菜品热度排行", icon: "fa-chart-bar" },
  { key: "relation", label: "体质-食疗关联", icon: "fa-diagram-project" },
  { key: "keywords", label: "AI 关键词", icon: "fa-cloud" },
];

export const constitutionColorMap: Record<string, string> = {
  平和质: "#8b4513",
  气虚质: "#c19a6b",
  阴虚质: "#a52a2a",
  阳虚质: "#cd5c5c",
  痰湿质: "#d2b48c",
  湿热质: "#bc8f8f",
  血瘀质: "#deb887",
  气郁质: "#f5deb3",
  特禀质: "#d3d3d3",
};

export const regions = [
  "北京市",
  "天津市",
  "上海市",
  "重庆市",
  "河北省",
  "山西省",
  "辽宁省",
  "吉林省",
  "黑龙江省",
  "江苏省",
  "浙江省",
  "安徽省",
  "福建省",
  "江西省",
  "山东省",
  "河南省",
  "湖北省",
  "湖南省",
  "广东省",
  "四川省",
  "贵州省",
  "云南省",
  "陕西省",
  "甘肃省",
  "青海省",
  "台湾省",
  "内蒙古自治区",
  "广西壮族自治区",
  "西藏自治区",
  "宁夏回族自治区",
  "新疆维吾尔自治区",
  "香港特别行政区",
  "澳门特别行政区",
];
