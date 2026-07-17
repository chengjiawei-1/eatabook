<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <div class="space-y-6"></div>
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题与顶部操作 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              古籍数据管理 <span class="text-amber-500 text-base ml-2">Archives</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              管理古籍库基础信息，系统状态：<span class="text-emerald-500 font-bold">运行良好</span>
            </p>
          </div>
          <div class="flex gap-3">
            <button 
              @click="openImportModal"
              class="px-5 py-2.5 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-2xl shadow-lg shadow-amber-200 dark:shadow-none transition-all flex items-center gap-2 active:scale-95"
            >
              <i class="fa-solid fa-file-import"></i>
              <span>导入古籍数据</span>
            </button>
          </div>
        </div>

        <!-- 数据统计卡片 (统一控制台风格) -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div v-for="(stat, index) in statsCards" :key="index" 
            class="bg-white dark:bg-slate-900 rounded-3xl p-5 border border-white dark:border-slate-800 shadow-xl shadow-slate-200/50 dark:shadow-none transition-all hover:-translate-y-1 relative group overflow-hidden">
            <div class="flex justify-between items-center relative z-10">
              <div>
                <p class="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1">{{ stat.label }}</p>
                <h3 class="text-2xl font-black text-slate-800 dark:text-white">{{ stat.value }}</h3>
              </div>
              <div :class="['w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg', stat.bgClass, stat.textClass]">
                <i :class="['fa-solid', stat.icon, 'text-xl']"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选工具栏 (参考控制台搜索风格) -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none p-6 border border-white dark:border-slate-800 mb-6">
          <div class="flex flex-col lg:flex-row gap-4 items-center">
            <!-- 搜索框 -->
            <div class="w-full lg:flex-1 relative">
              <i class="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索名称、作者、朝代..."
                class="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800/50 border-none rounded-2xl text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 transition-all font-bold"
              />
            </div>
            
            <div class="flex flex-wrap gap-3 w-full lg:w-auto">
              <select v-model="statusFilter" class="filter-select">
                <option value="all">全部状态</option>
                <option value="published">已发布</option>
                <option value="draft">草稿箱</option>
              </select>
              
              <select v-model="eraFilter" class="filter-select">
                <option value="all">全部朝代</option>
                <option v-for="era in availableEras" :key="era" :value="era">{{ era }}</option>
              </select>
              
              <select v-model="sortBy" class="filter-select">
                <option value="createdAt">最新录入</option>
                <option value="views">访问热度</option>
                <option value="entryCount">条目数量</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 批量操作工具栏 (完全保留您的功能按钮) -->
        <Transition name="fade-slide">
          <div v-if="selectedBookIds.length > 0" class="bg-indigo-600 rounded-3xl shadow-xl p-4 mb-6 flex flex-col sm:flex-row items-center justify-between gap-4 animate-fade-in">
            <div class="flex items-center gap-3">
              <span class="px-3 py-1 bg-white/20 rounded-lg text-white text-xs font-black uppercase tracking-widest">
                已选 {{ selectedBookIds.length }} 项
              </span>
              <button 
                @click="batchPublishSelected"
                :disabled="batchLoading"
                class="px-4 py-2 bg-emerald-500 hover:bg-emerald-600 disabled:bg-emerald-300 text-white rounded-xl text-sm font-bold shadow-lg transition-all flex items-center gap-2"
              >
                <i :class="['fa-solid', batchLoading ? 'fa-spinner fa-spin' : 'fa-upload']"></i>
                批量发布
              </button>
              
              <button 
                @click="batchDeleteSelected"
                :disabled="batchLoading"
                class="px-4 py-2 bg-rose-500 hover:bg-rose-600 disabled:bg-rose-300 text-white rounded-xl text-sm font-bold shadow-lg transition-all flex items-center gap-2"
              >
                <i :class="['fa-solid', batchLoading ? 'fa-spinner fa-spin' : 'fa-trash']"></i>
                批量删除
              </button>
              
              <button 
                @click="clearSelection"
                class="px-3 py-2 text-white/70 hover:text-white text-xs font-black uppercase tracking-widest transition-colors"
              >
                清空选择
              </button>
            </div>
            <p class="text-white/80 text-xs font-bold italic">请谨慎执行批量操作</p>
          </div>
        </Transition>

        <!-- 数据表格 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden">
          <div v-if="loading" class="py-24 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500 font-bold">正在加载数据...</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse table-fixed min-w-[950px]">
              <thead>
                <tr class="bg-slate-50/50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 text-slate-400 text-[11px] font-black uppercase tracking-widest">
                  <th class="px-6 py-4 w-[60px] text-center">
                    <input 
                      type="checkbox" 
                      :checked="isAllSelected" 
                      @change="toggleSelectAll"
                      class="rounded border-slate-300 text-amber-500 focus:ring-amber-500"
                    >
                  </th>
                  <th class="px-6 py-4 w-[22%]">古籍信息</th>
                  <th class="px-6 py-4 w-[12%]">作者</th>
                  <th class="px-6 py-4 w-[10%]">朝代</th>
                  <th class="px-6 py-4 w-[15%]">数据统计</th>
                  <th class="px-6 py-4 w-[10%] text-center">状态</th>
                  <th class="px-6 py-4 w-[18%] text-right">操作</th>
                </tr>
              </thead>

              <tbody class="divide-y divide-slate-50 dark:divide-slate-800">
                <tr 
                  v-for="book in paginatedBooks" 
                  :key="book.id"
                  class="group hover:bg-slate-50/80 dark:hover:bg-slate-800/50 transition-all"
                >
                  <td class="px-6 py-4 text-center">
                    <input 
                      type="checkbox" 
                      :checked="isBookSelected(book.id)"
                      @change="toggleBookSelection(book.id)"
                      :disabled="book.status === 'published'"
                      class="rounded border-slate-300 text-amber-500 focus:ring-amber-500 disabled:opacity-30 disabled:cursor-not-allowed"
                    >
                  </td>
                  
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                      <div class="w-10 h-10 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center text-amber-600 dark:text-amber-400 font-black text-lg">
                        {{ book.title.charAt(0) }}
                      </div>
                      <div class="overflow-hidden">
                        <div class="font-black text-slate-800 dark:text-slate-200 truncate text-sm" :title="book.title">{{ book.title }}</div>
                        <div class="text-[10px] text-slate-400 truncate max-w-[200px] font-bold">{{ book.description || '暂无简介' }}</div>
                      </div>
                    </div>
                  </td>

                  <td class="px-6 py-4 align-middle">
                    <span class="text-sm font-bold text-slate-700 dark:text-slate-300">{{ book.author || '佚名' }}</span>
                  </td>

                  <td class="px-6 py-4 align-middle">
                     <span class="px-2.5 py-1 rounded-lg text-[10px] font-black bg-slate-100 dark:bg-slate-800 text-slate-500 border border-slate-200 dark:border-slate-700 uppercase tracking-tighter">
                      {{ book.era || '未知' }}
                    </span>
                  </td>

                  <td class="px-6 py-4 align-middle">
                    <div class="flex flex-col gap-1 text-[10px] font-black text-slate-400 uppercase">
                      <span class="flex items-center gap-1.5"><i class="fa-solid fa-list-ul text-amber-500/70"></i> {{ book.entryCount }} 条目</span>
                      <span class="flex items-center gap-1.5"><i class="fa-regular fa-eye text-blue-500/70"></i> {{ book.views }} 浏览</span>
                    </div>
                  </td>

                  <td class="px-6 py-4 text-center align-middle">
                    <span :class="[
                      'px-2 py-0.5 rounded-lg text-[10px] font-black uppercase border',
                      book.status === 'published' 
                        ? 'bg-emerald-50 text-emerald-600 border-emerald-100 dark:bg-emerald-900/20'
                        : 'bg-amber-50 text-amber-600 border-amber-100 dark:bg-amber-900/20'
                    ]">
                      {{ book.status === 'published' ? '已发布' : '草稿' }}
                    </span>
                  </td>

                  <td class="px-6 py-4 text-right align-middle">
                    <div class="flex items-center justify-end gap-2">
                      <button v-if="book.status === 'draft'" @click="handlePublishBook(book)" class="action-btn hover:bg-emerald-500" title="发布">
                        <i class="fa-solid fa-upload"></i>
                      </button>
                      <button @click="editBook(book)" class="action-btn hover:bg-indigo-500" title="编辑">
                        <i class="fa-solid fa-pen"></i>
                      </button>
                      <button @click="viewBookDetail(book)" class="action-btn hover:bg-amber-500" title="卷宗">
                        <i class="fa-solid fa-eye"></i>
                      </button>
                      <button @click="deleteBook(book)" class="action-btn hover:bg-rose-500" title="销毁">
                        <i class="fa-solid fa-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页栏 -->
          <div class="px-8 py-6 bg-slate-50/50 dark:bg-slate-800/30 flex items-center justify-between">
            <span class="text-[11px] font-black text-slate-400 uppercase tracking-widest">
              Total {{ filteredBooks.length }} Records
            </span>
            <div class="flex items-center gap-2">
              <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <div class="px-4 py-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-black text-slate-800 dark:text-white">
                {{ currentPage }} / {{ Math.ceil(filteredBooks.length / pageSize) }}
              </div>
              <button @click="nextPage" :disabled="currentPage * pageSize >= filteredBooks.length" class="pagination-btn">
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

    <!-- 导入/编辑模态框 (样式更新，逻辑不动) -->
    <Transition name="fade">
      <div v-if="showImportModal || showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showImportModal = false; showEditModal = false"></div>
        <div class="relative bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-lg overflow-hidden transition-all">
          <div class="p-8 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/50">
            <h3 class="text-xl font-black text-slate-800 dark:text-white">{{ showImportModal ? '导入卷宗' : '编辑古籍' }}</h3>
            <button @click="showImportModal = false; showEditModal = false" class="text-slate-400 hover:text-rose-500 transition-colors">
              <i class="fa-solid fa-times text-xl"></i>
            </button>
          </div>
          
          <div class="p-8">
            <!-- 导入内容... -->
            <div v-if="showImportModal" class="space-y-6">
              <div class="p-6 bg-indigo-50 rounded-3xl border border-indigo-100">
                <h4 class="text-indigo-900 font-black mb-2 flex items-center gap-2 text-sm"><i class="fa-solid fa-circle-info"></i> Excel格式提示</h4>
                <div class="grid grid-cols-2 gap-x-4 gap-y-1 text-[11px] text-indigo-700 font-bold">
                  <span>1.古籍名称</span> <span>2.朝代</span>
                  <span>3.作者</span> <span>4.分类</span>
                  <span>5.简介</span> <span>6.条目数</span>
                </div>
              </div>
              <div 
                class="border-4 border-dashed border-slate-100 rounded-[2rem] p-10 text-center hover:border-amber-400 transition-all cursor-pointer bg-slate-50"
                @click="$refs.fileInput.click()"
              >
                <input type="file" ref="fileInput" accept=".xlsx,.xls" class="hidden" @change="handleFileSelect" />
                <i class="fa-solid fa-cloud-arrow-up text-4xl text-slate-300 mb-4"></i>
                <p class="text-sm font-black text-slate-600">{{ selectedFile ? selectedFile.name : '点击选择或拖拽Excel文件' }}</p>
              </div>
            </div>

            <!-- 编辑内容... -->
            <div v-if="showEditModal" class="grid grid-cols-2 gap-4">
              <div class="col-span-2">
                <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1 block">古籍名称</label>
                <input v-model="editForm.bookName" class="modal-input" />
              </div>
              <div><label class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1 block">作者</label><input v-model="editForm.author" class="modal-input" /></div>
              <div><label class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1 block">朝代</label><input v-model="editForm.dynasty" class="modal-input" /></div>
              <div class="col-span-2"><label class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1 block">简介</label><textarea v-model="editForm.intro" class="modal-input h-24 pt-2"></textarea></div>
            </div>
          </div>

          <div class="p-8 bg-slate-50 flex justify-end gap-3">
            <button @click="showImportModal = false; showEditModal = false" class="px-6 py-2.5 rounded-xl font-bold text-slate-400">取消</button>
            <button v-if="showImportModal" @click="handleImport" :disabled="importing || !selectedFile" class="px-8 py-2.5 rounded-xl bg-amber-500 text-white font-black shadow-lg shadow-amber-200">开始导入</button>
            <button v-if="showEditModal" @click="handleUpdateBook" :disabled="updating" class="px-8 py-2.5 rounded-xl bg-indigo-500 text-white font-black shadow-lg shadow-indigo-200">更新卷宗</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 古籍详情模态框逻辑保持原样，仅微调圆角 -->
    <Transition name="fade">
      <div v-if="showDetailModal" class="fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-md bg-[#3d2b1f]/30">
        <div class="absolute inset-0" @click="showDetailModal = false"></div>
        <div class="relative bg-[#fdfbf8] dark:bg-slate-900 rounded-[3rem] shadow-3xl w-full max-w-5xl overflow-hidden transform transition-all max-h-[92vh] border border-[#e8dec9]">
          <!-- 头部装饰栏 -->
          <div class="bg-[#5d3a1a] p-8 text-white flex justify-between items-center border-b-[6px] border-[#c19a6b]">
            <div class="flex items-center gap-4">
              <div class="w-10 h-10 rounded-full bg-[#c19a6b]/20 flex items-center justify-center border border-white/20">
                <i class="fa-solid fa-scroll text-[#deb887]"></i>
              </div>
              <div>
                <h3 class="text-2xl font-serif font-black tracking-widest uppercase">古籍详情 · 卷宗</h3>
                <p class="text-[10px] text-stone-300/60 font-mono tracking-widest uppercase">Archives Management</p>
              </div>
            </div>
            <button @click="showDetailModal = false" class="w-12 h-12 rounded-full hover:bg-white/10 transition-colors flex items-center justify-center group">
              <i class="fa-solid fa-times text-xl text-stone-300 group-hover:text-white"></i>
            </button>
          </div>
          
          <div class="p-10 overflow-y-auto max-h-[calc(92vh-120px)]">
             <!-- 内容区域完全复用您的 detailBook 代码结构... -->
             <div class="grid grid-cols-1 lg:grid-cols-12 gap-12">
               <div class="lg:col-span-4 space-y-8">
                  <div class="relative aspect-[3/4] bg-white rounded-xl shadow-2xl border-l-[12px] border-[#8b4513] overflow-hidden group">
                    <div class="absolute inset-0 opacity-[0.05] pointer-events-none" style="background-image: url('https://www.transparenttextures.com/patterns/paper-fibers.png');"></div>
                    <div class="h-full flex flex-col items-center justify-center p-6 border-2 border-[#e8dec9] m-2 rounded-sm">
                       <div class="[writing-mode:vertical-rl] text-4xl font-serif font-black text-[#5d3a1a] tracking-[0.5em] mb-4">{{ detailBook?.bookName }}</div>
                       <div class="h-16 w-px bg-[#8b4513]/20 mb-4"></div>
                       <div class="text-sm font-serif text-[#8b4513]">{{ detailBook?.author || '佚名' }} 著</div>
                    </div>
                  </div>
               </div>
               <div class="lg:col-span-8 space-y-10">
                  <div class="relative"><i class="fa-solid fa-quote-left text-4xl text-[#c19a6b]/20 absolute -top-4 -left-4"></i>
                    <div class="pl-8">
                      <h4 class="text-lg font-serif font-black text-[#5d3a1a] mb-4 flex items-center gap-2">卷首语</h4>
                      <p class="text-[#5d3a1a] leading-loose font-serif text-lg text-justify bg-[#fdfaf2] p-6 rounded-2xl border border-stone-100 italic">{{ detailBook?.intro || '此典籍暂无详细内容描述。' }}</p>
                    </div>
                  </div>
                  <div class="grid grid-cols-3 gap-6">
                    <div class="bg-white p-6 rounded-3xl border border-[#f4efe1] shadow-sm text-center">
                      <div class="text-[10px] text-stone-400 font-black uppercase">食养条目</div>
                      <div class="text-3xl font-serif font-black text-[#8b4513]">{{ detailBook?.nourishCount || 0 }}</div>
                    </div>
                    <div class="bg-white p-6 rounded-3xl border border-[#f4efe1] shadow-sm text-center">
                      <div class="text-[10px] text-stone-400 font-black uppercase">累计阅览</div>
                      <div class="text-3xl font-serif font-black text-[#8b4513]">{{ detailBook?.viewCount || 0 }}</div>
                    </div>
                    <div class="bg-white p-6 rounded-3xl border border-[#f4efe1] shadow-sm text-center">
                      <div class="text-[10px] text-stone-400 font-black uppercase tracking-tight">更新时间</div>
                      <div class="text-sm font-serif font-black text-[#8b4513] pt-2">{{ formatDate(detailBook?.updateTime) }}</div>
                    </div>
                  </div>
                  <div class="pt-6 border-t border-[#e8dec9] flex flex-col sm:flex-row gap-4">
                    <button @click="handleEditFromDetail" class="flex-1 px-8 py-4 bg-white border-2 border-[#8b4513] text-[#8b4513] rounded-2xl font-bold hover:bg-[#8b4513] hover:text-white transition-all flex items-center justify-center gap-3 shadow-lg shadow-orange-900/5"><i class="fa-solid fa-pen-nib"></i> 编辑古籍</button>
                    <button v-if="detailBook?.status === 0" @click="handlePublishFromDetail" class="flex-1 px-8 py-4 bg-[#8b4513] text-white rounded-2xl font-bold hover:bg-[#5d3a1a] transition-all flex items-center justify-center gap-3 shadow-lg shadow-orange-900/20"><i class="fa-solid fa-upload"></i> 发布古籍</button>
                    <button @click="handleDeleteFromDetail" class="px-8 py-4 bg-red-50 text-red-600 rounded-2xl font-bold hover:bg-red-600 hover:text-white transition-all flex items-center justify-center gap-3 border border-red-100"><i class="fa-solid fa-trash-can"></i> 删除古籍</button>
                  </div>
               </div>
             </div>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<style scoped>
