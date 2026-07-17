<template>
  <div class="min-h-screen bg-[#f4f7fe] dark:bg-slate-950 transition-colors duration-300">
    <div class="space-y-6">
      <div class="container mx-auto px-4 max-w-7xl">
        
        <!-- 页面标题与操作 -->
        <div class="flex flex-col md:flex-row justify-between items-end gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-slate-800 dark:text-slate-100 tracking-tight">
              食疗菜品管理 <span class="text-amber-500 text-base ml-2">Recipes</span>
            </h1>
            <p class="text-slate-500 text-sm mt-1">
              管理系统食疗方库，当前收录：<span class="text-amber-500 font-bold">{{ dishStats.totalDishes }}</span> 道精选食谱
            </p>
          </div>
          <!-- 统一改为琥珀色按钮 -->
          <button 
            @click="showImportModal = true"
            class="px-5 py-2.5 bg-amber-500 hover:bg-amber-600 text-white font-bold rounded-2xl shadow-lg shadow-amber-200 dark:shadow-none transition-all flex items-center gap-2 active:scale-95"
          >
            <i class="fa-solid fa-file-import"></i>
            <span>批量导入菜品数据</span>
          </button>
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
            <div class="absolute -right-2 -bottom-2 opacity-[0.03] text-5xl rotate-12 group-hover:scale-125 transition-transform">
              <i :class="['fa-solid', stat.icon]"></i>
            </div>
          </div>
        </div>

        <!-- 筛选工具栏 (统一风格) -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none p-6 border border-white dark:border-slate-800 mb-6">
          <div class="flex flex-col lg:flex-row gap-4 items-center">
            <div class="w-full lg:flex-1 relative">
              <i class="fa-solid fa-magnifying-glass absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索菜品名称、功效、适用体质..."
                class="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800/50 border-none rounded-2xl text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 transition-all font-bold"
              />
            </div>
            
            <div class="flex flex-wrap gap-3 w-full lg:w-auto">
              <select v-model="kindFilter" class="filter-select">
                <option value="all">全部类型</option>
                <option v-for="kind in dishKindOptions" :key="kind" :value="kind">{{ kind }}</option>
              </select>
              
              <select v-model="seasonFilter" class="filter-select">
                <option value="all">全部季节</option>
                <option value="spring">春季食疗</option>
                <option value="summer">夏季食疗</option>
                <option value="autumn">秋季食疗</option>
                <option value="winter">冬季食疗</option>
              </select>
            </div>
          </div>
        </div>

        <!-- 菜品列表表格 -->
        <div class="bg-white dark:bg-slate-900 rounded-3xl shadow-xl shadow-slate-200/50 dark:shadow-none border border-white dark:border-slate-800 overflow-hidden">
          <div v-if="loading" class="py-24 text-center">
            <i class="fa-solid fa-spinner fa-spin text-4xl text-amber-500 mb-4"></i>
            <p class="text-slate-500 font-bold">正在调取膳食卷宗...</p>
          </div>

          <div v-else-if="filteredDishes.length === 0" class="py-24 text-center">
            <div class="w-20 h-20 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
              <i class="fa-solid fa-utensils text-3xl text-slate-200"></i>
            </div>
            <p class="text-slate-400 font-bold">未找到相关菜品数据</p>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse min-w-[1000px]">
              <thead>
                <tr class="bg-slate-50/50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 text-slate-400 text-[11px] font-black uppercase tracking-widest">
                  <th class="px-8 py-4 w-[25%]">菜品基本信息</th>
                  <th class="px-6 py-4 w-[12%]">来源出处</th>
                  <th class="px-6 py-4 w-[15%] text-center">形态</th>
                  <th class="px-6 py-4 w-[13%]">适宜季节</th>
                  <th class="px-6 py-4 w-[20%]">核心功效</th>
                  <th class="px-8 py-4 w-[15%] text-right">操作管理</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-50 dark:divide-slate-800">
                <tr v-for="dish in paginatedDishes" :key="dish.dishId" class="group hover:bg-slate-50/80 dark:hover:bg-slate-800/50 transition-all">
                  <td class="px-8 py-4">
                    <div class="flex items-center gap-4">
                      <div class="w-10 h-10 rounded-2xl bg-amber-100 dark:bg-amber-900/30 flex items-center justify-center text-amber-600 dark:text-amber-400 font-black text-lg shadow-sm">
                        {{ dish.dishName.charAt(0) }}
                      </div>
                      <div @click="viewDishDetail(dish)" class="font-black text-slate-800 dark:text-slate-200 text-sm hover:text-amber-500 cursor-pointer transition-colors">
                        {{ dish.dishName }}
                      </div>
                    </div>
                  </td>

                  <td class="px-6 py-4">
                    <span class="text-xs font-bold text-slate-500">{{ dish.dishSource || '未知' }}</span>
                  </td>

                  <td class="px-6 py-4 text-center">
                    <span class="px-3 py-1 rounded-xl text-[10px] font-black bg-indigo-50 text-indigo-600 border border-indigo-100 dark:bg-indigo-900/20 dark:border-indigo-800">
                      {{ dish.kind || '通用' }}
                    </span>
                  </td>

                  <!-- 还原原始季节图标 -->
                  <td class="px-6 py-4">
                    <div class="flex gap-1.5">
                      <span v-if="dish.seasonSpring" class="season-badge bg-green-50 text-green-600 border-green-200" title="春季"><i class="fa-solid fa-leaf"></i></span>
                      <span v-if="dish.seasonSummer" class="season-badge bg-red-50 text-red-600 border-red-200" title="夏季"><i class="fa-solid fa-sun"></i></span>
                      <span v-if="dish.seasonAutumn" class="season-badge bg-orange-50 text-orange-600 border-orange-200" title="秋季"><i class="fa-brands fa-canadian-maple-leaf"></i></span>
                      <span v-if="dish.seasonWinter" class="season-badge bg-blue-50 text-blue-600 border-blue-200" title="冬季"><i class="fa-regular fa-snowflake"></i></span>
                      <span v-if="!dish.seasonSpring && !dish.seasonSummer && !dish.seasonAutumn && !dish.seasonWinter" class="text-[10px] font-bold text-slate-300 italic">四季皆宜</span>
                    </div>
                  </td>

                  <td class="px-6 py-4">
                    <div class="flex flex-wrap gap-1.5">
                      <template v-for="i in 3" :key="i">
                        <span v-if="dish[`effectTag${i}`]" class="px-2 py-0.5 rounded-lg text-[10px] font-black border bg-slate-50 text-slate-500 border-slate-200 dark:bg-slate-800 dark:border-slate-700">
                          {{ dish[`effectTag${i}`] }}
                        </span>
                      </template>
                    </div>
                  </td>

                  <td class="px-8 py-4 text-right">
                    <div class="flex items-center justify-end gap-2">
                      <button @click="viewDishDetail(dish)" class="row-action-btn hover:bg-amber-500" title="详情"><i class="fa-solid fa-eye"></i></button>
                      <button @click="editDish(dish)" class="row-action-btn hover:bg-indigo-500" title="编辑"><i class="fa-solid fa-pen-to-square"></i></button>
                      <button @click="deleteDish(dish)" class="row-action-btn hover:bg-rose-500" title="删除"><i class="fa-solid fa-trash"></i></button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页组件 (统一风格) -->
          <div class="px-8 py-6 bg-slate-50/50 dark:bg-slate-800/30 flex items-center justify-between">
            <span class="text-xs font-black text-slate-400 uppercase tracking-widest">
              Total {{ filteredDishes.length }} Records
            </span>
            <div class="flex items-center gap-3">
              <button @click="currentPage--" :disabled="currentPage === 1" class="pagination-btn">
                <i class="fa-solid fa-chevron-left"></i>
              </button>
              <div class="px-5 py-2 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm text-sm font-black text-slate-800 dark:text-white">
                {{ currentPage }} <span class="text-slate-400 font-normal mx-1">/</span> {{ Math.ceil(filteredDishes.length / pageSize) }}
              </div>
              <button @click="currentPage++" :disabled="currentPage * pageSize >= filteredDishes.length" class="pagination-btn">
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
      </div>

    <!-- 批量导入模态框 (完全同步古籍页面风格) -->
    <Transition name="fade">
      <div v-if="showImportModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showImportModal = false"></div>
        <div class="relative bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-lg overflow-hidden transition-all">
          <div class="p-8 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/50">
            <h3 class="text-xl font-black text-slate-800 dark:text-white">批量导入膳食卷宗</h3>
            <button @click="showImportModal = false" class="w-8 h-8 rounded-full bg-white dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-rose-500 shadow-sm transition-all">
              <i class="fa-solid fa-times text-lg"></i>
            </button>
          </div>
          
          <div class="p-8 space-y-6">
            <!-- 字段说明 box -->
            <div class="p-6 bg-amber-50 dark:bg-amber-900/20 rounded-3xl border border-amber-100 dark:border-amber-800">
              <h4 class="text-amber-900 dark:text-amber-400 font-black mb-3 flex items-center gap-2 text-sm">
                <i class="fa-solid fa-circle-info text-amber-500"></i> Excel 表格格式要求
              </h4>
              <div class="grid grid-cols-2 gap-x-4 gap-y-2 text-[10px] text-amber-700 dark:text-amber-500 font-black uppercase tracking-tighter">
                <div v-for="n in 12" :key="n" class="flex items-center gap-2">
                  <span class="w-4 h-4 bg-amber-200 dark:bg-amber-800 rounded-md flex items-center justify-center">{{ n }}</span>
                  <span>{{ getExcelFieldName(n) }}</span>
                </div>
              </div>
              <p class="mt-4 text-[9px] text-amber-600/70 font-bold italic">注：第一行为标题，季节请填写 1(是) 或 0(否)</p>
            </div>

            <!-- 文件上传区 -->
            <div 
              class="border-4 border-dashed border-slate-100 dark:border-slate-800 rounded-[2rem] p-10 text-center hover:border-amber-400 transition-all cursor-pointer bg-slate-50 dark:bg-slate-800/50 group"
              @click="$refs.fileInput.click()"
            >
              <input type="file" ref="fileInput" accept=".xlsx,.xls,.csv" class="hidden" @change="handleFileSelect" />
              <div v-if="!selectedFile">
                <i class="fa-solid fa-cloud-arrow-up text-4xl text-slate-300 group-hover:text-amber-400 transition-colors mb-4"></i>
                <p class="text-sm font-black text-slate-600 dark:text-slate-300">点击选择或拖拽 Excel 文件</p>
                <p class="text-[10px] text-slate-400 mt-2 font-bold uppercase tracking-widest">Support .xlsx / .xls / .csv</p>
              </div>
              <div v-else class="flex flex-col items-center">
                <i class="fa-solid fa-file-circle-check text-4xl text-emerald-500 mb-3"></i>
                <p class="text-sm font-black text-slate-800 dark:text-slate-100 truncate max-w-full">{{ selectedFile.name }}</p>
                <p class="text-[10px] text-slate-400 font-bold uppercase mt-1">{{ (selectedFile.size / 1024).toFixed(2) }} KB</p>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div class="p-8 bg-slate-50 dark:bg-slate-800/50 flex justify-end gap-3">
             <button @click="showImportModal = false" class="px-6 py-2.5 rounded-xl font-bold text-slate-400 hover:text-slate-600 transition-colors">取消</button>
             <button 
                @click="handleImport" 
                :disabled="!selectedFile || importing" 
                class="px-8 py-2.5 rounded-xl bg-amber-500 text-white font-black shadow-lg shadow-amber-200 active:scale-95 disabled:opacity-50 disabled:active:scale-100 transition-all flex items-center gap-2"
              >
               <i v-if="importing" class="fa-solid fa-circle-notch fa-spin"></i>
               <span>{{ importing ? '正在录入...' : '确认导入' }}</span>
             </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 详情/编辑模态框 (UI升级) -->
    <Transition name="fade">
      <div v-if="showDetailModal || showEditModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="showDetailModal = false; showEditModal = false"></div>
        <div class="relative bg-white dark:bg-slate-900 rounded-[2.5rem] shadow-2xl w-full max-w-3xl max-h-[90vh] overflow-hidden flex flex-col transition-all">
          <div class="p-8 border-b border-slate-50 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-800/50">
            <h3 class="text-xl font-black text-slate-800 dark:text-white">{{ showDetailModal ? '菜谱详细卷宗' : '编辑菜品信息' }}</h3>
            <button @click="showDetailModal = false; showEditModal = false" class="text-slate-400 hover:text-rose-500 transition-colors"><i class="fa-solid fa-times text-xl"></i></button>
          </div>
          
          <div class="p-10 overflow-y-auto space-y-8">
            <!-- 详情视图 -->
            <div v-if="showDetailModal && currentDish">
               <div class="grid grid-cols-2 gap-8">
                 <div class="detail-item"><label>菜品名称</label><p class="text-lg font-black text-slate-800 dark:text-white">{{ currentDish.dishName }}</p></div>
                 <div class="detail-item"><label>膳食来源</label><p>{{ currentDish.dishSource || '未知' }}</p></div>
                 <div class="detail-item"><label>形态类型</label><p>{{ currentDish.kind || '通用' }}</p></div>
                 <div class="detail-item"><label>推荐时令</label>
                    <div class="flex gap-2 mt-2">
                      <span v-if="currentDish.seasonSpring" class="season-badge bg-green-50 text-green-600 border-green-200"><i class="fa-solid fa-leaf"></i></span>
                      <span v-if="currentDish.seasonSummer" class="season-badge bg-red-50 text-red-600 border-red-200"><i class="fa-solid fa-sun"></i></span>
                      <span v-if="currentDish.seasonAutumn" class="season-badge bg-orange-50 text-orange-600 border-orange-200"><i class="fa-brands fa-canadian-maple-leaf"></i></span>
                      <span v-if="currentDish.seasonWinter" class="season-badge bg-blue-50 text-blue-600 border-blue-200"><i class="fa-regular fa-snowflake"></i></span>
                    </div>
                 </div>
               </div>
               <div class="detail-item"><label>功效标签体系</label>
                  <div class="flex flex-wrap gap-2 mt-2">
                    <span v-for="i in 5" :key="i" v-show="currentDish[`effectTag${i}`]" class="px-3 py-1 rounded-xl bg-slate-50 dark:bg-slate-800 text-[10px] font-black uppercase border border-slate-100 dark:border-slate-700">{{ currentDish[`effectTag${i}`] }}</span>
                  </div>
               </div>
               <div class="detail-item"><label>古籍原文记载</label>
                 <div class="bg-slate-50 dark:bg-slate-800/50 p-6 rounded-3xl border border-slate-100 dark:border-slate-800">
                   <p class="text-sm leading-relaxed text-slate-600 dark:text-slate-400 italic">"{{ currentDish.ancientCitation || '暂无详细原文记载。' }}"</p>
                 </div>
               </div>
            </div>

            <!-- 编辑视图 -->
            <div v-if="showEditModal && editingDish" class="grid grid-cols-2 gap-6">
               <div class="col-span-2"><label class="modal-label">菜品全称</label><input v-model="editingDish.dishName" class="modal-input" /></div>
               <div><label class="modal-label">出处文献</label><input v-model="editingDish.dishSource" class="modal-input" /></div>
               <div><label class="modal-label">食物形态</label><input v-model="editingDish.kind" class="modal-input" /></div>
               <div class="col-span-2"><label class="modal-label">适宜体质描述</label><textarea v-model="editingDish.suitableConstitution" class="modal-input h-20 pt-3"></textarea></div>
               <div class="col-span-2"><label class="modal-label">古籍原文核校</label><textarea v-model="editingDish.ancientCitation" class="modal-input h-24 pt-3"></textarea></div>
            </div>
          </div>

          <div class="p-8 bg-slate-50 dark:bg-slate-800/50 flex justify-end gap-3">
            <button @click="showDetailModal = false; showEditModal = false" class="px-6 py-2.5 rounded-xl font-bold text-slate-400 hover:text-slate-600 transition-colors">关闭</button>
            <button v-if="showEditModal" @click="handleUpdateDish" class="px-8 py-2.5 rounded-xl bg-indigo-500 text-white font-black shadow-lg shadow-indigo-200 active:scale-95 transition-all">保存更新</button>
            <button v-if="showDetailModal" @click="editDish(currentDish!)" class="px-8 py-2.5 rounded-xl bg-amber-500 text-white font-black shadow-lg shadow-amber-200 active:scale-95 transition-all">进入编辑模式</button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import AdminNav from '@/components/AdminNav.vue'
