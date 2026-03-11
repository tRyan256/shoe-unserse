<template>
  <div class="draw-container">
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
        <el-form-item label="抽签目标">
          <el-select v-model="queryParams.targetType" placeholder="请选择类型" clearable style="width: 120px;">
            <el-option label="鞋款" :value="1" />
            <el-option label="组合包" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已开奖" :value="2" />
            <el-option label="已取消" :value="3" />
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
        <el-table-column prop="targetName" label="抽签目标" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="target-info">
              <el-tag :type="row.targetType === 1 ? 'primary' : 'success'" size="small">
                {{ row.targetType === 1 ? '鞋款' : '组合包' }}
              </el-tag>
              <span class="target-name">
                {{ row.targetType === 1 && row.skuColorName ? `${row.targetName} - ${row.skuColorName}` : row.targetName }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="抽签价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="总库存" width="100">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.totalStock)">{{ row.totalStock || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="participantCount" label="参与人数" width="100">
          <template #default="{ row }">
            <span>{{ row.participantCount || 0 }}/{{ row.maxParticipants || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="winnerCount" label="中奖人数" width="90" />
        <el-table-column prop="description" label="活动说明" min-width="200" show-overflow-tooltip />
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
              <div>开奖: {{ row.drawTime }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="success" 
              link 
              @click="handleManualDraw(row)" 
              :disabled="row.status !== 1"
            >
              手动开奖
            </el-button>
            <el-button 
              type="warning" 
              link 
              @click="handleCancel(row)" 
              :disabled="!canCancel(row)"
            >
              取消
            </el-button>
            <el-button 
              type="info" 
              link 
              @click="handleViewWinners(row)"
              :disabled="row.status !== 2"
            >
              中奖名单
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
      width="700px"
      @close="handleDialogClose"
      :append-to-body="true"
      destroy-on-close
      class="draw-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入活动标题" maxlength="100" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抽签目标类型" prop="targetType">
              <el-select 
                v-model="formData.targetType" 
                placeholder="请选择类型" 
                style="width: 100%;"
                @change="handleTargetTypeChange"
              >
                <el-option label="鞋款" :value="1" />
                <el-option label="组合包" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <template v-if="formData.targetType === 1">
            <el-col :span="12">
              <el-form-item label="选择SPU" prop="spuId">
                <el-select 
                  v-model="formData.spuId" 
                  placeholder="请选择SPU" 
                  style="width: 100%;"
                  filterable
                  @change="handleSpuChange"
                >
                  <el-option 
                    v-for="item in spuOptions" 
                    :key="item.id" 
                    :label="item.name" 
                    :value="item.id"
                  >
                    <div class="target-option">
                      <span class="target-option-name">{{ item.name }}</span>
                      <el-tag 
                        :type="item.status === 1 ? 'success' : 'info'" 
                        size="small"
                        class="target-option-status"
                      >
                        {{ item.status === 1 ? '上架' : '下架' }}
                      </el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </template>
          <template v-else-if="formData.targetType === 2">
            <el-col :span="12">
              <el-form-item label="选择组合包" prop="bundleId">
                <el-select 
                  v-model="formData.bundleId" 
                  placeholder="请选择组合包" 
                  style="width: 100%;"
                  filterable
                >
                  <el-option 
                    v-for="item in targetOptions" 
                    :key="item.id" 
                    :label="item.name" 
                    :value="item.id"
                  >
                    <div class="target-option">
                      <span class="target-option-name">{{ item.name }}</span>
                      <el-tag 
                        :type="item.status === 1 ? 'success' : 'info'" 
                        size="small"
                        class="target-option-status"
                      >
                        {{ item.status === 1 ? '启用' : '未启用' }}
                      </el-tag>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </template>
        </el-row>
        <el-row :gutter="20" v-if="formData.targetType === 1 && formData.spuId">
          <el-col :span="12">
            <el-form-item label="选择SKU" prop="skuId">
              <el-select 
                v-model="formData.skuId" 
                placeholder="请选择SKU(颜色款式)" 
                style="width: 100%;"
                filterable
              >
                <el-option 
                  v-for="item in skuOptions" 
                  :key="item.id" 
                  :label="`${item.colorName} - ¥${item.price}`" 
                  :value="item.id"
                >
                  <div class="sku-option">
                    <el-image 
                      :src="item.image" 
                      fit="cover"
                      style="width: 32px; height: 32px; border-radius: 4px; margin-right: 8px;"
                    >
                      <template #error>
                        <div class="image-placeholder-small">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <span>{{ item.colorName }}</span>
                    <span class="sku-price">¥{{ item.price }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="抽签价格" prop="price">
              <el-input-number 
                v-model="formData.price" 
                :min="0" 
                :precision="2"
                :step="10"
                placeholder="请输入价格"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总库存" prop="stock">
              <el-input-number 
                v-model="formData.stock" 
                :min="1"
                :max="99999"
                placeholder="请输入总库存"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大参与人数" prop="maxParticipants">
              <el-input-number 
                v-model="formData.maxParticipants" 
                :min="1"
                :max="999999"
                placeholder="请输入最大参与人数"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中奖人数" prop="winnerCount">
              <el-input-number 
                v-model="formData.winnerCount" 
                :min="1"
                :max="formData.stock || 99999"
                placeholder="请输入中奖人数"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            time-format="HH:mm"
            class="draw-time-picker"
            popper-class="draw-date-picker"
            placement="top-start"
            :popper-options="datePickerPopperOptions"
            teleported
          />
        </el-form-item>
        <el-form-item label="活动说明" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入活动说明"
            maxlength="500"
            show-word-limit
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

    <!-- 中奖名单弹窗 -->
    <el-dialog
      v-model="winnerDialogVisible"
      title="中奖名单"
      width="800px"
      destroy-on-close
    >
      <el-table :data="winnerList" v-loading="winnerLoading" border stripe max-height="400">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="userName" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="userPhone" label="手机号" width="130" />
        <el-table-column prop="prizeName" label="奖品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orderNo" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.orderNo ? 'success' : 'warning'">
              {{ row.orderNo ? '已生成订单' : '待领取' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="winnerParams.page"
        v-model:page-size="winnerParams.pageSize"
        :total="winnerTotal"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getWinnerListData"
        @current-change="getWinnerListData"
        style="margin-top: 15px; justify-content: flex-end;"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  getDrawList, 
  addDraw, 
  cancelDraw,
  manualDraw,
  getWinnerList,
  getSpuOptions,
  getSkuOptionsBySpuId,
  getBundleOptions
} from '@/api/draw'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Picture } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const winnerLoading = ref(false)

// 查询参数
const queryParams = reactive({
  title: '',
  targetType: null,
  status: null,
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 目标选项
const spuOptions = ref([])
const skuOptions = ref([])
const targetOptions = ref([])

// 对话框
const dialogVisible = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  title: '',
  targetType: null,
  spuId: null,
  skuId: null,
  bundleId: null,
  price: null,
  stock: null,
  maxParticipants: null,
  winnerCount: null,
  timeRange: [],
  description: ''
})

// 表单校验规则
const formRules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  targetType: [
    { required: true, message: '请选择抽签目标类型', trigger: 'change' }
  ],
  spuId: [
    { 
      validator: (rule, value, callback) => {
        if (formData.targetType === 1 && !value) {
          callback(new Error('请选择SPU'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  skuId: [
    { 
      validator: (rule, value, callback) => {
        if (formData.targetType === 1 && !value) {
          callback(new Error('请选择SKU'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  bundleId: [
    { 
      validator: (rule, value, callback) => {
        if (formData.targetType === 2 && !value) {
          callback(new Error('请选择组合包'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  price: [
    { required: true, message: '请输入抽签价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入总库存', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' }
  ],
  winnerCount: [
    { required: true, message: '请输入中奖人数', trigger: 'blur' }
  ],
  timeRange: [
    { required: true, message: '请选择活动时间', trigger: 'change' }
  ]
}

// 中奖名单弹窗
const winnerDialogVisible = ref(false)
const winnerList = ref([])
const winnerTotal = ref(0)
const winnerParams = reactive({
  drawId: null,
  page: 1,
  pageSize: 10
})

// 获取列表
async function getList() {
  loading.value = true
  try {
    const res = await getDrawList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取活动列表失败:', error)
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
  queryParams.title = ''
  queryParams.targetType = null
  queryParams.status = null
  queryParams.page = 1
  getList()
}

// 加载SPU选项
async function loadSpuOptions() {
  try {
    spuOptions.value = await getSpuOptions()
  } catch (error) {
    console.error('获取SPU选项失败:', error)
    spuOptions.value = []
  }
}

// 根据SPU加载SKU选项
async function loadSkuOptions(spuId) {
  if (!spuId) {
    skuOptions.value = []
    return
  }
  try {
    skuOptions.value = await getSkuOptionsBySpuId(spuId)
  } catch (error) {
    console.error('获取SKU选项失败:', error)
    skuOptions.value = []
  }
}

// 加载组合包选项
async function loadBundleOptions() {
  try {
    targetOptions.value = await getBundleOptions()
  } catch (error) {
    console.error('获取组合包选项失败:', error)
    targetOptions.value = []
  }
}

// 目标类型变更
function handleTargetTypeChange(val) {
  formData.spuId = null
  formData.skuId = null
  formData.bundleId = null
  skuOptions.value = []
  targetOptions.value = []
  if (val === 1) {
    loadSpuOptions()
  } else if (val === 2) {
    loadBundleOptions()
  }
}

// SPU变更时加载对应SKU
async function handleSpuChange(spuId) {
  formData.skuId = null
  if (spuId) {
    await loadSkuOptions(spuId)
  } else {
    skuOptions.value = []
  }
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
    await cancelDraw(row.id)
    ElMessage.success('活动已取消')
    getList()
  }).catch(() => {})
}

// 手动开奖
function handleManualDraw(row) {
  ElMessageBox.confirm(
    `确定要对活动"${row.title}"进行手动开奖吗？开奖后将随机抽取${row.winnerCount}名中奖者。`,
    '开奖确认',
    {
      confirmButtonText: '确定开奖',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    await manualDraw(row.id)
    ElMessage.success('开奖成功')
    getList()
  }).catch(() => {})
}

// 查看中奖名单
async function handleViewWinners(row) {
  winnerParams.drawId = row.id
  winnerParams.page = 1
  winnerDialogVisible.value = true
  await getWinnerListData()
}

// 获取中奖名单数据
async function getWinnerListData() {
  winnerLoading.value = true
  try {
    const res = await getWinnerList(winnerParams.drawId)
    const allData = res || []
    winnerTotal.value = allData.length
    
    // 前端分页
    const start = (winnerParams.page - 1) * winnerParams.pageSize
    const end = start + winnerParams.pageSize
    winnerList.value = allData.slice(start, end)
  } catch (error) {
    console.error('获取中奖名单失败:', error)
  } finally {
    winnerLoading.value = false
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 校验中奖人数不能超过库存
  if (formData.winnerCount > formData.stock) {
    ElMessage.warning('中奖人数不能超过总库存')
    return
  }

  const normalizeDateTime = (v) => {
    if (typeof v !== 'string') return v
    if (v.length >= 19 && v[16] === ':' && v[13] === ':') return v.slice(0, 16)
    return v
  }

  const submitData = {
    title: formData.title,
    targetType: formData.targetType,
    spuId: formData.targetType === 1 ? formData.spuId : null,
    skuId: formData.targetType === 1 ? formData.skuId : null,
    bundleId: formData.targetType === 2 ? formData.bundleId : null,
    price: formData.price,
    totalStock: formData.stock,
    maxParticipants: formData.maxParticipants,
    winnerCount: formData.winnerCount,
    startTime: normalizeDateTime(formData.timeRange[0]),
    endTime: normalizeDateTime(formData.timeRange[1]),
    description: formData.description
  }

  submitLoading.value = true
  try {
    await addDraw(submitData)
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
    targetType: null,
    spuId: null,
    skuId: null,
    bundleId: null,
    price: null,
    stock: null,
    maxParticipants: null,
    winnerCount: null,
    timeRange: [],
    description: ''
  })
  spuOptions.value = []
  skuOptions.value = []
  targetOptions.value = []
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
    2: '已开奖',
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
})

const datePickerPopperOptions = {
  modifiers: [
    {
      name: 'flip',
      options: {
        fallbackPlacements: ['top-start', 'bottom-start', 'top', 'bottom']
      }
    },
    {
      name: 'preventOverflow',
      options: {
        boundary: 'viewport',
        padding: 8
      }
    }
  ]
}
</script>

<style lang="scss" scoped>
.draw-container {
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

  .target-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .target-name {
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

  .draw-time-picker {
    width: 260px;
    max-width: 100%;
  }

  .target-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;

    .target-option-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .target-option-status {
      margin-left: 10px;
      flex-shrink: 0;
    }
  }

  .sku-option {
    display: flex;
    align-items: center;
    width: 100%;

    .sku-price {
      margin-left: auto;
      color: #f56c6c;
      font-weight: 500;
    }
  }

  .image-placeholder-small {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 14px;
  }
}
</style>

<style lang="scss">
/* 对话框样式 */
.draw-dialog {
  .el-dialog__body {
    overflow: visible;
  }
}

.draw-date-picker {
  z-index: 3000 !important;

  .el-picker-panel {
    max-height: calc(100vh - 120px);
    overflow: auto;
  }
}
</style>
