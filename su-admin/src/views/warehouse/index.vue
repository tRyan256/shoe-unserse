<template>
  <div class="warehouse-container">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="仓库管理" name="warehouse">
          <el-form :model="queryParams" inline class="search-form">
            <el-form-item label="仓库名称">
              <el-input
                v-model="queryParams.name"
                placeholder="请输入仓库名称"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="仓库编码">
              <el-input
                v-model="queryParams.code"
                placeholder="请输入仓库编码"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px;">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="省市区">
              <el-cascader
                v-model="queryParams.regionCodes"
                :options="regionOptions"
                :props="queryRegionCascaderProps"
                clearable
                filterable
                placeholder="请选择省/市/区"
                style="width: 240px;"
              />
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

          <div class="table-operations">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增仓库
            </el-button>
          </div>

          <el-table :data="tableData" v-loading="loading" border stripe>
            <el-table-column prop="name" label="仓库名称" min-width="160" show-overflow-tooltip />
            <el-table-column prop="code" label="仓库编码" width="140" show-overflow-tooltip />
            <el-table-column label="地址" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">
                {{ getFullAddress(row) }}
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" width="140" show-overflow-tooltip />
            <el-table-column prop="capacity" label="容量" width="120" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="160" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewMap(row)">地图</el-button>
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
        </el-tab-pane>

        <el-tab-pane label="库存查询" name="stock">
          <el-form :model="stockQueryParams" inline class="search-form">
            <el-form-item label="鞋款ID">
              <el-input v-model="stockQueryParams.shoeId" placeholder="鞋款ID" clearable style="width: 140px;" />
            </el-form-item>
            <el-form-item label="鞋款名称">
              <el-input v-model="stockQueryParams.shoeName" placeholder="鞋款名称" clearable />
            </el-form-item>
            <el-form-item label="尺码">
              <el-input v-model="stockQueryParams.size" placeholder="尺码" clearable style="width: 120px;" />
            </el-form-item>
            <el-form-item label="库存范围">
              <el-input-number v-model="stockQueryParams.stockMin" :min="0" placeholder="最小" />
              <span class="range-sep">-</span>
              <el-input-number v-model="stockQueryParams.stockMax" :min="0" placeholder="最大" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleStockSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleStockReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>

          <el-table :data="stockTableData" v-loading="stockLoading" border stripe>
            <el-table-column prop="shoeId" label="鞋款ID" width="100" />
            <el-table-column prop="shoeName" label="鞋款名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="size" label="尺码" width="100" />
            <el-table-column prop="stock" label="库存" width="120" />
          </el-table>

          <el-pagination
            v-model:current-page="stockQueryParams.page"
            v-model:page-size="stockQueryParams.pageSize"
            :total="stockTotal"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getStockList"
            @current-change="getStockList"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入仓库名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="仓库编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入仓库编码" maxlength="50" />
        </el-form-item>
        <el-form-item label="省市区" prop="regionCodes">
          <el-cascader
            v-model="formData.regionCodes"
            :options="regionOptions"
            :props="regionCascaderProps"
            clearable
            filterable
            style="width: 100%;"
            placeholder="请选择省/市/区"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入详细地址" maxlength="200" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number v-model="formData.capacity" :min="0" :precision="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="经度" prop="longitude">
              <el-input-number
                v-model="formData.longitude"
                :min="-180"
                :max="180"
                :precision="6"
                :step="0.000001"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number
                v-model="formData.latitude"
                :min="-90"
                :max="90"
                :precision="6"
                :step="0.000001"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="4" class="map-col">
            <el-button type="primary" plain class="map-btn" @click="handlePickMap">
              地图选点
            </el-button>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <BaiduMapDialog
      v-model="mapVisible"
      :title="mapTitle"
      :mode="mapMode"
      :longitude="mapLongitude"
      :latitude="mapLatitude"
      :address="mapAddress"
      :city="mapCity"
      @confirm="handleMapConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { regionData, codeToText } from 'element-china-area-data'
import BaiduMapDialog from '@/components/map/BaiduMapDialog.vue'
import {
  getWarehousePage,
  addWarehouse,
  updateWarehouse,
  getWarehouseById,
  deleteWarehouse,
  updateWarehouseStatus,
  getWarehouseStockPage
} from '@/api/warehouse'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('warehouse')

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  name: '',
  code: '',
  status: null,
  province: '',
  city: '',
  regionCodes: [],
  page: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增仓库')
