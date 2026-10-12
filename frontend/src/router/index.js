import { createRouter, createWebHistory } from 'vue-router'
import About from '../views/About.vue'

// 静态路由配置
const staticRoutes = [
  // 用户端路由
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/hobbies',
    name: 'Hobbies',
    component: () => import('../views/Hobbies.vue'),
    meta: { title: '我的爱好' }
  },
  {
    path: '/post/:id',
    name: 'PostDetail',
    component: () => import('../views/PostDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/hobbies/create',
    name: 'CreateHobby',
    component: () => import('../views/CreateHobby.vue'),
    meta: { title: '创建爱好' }
  },
  {
    path: '/hobbies/:id',
    name: 'HobbyDetail',
    component: () => import('../views/HobbyDetail.vue'),
    meta: { title: '爱好详情' }
  },
  {
    path: '/hobbies/:hobbyId/articles/create',
    redirect: (to) => ({ path: `/admin/hobbies/${to.params.hobbyId}/articles/create` })
  },
  {
    path: '/hobbies/:hobbyId/articles/:articleId',
    name: 'HobbyArticleDetail',
    component: () => import('../views/HobbyArticleDetail.vue'),
    meta: { title: '文章详情' }
  },
  {
    path: '/articles',
    name: 'Articles',
    component: () => import('../views/Articles.vue'),
    meta: { title: '文章' }
  },
  {
    path: '/archive',
    name: 'Archive',
    component: () => import('../views/Archive.vue'),
    meta: { title: '文章归档' }
  },
  {
    path: '/archive/:year',
    name: 'ArchiveYear',
    component: () => import('../views/Archive.vue'),
    meta: { title: '归档' }
  },
  {
    path: '/notes',
    redirect: '/articles'
  },
  {
    path: '/notes/:id',
    name: 'NoteDetail',
    component: () => import('../views/NoteDetail.vue'),
    meta: { title: '笔记详情' }
  },
  // AI 问答已下架：旧链接进入首页
  {
    path: '/ai',
    redirect: '/'
  },
  {
    path: '/messages',
    name: 'MessageBoard',
    component: () => import('../views/MessageBoard.vue'),
    meta: { title: '留言板' }
  },
  {
    path: '/messages/:id',
    name: 'MessageDetail',
    component: () => import('../views/MessageDetail.vue'),
    meta: { title: '留言详情' }
  },
  {
    path: '/music',
    name: 'Music',
    component: () => import('../views/Music.vue'),
    meta: { title: '音乐' }
  },
  {
    path: '/about',
    name: 'About',
    // 使用同步组件，避免子目录部署时异步 chunk 路径错误导致「进了页面却没有任何请求/日志」
    component: About,
    meta: { title: '关于' }
  },
  {
    path: '/publish',
    name: 'PublishNote',
    component: () => import('../views/PublishNote.vue'),
    meta: { title: '发布笔记' }
  },
  // 管理端嵌套路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/Login.vue'),
    meta: { title: '管理端登录' }
  },
  {
    path: '/admin',
    component: () => import('../components/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('../views/admin/AdminHome.vue'),
        meta: { title: '管理端首页' }
      },
      {
        path: 'home-hero',
        name: 'AdminHomeHero',
        component: () => import('../views/admin/AdminHomeHero.vue'),
        meta: { title: '首页壁纸' }
      },
      {
        path: 'about',
        name: 'AdminAbout',
        component: () => import('@/components/AdminAboutPage.vue'),
        meta: { title: '关于页' }
      },
      {
        path: 'site',
        name: 'AdminSite',
        component: () => import('../views/admin/AdminSiteSettings.vue'),
        meta: { title: '站点图标' }
      },
      {
        path: 'music',
        name: 'AdminMusic',
        component: () => import('../views/admin/AdminMusic.vue'),
        meta: { title: '音乐管理' }
      },
      {
        path: 'hobbies',
        name: 'AdminHobbies',
        component: () => import('../views/admin/AdminHobbies.vue'),
        meta: { title: '爱好管理' }
      },
      {
        path: 'hobbies/:id/manage',
        name: 'AdminHobbyManage',
        component: () => import('../views/admin/AdminHobbyManage.vue'),
        meta: { title: '爱好专栏管理' }
      },
      {
        path: 'hobbies/:hobbyId/articles/create',
        name: 'AdminCreateHobbyArticle',
        component: () => import('../views/CreateHobbyArticle.vue'),
        meta: { title: '发布专栏文章' }
      },
      {
        path: 'notes',
        name: 'AdminNotes',
        component: () => import('../views/admin/AdminNotes.vue'),
        meta: { title: '笔记管理' }
      },
      {
        path: 'posts',
        name: 'AdminPosts',
        component: () => import('../views/admin/AdminPosts.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'posts/create',
        name: 'CreatePost',
        component: () => import('../views/admin/CreatePost.vue'),
        meta: { title: '发布文章' }
      },
      {
        path: 'posts/edit/:id',
        name: 'EditPost',
        component: () => import('../views/admin/CreatePost.vue'),
        meta: { title: '编辑文章' }
      },
      {
        path: 'comments',
        name: 'AdminComments',
        component: () => import('../views/admin/AdminComments.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('../views/admin/AdminMessages.vue'),
        meta: { title: '留言管理' }
      }
    ]
  },
  // 错误页面
  {
    path: '/401',
    name: 'Error401',
    component: () => import('../views/error/401.vue'),
    meta: { title: '授权已过期' }
  },
  {
    path: '/:pathMatch(.*)',
    name: 'Error404',
    component: () => import('../views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes
})

// 白名单路由
const allowList = ['/admin/login', '/401', '/404']
const loginPath = '/admin/login'

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 我的博客`
  }
  
  const token = localStorage.getItem('adminToken')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 需要认证的页面
    if (!token) {
      // 没有token，跳转到登录页面
      next({
        path: loginPath,
        query: {
          redirect: encodeURIComponent(to.fullPath)
        }
      })
      return
    }
    
    // 检查管理员权限
    if (to.meta.requiresAdmin && !isAdmin) {
      next({ path: '/' })
      return
    }
  } else {
    // 不需要认证的页面
    if (to.path === loginPath && token) {
      // 已登录用户访问登录页面，跳转到首页
      next({ path: '/admin' })
      return
    }
  }
  
  next()
})

export default router
