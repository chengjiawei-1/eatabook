<template>
  <section
    class="relative py-24 overflow-hidden
           bg-gradient-to-b from-amber-50 to-white
           dark:from-slate-900 dark:to-slate-800"
  >
    <!-- 漂浮背景光点 -->
    <div class="absolute inset-0 pointer-events-none">
      <span class="float-dot top-[20%] left-[15%]"></span>
      <span class="float-dot top-[40%] left-[80%]"></span>
      <span class="float-dot top-[70%] left-[30%]"></span>
    </div>

    <div class="container mx-auto px-4 max-w-5xl relative z-10">
      <!-- 主体 -->
      <div
        v-motion="{
          initial: { opacity: 0, y: 40 },
          enter: { opacity: 1, y: 0, transition: { type: 'spring', stiffness: 120, damping: 22 } }
        }"
        class="relative"
      >
        <!-- Badge -->
        <!-- 标题 -->
        <h2 class="text-3xl md:text-4xl font-serif-title font-bold text-amber-800 dark:text-amber-300 mb-12">
          坚持食养 · 见证改变
        </h2>
        <!-- 打字机文案 -->
        <div class="space-y-6 text-amber-700/80 dark:text-amber-200/80 leading-relaxed">
          <p class="text-lg">
            <span class="font-bold">每日食养建议：</span>
            <span v-for="(char, index) in typedText1" :key="index" class="text-char">
              {{ char }}
            </span>
            <span v-if="isTyping1" class="cursor"></span>
          </p>

          <p class="text-lg">
            <span class="font-bold">打卡记录：</span>
            <span v-for="(char, index) in typedText2" :key="index" class="text-char">
              {{ char }}
            </span>
            <span v-if="isTyping2" class="cursor"></span>
          </p>

          <p class="text-lg">
            <span class="font-bold">进度追踪：</span>
            <span v-for="(char, index) in typedText3" :key="index" class="text-char">
              {{ char }}
            </span>
            <span v-if="isTyping3" class="cursor"></span>
          </p>
        </div>

        <!-- 🌿 食养成长 · 时间流 -->
        <div class="relative mt-12 pl-6">
          <!-- 中轴线 -->
          <div
            class="absolute left-3 top-0 bottom-0 w-px
                   bg-gradient-to-b from-amber-300/60 via-amber-400/40 to-transparent"
          ></div>

          <!-- 节点 -->
          <div
            v-for="(item, index) in flowList"
            :key="index"
            v-motion="{
              initial: { opacity: 0, y: 30 },
              enter: { opacity: 1, y: 0, transition: { delay: index * 0.15 } }
            }"
            class="relative mb-10 flex items-start gap-6 group"
          >
            <!-- 节点圆点 -->
            <div
              class="relative z-10 w-6 h-6 rounded-full
                     bg-gradient-to-br from-amber-400 to-amber-600
                     shadow-lg shadow-amber-400/40"
            >
              <span
                class="absolute inset-0 rounded-full
                       bg-amber-400/40 animate-ping"
              ></span>
            </div>

            <!-- 文案 -->
            <div
              class="pt-0.5 transition-transform duration-300
                     group-hover:translate-x-1"
            >
                <h3 class="text-xl font-serif-title font-bold text-amber-800 dark:text-amber-300 mb-2">
                  {{ item.title }}
                </h3>
              <p class="text-amber-700/70 dark:text-amber-200/70 leading-relaxed">
                {{ item.desc }}
              </p>
            </div>
          </div>
        </div>

        <!-- CTA -->
        <div class="mt-18 text-center">
          <button
            @click="handleStartCheckin"
            class="inline-flex items-center gap-2 px-12 py-4
                   bg-gradient-to-r from-amber-600 to-amber-700
                   hover:from-amber-500 hover:to-amber-600
                   text-white font-bold rounded-full
                   shadow-xl transition-all
                   hover:-translate-y-1 active:scale-95"
          >
            开始我的食养打卡
            <i class="fa-solid fa-arrow-right"></i>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 弹窗组件 -->
    <JudgeMent 
      :show="showJudgement"
      message="您近7天还未进行体质测试哟~请先进行体质测试"
      @close="showJudgement = false"
      @go-to-test="showJudgement = false"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useAuthStore } from "@/stores/auth"
