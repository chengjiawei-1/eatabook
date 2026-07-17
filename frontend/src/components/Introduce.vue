<template>
  <!-- 背景底色统一为平台色 #fcfaf2 -->
  <section class="introduce-wrapper bg-[#fcfaf2] dark:bg-slate-950 transition-colors duration-500" ref="sectionRef">
    <!-- 装饰性纹理底层 -->
    <div class="absolute inset-0 opacity-[0.03] pointer-events-none texture-bg"></div>

    <header class="intro-header relative z-10">
      <div class="chapter-tag">CORE FEATURES</div>
      <h1 class="title font-serif">平台主要特点</h1>
      <p class="subtitle">
        基于古籍智慧的个性化膳食建议、体质分析与食谱推荐，帮助用户实现健康与养生的全面规划。
      </p>
      <div class="title-underline"></div>
    </header>

    <div class="features relative z-10">
      <!-- 左侧功能 -->
      <div class="column">
        <FeatureCard
          icon="fas fa-heartbeat"
          title="体质测试"
          desc="基于《中医体质分类与判定》的体质测试，支持周期性评估你的体质状态，持续了解身体变化，为个性化养生提供依据。"
          to="/physique-test"
        />
        <FeatureCard
          icon="fas fa-book-open"
          title="古籍推荐"
          desc="结合传统养生智慧，推荐与你体质和需求相符的古籍内容，深度解析养生理论。"
          to="/books"
        />
        <FeatureCard
          icon="fas fa-utensils"
          title="个性化膳食建议"
          desc="基于体质、地域和需求，提供定制化的膳食建议，帮助用户调整饮食结构。"
          to="/recipes"
        />
      </div>

      <!-- 中间分隔线：美化为琥珀珠链 -->
      <div class="divider">
        <div class="line"></div>
        <span class="dot" v-for="i in 5" :key="i"></span>
      </div>

      <!-- 右侧功能 -->
      <div class="column">
        <FeatureCard
          icon="fas fa-play-circle"
          title="菜品教程"
          desc="精选养生菜品，通过打卡积分解锁配套详细视频讲解，从食材处理到烹饪步骤，全程清晰呈现。"
          to="/recipes"
        />
        <FeatureCard
          icon="fas fa-calendar-check"
          title="食养打卡"
          desc="通过每日食养打卡，记录你的养生习惯，轻松获取积分，为解锁更多养生菜品视频教程打下基础。"
          to="/diet-checkin"
        />
        <FeatureCard
          icon="fas fa-comments"
          title="社区交流"
          desc="通过论坛和社区功能，促进用户之间的经验分享和交流，建立健康互助的社区。"
          to="/forum"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted, defineComponent } from 'vue'

const sectionRef = ref(null)

onMounted(() => {
  const observer = new IntersectionObserver(
    entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view')
        }
      })
    },
    { threshold: 0.2 }
  )

  if (sectionRef.value) {
    observer.observe(sectionRef.value)
  }
})

const FeatureCard = defineComponent({
  props: {
    icon: String,
    title: String,
    desc: String,
    to: {
      type: String,
      default: ''
    }
  },
  template: `
    <div class="feature-card reveal group" :class="{ 'cursor-pointer': to }" @click="handleClick">
      <div class="icon-wrapper">
        <div class="icon-bg"></div>
        <i :class="[icon, 'icon-main']"></i>
      </div>
      <h3 class="font-serif">{{ title }}</h3>
      <p>{{ desc }}</p>
    </div>
  `,
  methods: {
    handleClick() {
      if (this.to) {
        this.$router.push(this.to)
      }
    }
  }
})
</script>

<style scoped>
/* 引入宋体 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@700&display=swap');

.font-serif {
  font-family: 'Noto Serif SC', serif;
}

.introduce-wrapper {
  position: relative;
  padding: 100px 20px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.texture-bg {
  background-image: url('https://www.transparenttextures.com/patterns/paper-fibers.png');
}

.intro-header {
  text-align: center;
  margin-bottom: 80px;
  max-width: 800px;
}

/* 确保 Introduce 里的颜色与全局琥珀色统一 */
.chapter-tag {
  color: #d97706; /* Amber-600 */
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 800;
}

.title {
  font-size: 42px;
  color: #0f172a; /* Slate-900 */
  margin-bottom: 20px;
}

.title-underline {
  width: 60px;
  height: 3px;
  background: #d97706;
  margin: 0 auto;
}

.subtitle {
  font-size: 16px;
  color: #475569; /* Slate-600 */
  line-height: 1.8;
  margin-bottom: 24px;
}

.features {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 60px;
  max-width: 1200px;
  width: 100%;
}

.column {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

/* 中间分隔线美化 */
.divider {
  position: relative;
  width: 20px;
  height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
}

.divider .line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  background: linear-gradient(to bottom, transparent, #d97706 20%, #d97706 80%, transparent);
  opacity: 0.3;
}

.divider .dot {
  width: 8px;
  height: 8px;
  background: #d97706;
  border-radius: 50%;
  z-index: 2;
  box-shadow: 0 0 10px rgba(217, 119, 6, 0.4);
  animation: pulse 3s infinite ease-in-out;
}

/* 卡片美化 */
.feature-card {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.22, 1, 0.36, 1);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 35px 30px;
  border: 1px solid rgba(217, 119, 6, 0.1);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.05);
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-10px);
  background: #ffffff;
  border-color: #d97706;
  box-shadow: 0 20px 40px -15px rgba(217, 119, 6, 0.15);
}

.icon-wrapper {
  position: relative;
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-bg {
  position: absolute;
  inset: 0;
  background: #fef3c7; /* Amber-100 */
  border-radius: 18px;
  transform: rotate(45deg);
  transition: transform 0.4s ease;
}

.feature-card:hover .icon-bg {
  transform: rotate(135deg);
  background: #d97706;
}

.icon-main {
  position: relative;
  font-size: 24px;
  color: #d97706;
  z-index: 1;
  transition: color 0.4s ease;
}

.feature-card:hover .icon-main {
  color: #ffffff;
}

.feature-card h3 {
  font-size: 22px;
  color: #0f172a;
  margin-bottom: 12px;
  text-align: center;
}

.feature-card p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.7;
  text-align: center;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.3); opacity: 1; }
}

.in-view .feature-card {
  opacity: 1;
  transform: translateY(0);
}

/* 延迟动画 */
.in-view .column:first-child .feature-card:nth-child(1) { transition-delay: 0.1s; }
.in-view .column:first-child .feature-card:nth-child(2) { transition-delay: 0.2s; }
.in-view .column:first-child .feature-card:nth-child(3) { transition-delay: 0.3s; }
.in-view .column:last-child .feature-card:nth-child(1) { transition-delay: 0.4s; }
.in-view .column:last-child .feature-card:nth-child(2) { transition-delay: 0.5s; }
.in-view .column:last-child .feature-card:nth-child(3) { transition-delay: 0.6s; }

@media (max-width: 900px) {
  .features {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  .divider { display: none; }
  .title { font-size: 32px; }
}
</style>