.filter-select {
  @apply bg-slate-50 dark:bg-slate-800 border-none rounded-2xl px-5 py-3 text-sm font-bold text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-all;
}

.action-btn {
  @apply w-8 h-8 rounded-xl bg-slate-50 dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-white hover:rotate-6 transition-all duration-300;
}

.pagination-btn {
  @apply w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 disabled:hover:bg-white transition-all shadow-sm;
}

.modal-input {
  @apply w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-xl text-sm font-bold text-slate-800 dark:text-white focus:ring-2 focus:ring-amber-500/50;
}

.fade-enter-active, .fade-slide-enter-active { transition: all 0.3s ease; }
.fade-enter-from, .fade-slide-enter-from { opacity: 0; transform: translateY(10px); }
</style>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import { getAllBooks, AncientBook, deleteBookById, importBook, publishBook, updateBookById } from '@/api/ancientBooks'
import { getAllDishes } from '@/api/dishes'

// --- 脚本逻辑：完全保留您的原始实现 ---
const loading = ref(false)
const showImportModal = ref(false)
const importing = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const showEditModal = ref(false)
const updating = ref(false)
const showDetailModal = ref(false)
const detailBook = ref<AncientBook | null>(null)

const bookStats = ref({
  totalBooks: 0,
  totalViews: 0,
  totalDishes: 0,
  draftBooks: 0
})

