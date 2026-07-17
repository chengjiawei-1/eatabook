<template>
  <div class="min-h-screen flex flex-col bg-cover bg-center bg-no-repeat" style="background-image: url('/images/bg2.png')">
    <main class="flex-grow pt-24 pb-12 flex items-center justify-center p-4">
      <div
        v-motion="{
          initial: { opacity: 0, y: 20 },
          enter: { opacity: 1, y: 0 },
        }"
        class="max-w-md w-full bg-white/95 dark:bg-slate-800/95 rounded-2xl shadow-xl p-8 border border-amber-100 dark:border-slate-700 backdrop-blur-sm"
      >
        <div class="text-center mb-6">
          <h1
            class="text-3xl font-bold text-amber-800 dark:text-amber-300 mb-2"
            style="font-family: 'Noto Serif SC', serif"
          >
            创建账号
          </h1>
          <p class="text-amber-600 dark:text-amber-400">完善信息，开启您的健康食养之旅</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <!-- 头像上传区域 -->
          <div class="flex flex-col items-center mb-4">
            <div 
              class="relative group cursor-pointer w-24 h-24 rounded-full border-2 border-dashed border-amber-300 dark:border-slate-500 hover:border-amber-500 transition-colors overflow-hidden bg-amber-50 dark:bg-slate-700 flex items-center justify-center"
              @click="triggerFileInput"
            >
              <!-- 图片预览 -->
              <img 
                v-if="previewAvatar" 
                :src="previewAvatar" 
                class="w-full h-full object-cover" 
                alt="头像预览"
              />
              <!-- 默认图标 -->
              <div v-else class="text-center text-amber-400 dark:text-slate-400">
                <i class="fa-solid fa-camera text-2xl mb-1"></i>
                <div class="text-xs">上传头像</div>
              </div>
              
              <!-- 悬浮遮罩 -->
              <div class="absolute inset-0 bg-black/30 hidden group-hover:flex items-center justify-center text-white text-xs font-medium">
                更换头像
              </div>
            </div>
            
            <!-- 隐藏的文件输入框 -->
            <input 
              type="file" 
              ref="fileInputRef" 
              class="hidden" 
              accept="image/*"
              @change="handleFileChange"
            />
            <p v-if="errors.avatarUrl" class="mt-1 text-xs text-red-500">{{ errors.avatarUrl }}</p>
          </div>

          <!-- 用户名（区分大小写） -->
          <div>
            <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">用户名</label>
            <input
              v-model="formData.username"
              type="text"
              placeholder="请输入用户名"
              :class="inputClass(errors.username)"
            />
            <p v-if="errors.username" class="mt-1 text-xs text-red-500">{{ errors.username }}</p>
          </div>

          <!-- 手机号 -->
          <div>
            <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">手机号</label>
            <input
              v-model="formData.phone"
              type="tel"
              placeholder="请输入11位手机号"
              :class="inputClass(errors.phone)"
            />
            <p v-if="errors.phone" class="mt-1 text-xs text-red-500">{{ errors.phone }}</p>
          </div>

          <!-- 籍贯与城市 组合区域 -->
          <div class="grid grid-cols-2 gap-4">
            <!-- 籍贯/省份 -->
            <div>
              <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">籍贯 (省份)</label>
              <div class="relative">
                <select
                  v-model="formData.nativePlace"
                  :class="inputClass(errors.nativePlace) + ' appearance-none'"
                >
                  <option value="" disabled>请选择省份</option>
                  <option v-for="province in provinces" :key="province" :value="province">
                    {{ province }}
                  </option>
                </select>
                <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-amber-700 dark:text-amber-400">
                  <i class="fa-solid fa-chevron-down text-xs"></i>
                </div>
              </div>
              <p v-if="errors.nativePlace" class="mt-1 text-xs text-red-500">{{ errors.nativePlace }}</p>
            </div>

            <!-- 城市 -->
            <div>
              <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">城市</label>
              <input
                v-model="formData.city"
                type="text"
                placeholder="请输入城市"
                :class="inputClass(errors.city)"
              />
              <p v-if="errors.city" class="mt-1 text-xs text-red-500">{{ errors.city }}</p>
            </div>
          </div>

          <!-- 出生日期 -->
          <div>
            <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">出生日期</label>
            <div class="relative">
              <input
                v-model="formData.birthday"
                type="date"
                :max="new Date().toISOString().split('T')[0]" 
                :class="inputClass(errors.birthday)"
              />
            </div>
            <p v-if="errors.birthday" class="mt-1 text-xs text-red-500">{{ errors.birthday }}</p>
          </div>

          <!-- 邮箱 -->
          <div>
            <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">邮箱</label>
            <input
              v-model="formData.email"
              type="email"
              placeholder="请输入邮箱"
              :class="inputClass(errors.email)"
            />
            <p v-if="errors.email" class="mt-1 text-xs text-red-500">{{ errors.email }}</p>
          </div>

          <!-- 密码 -->
          <div>
            <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">密码</label>
            <div class="relative">
              <input
                :type="showPassword ? 'text' : 'password'"
                v-model="formData.password"
                placeholder="密码 (含大小写字母和数字)"
                @keyup.enter="handleSubmit"
                class="w-full px-4 py-2 bg-amber-50 dark:bg-slate-700 border border-amber-200 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-400 font-mono"
              />
              <button
                type="button"
                class="eye-btn"
                @click="showPassword = !showPassword"
              >
                <i :class="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"></i>
              </button>
            </div>
            <p v-if="errors.password" class="mt-1 text-xs text-red-500">{{ errors.password }}</p>
          </div>

          <!-- 确认密码 -->
          <div>
            <label class="block text-sm font-medium text-amber-700 dark:text-amber-400 mb-1">确认密码</label>
            <div class="relative">
              <input
                :type="showConfirmPassword ? 'text' : 'password'"
                v-model="formData.confirmPassword"
                placeholder="请再次输入密码"
                @keyup.enter="handleSubmit"
                class="w-full px-4 py-2 bg-amber-50 dark:bg-slate-700 border border-amber-200 dark:border-slate-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-400 font-mono"
              />
              <button
                type="button"
                class="eye-btn"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <i :class="showConfirmPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"></i>
              </button>
            </div>
            <p v-if="errors.confirmPassword" class="mt-1 text-xs text-red-500">{{ errors.confirmPassword }}</p>
          </div>

          <!-- 注册唯一性错误提示（手机号 / 邮箱） -->
          <div
            v-if="errors.phone || errors.email"
            class="mb-3 rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-600"
          >
            <i class="fa-solid fa-circle-exclamation mr-1"></i>
            {{ errors.phone || errors.email }}
          </div>

          <button
            type="submit"
            :disabled="isSubmitting"
            class="w-full py-3 mt-2 bg-amber-600 hover:bg-amber-700 text-white font-medium rounded-lg transition-colors shadow-md hover:shadow-lg disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center"
          >
            <i v-if="isSubmitting" class="fa-solid fa-circle-notch fa-spin mr-2"></i>
            {{ isSubmitting ? "注册中..." : "立即注册" }}
          </button>

          <div class="text-center text-sm text-amber-700 dark:text-amber-300">
            已有账号？
            <RouterLink to="/login" class="text-amber-600 dark:text-amber-400 hover:text-amber-800 dark:hover:text-amber-300 font-medium">
              去登录
            </RouterLink>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { z } from "zod";
