<template>
  <div class="bundle-container">
    <el-card>
      <!-- 搜索表单 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="组合包名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入组合包名称"
            clearable
            @keyup.enter="handleQuery"
          />
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
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增组合包
        </el-button>
        <el-button 
          type="danger" 
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>

      <!-- 数据表格 -->
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
        <el-table-column prop="name" label="组合包名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="shoeCount" label="关联鞋款" width="100">
          <template #default="{ row }">
            <el-tooltip content="点击查看详情" placement="top">
              <el-button link type="primary" @click="handleShowShoeDetail(row)">
                <el-tag type="info">{{ row.shoeCount || 0 }} 款</el-tag>
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
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="140" />
        <el-table-column prop="updateTime" label="修改时间" min-width="140" />
        <el-table-column label="操作" width="120" fixed="right">
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
      width="800px"
      @close="handleDialogClose"
      destroy-on-close
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
          title="若该商品关联抽签活动且活动缓存未结束，系统将禁止修改，请等待缓存结束后再操作。"
          class="edit-warning"
        />
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组合包名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入组合包名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
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
        </el-row>
        <el-form-item label="组合包图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
            accept="image/*"
          >
            <el-image 
              v-if="formData.image" 
              :src="formData.image" 
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
        <el-form-item label="关联鞋款" prop="skuIds">
          <div class="shoe-select-container">
            <div class="cascade-select-wrapper">
              <el-select
                v-model="selectedSpuId"
                placeholder="请先选择SPU（鞋款）"
                filterable
                clearable
                style="width: 200px; margin-right: 12px;"
                @change="handleSpuChange"
              >
                <el-option 
                  v-for="item in spuOptions" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.id"
                >
                  <div class="spu-option">
                    <span class="spu-option-name">{{ item.name }}</span>
                    <el-tag 
                      :type="item.status === 1 ? 'success' : 'info'" 
                      size="small"
                    >
                      {{ item.status === 1 ? '上架' : '下架' }}
                    </el-tag>
                  </div>
                </el-option>
              </el-select>
              <el-select
                v-model="tempSkuIds"
                multiple
                filterable
                placeholder="请选择SKU（颜色）"
                style="flex: 1;"
                :disabled="!selectedSpuId"
                @change="handleSkuSelect"
              >
                <el-option 
                  v-for="item in currentSkuOptions" 
                  :key="item.id" 
                  :label="item.colorName" 
                  :value="item.id"
                  :disabled="formData.skuIds.includes(item.id)"
                >
                  <div class="shoe-option">
                    <el-image 
                      :src="item.image" 
                      fit="cover"
                      style="width: 30px; height: 30px; border-radius: 4px; margin-right: 8px;"
                    >
                      <template #error>
                        <div class="shoe-option-placeholder">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <span class="shoe-option-name">{{ item.colorName }}</span>
                    <el-tag 
                      :type="item.status === 1 ? 'success' : 'info'" 
                      size="small"
                      class="shoe-option-status"
                    >
                      {{ item.status === 1 ? '上架' : '下架' }}
                    </el-tag>
                    <span class="shoe-option-price">¥{{ item.price }}</span>
                  </div>
                </el-option>
              </el-select>
            </div>
            <div v-if="selectedSkuList.length > 0" class="selected-shoes">
              <div class="selected-shoes-header">
                <span>已选择 {{ selectedSkuList.length }} 款SKU</span>
                <el-tag v-if="hasDisabledSku" type="warning" size="small">
                  包含下架SKU，上架时需先上架
                </el-tag>
              </div>
              <div class="selected-shoes-list">
                <el-tag 
                  v-for="sku in selectedSkuList" 
                  :key="sku.id"
                  closable
                  @close="handleRemoveSku(sku.id)"
                  class="selected-shoe-tag"
                  :type="sku.status === 0 ? 'info' : ''"
                >
                  {{ sku.spuName }} - {{ sku.colorName }}
                  <span v-if="sku.status === 0 || sku.spuStatus === 0" class="disabled-mark">(下架)</span>
                </el-tag>
              </div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入组合包描述"
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

    <!-- 鞋款详情弹窗 -->
    <el-dialog
      v-model="shoeDetailVisible"
      title="关联鞋款详情"
      width="600px"
    >
      <div class="shoe-detail">
        <div class="bundle-info">
          <el-image 
            :src="shoeDetailData.image" 
            fit="cover"
            style="width: 80px; height: 80px; border-radius: 4px;"
          />
          <div class="bundle-meta">
            <div class="bundle-name">{{ shoeDetailData.name }}</div>
            <div class="bundle-price">¥{{ shoeDetailData.price }}</div>
          </div>
        </div>
        <el-divider />
        <div class="shoe-list">
          <div class="shoe-header">
            <span class="col-image">图片</span>
            <span class="col-name">鞋款名称</span>
            <span class="col-price">单价</span>
            <span class="col-copies">数量</span>
          </div>
          <div
            v-for="item in shoeDetailData.shoeItems"
            :key="item.id"
            class="shoe-item"
          >
            <span class="col-image">
              <el-image
                :src="item.image"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px;"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </span>
            <span class="col-name">{{ item.name }}</span>
            <span class="col-price">¥{{ item.price }}</span>
            <span class="col-copies">{{ item.copies }}</span>
          </div>
          <div v-if="!shoeDetailData.shoeItems || shoeDetailData.shoeItems.length === 0" class="no-data">
            暂无关联鞋款
          </div>
        </div>
        <el-divider />
        <div class="shoe-total">
          <span>共 {{ shoeDetailData.shoeItems?.length || 0 }} 款鞋</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  getBundleList, 
  addBundle, 
  updateBundle, 
  getBundleById, 
  deleteBundle, 
  updateBundleStatus,
  uploadBundleImage 
} from '@/api/bundle'
import { getSpuOptions, getSkuOptionsBySpuId } from '@/api/draw'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Picture, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const uploadLoading = ref(false)

