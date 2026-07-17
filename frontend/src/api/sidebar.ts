import httpInstance from "@/utils/http";

// 定义接口类型
export interface SideBarData {
  totalPosts: number;
  totalUsers: number;
  totalCategories: number;
}

// 分类统计数据接口
export interface CategoryCount {
  categoryId: number;
  categoryName: string;
  count: number;
}

// 获取侧边栏数据
export const getSidebarData = () => {
  return httpInstance.get<SideBarData>('/api/sidebar');
};

//获取各类别贴子数
export const getCategoryCounts = () => {
  return httpInstance.get<CategoryCount[]>('/api/sidebar/category-counts');
};
