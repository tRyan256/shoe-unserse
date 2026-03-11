<template>
  <div class="shoe-container">
    <el-card>
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="鞋款名称">
          <el-input 
            v-model="queryParams.name" 
            placeholder="请输入鞋款名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select 
            v-model="queryParams.brand" 
            placeholder="请选择品牌" 
            clearable 
            filterable
            style="width: 150px;"
          >
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

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe
        @selection-change="handleSelectionChange"
        @expand-change="handleExpandChange"
        row-key="id"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="sku-expand-container">
              <div class="sku-header">
                <span class="sku-title">SKU 列表（颜色款式）</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="handleAddSku(row)"
                >
                  <el-icon><Plus /></el-icon>
                  增加SKU
                </el-button>
              </div>
              <div v-loading="row.skuLoading">
                <el-table :data="row.skuList" border size="small" v-if="row.skuList && row.skuList.length > 0" style="width: 100%;">
                  <el-table-column type="index" label="序号" width="50" align="center" />
                  <el-table-column prop="image" label="图片" width="80" align="center">
                    <template #default="{ row: sku }">
                      <el-image 
                        :src="sku.image" 
                        :preview-src-list="[sku.image]"
                        fit="cover"
                        style="width: 50px; height: 50px; border-radius: 4px;"
                      >
                        <template #error>
                          <div class="image-placeholder">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                    </template>
                  </el-table-column>
                  <el-table-column prop="colorName" label="颜色" width="120" />
                  <el-table-column prop="price" label="价格" width="100" align="center">
                    <template #default="{ row: sku }">
                      <span class="price">¥{{ sku.price }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="stock" label="库存" width="80" align="center">
                    <template #default="{ row: sku }">
                      <el-tag :type="getStockTagType(sku.stock)" size="small">{{ sku.stock || 0 }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="salesCount" label="销量" width="80" align="center" />
                  <el-table-column prop="isDefault" label="默认" width="70" align="center">
                    <template #default="{ row: sku }">
                      <el-tag v-if="sku.isDefault === 1" type="success" size="small">默认</el-tag>
                      <span v-else style="color: #909399;">-</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="80" align="center">
                    <template #default="{ row: sku }">
                      <el-switch
                        v-model="sku.status"
                        :active-value="1"
                        :inactive-value="0"
                        @change="handleSkuStatusChange(sku)"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200" align="center">
                    <template #default="{ row: sku }">
                      <el-button 
                        v-if="sku.isDefault !== 1" 
                        type="success" 
                        link 
                        size="small" 
                        @click="handleSetDefaultSku(row, sku)"
                      >
                        设为默认
                      </el-button>
                      <el-button type="primary" link size="small" @click="handleEditSku(row, sku)">
                        编辑
                      </el-button>
                      <el-button type="danger" link size="small" @click="handleDeleteSku(row, sku)">
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-else class="sku-empty-actions">
                  <el-empty description="暂无SKU数据" :image-size="80" />
                  <el-button type="primary" @click="handleAddSku(row)">
                    <el-icon><Plus /></el-icon>
                    增加SKU
                  </el-button>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="SPU名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="brand" label="品牌" width="100" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="100" show-overflow-tooltip />
        <el-table-column label="价格区间" width="120">
          <template #default="{ row }">
            <span v-if="row.minPrice && row.maxPrice">
              <span class="price">¥{{ row.minPrice }}</span>
              <span v-if="row.minPrice !== row.maxPrice"> - ¥{{ row.maxPrice }}</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="colorCount" label="颜色数" width="80">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.colorCount || 0 }}种</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalStock" label="总库存" width="90">
          <template #default="{ row }">
            <el-tag :type="getStockTagType(row.totalStock)">{{ row.totalStock || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="80" />
        <el-table-column prop="isLimited" label="限量款" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isLimited === 1 ? 'danger' : 'info'">
              {{ row.isLimited === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryNames" label="分类" min-width="150">
          <template #default="{ row }">
            <el-tag 
              v-for="(name, index) in parseCategoryNames(row.categoryNames)" 
              :key="index"
              type="info"
              size="small"
              style="margin-right: 5px; margin-bottom: 3px;"
            >
              {{ name }}
            </el-tag>
            <span v-if="!row.categoryNames">-</span>
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
        <el-table-column label="创建时间" min-width="140">
          <template #default="{ row }">
            {{ row.createTime || row.create_time }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditSpu(row)">
              编辑
            </el-button>
            <el-button type="success" link @click="handleAddSku(row)">
              新增SKU
            </el-button>
            <el-button type="danger" link @click="handleDeleteSpu(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
      v-model="spuDialogVisible"
      :title="spuDialogTitle"
      width="600px"
      @close="handleSpuDialogClose"
      destroy-on-close
    >
      <el-form
        ref="spuFormRef"
        :model="spuFormData"
        :rules="spuFormRules"
        label-width="100px"
      >
        <el-alert
          v-if="isSpuEdit"
          type="warning"
          show-icon
          :closable="false"
          title="若该商品关联抽签活动且活动缓存未结束，系统将禁止修改，请等待缓存结束后再操作。"
          class="edit-warning"
        />
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SPU名称" prop="name">
              <el-input v-model="spuFormData.name" placeholder="请输入SPU名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-select 
                v-model="spuFormData.brand" 
                placeholder="请选择品牌"
                filterable
                allow-create
                style="width: 100%;"
              >
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
                  :label="item.name"
                  :value="item.id" 
                />
              </el-select>
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
        <el-button type="primary" @click="handleSubmitSpu" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="skuDialogVisible"
      :title="skuDialogTitle"
      width="700px"
      @close="handleSkuDialogClose"
      destroy-on-close
    >
      <el-form
        ref="skuFormRef"
        :model="skuFormData"
        :rules="skuFormRules"
        label-width="100px"
      >
        <el-alert
          v-if="isSkuEdit"
          type="warning"
          show-icon
          :closable="false"
          title="若该商品关联抽签活动且活动缓存未结束，系统将禁止修改，请等待缓存结束后再操作。"
          class="edit-warning"
        />
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="颜色名称" prop="colorName">
              <el-input v-model="skuFormData.colorName" placeholder="如：黑白配色、纯白" maxlength="50" />
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
            :http-request="handleImageUpload"
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
        <el-form-item label="尺码库存" prop="sizes">
          <div class="size-stock-container">
            <div 
              v-for="(item, index) in skuFormData.sizes" 
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
                @click="handleRemoveSizeStock(index)"
              />
            </div>
            <el-button type="primary" plain @click="handleAddSizeStock">
              <el-icon><Plus /></el-icon>
              添加尺码
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitSku" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  getSpuList, 
  addSpu, 
  updateSpu, 
  getSpuById, 
  deleteSpu, 
  updateSpuStatus 
} from '@/api/shoeSpu'
import { 
  getSkuListBySpuId, 
  addSku, 
  updateSku, 
  getSkuById, 
  deleteSku,
  updateSkuStatus,
  setDefaultSku
} from '@/api/shoeSku'
import { getCategoryOptions } from '@/api/category'
import { uploadFile } from '@/api/common'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus, Search, Refresh, Picture } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)

const queryParams = reactive({
  name: '',
  brand: '',
  categoryIds: [],
  status: null,
  page: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const categoryOptions = ref([])
const brandOptions = ref([])

const sizeOptions = [
  '35', '35.5', '36', '36.5', '37', '37.5', '38', '38.5', 
  '39', '39.5', '40', '40.5', '41', '41.5', '42', '42.5', 
  '43', '43.5', '44', '44.5', '45', '45.5', '46', '46.5', '47'
]

const spuDialogVisible = ref(false)
const spuDialogTitle = ref('新增SPU')
const isSpuEdit = ref(false)
const spuFormRef = ref(null)

const skuDialogVisible = ref(false)
const skuDialogTitle = ref('新增SKU')
const isSkuEdit = ref(false)
const skuFormRef = ref(null)
const currentSpuId = ref(null)

const spuFormData = reactive({
  id: null,
  name: '',
  brand: '',
  model: '',
  releaseDate: null,
  isLimited: 0,
  categoryIds: [],
  description: ''
})

const skuFormData = reactive({
  id: null,
  spuId: null,
  colorName: '',
  price: null,
  image: '',
  sizes: []
})

const spuFormRules = {
  name: [
    { required: true, message: '请输入SPU名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

const skuFormRules = {
  colorName: [
    { required: true, message: '请输入颜色名称', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  image: [
    { required: true, message: '请上传SKU图片', trigger: 'change' }
  ]
}

async function loadCategoryOptions() {
  try {
    const allCategories = await getCategoryOptions()
    categoryOptions.value = allCategories.filter(item => item.type === 2)
    brandOptions.value = await getCategoryOptions(1)
  } catch (error) {
    console.error('获取分类选项失败:', error)
  }
}

async function getList() {
  loading.value = true
  try {
    const res = await getSpuList(queryParams)
    const oldDataMap = new Map(tableData.value.map(item => [item.id, item]))
    tableData.value = (res.records || []).map(item => {
      const oldItem = oldDataMap.get(item.id)
      return {
        ...item,
        skuList: oldItem?.skuList || [],
        skuLoading: oldItem?.skuLoading || false,
        expanded: oldItem?.expanded || false
      }
    })
    total.value = res.total || 0
  } catch (error) {
    console.error('获取SPU列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.page = 1
  getList()
}

function handleReset() {
  queryParams.name = ''
  queryParams.brand = ''
  queryParams.categoryIds = []
  queryParams.status = null
  queryParams.page = 1
  getList()
}

async function handleExpandChange(row, expandedRows) {
  // 检查当前行是否在展开列表中
  const isExpanded = expandedRows.some(item => item.id === row.id)
  if (isExpanded) {
    row.skuLoading = true
    try {
      const skuList = await getSkuListBySpuId(row.id)
      row.skuList = skuList || []
    } catch (error) {
      console.error('获取SKU列表失败:', error)
      row.skuList = []
    } finally {
      row.skuLoading = false
    }
  }
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleAddSpu() {
  spuDialogTitle.value = '新增SPU'
  isSpuEdit.value = false
  spuDialogVisible.value = true
}

async function handleEditSpu(row) {
  spuDialogTitle.value = '编辑SPU'
  isSpuEdit.value = true
  try {
    const res = await getSpuById(row.id)
    Object.assign(spuFormData, {
      id: res.id,
      name: res.name,
      brand: res.brand || '',
      model: res.model || '',
      releaseDate: res.releaseDate || null,
      isLimited: res.isLimited || 0,
      categoryIds: res.categoryIds || [],
      description: res.description || ''
    })
    spuDialogVisible.value = true
  } catch (error) {
    console.error('获取SPU信息失败:', error)
  }
}

function handleDeleteSpu(row) {
  ElMessageBox.confirm(`确定要删除SPU"${row.name}"吗？删除后该SPU下所有SKU也将被删除。`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteSpu(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function handleBatchDeleteSpu() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的SPU')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个SPU吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteSpu(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    getList()
  }).catch(() => {})
}

async function handleStatusChange(row) {
  try {
    await updateSpuStatus(row.status, row.id)
    ElMessage.success(row.status === 1 ? '上架成功' : '下架成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

async function handleSubmitSpu() {
  const valid = await spuFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isSpuEdit.value) {
      await updateSpu(spuFormData)
      ElMessage.success('修改成功')
    } else {
      await addSpu(spuFormData)
      ElMessage.success('新增成功')
    }
    spuDialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

function handleSpuDialogClose() {
  spuFormRef.value?.resetFields()
  Object.assign(spuFormData, {
    id: null,
    name: '',
    brand: '',
    model: '',
    releaseDate: null,
    isLimited: 0,
    categoryIds: [],
    description: ''
  })
}

function handleAddSku(row) {
  currentSpuId.value = row.id
  skuDialogTitle.value = '新增SKU'
  isSkuEdit.value = false
  Object.assign(skuFormData, {
    id: null,
    spuId: null,
    colorName: '',
    price: null,
    image: '',
    sizes: [{ size: '', stock: 0 }]
  })
  skuDialogVisible.value = true
}

async function handleEditSku(spu, sku) {
  currentSpuId.value = spu.id
  skuDialogTitle.value = '编辑SKU'
  isSkuEdit.value = true
  try {
    const res = await getSkuById(sku.id)
    Object.assign(skuFormData, {
      id: res.id,
      spuId: res.spuId,
      colorName: res.colorName,
      price: res.price,
      image: res.image,
      sizes: res.sizes && res.sizes.length > 0 ? res.sizes : [{ size: '', stock: 0 }]
    })
    skuDialogVisible.value = true
  } catch (error) {
    console.error('获取SKU信息失败:', error)
  }
}

function handleDeleteSku(spu, sku) {
  ElMessageBox.confirm(`确定要删除SKU"${sku.colorName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteSku(sku.id)
    ElMessage.success('删除成功')
    const skuList = await getSkuListBySpuId(spu.id)
    spu.skuList = skuList || []
    getList()
  }).catch(() => {})
}

async function handleSkuStatusChange(sku) {
  try {
    await updateSkuStatus(sku.status, sku.id)
    ElMessage.success(sku.status === 1 ? '上架成功' : '下架成功')
  } catch (error) {
    sku.status = sku.status === 1 ? 0 : 1
  }
}

async function handleSetDefaultSku(spu, sku) {
  try {
    await setDefaultSku(spu.id, sku.id)
    ElMessage.success('设置默认成功')
    const skuList = await getSkuListBySpuId(spu.id)
    spu.skuList = skuList || []
  } catch (error) {
    console.error('设置默认失败:', error)
  }
}

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

async function handleImageUpload(options) {
  try {
    const res = await uploadFile(options.file)
    skuFormData.image = res
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败')
  }
}

function handleAddSizeStock() {
  skuFormData.sizes.push({ size: '', stock: 0 })
}

function handleRemoveSizeStock(index) {
  if (skuFormData.sizes.length > 1) {
    skuFormData.sizes.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一条尺码库存记录')
  }
}

async function handleSubmitSku() {
  const valid = await skuFormRef.value.validate().catch(() => false)
  if (!valid) return

  const validSizes = skuFormData.sizes.filter(item => item.size && item.stock >= 0)
  if (validSizes.length === 0) {
    ElMessage.warning('请至少添加一条有效的尺码库存')
    return
  }

  const submitData = {
    id: skuFormData.id,
    spuId: currentSpuId.value,
    colorName: skuFormData.colorName,
    price: skuFormData.price,
    image: skuFormData.image,
    sizes: validSizes
  }

  submitLoading.value = true
  try {
    if (isSkuEdit.value) {
      await updateSku(submitData)
      ElMessage.success('修改成功')
    } else {
      await addSku(submitData)
      ElMessage.success('新增成功')
    }
    skuDialogVisible.value = false
    await getList()
    const spu = tableData.value.find(item => item.id === currentSpuId.value)
    if (spu) {
      const skuList = await getSkuListBySpuId(currentSpuId.value)
      spu.skuList = skuList || []
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

function handleSkuDialogClose() {
  skuFormRef.value?.resetFields()
  Object.assign(skuFormData, {
    id: null,
    spuId: null,
    colorName: '',
    price: null,
    image: '',
    sizes: [{ size: '', stock: 0 }]
  })
}

function getStockTagType(stock) {
  if (!stock || stock === 0) return 'danger'
  if (stock < 10) return 'warning'
  return 'success'
}

function parseCategoryNames(names) {
  if (!names) return []
  return names.split(',').filter(n => n.trim())
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
    width: 50px;
    height: 50px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 16px;
  }

  .edit-warning {
    margin-bottom: 12px;
  }
}

.sku-expand-container {
  padding: 15px 30px;
  background-color: #fafafa;

  .sku-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #e4e7ed;

    .sku-title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
  }

  .sku-empty-actions {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;

    .el-button {
      margin-top: 10px;
    }
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

.size-stock-container {
  width: 100%;

  .size-stock-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }
}
</style>
