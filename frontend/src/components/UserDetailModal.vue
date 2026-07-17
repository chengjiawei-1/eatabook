<template>
  <Transition name="fade">
    <div v-if="user" class="fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-md bg-slate-900/40">
      <!-- 遮罩层 -->
      <div class="absolute inset-0" @click="$emit('close')"></div>

      <!-- 模态框主体 -->
      <div 
        class="relative bg-[#fdfbf8] dark:bg-slate-900 rounded-[3rem] shadow-3xl w-full max-w-2xl max-h-[92vh] overflow-hidden flex flex-col transform transition-all border border-white dark:border-slate-800"
        @click.stop
      >
        <!-- 1. 头部区域：高级感背景 + 关闭按钮 -->
        <div class="relative h-40 bg-[#5d3a1a] dark:bg-slate-800 overflow-hidden">
          <div class="absolute inset-0 opacity-10 pointer-events-none" style="background-image: url('https://www.transparenttextures.com/patterns/paper-fibers.png');"></div>
          <button 
            @click="$emit('close')" 
            class="absolute top-6 right-6 w-10 h-10 rounded-full bg-white/10 text-white/80 hover:bg-white/20 hover:text-white transition-all flex items-center justify-center z-20 group"
          >
            <i class="fa-solid fa-times text-lg"></i>
          </button>
        </div>

        <!-- 2. 核心个人信息：重叠布局 -->
        <div class="relative z-10 px-10 pb-6 -mt-20">
          <div class="flex flex-col sm:flex-row items-end gap-6">
            <!-- 头像：双重边框高级感 -->
            <div class="w-36 h-36 rounded-[2.5rem] border-[6px] border-[#fdfbf8] dark:border-slate-900 shadow-2xl overflow-hidden bg-white flex-shrink-0">
              <img 
                :src="user.avatarUrl || '/default-avatar.png'" 
                :alt="user.username" 
                class="w-full h-full object-cover"
                @error="(e) => (e.target as HTMLImageElement).src = '/default-avatar.png'"
              />
            </div>
            
            <!-- 名字与角色 -->
            <div class="flex-1 pb-4">
              <div class="flex flex-wrap items-center gap-3 mb-2">
                <h2 class="text-3xl font-black text-slate-800 dark:text-slate-100 tracking-tight">{{ user.username }}</h2>
                <span :class="[
                  'px-3 py-1 rounded-xl text-[10px] font-black uppercase tracking-widest border',
                  user.role === 'admin' 
                    ? 'bg-rose-50 text-rose-600 border-rose-100 dark:bg-rose-900/30' 
                    : 'bg-blue-50 text-blue-600 border-blue-100 dark:bg-blue-900/30'
                ]">
                  {{ user.role === 'admin' ? 'Administrator' : 'System Member' }}
                </span>
              </div>
              <p class="text-slate-400 font-bold text-sm flex items-center gap-2 uppercase tracking-wider">
                <i class="fa-solid fa-envelope text-amber-500"></i> {{ user.email }}
              </p>
            </div>
          </div>
        </div>

        <!-- 3. 内容区域 -->
        <div class="flex-1 overflow-y-auto px-10 py-4 space-y-10 custom-scrollbar pb-10">
          
          <!-- 数据概览卡片 (统一统计风格) -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div class="stat-inner-card">
              <div class="stat-label">体质测评</div>
              <div class="stat-value text-amber-600">{{ testCount }}<span class="text-xs ml-1 opacity-50">次</span></div>
            </div>
            <div class="stat-inner-card">
              <div class="stat-label">膳食收藏</div>
              <div class="stat-value text-emerald-600">{{ dishCollectionCount }}<span class="text-xs ml-1 opacity-50">个</span></div>
            </div>
            <div class="stat-inner-card">
              <div class="stat-label">主体质</div>
              <div class="stat-value text-indigo-600 text-sm">{{ mainConstitution || '未测' }}</div>
            </div>
            <div class="stat-inner-card">
              <div class="stat-label">兼夹性</div>
              <div class="stat-value text-purple-600 text-sm">{{ subConstitution || '无' }}</div>
            </div>
          </div>

          <!-- 详细信息列表 (统一 detail-item) -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-12 gap-y-8">
            <div class="detail-item">
              <label>联络手机 Mobile</label>
              <p>{{ user.phone || '未绑定号码' }}</p>
            </div>
            <div class="detail-item">
              <label>籍贯分布 Location</label>
              <p>{{ user.nativePlace || '未录入地理信息' }}</p>
            </div>
            <div class="detail-item">
              <label>注册日期 Join Date</label>
              <p>{{ formatDate(user.registerTime) }}</p>
            </div>
            <div class="detail-item">
              <label>最后活跃 Last Activity</label>
              <p>{{ formatDate(user.lastLoginTime) || 'Never logged in' }}</p>
            </div>
          </div>

          <!-- 管理操作区 (高级深色风格) -->
          <div class="pt-8 border-t border-slate-100 dark:border-slate-800">
            <h3 class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-6">账户安全与权限管理 Governance</h3>
            
            <div class="space-y-4">
              <!-- 角色切换：改用系统统一 select 样式 -->
              <div class="flex items-center justify-between p-6 bg-slate-50 dark:bg-slate-800/50 rounded-3xl border border-slate-100 dark:border-slate-800">
                <div>
                  <div class="font-black text-slate-700 dark:text-slate-200 text-sm">权限等级变更</div>
                  <div class="text-[10px] font-bold text-slate-400 uppercase mt-1">Impacts administrative access</div>
                </div>
                <select 
                  :value="user.role"
                  @change="handleRoleChange"
                  class="bg-white dark:bg-slate-700 border-none shadow-sm text-slate-700 dark:text-slate-200 text-xs font-black rounded-xl px-4 py-2 focus:ring-2 focus:ring-amber-500 cursor-pointer"
                >
                  <option value="user">普通用户</option>
                  <option value="admin">系统管理员</option>
                </select>
              </div>

              <!-- 危险操作按钮组 -->
              <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <button @click="handleResetPassword" :disabled="resettingPassword" class="row-action-btn-large group">
                  <i :class="['fa-solid', resettingPassword ? 'fa-spinner fa-spin' : 'fa-key', 'text-amber-500 group-hover:rotate-12 transition-transform']"></i>
                  <span>重置密码</span>
                </button>
                
                <button @click="handleExportData" class="row-action-btn-large group">
                  <i class="fa-solid fa-file-export text-indigo-500 group-hover:translate-y-[-2px] transition-transform"></i>
                  <span>导出档案</span>
                </button>
                
                <button @click="confirmDelete" class="row-action-btn-large group text-rose-600 hover:bg-rose-50 border-rose-50">
                  <i class="fa-solid fa-user-xmark text-rose-500 group-hover:scale-110 transition-transform"></i>
                  <span>注销账户</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 4. 底部状态反馈 -->
        <Transition name="fade">
          <div v-if="successMessage || errorMessage" class="px-10 py-4 bg-white dark:bg-slate-900 border-t border-slate-50 dark:border-slate-800 text-[11px] font-black uppercase tracking-widest text-center">
            <span v-if="successMessage" class="text-emerald-500 flex items-center justify-center gap-2">
              <i class="fa-solid fa-circle-check"></i> {{ successMessage }}
            </span>
            <span v-if="errorMessage" class="text-rose-500 flex items-center justify-center gap-2">
              <i class="fa-solid fa-circle-exclamation"></i> {{ errorMessage }}
            </span>
          </div>
        </Transition>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
