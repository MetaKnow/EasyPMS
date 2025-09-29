import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../components/HomePage.vue'
import LoginPage from '../components/LoginPage.vue'
import Dashboard from '../components/Dashboard.vue'
import CustomerManagement from '../components/CustomerManagement.vue'
import ConstructionProjectManagement from '../components/ConstructionProjectManagement.vue'
import MaintenanceProjectManagement from '../components/MaintenanceProjectManagement.vue'
import OrganizationUserManagement from '../components/OrganizationUserManagement.vue'
import RoleManagement from '../components/RoleManagement.vue'

/**
 * 路由配置
 */
const routes = [
  {
    path: '/',
    name: 'Login',
    component: LoginPage,
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    component: HomePage,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/home/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '工作台' }
      },
      {
        path: 'customers',
        name: 'CustomerManagement',
        component: CustomerManagement,
        meta: { title: '客户管理' }
      },
      {
        path: 'construction',
        name: 'ConstructionProjects',
        component: ConstructionProjectManagement,
        meta: { title: '在建项目管理' }
      },
      {
        path: 'maintenance',
        name: 'MaintenanceProjects',
        component: MaintenanceProjectManagement,
        meta: { title: '运维项目管理' }
      },
      {
        path: 'organization',
        name: 'OrganizationManagement',
        component: OrganizationUserManagement,
        meta: { title: '机构用户管理' }
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: RoleManagement,
        meta: { title: '角色管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫 - 检查登录状态
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  
  if (requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页（根路径）
    next('/')
  } else if (to.path === '/' && token) {
    // 已登录但访问登录页，跳转到工作台
    next('/home/dashboard')
  } else {
    next()
  }
})

export default router