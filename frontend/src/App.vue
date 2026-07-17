<template>
  <RouterView />
  <!-- 如果 Toaster 是你引入的库，请确保它已经正确导入 -->
  <Toaster position="top-center" rich-colors />
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
// import { Toaster } from '...'; // 确保你的 Toaster 组件已在此处或全局注册
</script>

<style>
/* 引入字体 (通过 Google Fonts 引入，确保网络环境可访问) */
@import url('https://fonts.googleapis.com/css2?family=Ma+Shan+Zheng&family=Noto+Sans+SC:wght@400;700&family=Noto+Serif+SC:wght@600;900&display=swap');

/* 定义全局字体变量和基础样式 */
:root {
  --font-serif: 'Noto Serif SC', serif;
  --font-sans: 'Noto Sans SC', sans-serif;
  --font-cursive: 'Ma Shan Zheng', cursive;
}

body {
  margin: 0;
  /* 设置全站默认字体为无衬线体，保证清晰易读 */
  font-family: var(--font-sans);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 字体功能类 (方便在各个组件中使用) */

/* 衬线体：用于大标题、古籍感标题 */
.font-serif-title {
  font-family: var(--font-serif);
  letter-spacing: 0.02em;
}

/* 无衬线体：用于正文、按钮、说明 */
.font-sans-body {
  font-family: var(--font-sans);
}

/* 装饰体：用于局部点缀（如 Logo、印章感文字） */
.font-cursive {
  font-family: var(--font-cursive);
}

/* 自动统一所有标题样式 (可选建议) */
h1, h2, h3, h4, .title {
  font-family: var(--font-serif);
  font-weight: 700;
}
</style>