import { toast } from "vue-sonner";
import { useAuthStore } from "@/stores/auth";
import { uploadAvatarAPI ,registerAPI, RegisterResponse} from "@/api/user";
import { getAvatarUrl } from '@/utils/imageUtils';

// 中国省份列表
const provinces = [
  "北京市", "天津市", "河北省", "山西省", "内蒙古自治区",
  "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省",
  "浙江省", "安徽省", "福建省", "江西省", "山东省",
  "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区",
  "海南省", "重庆市", "四川省", "贵州省", "云南省",
  "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区",
  "新疆维吾尔自治区", "台湾省", "香港特别行政区", "澳门特别行政区"
];

// <script setup lang="ts"> 部分

//更新 Zod 验证模式
const registerSchema = z
  .object({
    username: z.string().min(2, "用户名至少需要2个字符").max(20, "用户名不能超过20个字符"),
    email: z.string().email("请输入有效的邮箱地址"),
    phone: z.string().regex(/^1[3-9]\d{9}$/, "请输入有效的11位手机号"),
    nativePlace: z.string().min(1, "请选择省份"),
    city: z.string().min(1, "请输入城市"), // 新增校验
    birthday: z.string().min(1, "请选择出生日期"),
    password: z.string().min(6, "密码至少需要6个字符").regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, "密码需包含大小写字母和数字"),
    confirmPassword: z.string(),
    avatarUrl: z.string().optional(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "两次输入的密码不一致",
    path: ["confirmPassword"],
  });

// 初始化数据增加 city
const formData = reactive({
  username: "",
  email: "",
  phone: "",
  nativePlace: "",
  city: "", // 新增
  birthday: "",
  password: "",
  confirmPassword: "",
  avatarUrl: "",
});
type RegisterForm = z.infer<typeof registerSchema>;

const errors = reactive<Partial<Record<keyof RegisterForm, string>>>({});
const isSubmitting = ref(false);
const showPassword = ref(false);
const showConfirmPassword = ref(false);