import { getAllDishes, importDishes, deleteDishById, getDishById, updateDishById, Dish } from '@/api/dishes'

// --- 脚本逻辑保持 100% 原始实现 ---
const dishes = ref<Dish[]>([])
const loading = ref(false)
const searchQuery = ref('')
const kindFilter = ref('all')
const seasonFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const showImportModal = ref(false)
const showDetailModal = ref(false)
const showEditModal = ref(false)
const importing = ref(false)
const selectedFile = ref<File | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const currentDish = ref<Dish | null>(null)
const editingDish = ref<Partial<Dish>>({})
const dishStats = ref({ totalDishes: 0, springDishes: 0, summerDishes: 0, autumnWinterDishes: 0 })

watch([searchQuery, kindFilter, seasonFilter], () => { currentPage.value = 1 })

const statsCards = computed(() => [
  { label: '总菜品收录', value: dishStats.value.totalDishes, icon: 'fa-utensils', bgClass: 'bg-amber-50 dark:bg-amber-900/30', textClass: 'text-amber-600' },
  { label: '春季食疗方', value: dishStats.value.springDishes, icon: 'fa-leaf', bgClass: 'bg-emerald-50 dark:bg-emerald-900/30', textClass: 'text-emerald-600' },
  { label: '夏季食疗方', value: dishStats.value.summerDishes, icon: 'fa-sun', bgClass: 'bg-rose-50 dark:bg-rose-900/30', textClass: 'text-rose-600' },
  { label: '秋冬滋补方', value: dishStats.value.autumnWinterDishes, icon: 'fa-snowflake', bgClass: 'bg-blue-50 dark:bg-blue-900/30', textClass: 'text-blue-600' },
])

