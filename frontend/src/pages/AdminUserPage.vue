<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题与新增操作 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              用户管理中心 <span class="text-amber-500 text-base ml-2">User Registry</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              管理系统注册用户及权限分配，当前共有 <span class="text-emerald-500 font-bold">{{ userStats.total }}</span> 名成员
            </p>
          </div>
          <button 
            @click="openAddUserModal"
            class="px-6 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white font-black rounded-2xl shadow-lg shadow-emerald-200 dark:shadow-none transition-all flex items-center gap-2 active:scale-95"
          >
            <i class="fa-solid fa-user-plus"></i>
            <span>新增系统用户</span>
          </button>
        </div>

        <!-- 统计卡片 (带微型趋势图) -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div v-for="(stat, index) in statsCards" :key="index" 
            class="bg-white dark:bg-slate-900 rounded-3xl p-5 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none transition-all hover:-translate-y-1 group overflow-hidden relative">
            <div class="flex justify-between relative z-10">
              <div>
                <p class="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1">{{ stat.label }}</p>
                <h3 class="text-2xl font-black text-slate-800 dark:text-white">{{ stat.value }}</h3>
              </div>
              <div :class="['w-10 h-10 rounded-xl flex items-center justify-center text-lg shadow-lg', stat.bgClass, stat.textClass]">
                <i :class="['fa-solid', stat.icon]"></i>
              </div>
            </div>
            <!-- 微型图表 -->
            <div class="mt-4 h-12 w-full opacity-30 group-hover:opacity-100 transition-opacity">
               <v-chart class="w-full h-full" :option="getTinyLineOption(stat.textClass)" autoresize />
            </div>
          </div>
        </div>

        <!-- 用户画像分析图表 -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          <div class="lg:col-span-2 bg-white dark:bg-slate-900 rounded-[2.5rem] p-8 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50">
            <h3 class="text-xs font-black text-slate-400 uppercase tracking-widest mb-6 flex items-center gap-2">
              <i class="fa-solid fa-chart-area text-indigo-500"></i> 近 7 日用户增长趋势 Growth
            </h3>
            <div class="h-64 w-full">
              <v-chart :option="registrationTrendOption" autoresize />
            </div>
          </div>
          <div class="bg-white dark:bg-slate-900 rounded-[2.5rem] p-8 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50">
            <h3 class="text-xs font-black text-slate-400 uppercase tracking-widest mb-6 flex items-center gap-2">
              <i class="fa-solid fa-pie-chart text-amber-500"></i> 角色权限配比 Roles
            </h3>
            <div class="h-64 w-full">
              <v-chart :option="roleDistributionOption" autoresize />
            </div>
          </div>
        </div>

        <!-- 筛选与搜索工具栏 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none p-6 border border-white dark:border-slate-800 mb-6">
          <div class="flex flex-col lg:flex-row gap-4 items-center">
            <div class="w-full lg:flex-1 relative">
              <i class="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索用户名、邮箱、手机号、籍贯..."
                class="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800/50 border-none rounded-2xl text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 transition-all font-bold"
              />
            </div>
            <div class="flex flex-wrap gap-3 w-full lg:w-auto">
              <select v-model="roleFilter" class="filter-select">
                <option value="all">全部角色</option>
                <option value="admin">管理员人员</option>
                <option value="user">普通注册用户</option>
              </select>
              <select v-model="sortBy" class="filter-select">
                <option value="registrationDate">按注册时间</option>
                <option value="lastLogin">最后登录时间</option>
                <option value="username">按用户名字</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 用户数据表格 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden">
          <div v-if="loading" class="py-24 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500 font-bold">正在调取核心用户信息...</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[1000px]">
              <thead>
                <tr class="bg-slate-50/50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 text-slate-400 text-[11px] font-black uppercase tracking-widest">
                  <th class="px-8 py-4 w-[25%]">用户信息 Profile</th>
                  <th class="px-6 py-4 w-[20%]">联络资料 Contact</th>
                  <th class="px-6 py-4 w-[20%]">最后活跃 Activity</th>
                  <th class="px-6 py-4 w-[15%] text-center">系统角色 Role</th>
                  <th class="px-8 py-4 w-[20%] text-right">操作管理</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50 dark:divide-slate-800">
                <tr v-for="user in paginatedUsers" :key="user.userId" class="group hover:bg-slate-50/80 transition-all">
                  <td class="px-8 py-6">
                    <div class="flex items-center gap-4">
                      <div class="w-11 h-11 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center text-amber-600 dark:text-amber-400 font-black text-lg shadow-sm overflow-hidden relative">
                        <img v-if="getUserAvatarUrl(user.avatarUrl)" :src="getUserAvatarUrl(user.avatarUrl)" @error="handleAvatarError" class="w-full h-full object-cover">
                        <span v-else>{{ user.username.charAt(0) }}</span>
                        <!-- 在线状态点 -->
                        <div v-if="isUserOnline(user.lastLoginTime)" class="absolute bottom-0 right-0 w-3 h-3 bg-emerald-500 border-2 border-white dark:border-slate-900 rounded-full"></div>
                      </div>
                      <div class="min-w-0">
                        <div class="font-black text-slate-800 dark:text-slate-200 text-sm truncate">{{ user.username }}</div>
                        <div class="text-[10px] text-slate-400 truncate font-bold uppercase tracking-tighter">{{ user.email }}</div>
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-6">
                    <div class="space-y-1">
                      <div v-if="user.phone" class="flex items-center gap-2 text-xs font-bold text-slate-600 dark:text-slate-300">
                        <i class="fa-solid fa-phone text-indigo-400 w-4"></i> {{ user.phone }}
                      </div>
                      <div v-if="user.nativePlace" class="flex items-center gap-2 text-[11px] font-bold text-slate-400">
                        <i class="fa-solid fa-location-dot text-emerald-400 w-4"></i> {{ user.nativePlace }}
                      </div>
                      <div v-if="!user.phone && !user.nativePlace" class="text-[10px] text-slate-300 font-bold uppercase">信息未完善</div>
                    </div>
                  </td>
                  <td class="px-6 py-6">
                    <div class="space-y-1">
                      <div class="text-[10px] font-black text-slate-400 uppercase tracking-widest">注册：{{ formatDate(user.registerTime) }}</div>
                      <div class="text-xs font-black text-slate-700 dark:text-slate-200">
                        <i class="fa-regular fa-clock mr-1 text-amber-500"></i>{{ formatLastLoginTime(user.lastLoginTime) }}
                      </div>
                    </div>
                  </td>
                  <td class="px-6 py-6 text-center">
                    <span :class="[
                      'px-3 py-1 rounded-xl text-[10px] font-black uppercase border tracking-widest',
                      user.role === 'admin' 
                        ? 'bg-rose-50 text-rose-600 border-rose-100 dark:bg-rose-900/20' 
                        : 'bg-blue-50 text-blue-600 border-blue-100 dark:bg-blue-900/20'
                    ]">
                      {{ user.role === 'admin' ? 'Administrator' : 'General User' }}
                    </span>
                  </td>
                  <td class="px-8 py-6 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button @click="viewUserDetail(user)" class="row-action-btn hover:bg-amber-500" title="卷宗"><i class="fa-solid fa-id-card"></i></button>
                      <button @click="editUser(user)" class="row-action-btn hover:bg-indigo-500" title="编辑"><i class="fa-solid fa-pen-to-square"></i></button>
                      <button @click="deleteUser(user)" class="row-action-btn hover:bg-rose-500" title="注销"><i class="fa-solid fa-user-xmark"></i></button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页组件  -->
          <div class="px-8 py-6 bg-slate-50/50 dark:bg-slate-800/30 flex items-center justify-between">
            <span class="text-[11px] font-black text-slate-400 uppercase tracking-widest">
              Displaying {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, filteredUsers.length) }} of {{ filteredUsers.length }}
            </span>
            <div class="flex items-center gap-3">
              <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <div class="px-5 py-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm text-sm font-black">
                {{ currentPage }} <span class="text-slate-400 font-normal mx-1">/</span> {{ Math.ceil(filteredUsers.length / pageSize) }}
              </div>
              <button @click="nextPage" :disabled="currentPage * pageSize >= filteredUsers.length" class="pagination-btn">
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户操作模态框-->
    <Transition name="fade">
      <div v-if="showModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="closeModal"></div>
        <div class="relative bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-md overflow-hidden transition-all">
          <div class="p-8 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/50">
            <h3 class="text-xl font-black text-slate-800 dark:text-white">{{ isEditing ? '编辑成员档案' : '录入新成员' }}</h3>
            <button @click="closeModal" class="text-slate-400 hover:text-rose-500 transition-colors"><i class="fa-solid fa-times text-xl"></i></button>
          </div>
          
          <div class="p-8 max-h-[70vh] overflow-y-auto custom-scrollbar">
            <form @submit.prevent="handleFormSubmit" class="space-y-6">
              <div class="grid grid-cols-2 gap-4">
                <div class="col-span-2">
                  <label class="modal-label">系统账户名 Account <span class="text-rose-500">*</span></label>
                  <input v-model="form.username" type="text" required class="modal-input" placeholder="用于系统登录" />
                </div>
                <div class="col-span-2">
                  <label class="modal-label">电子邮箱 Email <span class="text-rose-500">*</span></label>
                  <input v-model="form.email" type="email" required class="modal-input" placeholder="example@email.com" />
                </div>
                <div class="col-span-2" v-if="!isEditing || showPasswordField">
                  <label class="modal-label">登录密码 Password <span v-if="!isEditing" class="text-rose-500">*</span></label>
                  <input v-model="form.password" type="password" :required="!isEditing" class="modal-input" placeholder="设置强安全密码" />
                </div>
                <div class="col-span-2 flex justify-end" v-else-if="isEditing">
                  <button type="button" @click="showPasswordField = true" class="text-[10px] font-black text-indigo-500 uppercase hover:underline">重置登录密码</button>
                </div>
                <div>
                  <label class="modal-label">手机号 Mobile</label>
                  <input v-model="form.phone" type="tel" class="modal-input" placeholder="11位手机" />
                </div>
                <div>
                  <label class="modal-label">籍贯 Location</label>
                  <input v-model="form.nativePlace" type="text" class="modal-input" placeholder="省/市" />
                </div>
              </div>

              <div>
                <label class="modal-label">授予角色权限 Role Assignment</label>
                <div class="flex gap-4 mt-2">
                  <label class="flex-1 cursor-pointer group">
                    <input type="radio" v-model="form.role" value="user" class="hidden peer" />
                    <div class="p-4 rounded-2xl border-2 border-slate-50 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 peer-checked:border-blue-500 peer-checked:bg-blue-50 dark:peer-checked:bg-blue-900/20 transition-all text-center">
                      <i class="fa-solid fa-user mb-1 text-slate-400 peer-checked:text-blue-500"></i>
                      <p class="text-[10px] font-black uppercase text-slate-500 peer-checked:text-blue-600">General User</p>
                    </div>
                  </label>
                  <label class="flex-1 cursor-pointer group">
                    <input type="radio" v-model="form.role" value="admin" class="hidden peer" />
                    <div class="p-4 rounded-2xl border-2 border-slate-50 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 peer-checked:border-rose-500 peer-checked:bg-rose-50 dark:peer-checked:bg-rose-900/20 transition-all text-center">
                      <i class="fa-solid fa-shield-halved mb-1 text-slate-400 peer-checked:text-rose-500"></i>
                      <p class="text-[10px] font-black uppercase text-slate-500 peer-checked:text-rose-600">Administrator</p>
                    </div>
                  </label>
                </div>
              </div>

              <div class="pt-4 flex gap-3">
                <button type="button" @click="closeModal" class="flex-1 py-3 rounded-2xl font-bold text-slate-400 hover:bg-slate-50 transition-all">取消</button>
                <button type="submit" :disabled="submitting" class="flex-1 py-3 bg-amber-500 text-white rounded-2xl font-black shadow-lg shadow-amber-200 active:scale-95 transition-all flex items-center justify-center gap-2">
                  <i v-if="submitting" class="fa-solid fa-circle-notch fa-spin"></i>
                  <span>{{ submitting ? '同步中...' : '保存成员信息' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </Transition>

    <UserDetailModal v-if="selectedUser" :user="selectedUser" @close="selectedUser = null" @change-role="handleRoleChange" @delete-user="handleDeleteUser" />
  </div>
</template>

<script setup lang="ts">
/* 脚本逻辑完全保留原始实现，仅增加了 ECharts 图表的颜色主题匹配 */
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import UserDetailModal from '@/components/UserDetailModal.vue'
import { getAllUserAPI, deleteUserAPI, registerAPI, updateUserAPI } from '@/api/user'
import type { UserInfo } from '@/api/user'
import { getAvatarUrl } from '@/utils/imageUtils';
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import * as echarts from 'echarts/core'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const users = ref<UserInfo[]>([])
const loading = ref(false)
const errorMessage = ref('')
const submitting = ref(false)
const userStats = ref({ total: 0, newToday: 0, admins: 0, activeToday: 0 })
const searchQuery = ref('')
const roleFilter = ref('all')
const sortBy = ref('registrationDate')
const currentPage = ref(1)
const pageSize = ref(10)

watch([searchQuery, roleFilter, sortBy], () => { currentPage.value = 1 })

const showModal = ref(false)
const isEditing = ref(false)
const showPasswordField = ref(false)
const selectedUser = ref<UserInfo | null>(null)
const form = ref({ userId: 0, username: '', email: '', password: '', phone: '', nativePlace: '', role: 'user', avatarUrl: '' })

// 注册趋势：应用系统配色
const registrationTrendOption = computed(() => {
  const last7Days = [...Array(7)].map((_, i) => {
    const d = new Date(); d.setDate(d.getDate() - (6 - i));
    return d.toLocaleDateString();
  });
  const data = last7Days.map(dateStr => users.value.filter(u => u.registerTime && new Date(u.registerTime).toLocaleDateString() === dateStr).length);
  return {
    grid: { left: '3%', right: '5%', bottom: '5%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: last7Days.map(d => d.split('/')[2]), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#94a3b8', fontSize: 10, fontWeight: 'bold' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } }, axisLabel: { color: '#94a3b8' } },
    series: [{
      data: data, type: 'line', smooth: true, symbol: 'circle', symbolSize: 8,
      lineStyle: { width: 4, color: '#6366f1', shadowBlur: 10, shadowColor: 'rgba(99, 102, 241, 0.3)' },
      itemStyle: { color: '#6366f1', borderWidth: 2, borderColor: '#fff' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(99, 102, 241, 0.2)' }, { offset: 1, color: 'rgba(99, 102, 241, 0)' }]) }
    }]
  };
});

const roleDistributionOption = computed(() => ({
  tooltip: { trigger: 'item', backgroundColor: 'rgba(255, 255, 255, 0.9)' },
  legend: { bottom: '0', icon: 'circle', textStyle: { fontSize: 10, fontWeight: 'bold', color: '#64748b' }, itemGap: 15 },
  series: [{
    type: 'pie', radius: ['60%', '85%'], center: ['50%', '42%'], avoidLabelOverlap: true,
    itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    data: [
      { value: userStats.value.admins, name: '管理员', itemStyle: { color: '#f43f5e' } },
      { value: Math.max(0, userStats.value.total - userStats.value.admins), name: '注册用户', itemStyle: { color: '#6366f1' } }
    ]
  }]
}));

const getTinyLineOption = (colorClass: string) => {
  const color = colorClass.includes('indigo') ? '#6366f1' : colorClass.includes('green') ? '#10b981' : colorClass.includes('red') ? '#f43f5e' : '#f59e0b';
  return {
    grid: { left: 0, right: 0, top: 0, bottom: 0 },
    xAxis: { type: 'category', show: false, boundaryGap: false },
    yAxis: { type: 'value', show: false, min: 'dataMin' },
    series: [{
      data: [30, 40, 35, 50, 45, 60, 55, 70], type: 'line', smooth: true, symbol: 'none',
      lineStyle: { width: 2, color: color },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: `${color}44` }, { offset: 1, color: `${color}00` }]) }
    }]
  };
};

