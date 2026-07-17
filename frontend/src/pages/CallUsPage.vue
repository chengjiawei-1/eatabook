<template>
  <!-- 背景底色统一为平台色 #fcfaf2 -->
  <div class="call-us-page bg-[#fcfaf2] dark:bg-slate-950 transition-colors duration-500">
    <!-- 顶部导航触发区 -->
    <div class="nav-trigger-area" :class="{ disabled: showNav }" @mouseenter="handleNavEnter" />

    <Nav :class="{ 'nav-hidden': !showNav }" @mouseenter="handleNavEnter" />

    <!-- 分镜叙事容器 -->
    <div class="parallax-container mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">

      <!-- 装饰性背景层 -->
      <div class="absolute inset-0 opacity-[0.03] pointer-events-none" style="background-image: url('https://www.transparenttextures.com/patterns/paper-fibers.png');"></div>

      <!-- 分镜板块: 实训引航 -->
      <section
        class="parallax-section section-1 border-b border-amber-100/50"
        id="section1"
        :ref="el => sectionRefs[0] = el"
      >
        <div class="section-content relative z-10">
          <!-- 视差层 -->
          <div class="parallax-layer bg-layer">
            <div class="floating-item item-1"><i class="fa-solid fa-leaf text-amber-200/40"></i></div>
            <div class="background-circle circle-1"></div>
          </div>

          <div class="intro-container">
            <!-- 文字居中容器 -->
            <div class="intro-text-centered" :class="{ 'text-hidden': showImages }">
              <span class="text-amber-600 font-bold tracking-widest text-sm mb-4 block">CHAPTER 01</span>
              <h2 class="section-title font-serif" :class="{ active: isInView.section1 }">
                {{ comicFrames[0].title }}
              </h2>
              <p class="section-content-text text-xl" :class="{ active: isInView.section1 }">
                "{{ comicFrames[0].content }}"
              </p>
              <div class="w-24 h-1 bg-gradient-to-r from-amber-500 to-transparent mx-auto mt-6"></div>
            </div>
            
            <div class="intro-photos-container">
              <div
                v-for="(photo, photoIndex) in introPhotos"
                :key="photoIndex"
                class="intro-card"
                :class="{ 'active': showImages && imageSequence >= photoIndex }"
              >
                <img :src="photo.img" class="intro-card-image" />
                <div class="absolute inset-0 border-2 border-amber-500/20 rounded-xl pointer-events-none"></div>
              </div>
            </div>

            <!-- 实训大图 -->
            <div class="extra-photo-wrapper">
              <div class="extra-photo-inner">
                <img src="@/assets/images/bg.png" alt="实训补充照片" class="extra-photo" />
                <div class="extra-photo-caption">
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 分镜板块 : 协作同行 -->
      <section
        class="parallax-section section-2"
        id="section2"
        :ref="el => sectionRefs[1] = el"
      >
        <div class="section-content relative z-10">
          <div class="parallax-layer bg-layer">
            <div class="background-circle circle-2"></div>
          </div>

          <!-- 文字居中容器 -->
          <div class="dream-text-container">
            <span class="text-orange-500 font-bold tracking-widest text-sm mb-4 block">CHAPTER 02</span>
            <h2 class="section-title font-serif" :class="{ active: isInView.section2 }">
              {{ comicFrames[1].title }}
            </h2>
            <p class="section-content-text" :class="{ active: isInView.section2 }">
              "{{ comicFrames[1].content }}"
            </p>
          </div>

          <!-- 照片墙 -->
          <div class="photos-container-custom">
            <div class="photo-row row-1">
              <div v-for="idx in [0,1,2,3]" :key="`row1-${idx}`" class="card" :class="`card-${idx} ${isInView.section2 ? 'active' : ''}`" :style="{ animationDelay: `${idx * 0.1}s` }">
                <div class="card-face first-content"><img :src="dreamPhotos[idx].frontImg" class="card-image" /></div>
                <div class="card-face second-content"><img :src="dreamPhotos[idx].backImg" class="card-image" /></div>
              </div>
            </div>
            <div class="photo-row row-2">
              <div v-for="(idx, localIdx) in [5,8,9]" :key="`row2-${localIdx}`" class="card" :class="`card-${idx} ${isInView.section2 ? 'active' : ''}`" :style="{ animationDelay: `${(4 + localIdx) * 0.1}s` }">
                <div class="card-face first-content"><img :src="dreamPhotos[idx].frontImg" class="card-image" /></div>
                <div class="card-face second-content"><img :src="dreamPhotos[idx].backImg" class="card-image" /></div>
              </div>
            </div>
            <div class="photo-row row-3">
              <div v-for="(idx, localIdx) in [4,6,7]" :key="`row3-${localIdx}`" class="card card-horizontal" :class="`card-${idx} ${isInView.section2 ? 'active' : ''}`" :style="{ animationDelay: `${(7 + localIdx) * 0.1}s` }">
                <div class="card-face first-content"><img :src="dreamPhotos[idx].frontImg" class="card-image" /></div>
                <div class="card-face second-content"><img :src="dreamPhotos[idx].backImg" class="card-image" /></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 联系我们板块 -->
      <section class="contact-section relative" id="contactSection" ref="contactSection">
        <div class="contact-content z-10">
          <header class="contact-header text-center mb-16">
            <div class="inline-block px-4 py-1 rounded-full bg-amber-100 text-amber-700 text-xs font-bold mb-4">GET IN TOUCH</div>
            <h1 class="text-4xl md:text-5xl font-serif font-bold text-slate-900 mb-6 section-title" :class="{ active: isInView.contactSection }">
              联系我们
            </h1>
            <p class="text-slate-600 max-w-2xl mx-auto leading-relaxed">
              喜欢我们的食养故事吗？欢迎与我们的团队交流。
            </p>
          </header>

          <div class="team-section" ref="teamSection">
            <!-- 标题居中 -->
            <h2 class="team-title section-title text-center" :class="{ active: isInView.teamSection }">
              我们的团队
            </h2>
            <div class="team-members">
              <div
                v-for="(character, charIndex) in characters"
                :key="character.id"
                class="team-member"
                :class="{ active: isInView.teamSection }"
                :style="{ animationDelay: `${charIndex * 0.15}s` }"
              >
                <!-- 保留点击头像复制邮箱功能 -->
                <img :src="character.avatar" class="character-avatar" @click="showEmailPopup(character)" />
                <div class="character-info"><div class="character-name">{{ character.name }}</div></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <footer class="page-footer bg-white/80 backdrop-blur-md border-t border-amber-100">
        <div class="container mx-auto px-4 py-8 flex flex-col md:flex-row justify-between items-center text-slate-500 text-sm">
          <p>© {{ currentYear }} 寻味食养团队 · 匠心出品</p>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import Nav from '@/components/Nav.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const currentYear = new Date().getFullYear()