const queryParams = reactive({
  name: '',
  status: null,
  page: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)

const selectedIds = ref([])

const spuOptions = ref([])
const allSkuData = ref({})
const selectedSpuId = ref(null)
const tempSkuIds = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增组合包')
const isEdit = ref(false)
const formRef = ref(null)

const shoeDetailVisible = ref(false)
const shoeDetailData = ref({
  id: null,
  name: '',
  image: '',
  price: 0,
  shoeItems: []
})

const formData = reactive({
  id: null,
  name: '',
  price: null,
  image: '',
  skuIds: [],
  description: ''
})

const formRules = {
  name: [
    { required: true, message: '请输入组合包名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传组合包图片', trigger: 'change' }
  ],
  skuIds: [
    { required: true, message: '请选择关联鞋款', trigger: 'change', type: 'array', min: 1 }
  ]
}

const currentSkuOptions = computed(() => {
  if (!selectedSpuId.value) return []
  return allSkuData.value[selectedSpuId.value] || []
})

const selectedSkuList = computed(() => {
  const result = []
  for (const spuId of Object.keys(allSkuData.value)) {
    const skus = allSkuData.value[spuId]
    for (const sku of skus) {
      if (formData.skuIds.includes(sku.id)) {
        result.push(sku)
      }
    }
  }
  return result
})

const hasDisabledSku = computed(() => {
  return selectedSkuList.value.some(item => item.status === 0 || item.spuStatus === 0)
})

async function loadSpuOptions() {
  try {
    spuOptions.value = await getSpuOptions()
  } catch (error) {
    console.error('获取SPU选项失败:', error)
  }
}

async function loadSkuOptionsForSpu(spuId) {
  if (allSkuData.value[spuId]) return
  try {
    const skus = await getSkuOptionsBySpuId(spuId)
    allSkuData.value[spuId] = skus || []
  } catch (error) {
    console.error('获取SKU选项失败:', error)
    allSkuData.value[spuId] = []
  }
}

async function handleSpuChange(spuId) {
  tempSkuIds.value = []
  if (spuId) {
    await loadSkuOptionsForSpu(spuId)
  }
}

// 获取列表
async function getList() {
  loading.value = true
  try {
    const res = await getBundleList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取组合包列表失败:', error)
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
  queryParams.status = null
  queryParams.page = 1
  getList()
}

// 新增
function handleAdd() {
  dialogTitle.value = '新增组合包'
  isEdit.value = false
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogTitle.value = '编辑组合包'
  isEdit.value = true
  try {
    const res = await getBundleById(row.id)
    const skuIds = res.bundleShoes?.map(item => item.skuId) || []
    await loadSkuDataForEdit(skuIds)
    Object.assign(formData, {
      id: res.id,
      name: res.name,
      price: res.price,
      image: res.image,
      skuIds: skuIds,
      description: res.description || ''
    })
    dialogVisible.value = true
  } catch (error) {
    console.error('获取组合包信息失败:', error)
  }
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除组合包"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteBundle(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 状态切换（上下架）
async function handleStatusChange(row) {
  if (row.status === 1) {
    try {
      await updateBundleStatus(row.status, row.id)
      ElMessage.success('上架成功')
    } catch (error) {
      row.status = 0
      ElMessage.error(error.message || '上架失败，组合包内包含下架鞋款，请先上架鞋款')
    }
  } else {
    try {
      await updateBundleStatus(row.status, row.id)
      ElMessage.success('下架成功')
    } catch (error) {
      row.status = 1
      ElMessage.error('下架失败')
    }
  }
}

function handleSkuSelect(skuIds) {
  const newSkuIds = skuIds.filter(id => !formData.skuIds.includes(id))
  formData.skuIds.push(...newSkuIds)
  tempSkuIds.value = []
}

function handleRemoveSku(skuId) {
  const index = formData.skuIds.indexOf(skuId)
  if (index > -1) {
    formData.skuIds.splice(index, 1)
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
    const res = await uploadBundleImage(options.file)
    formData.image = res
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const bundleShoes = formData.skuIds.map(skuId => {
    const sku = findSkuById(skuId)
    return {
      skuId: skuId,
      name: sku ? `${sku.spuName} - ${sku.colorName}` : '',
      price: sku?.price || 0,
      copies: 1
    }
  })

  const submitData = {
    ...formData,
    bundleShoes: bundleShoes
  }
  delete submitData.skuIds

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBundle(submitData)
      ElMessage.success('修改成功')
    } else {
      await addBundle(submitData)
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

function findSkuById(skuId) {
  for (const spuId of Object.keys(allSkuData.value)) {
    const sku = allSkuData.value[spuId].find(s => s.id === skuId)
    if (sku) return sku
  }
  return null
}

function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: null,
    name: '',
    price: null,
    image: '',
    skuIds: [],
    description: ''
  })
  selectedSpuId.value = null
  tempSkuIds.value = []
}

// 表格选择变化
function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

// 批量删除
function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的组合包')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个组合包吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteBundle(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    getList()
  }).catch(() => {})
}