const isEdit = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  name: '',
  code: '',
  longitude: null,
  latitude: null,
  address: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  regionCodes: [],
  status: 1,
  capacity: 0
})

const regionOptions = regionData
const regionCascaderProps = {
  expandTrigger: 'hover',
  emitPath: true,
  checkStrictly: false
}

const queryRegionCascaderProps = {
  expandTrigger: 'hover',
  emitPath: true,
  checkStrictly: true
}

const stockLoading = ref(false)
const stockTableData = ref([])
const stockTotal = ref(0)
const stockQueryParams = reactive({
  shoeId: '',
  shoeName: '',
  size: '',
  stockMin: null,
  stockMax: null,
  page: 1,
  pageSize: 10
})

const formRules = {
  name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  regionCodes: [
    {
      trigger: 'change',
      validator: (_rule, value, callback) => {
        if (!Array.isArray(value) || value.length === 0) {
          callback(new Error('请选择省市区'))
          return
        }
        callback()
      }
    }
  ],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

function normalizeRegionCodes(codes) {
  const arr = Array.isArray(codes) ? codes.filter(Boolean) : []
  return arr.map((x) => String(x))
}

function applyRegionFromCodes(codes) {
  const arr = normalizeRegionCodes(codes)
  if (arr.length === 0) {
    formData.province = ''
    formData.city = ''
    formData.district = ''
    return
  }
  const provinceName = codeToText[arr[0]] || ''
  const cityName = arr.length >= 2 ? (codeToText[arr[1]] || provinceName) : provinceName
  const districtName = arr.length >= 3 ? (codeToText[arr[2]] || '') : (arr.length === 2 ? (codeToText[arr[1]] || '') : '')
  formData.province = provinceName
  formData.city = cityName
  formData.district = districtName
}

function findRegionPathByNames(options, provinceName, cityName, districtName) {
  const p = (provinceName || '').trim()
  const c = (cityName || '').trim()
  const d = (districtName || '').trim()
  if (!p && !c && !d) return []

  const provinceNode = (options || []).find((x) => (x?.label || '') === p)
  const provinceNodes = provinceNode ? [provinceNode] : (options || [])

  const matchLeaf = (nodes, path) => {
    for (const n of nodes || []) {
      const next = [...path, n.value]
      const label = n?.label || ''
      if (d && label === d) return next
      if (!d && c && label === c) return next
      if (n?.children?.length) {
        const found = matchLeaf(n.children, next)
        if (found.length) return found
      }
    }
    return []
  }

  if (provinceNode) {
    if (c && c !== p) {
      const cityNode = (provinceNode.children || []).find((x) => (x?.label || '') === c)
      if (cityNode) {
        const found = matchLeaf(cityNode.children || [], [provinceNode.value, cityNode.value])
        if (found.length) return found
        return [provinceNode.value, cityNode.value]
      }
    }
    const found = matchLeaf(provinceNode.children || [], [provinceNode.value])
    if (found.length) return found
    return [provinceNode.value]
  }

  const found = matchLeaf(provinceNodes, [])
  return found
}

function getFullAddress(row) {
  return `${row.province || ''}${row.city || ''}${row.district || ''}${row.address || ''}` || '-'
}

const mapVisible = ref(false)
const mapMode = ref('view')
const mapTitle = ref('地图')
const mapLongitude = ref(null)
const mapLatitude = ref(null)
const mapAddress = ref('')
const mapCity = ref('')

function fullAddressForForm() {
  return `${formData.province || ''}${formData.city || ''}${formData.district || ''}${formData.address || ''}` || ''
}

function handleViewMap(row) {
  mapMode.value = 'view'
  mapTitle.value = '仓库位置'
  mapLongitude.value = row?.longitude ?? null
  mapLatitude.value = row?.latitude ?? null
  mapAddress.value = getFullAddress(row)
  mapCity.value = row?.city || ''
  mapVisible.value = true
}

function handlePickMap() {
  mapMode.value = 'pick'
  mapTitle.value = '仓库选点'
  mapLongitude.value = formData.longitude ?? null
  mapLatitude.value = formData.latitude ?? null
  mapAddress.value = fullAddressForForm()
  mapCity.value = formData.city || ''
  mapVisible.value = true
}

function handleMapConfirm(payload) {
  if (!payload) return
  if (payload.longitude !== undefined && payload.longitude !== null) {
    formData.longitude = payload.longitude
  }
  if (payload.latitude !== undefined && payload.latitude !== null) {
    formData.latitude = payload.latitude
  }
}

async function getList() {
  loading.value = true
  try {
    const res = await getWarehousePage(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取仓库列表失败:', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.page = 1
  getList()
}

function handleReset() {
  queryParams.name = ''
  queryParams.code = ''
  queryParams.status = null
  queryParams.province = ''
  queryParams.city = ''
  queryParams.regionCodes = []
  queryParams.page = 1
  getList()
}

function handleAdd() {
  dialogTitle.value = '新增仓库'
  isEdit.value = false
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogTitle.value = '编辑仓库'
  isEdit.value = true
  try {
    const res = await getWarehouseById(row.id)
    Object.assign(formData, {
      id: res.id,
      name: res.name,
      code: res.code,
      longitude: res.longitude ?? null,
      latitude: res.latitude ?? null,
      address: res.address,
      phone: res.phone,
      province: res.province,
      city: res.city,
      district: res.district,
      status: res.status,
      capacity: res.capacity ?? 0
    })
    formData.regionCodes = findRegionPathByNames(regionOptions, res.province, res.city, res.district)
    dialogVisible.value = true
  } catch (e) {
    console.error('获取仓库信息失败:', e)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除仓库"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteWarehouse(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (e) {
      console.error('删除失败:', e)
    }
  }).catch(() => {})
}

async function handleStatusChange(row) {
  const nextStatus = row.status
  const prevStatus = nextStatus === 1 ? 0 : 1
  const text = nextStatus === 1 ? '启用' : '禁用'
  try {
    await updateWarehouseStatus(nextStatus, row.id)
    ElMessage.success(`${text}成功`)
  } catch (e) {
    row.status = prevStatus
    console.error('状态更新失败:', e)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      id: formData.id,
      name: formData.name,
      code: formData.code,
      longitude: formData.longitude,
      latitude: formData.latitude,
      address: formData.address,
      phone: formData.phone,
      province: formData.province,
      city: formData.city,
      district: formData.district,
      status: formData.status,
      capacity: formData.capacity
    }
    if (isEdit.value) {
      await updateWarehouse(submitData)
      ElMessage.success('修改成功')
    } else {
      await addWarehouse(submitData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (e) {
    console.error('提交失败:', e)
  } finally {
    submitLoading.value = false
  }
}

function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: null,
    name: '',
    code: '',
    longitude: null,
    latitude: null,
    address: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    regionCodes: [],
    status: 1,
    capacity: 0
  })
}

