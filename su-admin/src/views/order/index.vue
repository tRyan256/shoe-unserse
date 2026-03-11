<template>
  <div class="order-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="订单号">
          <el-input 
            v-model="queryParams.number" 
            placeholder="请输入订单号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input 
            v-model="queryParams.phone" 
            placeholder="请输入手机号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="待付款" :value="1" />
            <el-option label="待发货" :value="2" />
            <el-option label="已发货" :value="3" />
            <el-option label="运输中" :value="4" />
            <el-option label="派送中" :value="5" />
            <el-option label="已签收" :value="6" />
            <el-option label="已取消" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="number" label="订单号" min-width="180" />
        <el-table-column prop="consignee" label="收货人" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="amount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" min-width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 2" 
              type="success" 
              link 
              @click="handleShip(row)"
            >
              发货
            </el-button>
            <el-button
              v-if="row.status === 2"
              type="warning"
              link
              @click="handleReject(row)"
            >
              拒单
            </el-button>
            <el-button
              v-if="row.status === 3"
              type="success"
              link
              @click="handleInTransit(row)"
            >
              运输中
            </el-button>
            <el-button
              v-if="row.status === 4"
              type="success"
              link
              @click="handleOutForDelivery(row)"
            >
              派送中
            </el-button>
            <el-button
              v-if="row.status === 5"
              type="success"
              link
              @click="handleComplete(row)"
            >
              签收
            </el-button>
            <el-button 
              v-if="row.status === 1 || row.status === 2" 
              type="danger" 
              link 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getList"
        @current-change="getList"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="800px">
      <el-descriptions :column="2" border v-if="orderDetail">
        <el-descriptions-item label="订单号">{{ orderDetail.number }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(orderDetail.status)">
            {{ getStatusText(orderDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ orderDetail.consignee }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ orderDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ orderDetail.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount">¥{{ orderDetail.amount }}</span>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider>收货地址</el-divider>
      
      <el-descriptions :column="2" border v-if="orderDetail">
        <el-descriptions-item label="收货人">{{ orderDetail.consignee }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ orderDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.address }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>订单商品</el-divider>
      
      <el-table :data="orderDetail?.orderDetailList || []" border>
        <el-table-column prop="name" label="商品名称" min-width="200">
          <template #default="{ row }">
            <div class="goods-info">
              <el-image 
                :src="getImageSrc(row.image)" 
                :preview-src-list="[getImageSrc(row.image)]"
                fit="cover"
                class="goods-image"
              >
                <template #error>
                  <img :src="placeholderImage" class="goods-image" />
                </template>
              </el-image>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="number" label="数量" width="100" align="center" />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 物流信息 -->
      <template v-if="orderDetail && orderDetail.status >= 3 && orderDetail.logistics">
        <el-divider>物流信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="快递公司">{{ orderDetail.logistics.expressCompany }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ orderDetail.logistics.expressNo }}</el-descriptions-item>
        </el-descriptions>
        
        <el-timeline v-if="orderDetail.logisticsTraceList && orderDetail.logisticsTraceList.length > 0" class="logistics-timeline">
          <el-timeline-item
            v-for="(trace, index) in orderDetail.logisticsTraceList"
            :key="index"
            :timestamp="trace.operateTime"
            placement="top"
          >
            {{ trace.location }}{{ trace.location ? ' - ' : '' }}{{ trace.description }}
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无物流轨迹" :image-size="60" />
      </template>

      <template #footer>
        <el-button v-if="orderDetail?.status >= 3" @click="handleRefreshLogistics" :loading="logisticsLoading">
          刷新物流
        </el-button>
        <el-button
          v-if="orderDetail?.status === 2"
          type="success"
          @click="handleShip(orderDetail)"
        >
          发货
        </el-button>
        <el-button
          v-if="orderDetail?.status === 2"
          type="warning"
          @click="handleReject(orderDetail)"
        >
          拒单
        </el-button>
        <el-button
          v-if="orderDetail?.status === 3"
          type="success"
          @click="handleInTransit(orderDetail)"
        >
          运输中
        </el-button>
        <el-button
          v-if="orderDetail?.status === 4"
          type="success"
          @click="handleOutForDelivery(orderDetail)"
        >
          派送中
        </el-button>
        <el-button
          v-if="orderDetail?.status === 5"
          type="success"
          @click="handleComplete(orderDetail)"
        >
          签收
        </el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消订单"
      width="500px"
      @close="handleCancelDialogClose"
    >
      <el-form
        ref="cancelFormRef"
        :model="cancelFormData"
        :rules="cancelFormRules"
        label-width="100px"
      >
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.number" disabled />
        </el-form-item>
        <el-form-item label="取消原因" prop="cancelReason">
          <el-select 
            v-model="cancelFormData.cancelReason" 
            placeholder="请选择取消原因"
            style="width: 100%"
          >
            <el-option label="用户申请取消" value="用户申请取消" />
            <el-option label="库存不足" value="库存不足" />
            <el-option label="商品已下架" value="商品已下架" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="cancelFormData.cancelReason === '其他原因'" label="详细原因" prop="cancelDetail">
          <el-input 
            v-model="cancelFormData.cancelDetail" 
            type="textarea"
            :rows="3"
            placeholder="请输入详细原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleCancelSubmit" :loading="cancelLoading">
          确认取消订单
        </el-button>
      </template>
    </el-dialog>

    <!-- 拒单对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒单"
      width="500px"
      @close="handleRejectDialogClose"
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectFormData"
        :rules="rejectFormRules"
        label-width="100px"
      >
        <el-form-item label="订单号">
          <el-input :value="currentOrder?.number" disabled />
        </el-form-item>
        <el-form-item label="拒单原因" prop="rejectionReason">
          <el-select v-model="rejectFormData.rejectionReason" placeholder="请选择拒单原因" style="width: 100%">
            <el-option label="库存不足" value="库存不足" />
            <el-option label="地址超区" value="地址超区" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="rejectFormData.rejectionReason === '其他原因'" label="详细原因" prop="rejectDetail">
          <el-input
            v-model="rejectFormData.rejectDetail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleRejectSubmit" :loading="rejectLoading">
          确认拒单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import placeholderImage from '@/assets/placeholder.svg'
import { 
  getOrderList, 
  getOrderDetail, 
  cancelOrder,
  confirmOrder,
  rejectOrder,
  deliverOrder,
  outForDeliveryOrder,
  completeOrder
} from '@/api/order'
import { getLogisticsDetail } from '@/api/logistics'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
const cancelLoading = ref(false)
const rejectLoading = ref(false)
const logisticsLoading = ref(false)

// 查询参数
const queryParams = reactive({
  number: '',
  phone: '',
  status: null,
  dateRange: [],
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 订单详情
const detailVisible = ref(false)
const orderDetail = ref(null)

const currentOrder = ref(null)

// 取消订单对话框
const cancelDialogVisible = ref(false)
const cancelFormRef = ref(null)
const cancelFormData = reactive({
  id: null,
  cancelReason: '',
  cancelDetail: ''
})

// 取消订单表单校验规则
const cancelFormRules = {
  cancelReason: [{ required: true, message: '请选择取消原因', trigger: 'change' }],
  cancelDetail: [{ required: true, message: '请输入详细原因', trigger: 'blur' }]
}

// 拒单对话框
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectFormData = reactive({
  id: null,
  rejectionReason: '',
  rejectDetail: ''
})
const rejectFormRules = {
  rejectionReason: [{ required: true, message: '请选择拒单原因', trigger: 'change' }],
  rejectDetail: [{ required: true, message: '请输入详细原因', trigger: 'blur' }]
}

// 状态文本映射
const statusMap = {
  1: '待付款',
  2: '待发货',
  3: '已发货',
  4: '运输中',
  5: '派送中',
  6: '已签收',
  7: '已取消'
}

// 获取状态文本
function getStatusText(status) {
  return statusMap[status] || '未知'
}

// 获取状态类型
function getStatusType(status) {
  const typeMap = {
    1: 'warning',
    2: 'info',
    3: 'primary',
    4: 'primary',
    5: 'primary',
    6: 'success',
    7: 'danger'
  }
  return typeMap[status] || 'info'
}

function getImageSrc(url) {
  if (!url) return placeholderImage
  if (typeof url === 'string' && url.includes('example.com/')) return placeholderImage
  return url
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (params.dateRange && params.dateRange.length === 2) {
      params.beginTime = `${params.dateRange[0]} 00:00:00`
      params.endTime = `${params.dateRange[1]} 23:59:59`
    }
    delete params.dateRange
    
    const res = await getOrderList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleQuery() {
  queryParams.page = 1
  getList()
}

// 重置
function handleReset() {
  queryParams.number = ''
  queryParams.phone = ''
  queryParams.status = null
  queryParams.dateRange = []
  queryParams.page = 1
  getList()
}

// 查看详情
async function handleDetail(row) {
  try {
    orderDetail.value = await getOrderDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

// 发货
function handleShip(row) {
  ElMessageBox.confirm(`确定要对订单"${row.number}"执行发货吗？`, '发货确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await confirmOrder({ id: row.id })
    ElMessage.success('发货成功')
    await refreshAfterOperate(row)
  }).catch(() => {})
}

function handleInTransit(row) {
  ElMessageBox.confirm(`确定将订单"${row.number}"更新为运输中吗？`, '状态更新', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deliverOrder(row.id)
    ElMessage.success('已更新为运输中')
    await refreshAfterOperate(row)
  }).catch(() => {})
}

function handleOutForDelivery(row) {
  ElMessageBox.confirm(`确定将订单"${row.number}"更新为派送中吗？`, '状态更新', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await outForDeliveryOrder(row.id)
    ElMessage.success('已更新为派送中')
    await refreshAfterOperate(row)
  }).catch(() => {})
}

function handleComplete(row) {
  ElMessageBox.confirm(`确定将订单"${row.number}"更新为已签收吗？`, '状态更新', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await completeOrder(row.id)
    ElMessage.success('订单已签收')
    await refreshAfterOperate(row)
  }).catch(() => {})
}

// 取消订单
function handleCancel(row) {
  currentOrder.value = row
  cancelFormData.id = row.id
  cancelFormData.cancelReason = ''
  cancelFormData.cancelDetail = ''
  cancelDialogVisible.value = true
}

function handleReject(row) {
  currentOrder.value = row
  rejectFormData.id = row.id
  rejectFormData.rejectionReason = ''
  rejectFormData.rejectDetail = ''
  rejectDialogVisible.value = true
}

// 取消订单对话框关闭
function handleCancelDialogClose() {
  cancelFormRef.value?.resetFields()
  cancelFormData.id = null
  cancelFormData.cancelReason = ''
  cancelFormData.cancelDetail = ''
}

function handleRejectDialogClose() {
  rejectFormRef.value?.resetFields()
  rejectFormData.id = null
  rejectFormData.rejectionReason = ''
  rejectFormData.rejectDetail = ''
}

// 提交取消订单
async function handleCancelSubmit() {
  const valid = await cancelFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  cancelLoading.value = true
  try {
    await cancelOrder({
      id: cancelFormData.id,
      cancelReason: cancelFormData.cancelReason === '其他原因' 
        ? cancelFormData.cancelDetail 
        : cancelFormData.cancelReason
    })
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    await refreshAfterOperate(currentOrder.value)
  } catch (error) {
    console.error('取消订单失败:', error)
  } finally {
    cancelLoading.value = false
  }
}

async function handleRejectSubmit() {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    await rejectOrder({
      id: rejectFormData.id,
      rejectionReason: rejectFormData.rejectionReason === '其他原因'
        ? rejectFormData.rejectDetail
        : rejectFormData.rejectionReason
    })
    ElMessage.success('已拒单')
    rejectDialogVisible.value = false
    await refreshAfterOperate(currentOrder.value)
  } catch (error) {
    console.error('拒单失败:', error)
  } finally {
    rejectLoading.value = false
  }
}

async function handleRefreshLogistics() {
  const orderNo = orderDetail.value?.number
  if (!orderNo) return
  logisticsLoading.value = true
  try {
    const res = await getLogisticsDetail(orderNo)
    if (!orderDetail.value) return
    orderDetail.value.logistics = res?.logistics || null
    orderDetail.value.logisticsTraceList = res?.traces || []
    ElMessage.success('物流已刷新')
  } catch (e) {
    console.error('刷新物流失败:', e)
  } finally {
    logisticsLoading.value = false
  }
}

async function refreshAfterOperate(row) {
  await getList()
  if (detailVisible.value && orderDetail.value?.id) {
    try {
      orderDetail.value = await getOrderDetail(orderDetail.value.id)
    } catch (e) {}
  }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.order-container {
  .search-form {
    margin-bottom: 15px;
  }
  
  .el-pagination {
    margin-top: 20px;
  }

  .amount {
    color: #f56c6c;
    font-weight: 600;
  }

  .goods-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .goods-image {
      width: 50px;
      height: 50px;
      border-radius: 4px;
      flex-shrink: 0;
    }

    .image-placeholder {
      width: 50px;
      height: 50px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-fill-color-light);
      color: var(--el-text-color-placeholder);
      flex-shrink: 0;
    }
  }

  .logistics-timeline {
    margin-top: 20px;
    padding-left: 10px;
  }
}
</style>