// 显示鞋款详情
async function handleShowShoeDetail(row) {
  try {
    const res = await getBundleById(row.id)
    shoeDetailData.value = {
      id: res.id,
      name: res.name,
      image: res.image,
      price: res.price,
      shoeItems: res.shoeItems || []
    }
    shoeDetailVisible.value = true
  } catch (error) {
    console.error('获取鞋款详情失败:', error)
  }
}

async function loadSkuDataForEdit(skuIds) {
  const spuIds = new Set()
  for (const skuId of skuIds) {
    for (const spu of spuOptions.value) {
      if (!allSkuData.value[spu.id]) {
        await loadSkuOptionsForSpu(spu.id)
      }
      const sku = allSkuData.value[spu.id]?.find(s => s.id === skuId)
      if (sku) {
        spuIds.add(spu.id)
        break
      }
    }
  }
}

onMounted(async () => {
  await loadSpuOptions()
  getList()
})
</script>

<style lang="scss" scoped>
.bundle-container {
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

  .edit-warning {
    margin-bottom: 12px;
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

.shoe-select-container {
  width: 100%;

  .cascade-select-wrapper {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }

  .spu-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;

    .spu-option-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .shoe-option {
    display: flex;
    align-items: center;
    width: 100%;

    .shoe-option-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .shoe-option-status {
      margin: 0 8px;
    }

    .shoe-option-placeholder {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f7fa;
      color: #909399;
      font-size: 12px;
    }

    .shoe-option-price {
      margin-left: auto;
      color: #f56c6c;
      font-weight: 500;
    }
  }

  .selected-shoes {
    margin-top: 12px;
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 4px;

    .selected-shoes-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 13px;
      color: #606266;
      margin-bottom: 10px;
    }

    .selected-shoes-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .selected-shoe-tag {
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;

        .disabled-mark {
          font-size: 12px;
          opacity: 0.7;
        }
      }
    }
  }
}

.shoe-detail {
  .bundle-info {
    display: flex;
    align-items: center;
    gap: 15px;

    .bundle-meta {
      .bundle-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      .bundle-price {
        font-size: 14px;
        color: #f56c6c;
        margin-top: 5px;
      }
    }
  }

  .shoe-list {
    .shoe-header {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      font-weight: 600;
      color: #606266;
      border-bottom: 1px solid #ebeef5;

      .col-image { width: 60px; }
      .col-name { flex: 1; padding-left: 10px; }
      .col-price { width: 80px; text-align: center; }
      .col-copies { width: 60px; text-align: center; }
    }

    .shoe-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .col-image { width: 60px; }
      .col-name { 
        flex: 1; 
        padding-left: 10px;
        color: #303133;
      }
      .col-price { 
        width: 80px; 
        text-align: center;
        color: #f56c6c;
      }
      .col-copies { 
        width: 60px; 
        text-align: center;
        color: #909399;
      }
    }

    .no-data {
      text-align: center;
      color: #909399;
      padding: 20px 0;
    }
  }

  .shoe-total {
    text-align: right;
    font-size: 14px;
    color: #606266;
  }
}
</style>