const editForm = ref({
  bookId: '',
  bookName: '',
  author: '',
  dynasty: '',
  bookCategory: '',
  intro: '',
  bookCoverUrl: '',
  nourishCount: 0
})

const searchQuery = ref('')
const statusFilter = ref('all')
const eraFilter = ref('all')
const sortBy = ref('createdAt')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedBookIds = ref<string[]>([])
const batchLoading = ref(false)

watch([searchQuery, statusFilter, eraFilter, sortBy], () => { currentPage.value = 1 })

const isBookSelected = (bookId: string) => selectedBookIds.value.includes(bookId)
const toggleBookSelection = (bookId: string) => {
  const book = books.value.find(b => b.id === bookId)
  if (book && book.status === 'published') return
  const index = selectedBookIds.value.indexOf(bookId)
  if (index > -1) selectedBookIds.value.splice(index, 1)
  else selectedBookIds.value.push(bookId)
}
const toggleSelectAll = () => {
  if (isAllSelected.value) selectedBookIds.value = []
  else selectedBookIds.value = paginatedBooks.value.filter(book => book.status !== 'published').map(book => book.id)
}
const isAllSelected = computed(() => {
  const draftBooks = paginatedBooks.value.filter(book => book.status !== 'published')
  return draftBooks.length > 0 && draftBooks.every(book => selectedBookIds.value.includes(book.id))
})
const clearSelection = () => selectedBookIds.value = []

