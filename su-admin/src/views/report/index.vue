<template>
  <div class="report-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据报表</h2>
      <el-button type="primary" @click="handleExport">
        <el-icon><Download /></el-icon>
        导出报表
      </el-button>
    </div>

    <!-- 日期范围选择 -->
    <div class="filter-section">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        :shortcuts="dateShortcuts"
        @change="handleDateChange"
      />
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total-turnover">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">总营业额（全部）</div>
          <div class="stat-value">¥{{ formatNumber(totalTurnoverData) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon turnover">
          <el-icon><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">区间营业额</div>
          <div class="stat-value">¥{{ formatNumber(summaryData.totalTurnover) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon user">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">新增用户</div>
          <div class="stat-value">{{ summaryData.newUsers }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon order">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">订单总数</div>
          <div class="stat-value">{{ summaryData.totalOrders }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon complete">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-label">完成订单</div>
          <div class="stat-value">{{ summaryData.completedOrders }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 营业额趋势图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>营业额趋势</h3>
        </div>
        <div ref="turnoverChartRef" class="chart-container"></div>
      </div>

      <!-- 用户统计图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>新增用户趋势</h3>
        </div>
        <div ref="userChartRef" class="chart-container"></div>
      </div>

      <!-- 订单统计图 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>订单数量趋势</h3>
        </div>
        <div ref="orderChartRef" class="chart-container"></div>
      </div>

      <!-- 销量Top10 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>销量Top10</h3>
        </div>
        <div ref="top10ChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 营业额数据表格 -->
    <div class="table-section">
      <div class="table-header">
        <h3>营业额明细</h3>
      </div>
      <el-table :data="turnoverTableData" stripe border style="width: 100%">
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="turnover" label="营业额">
          <template #default="{ row }">
            ¥{{ formatNumber(row.turnover) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderCount" label="订单数" width="120" />
        <el-table-column prop="validOrderCount" label="完成订单数" width="120" />
        <el-table-column label="订单完成率" width="120">
          <template #default="{ row }">
            {{ row.orderCount > 0 ? ((row.validOrderCount / row.orderCount) * 100).toFixed(1) + '%' : '0%' }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 订单汇总统计 -->
    <div class="order-status-section">
      <div class="section-header">
        <h3>订单汇总统计</h3>
      </div>
      <div class="status-cards">
        <div class="status-card" v-for="item in orderStatusList" :key="item.key">
          <div class="status-count">{{ item.count }}</div>
          <div class="status-label">{{ item.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Download, Money, User, Document, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getTurnoverStatistics,
  getUserStatistics,
  getOrderStatistics,
  getTop10Statistics,
  exportReport,
  getTotalTurnover,
  getTotalValidOrderCount
} from '@/api/report'

// 日期范围
const dateRange = ref([])
const dateShortcuts = [
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(1)
      return [start, end]
    }
  }
]

// 汇总数据
const summaryData = ref({
  totalTurnover: 0,
  newUsers: 0,
  totalOrders: 0,
  completedOrders: 0
})

// 总营业额（所有已签收订单）
const totalTurnoverData = ref(0)

// 图表引用
const turnoverChartRef = ref(null)
const userChartRef = ref(null)
const orderChartRef = ref(null)
const top10ChartRef = ref(null)

// 图表实例
let turnoverChart = null
let userChart = null
let orderChart = null
let top10Chart = null

// 营业额表格数据
const turnoverTableData = ref([])

// 订单状态列表
const orderStatusList = ref([
  { key: 'total', label: '订单总数', count: 0 },
  { key: 'valid', label: '完成订单', count: 0 },
  { key: 'rate', label: '完成率', count: '0%' }
])

// 格式化数字
const formatNumber = (num) => {
  if (!num && num !== 0) return '0.00'
  return Number(num).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 初始化日期范围（默认最近7天）
const initDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
  dateRange.value = [
    formatDate(start),
    formatDate(end)
  ]
}

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 日期变化处理
const handleDateChange = () => {
  loadAllData()
}

// 加载所有数据
const loadAllData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }

  const params = {
    begin: dateRange.value[0],
    end: dateRange.value[1]
  }

  try {
    await Promise.all([
      loadTurnoverData(params),
      loadUserData(params),
      loadOrderData(params),
      loadTop10Data(params)
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

// 加载营业额数据
const loadTurnoverData = async (params) => {
  try {
    const data = await getTurnoverStatistics(params)
    rawTurnover.value = data || null
    rebuildTurnoverTable()
    updateTurnoverChart(data)
    summaryData.value.totalTurnover = sumNumberList(data?.turnoverList)
  } catch (error) {
    console.error('加载营业额数据失败:', error)
  }
}

// 加载用户数据
const loadUserData = async (params) => {
  try {
    const data = await getUserStatistics(params)
    summaryData.value.newUsers = lastNumber(data?.totalUserList)
    updateUserChart(data)
  } catch (error) {
    console.error('加载用户数据失败:', error)
  }
}

// 加载订单数据
const loadOrderData = async (params) => {
  try {
    const data = await getOrderStatistics(params)
    rawOrder.value = data || null
    summaryData.value.totalOrders = data?.totalOrderCount || 0
    summaryData.value.completedOrders = data?.validOrderCount || 0
    orderStatusList.value = [
      { key: 'total', label: '订单总数', count: data?.totalOrderCount || 0 },
      { key: 'valid', label: '完成订单', count: data?.validOrderCount || 0 },
      { key: 'rate', label: '完成率', count: `${(((data?.orderCompletionRate || 0) * 100)).toFixed(1)}%` }
    ]
    rebuildTurnoverTable()
    updateOrderChart(data)
  } catch (error) {
    console.error('加载订单数据失败:', error)
  }
}

// 加载Top10数据
const loadTop10Data = async (params) => {
  try {
    const data = await getTop10Statistics(params)
    updateTop10Chart(data)
  } catch (error) {
    console.error('加载Top10数据失败:', error)
  }
}

// 加载总营业额
const loadTotalTurnover = async () => {
  try {
    const data = await getTotalTurnover()
    totalTurnoverData.value = data || 0
  } catch (error) {
    console.error('加载总营业额失败:', error)
  }
}

// 初始化营业额图表
const initTurnoverChart = () => {
  if (!turnoverChartRef.value) return

  turnoverChart = echarts.init(turnoverChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>营业额: ¥{c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '营业额',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#409EFF'
        },
        data: []
      }
    ]
  }
  turnoverChart.setOption(option)
}

// 更新营业额图表
const updateTurnoverChart = (data) => {
  if (!turnoverChart) return
  const { dates, numbers } = parseSeries(data?.dateList, data?.turnoverList)

  turnoverChart.setOption({
    xAxis: {
      data: dates
    },
    series: [
      {
        data: numbers
      }
    ]
  })
}

// 初始化用户图表
const initUserChart = () => {
  if (!userChartRef.value) return

  userChart = echarts.init(userChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>新增用户: {c}人'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}人'
      }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#67C23A'
        },
        data: []
      }
    ]
  }
  userChart.setOption(option)
}

// 更新用户图表
const updateUserChart = (data) => {
  if (!userChart) return
  const { dates, numbers } = parseSeries(data?.dateList, data?.newUserList)

  userChart.setOption({
    xAxis: {
      data: dates
    },
    series: [
      {
        data: numbers
      }
    ]
  })
}

// 初始化订单图表
const initOrderChart = () => {
  if (!orderChartRef.value) return

  orderChart = echarts.init(orderChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['订单总数', '完成订单']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单总数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#E6A23C'
        },
        itemStyle: {
          color: '#E6A23C'
        },
        data: []
      },
      {
        name: '完成订单',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          color: '#409EFF'
        },
        itemStyle: {
          color: '#409EFF'
        },
        data: []
      }
    ]
  }
  orderChart.setOption(option)
}

// 更新订单图表
const updateOrderChart = (data) => {
  if (!orderChart) return
  const { dates, numbers: totalOrders } = parseSeries(data?.dateList, data?.orderCountList)
  const { numbers: validOrders } = parseSeries(data?.dateList, data?.validOrderCountList)

  orderChart.setOption({
    xAxis: {
      data: dates
    },
    series: [
      {
        data: totalOrders
      },
      {
        data: validOrders
      }
    ]
  })
}

// 初始化Top10图表
const initTop10Chart = () => {
  if (!top10ChartRef.value) return

  top10Chart = echarts.init(top10ChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>销量: {c}件'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}件'
      }
    },
    yAxis: {
      type: 'category',
      data: [],
      axisLabel: {
        width: 100,
        overflow: 'truncate',
        ellipsis: '...'
      }
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        barWidth: '60%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#67C23A' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        data: []
      }
    ]
  }
  top10Chart.setOption(option)
}

