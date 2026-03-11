<template>
  <div class="coupon-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="优惠券名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入优惠券名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="优惠券类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
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
          新增优惠券
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="优惠券名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'">
              {{ row.type === 1 ? '满减券' : '折扣券' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="金额/折扣" width="120">
          <template #default="{ row }">
            <span v-if="row.type === 1">{{ row.value }}元</span>
            <span v-else>{{ Number(row.value) * 10 }}折</span>
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="使用门槛" width="120">
          <template #default="{ row }">
            {{ row.minAmount ? `满${row.minAmount}元可用` : '无门槛' }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="有效期" min-width="200">
          <template #default="{ row }">
            {{ row.startTime }} 至 {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-alert
          v-if="isEdit"
          type="warning"
          show-icon
          :closable="false"
          title="若该优惠券关联空投活动且活动缓存未结束，系统将禁止修改，请等待缓存结束后再操作。"
          class="edit-warning"
        />
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入优惠券名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-radio-group v-model="formData.type" @change="handleTypeChange">
            <el-radio :label="1">满减券</el-radio>
            <el-radio :label="2">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="formData.type === 1 ? '优惠金额' : '折扣比例'" prop="value">
          <el-input-number 
            v-model="formData.value" 
            :min="formData.type === 1 ? 1 : 1" 
            :max="formData.type === 1 ? 9999 : 9.9"
            :precision="formData.type === 1 ? 0 : 1"
            :step="formData.type === 1 ? 1 : 0.1"
            style="width: 200px"
          />
          <span style="margin-left: 10px">{{ formData.type === 1 ? '元' : '折' }}</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number 
            v-model="formData.minAmount" 
            :min="0" 
            :max="99999"
            :precision="2"
            style="width: 200px"
          />
          <span style="margin-left: 10px">元（0表示无门槛）</span>
        </el-form-item>
        <el-form-item label="有效期" prop="validTime">
          <el-date-picker
            v-model="formData.validTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
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
import { ref, reactive } from 'vue'
import { 
  getCouponList, 
  addCoupon, 
  updateCoupon, 
  getCouponById,
  deleteCoupon,
  updateCouponStatus 
} from '@/api/coupon'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 查询参数
const queryParams = reactive({
  name: '',
  type: null,
  status: null,
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  type: 1,
  value: null,
  minAmount: 0,
  validTime: []
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入优惠券名称', trigger: 'blur' },
    { min: 1, max: 50, message: '优惠券名称长度在1到50个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入优惠金额或折扣比例', trigger: 'blur' }],
  validTime: [{ required: true, message: '请选择有效期', trigger: 'change' }]
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const res = await getCouponList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取优惠券列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索（重置到第一页）
function handleSearch() {
  queryParams.page = 1
  getList()
}

// 重置
function handleReset() {
  queryParams.name = ''
  queryParams.type = null
  queryParams.status = null
  queryParams.page = 1
  getList()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增优惠券'
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
async function handleEdit(row) {
  dialogTitle.value = '编辑优惠券'
  isEdit.value = true
  try {
    const res = await getCouponById(row.id)
    Object.assign(formData, {
      id: res.id,
      name: res.name,
      type: res.type,
      value: res.type === 2 ? Number(res.value) * 10 : res.value,
      minAmount: res.minAmount || 0,
      validTime: [res.startTime, res.endTime]
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取优惠券信息失败:', error)
  }
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除优惠券"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCoupon(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 状态切换
async function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '禁用'
  try {
    await updateCouponStatus(row.status, row.id)
    ElMessage.success(`${text}成功`)
  } catch (error) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
  }
}

// 类型切换时重置value
function handleTypeChange() {
  formData.value = null
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    const value = formData.type === 2 ? Number(formData.value) / 10 : formData.value
    const submitData = {
      id: formData.id,
      name: formData.name,
      type: formData.type,
      value,
      minAmount: formData.minAmount || 0,
      startTime: formData.validTime[0],
      endTime: formData.validTime[1]
    }
    
    if (isEdit.value) {
      await updateCoupon(submitData)
      ElMessage.success('修改成功')
    } else {
      await addCoupon(submitData)
      ElMessage.success('新增成功')
    }
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
    id: null,
    name: '',
    type: 1,
    value: null,
    minAmount: 0,
    validTime: []
  })
}

// 初始化
getList()
</script>

<style lang="scss" scoped>
.coupon-container {
  .search-form {
    margin-bottom: 15px;
  }
  
  .table-operations {
    margin-bottom: 15px;
  }
  
  .el-pagination {
    margin-top: 20px;
  }

  .edit-warning {
    margin-bottom: 12px;
  }
}
</style>