const batchPublishSelected = async () => {
  if (selectedBookIds.value.length === 0) return alert('请先选择要发布的古籍')
  if (!confirm(`确定要批量发布选中的 ${selectedBookIds.value.length} 本古籍吗？`)) return
  batchLoading.value = true; let successCount = 0; let failCount = 0
  for (const bookId of selectedBookIds.value) {
    try {
      const success = await publishBook(bookId)
      if (success) { successCount++; const book = books.value.find(b => b.id === bookId); if (book) book.status = 'published' }
      else failCount++
    } catch { failCount++ }
  }
  bookStats.value.draftBooks = Math.max(0, bookStats.value.draftBooks - successCount)
  alert(`批量发布完成！成功 ${successCount} 本，失败 ${failCount} 本`); clearSelection(); batchLoading.value = false
}

const batchDeleteSelected = async () => {
  if (selectedBookIds.value.length === 0) return alert('请先选择要删除的古籍')
  if (!confirm(`确定要批量删除选中的 ${selectedBookIds.value.length} 本古籍吗？`)) return
  batchLoading.value = true; let successCount = 0; let failCount = 0
  for (const bookId of selectedBookIds.value) {
    try { const success = await deleteBookById(bookId); if (success) successCount++; else failCount++ } catch { failCount++ }
  }
  alert(`批量删除完成！成功 ${successCount} 本，失败 ${failCount} 本`); await loadBooks(); clearSelection(); batchLoading.value = false
}