const sectionRefs = ref<HTMLElement[]>([])
const contactSection = ref<HTMLElement | null>(null)
const teamSection = ref<HTMLElement | null>(null)
const showImages = ref(false)
const imageSequence = ref(-1)
const showNav = ref(false)

const handleNavEnter = () => { showNav.value = true }

const isInView = reactive<Record<string, boolean>>({
  section1: true,
  section2: false,
  contactSection: false,
  teamSection: false,
})

// 邮箱弹窗功能
const showEmailPopup = (character: { name: string; email: string }) => {
  ElMessageBox({
    title: '复制QQ邮箱',
    message: `<div style="text-align:center;padding:10px 0;">
      <p style="margin-bottom:10px;color:#666;font-size:16px;">${character.name} 的QQ邮箱号码</p>
      <div style="background:#f5f7fa;padding:15px;border-radius:8px;font-family:monospace;font-size:1.3rem;color:#FF6C32;border:2px dashed #FF6C32;font-weight:bold;">
        ${character.email}
      </div>
      <p style="margin-top:10px;color:#999;font-size:14px;">点击"复制邮箱"按钮即可复制QQ邮箱号码</p>
    </div>`,
    dangerouslyUseHTMLString: true,
    showCancelButton: true,
    confirmButtonText: '复制邮箱',
    cancelButtonText: '关闭',
    customClass: 'email-popup-custom',
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        // 复制邮箱号码到剪贴板
        copyToClipboard(character.email)
          .then(() => {
            ElMessage.success(`已成功复制QQ邮箱: ${character.email}`);
            done();
          })
          .catch((error) => {
            console.error('复制失败:', error);
            ElMessage.error('复制失败，请手动复制邮箱号码');
            done();
          });
      } else {
        done();
      }
    }
  });
}

