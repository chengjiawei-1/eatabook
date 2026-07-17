<template>
  <div class="scroll-container">
    <!-- 跳过按钮 - 固定在右下角，只有老用户才显示 -->
    <button 
      v-if="showSkipButton"
      class="skip-button-fixed"
      @click="handleSkip"
    >跳过</button>

    <!-- 左边把手 -->
    <div
      class="handle handle-left"
      :class="{ 'handle-hidden': animationComplete }"
      :style="{ transform: `translateX(${leftHandleOffset}px)` }"
    >
      <img src="@/assets/images/gz/zuo.jpg" alt="左把手" />
    </div>

    <!-- 中间展开的画 -->
    <div class="scroll-content" :style="{ width: `${scrollWidth}vw` }">
      <div class="scroll-image-wrapper">
        <img src="@/assets/images/gz/zhongjian.jpg" alt="卷轴内容" />
        
        <!-- 当用户content不为空时，显示"古籍食养" -->
        <div class="welcome-panel" v-if="animationComplete && hasContent">
          <h1 class="main-title">古籍食养</h1>
        </div>
        
        <!-- 当用户content为空时，显示Tab选择区域 -->
        <div class="tab-panel" v-if="animationComplete && !hasContent">
          <!-- Tab 标签 -->
          <div class="tab-headers">
            <div
              v-for="(category, index) in categories"
              :key="index"
              class="tab-header"
              :class="{ active: activeTab === index }"
              @click="activeTab = index"
            >
              {{ category.name }}
            </div>
          </div>
          
          <!-- Tab 内容 -->
          <div class="tab-content">
            <div
              v-for="group in categories[activeTab].groups"
              :key="group.title"
              class="option-group"
            >
              <h3 class="group-title">{{ group.title }}</h3>
              <div class="options-list">
                <label
                  v-for="option in group.items"
                  :key="option.value"
                  class="option-item"
                >
                  <input
                    type="checkbox"
                    :value="option.label"
                    v-model="selectedOptions"
                  />
                  <span class="option-label">{{ option.label }}</span>
                  <span v-if="option.desc" class="option-desc">{{ option.desc }}</span>
                </label>
              </div>
            </div>
          </div>
          
          <!-- 保存按钮 -->
          <div class="save-button-container">
            <button class="save-button" @click="handleSave" :disabled="selectedOptions.length === 0 || isSaving">
              {{ isSaving ? '保存中...' : '保存并继续' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右边把手 -->
    <div
      class="handle handle-right"
      :class="{ 'handle-hidden': animationComplete }"
      :style="{ transform: `translateX(${rightHandleOffset}px)` }"
    >
      <img src="@/assets/images/gz/you.jpg" alt="右把手" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import httpInstance from '@/utils/http';

const router = useRouter();
const authStore = useAuthStore();

const leftHandleOffset = ref(0);
const rightHandleOffset = ref(0);
const scrollWidth = ref(0);
const animationComplete = ref(false);
const activeTab = ref(0);
const selectedOptions = ref<string[]>([]);
const hasContent = ref(false); // 是否已有content数据
const isSaving = ref(false); // 保存状态，防止重复提交
const isClosing = ref(false); // 是否正在关闭，防止重复执行收起动画
const showSkipButton = ref(false); // 控制跳过按钮显示，第一次登录不显示

let animationFrameId: number | null = null; // 展开动画的请求ID，用于取消动画

const ANIMATION_DURATION = 8000; // 动画持续时间（毫秒）
const HIDE_DELAY = 1000; // 把手隐藏延迟时间（毫秒）

// 五个分类的数据
const categories = [
  {
    name: '健康功效',
    groups: [
      {
        title: '核心补益类',
        items: [
          { value: 'yiqijiianpi', label: '益气健脾', desc: '增强元气，健运脾胃' },
          { value: 'ziyinbuxu', label: '滋阴补虚', desc: '滋养阴液，补充虚损' },
          { value: 'buzhongyiqi', label: '补中益气', desc: '补充中焦之气，提升体力' },
          { value: 'yangxueanshen', label: '养血安神', desc: '滋养血液，安定心神' },
          { value: 'bushenyijing', label: '补肾益精', desc: '补益肾脏，滋养精气' }
        ]
      },
      {
        title: '调理身体类',
        items: [
          { value: 'qingrejedu', label: '清热解毒', desc: '清除内热，解除毒素' },
          { value: 'kaiweixiaoshi', label: '开胃消食', desc: '增进食欲，帮助消化' },
          { value: 'wenzhongsanhan', label: '温中散寒', desc: '温暖中焦，驱散寒气' },
          { value: 'qushihuatan', label: '祛湿化痰', desc: '去除湿气，化解痰液' },
          { value: 'huoxuexiaozhong', label: '活血消肿', desc: '促进血液循环，消除肿胀' }
        ]
      },
      {
        title: '专项功能类',
        items: [
          { value: 'mingmuhuyan', label: '明目护眼', desc: '改善视力，保护眼睛' },
          { value: 'runfeizhike', label: '润肺止咳', desc: '滋润肺部，止住咳嗽' },
          { value: 'anshenzhusui', label: '安神助眠', desc: '安定心神，帮助睡眠' },
          { value: 'tiaojingyangxue', label: '调经养血', desc: '调理月经，滋养血液' },
          { value: 'runchangtongbian', label: '润肠通便', desc: '滋润肠道，通畅大便' }
        ]
      }
    ]
  },
  {
    name: '口味偏好',
    groups: [
      {
        title: '主流口味',
        items: [
          { value: 'gantian', label: '甘甜', desc: '味甘性平，口感温和' },
          { value: 'xinxiang', label: '辛香', desc: '辛辣芳香，刺激食欲' },
          { value: 'xianxian', label: '咸鲜', desc: '咸淡适中，鲜味突出' }
        ]
      },
      {
        title: '特色口味',
        items: [
          { value: 'weisuan', label: '微酸', desc: '酸甜可口，生津止渴' },
          { value: 'weiku', label: '微苦', desc: '略带苦味，清热降火' },
          { value: 'kugan', label: '苦甘', desc: '苦中带甘，药性较强' },
          { value: 'ganxin', label: '甘辛', desc: '甜中带辛辣，风味独特' }
        ]
      }
    ]
  },
  {
    name: '菜品种类',
    groups: [
      {
        title: '主餐类',
        items: [
          { value: 'zhucai', label: '主菜类', desc: '肉类、鱼类、禽类主菜' },
          { value: 'zhushimiandian', label: '主食面点类', desc: '米饭、面条、馒头等' },
          { value: 'zhoulei', label: '粥类', desc: '杂粮粥、养生粥、药膳粥' }
        ]
      },
      {
        title: '辅食类',
        items: [
          { value: 'xiaocai', label: '小菜类', desc: '凉拌菜、腌制菜、酱菜等' },
          { value: 'dianxin', label: '点心类', desc: '传统糕点、小吃、甜品等' },
          { value: 'tang', label: '汤类', desc: '清汤、浓汤、药膳汤等' }
        ]
      },
      {
        title: '特色类',
        items: [
          { value: 'chashui', label: '茶水类', desc: '养生茶、花草茶、药茶等' },
          { value: 'genglei', label: '羹类', desc: '浓稠羹汤、糊状菜肴' },
          { value: 'shucai', label: '蔬菜类', desc: '全蔬菜菜品' }
        ]
      }
    ]
  },
  {
    name: '食材属性',
    groups: [
      {
        title: '食材分类',
        items: [
          { value: 'shucai_shicai', label: '蔬菜食材', desc: '植物的根、茎、叶、花等' },
          { value: 'qinchu_shicai', label: '禽畜食材', desc: '家禽、家畜肉类' },
          { value: 'shuichan_shicai', label: '水产食材', desc: '鱼类、虾类、贝类等' },
          { value: 'guwu_shicai', label: '谷物食材', desc: '稻、麦、粟、豆等' },
          { value: 'shuiguo_shicai', label: '水果食材', desc: '各类含水量高的果实' }
        ]
      }
    ]
  },
  {
    name: '烹饪方式',
    groups: [
      {
        title: '烹饪方法',
        items: [
          { value: 'zhengzhi', label: '蒸制', desc: '无油、高温蒸汽加热' },
          { value: 'zhudun', label: '煮炖', desc: '加水慢煮、入味透彻' },
          { value: 'chaozhi', label: '炒制', desc: '少油快炒、口感鲜嫩' },
          { value: 'aozhu', label: '熬煮', desc: '长时间慢熬、质地浓稠' },
          { value: 'liangban', label: '凉拌', desc: '生食拌制、少油' }
        ]
      }
    ]
  }
];

onMounted(async () => {
  // 获取当前用户信息
  await checkUserContent();
  
  // 延迟一点开始动画，让页面加载完成
  setTimeout(() => {
    startAnimation();
  }, 300);
});

// 检查用户是否已有content数据
const checkUserContent = async () => {
  try {
    const userId = authStore.user?.userId;
    if (!userId) {
      console.log('用户未登录');
      return;
    }

    const response = await httpInstance.get(`/api/users/${userId}/profile`);
    
    if (response.code === 200) {
      const user = response.data;
      hasContent.value = !!(user.content && user.content.trim());
      
      // 根据是否有内容决定是否显示跳过按钮
      // 已有内容 = 老用户，显示跳过按钮
      // 无内容 = 第一次登录，不显示跳过按钮
      showSkipButton.value = hasContent.value;
      
      // 如果有content，卷轴展开后立即开始收起动画
      if (hasContent.value) {
        setTimeout(() => {
          console.log('自动收起卷轴');
          closeScrollAndNavigate();
        }, ANIMATION_DURATION + HIDE_DELAY);
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
};

// 保存用户选择
const handleSave = async () => {
  if (selectedOptions.value.length === 0) {
    alert('请至少选择一项');
    return;
  }

  // 防止重复提交
  if (isSaving.value) {
    console.log('正在保存中，请勿重复点击');
    return;
  }

  try {
    isSaving.value = true;
    const userId = authStore.user?.userId;
    if (!userId) {
      alert('请先登录');
      isSaving.value = false;
      return;
    }

    // 将选中的选项用逗号连接
    const content = selectedOptions.value.join(',');
    console.log('准备保存，userId:', userId, 'content:', content);
    
    const response = await httpInstance.put(`/api/users/${userId}/preferences`, {
      content: content
    });

    console.log('保存响应:', response);
    
    if (response.code === 200) {
      // 保存成功后,收起卷轴并跳转到首页
      console.log('保存成功，准备跳转');
      closeScrollAndNavigate();
    } else {
      alert('保存失败：' + response.msg);
    }
  } catch (error) {
    console.error('保存失败:', error);
    alert('保存失败，请重试');
  } finally {
    isSaving.value = false;
  }
};

// 点击跳过按钮
const handleSkip = () => {
  console.log('手动点击跳过');
  
  // 取消正在进行的展开动画
  if (animationFrameId !== null) {
    cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }

  // 立即将卷轴设置为完全展开状态
  scrollWidth.value = 100;
  const scrollWidthPx = (scrollWidth.value / 100) * window.innerWidth;
  const halfWidth = scrollWidthPx / 2;
  leftHandleOffset.value = -halfWidth;
  rightHandleOffset.value = halfWidth;

  // 立即隐藏文字，准备开始收起动画
  animationComplete.value = false;

  // 开始收起动画
  closeScrollAndNavigate();
};

// 收起卷轴并跳转到首页
const closeScrollAndNavigate = () => {
  // 防止重复执行
  if (isClosing.value) {
    console.log('收起动画已在进行中，忽略重复调用');
    return;
  }

  isClosing.value = true;

  // 立即隐藏文字内容并显示把手
  animationComplete.value = false;

  // 等待把手完全显示（0.5秒过渡时间）后再开始收起动画
  setTimeout(() => {
    const startTime = Date.now();
    const CLOSE_DURATION = 2000; // 收起动画2秒
    
    const closeAnimate = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / CLOSE_DURATION, 1);
      const easeProgress = easeOutCubic(progress);

      // 反向收起
      const currentWidth = 100 - easeProgress * 100;
      scrollWidth.value = currentWidth;
      
      const scrollWidthPx = (scrollWidth.value / 100) * window.innerWidth;
      const halfWidth = scrollWidthPx / 2;
      leftHandleOffset.value = -halfWidth;
      rightHandleOffset.value = halfWidth;

      if (progress < 1) {
        requestAnimationFrame(closeAnimate);
      } else {
        // 动画完成后立即跳转到首页，不延迟
        router.push('/');
      }
    };

    requestAnimationFrame(closeAnimate);
  }, 500); // 等待把手的opacity过渡完成
};

const startAnimation = () => {
  const startTime = Date.now();
  
  const animate = () => {
    const elapsed = Date.now() - startTime;
    const progress = Math.min(elapsed / ANIMATION_DURATION, 1);
    
    // 使用缓动函数使动画更自然
    const easeProgress = easeOutCubic(progress);

    // 计算卷轴展开的宽度（从0到100vw）
    scrollWidth.value = easeProgress * 100;

    // 把手紧贴着画卷边缘移动
    // 画卷宽度是 scrollWidth vw，画卷居中
    const scrollWidthPx = (scrollWidth.value / 100) * window.innerWidth;
    const halfWidth = scrollWidthPx / 2;
    
    // 左右把手对称移动
    leftHandleOffset.value = -halfWidth;
    rightHandleOffset.value = halfWidth;

    // 当动画进行到60%时就显示内容
    if (progress >= 0.6 && !animationComplete.value) {
      animationComplete.value = true;
    }

    if (progress < 1) {
      animationFrameId = requestAnimationFrame(animate);
    } else {
      animationFrameId = null;
    }
  };

  animationFrameId = requestAnimationFrame(animate);
};

// 缓动函数：三次方缓出
const easeOutCubic = (t: number): number => {
  return 1 - Math.pow(1 - t, 3);
};
</script>

<style scoped>
.scroll-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: linear-gradient(to bottom, #f5e6d3, #e8d4b8);
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Tab 面板样式 */
.tab-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 1000px;
  max-height: 70%;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  z-index: 10;
  overflow: hidden;
  animation: fadeIn 0.6s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

/* Tab 标签头部 */
.tab-headers {
  display: flex;
  background: linear-gradient(135deg, #8b7355, #a0826d);
  border-bottom: 2px solid #6d5d4b;
}

.tab-header {
  flex: 1;
  padding: 16px 20px;
  text-align: center;
  color: #f5e6d3;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
}

.tab-header:last-child {
  border-right: none;
}

.tab-header:hover {
  background: rgba(255, 255, 255, 0.1);
}

.tab-header.active {
  background: #fff;
  color: #8b7355;
  font-weight: 700;
}

.tab-header.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #8b7355;
}

/* Tab 内容区域 */
.tab-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #fff;
}

.tab-content::-webkit-scrollbar {
  width: 8px;
}

.tab-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.tab-content::-webkit-scrollbar-thumb {
  background: #8b7355;
  border-radius: 4px;
}

.tab-content::-webkit-scrollbar-thumb:hover {
  background: #6d5d4b;
}

/* 选项组 */
.option-group {
  margin-bottom: 32px;
}

.option-group:last-child {
  margin-bottom: 0;
}

.group-title {
  font-size: 18px;
  font-weight: 700;
  color: #8b7355;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e8d4b8;
}

/* 选项列表 */
.options-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.option-item:hover {
  background: #f5e6d3;
  border-color: #8b7355;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.15);
}

.option-item input[type="checkbox"] {
  margin-right: 12px;
  margin-top: 2px;
  cursor: pointer;
  width: 18px;
  height: 18px;
  accent-color: #8b7355;
  flex-shrink: 0;
}

.option-label {
  font-weight: 600;
  color: #333;
  margin-right: 8px;
  white-space: nowrap;
}

.option-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
}

/* 保存按钮容器 */
.save-button-container {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: center;
}

.save-button {
  padding: 12px 40px;
  background: linear-gradient(135deg, #8b7355, #a0826d);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
}

.save-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #6d5d4b, #8b7355);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(139, 115, 85, 0.4);
}

.save-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 欢迎面板 - 古籍食养 */
.welcome-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  animation: fadeIn 0.6s ease-out;
}

.main-title {
  font-size: 96px;
  font-weight: 900;
  color: #5d4d3b; /* 更深的颜色 */
  text-shadow: 4px 4px 8px rgba(0, 0, 0, 0.3); /* 增强阴影 */
  letter-spacing: 20px; /* 更大的字间距 */
  font-family: "STXingkai", "Xingkai SC", "KaiTi", "STKaiti", serif;
}

/* 固定在右下角的跳过按钮 */
.skip-button-fixed {
  position: fixed;
  bottom: 40px;
  right: 40px;
  padding: 14px 48px;
  background: rgba(139, 115, 85, 0.9);
  color: #fff;
  border: 2px solid #8b7355;
  border-radius: 30px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.3);
  z-index: 100;
}

