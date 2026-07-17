<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-slate-900">
    <Nav />

    <main class="flex-grow">
      <HeroSection />
      <CheckPlan />
      <SeasonalRecommendation />
      <Introduce />
      <Post />
      <Letter />
    </main>

    <Footer />

    <LaojiChat />
  </div>
</template>

<script setup lang="ts">
import Nav from "@/components/Nav.vue";
import HeroSection from "@/components/HeroSection.vue";
import CheckPlan from "@/components/CheckPlan.vue";
import SeasonalRecommendation from "@/components/SeasonalRecommendation.vue";
import Footer from "@/components/Footer.vue";
import Post from "@/components/Post.vue";
import Letter from "@/components/Letter.vue";
import Introduce from "@/components/Introduce.vue";

//引入你的智能体组件
import LaojiChat from "@/components/LaojiChat.vue"; 
</script>
<style>
/* 放在 Home.vue 底部，不加 scoped 影响全局 */
html {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

h1, h2, h3, h4 {
  font-family: 'Noto Serif SC', serif;
}

body {
  font-family: 'Noto Sans SC', sans-serif;
  color: #334155; /* Slate-700 */
}
</style>