// 头像相关
const fileInputRef = ref<HTMLInputElement | null>(null);
const previewAvatar = ref<string>("");
const avatarFile = ref<File | null>(null);

const router = useRouter();
const auth = useAuthStore();

// 样式工具
const inputClass = (error?: string, withPadding = false) =>
  [
    "w-full",
    "px-4",
    withPadding ? "pr-10" : "",
    "py-2.5",
    "bg-amber-50 dark:bg-slate-700",
    "border",
    error ? "border-red-300 dark:border-red-700 focus:ring-red-400" : "border-amber-200 dark:border-slate-600 focus:ring-amber-400 dark:focus:ring-amber-600",
    "rounded-lg",
    "focus:outline-none",
    "focus:ring-2",
    "transition-colors",
    "text-amber-900 dark:text-slate-100 placeholder-amber-400/70 dark:placeholder-slate-500"
  ].join(" ");

// 触发文件选择
const triggerFileInput = () => {
  fileInputRef.value?.click();
};

// 处理文件选择
const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement;
  if (input.files && input.files[0]) {
    const file = input.files[0];
    
    // 验证大小 (限制 2MB)
    if (file.size > 2 * 1024 * 1024) {
      toast.error("图片大小不能超过 2MB");
      return;
    }
    avatarFile.value = file;
    // 创建本地预览
    previewAvatar.value = URL.createObjectURL(file);
    // 清除头像相关的错误信息（如果有）
    if (errors.avatarUrl) errors.avatarUrl = undefined;
  }
};
const handleSubmit = async () => {
  // 清除旧错误
  Object.keys(errors).forEach(key => delete errors[key as keyof RegisterForm]);

  try {
    // 1. 本地校验
    registerSchema.parse(formData);
    isSubmitting.value = true;
    
    // 2. 头像上传逻辑（注册时上传头像）
    if (avatarFile.value) {
      const uploadFormData = new FormData();
      uploadFormData.append('file', avatarFile.value);
      // 注册时没有userId，后端会处理这种情况
      try {
        const uploadRes = await uploadAvatarAPI(uploadFormData);
        if (uploadRes && uploadRes.code === 200 && uploadRes.url) {
          formData.avatarUrl = getAvatarUrl(uploadRes.url);  // 使用统一的图片处理函数
        } else {
          toast.error(uploadRes?.msg || "头像上传失败");
          isSubmitting.value = false;
          return;
        }
      } catch (uploadErr) {
        console.error("头像上传失败:", uploadErr);
        toast.error("头像上传失败，请重试");
        isSubmitting.value = false;
        return; 
      }
    }

    // 构建数据：增加 city 字段，对应后端 Entity
    const registerData = {
      username: formData.username,
      email: formData.email,
      password: formData.password,
      nativePlace: formData.nativePlace,
      city: formData.city, // 发送城市数据
      phone: formData.phone,
      avatarUrl: formData.avatarUrl,
      birthday: formData.birthday, 
    };

    // 调用注册API
    const response: RegisterResponse = await registerAPI(registerData);
    
    if (response.code === "200") {
      toast.success("注册成功，请登录！");
      router.push("/login");
    } else if (response.code === "400") {
      // 处理唯一性错误
      if (response.msg.includes('用户名')) {
        errors.username = '该用户名已被使用，请选择其他用户名';
      } else if (response.msg.includes('手机号')) {
        errors.phone = '该手机号已注册';
      } else if (response.msg.includes('邮箱')) {
        errors.email = '该邮箱已注册';
      } else {
        toast.error(response.msg || "注册失败");
      }
    } else {
      toast.error(response.msg || "注册失败，请稍后再试");
    }
    
  } catch (err: any) {
    // Zod 本地校验错误
    if (err instanceof z.ZodError) {
      const map: Partial<Record<keyof RegisterForm, string>> = {};
      err.errors.forEach((e) => {
        const field = e.path[0] as keyof RegisterForm;
        map[field] = e.message;
      });
      Object.assign(errors, map);
      toast.error("请检查表单填写是否正确");
      return;
    }

    // 网络错误或其他异常
    console.error("注册错误:", err);
    toast.error("网络错误，请稍后再试");
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.eye-btn {
  @apply absolute right-3 top-1/2 -translate-y-1/2 text-amber-500 dark:text-amber-400 hover:text-amber-700 dark:hover:text-amber-300;
}

/* 让密码圆点变粗 */
input[type="password"] {
  font-weight: 900 !important;
  font-size: 18px !important;
  line-height: 1 !important;
}

/* 保持 placeholder 字体大小和粗细正常 */
input[type="password"]::placeholder {
  font-size: 1rem !important;
  font-weight: normal !important;
}
</style>