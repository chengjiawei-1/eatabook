<template>
  <div class="test-container">
    <h2>🍳 古法烹饪生图测试</h2>
    
    <div class="input-section">
      <el-input
        v-model="stepsText"
        type="textarea"
        :rows="6"
        placeholder="请粘贴 AI 生成的【造法】内容，例如：1. 将肉切块 2. 慢炖..."
      />
      <el-button 
        type="primary" 
        :loading="loading" 
        @click="handleStartGenerate"
        style="margin-top: 15px"
      >
        {{ loading ? '正在通过火山引擎生图中...' : '开始生成步骤图' }}
      </el-button>
    </div>

    <div v-if="resultUrls.length > 0" class="result-section">
      <h3>生成结果：</h3>
      <div class="image-grid">
        <div v-for="(url, index) in resultUrls" :key="index" class="image-item">
          <p>步骤 {{ index + 1 }}</p>
          <el-image 
            :src="url" 
            :preview-src-list="resultUrls"
            fit="cover"
            class="step-img"
          >
            <template #placeholder>
              <div class="image-slot">加载中...</div>
            </template>
            <template #error>
              <div class="image-slot">生成失败</div>
            </template>
          </el-image>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { generateCulinaryImages } from '@/api/culinaryImage';
import { ElMessage } from 'element-plus';

const stepsText = ref('');
const loading = ref(false);
const resultUrls = ref<string[]>([]);

const handleStartGenerate = async () => {
  if (!stepsText.value.trim()) {
    ElMessage.warning('请输入文字内容');
    return;
  }

  loading.value = true;
  resultUrls.value = []; // 清空上次结果

  try {
    const stepsArray = stepsText.value
      .split(/(?=步骤\d+：)/)
      .filter(s => s.trim());

    const res = await generateCulinaryImages({
      dishName: "测试菜品",
      sourceText: stepsText.value,
      stepsText: stepsArray
    });
    
    if (res.success) {
      resultUrls.value = res.imageUrls;
      ElMessage.success(res.message);
    } else {
      ElMessage.error(res.message || '生成失败');
    }
  } catch (error) {
    ElMessage.error('请求发生异常');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.test-container {
  padding: 30px;
  max-width: 900px;
  margin: 0 auto;
}
.input-section {
  margin-bottom: 30px;
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 8px;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.image-item {
  text-align: center;
  border: 1px dashed #ccc;
  padding: 10px;
  border-radius: 4px;
}
.step-img {
  width: 100%;
  height: 250px;
  border-radius: 4px;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}
</style>
