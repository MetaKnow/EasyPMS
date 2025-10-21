import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../components/HomePage.vue'
import LoginPage from '../components/LoginPage.vue'
import Dashboard from '../components/Dashboard.vue'
import CustomerManagement from '../components/CustomerManagement.vue'
import ConstructionProjectManagement from '../components/ConstructionProjectManagement.vue'
import MaintenanceProjectManagement from '../components/MaintenanceProjectManagement.vue'
import OrganizationUserManagement from '../components/OrganizationUserManagement.vue'
import RoleManagement from '../components/RoleManagement.vue'
import StandardMilestoneManagement from '../components/StandardMilestoneManagement.vue'
import StandardConstructStepManagement from '../components/StandardConstructStepManagement.vue'
import StandardDeliverableManagement from '../components/StandardDeliverableManagement.vue'
import ProductManagement from '../components/ProductManagement.vue'
import ChannelDistributorManagement from '../components/ChannelDistributorManagement.vue'
import ProjectDetail from '../components/ProjectDetail.vue'

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
        path: 'project/:projectId',
        name: 'ProjectDetail',
        component: ProjectDetail,
        meta: { title: '项目详情', fullscreen: true }
      },
      {
        path: 'maintenance',
        name: 'MaintenanceProjects',
        component: MaintenanceProjectManagement,
        meta: { title: '运维项目管理' }
      },
      {
        path: 'system/organization',
        name: 'OrganizationManagement',
        component: OrganizationUserManagement,
        meta: { title: '机构用户管理' }
      },
      {
        path: 'system/roles',
        name: 'RoleManagement',
        component: RoleManagement,
        meta: { title: '角色管理' }
      },
      {
        path: 'system/milestones',
        name: 'StandardMilestoneManagement',
        component: StandardMilestoneManagement,
        meta: { title: '标准里程碑' }
      },
      {
        path: 'system/steps',
        name: 'StandardConstructStepManagement',
        component: StandardConstructStepManagement,
        meta: { title: '标准交付步骤' }
      },
      {
        path: 'system/deliverables',
        name: 'StandardDeliverableManagement',
        component: StandardDeliverableManagement,
        meta: { title: '标准交付物' }
      },
      {
        path: 'system/products',
        name: 'BaseProductManagement',
        component: ProductManagement,
        meta: { title: '基础产品维护' }
      },
      {
        path: 'system/partners',
        name: 'ChannelDistributorManagement',
        component: ChannelDistributorManagement,
        meta: { title: '渠道商维护' }
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