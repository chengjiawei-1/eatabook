<template>
  <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题与操作 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              关联与章节管理 <span class="text-indigo-500 text-base ml-2">Relations</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              集中维护古籍、菜品、食材间的配伍关系，确保数据<span class="text-emerald-500 font-bold">逻辑严密</span>
            </p>
          </div>
          <button 
            @click="openImportModal"
            class="px-5 py-2.5 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-2xl shadow-lg shadow-amber-200 dark:shadow-none transition-all flex items-center gap-2 active:scale-95"
          >
            <i class="fa-solid fa-cloud-arrow-up"></i>
            <span>综合数据导入中心</span>
          </button>
        </div>

        <!-- 统计卡片 (统一控制台风格) -->
        <div class="grid grid-cols-1 sm:grid-cols-3 gap-6 mb-8">
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
            <div class="absolute -right-2 -bottom-2 opacity-[0.03] text-5xl rotate-12 group-hover:scale-125 transition-transform">
              <i :class="['fa-solid', stat.icon]"></i>
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
                placeholder="搜索菜品名称或食材关键字..."
                class="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800/50 border-none rounded-2xl text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 transition-all font-bold"
              />
            </div>
            
            <div class="flex flex-wrap gap-3 w-full lg:w-auto">
              <select v-model="dishFilter" class="filter-select">
                <option value="all">全部菜品</option>
                <option v-for="dish in availableDishes" :key="dish.dishId" :value="dish.dishId">{{ dish.dishName }}</option>
              </select>
              
              <select v-model="ingredientFilter" class="filter-select">
                <option value="all">全部食材</option>
                <option v-for="ingredient in availableIngredients" :key="ingredient.ingredientId" :value="ingredient.ingredientId">{{ ingredient.ingredientName }}</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 关联列表表格 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden">
          <div class="px-8 py-4 border-b border-slate-50 dark:border-slate-800 bg-slate-50/30 dark:bg-slate-800/30 flex justify-between items-center">
            <h3 class="text-sm font-black text-slate-700 dark:text-slate-200 uppercase tracking-widest">
              <i class="fa-solid fa-link mr-2 text-amber-500"></i>菜品-食材配伍详情
            </h3>
          </div>

          <!-- Loading -->
          <div v-if="loading" class="py-24 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500 font-bold">正在建立数据索引...</p>
          </div>

          <!-- Empty -->
          <div v-else-if="filteredRelations.length === 0" class="py-24 text-center">
            <div class="w-20 h-20 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
              <i class="fa-solid fa-link-slash text-3xl text-slate-200"></i>
            </div>
            <p class="text-slate-400 font-bold">暂无关联记录</p>
          </div>

          <!-- Table -->
          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[1000px]">
              <thead>
                <tr class="bg-slate-50/50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 text-slate-400 text-[11px] font-black uppercase tracking-widest">
                  <th class="px-8 py-4 w-[45%]">配伍链路 (菜品 ↔ 食材)</th>
                  <th class="px-6 py-4 w-[20%]">用量核校</th>
                  <th class="px-6 py-4 w-[15%]">创建日期</th>
                  <th class="px-8 py-4 w-[20%] text-right">操作管理</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50 dark:divide-slate-800">
                <tr v-for="(relation, index) in paginatedRelations" :key="relation.relId" class="group hover:bg-slate-50/80 transition-all">
                  <td class="px-8 py-6">
                    <div class="flex items-center gap-4">
                      <!-- 菜品小卡片 -->
                      <div class="flex items-center gap-3 bg-white dark:bg-slate-800 p-2 pr-4 rounded-2xl border border-slate-100 dark:border-slate-700 shadow-sm min-w-[160px]">
                        <div class="w-9 h-9 bg-amber-100 dark:bg-amber-900/30 rounded-xl flex items-center justify-center text-amber-600">
                          <i class="fa-solid fa-utensils text-sm"></i>
                        </div>
                        <div class="min-w-0">
                          <div class="font-black text-slate-800 dark:text-slate-200 text-xs truncate">{{ relation.dishName }}</div>
                          <div class="text-[9px] font-black text-slate-400 uppercase tracking-tighter">{{ relation.dishType }}</div>
                        </div>
                      </div>

                      <div class="text-slate-300 animate-pulse"><i class="fa-solid fa-chevron-right"></i></div>

                      <!-- 食材小卡片 -->
                      <div class="flex items-center gap-3 bg-white dark:bg-slate-800 p-2 pr-4 rounded-2xl border border-slate-100 dark:border-slate-700 shadow-sm min-w-[160px]">
                        <div class="w-9 h-9 bg-emerald-100 dark:bg-emerald-900/30 rounded-xl flex items-center justify-center text-emerald-600">
                          <i class="fa-solid fa-leaf text-sm"></i>
                        </div>
                        <div class="min-w-0">
                          <div class="font-black text-slate-800 dark:text-slate-200 text-xs truncate">{{ relation.ingredientName }}</div>
                          <div class="text-[9px] font-black text-slate-400 uppercase tracking-tighter">{{ relation.ingredientProperty }}</div>
                        </div>
                      </div>
                    </div>
                  </td>

                  <td class="px-6 py-6">
                    <span class="inline-flex items-center px-3 py-1 bg-indigo-50 text-indigo-600 dark:bg-indigo-900/20 dark:text-indigo-400 rounded-xl text-[10px] font-black border border-indigo-100 dark:border-indigo-800">
                      {{ relation.usageDetail || '适量' }}
                    </span>
                  </td>

                  <td class="px-6 py-6">
                    <span class="text-[11px] font-black text-slate-400">{{ formatDate(relation.createdAt || '') }}</span>
                  </td>

                  <td class="px-8 py-6 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button @click="deleteRelation(relation.relId, index)" class="row-action-btn hover:bg-rose-500" title="移除关联"><i class="fa-solid fa-trash-can"></i></button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页栏 -->
          <div class="px-8 py-6 bg-slate-50/50 dark:bg-slate-800/30 flex items-center justify-between">
            <span class="text-xs font-black text-slate-400 uppercase tracking-widest">Total {{ filteredRelations.length }} Records</span>
            <div class="flex items-center gap-3">
              <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn"><i class="fa-solid fa-chevron-left"></i></button>
              <div class="px-5 py-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm text-sm font-black">{{ currentPage }} / {{ Math.ceil(filteredRelations.length / pageSize) }}</div>
              <button @click="nextPage" :disabled="currentPage * pageSize >= filteredRelations.length" class="pagination-btn"><i class="fa-solid fa-chevron-right"></i></button>
            </div>
          </div>
        </div>
      </div>

      <!-- 增强版导入模态框 (UI升级) -->
      <Transition name="fade">
        <div v-if="showImportModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showImportModal = false"></div>
          
          <div class="relative bg-white dark:bg-slate-800 rounded-[2.5rem] shadow-2xl w-full max-w-lg overflow-hidden transform transition-all flex flex-col max-h-[90vh]">
            <div class="p-8 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-800/50">
              <h3 class="text-xl font-black text-slate-800 dark:text-white">综合卷宗导入</h3>
              <button @click="showImportModal = false" class="w-8 h-8 rounded-full bg-white dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-rose-500 shadow-sm transition-all"><i class="fa-solid fa-times"></i></button>
            </div>
            
            <div class="p-8 space-y-8 overflow-y-auto custom-scrollbar">
              <!-- 类型选择 -->
              <div>
                <label class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 block ml-1">请选择目标数据模型</label>
                <div class="relative">
                  <select v-model="importType" class="modal-input appearance-none cursor-pointer pr-10">
                    <option value="" disabled>-- 选择导入目标 --</option>
                    <option value="chapters">📑 典籍章节 (Chapter)</option>
                    <option value="dish_ingredient">🍲 菜品食材关联 (Dish-Ing)</option>
                    <option value="dish_chapter">🔗 菜品章节关联 (Dish-Ch)</option>
                    <option value="ingredient_chapter">🔗 食材章节关联 (Ing-Ch)</option>
                  </select>
                  <i class="fa-solid fa-chevron-down absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none text-xs"></i>
                </div>
              </div>

              <!-- 字段提示 -->
              <Transition name="fade">
                <div v-if="importType" class="p-6 bg-indigo-50 dark:bg-indigo-900/20 rounded-3xl border border-indigo-100 dark:border-indigo-800">
                  <h4 class="text-indigo-900 dark:text-indigo-400 font-black mb-3 flex items-center gap-2 text-xs">
                    <i class="fa-solid fa-circle-info"></i> {{ getTemplateTitle }} 字段规范
                  </h4>
                  <div class="flex flex-wrap gap-2">
                    <span v-for="(field, idx) in currentFields" :key="idx" class="px-2.5 py-1 bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-400 rounded-lg text-[9px] font-black border border-indigo-100 dark:border-indigo-700 uppercase">
                      {{ idx + 1 }}. {{ field }}
                    </span>
                  </div>
                </div>
              </Transition>

              <!-- 上传区 -->
              <div 
                class="border-4 border-dashed border-slate-100 dark:border-slate-800 rounded-[2rem] p-10 text-center hover:border-indigo-400 transition-all cursor-pointer bg-slate-50 dark:bg-slate-800/50 group"
                @click="$refs.fileInput.click()"
              >
                <input type="file" ref="fileInput" accept=".xlsx,.xls,.csv" class="hidden" @change="handleFileSelect" />
                
                <div v-if="!selectedFile">
                  <i class="fa-solid fa-cloud-arrow-up text-4xl text-slate-300 group-hover:text-indigo-500 transition-colors mb-4"></i>
                  <p class="text-sm font-black text-slate-600 dark:text-slate-300">选择或拖拽 Excel 文件至此</p>
                  <p class="text-[10px] text-slate-400 mt-2 font-bold uppercase tracking-widest">Support .xlsx / .xls</p>
                </div>
                <div v-else class="flex flex-col items-center">
                  <i class="fa-solid fa-file-circle-check text-4xl text-emerald-500 mb-3"></i>
                  <p class="text-sm font-black text-slate-800 dark:text-slate-100 truncate max-w-full">{{ selectedFile.name }}</p>
                  <button @click.stop="selectedFile = null" class="mt-2 text-[10px] font-black text-rose-500 uppercase tracking-widest hover:underline">重新选择</button>
                </div>
              </div>
            </div>

            <div class="p-8 bg-slate-50 dark:bg-slate-800/50 flex justify-end gap-3">
              <button @click="showImportModal = false" class="px-6 py-2.5 rounded-xl font-bold text-slate-400 hover:text-slate-600 transition-colors">取消</button>
              <button 
                @click="handleImport" 
                :disabled="!selectedFile || !importType || importing" 
                class="px-8 py-2.5 rounded-xl bg-indigo-600 text-white font-black shadow-lg shadow-indigo-200 active:scale-95 disabled:opacity-50 transition-all flex items-center gap-2"
              >
                <i v-if="importing" class="fa-solid fa-circle-notch fa-spin"></i>
                <span>{{ importing ? '正在同步...' : '确认导入卷宗' }}</span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import { importDishIngredientRel, importDishBookRel, findAllDishIngredientRels, getAllDishes, deleteDishIngredientRelById } from '@/api/dishes'
