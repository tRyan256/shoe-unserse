<template>
  <div class="shoe-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="SPU名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入SPU名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="queryParams.brand" placeholder="请选择品牌" clearable filterable style="width: 150px;">
            <el-option 
              v-for="item in brandOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.name" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select 
            v-model="queryParams.categoryIds" 
            placeholder="请选择分类" 
            multiple
            clearable 
            collapse-tags
            collapse-tags-tooltip
            style="width: 250px;"
          >
            <el-option 
              v-for="item in categoryOptions" 
              :key="item.id" 
              :label="`${item.name} (${item.type === 1 ? '品牌' : '风格'})`"
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px;">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
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
        <el-button type="primary" @click="handleAddSpu">
          <el-icon><Plus /></el-icon>
          新增SPU
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDeleteSpu"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <!-- SPU 数据表格 -->
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image 
              :src="row.image" 
              :preview-src-list="[row.image]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
              placeholder="暂无图片"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="SPU名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="100" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="100" show-overflow-tooltip />
        <el-table-column prop="releaseDate" label="发售日期" width="100" />
        <el-table-column prop="isLimited" label="限量款" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isLimited === 1 ? 'danger' : 'info">
              {{ row.isLimited === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryNames" label="分类" min-width="150">
          <template #default="{ row }">
            <el-tag 
              v-for="(name, index) in row.categoryNames" 
              :key="index"
              type="info"
              size="small"
              style="margin-right: 5px; margin-bottom: 3px;"
            >
              {{ name }}
            </el-tag>
            <span v-if="!row.categoryNames || row.categoryNames.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="140" />
        <el-table-column prop="updateTime" label="修改时间" min-width="140" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewSkus(row)">
              查看SKU
            </el-button>
            <el-button type="primary" link @click="handleEditSpu(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDeleteSpu(row)">
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

    <!-- SPU 新增/编辑对话框 -->
    <el-dialog
      v-model="spuDialogVisible"
      :title="spuDialogTitle"
      width="700px"
      @close="handleSpuDialogClose"
      destroy-on-close
    >
      <el-form
        ref="spuFormRef"
        :model="spuFormData"
        :rules="spuFormRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SPU名称" prop="name">
              <el-input v-model="spuFormData.name" placeholder="请输入SPU名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-select v-model="spuFormData.brand" placeholder="请选择品牌" filterable allow-create style="width: 100%;">
                <el-option 
                  v-for="item in brandOptions" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.name" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="spuFormData.model" placeholder="请输入型号" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发售日期" prop="releaseDate">
              <el-date-picker
                v-model="spuFormData.releaseDate"
                type="date"
                placeholder="请选择发售日期"
                style="width: 100%;"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="分类" prop="categoryIds">
              <el-select 
                v-model="spuFormData.categoryIds" 
                placeholder="请选择分类(可多选)"
                multiple
                collapse-tags
                collapse-tags-tooltip
                style="width: 100%;"
              >
                <el-option 
                  v-for="item in categoryOptions" 
                  :key="item.id" 
                  :label="`${item.name} (${item.type === 1 ? '品牌' : '风格'})`"
                  :value="item.id" 
                />
              </el-select>
              <div class="form-tip">可以关联多个品牌或风格分类，例如同时关联"Nike"品牌和"篮球鞋"风格</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="限量款" prop="isLimited">
              <el-radio-group v-model="spuFormData.isLimited">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="SPU图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
            accept="image/*"
          >
            <el-image 
              v-if="spuFormData.image" 
              :src="spuFormData.image" 
              fit="cover"
              class="uploaded-image"
            />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x800px，支持 jpg、png 格式，大小不超过 2MB</div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="spuFormData.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入SPU描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="spuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSpuSubmit" :loading="spuSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- SKU 管理对话框 -->
    <el-dialog
      v-model="skuDialogVisible"
      :title="`SKU管理 - ${currentSpu?.name || ''}`"
      width="900px"
      @close="handleSkuDialogClose"
      destroy-on-close
    >
      <el-button type="primary" @click="handleAddSku" style="margin-bottom: 15px;">
        <el-icon><Plus /></el-icon>
        新增SKU
      </el-button>
      
      <el-table 
        :data="skuTableData" 
        v-loading="skuLoading" 
        border 
        stripe
      >
        <el-table-column prop="colorName" label="颜色" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="总库存" width="100">
          <template #default="{ row }">
            <el-tooltip content="点击查看详情" placement="top">
              <el-button 
                link 
                type="primary" 
                @click="handleShowSkuStockDetail(row)"
              >
                <el-tag :type="getStockTagType(row.stock)">{{ row.stock || 0 }}</el-tag>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleSkuStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditSku(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDeleteSku(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- SKU 新增/编辑对话框 -->
    <el-dialog
      v-model="singleSkuDialogVisible"
      :title="singleSkuDialogTitle"
      width="700px"
      @close="handleSingleSkuDialogClose"
      destroy-on-close
    >
      <el-form
        ref="skuFormRef"
        :model="skuFormData"
        :rules="skuFormRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="颜色名称" prop="colorName">
              <el-input v-model="skuFormData.colorName" placeholder="请输入颜色名称" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number 
                v-model="skuFormData.price" 
                :min="0" 
                :precision="2"
                :step="10"
                placeholder="请输入价格"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="SKU图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleSkuImageUpload"
            accept="image/*"
          >
            <el-image 
              v-if="skuFormData.image" 
              :src="skuFormData.image" 
              fit="cover"
              class="uploaded-image"
            />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <span>上传图片</span>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸: 800x800px，支持 jpg、png 格式，大小不超过 2MB</div>
        </el-form-item>
        <el-form-item label="尺码库存" prop="sizeStockList">
          <div class="size-stock-container">
            <div 
              v-for="(item, index) in skuFormData.sizeStockList" 
              :key="index" 
              class="size-stock-item"
            >
              <el-select 
                v-model="item.size" 
                placeholder="选择尺码"
                style="width: 120px;"
                filterable
                allow-create
              >
                <el-option 
                  v-for="size in sizeOptions" 
                  :key="size" 
                  :label="size" 
                  :value="size" 
                />
              </el-select>
              <el-input-number 
                v-model="item.stock" 
                :min="0" 
                :max="99999"
                placeholder="库存"
                style="width: 120px; margin-left: 10px;"
              />
              <el-button 
                type="danger" 
                :icon="Delete" 
                circle 
                style="margin-left: 10px;"
                @click="handleRemoveSkuSizeStock(index)"
              />
            </div>
            <el-button type="primary" plain @click="handleAddSkuSizeStock">
              <el-icon><Plus /></el-icon>
              添加尺码
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="singleSkuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSingleSkuSubmit" :loading="singleSkuSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- SKU 库存详情弹窗 -->
    <el-dialog
      v-model="skuStockDetailVisible"
      title="SKU库存详情"
      width="500px"
    >
      <div class="stock-detail">
        <div class="shoe-info">
          <el-image 
            :src="skuStockDetailData.image" 
            fit="cover"
            style="width: 80px; height: 80px; border-radius: 4px;"
          />
          <div class="shoe-meta">
            <div class="shoe-name">{{ skuStockDetailData.colorName }}</div>
            <div class="shoe-brand">{{ currentSpu?.name }}</div>
          </div>
        </div>
        <el-divider />
        <div class="stock-list">
          <div class="stock-header">
            <span class="col-size">尺码</span>
            <span class="col-stock">库存</span>
          </div>
          <div 
            v-for="item in skuStockDetailData.sizes" 
            :key="item.id" 
            class="stock-item"
          >
            <span class="col-size">{{ item.size }}码</span>
            <span class="col-stock">
              <el-tag :type="getStockTagType(item.stock)" size="small">
                {{ item.stock }}
              </el-tag>
            </span>
          </div>
          <div v-if="!skuStockDetailData.sizes || skuStockDetailData.sizes.length === 0" class="no-data">
            暂无库存数据
          </div>
        </div>
        <el-divider />
        <div class="stock-total">
          <span>总库存：</span>
          <el-tag :type="getStockTagType(skuStockDetailData.stock)" size="large">
            {{ skuStockDetailData.stock || 0 }} 双
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  getShoeList, 
  addShoe, 
  updateShoe, 
  getShoeById, 
  deleteShoe, 
  updateShoeStatus,
  uploadShoeImage 
} from '@/api/shoe'
import { getCategoryOptions } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Search, Refresh, Picture } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const uploadLoading = ref(false)

// 查询参数
const queryParams = reactive({
  name: '',
  brand: '',
  categoryIds: [],
  status: null,
  page: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref([])
const total = ref(0)

// 选中的ID列表
const selectedIds = ref([])

// 分类选项
const categoryOptions = ref([])

// 品牌选项
const brandOptions = ref([])

// 常用尺码选项
const sizeOptions = [
  '35', '35.5', '36', '36.5', '37', '37.5', '38', '38.5', 
  '39', '39.5', '40', '40.5', '41', '41.5', '42', '42.5', 
  '43', '43.5', '44', '44.5', '45', '45.5', '46', '46.5', '47'
]

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增鞋款')
const isEdit = ref(false)
const formRef = ref(null)

// 库存详情弹窗
const stockDetailVisible = ref(false)
const stockDetailData = ref({
  id: null,
  name: '',
  brand: '',
  image: '',
  stock: 0,
  sizes: []
})

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  brand: '',
  model: '',
  color: '',
  releaseDate: null,
  isLimited: 0,
  price: null,
  categoryIds: [],
  image: '',
  description: '',
  sizeStockList: []
})

// 表单校验规则
const formRules = {
  name: [
    { required: true, message: '请输入鞋款名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  brand: [
    { required: true, message: '请输入品牌', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  categoryIds: [
    { required: true, message: '请选择至少一个分类', trigger: 'change', type: 'array', min: 1 }
  ],
  image: [
    { required: true, message: '请上传鞋款图片', trigger: 'change' }
  ]
}

// 获取分类选项
async function loadCategoryOptions() {
  try {
    // 加载所有分类(品牌+风格)
    const allCategories = await getCategoryOptions()
    categoryOptions.value = allCategories
    // 品牌选项只显示品牌类型（type=1）
    brandOptions.value = await getCategoryOptions(1)
  } catch (error) {
    console.error('获取分类选项失败:', error)
  }
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const res = await getShoeList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取鞋款列表失败:', error)
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
  queryParams.brand = ''
  queryParams.categoryIds = []
  queryParams.status = null
  queryParams.page = 1
  getList()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增鞋款'
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑
async function handleEdit(row) {
  dialogTitle.value = '编辑鞋款'
  isEdit.value = true
  try {
    const res = await getShoeById(row.id)
    Object.assign(formData, {
      id: res.id,
      name: res.name,
      brand: res.brand,
      model: res.model || '',
      color: res.color || '',
      releaseDate: res.releaseDate || null,
      isLimited: res.isLimited || 0,
      price: res.price,
      categoryIds: res.categoryIds || [],
      image: res.image,
      description: res.description,
      sizeStockList: res.sizes || res.sizeStockList || []
    })
    // 如果没有尺码库存数据，初始化一个空项
    if (formData.sizeStockList.length === 0) {
      formData.sizeStockList.push({ size: '', stock: 0 })
    }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取鞋款信息失败:', error)
  }
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除鞋款"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteShoe(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 表格选择变化
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

// 批量删除
function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的鞋款')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个鞋款吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteShoe(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    getList()
  }).catch(() => {})
}

// 状态切换（上下架）
async function handleStatusChange(row) {
  try {
    await updateShoeStatus(row.status, row.id)
    ElMessage.success(row.status === 1 ? '上架成功' : '下架成功')
  } catch (error) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 图片上传前校验
function beforeImageUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传
async function handleImageUpload(options) {
  uploadLoading.value = true
  try {
    const res = await uploadShoeImage(options.file)
    formData.image = res
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
}

// 添加尺码库存项
function handleAddSizeStock() {
  formData.sizeStockList.push({ size: '', stock: 0 })
}

// 删除尺码库存项
function handleRemoveSizeStock(index) {
  if (formData.sizeStockList.length > 1) {
    formData.sizeStockList.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一条尺码库存记录')
  }
}

// 提交表单
async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 校验尺码库存
  const validSizeStock = formData.sizeStockList.every(item => item.size && item.stock >= 0)
  if (!validSizeStock) {
    ElMessage.warning('请完善尺码库存信息')
    return
  }

  // 过滤掉空的尺码库存，转换为后端格式
  const submitData = {
    ...formData,
    sizes: formData.sizeStockList.filter(item => item.size)
  }
  delete submitData.sizeStockList

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateShoe(submitData)
      ElMessage.success('修改成功')
    } else {
      await addShoe(submitData)
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
    brand: '',
    model: '',
    color: '',
    releaseDate: null,
    isLimited: 0,
    price: null,
    categoryIds: [],
    image: '',
    description: '',
    sizeStockList: [{ size: '', stock: 0 }]
  })
}

// 获取库存标签类型
function getStockTagType(stock) {
  if (!stock || stock === 0) return 'danger'
  if (stock < 10) return 'warning'
  return 'success'
}

// 显示库存详情
async function handleShowStockDetail(row) {
  try {
    const res = await getShoeById(row.id)
    stockDetailData.value = {
      id: res.id,
      name: res.name,
      brand: res.brand,
      image: res.image,
      stock: res.stock || row.stock || 0,
      sizes: res.sizes || []
    }
    stockDetailVisible.value = true
  } catch (error) {
    console.error('获取库存详情失败:', error)
  }
}

onMounted(() => {
  loadCategoryOptions()
  getList()
})
</script>

<style lang="scss" scoped>
.shoe-container {
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

  .image-placeholder {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 20px;
  }
}

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }

  .uploaded-image {
    width: 148px;
    height: 148px;
    display: block;
  }

  .upload-placeholder {
    width: 148px;
    height: 148px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #8c939d;

    .upload-icon {
      font-size: 28px;
      margin-bottom: 8px;
    }
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.5;
}

.size-stock-container {
  width: 100%;

  .size-stock-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
}

.stock-detail {
  .shoe-info {
    display: flex;
    align-items: center;
    gap: 15px;

    .shoe-meta {
      .shoe-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      .shoe-brand {
        font-size: 14px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }

  .stock-list {
    .stock-header {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      font-weight: 600;
      color: #606266;
      border-bottom: 1px solid #ebeef5;
    }

    .stock-item {
      display: flex;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .col-size {
        font-size: 14px;
        color: #303133;
      }
    }

    .no-data {
      text-align: center;
      color: #909399;
      padding: 20px 0;
    }
  }

  .stock-total {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
  }
}
</style>