const books = ref<Book[]>([])
const availableEras = ref<string[]>([])
interface Book { id: string; title: string; description: string; author: string; era: string; entryCount: number; views: number; status: string; coverUrl?: string; createdAt?: string; category?: string; }

const statsCards = computed(() => [
  { label: '总古籍收录', value: bookStats.value.totalBooks, icon: 'fa-book', bgClass: 'bg-indigo-50 dark:bg-indigo-900/30', textClass: 'text-indigo-600' },
  { label: '累计访问量', value: bookStats.value.totalViews, icon: 'fa-eye', bgClass: 'bg-blue-50 dark:bg-blue-900/30', textClass: 'text-blue-600' },
  { label: '关联食疗方', value: bookStats.value.totalDishes, icon: 'fa-utensils', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600' },
  { label: '草稿箱存量', value: bookStats.value.draftBooks, icon: 'fa-file-pen', bgClass: 'bg-amber-50 dark:bg-amber-900/30', textClass: 'text-amber-600' },
])

const openImportModal = () => { showImportModal.value = true; selectedFile.value = null }
const handleFileSelect = (event: Event) => { const target = event.target as HTMLInputElement; if (target.files && target.files.length > 0) selectedFile.value = target.files[0] }
const handleImport = async () => {
  if (!selectedFile.value) return; importing.value = true
  try { if (await importBook(selectedFile.value)) { alert('古籍导入成功！'); showImportModal.value = false; await loadBooks() } } 
  catch (e: any) { alert(`导入出错: ${e.message}`) } finally { importing.value = false }
}

const loadBooks = async () => {
  loading.value = true
  try {
    const [apiBooks, dishes] = await Promise.all([getAllBooks(), getAllDishes()])
    books.value = apiBooks.map((b: AncientBook) => ({
      id: b.bookId, title: b.bookName, description: b.intro, author: b.author, era: b.dynasty,
      entryCount: b.nourishCount || 0, views: b.viewCount || 0, status: b.status === 1 ? 'published' : 'draft',
      coverUrl: b.bookCoverUrl, createdAt: b.updateTime, category: b.bookCategory
    }))
    const totalViews = books.value.reduce((sum, b) => sum + (b.views || 0), 0)
    bookStats.value = { totalBooks: books.value.length, totalViews, totalDishes: dishes.length, draftBooks: books.value.filter(b => b.status === 'draft').length }
    const eras = new Set<string>(); books.value.forEach(b => { if(b.era) eras.add(b.era) }); availableEras.value = Array.from(eras).sort()
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const filteredBooks = computed(() => {
  let list = books.value.filter(b => {
    const searchLower = searchQuery.value.toLowerCase()
    const matchesSearch = b.title.toLowerCase().includes(searchLower) || b.author.toLowerCase().includes(searchLower)
    const matchesStatus = statusFilter.value === 'all' || b.status === statusFilter.value
    const matchesEra = eraFilter.value === 'all' || b.era === eraFilter.value
    return matchesSearch && matchesStatus && matchesEra
  })
  if (sortBy.value === 'views') list.sort((a, b) => b.views - a.views)
  else if (sortBy.value === 'entryCount') list.sort((a, b) => b.entryCount - a.entryCount)
  else list.sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
  return list
})

const paginatedBooks = computed(() => { const start = (currentPage.value - 1) * pageSize.value; return filteredBooks.value.slice(start, start + pageSize.value) })
const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value * pageSize.value < filteredBooks.value.length) currentPage.value++ }

const editBook = (book: Book) => { editForm.value = { bookId: book.id, bookName: book.title, author: book.author, dynasty: book.era, bookCategory: book.category || '', intro: book.description, bookCoverUrl: book.coverUrl || '', nourishCount: book.entryCount }; showEditModal.value = true }
const handleUpdateBook = async () => {
  if (!editForm.value.bookId) return; updating.value = true
  try { if (await updateBookById(editForm.value.bookId, editForm.value)) { alert('更新成功！'); showEditModal.value = false; await loadBooks() } } 
  catch { alert('更新失败') } finally { updating.value = false }
}

const deleteBook = async (book: Book) => { if (confirm(`确定删除《${book.title}》吗？`)) { try { if (await deleteBookById(book.id)) { await loadBooks(); alert('已删除') } } catch { alert('操作失败') } } }
const handlePublishBook = async (book: Book) => { if(confirm(`确认发布《${book.title}》吗？`)) { try { if (await publishBook(book.id)) { book.status = 'published'; bookStats.value.draftBooks = Math.max(0, bookStats.value.draftBooks - 1) } } catch { alert('发布失败') } } }
const viewBookDetail = (book: Book) => { detailBook.value = { bookId: book.id, bookName: book.title, author: book.author, dynasty: book.era, bookCategory: book.category || '', intro: book.description, bookCoverUrl: book.coverUrl || '', nourishCount: book.entryCount, viewCount: book.views, status: book.status === 'published' ? 1 : 0, updateTime: book.createdAt }; showDetailModal.value = true }

const handleEditFromDetail = () => { if (detailBook.value) { editBook({ id: detailBook.value.bookId, title: detailBook.value.bookName, author: detailBook.value.author, era: detailBook.value.dynasty, category: detailBook.value.bookCategory, description: detailBook.value.intro, coverUrl: detailBook.value.bookCoverUrl || '', entryCount: detailBook.value.nourishCount, views: detailBook.value.viewCount, status: detailBook.value.status === 1 ? 'published' : 'draft' } as Book); showDetailModal.value = false } }
const handlePublishFromDetail = async () => { if (detailBook.value && confirm(`确定发布吗？`)) { try { if (await publishBook(detailBook.value.bookId)) { alert('已发布'); showDetailModal.value = false; await loadBooks() } } catch { alert('发布失败') } } }
const handleDeleteFromDetail = async () => { if (detailBook.value && confirm(`确定删除吗？`)) { try { if (await deleteBookById(detailBook.value.bookId)) { alert('已删除'); showDetailModal.value = false; await loadBooks() } } catch { alert('删除失败') } } }

const formatDate = (dateString?: string) => { if (!dateString) return '未知'; const date = new Date(dateString); return date.toLocaleDateString('zh-CN') }

onMounted(() => loadBooks())
</script>