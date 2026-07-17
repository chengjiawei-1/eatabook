<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              食材库管理 <span class="text-emerald-500 text-base ml-2">Ingredients</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              管理药食同源食材，当前库存：<span class="text-emerald-500 font-bold">{{ ingredientStats.totalIngredients }}</span> 种
            </p>
          </div>
          <button 
            @click="showImportModal = true"
            class="px-5 py-2.5 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-2xl shadow-lg shadow-amber-200 dark:shadow-none transition-all flex items-center gap-2 active:scale-95"
          >
            <i class="fa-solid fa-file-import"></i>
            <span>导入食材卷宗</span>
          </button>
        </div>

        <!-- 统计卡片 -->
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

        <!-- 筛选工具栏 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none p-6 border border-white dark:border-slate-800 mb-6">
          <div class="flex flex-col lg:flex-row gap-4 items-center">
            <div class="w-full lg:flex-1 relative">
              <i class="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索食材、别名、功效..."
                class="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800/50 border-none rounded-2xl text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-emerald-500/50 transition-all font-bold"
              />
            </div>
            <div class="flex flex-wrap gap-3 w-full lg:w-auto">
              <select v-model="propertyFilter" class="filter-select">
                <option value="all">全部性质</option>
                <option value="温">温热 🔥</option>
                <option value="凉">寒凉 ❄️</option>
                <option value="平">平性 🍃</option>
              </select>
              <select v-model="categoryFilter" class="filter-select">
                <option value="all">全部类别</option>
                <option v-for="cat in ingredientCategoryOptions" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 食材列表表格 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden">
          <div v-if="loading" class="py-24 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-emerald-500 mb-4"></i>
            <p class="text-slate-500 font-bold">正在调取本草经注...</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[1100px]">
              <thead>
                <tr class="bg-slate-50/50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 text-slate-400 text-[11px] font-black uppercase tracking-widest">
                  <th class="px-8 py-4 w-[20%]">食材信息</th>
                  <th class="px-6 py-4 w-[20%]">性味特质 Nature</th>
                  <th class="px-6 py-4 w-[12%] text-center">分类</th>
                  <th class="px-6 py-4 w-[30%]">核心功效概览</th>
                  <th class="px-8 py-4 w-[18%] text-right">操作管理</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50 dark:divide-slate-800">
                <tr v-for="item in paginatedIngredients" :key="item.ingredientId" class="group hover:bg-slate-50/80 transition-all">
                  <td class="px-8 py-6">
                    <div class="flex items-center gap-4">
                      <div class="w-10 h-10 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-black text-lg shadow-sm">{{ item.ingredientName.charAt(0) }}</div>
                      <div class="min-w-0">
                        <div @click="viewIngredientDetail(item)" class="font-black text-slate-800 dark:text-slate-200 text-sm hover:text-emerald-500 cursor-pointer transition-colors">{{ item.ingredientName }}</div>
                        <div class="text-[10px] text-slate-400 font-bold">别名: {{ item.alias || '-' }}</div>
                      </div>
                    </div>
                  </td>

                  <!-- 列表页性味显示 -->
                  <td class="px-6 py-6">
                    <div :class="['text-[10px] leading-relaxed p-3 rounded-2xl border-none min-h-[60px] max-h-[80px] overflow-hidden flex items-center', getPropertyStyle(item.property).badge]">
                      <p class="font-bold line-clamp-3 text-slate-600">{{ item.property || '未知' }}</p>
                    </div>
                  </td>

                  <td class="px-6 py-6 text-center">
                    <span class="px-3 py-1 rounded-xl text-[10px] font-black bg-indigo-50 text-indigo-500 border border-indigo-100 uppercase">{{ item.category || '未分类' }}</span>
                  </td>

                  <td class="px-6 py-6">
                    <p class="text-xs font-bold text-slate-500 line-clamp-2 leading-relaxed">{{ item.ancientEffect || '暂无功效描述' }}</p>
                  </td>

                  <td class="px-8 py-6 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button @click="viewIngredientDetail(item)" class="row-action-btn hover:bg-amber-500" title="详情"><i class="fa-solid fa-eye"></i></button>
                      <button @click="editIngredient(item)" class="row-action-btn hover:bg-indigo-500" title="编辑"><i class="fa-solid fa-pen-to-square"></i></button>
                      <button @click="deleteIngredient(item)" class="row-action-btn hover:bg-rose-500" title="删除"><i class="fa-solid fa-trash"></i></button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页组件 -->
          <div class="px-8 py-6 bg-slate-50/50 flex items-center justify-between">
            <span class="text-xs font-black text-slate-400 uppercase tracking-widest">Total {{ filteredIngredients.length }} Records</span>
            <div class="flex items-center gap-3">
              <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn"><i class="fa-solid fa-chevron-left"></i></button>
              <div class="px-5 py-2 bg-white rounded-xl border border-slate-200 shadow-sm text-sm font-black">{{ currentPage }} / {{ Math.ceil(filteredIngredients.length / pageSize) }}</div>
              <button @click="nextPage" :disabled="currentPage * pageSize >= filteredIngredients.length" class="pagination-btn"><i class="fa-solid fa-chevron-right"></i></button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 导入模态框 -->
    <Transition name="fade">
      <div v-if="showImportModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showImportModal = false"></div>
        <div class="relative bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-lg overflow-hidden transition-all">
          <div class="p-8 border-b border-slate-50 flex justify-between items-center bg-slate-50/50">
            <h3 class="text-xl font-black text-slate-800">导入食材档案</h3>
            <button @click="showImportModal = false" class="text-slate-400 hover:text-rose-500 transition-colors"><i class="fa-solid fa-times text-xl"></i></button>
          </div>
          <div class="p-8 space-y-6">
            <div class="p-6 bg-blue-50 rounded-3xl border border-blue-100">
              <h4 class="text-blue-900 font-black mb-3 flex items-center gap-2 text-sm"><i class="fa-solid fa-circle-info"></i> Excel 字段顺序</h4>
              <div class="grid grid-cols-2 gap-x-4 gap-y-2 text-[10px] text-blue-700 font-black uppercase">
                <div v-for="(n, i) in ['食材名称', '别名', '性味', '分类', '核心功效', '古籍原文']" :key="i" class="flex items-center gap-2">
                  <span class="w-4 h-4 bg-blue-200 rounded-md flex items-center justify-center text-[8px]">{{ i+1 }}</span><span>{{ n }}</span>
                </div>
              </div>
            </div>
            <div @click="$refs.fileInput.click()" class="border-4 border-dashed border-slate-100 rounded-[2rem] p-10 text-center hover:border-amber-400 cursor-pointer transition-all bg-slate-50">
              <input type="file" ref="fileInput" class="hidden" @change="handleFileSelect" />
              <i class="fa-solid fa-cloud-arrow-up text-4xl text-slate-300 mb-4"></i>
              <p class="text-sm font-black text-slate-600">{{ selectedFile ? selectedFile.name : '点击选择或拖拽 Excel 文件' }}</p>
            </div>
          </div>
          <div class="p-8 bg-slate-50 flex justify-end gap-3">
             <button @click="showImportModal = false" class="px-6 py-2.5 rounded-xl font-bold text-slate-400">取消</button>
             <button @click="handleImport" :disabled="!selectedFile || importing" class="px-8 py-2.5 rounded-xl bg-amber-500 text-white font-black shadow-lg shadow-amber-200">确认导入</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 详情/编辑模态框-->
    <Transition name="fade">
      <div v-if="showDetailModal || showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showDetailModal = false; showEditModal = false"></div>
        <div class="relative bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col transition-all">
          <div class="p-8 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-800/50">
            <h3 class="text-xl font-black text-slate-800 dark:text-white">{{ showDetailModal ? '食材经络档案' : '校对食材信息' }}</h3>
            <button @click="showDetailModal = false; showEditModal = false" class="text-slate-400 hover:text-rose-500 transition-colors"><i class="fa-solid fa-times text-xl"></i></button>
          </div>
          
          <div class="p-10 overflow-y-auto space-y-10 custom-scrollbar">
            <!-- 详情视图优化 -->
            <div v-if="showDetailModal && currentIngredient">
               <!-- 基础信息栅格 -->
               <div class="grid grid-cols-2 sm:grid-cols-3 gap-6 mb-8">
                 <div class="detail-item"><label>食材全称</label><p class="text-lg font-black text-slate-800">{{ currentIngredient.ingredientName }}</p></div>
                 <div class="detail-item"><label>别名</label><p class="text-slate-500">{{ currentIngredient.alias || '无' }}</p></div>
                 <div class="detail-item"><label>典籍分类</label><div><span class="px-3 py-1 rounded-lg bg-indigo-50 text-indigo-500 font-black text-[10px]">{{ currentIngredient.category || '未分类' }}</span></div></div>
               </div>

               <!-- 性味归经卡片：不再拥挤，使用全宽大区块 -->
               <div class="detail-item">
                 <label class="flex items-center gap-2"><i class="fa-solid fa-mortar-pestle text-rose-400"></i> 性味归经 Nature & Flavor</label>
                 <div :class="['mt-3 p-6 rounded-[2rem] border-none font-bold text-sm leading-loose shadow-inner', getPropertyStyle(currentIngredient.property).badge]">
                   {{ currentIngredient.property || '未知' }}
                 </div>
               </div>

               <!-- 功效卡片：全宽显示，增加行高 -->
               <div class="detail-item">
                 <label class="flex items-center gap-2"><i class="fa-solid fa-sparkles text-emerald-400"></i> 核心功效解析 Efficacy</label>
                 <div class="mt-3 bg-emerald-50/40 p-6 rounded-[2rem] border border-emerald-50">
                   <p class="text-sm font-bold text-emerald-900 leading-[1.8] text-justify">{{ currentIngredient.ancientEffect || '暂无功效记载' }}</p>
                 </div>
               </div>

               <!-- 古籍原文：使用宣纸色背景风格 -->
               <div class="detail-item">
                 <label class="flex items-center gap-2"><i class="fa-solid fa-scroll text-amber-400"></i> 本草原文 Origin Text</label>
                 <div class="mt-3 bg-[#fdfaf2] p-8 rounded-[2.5rem] border border-[#f4efe1]">
                    <p class="text-sm leading-loose text-stone-600 italic whitespace-pre-wrap font-serif">"{{ currentIngredient.ancientOriginText || '暂无原文记载' }}"</p>
                 </div>
               </div>
            </div>

            <!-- 编辑视图优化 -->
            <div v-if="showEditModal && editingIngredient" class="space-y-6">
               <div class="grid grid-cols-2 gap-6">
                  <div><label class="modal-label">食材名称</label><input v-model="editingIngredient.ingredientName" class="modal-input" /></div>
                  <div><label class="modal-label">别名</label><input v-model="editingIngredient.alias" class="modal-input" /></div>
               </div>
               <div><label class="modal-label">典籍分类</label><input v-model="editingIngredient.category" class="modal-input" /></div>
               <!-- 性味编辑：增大输入区域 -->
               <div><label class="modal-label">性味归经 (支持详细长描述)</label><textarea v-model="editingIngredient.property" class="modal-input h-24 pt-3"></textarea></div>
               <div><label class="modal-label">核心功效描述</label><textarea v-model="editingIngredient.ancientEffect" class="modal-input h-32 pt-3"></textarea></div>
               <div><label class="modal-label">古籍原文核校</label><textarea v-model="editingIngredient.ancientOriginText" class="modal-input h-48 pt-3 font-serif"></textarea></div>
            </div>
          </div>

          <div class="p-8 bg-slate-50 flex justify-end gap-3">
            <button @click="showDetailModal = false; showEditModal = false" class="px-6 py-2.5 rounded-xl font-bold text-slate-400 hover:text-slate-600 transition-colors">取消</button>
            <button v-if="showEditModal" @click="handleUpdateIngredient" class="px-8 py-2.5 rounded-xl bg-indigo-500 text-white font-black shadow-lg active:scale-95 transition-all">保存卷宗</button>
            <button v-if="showDetailModal" @click="editIngredient(currentIngredient!)" class="px-8 py-2.5 rounded-xl bg-amber-500 text-white font-black shadow-lg active:scale-95 transition-all">进入编辑模式</button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import { importFoodIngredients, findAllIngredients, getIngredientById, updateIngredientById, deleteIngredientById } from '@/api/foodIngredients'

