<template>
  <div class="laoji-container">
    <Transition name="scale-fade">
      <div v-if="isCollapsed" class="mini-ball" @click="isCollapsed = false">
        <div class="ball-inner"></div>
        <div class="ball-pulse"></div>
      </div>
    </Transition>

    <Transition name="scale-fade">
      <div v-if="!isCollapsed" class="laoji-content-wrapper">
        <div class="avatar-group" @click="toggleChat">
          <div v-if="!showChat" class="classic-hover-tip">老己啊，在这呢。</div>
          
          <img :src="ipImageUrl" alt="老己" :class="['laoji-ip', { 'is-active': showChat }]" />
          
          <div class="collapse-x" @click.stop="isCollapsed = true" title="收起">×</div>
        </div>

        <Transition name="fade-up">
          <div v-if="showChat" class="chat-window">
            <div class="chat-header">
              <div class="header-title">
                <span class="classic-dot"></span>
                老己的安全屋
              </div>
              <div class="header-ops">
                <button class="icon-btn" title="刷新对话" @click="resetChat">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                    <path d="M17.65 6.35A7.958 7.958 0 0012 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
                  </svg>
                </button>
                <button class="close-btn" @click="showChat = false">×</button>
              </div>
            </div>
            
            <div class="chat-messages" ref="msgContainer">
              <div v-for="(msg, index) in messages" :key="index" :class="['msg-wrapper', msg.role]">
                <div class="msg-content">{{ msg.text }}</div>
              </div>
              
              <div v-if="isTyping" class="msg-wrapper assistant">
                <div class="msg-content typing">
                  {{ currentStreamingText }}
                  <span class="cursor">|</span>
                </div>
              </div>
            </div>

            <div class="chat-input-area">
              <input 
                v-model="userInput" 
                @keyup.enter="handleSend" 
                placeholder="和老己说说话..." 
                :disabled="isTyping"
              />
              <button @click="handleSend" :disabled="!userInput.trim() || isTyping">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"></path>
                </svg>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { chatStream } from '@/api/ai';

const ipImageUrl = new URL('@/assets/images/argentip.png', import.meta.url).href;

const showChat = ref(false);
const isCollapsed = ref(false);
const userInput = ref('');
const isTyping = ref(false);
const currentStreamingText = ref('');
const msgContainer = ref<HTMLElement | null>(null);

const defaultMessage = { role: 'assistant', text: '老己啊，看你气色一般，是不是又在外面“负重行”了？来进屋坐，咱翻翻古籍，喝碗热汤。' };
const messages = ref([{ ...defaultMessage }]);

const toggleChat = () => {
  showChat.value = !showChat.value;
  if (showChat.value) scrollToBottom();
};

const resetChat = () => {
  isTyping.value = false;
  currentStreamingText.value = '';
  messages.value = [{ role: 'assistant', text: '老己啊，刚才那是翻篇了。咱重新唠唠，你想问点啥？' }];
};

const handleSend = async () => {
  if (!userInput.value.trim() || isTyping.value) return;
  const prompt = userInput.value;
  messages.value.push({ role: 'user', text: prompt });
  userInput.value = '';
  isTyping.value = true;
  currentStreamingText.value = '';

  try {
    await chatStream({ agent: 'laoji', prompt }, (chunk) => {
      currentStreamingText.value += chunk;
      scrollToBottom();
    }, () => {
      if (currentStreamingText.value) messages.value.push({ role: 'assistant', text: currentStreamingText.value });
      currentStreamingText.value = '';
      isTyping.value = false;
      scrollToBottom();
    });
  } catch (err) {
    messages.value.push({ role: 'assistant', text: '哎呀老己，我这会儿断片了，等下再聊？' });
    isTyping.value = false;
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
  });
};
</script>

