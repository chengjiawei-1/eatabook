import { createApp } from "vue";
import { createPinia } from "pinia";
import { MotionPlugin } from "@vueuse/motion";
import ElementPlus from "element-plus"; // 导入Element Plus
import "element-plus/dist/index.css"; // 导入Element Plus样式
import * as ElementPlusIconsVue from "@element-plus/icons-vue"; // 导入所有Element Plus图标

import App from "./App.vue";
import router from "./router";
import { Toaster } from "vue-sonner";
import { useAuthStore } from "./stores/auth";
import "./index.css";

const app = createApp(App);

// 保留原有Pinia配置
app.use(createPinia());

// 保留原有认证状态初始化
const authStore = useAuthStore();

// 保留原有路由和动画插件
app.use(router);
app.use(MotionPlugin);

// 注册Element Plus及所有图标
app.use(ElementPlus);
// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 保留原有Toaster组件
app.component("Toaster", Toaster);

// 挂载应用
app.mount("#app");