const statsCards = computed(() => [
  { label: '用户总量 Registry', value: userStats.value.total, icon: 'fa-users', bgClass: 'bg-indigo-50 dark:bg-indigo-900/30', textClass: 'text-indigo-600' },
  { label: '今日新增 Newcomer', value: userStats.value.newToday, icon: 'fa-user-plus', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600' },
  { label: '管理层级 Admin', value: userStats.value.admins, icon: 'fa-user-shield', bgClass: 'bg-rose-50 dark:bg-rose-900/30', textClass: 'text-rose-600' },
  { label: '活跃热度 Active', value: userStats.value.activeToday, icon: 'fa-bolt', bgClass: 'bg-amber-50 dark:bg-amber-900/30', textClass: 'text-amber-600' },
])

const loadUsers = async () => {
  loading.value = true;
  try {
    const res = await getAllUserAPI();
    users.value = Array.isArray(res) ? res : (res as any).data || [];
    calculateStats();
  } catch (e: any) { errorMessage.value = e.message } finally { loading.value = false }
}

const calculateStats = () => {
  const today = new Date().setHours(0,0,0,0);
  userStats.value = {
    total: users.value.length,
    newToday: users.value.filter(u => u.registerTime && new Date(u.registerTime).setHours(0,0,0,0) === today).length,
    admins: users.value.filter(u => u.role === 'admin').length,
    activeToday: users.value.filter(u => u.lastLoginTime && new Date(u.lastLoginTime).setHours(0,0,0,0) === today).length
  }
}

const isUserOnline = (lastLogin?: string) => lastLogin ? (Date.now() - new Date(lastLogin).getTime()) < 10 * 60 * 1000 : false;

const filteredUsers = computed(() => {
  let list = users.value.filter(u => {
    const q = searchQuery.value.toLowerCase()
    return (u.username.toLowerCase().includes(q) || u.email.toLowerCase().includes(q) || u.phone?.includes(q))
      && (roleFilter.value === 'all' || u.role === roleFilter.value)
  })
  list.sort((a, b) => {
    if (sortBy.value === 'username') return a.username.localeCompare(b.username)
    const timeA = new Date(a[sortBy.value === 'lastLogin' ? 'lastLoginTime' : 'registerTime'] || 0).getTime()
    const timeB = new Date(b[sortBy.value === 'lastLogin' ? 'lastLoginTime' : 'registerTime'] || 0).getTime()
    return timeB - timeA
  })
  return list
})

const paginatedUsers = computed(() => filteredUsers.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

const openAddUserModal = () => { isEditing.value = false; showPasswordField.value = true; form.value = { userId: 0, username: '', email: '', password: '', phone: '', nativePlace: '', role: 'user', avatarUrl: '' }; showModal.value = true; }
const editUser = (user: UserInfo) => { isEditing.value = true; showPasswordField.value = false; form.value = { ...user, password: '' }; showModal.value = true; }
const closeModal = () => { showModal.value = false }

const handleFormSubmit = async () => {
  submitting.value = true
  try {
    if (isEditing.value) {
      const updateData = { ...form.value }; if (!showPasswordField.value || !updateData.password) delete (updateData as any).password;
      await updateUserAPI(updateData);
    } else { await registerAPI(form.value); }
    await loadUsers(); closeModal();
  } catch (e: any) { alert(e.message) } finally { submitting.value = false }
}

const deleteUser = async (user: UserInfo) => {
  if (confirm(`确认注销用户 ${user.username} 吗？`)) {
    try { await deleteUserAPI(user.userId); loadUsers(); } catch (e) { alert('失败') }
  }
}

const formatDate = (str?: string) => str ? new Date(str).toLocaleDateString() : '-'
const formatLastLoginTime = (str?: string) => {
  if (!str) return 'Never Logged In';
  const date = new Date(str); const diff = Date.now() - date.getTime();
  if (diff < 3600000) return 'Just Now';
  if (diff < 86400000) return 'Today';
  return date.toLocaleDateString();
}

// 使用统一的头像处理函数
const getUserAvatarUrl = (avatarUrl?: string) => {
  return getAvatarUrl(avatarUrl || '');
};

const handleAvatarError = (e: Event) => { 
  const img = e.target as HTMLImageElement;
  img.style.display = 'none'; 
  // 显示默认头像（用户名首字母）
  const parent = img.parentElement;
  if (parent) {
    const fallback = parent.querySelector('span');
    if (fallback) {
      fallback.style.display = 'flex';
    }
  }
}
const viewUserDetail = (u: UserInfo) => { selectedUser.value = u }
const handleRoleChange = () => { loadUsers() }
const handleDeleteUser = () => { if(selectedUser.value) deleteUser(selectedUser.value); selectedUser.value = null }
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value * pageSize.value < filteredUsers.value.length) currentPage.value++ }

onMounted(loadUsers)
</script>

<style scoped>
.filter-select {
  @apply bg-slate-50 dark:bg-slate-800 border-none rounded-2xl px-5 py-3 text-sm font-bold text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-all;
}

.row-action-btn {
  @apply w-8 h-8 rounded-xl bg-slate-50 dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-white hover:rotate-6 transition-all duration-300;
}

.pagination-btn {
  @apply w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 transition-all shadow-sm;
}

.modal-label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1.5 block ml-1;
}

.modal-input {
  @apply w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-sm font-bold text-slate-800 dark:text-white focus:ring-2 focus:ring-indigo-500/50 transition-all;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }
</style>