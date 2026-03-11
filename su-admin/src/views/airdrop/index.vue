<template>
  <div class="airdrop-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="活动标题">
          <el-input 
            v-model="queryParams.title" 
            placeholder="请输入活动标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
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

      <!-- 操作按钮 -->
      <div class="table-operations">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增活动
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="title" label="活动标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="couponId" label="关联优惠券" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <template v-if="getCouponById(row.couponId)">
            <div class="coupon-info">
              <el-tag type="success" size="small">
                {{ getCouponTypeText(getCouponById(row.couponId).type) }}
              </el-tag>
              <span class="coupon-name">{{ getCouponById(row.couponId).name }}</span>
            </div>
            </template>
            <template v-else>
              <span>{{ row.couponId }}</span>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="优惠信息" width="140">
          <template #default="{ row }">
            <span class="price">{{ formatCouponValue(getCouponById(row.couponId)) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总库存" width="100">
          <template #default="{ row }">
            <span>{{ row.totalCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remainCount" label="剩余" width="90">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.remainCount)">{{ row.remainCount ?? 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已领取" width="90">
          <template #default="{ row }">
            <span>{{ getClaimedCount(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="活动时间" min-width="180">
          <template #default="{ row }">
            <div class="time-info">
              <div>开始: {{ row.startTime }}</div>
              <div>结束: {{ row.endTime }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              link 
              @click="handleCancel(row)" 
              :disabled="!canCancel(row)"
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

    <!-- 新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新增活动"
      width="600px"
      @close="handleDialogClose"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入活动标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="关联优惠券" prop="couponId">
          <el-select 
            v-model="formData.couponId" 
            placeholder="请选择优惠券" 
            style="width: 100%;"
            filterable
            @change="handleCouponChange"
          >
            <el-option 
              v-for="item in couponOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id"
            >
              <div class="coupon-option">
                <span>{{ item.name }}</span>
                <span class="coupon-value">¥{{ item.value }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="优惠券信息" v-if="selectedCoupon">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="类型">{{ getCouponTypeText(selectedCoupon.type) }}</el-descriptions-item>
            <el-descriptions-item label="优惠">{{ formatCouponValue(selectedCoupon) }}</el-descriptions-item>
            <el-descriptions-item label="使用门槛">{{ selectedCoupon.minAmount > 0 ? `满¥${selectedCoupon.minAmount}可用` : '无门槛' }}</el-descriptions-item>
            <el-descriptions-item label="有效期">{{ selectedCoupon.startTime }} ~ {{ selectedCoupon.endTime }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="总库存" prop="totalCount">
          <el-input-number 
            v-model="formData.totalCount" 
            :min="1"
            :max="99999"
            placeholder="请输入总库存"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  getAirdropList, 
  addAirdrop, 
  cancelAirdrop,
  getCouponOptions
} from '@/api/airdrop'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 查询参数
const queryParams = reactive({
  title: '',
  status: '',
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 优惠券选项
const couponOptions = ref([])

// 选中的优惠券
const selectedCoupon = computed(() => {
  if (!formData.couponId) return null
  return couponOptions.value.find(item => item.id === formData.couponId)
})

// 对话框
const dialogVisible = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  title: '',
  couponId: null,
  totalCount: null,
  timeRange: []
})

// 表单校验规则
const formRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  couponId: [
    { required: true, message: '请选择关联优惠券', trigger: 'change' }
  ],
  totalCount: [
    { required: true, message: '请输入总库存', trigger: 'blur' }
  ],
  timeRange: [
    { required: true, message: '请选择活动时间', trigger: 'change' }
  ]
}

// 优惠券类型映射
const couponTypeMap = {
  1: '满减券',
  2: '折扣券',
  3: '现金券'
}

// 获取优惠券类型文本
function getCouponTypeText(type) {
  return couponTypeMap[type] || '未知'
}

function getCouponById(couponId) {
  if (!couponId) return null
  return couponOptions.value.find(item => item.id === couponId) || null
}

function formatCouponValue(coupon) {
  if (!coupon) return '-'
  if (coupon.type === 2) {
    return coupon.value != null ? `${Number(coupon.value) * 10}折` : '-'
  }
  return coupon.value != null ? `¥${coupon.value}` : '-'
}

function getClaimedCount(row) {
  if (!row) return 0
  const totalCount = row.totalCount ?? 0
  const remainCount = row.remainCount ?? 0
  const claimed = totalCount - remainCount
  return claimed < 0 ? 0 : claimed
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const params = {
      page: queryParams.page,
      pageSize: queryParams.pageSize,
      title: queryParams.title || undefined,
      status: queryParams.status === '' ? undefined : queryParams.status
    }
    const res = await getAirdropList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取活动列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载优惠券选项
async function loadCouponOptions() {
  try {
    const res = await getCouponOptions({ page: 1, pageSize: 1000 })
    couponOptions.value = res.records || []
  } catch (error) {
    console.error('获取优惠券选项失败:', error)
    couponOptions.value = []
  }
}

// 搜索
function handleQuery() {
  queryParams.page = 1
  getList()
}

// 重置
function handleReset() {
  queryParams.title = ''
  queryParams.status = ''
  queryParams.page = 1
  getList()
}

// 优惠券选择变更
function handleCouponChange(val) {
  // 选择优惠券后可以做一些额外处理
}

// 新增
function handleAdd() {
  dialogVisible.value = true
}

// 取消活动
function handleCancel(row) {
  ElMessageBox.confirm(`确定要取消活动"${row.title}"吗？取消后无法恢复。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cancelAirdrop(row.id)
    ElMessage.success('活动已取消')
    getList()
  }).catch(() => {})
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const submitData = {
    title: formData.title,
    couponId: formData.couponId,
    totalCount: formData.totalCount,
    remainCount: formData.totalCount,
    startTime: formData.timeRange[0],
    endTime: formData.timeRange[1]
  }

  submitLoading.value = true
  try {
    await addAirdrop(submitData)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 对话框关闭
function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, {
    title: '',
    couponId: null,
    totalCount: null,
    timeRange: []
  })
}

// 获取库存标签类型
function getStockTagType(stock) {
  if (!stock || stock === 0) return 'danger'
  if (stock < 10) return 'warning'
  return 'success'
}

// 获取状态标签类型
function getStatusTagType(status) {
  const typeMap = {
    0: 'info',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
function getStatusText(status) {
  const textMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束',
    3: '已取消'
  }
  return textMap[status] || '未知'
}

function parseTime(value) {
  if (!value) return null
  const normalized = String(value).replace(/-/g, '/')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

function canCancel(row) {
  if (!row || row.status !== 0) return false
  const start = parseTime(row.startTime)
  if (!start) return true
  return start.getTime() > Date.now()
}

onMounted(() => {
  getList()
  loadCouponOptions()
})
</script>

<style lang="scss" scoped>
.airdrop-container {
  .search-form {
    margin-bottom: 15px;
  }
  
  .table-operations {
    margin-bottom: 15px;
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .price {
    color: #f56c6c;
    font-weight: 600;
  }

  .coupon-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .coupon-name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .time-info {
    font-size: 12px;
    color: #606266;
    line-height: 1.6;

    div {
      white-space: nowrap;
    }
  }

  .coupon-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;

    .coupon-value {
      color: #f56c6c;
      font-weight: 600;
    }
  }
}
</style>
