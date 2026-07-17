// src/api/bookChapters.ts

// 导入封装好的 http 实例
import httpInstance from "@/utils/http";

// 章节实体定义
export interface BookChapters {
  chapterId: number;
  bookId: number;
  chapterTitle: string;
  chapterSeq: number;
  ingredientCount: number;
  dishCount: number;
  updateTime: string;
}

// 修改根据书籍 ID 获取章节
export async function getBookChaptersByBookId(
  bookId: string
): Promise<BookChapters[]> {
  try {
    const response = await httpInstance.get(`/api/ancientbooks/${bookId}/book-chapters`);
    return response.data || response;
  } catch (err) {
    console.error("getBookChaptersByBookId error:", err);
    throw new Error("获取书籍章节失败");
  }
}