import { findAllIngredients, importIngredientBookRel } from '@/api/foodIngredients'
import { importBookChapters } from '@/api/ancientBooks'

//逻辑完全保留
const relations = ref<DishIngredientRelation[]>([])
const availableDishes = ref<Dish[]>([])
const availableIngredients = ref<FoodIngredients[]>([])
const loading = ref(false)
const searchQuery = ref('')
const dishFilter = ref('all')
const ingredientFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const showImportModal = ref(false)
const importing = ref(false)
const importType = ref('') 
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const relStats = ref({ totalRelations: 0, uniqueDishes: 0, uniqueIngredients: 0 })

watch([searchQuery, dishFilter, ingredientFilter], () => { currentPage.value = 1 })

const fieldTemplates: Record<string, string[]> = {
  chapters: ['所属古籍名称', '章节标题', '章节序号', '食材数量', '菜品数量'],
  dish_ingredient: ['菜品名称', '食材名称', '用量说明'],
  dish_chapter: ['菜品名称', '所属古籍名称', '所属章节标题'],
  ingredient_chapter: ['食材名称', '所属古籍名称', '所属章节标题']
}
const currentFields = computed(() => importType.value ? fieldTemplates[importType.value] : [])
const getTemplateTitle = computed(() => {
  const map: Record<string, string> = { chapters: '章节表', dish_ingredient: '菜品-食材关联', dish_chapter: '菜品-章节关联', ingredient_chapter: '食材-章节关联' }
  return map[importType.value] || ''
})

