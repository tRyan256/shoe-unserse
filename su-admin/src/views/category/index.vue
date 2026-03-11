<template>
  <div class="category-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="分类名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入分类名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 120px">
            <el-option label="品牌" :value="1" />
            <el-option label="风格" :value="2" />
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
          新增分类
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="type" label="分类类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'">
              {{ row.type === 1 ? '品牌' : '风格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
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
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :label="1">品牌</el-radio>
            <el-radio :label="2">风格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
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
  getCategoryList, 
  addCategory, 
  updateCategory, 
  deleteCategory,
  updateCategoryStatus 
} from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 查询参数
const queryParams = reactive({
  name: '',
  type: null,
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  type: 1,
  sort: 0
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度在1到50个字符', trigger: 'blur' }
  ],
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }]
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const res = await getCategoryList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取分类列表失败:', error)
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
  queryParams.page = 1
  getList()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增分类'
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  dialogTitle.value = '编辑分类'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    type: row.type,
    sort: row.sort || 0
  })
  dialogVisible.value = true
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除分类"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCategory(row.id)
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
    await updateCategoryStatus(row.status, row.id)
    ElMessage.success(`${text}成功`)
  } catch (error) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCategory(formData)
      ElMessage.success('修改成功')
    } else {
      await addCategory(formData)
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
    sort: 0
  })
}

// 初始化
getList()
</script>

<style lang="scss" scoped>
.category-container {
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