/* 脚本逻辑保持 100% 原始实现，仅增加了格式化逻辑 */
import { ref, watch } from 'vue'
import type { UserInfo } from '@/api/user'
import { updateUserAPI, updatePasswordAPI, deleteUserAPI } from '@/api/user'
import { getHistoryAPI } from '@/api/constitution'
import { collectionApi } from '@/api/collection'

interface Props { user: UserInfo | null }
const props = defineProps<Props>()
const emit = defineEmits(['close', 'change-role', 'delete-user'])

const loading = ref(false)
const resettingPassword = ref(false)
const testCount = ref(0)
const dishCollectionCount = ref(0)
const mainConstitution = ref('')
const subConstitution = ref('')
const successMessage = ref('')
const errorMessage = ref('')

const loadUserStats = async () => {
  if (!props.user) return
  loading.value = true
  try {
    const historyRes = await getHistoryAPI(props.user.userId)
    const historyList = Array.isArray(historyRes) ? historyRes : (historyRes as any).data || []
    testCount.value = historyList.length
    if (historyList.length > 0) {
      const latest = historyList[historyList.length - 1] 
      mainConstitution.value = latest.mainConstitution || '平和质'
      subConstitution.value = latest.subConstitution || '无'
    } else {
      mainConstitution.value = '未测试'; subConstitution.value = '-'
    }
    const collectionRes = await collectionApi.getUserCollections(props.user.userId)
    const collections = collectionRes.data || []
    dishCollectionCount.value = collections.filter((i: any) => i.targetType === 'dish').length
  } catch (e) { console.error(e) } finally { loading.value = false }
}