const statsCards = computed(() => [
  { label: '配伍关联总数', value: relStats.value.totalRelations, icon: 'fa-link', bgClass: 'bg-amber-50 dark:bg-amber-900/30', textClass: 'text-amber-600' },
  { label: '覆盖膳食方', value: relStats.value.uniqueDishes, icon: 'fa-utensils', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600' },
  { label: '覆盖本草食材', value: relStats.value.uniqueIngredients, icon: 'fa-leaf', bgClass: 'bg-blue-50 dark:bg-blue-900/30', textClass: 'text-blue-600' },
])

const filteredRelations = computed(() => relations.value.filter(relation => {
  const q = searchQuery.value.toLowerCase()
  const matchSearch = (relation.dishName?.toLowerCase() || '').includes(q) || (relation.ingredientName?.toLowerCase() || '').includes(q)
  const matchDish = dishFilter.value === 'all' || relation.dishId === parseInt(dishFilter.value)
  const matchIngredient = ingredientFilter.value === 'all' || relation.ingredientId === parseInt(ingredientFilter.value)
  return matchSearch && matchDish && matchIngredient
}))
const paginatedRelations = computed(() => filteredRelations.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

const openImportModal = () => { showImportModal.value = true; importType.value = ''; selectedFile.value = null }
const handleFileSelect = (e: Event) => { const target = e.target as HTMLInputElement; if (target.files?.length) selectedFile.value = target.files[0] }

const deleteRelation = async (relId: number, index: number) => {
  if (!confirm('确定要删除这条配伍关联吗？')) return
  try { if (await deleteDishIngredientRelById(relId)) { alert('删除成功'); relations.value.splice(index, 1); calculateStats() } } 
  catch { alert('删除失败') }
}

const loadRelations = async () => {
  loading.value = true
  try {
    const [rels, dishes, ingredients] = await Promise.all([findAllDishIngredientRels(), getAllDishes(), findAllIngredients()])
    relations.value = rels.map(rel => {
      const d = dishes.find(d => d.dishId === rel.dishId); const i = ingredients.find(i => i.ingredientId === rel.ingredientId)
      return { ...rel, dishName: d?.dishName || '未知菜品', dishType: d?.kind || '菜品', ingredientName: i?.ingredientName || '未知食材', ingredientProperty: i?.property || '食材', createdAt: rel.createdAt || new Date().toISOString() }
    })
    availableDishes.value = dishes; availableIngredients.value = ingredients; calculateStats()
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const calculateStats = () => {
  relStats.value = { totalRelations: relations.value.length, uniqueDishes: new Set(relations.value.map(r => r.dishId)).size, uniqueIngredients: new Set(relations.value.map(r => r.ingredientId)).size }
}

const handleImport = async () => {
  if (!selectedFile.value || !importType.value) return; importing.value = true
  try {
    let res = null
    if (importType.value === 'chapters') res = await importBookChapters(selectedFile.value)
    else if (importType.value === 'dish_ingredient') res = await importDishIngredientRel(selectedFile.value)
    else if (importType.value === 'dish_chapter') res = await importDishBookRel(selectedFile.value)
    else if (importType.value === 'ingredient_chapter') res = await importIngredientBookRel(selectedFile.value)
    alert('导入成功！'); showImportModal.value = false; if (importType.value === 'dish_ingredient') loadRelations()
  } catch (e: any) { alert(`失败: ${e.message}`) } finally { importing.value = false }
}

const prevPage = () => { if (currentPage.value > 1) currentPage.value-- }
const nextPage = () => { if (currentPage.value * pageSize.value < filteredRelations.length) currentPage.value++ }
const formatDate = (str: string) => new Date(str).toLocaleDateString()

onMounted(loadRelations)

interface Dish { dishId: number; dishName: string; kind: string; }
interface FoodIngredients { ingredientId: number; ingredientName: string; property?: string; }
interface DishIngredientRelation { relId: number; dishId: number; ingredientId: number; usageDetail?: string; dishName?: string; dishType?: string; ingredientName?: string; ingredientProperty?: string; createdAt?: string; }
</script>

<style scoped>
.filter-select {
  @apply bg-slate-50 dark:bg-slate-800 border-none rounded-2xl px-5 py-3 text-sm font-bold text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-all max-w-[160px];
}

.row-action-btn {
  @apply w-8 h-8 rounded-xl bg-slate-50 dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-white hover:rotate-6 transition-all duration-300;
}

.pagination-btn {
  @apply w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 transition-all shadow-sm;
}

.modal-input {
  @apply w-full px-5 py-3 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-sm font-bold text-slate-800 dark:text-white focus:ring-2 focus:ring-indigo-500/50 transition-all;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }
</style>