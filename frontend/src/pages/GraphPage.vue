<template>
  <div class="min-h-screen flex flex-col bg-white dark:bg-slate-900 transition-colors duration-300">
    <!-- 导航栏 -->
    <Nav />

    <!-- 主体内容 -->
    <main class="flex-grow pt-20 pb-8">
      <div class="container mx-auto px-4">
        
        <!-- 页面标题 -->
        <div class="mb-6 text-center">
          <h2 class="text-[clamp(1.5rem,3vw,2rem)] font-bold text-amber-800 dark:text-amber-400 mb-2" style="font-family: 'Noto Serif SC', serif;">
            食养知识图谱
          </h2>
          <p class="text-amber-700/80 dark:text-amber-300/80 max-w-2xl mx-auto text-sm md:text-base">
            探索古籍、食疗方、食材、功效与体质之间的关联<br>
          </p>
        </div>

        <!-- 搜索与操作栏 -->
        <div class="flex flex-col md:flex-row gap-4 mb-6 items-center">
          <div class="relative flex-1 w-full">
            <i class="fa-solid fa-search absolute left-3 top-1/2 -translate-y-1/2 text-amber-400"></i>
            <input 
              type="text" 
              placeholder="全局搜索 (输入关键词将显示相关所有节点)..." 
              v-model="searchQuery"
              @keyup.enter="performSearch"
              class="w-full h-10 pl-10 pr-4 rounded-lg border border-amber-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-amber-900 dark:text-amber-200 placeholder:text-amber-500/60 focus:outline-none focus:ring-2 focus:ring-amber-500/50 transition-all shadow-sm"
            >
            <!-- 添加搜索按钮 -->
            <button 
              @click="performSearch"
              class="absolute right-2 top-1/2 -translate-y-1/2 h-7 px-3 rounded-md bg-amber-500 text-white hover:bg-amber-600 transition-colors text-xs font-medium flex items-center gap-1"
            >
              <i class="fa-solid fa-search text-xs"></i>
              搜索
            </button>
          </div>
          
          <div class="flex gap-3 items-center w-full md:w-auto justify-end">
            <button @click="resetToBooks" class="h-10 px-4 rounded-lg bg-amber-100 dark:bg-slate-800 text-amber-800 dark:text-amber-300 hover:bg-amber-200 dark:hover:bg-slate-700 transition-colors flex items-center gap-2 text-sm font-medium">
              <i class="fa-solid fa-rotate-right"></i> 重置视图
            </button>
            <div class="hidden sm:block text-xs text-amber-600 dark:text-amber-500 bg-amber-50 dark:bg-slate-800 px-3 py-2 rounded-lg border border-amber-100 dark:border-slate-700">
              当前展示: <span class="font-bold">{{ visibleNodes.length }}</span> / 总量: {{ rawGraphData.nodes.length }}
            </div>
          </div>
        </div>

        <!-- 节点类型说明 (图例) -->
        <div class="mb-6 rounded-2xl border border-amber-200/60 dark:border-slate-700 bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm p-5 shadow-lg">
          <div class="flex flex-col md:flex-row md:items-center gap-4">
            <!-- 标题区域 -->
            <div class="flex items-center gap-3 flex-shrink-0">
              <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-amber-500 to-orange-500 flex items-center justify-center shadow-sm">
                <i class="fa-solid fa-layer-group text-white text-sm"></i>
              </div>
              <div>
                <h4 class="font-bold text-amber-900 dark:text-amber-300 text-base">节点类型说明</h4>
                <p class="text-xs text-amber-600/70 dark:text-amber-400/70 mt-0.5">点击节点查看详细信息</p>
              </div>
            </div>
            
            <!-- 节点类型列表 -->
            <div class="flex flex-wrap gap-3 flex-1">
              <!-- 只遍历 unique 的类型，防止中英文重复显示 -->
              <div v-for="type in uniqueNodeTypes" :key="type.value" class="flex items-center gap-2 bg-gradient-to-r from-amber-50/50 to-orange-50/30 dark:from-slate-800/50 dark:to-slate-700/30 px-3 py-2.5 rounded-lg border border-amber-100/50 dark:border-slate-600/50 hover:shadow-md transition-all group">
                <span class="w-3 h-3 rounded-full shadow-lg group-hover:scale-110 transition-transform" :style="{ backgroundColor: type.color, boxShadow: `0 0 0 2px ${type.color}20` }"></span>
                <span class="text-xs font-semibold text-amber-900/90 dark:text-amber-200/90 tracking-wide">{{ type.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="flex flex-col justify-center items-center py-32 bg-white dark:bg-slate-900 rounded-xl border border-amber-100 dark:border-slate-800">
          <div class="animate-spin rounded-full h-12 w-12 border-4 border-amber-100 border-t-amber-500 mb-4"></div>
          <p class="text-amber-600 dark:text-amber-400 text-sm">正在加载知识图谱数据...</p>
        </div>

        <!-- 主体内容区 -->
        <div v-else class="flex flex-col lg:flex-row gap-6 h-[750px]">
          
          <!-- 左侧：知识图谱可视区域 -->
          <div class="w-full lg:w-3/4 rounded-xl border border-amber-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900 shadow-sm overflow-hidden relative flex flex-col">
            <!-- 悬浮操作指引 -->
            <div class="absolute top-4 left-4 z-10 bg-white/90 dark:bg-slate-800/90 p-3 rounded-lg text-xs text-amber-800 dark:text-amber-300 shadow-md border border-amber-100 dark:border-slate-700 backdrop-blur-sm">
              <p class="mb-1"><i class="fa-solid fa-computer-mouse w-4 text-center"></i> <strong>单击</strong>: 查看详情</p>
              <p class="mb-1"><i class="fa-solid fa-circle-dot w-4 text-center"></i> <strong>双击</strong>: 展开/折叠关联</p>
              <p><i class="fa-solid fa-up-down-left-right w-4 text-center"></i> <strong>滚轮</strong>: 缩放画布</p>
            </div>

            <!-- 图谱容器 -->
            <div ref="graphContainer" class="flex-grow w-full h-full cursor-grab active:cursor-grabbing relative">
              <!-- 空状态提示 (如果筛选结果为空) -->
              <div v-if="visibleNodes.length === 0" class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none z-20">
                <div class="bg-white/90 dark:bg-slate-800/90 p-8 rounded-xl shadow-xl border border-amber-200 dark:border-slate-700 text-center backdrop-blur-sm pointer-events-auto max-w-sm">
                  <div class="w-16 h-16 bg-amber-100 dark:bg-slate-700 rounded-full flex items-center justify-center mx-auto mb-4">
                    <i class="fa-solid fa-network-wired text-amber-500 text-2xl"></i>
                  </div>
                  <h3 class="font-bold text-amber-800 dark:text-amber-300 text-lg mb-2">暂无可见节点</h3>
                  <p class="text-sm text-gray-500 dark:text-gray-400 mb-6">
                    当前视图没有节点。可能是因为没有找到“古籍”类型的数据，或者搜索无结果。
                  </p>
                  <button @click="forceShowAll" class="w-full px-4 py-2.5 bg-gradient-to-r from-amber-500 to-orange-500 text-white rounded-lg text-sm font-medium hover:shadow-lg transition-all">
                    显示前 50 个节点 (兜底模式)
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：节点详情区域 -->
          <div class="w-full lg:w-1/4 rounded-xl border border-amber-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm overflow-hidden flex flex-col">
            <div class="px-5 py-4 border-b border-amber-200 dark:border-slate-800 bg-amber-50/50 dark:bg-slate-800/50 flex justify-between items-center">
              <h3 class="font-bold text-amber-800 dark:text-amber-400" style="font-family: 'Noto Serif SC', serif;">
                节点详情
              </h3>
              <span v-if="selectedNode" class="text-xs px-2 py-0.5 rounded bg-amber-200/50 dark:bg-slate-700 text-amber-800 dark:text-amber-300">
                ID: {{ selectedNode.id }}
              </span>
            </div>
            
            <!-- 有选中节点时 -->
            <div v-if="selectedNode" class="p-5 overflow-y-auto flex-1 custom-scrollbar">
              <!-- 头部 -->
              <div class="flex items-center gap-2 mb-4">
                <div class="w-3 h-3 rounded-full shadow-sm" :style="{ backgroundColor: getNodeColor(selectedNode.label) }"></div>
                <span class="text-sm font-medium text-gray-500 dark:text-gray-400">
                  {{ nodeTypeText(selectedNode.label) }}
                </span>
                <span v-if="isExpanded(selectedNode)" class="ml-auto text-[10px] bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400 px-2 py-0.5 rounded-full border border-green-200 dark:border-green-800">
                  已展开关联
                </span>
              </div>
              
              <h4 class="text-xl font-bold text-slate-800 dark:text-slate-100 mb-6 leading-tight break-words">
                {{ getNodeName(selectedNode) }}
              </h4>

              <!-- 操作按钮 -->
              <button 
                @click="toggleNodeExpand(selectedNode)"
                class="w-full mb-6 py-2.5 rounded-xl border border-amber-200 dark:border-slate-700 text-amber-700 dark:text-amber-300 hover:bg-amber-50 dark:hover:bg-slate-800 transition-all text-sm font-medium flex items-center justify-center gap-2 group"
              >
                <i :class="['transition-transform duration-300', isExpanded(selectedNode) ? 'fa-solid fa-compress group-hover:scale-90' : 'fa-solid fa-expand group-hover:scale-110']"></i>
                {{ isExpanded(selectedNode) ? '折叠关联节点' : '展开关联节点' }}
              </button>
              
              <!-- 属性列表 -->
              <div class="space-y-4">
                <div class="flex items-center gap-2 pb-2 border-b border-gray-100 dark:border-slate-800">
                   <i class="fa-solid fa-list-ul text-amber-400 text-xs"></i>
                   <span class="text-xs font-bold text-gray-400 uppercase tracking-wider">详细属性</span>
                </div>

                <div v-if="selectedNode.properties && Object.keys(selectedNode.properties).length" class="space-y-3">
                  <div v-for="[key, value] in Object.entries(selectedNode.properties)" :key="key" class="group/prop">
                    <span class="text-gray-400 dark:text-gray-500 text-xs block mb-1 group-hover/prop:text-amber-600 transition-colors">{{ formatKeyName(key) }}</span>
                    <span class="text-slate-700 dark:text-slate-300 text-sm block leading-relaxed bg-gray-50 dark:bg-slate-800/50 p-2 rounded-lg border border-transparent group-hover/prop:border-amber-100 dark:group-hover/prop:border-slate-700 transition-colors">{{ value || "-" }}</span>
                  </div>
                </div>
                <div v-else class="text-center py-8 text-gray-400 text-sm italic">
                  暂无额外属性
                </div>
              </div>
            </div>
            
            <!-- 无选中节点时 -->
            <div v-else class="flex flex-col items-center justify-center h-full p-8 text-center text-gray-400 dark:text-gray-500">
              <div class="w-16 h-16 rounded-full bg-gray-50 dark:bg-slate-800 flex items-center justify-center mb-4 animate-pulse">
                <i class="fa-regular fa-hand-pointer text-2xl text-amber-300"></i>
              </div>
              <p class="font-medium text-slate-600 dark:text-slate-400 mb-1">未选择节点</p>
              <p class="text-xs">请点击左侧图谱中的节点<br>查看详细信息</p>
            </div>
          </div>
        </div>
      </div>
    </main>
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from "vue";
import { getGraph } from "@/api/graph"; 
import Nav from "@/components/Nav.vue";
import Footer from "@/components/Footer.vue";
import * as d3 from "d3";

//类型定义 
interface GraphNode extends d3.SimulationNodeDatum {
  id: string; // 强制统一为字符串
  label: string;
  properties?: Record<string, any>;
  x?: number;
  y?: number;
  fx?: number | null;
  fy?: number | null;
}

interface GraphLink extends d3.SimulationLinkDatum<GraphNode> {
  source: string | GraphNode;
  target: string | GraphNode;
}

interface GraphData {
  nodes: GraphNode[];
  links: GraphLink[];
}

//状态管理 
const rawGraphData = ref<GraphData>({ nodes: [], links: [] });
const visibleNodes = ref<GraphNode[]>([]);
const visibleLinks = ref<GraphLink[]>([]);
const expandedNodeIds = ref<Set<string>>(new Set());

const graphContainer = ref<HTMLDivElement | null>(null);
const selectedNode = ref<GraphNode | null>(null);
const searchQuery = ref(""); 
const loading = ref(true);

// D3 对象
let simulation: d3.Simulation<GraphNode, GraphLink> | null = null;
let svg: d3.Selection<SVGSVGElement, unknown, null, undefined> | null = null;
let g: d3.Selection<SVGGElement, unknown, null, undefined> | null = null;
let zoomBehavior: d3.ZoomBehavior<SVGSVGElement, unknown> | null = null;

// 节点颜色配置
const nodeTypes = [
  { label: "古籍", value: "Book", color: "#d97814" },
  { label: "古籍", value: "古籍", color: "#d97814" },
  { label: "章节", value: "Chapter", color: "#f39c12" },
  { label: "章节", value: "章节", color: "#f39c12" },
  { label: "食材", value: "Ingredient", color: "#8dc63f" },
  { label: "食材", value: "食材", color: "#8dc63f" },
  { label: "菜品", value: "Dish", color: "#e74c3c" },
  { label: "菜品", value: "菜品", color: "#e74c3c" },
  { label: "功效", value: "Effect", color: "#9b59b6" },
  { label: "功效", value: "功效", color: "#9b59b6" },
  { label: "体质", value: "Constitution", color: "#ff69b4" },
  { label: "体质", value: "体质", color: "#ff69b4" }
];

const uniqueNodeTypes = computed(() => {
  const map = new Map();
  nodeTypes.forEach(t => {
    if (!map.has(t.label)) map.set(t.label, t);
  });
  return Array.from(map.values());
});

//辅助工具 
const getId = (n: string | GraphNode): string => {
  return (typeof n === 'object') ? n.id : String(n); // 确保返回字符串
};

const cleanLabel = (l = "") => l ? l.replace(/["'`\s]/g, "").trim() : "";

const getNodeName = (n: GraphNode) => {
  if (!n) return "";
  if (n.properties) {
    const keys = ["name", "book_name", "chapter_title", "ingredient_name", "dish_name", "effect_name", "constitution_name", "title"];
    for (const k of keys) {
      if (n.properties[k]) return String(n.properties[k]);
    }
  }
  return cleanLabel(n.label) || n.id;
};

const formatKeyName = (key: string) => {
  const map: Record<string, string> = {
    name: "名称", book_name: "古籍名", chapter_title: "章节",
    ingredient_name: "食材", dish_name: "菜品", effect_name: "功效",
    description: "描述", efficacy: "功效", taste: "性味", nature: "归经",
    create_time: "创建时间"
  };
  return map[key] || key;
};

const getNodeColor = (label: string) => {
  return nodeTypes.find(t => t.value === cleanLabel(label))?.color || "#94a3b8";
};

const nodeTypeText = (l: string) => {
  const clean = cleanLabel(l);
  const type = nodeTypes.find(t => t.value === clean);
  return type ? type.label : clean;
};

//核心逻辑：渐进式展开 

const initBookView = () => {
  if (!rawGraphData.value.nodes.length) return;

   // 优先筛选体质节点
  let constitutionNodes = rawGraphData.value.nodes.filter(n => {
    const l = cleanLabel(n.label);
     return l === 'Constitution' || l === '体质';
  });
   // 如果没有体质节点，再筛选古籍节点
  let displayNodes = constitutionNodes;
  if (constitutionNodes.length === 0) {
    displayNodes = rawGraphData.value.nodes.filter(n => {
      const l = cleanLabel(n.label);
      return l === 'Book' || l === 'book' || l === '古籍';
    });
  }
  if (displayNodes.length === 0) {
    console.warn("未找到体质或古籍节点，使用兜底显示");
    forceShowAll();
    return;
  }
  
  // 布局
  let width = 800, height = 600;
  if (graphContainer.value) {
    width = graphContainer.value.clientWidth || 800;
    height = graphContainer.value.clientHeight || 600;
  }

  // 初始化位置（圆形布局）
  visibleNodes.value = displayNodes.map((n, index) => {
    const angle = (index / displayNodes.length) * 2 * Math.PI;
    const radius = Math.min(width, height) * 0.25; // 稍微增大半径
    return {
      ...n, 
      x: width / 2 + Math.cos(angle) * radius, 
      y: height / 2 + Math.sin(angle) * radius,
      vx: 0, 
      vy: 0
    };
  });
  
  visibleLinks.value = [];
  expandedNodeIds.value.clear();
  
  // 视角归位
  if (svg && zoomBehavior) {
    svg.transition().duration(750).call(zoomBehavior.transform, d3.zoomIdentity);
  }

  updateSimulation();
};

const forceShowAll = () => {
  let width = graphContainer.value?.clientWidth || 800;
  let height = graphContainer.value?.clientHeight || 600;
  
  visibleNodes.value = rawGraphData.value.nodes.slice(0, 50).map(n => ({
    ...n,
    x: width / 2 + (Math.random() - 0.5) * 50,
    y: height / 2 + (Math.random() - 0.5) * 50
  }));
  visibleLinks.value = [];
  expandedNodeIds.value.clear();
  updateSimulation();
};

const toggleNodeExpand = (node: GraphNode) => {
  if (expandedNodeIds.value.has(node.id)) {
    collapseNode(node);
  } else {
    expandNode(node);
  }
};

const expandNode = (centerNode: GraphNode) => {
  const centerId = String(centerNode.id); // 确保中心节点ID是字符串

  // 在原始连线数据中查找
  const relatedLinks = rawGraphData.value.links.filter(l => {
    // 这里的 source/target 已经被初始化为字符串了
    const sId = String(l.source);
    const tId = String(l.target);
    return sId === centerId || tId === centerId;
  });

  if (relatedLinks.length === 0) {
    console.log(`节点 ${centerId} 没有关联数据`);
    // 可以添加一个 Toast 提示用户
    return;
  }

  const newNodes: GraphNode[] = [];
  const newLinks: GraphLink[] = [];
  
  const visibleNodeIds = new Set(visibleNodes.value.map(n => n.id));
  const visibleLinkIds = new Set(visibleLinks.value.map(l => `${getId(l.source)}-${getId(l.target)}`));

  relatedLinks.forEach(link => {
    const sId = String(link.source);
    const tId = String(link.target);
    const neighborId = sId === centerId ? tId : sId;
    
    // 查找邻居节点对象
    const rawNeighbor = rawGraphData.value.nodes.find(n => n.id === neighborId);
    
    if (rawNeighbor) {
      if (!visibleNodeIds.has(neighborId)) {
        // 克隆节点，并继承父节点位置 + 少量随机偏移防止完全重叠
        newNodes.push({ 
          ...rawNeighbor,
          x: centerNode.x! + (Math.random() - 0.5) * 10, 
          y: centerNode.y! + (Math.random() - 0.5) * 10,
          vx: 0,
          vy: 0
        });
        visibleNodeIds.add(neighborId);
      }
      
      const linkKey = `${sId}-${tId}`;
      if (!visibleLinkIds.has(linkKey)) {
        //D3 需要 source 和 target 指向 visibleNodes 中的 ID
        newLinks.push({ source: sId, target: tId });
        visibleLinkIds.add(linkKey);
      }
    }
  });

  // 更新视图
  visibleNodes.value = [...visibleNodes.value, ...newNodes];
  visibleLinks.value = [...visibleLinks.value, ...newLinks];
  expandedNodeIds.value.add(centerNode.id);

  updateSimulation();
};

const collapseNode = (centerNode: GraphNode) => {
  const centerId = String(centerNode.id);

  const linksToRemove = visibleLinks.value.filter(l => {
    const sId = getId(l.source);
    const tId = getId(l.target);
    return sId === centerId || tId === centerId;
  });

  const potentialRemovals = new Set<string>();
  linksToRemove.forEach(l => {
    const sId = getId(l.source);
    const tId = getId(l.target);
    potentialRemovals.add(sId === centerId ? tId : sId);
  });

  visibleLinks.value = visibleLinks.value.filter(l => !linksToRemove.includes(l));

  const activeNodeIds = new Set<string>();
  visibleLinks.value.forEach(l => {
    activeNodeIds.add(getId(l.source));
    activeNodeIds.add(getId(l.target));
  });
  activeNodeIds.add(centerNode.id);

  visibleNodes.value = visibleNodes.value.filter(n => {
    if (potentialRemovals.has(n.id) && !activeNodeIds.has(n.id)) {
      expandedNodeIds.value.delete(n.id); 
      return false;
    }
    return true;
  });

  expandedNodeIds.value.delete(centerNode.id);

  if (
  selectedNode.value &&
  !visibleNodes.value.find(n => n.id === selectedNode.value!.id)
) {
  selectedNode.value = null;
}
  updateSimulation();
};

const resetToBooks = () => {
  searchQuery.value = "";
  selectedNode.value = null;
  initBookView();
};

const isExpanded = (node: GraphNode) => expandedNodeIds.value.has(node.id);

const performSearch = () => {
  if (!searchQuery.value.trim()) {
    resetToBooks();
    return;
  }
  const keyword = searchQuery.value.toLowerCase();
  const matches = rawGraphData.value.nodes.filter(n => {
    const name = getNodeName(n).toLowerCase();
    const props = n.properties ? Object.values(n.properties).join("").toLowerCase() : "";
    return name.includes(keyword) || props.includes(keyword);
  });

  if (matches.length > 0) {
    const width = graphContainer.value?.clientWidth || 800;
    const height = graphContainer.value?.clientHeight || 600;
    
    visibleNodes.value = matches.map(n => ({
      ...n,
      x: width/2 + (Math.random() - 0.5) * 100,
      y: height/2 + (Math.random() - 0.5) * 100
    }));
    visibleLinks.value = [];
    expandedNodeIds.value.clear();
    updateSimulation();
  }
};

//D3 渲染逻辑

const initGraph = () => {
  console.log("d3:", d3);
  console.log("container:", graphContainer.value);
  console.log("location:", window.location.href);
  simulation?.stop();
  
  // 增强DOM元素存在性检查
  if (
  !graphContainer.value ||
  !graphContainer.value.isConnected ||
  graphContainer.value.clientWidth === 0
) {
  console.warn("Graph container not ready");
  return;
}
  const width = graphContainer.value.clientWidth;
  const height = graphContainer.value.clientHeight;

  // 安全地清理现有SVG
  try {
    d3.select(graphContainer.value).select("svg").remove();
  } catch (error) {
    console.warn('Error cleaning graph container:', error);
  }

  // 安全地创建SVG元素
  try {
    svg = d3.select(graphContainer.value)
      .append("svg")
      .attr("width", "100%")
      .attr("height", "100%")
      .attr("viewBox", [0, 0, width, height])
      .style("max-width", "100%")
      .style("height", "auto");
  } catch (error) {
    console.error('Error creating SVG:', error);
    return;
  }

  zoomBehavior = d3.zoom<SVGSVGElement, unknown>()
      .scaleExtent([0.1, 4])
      .on("zoom", (event) => g?.attr("transform", event.transform));

  svg.call(zoomBehavior).on("dblclick.zoom", null);
  g = svg.append("g");

  simulation = d3.forceSimulation<GraphNode, GraphLink>()
    .force("link", d3.forceLink<GraphNode, GraphLink>().id(d => d.id).distance(100))
    .force("charge", d3.forceManyBody().strength(-300))
    .force("collide", d3.forceCollide().radius(35))
    .force("center", d3.forceCenter(width / 2, height / 2).strength(0.05));
};

const updateSimulation = () => {
  console.log("当前节点数量:", visibleNodes.value.length);
  if (!simulation || !g || !svg) return;

  simulation.nodes(visibleNodes.value);
  (simulation.force("link") as d3.ForceLink<GraphNode, GraphLink>).links(visibleLinks.value);

  const linkSelection = g.selectAll<SVGLineElement, GraphLink>(".link")
    .data(visibleLinks.value, d => `${getId(d.source)}-${getId(d.target)}`);

  const linkEnter = linkSelection.enter().append("line")
    .attr("class", "link")
    .attr("stroke", "#cbd5e1")
    .attr("stroke-width", 1.5)
    .attr("stroke-opacity", 0.6);
  
  linkSelection.exit().remove();
  const links = linkEnter.merge(linkSelection);

  const nodeSelection = g.selectAll<SVGGElement, GraphNode>(".node")
    .data(visibleNodes.value, d => String(d.id));

  const nodeEnter = nodeSelection.enter().append("g")
    .attr("class", "node")
    .call(d3.drag<SVGGElement, GraphNode>()
      .on("start", dragStarted)
      .on("drag", dragged)
      .on("end", dragEnded)
    );

  nodeEnter.append("circle")
    .attr("r", 0) 
    .attr("fill", d => getNodeColor(d.label))
    .attr("stroke", "#fff")
    .attr("stroke-width", 2)
    .attr("cursor", "pointer")
    .transition().duration(500)
    .attr("r", d => {
      const l = cleanLabel(d.label);
      return (l === 'Book' || l === '古籍') ? 24 : 16;
    });

  nodeEnter.append("text")
    .text(d => {
      const name = getNodeName(d);
      return name.length > 5 ? name.substring(0, 5) + ".." : name;
    })
    .attr("font-size", "10px")
    .attr("text-anchor", "middle")
    .attr("dy", 4)
    .attr("fill", "white")
    .style("pointer-events", "none")
    .style("opacity", 0)
    .transition().duration(500)
    .style("opacity", 1);

  nodeEnter.on("click", (event, d) => {
    try {
      event.stopPropagation();
      console.log("点击节点:", d);
      selectedNode.value = d;
      updateHighlight(); 
    } catch (error) {
      console.error('Error handling node click:', error);
    }
  });

  nodeEnter.on("dblclick", (event, d) => {
    try {
      event.stopPropagation();
      selectedNode.value = d;
      toggleNodeExpand(d);
    } catch (error) {
      console.error('Error handling node double click:', error);
    }
  });

  nodeSelection.exit().transition().duration(300).attr("opacity", 0).remove();
  
  const nodes = nodeEnter.merge(nodeSelection);
  updateHighlight();

  simulation.on("tick", () => {
    if (!g) return;
    links
      .attr("x1", d => (d.source as GraphNode).x!)
      .attr("y1", d => (d.source as GraphNode).y!)
      .attr("x2", d => (d.target as GraphNode).x!)
      .attr("y2", d => (d.target as GraphNode).y!);

    nodes.attr("transform", d => `translate(${d.x},${d.y})`);
  });

  simulation.alpha(0.3).restart();
};

const updateHighlight = () => {
  if (!g || !g.node()?.isConnected) return;
  
  try {
    const nodes = g.selectAll<SVGGElement, GraphNode>(".node");
    if (nodes.empty()) return;
    
    nodes.select("circle")
      .attr("stroke", d => d.id === selectedNode.value?.id ? "#f59e0b" : "#fff")
      .attr("stroke-width", d => d.id === selectedNode.value?.id ? 4 : 2)
      .attr("stroke-dasharray", d => isExpanded(d) ? "3,1" : "none");
  } catch (error) {
    console.warn('Error updating highlight:', error);
  }
};

const dragStarted = (event: any, d: GraphNode) => {
  if (!event.active) simulation?.alphaTarget(0.3).restart();
  d.fx = d.x;
  d.fy = d.y;
};
const dragged = (event: any, d: GraphNode) => {
  d.fx = event.x;
  d.fy = event.y;
};
const dragEnded = (event: any, d: GraphNode) => {
  if (!event.active) simulation?.alphaTarget(0);
  d.fx = null;
  d.fy = null;
};

// 生命周期
let resizeTimer: any = null;

const handleResize = () => {
  console.log("窗口 resize 触发");

  if (resizeTimer) {
    clearTimeout(resizeTimer);
  }

  resizeTimer = setTimeout(async () => {
    if (loading.value) return;

    await nextTick();

    if (!graphContainer.value?.isConnected) {
      console.warn("resize 时 container 不存在");
      return;
    }

    initGraph();
    updateSimulation();

  }, 200); // 防抖
};

onMounted(async () => {
  try {
    loading.value = true;
    const res = await getGraph();
    
    if (res && res.nodes) {
      const normalizedNodes = res.nodes.map((n: any) => ({
        ...n,
        id: String(n.id) 
      }));
      
      const normalizedLinks = res.links.map((l: any) => ({
        ...l,
        source: String(l.source),
        target: String(l.target)
      }));

      rawGraphData.value = {
        nodes: normalizedNodes,
        links: normalizedLinks
      };
      
      loading.value = false;
      await nextTick();
      initGraph();
      initBookView();
    } 
  } catch (e) {
    console.error("加载失败", e);
    loading.value = false;
  }
  
  window.addEventListener("resize", handleResize);
});

onUnmounted(() => {
  simulation?.stop();
  window.removeEventListener("resize", handleResize);
});
</script>

<style scoped>
/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #e2e8f0;
  border-radius: 20px;
}
.dark .custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: #475569;
}

/* 文本阴影增强可读性 */
:deep(.node text) {
  text-shadow: 0 1px 3px rgba(0,0,0,0.8);
}
/* 表格内按钮 Hover 效果 */
.group:hover td {
  padding-top: 1.5rem;
  padding-bottom: 1.5rem;
  transition: all 0.3s;
}
</style>