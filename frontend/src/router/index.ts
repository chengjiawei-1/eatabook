import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import AdminPage from '@/pages/AdminPage.vue';

const routes: RouteRecordRaw[] = [
  // 公开页面
  { path: "/", component: () => import("@/pages/Home.vue") },
  { path: "/books", component: () => import("@/pages/BooksPage.vue") },
  { path: "/books/:id", component: () => import("@/pages/BookDetailPage.vue") },
  { path: "/recipes", component: () => import("@/pages/RecipesPage.vue") },
  { path: "/dishes/:id", component: () => import("@/pages/DishDetailPage.vue") },
  { path: "/forum", component: () => import("@/pages/ForumPage.vue") },
  { path: "/login", component: () => import("@/pages/LoginPage.vue") },
  { path: "/register", component: () => import("@/pages/RegisterPage.vue") },
  { path: "/profile/edit", component: () => import("@/pages/EditProfilePage.vue") },
  { path: "/test", component: () => import("@/pages/PhysiqueTestPage.vue") },
  { path: "/contact", component: () => import("@/pages/CallUsPage.vue") },
  { path: "/scroll-animation", component: () => import("@/pages/ScrollAnimationPage.vue") },
  
  // 用户主页
  { 
    path: "/user/:userId", 
    component: () => import("@/pages/UserProfilePage.vue"),
    meta: { 
      title: '用户主页'
    }
  },
  // 需要登录权限的页面 
  { 
    path: "/profile/sidebar", 
    component: () => import("@/pages/ProfileSidebarLayout.vue"),
    meta: { 
      title: '个人中心',
      requiresAuth: true 
    }
  },
  { 
    path: "/my-profile", 
    component: () => import("@/pages/ProfileSidebarLayout.vue"),
    meta: { requiresAuth: true }
  },
  { 
    path: '/edit-profile', 
    component: () => import('@/pages/EditProfilePage.vue'),
    meta: { requiresAuth: true }
  }, 
  { 
    path: "/physique-test", 
    component: () => import("@/pages/PhysiqueTestPage.vue"),
    meta: { requiresAuth: true }
  },
  { 
    path: "/knowledge-graph", 
    component: () => import("@/pages/GraphPage.vue"),
    meta: { requiresAuth: true }
  },
  { 
    path: "/diet-checkin", 
    component: () => import("@/pages/DietCheckinPage.vue"),
    meta: { requiresAuth: true }
  },
  { 
    path: '/forum-post-detail/:id',
    component: () => import('@/pages/ForumPostDetail.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: "/notifications", 
    component: () => import("@/pages/NotificationsPage.vue"), 
    meta: { 
      title: '消息中心',
      requiresAuth: true 
    }
  },
  {
    path: '/test-culinary', // 这就是你在浏览器地址栏输入的地址
    name: 'CulinaryTest',
    component: () => import('@/pages/CulinaryTest.vue') // 确保文件路径正确
  },
  // 管理员页面
  {
    path: '/admin',
    name: 'Admin',
    component: AdminPage,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/pages/AdminDashboardPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/pages/AdminUserPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'books',
        name: 'AdminBooks',
        component: () => import('@/pages/AdminBooksPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'dishes',
        name: 'AdminDishes',
        component: () => import('@/pages/AdminDishPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'ingredients',
        name: 'AdminIngredients',
        component: () => import('@/pages/AdminIngredientsPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'dish-ingredient-rel',
        name: 'AdminDishIngredientRel',
        component: () => import('@/pages/AdminDishIngredientRelPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'forum',
        name: 'AdminForum',
        component: () => import('@/pages/AdminForumPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('@/pages/AdminMessagesPage.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'feedback',
        name: 'AdminFeedback',
        component: () => import('@/pages/AdminFeedback.vue'),
        meta: {
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 古籍食养`;
  } else if (to.path === '/') {
    document.title = '古籍食养';
  } else {
    document.title = '古籍食养';
  }
  
  // 检查目标路由是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 如果未登录，跳转到登录页，并记录当前想去的页面
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`);
  } 
  // 检查目标路由是否需要管理员权限
  else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    // 如果不是管理员，跳转到首页并显示提示
    next('/?error=unauthorized');
  }
  else {
    next();
  }
});

export default router;