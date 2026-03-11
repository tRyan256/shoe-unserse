import {
    createRouter,
    createWebHistory
} from 'vue-router'
import {
    useUserStore
} from '@/stores/user'

const routes = [{
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: {
            title: '登录',
            requiresAuth: false
        }
    },
    {
        path: '/',
        component: () => import('@/components/layout/MainLayout.vue'),
        redirect: '/dashboard',
        children: [{
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: {
                    title: '工作台',
                    icon: 'DataBoard'
                }
            },
            {
                path: 'employee',
                name: 'Employee',
                component: () => import('@/views/employee/index.vue'),
                meta: {
                    title: '员工管理',
                    icon: 'User'
                }
            },
            {
                path: 'category',
                name: 'Category',
                component: () => import('@/views/category/index.vue'),
                meta: {
                    title: '分类管理',
                    icon: 'Menu'
                }
            },
            {
                path: 'shoe',
                name: 'Shoe',
                component: () => import('@/views/shoe/index.vue'),
                meta: {
                    title: '鞋款管理',
                    icon: 'ShoppingBag'
                }
            },
            {
                path: 'bundle',
                name: 'Bundle',
                component: () => import('@/views/bundle/index.vue'),
                meta: {
                    title: '套装管理',
                    icon: 'Box'
                }
            },
            {
                path: 'draw',
                name: 'Draw',
                component: () => import('@/views/draw/index.vue'),
                meta: {
                    title: '抽签管理',
                    icon: 'TrendCharts'
                }
            },
            {
                path: 'airdrop',
                name: 'Airdrop',
                component: () => import('@/views/airdrop/index.vue'),
                meta: {
                    title: '空投管理',
                    icon: 'Present'
                }
            },
            {
                path: 'order',
                name: 'Order',
                component: () => import('@/views/order/index.vue'),
                meta: {
                    title: '订单管理',
                    icon: 'List'
                }
            },
            {
                path: 'coupon',
                name: 'Coupon',
                component: () => import('@/views/coupon/index.vue'),
                meta: {
                    title: '优惠券管理',
                    icon: 'Ticket'
                }
            },
            {
                path: 'outlet',
                name: 'Outlet',
                component: () => import('@/views/outlet/index.vue'),
                meta: {
                    title: '门店管理',
                    icon: 'Shop'
                }
            },
            {
                path: 'warehouse',
                name: 'Warehouse',
                component: () => import('@/views/warehouse/index.vue'),
                meta: {
                    title: '仓库管理',
                    icon: 'House'
                }
            },
            {
                path: 'comment',
                name: 'Comment',
                component: () => import('@/views/comment/index.vue'),
                meta: {
                    title: '评价管理',
                    icon: 'ChatDotRound'
                }
            },
            {
                path: 'experience-post',
                name: 'ExperiencePost',
                component: () => import('@/views/experience/post.vue'),
                meta: {
                    title: '心得管理',
                    icon: 'Document'
                }
            },
            {
                path: 'report',
                name: 'Report',
                component: () => import('@/views/report/index.vue'),
                meta: {
                    title: '数据报表',
                    icon: 'DataAnalysis'
                }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
        meta: {
            title: '页面未找到'
        }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title ? `${to.meta.title} - 鞋宙管理后台` : '鞋宙管理后台'

    const userStore = useUserStore()
    const token = userStore.token

    if (to.meta.requiresAuth !== false && !token) {
        // 需要认证但没有token，跳转登录页
        next({
            name: 'Login',
            query: {
                redirect: to.fullPath
            }
        })
    } else if (to.name === 'Login' && token) {
        // 已登录访问登录页，跳转首页
        next({
            name: 'Dashboard'
        })
    } else {
        next()
    }
})

export default router