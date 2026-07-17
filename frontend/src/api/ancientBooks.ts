// src/api/ancientBooks.ts
import httpInstance from "@/utils/http";

// 定义与后端返回数据对应的 TypeScript 接口
export interface AncientBook {
  bookId: string;
  bookName: string;
  bookCoverUrl: string;
  intro: string;
  nourishCount: number;
  bookCategory: string;
  dynasty: string;
  author: string;
  updateTime: string;
  status: number;  
  viewCount: number; 
}

// 获取全部古籍
export async function getAllBooks(): Promise<AncientBook[]> {
  try {
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: AncientBook[] = await httpInstance.get('/api/ancientbooks');
    return data;
  } catch (error) {
    console.error('getAllBooks error:', error);
    throw error;
  }
}

// 根据分类从后端获取古籍
export async function getBooksByCategory(category: string): Promise<AncientBook[]> {
  try {
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: AncientBook[] = await httpInstance.get(`/api/ancientbooks/categories/${category}`);
    return data;
  } catch (error) {
    console.error('getBooksByCategory error:', error);
    throw error;
  }
}

// 根据ID获取古籍
export async function getBookById(id: string): Promise<AncientBook | null> {
  try {
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: AncientBook = await httpInstance.get(`/api/ancientbooks/${id}`);
    return data;
  } catch (error) {
    console.error('getBookById error:', error);
    return null;
  }
}

// 更新古籍访问量
export async function updateBookViewCount(id: string): Promise<boolean> {
  try {
    console.log('调用访问量更新接口，路径:', `/api/ancientbooks/${id}/views`);
    const response = await httpInstance.post(`/api/ancientbooks/${id}/views`);
    console.log('访问量更新响应:', response);
    return response === true || response === 'true' || response?.success === true;
  } catch (error) {
    console.error('updateBookViewCount error:', error);
    // 添加更详细的错误信息
    if (error instanceof Error) {
      console.error('错误详情:', error.message);
    }
    return false;
  }
}

// 导入古籍
export async function importBook(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: string = await httpInstance.post('/api/ancientbooks/imports', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('导入古籍成功:', data);
    return data;
  } catch (error) {
    console.error('importBook error:', error);
    return null;
  }
}

// 发布古籍
export async function publishBook(id: string): Promise<boolean> {
  try {
    console.log(`尝试发布古籍，ID: ${id}`);
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: boolean = await httpInstance.put(`/api/ancientbooks/${id}/publication`);
    console.log(`发布古籍成功，服务器响应: ${data}`);
    return data;
  } catch (error) {
    console.error('publishBook error:', error);
    return false;
  }
}

// 导入章节
export async function importBookChapters(file: File): Promise<string | null> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: string = await httpInstance.post('/api/book-chapters/imports', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    console.log('导入章节成功:', data);
    return data;
  } catch (error) {
    console.error('importBookChapters error:', error);
    return null;
  }
}

// 删除古籍
export async function deleteBookById(id: string): Promise<boolean> {
  try {
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: boolean = await httpInstance.delete(`/api/ancientbooks/${id}`);
    console.log('删除古籍成功:', data);
    return data;
  } catch (error) {
    console.error('deleteBookById error:', error);
    return false;
  }
}

// 更新古籍信息
export async function updateBookById(id: string, bookData: Partial<AncientBook>): Promise<boolean> {
  try {
    // httpInstance 已经配置了响应拦截器，直接返回数据部分
    const data: boolean = await httpInstance.put(`/api/ancientbooks/${id}`, bookData);
    console.log('更新古籍成功:', data);
    return data;
  } catch (error) {
    console.error('updateBookById error:', error);
    return false;
  }
}

