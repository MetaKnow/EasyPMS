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
import SystemBackup from '../components/SystemBackup.vue'
import ProjectDetail from '../components/ProjectDetail.vue'
import MaintenanceRecord from '../components/MaintenanceRecord.vue'

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
        meta: { title: '客户管理', roles: ['管理员', '公司领导', '公司领导角色', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin', '销售', 'sales', '销售总监', '销售总监角色'] }
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
        path: 'maintenance/:projectId',
        name: 'MaintenanceRecord',
        component: MaintenanceRecord,
        meta: { title: '运维记录', fullscreen: true }
      },
      {
        path: 'system/organization',
        name: 'OrganizationManagement',
        component: OrganizationUserManagement,
        meta: { title: '机构用户管理', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin'] }
      },
      {
        path: 'system/roles',
        name: 'RoleManagement',
        component: RoleManagement,
        meta: { title: '角色管理', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin'] }
      },
      {
        path: 'system/milestones',
        name: 'StandardMilestoneManagement',
        component: StandardMilestoneManagement,
        meta: { title: '标准里程碑', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin'] }
      },
      {
        path: 'system/steps',
        name: 'StandardConstructStepManagement',
        component: StandardConstructStepManagement,
        meta: { title: '标准交付步骤', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin'] }
      },
      {
        path: 'system/deliverables',
        name: 'StandardDeliverableManagement',
        component: StandardDeliverableManagement,
        meta: { title: '标准交付物', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin'] }
      },
      {
        path: 'system/products',
        name: 'BaseProductManagement',
        component: ProductManagement,
        meta: { title: '基础产品维护', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin'] }
      },
      {
        path: 'system/partners',
        name: 'ChannelDistributorManagement',
        component: ChannelDistributorManagement,
        meta: { title: '渠道管理', roles: ['管理员', '公司领导', '超级管理员', 'admin', 'leader', 'super admin', 'superadmin', '销售', '销售角色', 'sales', '销售总监', '销售总监角色'] }
      },
      {
        path: 'system/backup',
        name: 'SystemBackup',
        component: SystemBackup,
        meta: { title: '系统备份', roles: ['管理员', 'admin'] }
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
  const token = sessionStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false)
  const userInfoRaw = sessionStorage.getItem('userInfo')
  const userInfo = userInfoRaw ? JSON.parse(userInfoRaw) : null
  const roleName = userInfo && userInfo.roleName ? String(userInfo.roleName).trim() : ''
  const roles = to.meta && Array.isArray(to.meta.roles) ? to.meta.roles : null
  
  // 登录页特殊处理：允许未登录访问；已登录则跳转工作台
  if (to.path === '/') {
    if (token) {
      next('/home/dashboard')
    } else {
      next()
    }
    return
  }

  if (requiresAuth && !token) {
    next('/')
    return
  }

  if (roles && roles.length > 0) {
    const normalized = roleName ? roleName.toLowerCase() : ''
    const isSalesRole = (roleName && roleName.includes('销售')) || (normalized.includes('sales'))
    const allowed = roles.some(r => {
      const rl = String(r).toLowerCase()
      if (rl === normalized) return true
      if (rl.includes('sales') && isSalesRole) return true
      if (r === '销售' && roleName && roleName.includes('销售')) return true
      return false
    })
    if (!allowed) {
      next('/home/dashboard')
    } else {
      next()
    }
  } else {
    const lower = roleName ? roleName.toLowerCase() : ''
    const isPrivileged = (roleName === '管理员' || roleName === '公司领导' || roleName === '超级管理员' || roleName === '销售总监' || roleName === '项目总监' || lower === 'admin' || lower === 'leader' || lower === 'super admin' || lower === 'superadmin' || lower === 'sales director' || lower === 'project director')
    if (!isPrivileged) {
      const path = to.path || ''
      const ok = (
        path === '/home/dashboard' ||
        path === '/home/construction' ||
        path === '/home/maintenance' ||
        /^\/home\/project\/[\d]+$/.test(path) ||
        /^\/home\/maintenance\/[\d]+$/.test(path)
      )
      if (!ok) {
        next('/home/dashboard')
        return
      }
    }
    next()
  }
})

export default router
