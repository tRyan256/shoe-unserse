 <template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'is-collapsed': isCollapsed }">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
        <span v-show="!isCollapsed" class="logo-text">鞋宙管理后台</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item :index="'/' + route.path">
            <el-icon>
              <component :is="route.meta?.icon" />
            </el-icon>
            <template #title>{{ route.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 头部 -->
      <header class="header">
        <div class="header-left">
          <el-icon 
            class="collapse-btn" 
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
              {{ userStore.userName ? userStore.userName.charAt(0).toUpperCase() : 'A' }}
            </el-avatar>
              <span class="user-name">{{ userStore.userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 面包屑
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

// 菜单路由
const menuRoutes = computed(() => {
  const mainRoute = router.options.routes.find(r => r.path === '/')
  return mainRoute?.children || []
})

// 切换折叠
function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
}

// 下拉菜单命令
function handleCommand(command) {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中')
      break
    case 'password':
      ElMessage.info('修改密码功能开发中')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push({ name: 'Login' })
    ElMessage.success('退出成功')
  }).catch(() => {})
}
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  width: 100%;
  height: 100vh;
}

.sidebar {
  width: $sidebar-width;
  height: 100%;
  background-color: $sidebar-bg-color;
  transition: width $transition-duration;
  overflow: hidden;
  
  &.is-collapsed {
    width: $sidebar-collapsed-width;
    
    .logo-text {
      display: none;
    }
  }
}

.logo {
  display: flex;
  align-items: center;
  height: $header-height;
  padding: 0 15px;
  background-color: darken($sidebar-bg-color, 5%);
  
  .logo-img {
    width: 32px;
    height: 32px;
  }
  
  .logo-text {
    margin-left: 10px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - #{$header-height});

  &:not(.el-menu--collapse) {
    width: $sidebar-width;
  }

  // 缩小菜单项的上下边距
  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    margin: 2px 0;

    .el-icon {
      font-size: 18px;
    }
  }

  // 折叠状态下的菜单项
  :deep(.el-menu--collapse) {
    .el-menu-item {
      height: 44px;
      line-height: 44px;
      margin: 2px 0;
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: $header-height;
  padding: 0 20px;
  background-color: $header-bg-color;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  
  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    margin-right: 15px;
    
    &:hover {
      color: $primary-color;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  
  .user-avatar {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 14px;
    font-weight: 600;
  }
  
  .user-name {
    margin: 0 8px;
    font-size: 14px;
  }
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background-color: $content-bg-color;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