async function getStockList() {
  stockLoading.value = true
  try {
    const res = await getWarehouseStockPage(stockQueryParams)
    stockTableData.value = res.records || []
    stockTotal.value = res.total || 0
  } catch (e) {
    console.error('获取库存失败:', e)
  } finally {
    stockLoading.value = false
  }
}

function handleStockSearch() {
  stockQueryParams.page = 1
  getStockList()
}

function handleStockReset() {
  stockQueryParams.shoeId = ''
  stockQueryParams.shoeName = ''
  stockQueryParams.size = ''
  stockQueryParams.stockMin = null
  stockQueryParams.stockMax = null
  stockQueryParams.page = 1
  getStockList()
}

watch(
  () => formData.regionCodes,
  (codes) => {
    applyRegionFromCodes(codes)
  },
  { deep: true }
)

watch(
  () => queryParams.regionCodes,
  (codes) => {
    const arr = normalizeRegionCodes(codes)
    if (arr.length === 0) {
      queryParams.province = ''
      queryParams.city = ''
      return
    }
    const provinceName = codeToText[arr[0]] || ''
    const cityName = arr.length >= 2 ? (codeToText[arr[1]] || '') : ''
    queryParams.province = provinceName
    queryParams.city = cityName
  },
  { deep: true }
)

watch(activeTab, (v) => {
  if (v === 'warehouse') {
    getList()
  } else if (v === 'stock') {
    getStockList()
  }
})

getList()
</script>

<style lang="scss" scoped>
.warehouse-container {
  .search-form {
    margin-bottom: 15px;
  }

  .table-operations {
    margin-bottom: 15px;
  }

  .el-pagination {
    margin-top: 20px;
  }

  .map-col {
    display: flex;
    align-items: flex-end;
    padding-bottom: 18px;
  }

  .map-btn {
    width: 100%;
  }
}

.range-sep {
  margin: 0 8px;
  color: var(--el-text-color-regular);
}
</style>