// 专门的复制函数
const copyToClipboard = async (text: string): Promise<void> => {
  // 方法1: 使用现代 Clipboard API
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(text);
      console.log('使用Clipboard API复制成功');
      return;
    } catch (err) {
      console.warn('Clipboard API失败，尝试备用方法:', err);
    }
  }
  
  // 方法2: 使用传统的execCommand方法
  const textArea = document.createElement('textarea');
  textArea.value = text;
  textArea.style.position = 'fixed';
  textArea.style.left = '-999999px';
  textArea.style.top = '-999999px';
  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();
  
  try {
    const successful = document.execCommand('copy');
    document.body.removeChild(textArea);
    if (successful) {
      console.log('使用execCommand复制成功');
      return;
    } else {
      throw new Error('execCommand复制失败');
    }
  } catch (err) {
    document.body.removeChild(textArea);
    throw err;
  }
}



const startIntroAnimation = async () => {
  await nextTick();
  setTimeout(() => { showImages.value = true }, 2000);
  setTimeout(() => {
    let currentIndex = 0;
    const interval = setInterval(() => {
      if (currentIndex < introPhotos.length) {
        imageSequence.value = currentIndex;
        currentIndex++;
      } else { clearInterval(interval); }
    }, 100);
  }, 1000);
}

onMounted(() => {
  const observer = new IntersectionObserver(entries => {
    entries.forEach(e => {
      if (e.target.id && e.target.id in isInView) isInView[e.target.id] = e.isIntersecting;
      if (e.target === teamSection.value) isInView.teamSection = e.isIntersecting;
    });
  }, { threshold: 0.2 });

  sectionRefs.value.forEach(el => el && observer.observe(el));
  if (contactSection.value) observer.observe(contactSection.value);
  if (teamSection.value) observer.observe(teamSection.value);

  startIntroAnimation();
});

// 数据定义
const comicFrames = [
  { title: '实训引航', content: '实训伊始，老师们悉心指导、耐心答疑，在关键节点为我们指明方向。' },
  { title: '协作同行', content: '实训过程中，小组成员相互配合、彼此支持，在磨合中凝聚成果。' },
];

const introPhotos = [
  { img: new URL('@/assets/images/ttt1.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/tt2.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/tt3.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/tt4.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/t5.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/tgd1.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/yy2.jpg', import.meta.url).href },
  { img: new URL('@/assets/images/wwjc2.jpg', import.meta.url).href },
];

const characters = [
  { id: 1, name: '刘畅月', avatar: new URL('@/assets/images/yy2.jpg', import.meta.url).href, email: '2976476297@qq.com' },
  { id: 2, name: '魏家城', avatar: new URL('@/assets/images/wwjc2.jpg', import.meta.url).href, email: '2352943800@qq.com' },
  { id: 3, name: '樊倩', avatar: new URL('@/assets/images/ffq2.jpg', import.meta.url).href, email: '3346146409@qq.com' },
  { id: 4, name: '杨梓佳', avatar: new URL('@/assets/images/zzj2.jpg', import.meta.url).href, email: '3209535865@qq.com' },
];

const dreamPhotos = [
  { frontImg: new URL('@/assets/images/yy2.jpg', import.meta.url).href, backImg: new URL('@/assets/images/yy2.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/wwjc2.jpg', import.meta.url).href, backImg: new URL('@/assets/images/wwjc2.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/ffq2.jpg', import.meta.url).href, backImg: new URL('@/assets/images/ffq2.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/zzj2.jpg', import.meta.url).href, backImg: new URL('@/assets/images/zzj2.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/ttt1.jpg', import.meta.url).href, backImg: new URL('@/assets/images/ttt1.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/tt2.jpg', import.meta.url).href, backImg: new URL('@/assets/images/tt2.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/tt3.jpg', import.meta.url).href, backImg: new URL('@/assets/images/tt3.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/tt4.jpg', import.meta.url).href, backImg: new URL('@/assets/images/tt4.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/tgd1.jpg', import.meta.url).href, backImg: new URL('@/assets/images/tgd1.jpg', import.meta.url).href },
  { frontImg: new URL('@/assets/images/t5.jpg', import.meta.url).href, backImg: new URL('@/assets/images/t5.jpg', import.meta.url).href }
];
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@700&display=swap');

/* ========== 居中布局逻辑 ========== */
.intro-text-centered, 
.dream-text-container {
  display: flex;
  flex-direction: column;
  align-items: center;  /* 水平居中 */
  text-align: center;   /* 文字居中 */
  width: 100%;
  margin-bottom: 2rem;
}