watch(() => props.user, (newUser) => { if (newUser) loadUserStats() }, { immediate: true })

const handleRoleChange = async (e: Event) => {
  if (!props.user) return
  const select = e.target as HTMLSelectElement; const newRole = select.value
  try {
    await updateUserAPI({ userId: props.user.userId, role: newRole })
    successMessage.value = `Permissions Updated to ${newRole.toUpperCase()}`
    emit('change-role', newRole)
    setTimeout(() => successMessage.value = '', 3000)
  } catch (e: any) { errorMessage.value = e.message; select.value = props.user.role || 'user' }
}

const handleResetPassword = async () => {
  if (!props.user || !confirm('确定重置密码为随机密码吗？')) return
  resettingPassword.value = true
  try {
    // 生成随机密码（8位，包含字母和数字）
    const randomPassword = generateRandomPassword()
    await updatePasswordAPI(props.user.userId, randomPassword)
    successMessage.value = `密码已重置为: ${randomPassword}（请及时通知用户修改）`
    setTimeout(() => successMessage.value = '', 3000)
  } catch (e: any) { errorMessage.value = '重置失败' } finally { resettingPassword.value = false }
}

// 生成随机密码函数
const generateRandomPassword = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let password = ''
  for (let i = 0; i < 8; i++) {
    password += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return password
}

const confirmDelete = () => { if (confirm('⚠️ 危险操作：注销后不可恢复！')) handleDeleteUser() }
const handleDeleteUser = async () => {
  if (!props.user) return
  try { await deleteUserAPI(props.user.userId); emit('delete-user'); emit('close') } 
  catch (e: any) { errorMessage.value = 'Delete Failed' }
}

const handleExportData = () => {
  if (!props.user) return
  const blob = new Blob([JSON.stringify(props.user, null, 2)], { type: 'application/json' })
  const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = `user_${props.user.username}.json`; a.click()
  successMessage.value = 'Profile Exported Successfully'
  setTimeout(() => successMessage.value = '', 2000)
}

const formatDate = (str?: string) => str ? new Date(str).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }) : '-'
</script>

<style scoped>
.stat-inner-card {
  @apply bg-white dark:bg-slate-800/80 rounded-3xl p-4 text-center border border-slate-100 dark:border-slate-800 shadow-sm transition-all hover:border-amber-200 hover:shadow-md;
}
.stat-label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1.5;
}
.stat-value {
  @apply font-black tracking-tight;
}

.detail-item label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 block ml-1;
}
.detail-item p {
  @apply text-sm font-black text-slate-800 dark:text-slate-200 leading-relaxed;
}

.row-action-btn-large {
  @apply flex flex-col items-center justify-center gap-2 p-4 bg-white dark:bg-slate-800 rounded-[2rem] border border-slate-100 dark:border-slate-800 text-[10px] font-black uppercase tracking-widest text-slate-600 dark:text-slate-400 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all active:scale-95;
}

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>