.skip-button-fixed:hover {
  background: #6d5d4b;
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(139, 115, 85, 0.4);
}

/* 把手样式 */
.handle {
  position: absolute;
  height: 80vh;
  display: flex;
  align-items: center;
  z-index: 10;
  transition: opacity 0.5s ease-out;
}

.handle img {
  height: 100%;
  width: auto;
  object-fit: contain;
}

.handle-left {
  right: 50%;
  transform-origin: right center;
}

.handle-right {
  left: 50%;
  transform-origin: left center;
}

.handle-hidden {
  opacity: 0;
  pointer-events: none;
}

/* 卷轴内容样式 */
.scroll-content {
  position: absolute;
  height: 80vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  left: 50%;
  transform: translateX(-50%);
}

.scroll-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.scroll-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .skip-button-fixed {
    bottom: 20px;
    right: 20px;
    padding: 10px 32px;
    font-size: 16px;
  }
  
  .tab-panel {
    width: 90%;
    max-height: 80%;
  }
  
  .tab-headers {
    flex-wrap: wrap;
  }
  
  .tab-header {
    flex: 1 1 33.333%;
    font-size: 14px;
    padding: 12px 8px;
  }
  
  .tab-content {
    padding: 16px;
  }
  
  .options-list {
    grid-template-columns: 1fr;
  }
  
  .main-title {
    font-size: 64px;
    letter-spacing: 12px; /* 移动端也相应增大字间距 */
  }
  
  .handle {
    height: 60vh;
  }
  
  .scroll-content {
    height: 60vh;
  }
}
</style>
