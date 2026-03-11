<template>
  <div class="dashboard-container">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="data-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="item in dataCards" :key="item.key">
        <el-card class="data-card" :body-style="{ padding: '20px' }" shadow="hover">
          <div class="card-content">
            <div class="card-info">
              <div class="card-title">{{ item.title }}</div>
              <el-statistic :value="item.value" :precision="item.precision || 0">
                <template #prefix v-if="item.prefix">
                  <span class="value-prefix">{{ item.prefix }}</span>
                </template>
              </el-statistic>
              <div class="card-footer" :class="item.trend > 0 ? 'trend-up' : item.trend < 0 ? 'trend-down' : ''">
                <el-icon v-if="item.trend > 0"><CaretTop /></el-icon>
                <el-icon v-else-if="item.trend < 0"><CaretBottom /></el-icon>
                <span>{{ item.trend > 0 ? '较昨日上涨' : item.trend < 0 ? '较昨日下降' : '与昨日持平' }}</span>
                <span v-if="item.trend !== 0">{{ Math.abs(item.trend) }}%</span>
              </div>
            </div>
            <div class="card-icon" :style="{ backgroundColor: item.color }">
              <el-icon :size="32">
                <component :is="item.icon" />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" class="quick-entry">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Grid /></el-icon>
              <span class="card-header-title">快捷入口</span>
            </div>
          </template>
          <div class="quick-links">
            <div
              class="quick-link-item"
              v-for="link in quickLinks"
              :key="link.path"
              @click="handleQuickLink(link)"
            >
              <div class="link-icon" :style="{ backgroundColor: link.bgColor }">
                <el-icon :size="24" color="#fff">
                  <component :is="link.icon" />
                </el-icon>
              </div>
              <span class="link-text">{{ link.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><DataLine /></el-icon>
              <span class="card-header-title">订单统计</span>
            </div>
          </template>
          <div class="order-stats">
            <div class="stat-item" v-for="stat in orderStats" :key="stat.label">
              <el-badge :value="stat.value" :max="99" class="stat-badge">
                <div class="stat-circle" :style="{ borderColor: stat.color }">
                  <el-icon :size="28" :color="stat.color">
                    <component :is="stat.icon" />
                  </el-icon>
                </div>
              </el-badge>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 欢迎信息 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-icon">
          <el-icon :size="48" color="#409eff"><DataBoard /></el-icon>
        </div>
        <div class="welcome-text">
          <h2>欢迎使用鞋宙管理后台</h2>
          <p>{{ greeting }}，当前时间：{{ currentTime }}</p>
        </div>
        <div class="welcome-actions">
          <el-button type="primary" @click="router.push('/order')">
            <el-icon><List /></el-icon>
            查看订单
          </el-button>
          <el-button @click="router.push('/shoe')">
            <el-icon><ShoppingBag /></el-icon>
            鞋款管理
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Money,
  List,
  User,
  Clock,
  ShoppingBag,
  DataBoard,
  Grid,
  DataLine,
  CaretTop,
  CaretBottom,
  Tickets,
  Document,
  Van,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { getBusinessData, getOrderOverview } from '@/api/workspace'

const router = useRouter()

// 当前时间
const currentTime = ref('')
let timer = null

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 数据卡片
const dataCards = ref([
  {
    key: 'turnover',
    title: '今日营业额',
    value: 0,
    prefix: '¥',
    precision: 2,
    trend: 0,
    icon: 'Money',
    color: 'linear-gradient(135deg, #409EFF 0%, #337ecc 100%)'
  },
  {
    key: 'validOrderCount',
    title: '完成订单数',
    value: 0,
    trend: 0,
    icon: 'List',
    color: 'linear-gradient(135deg, #67C23A 0%, #529b2e 100%)'
  },
  {
    key: 'newUsers',
    title: '新增用户数',
    value: 0,
    trend: 0,
    icon: 'User',
    color: 'linear-gradient(135deg, #E6A23C 0%, #b88230 100%)'
  },
  {
    key: 'waitingOrders',
    title: '待接单',
    value: 0,
    trend: 0,
    icon: 'Clock',
    color: 'linear-gradient(135deg, #F56C6C 0%, #c45656 100%)'
  }
])

// 快捷入口
const quickLinks = ref([
  { title: '鞋款管理', path: '/shoe', icon: 'ShoppingBag', bgColor: '#409eff' },
  { title: '订单管理', path: '/order', icon: 'List', bgColor: '#67c23a' },
  { title: '抽签活动', path: '/draw', icon: 'Tickets', bgColor: '#e6a23c' },
  { title: '数据报表', path: '/report', icon: 'DataLine', bgColor: '#f56c6c' }
])

// 订单统计
const orderStats = ref([
  { label: '待接单', value: 0, icon: 'Document', color: '#e6a23c' },
  { label: '已发货', value: 0, icon: 'Van', color: '#67c23a' },
  { label: '已签收', value: 0, icon: 'CircleCheck', color: '#909399' },
  { label: '已取消', value: 0, icon: 'CircleClose', color: '#f56c6c' }
])

// 快捷入口点击
function handleQuickLink(link) {
  router.push(link.path)
}

// 获取工作台数据
async function fetchWorkspaceData() {
  try {
    const businessData = await getBusinessData()
    if (businessData) {
      dataCards.value[0].value = businessData.turnover || 0
      dataCards.value[0].trend = businessData.turnoverTrend || 0
      dataCards.value[1].value = businessData.validOrderCount || 0
      dataCards.value[1].trend = businessData.validOrderCountTrend || 0
      dataCards.value[2].value = businessData.newUsers || 0
      dataCards.value[2].trend = businessData.newUsersTrend || 0
      dataCards.value[3].value = businessData.todayWaitingOrders || 0
    }

    const orderData = await getOrderOverview()
    if (orderData) {
      orderStats.value[0].value = orderData.waitingOrders || 0
      orderStats.value[1].value = orderData.deliveredOrders || 0
      orderStats.value[2].value = orderData.completedOrders || 0
      orderStats.value[3].value = orderData.cancelledOrders || 0
    }
  } catch (error) {
    console.error('获取工作台数据失败:', error)
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  fetchWorkspaceData()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0 4px;

  .data-cards {
    margin-bottom: 20px;

    .el-col {
      margin-bottom: 12px;

      @media (min-width: 768px) {
        margin-bottom: 0;
      }
    }
  }

  .data-card {
    border-radius: 12px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-4px);
    }

    .card-content {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }

    .card-info {
      flex: 1;

      :deep(.el-statistic) {
        .el-statistic__content {
          font-size: 28px;
          font-weight: 600;
          color: #303133;
        }
      }

      .value-prefix {
        font-size: 18px;
        margin-right: 4px;
      }
    }

    .card-title {
      font-size: 14px;
      color: #909399;
      margin-bottom: 12px;
    }

    .card-footer {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #909399;
      margin-top: 8px;

      &.trend-up {
        color: #67c23a;
      }

      &.trend-down {
        color: #f56c6c;
      }
    }

    .card-icon {
      width: 64px;
      height: 64px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }
  }

  .quick-entry {
    margin-bottom: 20px;

    .el-col {
      margin-bottom: 12px;

      @media (min-width: 768px) {
        margin-bottom: 0;
      }
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;

    .card-header-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .quick-links {
    display: flex;
    flex-wrap: wrap;
    padding: 8px 0;

    .quick-link-item {
      width: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16px 0;
      cursor: pointer;
      transition: all 0.3s ease;

      @media (min-width: 576px) {
        width: 25%;
      }

      &:hover {
        transform: translateY(-4px);

        .link-icon {
          transform: scale(1.1);
        }
      }

      .link-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.3s ease;
      }

      .link-text {
        margin-top: 12px;
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }
    }
  }

  .order-stats {
    display: flex;
    justify-content: space-around;
    padding: 16px 0;

    .stat-item {
      text-align: center;
      cursor: pointer;
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05);
      }

      .stat-badge {
        :deep(.el-badge__content) {
          font-size: 12px;
        }
      }

      .stat-circle {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        border: 3px solid;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        transition: all 0.3s ease;
      }

      .stat-label {
        margin-top: 12px;
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .welcome-card {
    border-radius: 12px;

    .welcome-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24px;
      gap: 16px;

      @media (min-width: 768px) {
        flex-direction: row;
        gap: 24px;
      }

      .welcome-icon {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background: linear-gradient(135deg, #e0e5ec 0%, #f5f7fa 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .welcome-text {
        flex: 1;
        text-align: center;

        @media (min-width: 768px) {
          text-align: left;
        }

        h2 {
          font-size: 22px;
          color: #303133;
          margin: 0 0 8px 0;
          font-weight: 600;
        }

        p {
          font-size: 14px;
          color: #909399;
          margin: 0;
        }
      }

      .welcome-actions {
        display: flex;
        gap: 12px;
        flex-shrink: 0;

        .el-button {
          .el-icon {
            margin-right: 6px;
          }
        }
      }
    }
  }
}
</style>