<style scoped>
/* 容器：固定宽高，作为所有组件的对齐基准点 */
.laoji-container {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 9999;
  font-family: "Microsoft YaHei", sans-serif;
  /* 固定为头像宽度，确保切换圆圈时中心点不抖动 */
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 1. 收起后的圆点：强制在父容器中心 */
.mini-ball {
  position: absolute;
  width: 40px;
  height: 40px;
  background: #b86207;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(184, 98, 7, 0.3);
}

.ball-inner {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.ball-pulse {
  position: absolute;
  width: 40px;
  height: 40px;
  border: 2px solid #b86207;
  border-radius: 50%;
  animation: pulse 2s infinite ease-out;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.6); opacity: 0; }
}

/* 2. 正常状态布局：绝对定位撑满容器 */
.laoji-content-wrapper {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-group {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 还原经典气泡样式：基于头像中心对齐 */
.classic-hover-tip {
  position: absolute;
  top: -45px;
  left: 50%;
  transform: translateX(-50%) translateY(10px);
  background: white;
  padding: 6px 14px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  font-size: 13px;
  color: #5d534a;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: all 0.3s ease;
}

.avatar-group:hover .classic-hover-tip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.laoji-ip {
  width: 90px;
  height: auto;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: float 4s infinite ease-in-out;
}

.laoji-ip.is-active { transform: scale(0.85) rotate(-5deg); }

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* 收起叉叉：定位在头像边缘 */
.collapse-x {
  position: absolute;
  top: 0;
  left: 0;
  width: 22px;
  height: 22px;
  background: rgba(93, 83, 74, 0.6);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-group:hover .collapse-x { opacity: 1; }

/* 3. 聊天窗样式 (回归原版) */
.chat-window {
  position: absolute;
  bottom: 110px;
  right: 0;
  width: 340px;
  height: 500px;
  background: #fdfaf5;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 15px 50px rgba(0,0,0,0.15);
  overflow: hidden;
  border: 1px solid #efe8d8;
  cursor: default; /* 防止点击窗口触发关闭 */
}

.chat-header {
  background: #efe8d8;
  padding: 12px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title { display: flex; align-items: center; gap: 8px; font-weight: bold; color: #5d534a; }
.classic-dot { width: 8px; height: 8px; background: #8e9e82; border-radius: 50%; }

.chat-messages { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 15px; }
.msg-wrapper { max-width: 85%; display: flex; }
.msg-wrapper.user { align-self: flex-end; }
.msg-wrapper.assistant { align-self: flex-start; }

.msg-content { padding: 10px 14px; border-radius: 15px; font-size: 14px; line-height: 1.6; }
.user .msg-content { background: #b86207; color: white; border-bottom-right-radius: 2px; }
.assistant .msg-content { background: white; color: #4a4a4a; border: 1px solid #eceae0; border-bottom-left-radius: 2px; }

.chat-input-area { padding: 15px; background: white; border-top: 1px solid #f0ede5; display: flex; gap: 10px; }
.chat-input-area input { flex: 1; border: 1px solid #e0ddd5; padding: 10px 14px; border-radius: 10px; outline: none; background: #faf9f6; }
.chat-input-area button { background: #b86207; color: white; border: none; width: 40px; height: 40px; border-radius: 10px; cursor: pointer; display: flex; align-items: center; justify-content: center; }

/* 动画效果：统一缩放原点 */
.scale-fade-enter-active, .scale-fade-leave-active { 
  transition: all 0.3s ease; 
  position: absolute; /* 切换时重叠，防止布局撑开 */
}
.scale-fade-enter-from, .scale-fade-leave-to { opacity: 0; transform: scale(0); }

.fade-up-enter-active, .fade-up-leave-active { transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); }
.fade-up-enter-from, .fade-up-leave-to { opacity: 0; transform: translateY(30px) scale(0.95); }

.cursor { animation: blink 1s infinite; color: #b86207; }
@keyframes blink { 50% { opacity: 0; } }

.icon-btn, .close-btn { background: transparent; border: none; color: #a89f94; cursor: pointer; }
.close-btn { font-size: 24px; }
</style>