import { getHistoryAPI } from "@/api/constitution"
import JudgeMent from "./JudgeMent.vue"

//打字机文案
const text1 = 
  "根据您的体质特点和当季节气，为您提供个性化的食养建议，让您的饮食更科学、更健康。"
const text2 = 
  "记录您每天的食养情况，轻松打卡，逐步建立稳定而健康的饮食习惯。"
const text3 = 
  "通过可视化方式呈现打卡进度和坚持天数，陪伴您见证身体的积极变化。"

const typedText1 = ref("")
const typedText2 = ref("")
const typedText3 = ref("")

const isTyping1 = ref(true)
const isTyping2 = ref(false)
const isTyping3 = ref(false)

let i1 = 0
let i2 = 0
let i3 = 0

const randomDelay = (base = 40) => base + Math.random() * 30

const type1 = () => {
  if (i1 < text1.length) {
    typedText1.value += text1[i1++]
    setTimeout(type1, randomDelay())
  } else {
    isTyping1.value = false
    isTyping2.value = true
    setTimeout(type2, 600)
  }
}

const type2 = () => {
  if (i2 < text2.length) {
    typedText2.value += text2[i2++]
    setTimeout(type2, randomDelay())
  } else {
    isTyping2.value = false
    isTyping3.value = true
    setTimeout(type3, 600)
  }
}

const type3 = () => {
  if (i3 < text3.length) {
    typedText3.value += text3[i3++]
    setTimeout(type3, randomDelay())
  } else {
    isTyping3.value = false
  }
}

onMounted(() => {
  type1()
})

//时间流数据

const flowList = [
  {
    title: "个性化食养方案",
    desc: "结合体质与节气变化，为你生成贴近生活的专属食养建议。"
  },
  {
    title: "每日轻松打卡",
    desc: "不打扰、不焦虑，用最轻的方式记录你的坚持。"
  },
  {
    title: "进度可视化",
    desc: "把坚持变成看得见的变化，让动力自然生长。"
  },
  {
    title: "成就与奖励",
    desc: "坚持打卡7天，就能获取菜品教程兑换积分。"
  }
]

//体质测试判断功能
const router = useRouter()
const authStore = useAuthStore()
const showJudgement = ref(false)

// 判断用户近7天是否进行了体质测试
const hasRecentConstitutionTest = async () => {
  if (!authStore.user?.userId) return false
  
  try {
    const testHistory = await getHistoryAPI(authStore.user.userId)
    const historyData = Array.isArray(testHistory) ? testHistory : testHistory.data || []
    
    if (historyData.length === 0) return false
    
    // 计算7天前的日期
    const sevenDaysAgo = new Date()
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
    
    // 检查是否有近7天的测试记录
    return historyData.some((record: any) => {
      if (!record.testTime) return false
      const testDate = new Date(record.testTime)
      return testDate >= sevenDaysAgo
    })
  } catch (error) {
    console.error("获取体质测试历史失败:", error)
    return false
  }
}

// 处理开始食养打卡按钮点击
const handleStartCheckin = async () => {
  // 判断用户是否有近7天的体质测试记录
  const hasTest = await hasRecentConstitutionTest()
  
  if (hasTest) {
    // 如果有记录，直接跳转到食养打卡页面
    router.push("/diet-checkin")
  } else {
    // 如果没有记录，显示弹窗提醒
    showJudgement.value = true
  }
}
</script>

<style scoped>
/* 打字字符 */
.text-char {
  white-space: pre;
}

/* 光标 */
.cursor {
  display: inline-block;
  width: 6px;
  height: 1.2em;
  background-color: currentColor;
  margin-left: 4px;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%,
  50% {
    opacity: 1;
  }
  51%,
  100% {
    opacity: 0;
  }
}

/* 漂浮光点 */
.float-dot {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 9999px;
  background: rgba(251, 191, 36, 0.4);
  animation: float 6s ease-in-out infinite;
}

.float-dot:nth-child(2) {
  animation-delay: 2s;
}
.float-dot:nth-child(3) {
  animation-delay: 4s;
}

@keyframes float {
  0% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px);
    opacity: 1;
  }
  100% {
    transform: translateY(0);
    opacity: 0.6;
  }
}
</style>