// 更新Top10图表
const updateTop10Chart = (data) => {
  if (!top10Chart) return
  const items = parseNameNumberList(data?.nameList, data?.numberList)
  const sortedData = [...items].sort((a, b) => b.number - a.number).slice(0, 10)
  const names = sortedData.map((item) => item.name)
  const sales = sortedData.map((item) => item.number || 0)

  top10Chart.setOption({
    yAxis: {
      data: names
    },
    series: [
      {
        data: sales
      }
    ]
  })
}

// 导出报表
const handleExport = async () => {
  try {
    const res = await exportReport()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `运营数据报表_${formatDate(new Date())}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出报表失败:', error)
  }
}

// 窗口大小变化时重绘图表
const handleResize = () => {
  turnoverChart?.resize()
  userChart?.resize()
  orderChart?.resize()
  top10Chart?.resize()
}

const rawTurnover = ref(null)
const rawOrder = ref(null)

const parseList = (value) => {
  if (value == null) return []
  const s = String(value).trim()
  if (!s) return []
  return s.split(',').map((x) => x.trim()).filter(Boolean)
}

const parseSeries = (dateList, numberList) => {
  const dates = parseList(dateList)
  const numbers = parseList(numberList).map((x) => Number(x || 0))
  while (numbers.length < dates.length) numbers.push(0)
  return { dates, numbers }
}

const sumNumberList = (numberList) => parseList(numberList).reduce((sum, x) => sum + (Number(x) || 0), 0)

const lastNumber = (numberList) => {
  const arr = parseList(numberList).map((x) => Number(x || 0))
  return arr.length ? (arr[arr.length - 1] || 0) : 0
}

const parseNameNumberList = (nameList, numberList) => {
  const names = parseList(nameList)
  const numbers = parseList(numberList).map((x) => Number(x || 0))
  return names.map((name, idx) => ({ name, number: numbers[idx] || 0 }))
}

const rebuildTurnoverTable = () => {
  const { dates, numbers: turnovers } = parseSeries(rawTurnover.value?.dateList, rawTurnover.value?.turnoverList)
  const { numbers: orderCounts } = parseSeries(rawOrder.value?.dateList, rawOrder.value?.orderCountList)
  const { numbers: validOrderCounts } = parseSeries(rawOrder.value?.dateList, rawOrder.value?.validOrderCountList)

  turnoverTableData.value = dates.map((date, idx) => ({
    date,
    turnover: turnovers[idx] || 0,
    orderCount: orderCounts[idx] || 0,
    validOrderCount: validOrderCounts[idx] || 0
  }))
}

// 初始化
onMounted(async () => {
  initDateRange()

  await nextTick()

  initTurnoverChart()
  initUserChart()
  initOrderChart()
  initTop10Chart()

  loadAllData()
  loadTotalTurnover()

  window.addEventListener('resize', handleResize)
})

// 销毁
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  turnoverChart?.dispose()
  userChart?.dispose()
  orderChart?.dispose()
  top10Chart?.dispose()
})
</script>

<style lang="scss" scoped>
.report-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
  }
}

.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fff;
  border-radius: 4px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .stat-icon {
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    margin-right: 16px;
    font-size: 28px;
    color: #fff;

    &.turnover {
      background: linear-gradient(135deg, #409EFF, #66b1ff);
    }

    &.total-turnover {
      background: linear-gradient(135deg, #F56C6C, #f78989);
    }

    &.user {
      background: linear-gradient(135deg, #67C23A, #85ce61);
    }

    &.order {
      background: linear-gradient(135deg, #E6A23C, #ebb563);
    }

    &.complete {
      background: linear-gradient(135deg, #909399, #a6a9ad);
    }
  }

  .stat-info {
    flex: 1;

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .chart-header {
    padding: 16px 20px;
    border-bottom: 1px solid #EBEEF5;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #303133;
    }
  }

  .chart-container {
    height: 320px;
    padding: 16px;
  }
}

.table-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;

  .table-header {
    padding: 16px 20px;
    border-bottom: 1px solid #EBEEF5;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #303133;
    }
  }

  :deep(.el-table) {
    .el-table__header th {
      background-color: #fafafa;
    }
  }
}

.order-status-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .section-header {
    padding: 16px 20px;
    border-bottom: 1px solid #EBEEF5;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #303133;
    }
  }
}

.status-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  padding: 20px;
}

.status-card {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background-color: #ecf5ff;
    transform: translateY(-2px);
  }

  .status-count {
    font-size: 28px;
    font-weight: 600;
    color: #409EFF;
    margin-bottom: 8px;
  }

  .status-label {
    font-size: 14px;
    color: #606266;
  }
}

@media screen and (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .status-cards {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .status-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
