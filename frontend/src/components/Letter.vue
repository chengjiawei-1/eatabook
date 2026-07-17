<template>
  <div class="letter-container">
    <!-- 标题 -->
    <transition name="fade-slow">
      <h1 v-show="showTitle" class="letter-title">致用户的一封信</h1>
    </transition>

    <!-- 3D 交互信封 -->
    <div 
      class="envelope-wrapper" 
      :class="{ 'is-open': opening, 'is-flying': flying }"
      @click="openLetter"
    >
      <!-- 1. 信封底壳 -->
      <div class="envelope-back"></div>

      <!-- 2. 信纸 (初始完全隐藏在里面) -->
      <div class="letter-paper" :class="{ 'slide-up': showLetter }">
        <div class="paper-content">
          <p class="greeting">
            <span
              v-for="(char, i) in greetingChars"
              :key="i"
              class="char"
              :style="{ animationDelay: `${i * 0.1}s` }"
            >
              {{ char }}
            </span>
          </p>
          <div class="letter-text" v-if="showLetter">
            <p>非常感谢你选择了古籍食养。</p>
            <p>我们致力于将传统中医养生智慧与现代AI技术结合，为您提供最贴心的食疗建议。</p>
            <p>愿您在快节奏的生活里，亦能拥有一份属于自己的宁静与健康。记得坚持打卡，关爱身体。</p>
            <div class="signature">—— 古籍食养</div>
          </div>
          <button v-show="showClose" class="close-btn" @click.stop="closeLetter">收起锦书</button>
        </div>
      </div>

      <!-- 3. 信封前脸 (关键：遮挡层) -->
      <div class="envelope-front"></div>

      <!-- 4. 信封翻盖 -->
      <div class="envelope-flap">
        <div class="wax-seal">
          <span class="seal-text">养</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const showTitle = ref(false)
const opening = ref(false)
const showLetter = ref(false)
const showClose = ref(false)
const flying = ref(false)

const greetingChars = '亲爱的用户：'.split('')

onMounted(() => {
  setTimeout(() => {
    showTitle.value = true
  }, 500)
})

const openLetter = () => {
  if (opening.value) return
  opening.value = true
  
  // 先开盖子
  // 盖子开一半后，信纸升起
  setTimeout(() => {
    showLetter.value = true
  }, 600)

  // 显示关闭按钮
  setTimeout(() => {
    showClose.value = true
  }, 1500)
}

const closeLetter = () => {
  flying.value = true
  showClose.value = false
  
  // 纸飞机飞走动画
  setTimeout(() => {
    // 重置所有状态
    opening.value = false
    showLetter.value = false
    flying.value = false
  }, 1200)
}
</script>

<style scoped>
/* 容器 */
.letter-container {
  min-height: 85vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  perspective: 1500px;
  background-color: transparent;
}

/* 标题样式：古风书法感 */
.letter-title {
  font-size: 38px;
  font-family: "Ma Shan Zheng", "STKaiti", serif;
  color: #8d4b31;
  margin-bottom: 80px;
  letter-spacing: 8px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
}

/* 信封外壳容器 */
.envelope-wrapper {
  position: relative;
  width: 320px;
  height: 200px;
  background-color: #e6d5b8;
  cursor: pointer;
  transform-style: preserve-3d;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.envelope-wrapper:hover:not(.is-open) {
  transform: translateY(-10px) rotateX(10deg);
}

/* 1. 信封底壳 */
.envelope-back {
  position: absolute;
  inset: 0;
  background-color: #d4c393;
  z-index: 1;
}

/* 2. 信封前脸 (遮挡层) */
.envelope-front {
  position: absolute;
  inset: 0;
  z-index: 3; /* 高于信纸 */
  background-color: #e6d5b8;
  /* 使用剪裁路径制造信封开口效果 */
  clip-path: polygon(0 0, 0% 100%, 100% 100%, 100% 0, 50% 45%);
  border: 1px solid #d4c393;
}

/* 3. 信封翻盖 */
.envelope-flap {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 4; /* 初始在最顶层 */
  background-color: #d4c393;
  clip-path: polygon(0 0, 100% 0, 50% 50%);
  transform-origin: top;
  transition: transform 0.6s ease-in-out, z-index 0.1s 0.3s;
}

.is-open .envelope-flap {
  transform: rotateX(180deg);
  z-index: 1; /* 翻开后层级降到底部 */
}

/* 火漆印 */
.wax-seal {
  position: absolute;
  top: 30%;
  left: 50%;
  transform: translateX(-50%);
  width: 45px;
  height: 45px;
  background: radial-gradient(#a52a2a, #800000);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  border: 2px solid #912b2b;
}

.seal-text {
  color: #ffd700;
  font-family: "Ma Shan Zheng", serif;
  font-size: 20px;
}

/* 4. 信纸优化 */
.letter-paper {
  position: absolute;
  top: 5px;
  left: 10px;
  width: 300px;
  height: 190px;
  background: #fdfaf2; /* 宣纸色 */
  z-index: 2; /* 夹在底壳和前脸中间 */
  transition: all 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  padding: 25px;
  box-sizing: border-box;
  box-shadow: 0 0 15px rgba(0,0,0,0.05);
  border: 1px solid #eee;
}

/* 信纸升起逻辑 */
.letter-paper.slide-up {
  height: 420px;
  transform: translateY(-240px) scale(1.1);
  z-index: 5; /* 升起后高于一切 */
  box-shadow: 0 20px 50px rgba(0,0,0,0.15);
}

/* 飞走动画 */
.is-flying .letter-paper {
  animation: fly-away 1.2s ease-in forwards;
}

@keyframes fly-away {
  0% { transform: translateY(-240px) scale(1.1) rotate(0); opacity: 1; }
  30% { transform: translateY(-260px) scale(1.05) rotate(-5deg); }
  100% { transform: translate(600px, -600px) scale(0.2) rotate(40deg); opacity: 0; }
}

/* 信纸内容排版 */
.paper-content {
  font-family: "STKaiti", "KaiTi", serif;
  color: #333;
  line-height: 1.8;
}

.greeting {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #8d4b31;
}

.letter-text {
  font-size: 16px;
  text-align: justify;
}

.signature {
  text-align: right;
  margin-top: 20px;
  font-weight: bold;
  color: #8d4b31;
}

/* 按钮样式 */
.close-btn {
  display: block;
  margin: 30px auto 0;
  padding: 8px 24px;
  background: #8d4b31;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
  font-family: "STKaiti", serif;
}

.close-btn:hover {
  background: #ff6c32;
}

/* 逐字淡入 */
.char {
  opacity: 0;
  display: inline-block;
  animation: char-in 0.5s ease forwards;
}

@keyframes char-in {
  to { opacity: 1; }
}

/* 渐变过渡 */
.fade-slow-enter-active { transition: opacity 2s ease; }
.fade-slow-enter-from { opacity: 0; }
</style>