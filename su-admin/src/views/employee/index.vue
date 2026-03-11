<template>
  <div class="employee-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="员工姓名">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入员工姓名"
            clearable
            @keyup.enter="handleQuery"
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

      <!-- 操作按钮 -->
      <div class="table-operations">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增员工
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="员工姓名" min-width="100" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="sex" label="性别" width="80">
          <template #default="{ row }">
            {{ row.sex === '1' ? '男' : '女' }}
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
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
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="formData.username" 
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="formData.sex">
            <el-radio label="1">男</el-radio>
            <el-radio label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="formData.idNumber" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input 
            v-model="formData.password" 
            type="password"
            placeholder="请输入密码"
            show-password
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
  getEmployeeList, 
  addEmployee, 
  updateEmployee, 
  getEmployeeById,
  updateEmployeeStatus 
} from '@/api/employee'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)

// 查询参数
const queryParams = reactive({
  name: '',
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  username: '',
  phone: '',
  sex: '1',
  idNumber: '',
  password: ''
})

// 表单校验规则
const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const res = await getEmployeeList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取员工列表失败:', error)
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
  queryParams.name = ''
  queryParams.page = 1
  getList()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增员工'
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
async function handleEdit(row) {
  dialogTitle.value = '编辑员工'
  isEdit.value = true
  try {
    const res = await getEmployeeById(row.id)
    Object.assign(formData, res)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取员工信息失败:', error)
  }
}

// 状态切换
async function handleStatusChange(row) {
  try {
    await updateEmployeeStatus(row.status, row.id)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateEmployee(formData)
      ElMessage.success('修改成功')
    } else {
      await addEmployee(formData)
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
    username: '',
    phone: '',
    sex: '1',
    idNumber: '',
    password: ''
  })
}

// 初始化
getList()
</script>

<style lang="scss" scoped>
.employee-container {
  .search-form {
    margin-bottom: 15px;
  }
  
  .table-operations {
    margin-bottom: 15px;
  }
  
  .el-pagination {
    margin-top: 20px;
  }
}
</style>
