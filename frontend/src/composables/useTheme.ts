import { onMounted, ref, watch, readonly } from "vue";

type Theme = "light" | "dark";

// 全局主题状态
const globalTheme = ref<Theme>("light");

export function useTheme() {
  // 使用全局主题状态
  const theme = readonly(globalTheme);

  onMounted(() => {
    // 只在首次挂载时从 localStorage 读取主题
    if (globalTheme.value === "light") {
      const saved = (localStorage.getItem("theme") as Theme | null) ?? undefined;
      if (saved) {
        globalTheme.value = saved;
      } else {
        globalTheme.value = window.matchMedia("(prefers-color-scheme: dark)").matches
          ? "dark"
          : "light";
      }
    }
  });

  watch(
    globalTheme,
    (val) => {
      document.documentElement.classList.remove("light", "dark");
      document.documentElement.classList.add(val);
      localStorage.setItem("theme", val);
    },
    { immediate: true }
  );

  const toggleTheme = () => {
    globalTheme.value = globalTheme.value === "light" ? "dark" : "light";
  };

  return {
    theme,
    toggleTheme,
    isDark: theme.value === "dark",
  };
}