.section-content-text {
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease;
}
.section-content-text.active { opacity: 1; transform: translateY(0); }

.section-title {
  font-size: 3rem;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s ease;
}
.section-title.active { opacity: 1; transform: translateY(0); }

/* ========== 遮挡与间距修正 ========== */
.parallax-section {
  position: relative;
  overflow: visible; /* 允许大图撑开，不被截断 */
}

.section-1 {
  padding-top: 80px; /* 减小留白 */
  padding-bottom: 50px;
  background: #fcfaf2;
}

.section-2 {
  padding-top: 100px; /* 确保不遮挡上方的 extra-photo */
  padding-bottom: 150px; /* 增加底部间距，防止联系我们遮挡卡片 */
}

.extra-photo-wrapper {
  margin: 100px auto 40px;
  width: 100%;
  max-width: 1000px;
  position: relative;
}

.extra-photo {
  width: 100%;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.contact-section {
  padding: 100px 20px;
  background: #fff;
  z-index: 30;
}

/* ========== 照片墙样式保持不变 ========== */
.intro-photos-container { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1rem; margin-top: 2rem; }
.intro-card { aspect-ratio: 1; border-radius: 12px; overflow: hidden; opacity: 0; transform: scale(0.9); transition: 0.5s; }
.intro-card.active { opacity: 1; transform: scale(1); }
.intro-card-image { width: 100%; height: 100%; object-fit: cover; }

.photos-container-custom { display: flex; flex-direction: column; gap: 20px; }
.photo-row { display: flex; justify-content: center; gap: 20px; flex-wrap: wrap; }
.card { width: 200px; height: 280px; perspective: 1000px; opacity: 0; transform: translateY(30px); transition: 0.6s; }
.card.active { opacity: 1; transform: translateY(0); }
.card-horizontal { width: 280px; height: 200px; }
.card-face { position: absolute; width: 100%; height: 100%; backface-visibility: hidden; transition: 0.8s; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
.card-image { width: 100%; height: 100%; object-fit: cover; }
.second-content { transform: rotateY(-180deg); }
.card:hover .first-content { transform: rotateY(180deg); }
.card:hover .second-content { transform: rotateY(0deg); }

/* ========== 团队样式 ========== */
.team-members { display: flex; justify-content: center; gap: 3rem; flex-wrap: wrap; margin-top: 3rem; }
.team-member { text-align: center; opacity: 0; transform: translateY(20px); transition: 0.6s; }
.team-member.active { opacity: 1; transform: translateY(0); }
.character-avatar { width: 120px; height: 120px; border-radius: 50%; border: 4px solid #fff; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.character-avatar:hover { transform: scale(1.1); box-shadow: 0 8px 20px rgba(0,0,0,0.15); }
.character-name { margin-top: 1rem; font-weight: bold; font-size: 1.1rem; }

/* 装饰物 */
.bg-layer { position: absolute; inset: 0; pointer-events: none; }
.floating-item { position: absolute; font-size: 2rem; opacity: 0.3; }

@media (max-width: 768px) {
  .intro-photos-container { grid-template-columns: repeat(2, 1fr); }
  .card { width: 140px; height: 200px; }
  .card-horizontal { width: 200px; height: 140px; }
}

/* ========== 邮箱弹窗样式 ========== */
:deep(.email-popup-custom) {
  width: 400px;
}

:deep(.email-popup-custom .el-message-box__header) {
  border-bottom: 1px solid #e4e7ed;
  padding: 15px 20px;
}

:deep(.email-popup-custom .el-message-box__title) {
  color: #333;
  font-weight: 600;
  font-size: 18px;
}

:deep(.email-popup-custom .el-message-box__content) {
  padding: 20px;
}

:deep(.email-popup-custom .el-message-box__btns) {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
}

:deep(.email-popup-custom .el-message-box__btns .el-button--primary) {
  background-color: #FF6C32;
  border-color: #FF6C32;
  color: white;
  padding: 10px 20px;
  font-size: 14px;
}

:deep(.email-popup-custom .el-message-box__btns .el-button--primary:hover) {
  background-color: #ff8c5a;
  border-color: #ff8c5a;
}

:deep(.email-popup-custom .el-message-box__btns .el-button--default) {
  padding: 10px 20px;
  font-size: 14px;
}


</style>