const getExcelFieldName = (n: number) => {
  const fields = ["菜品名称", "出处", "食物形态", "适合春季", "适合夏季", "适合秋季", "适合冬季", "功效1", "功效2", "适宜体质", "不适宜体质", "古籍原文"]
  return fields[n-1]
}

const loadDishes = async () => {
  loading.value = true
  try {
    dishes.value = await getAllDishes()
    const total = dishes.value.length
    dishStats.value = {
      totalDishes: total,
      springDishes: dishes.value.filter(d => d.seasonSpring === 1).length,
      summerDishes: dishes.value.filter(d => d.seasonSummer === 1).length,
      autumnWinterDishes: dishes.value.filter(d => d.seasonAutumn === 1 || d.seasonWinter === 1).length
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleFileSelect = (e: Event) => { const target = e.target as HTMLInputElement; if (target.files?.length) selectedFile.value = target.files[0] }
const handleImport = async () => {
  if (!selectedFile.value) return; importing.value = true
  try { await importDishes(selectedFile.value); alert('导入成功'); showImportModal.value = false; loadDishes() } 
  catch (e: any) { alert(`导入失败: ${e.message}`) } finally { importing.value = false }
}

const viewDishDetail = async (dish: Dish) => { try { const res = await getDishById(dish.dishId); if (res) { currentDish.value = res; showDetailModal.value = true } } catch { alert('加载失败') } }
const editDish = (dish: Dish) => { currentDish.value = dish; editingDish.value = { ...dish }; showEditModal.value = true }
const handleUpdateDish = async () => {
  if (!currentDish.value || !editingDish.value) return
  try { if (await updateDishById(currentDish.value.dishId, editingDish.value)) { alert('更新成功'); showEditModal.value = false; loadDishes() } } catch { alert('更新失败') }
}
const deleteDish = async (dish: Dish) => { if (confirm(`确认删除 "${dish.dishName}" 吗？`)) { try { if (await deleteDishById(dish.dishId)) { alert('已删除'); await loadDishes() } } catch { alert('删除失败') } } }

const dishKindOptions = computed(() => { const ks = new Set<string>(); dishes.value.forEach(d => d.kind && ks.add(d.kind)); return Array.from(ks).sort() })
const filteredDishes = computed(() => dishes.value.filter(d => {
  const q = searchQuery.value.toLowerCase()
  const matchSearch = d.dishName.toLowerCase().includes(q) || d.dishSource?.toLowerCase().includes(q) || d.effectTag1?.toLowerCase().includes(q)
  const matchKind = kindFilter.value === 'all' || d.kind === kindFilter.value
  const matchSeason = seasonFilter.value === 'all' || (seasonFilter.value === 'spring' && d.seasonSpring === 1) || (seasonFilter.value === 'summer' && d.seasonSummer === 1) || (seasonFilter.value === 'autumn' && d.seasonAutumn === 1) || (seasonFilter.value === 'winter' && d.seasonWinter === 1)
  return matchSearch && matchKind && matchSeason
}))
const paginatedDishes = computed(() => filteredDishes.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))

onMounted(loadDishes)
</script>

<style scoped>
/* 核心自定义类 - 保持全系统统一 */
.filter-select {
  @apply bg-slate-50 dark:bg-slate-800 border-none rounded-2xl px-5 py-3 text-sm font-bold text-slate-700 dark:text-slate-200 focus:ring-2 focus:ring-amber-500/50 cursor-pointer transition-all;
}

.row-action-btn {
  @apply w-8 h-8 rounded-xl bg-slate-50 dark:bg-slate-800 text-slate-400 flex items-center justify-center hover:text-white hover:rotate-6 transition-all duration-300;
}

.pagination-btn {
  @apply w-10 h-10 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-amber-500 hover:text-white disabled:opacity-30 disabled:hover:bg-white transition-all shadow-sm;
}

.season-badge {
  @apply w-7 h-7 rounded-full flex items-center justify-center text-[10px] border shadow-sm;
}

.season-dot {
  @apply w-2.5 h-2.5 rounded-full shadow-sm;
}

.detail-item label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1 block;
}

.detail-item p {
  @apply text-sm font-bold text-slate-800 dark:text-slate-200 leading-relaxed;
}

.modal-label {
  @apply text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1.5 block ml-1;
}

.modal-input {
  @apply w-full px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border-none rounded-2xl text-sm font-bold text-slate-800 dark:text-white focus:ring-2 focus:ring-indigo-500/50 transition-all;
}

/* 动画过度 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

::-webkit-scrollbar { width: 4px; }
::-webkit-scrollbar-thumb { @apply bg-slate-200 dark:bg-slate-800 rounded-full; }
</style>