// --- 逻辑完全保留 ---
const ingredients = ref<FoodIngredients[]>([])
const loading = ref(false)
const searchQuery = ref('')
const propertyFilter = ref('all')
const categoryFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const showImportModal = ref(false)
const showDetailModal = ref(false)
const showEditModal = ref(false)
const importing = ref(false)
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const currentIngredient = ref<FoodIngredients | null>(null)
const editingIngredient = ref<Partial<FoodIngredients>>({})
const ingredientStats = ref({ totalIngredients: 0, warmIngredients: 0, coolIngredients: 0, neutralIngredients: 0 })

watch([searchQuery, propertyFilter, categoryFilter], () => { currentPage.value = 1 })

const statsCards = computed(() => [
  { label: '收录食材总数', value: ingredientStats.value.totalIngredients, icon: 'fa-carrot', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600' },
  { label: '温热性食材', value: ingredientStats.value.warmIngredients, icon: 'fa-fire', bgClass: 'bg-rose-50 dark:bg-rose-900/30', textClass: 'text-rose-600' },
  { label: '寒凉性食材', value: ingredientStats.value.coolIngredients, icon: 'fa-snowflake', bgClass: 'bg-blue-50 dark:bg-blue-900/30', textClass: 'text-blue-600' },
  { label: '平性食材', value: ingredientStats.value.neutralIngredients, icon: 'fa-scale-balanced', bgClass: 'bg-indigo-50 dark:bg-indigo-900/30', textClass: 'text-indigo-600' },
])

// 返回极其淡化的背景色，让长文本视觉压力更小
const getPropertyStyle = (prop: string | undefined) => {
  if (!prop) return { badge: 'bg-slate-50 text-slate-400', icon: 'fa-question' }
  if (prop.includes('温') || prop.includes('热')) return { badge: 'bg-rose-50/60 text-rose-500', icon: 'fa-temperature-arrow-up' }
  if (prop.includes('凉') || prop.includes('寒')) return { badge: 'bg-blue-50/60 text-blue-500', icon: 'fa-temperature-arrow-down' }
  return { badge: 'bg-emerald-50/60 text-emerald-500', icon: 'fa-leaf' }
}

const loadIngredients = async () => {
  loading.value = true
  try {
    ingredients.value = await findAllIngredients()
    const list = ingredients.value
    ingredientStats.value = {
      totalIngredients: list.length,
      warmIngredients: list.filter(i => i.property?.includes('温') || i.property?.includes('热')).length,
      coolIngredients: list.filter(i => i.property?.includes('凉') || i.property?.includes('寒')).length,
      neutralIngredients: list.filter(i => i.property?.includes('平')).length
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleFileSelect = (e: Event) => { const target = e.target as HTMLInputElement; if (target.files?.length) selectedFile.value = target.files[0] }
const handleImport = async () => {
  if (!selectedFile.value) return; importing.value = true
  try { await importFoodIngredients(selectedFile.value); alert('导入成功'); showImportModal.value = false; loadIngredients() } 
  catch (e: any) { alert(`导入失败: ${e.message}`) } finally { importing.value = false }
}

const viewIngredientDetail = async (ingredient: FoodIngredients) => { try { const res = await getIngredientById(ingredient.ingredientId); if (res) { currentIngredient.value = res; showDetailModal.value = true } } catch { alert('加载失败') } }
const editIngredient = (ingredient: FoodIngredients) => { currentIngredient.value = ingredient; editingIngredient.value = { ...ingredient }; showDetailModal.value = false; showEditModal.value = true }
const handleUpdateIngredient = async () => {
  if (!currentIngredient.value || !editingIngredient.value) return
  try { const result = await updateIngredientById(currentIngredient.value.ingredientId, editingIngredient.value); if (result) { alert('更新成功'); showEditModal.value = false; loadIngredients() } } catch { alert('更新失败') }
}
const deleteIngredient = async (item: any) => { if (confirm(`确认删除食材 "${item.ingredientName}" 吗？`)) { try { if (await deleteIngredientById(item.ingredientId)) { alert('已删除'); await loadIngredients() } } catch { alert('删除失败') } } }

const ingredientCategoryOptions = computed(() => { const cats = new Set<string>(); ingredients.value.forEach(i => i.category && cats.add(i.category)); return Array.from(cats).sort() })
const filteredIngredients = computed(() => ingredients.value.filter(item => {
  const q = searchQuery.value.toLowerCase()
  const matchSearch = item.ingredientName.toLowerCase().includes(q) || (item.alias && item.alias.toLowerCase().includes(q)) || (item.ancientEffect && item.ancientEffect.toLowerCase().includes(q))
  const matchProp = propertyFilter.value === 'all' || item.property?.includes(propertyFilter.value)
  const matchCat = categoryFilter.value === 'all' || item.category === categoryFilter.value
  return matchSearch && matchProp && matchCat
}))
const paginatedIngredients = computed(() => filteredIngredients.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value * pageSize.value < filteredIngredients.value.length) currentPage.value++ }

onMounted(loadIngredients)

interface FoodIngredients {
  ingredientId: number; ingredientName: string; alias?: string;
  property?: string; category?: string; ancientEffect?: string; ancientOriginText?: string; updateTime?: string;
}
</script>

<style scoped>
.filter-select {
  @apply bg-slate-50 dark:bg-slate-800 border-none rounded-2xl px-5 py-3 text-sm font-bold text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-emerald-500/50 cursor-pointer transition-all;
}
.row-action-btn {
  @apply w-8 h-8 rounded-xl bg-slate-50 dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-white hover:rotate-6 transition-all duration-300;
}
.pagination-btn {
  @apply w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 transition-all shadow-sm;
}
.detail-item label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1.5 block ml-1;
}
.detail-item p {
  @apply text-sm font-bold text-slate-800 leading-relaxed;
}
.modal-label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1.5 block ml-1;
}
.modal-input {
  @apply w-full px-5 py-3 bg-slate-50 border-none rounded-2xl text-sm font-bold text-slate-800 focus:ring-2 focus:ring-emerald-500/50 transition-all;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { @apply bg-slate-200 rounded-full; }
</style>