<template>
  <div class="max-w-4xl mx-auto px-3 sm:px-6 pb-3 sm:pb-6 antialiased">
    
    <div v-if="loading" class="flex flex-col items-center justify-center py-24">
      <i class="fa-solid fa-yin-yang fa-spin text-4xl text-[#d4a373] mb-6"></i>
      <p class="text-lg text-[#8a6d51] font-serif tracking-widest animate-pulse">正在为您翻阅中医馆档案...</p>
    </div>
    
    <div v-else class="grid grid-cols-1 gap-6">
      <div
        v-for="report in reports"
        :key="report.testId"
        class="group relative bg-white dark:bg-slate-800 border-2 border-[#f2ece4] dark:border-slate-700 rounded-2xl p-6 transition-all hover:shadow-2xl hover:border-[#d4a373] cursor-pointer"
        @click="openReportDetail(report)"
      >
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div class="flex items-center gap-6">
            <div class="w-16 h-16 rounded-2xl border-2 border-[#f2ece4] flex flex-col items-center justify-center bg-[#fdfaf7] text-[#8a6d51] group-hover:bg-[#d4a373] group-hover:text-white transition-colors">
              <span class="text-xs leading-none uppercase font-bold mb-1">{{ getMonth(report.testTime) }}</span>
              <span class="text-2xl font-serif font-black">{{ getDay(report.testTime) }}</span>
            </div>
            <div>
              <div class="font-serif font-bold text-[#6d4c3d] dark:text-[#deb887] text-xl mb-1">体质分析报告</div>
              <div class="text-sm text-gray-500 flex items-center gap-2">
                <i class="fa-regular fa-clock text-[#d4a373]"></i> {{ report.testTime }}
              </div>
            </div>
          </div>
          <div class="flex items-center justify-between md:justify-end gap-8 border-t md:border-t-0 pt-4 md:pt-0 border-[#f2ece4]">
            <div class="text-left md:text-right">
              <span class="inline-block px-5 py-2 bg-[#fdfaf7] border-2 border-[#d4a373]/30 rounded-xl text-lg font-serif font-bold text-[#8a6d51]">{{ report.mainConstitution }}</span>
            </div>
            <button
              class="report-action-btn"
              title="下载体质分析报告"
              @click.stop="downloadReport(report)"
            >
              <i class="fa-solid fa-file-arrow-down"></i>
            </button>
            <i class="fa-solid fa-chevron-right text-2xl text-[#e3d5ca] group-hover:text-[#d4a373] group-hover:translate-x-2 transition-all"></i>
          </div>
        </div>
      </div>
    </div>

    <Transition name="fade-modal">
      <div v-if="selectedReport" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-[#4a3728]/40 backdrop-blur-[4px]" @click="selectedReport = null"></div>
        
        <div class="bg-[#fdfcf8] dark:bg-slate-900 w-full max-w-3xl max-h-[90vh] overflow-y-auto rounded-3xl shadow-2xl relative border-2 border-[#d4a373]/30 flex flex-col scale-on-enter">
          
          <div class="sticky top-0 z-10 p-6 sm:p-8 bg-[#fdfcf8]/95 dark:bg-slate-900/95 border-b-2 border-[#f2ece4] flex justify-between items-center">
            <div class="flex items-center gap-4">
              <div class="w-3 h-10 bg-[#d4a373] rounded-full"></div>
              <div>
                <h2 class="text-2xl font-serif font-black text-[#6d4c3d] dark:text-[#deb887]">体质调理全案</h2>
                <p class="text-xs text-[#c4a484] tracking-[0.3em] font-bold uppercase">Personalized Health Guide</p>
              </div>
            </div>
            <button @click="selectedReport = null" class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-red-50 text-[#8a6d51] hover:text-red-500 transition-all">
              <i class="fa-solid fa-times text-xl"></i>
            </button>
          </div>

          <div class="p-6 sm:p-10 space-y-12">
            <div class="bg-white dark:bg-slate-800 p-8 rounded-3xl border-2 border-[#f2ece4] shadow-sm">
              <div v-if="!selectedReport.plan" class="py-12 text-center text-[#8a6d51]">
                  正在定制方案...
              </div>
              <div v-else class="flex flex-col md:flex-row gap-8 items-center">
                <div class="flex-1 text-center md:text-left">
                  <div class="text-sm font-bold text-[#c4a484] mb-2 uppercase">辨证结论</div>
                  <h3 class="text-4xl font-serif font-black text-[#8a6d51] mb-3">{{ selectedReport.mainConstitution }}</h3>
                  <div class="inline-flex items-center gap-2 px-3 py-1 bg-[#fdfaf7] border border-[#f2ece4] text-[#8a6d51] rounded-lg text-sm font-medium">
                    兼有：{{ selectedReport.subConstitution || '无' }}
                  </div>
                </div>
                <div class="hidden md:block h-20 w-[2px] bg-[#f2ece4]"></div>
                <div class="flex-1 text-center font-serif text-[#a67c52]">
                  <div class="text-lg font-bold italic mb-2">“ {{ getAphorism(selectedReport.mainConstitution) }} ”</div>
                  <p class="text-sm font-bold text-[#d4a373] mt-2">{{ selectedReport.ageFocus }}</p>
                </div>
              </div>
            </div>

            <div v-if="selectedReport.plan">
              <h4 class="flex items-center gap-3 font-serif font-black text-xl text-[#6d4c3d] mb-6">
                <i class="fa-solid fa-feather-pointed text-[#d4a373]"></i> 治本方针
              </h4>
              <div class="text-lg leading-[2.2] text-gray-700 dark:text-gray-200 bg-[#fdfaf7] dark:bg-slate-800/40 p-8 rounded-2xl border-l-4 border-[#d4a373]">
                {{ selectedReport.plan }}
              </div>
            </div>

            <div v-if="selectedReport.plan" class="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div class="flex flex-col overflow-hidden">
                <h4 class="flex items-center gap-3 font-serif font-black text-xl text-[#6d4c3d] mb-6">
                  <i class="fa-solid fa-utensils text-[#d4a373]"></i> 膳食建议
                </h4>
                <div class="flex-1 p-8 bg-white border-2 border-[#f2ece4] rounded-3xl space-y-6">
                  <div v-for="(val, label) in { '主食推荐': 'foodStaple', '蔬果搭配': 'foodVeg', '肉类选材': 'foodMeat' }" :key="label">
                    <div class="text-sm font-black text-[#c4a484] mb-2">
                      <span class="inline-block w-1.5 h-3 bg-[#d4a373] mr-2"></span>{{ label }}
                    </div>
                    <div class="text-[16px] text-gray-700 leading-relaxed pl-3.5 font-bold">{{ selectedReport[val] }}</div>
                  </div>
                </div>
              </div>
              <div class="flex flex-col overflow-hidden">
                <h4 class="flex items-center gap-3 font-serif font-black text-xl text-red-900/70 mb-6">
                  <i class="fa-solid fa-circle-exclamation"></i> 禁忌提醒
                </h4>
                <div class="flex-1 p-8 bg-red-50/40 border-2 border-red-100 rounded-3xl">
                  <p class="text-[17px] leading-[2.2] text-red-900/80 font-serif font-bold italic">
                    {{ selectedReport.ageAvoid }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="selectedReport.plan" class="grid grid-cols-1 md:grid-cols-2 gap-8 border-t-2 border-dashed border-[#f2ece4] pt-10">
              <section class="bg-[#fdfaf7] p-8 rounded-3xl">
                <h5 class="font-serif font-black text-[#8a6d51] mb-4 text-lg flex items-center gap-2">
                   <i class="fa-solid fa-map-location-dot text-[#d4a373]"></i> 地域时令
                </h5>
                <p class="text-[16px] text-gray-700 leading-loose font-bold">{{ selectedReport.region }}</p>
              </section>
              <section class="bg-[#fdfaf7] p-8 rounded-3xl">
                <h5 class="font-serif font-black text-[#8a6d51] mb-4 text-lg flex items-center gap-2">
                  <i class="fa-solid fa-bed text-[#d4a373]"></i> 日常起居
                </h5>
                <p class="text-[16px] text-gray-700 leading-loose font-bold">{{ selectedReport.daily }}</p>
              </section>
            </div>
          </div>

          <div class="p-8 bg-[#fdfcf8] dark:bg-slate-900 border-t-2 border-[#f2ece4] flex flex-col sm:flex-row justify-center gap-3 sticky bottom-0">
            <button @click="downloadReport(selectedReport)" class="w-full sm:w-64 py-4 bg-[#b86207] text-white rounded-2xl text-lg font-serif font-bold shadow-xl active:scale-95 transition-all">
              <i class="fa-solid fa-file-arrow-down mr-2"></i>下载报告
            </button>
            <button @click="selectedReport = null" class="w-full sm:w-64 py-4 bg-[#8a6d51] text-white rounded-2xl text-lg font-serif font-bold shadow-xl active:scale-95 transition-all">
              关闭报告
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.font-serif { font-family: "Source Han Serif CN", serif; }

/* 针对“不丝滑”背景的优化：改用 CSS 关键帧和硬件加速曲线 */
.fade-modal-enter-active, .fade-modal-leave-active {
  transition: opacity 0.4s ease;
}

.fade-modal-enter-from, .fade-modal-leave-to {
  opacity: 0;
}

/* 窗体的位移+缩放效果，使用苹果常用的贝塞尔曲线 */
.fade-modal-enter-active .scale-on-enter {
  animation: modal-pop 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.fade-modal-leave-active .scale-on-enter {
  animation: modal-pop 0.3s cubic-bezier(0.2, 0.8, 0.2, 1) reverse;
}

@keyframes modal-pop {
  from { opacity: 0; transform: scale(0.96) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.max-h-\[90vh\]::-webkit-scrollbar { width: 5px; }
.max-h-\[90vh\]::-webkit-scrollbar-thumb { background: #d4a373; border-radius: 10px; }

.report-action-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid #eadbca;
  background: #fffaf4;
  color: #8a6d51;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.report-action-btn:hover {
  background: #8a6d51;
  color: #fff;
}
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHistoryAPI } from '@/api/constitution'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const reports = ref<any[]>([])
const total = ref(0)
const selectedReport = ref<any>(null)

// 日期解析优化
const getDay = (dateStr: string) => {
  if (!dateStr) return '00'
  const date = new Date(dateStr)
  return date.getDate().toString().padStart(2, '0')
}

const getMonth = (dateStr: string) => {
  if (!dateStr) return 'NAN'
  const months = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC']
  return months[new Date(dateStr).getMonth()]
}

const getAphorism = (con: string) => {
  const dict: Record<string, string> = {
    '平和质': '阴阳匀平，气血和畅',
    '气虚质': '气力不充，温养为主',
    '阳虚质': '阳气不足，宜补火种',
    '阴虚质': '滋阴清热，静养生津',
    '痰湿质': '祛湿化痰，动则生阳',
    '湿热质': '清热利湿，忌避肥甘',
    '血瘀质': '疏肝理气，活血化瘀',
    '气郁质': '解郁舒压，顺气开怀',
    '特禀质': '固表辟邪，慎避异物'
  }
  return dict[con] || '调和阴阳，顺应四时'
}

const normalizeText = (value: any, fallback = '暂无记录') => {
  const text = String(value || '').trim()
  return text || fallback
}

const downloadTextFile = (filename: string, content: string) => {
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.download = filename
  link.href = url
  link.click()
  URL.revokeObjectURL(url)
}

const downloadReport = (report: any) => {
  if (!report) return

  const main = normalizeText(report.mainConstitution, '暂无体质')
  const date = normalizeText(report.testTime, '暂无测试时间')
  const content = [
    '体质分析报告',
    '',
    `生成日期：${new Date().toLocaleDateString()}`,
    `测试时间：${date}`,
    `主体质：${main}`,
    `兼有体质：${normalizeText(report.subConstitution, '无')}`,
    '',
    '辨证结论',
    `体质引语：${getAphorism(report.mainConstitution)}`,
    `年龄关注：${normalizeText(report.ageFocus)}`,
    '',
    '治本方针',
    normalizeText(report.plan),
    '',
    '膳食建议',
    `主食推荐：${normalizeText(report.foodStaple)}`,
    `蔬果搭配：${normalizeText(report.foodVeg)}`,
    `肉类选材：${normalizeText(report.foodMeat)}`,
    '',
    '禁忌提醒',
    normalizeText(report.ageAvoid),
    '',
    '地域时令',
    normalizeText(report.region),
    '',
    '日常起居',
    normalizeText(report.daily),
    '',
    '提示：体质结果是食养参考，不替代医生诊断。若有明显不适，请及时咨询医生或营养师。',
    '',
    'eatabook · 老己食养'
  ].join('\n')

  const filenameDate = String(report.testTime || new Date().toISOString()).slice(0, 10)
  downloadTextFile(`体质分析报告-${main}-${filenameDate}.txt`, content)
}

const loadReports = async () => {
  if (!authStore.user?.userId) return
  loading.value = true
  try {
    // 调用不分页的接口获取全部数据
    const res = await getHistoryAPI(authStore.user.userId)
    
    // 确保拿到的是数组
    const allData = Array.isArray(res) ? res : (res.data || [])

    // 执行严格过滤逻辑
    // 定义必须非空的字段列表
    const requiredFields = [
      'plan', 'region', 'ageAvoid', 'ageFocus', 
      'foodStaple', 'foodVeg', 'foodMeat', 'daily'
    ]

    const filtered = allData.filter((item: any) => {
      // 只有当所有字段都有值（且不是空字符串）时才保留
      return requiredFields.every(field => {
        const val = item[field]
        return val !== null && val !== undefined && String(val).trim() !== ''
      })
    })

    // 赋值给响应式变量
    reports.value = filtered
    total.value = filtered.length
    
    console.log(`原始数据共 ${allData.length} 条，过滤后剩余 ${filtered.length} 条有效报告`)

  } catch (error) {
    console.error('获取历史记录失败:', error)
  } finally {
    loading.value = false
  }
}

const openReportDetail = (report: any) => {
  selectedReport.value = report
}

onMounted(() => {
  loadReports()
})
</script>
