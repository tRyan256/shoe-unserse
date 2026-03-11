<template>
  <div class="outlet-container">
    <el-card>
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="门店名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入门店名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="门店编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入门店编码"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择" clearable style="width: 120px;">
            <el-option label="门店" :value="1" />
            <el-option label="自提点" :value="2" />
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

      <div class="table-operations">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增门店
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="name" label="门店名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="code" label="门店编码" width="140" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'">
              {{ row.type === 1 ? '门店' : '自提点' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="地址" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getFullAddress(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="140" show-overflow-tooltip />
        <el-table-column prop="businessHours" label="营业时间" width="140" show-overflow-tooltip />
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
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入门店名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="门店编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入门店编码" maxlength="50" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择" style="width: 100%;">
                <el-option label="门店" :value="1" />
                <el-option label="自提点" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="省市区" prop="regionCodes">
          <el-cascader
            v-model="formData.regionCodes"
            :options="regionOptions"
            :props="regionCascaderProps"
            placeholder="请选择省/市/区"
            clearable
            filterable
            style="width: 100%;"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入电话" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="营业时间" prop="businessHours">
              <el-input v-model="formData.businessHours" placeholder="如：09:00-22:00" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入详细地址" maxlength="200" />
        </el-form-item>
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
  getOutletList,
  addOutlet,
  updateOutlet,
  getOutletById,
  deleteOutlet,
  updateOutletStatus
} from '@/api/outlet'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)

const queryParams = reactive({
  name: '',
  code: '',
  type: null,
  status: null,
  page: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('新增门店')
const isEdit = ref(false)
const formRef = ref(null)

const formData = reactive({
  id: null,
  name: '',
  code: '',
  type: 1,
  longitude: null,
  latitude: null,
  address: '',
  phone: '',
  businessHours: '',
  province: '',
  city: '',
  district: '',
  regionCodes: [],
  status: 1
})

const regionOptions = regionData
const regionCascaderProps = {
  expandTrigger: 'hover',
  emitPath: true,
  checkStrictly: false
}

const formRules = {
  name: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入门店编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
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
  ]
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
  mapTitle.value = '门店位置'
  mapLongitude.value = row?.longitude ?? null
  mapLatitude.value = row?.latitude ?? null
  mapAddress.value = getFullAddress(row)
  mapCity.value = row?.city || ''
  mapVisible.value = true
}

function handlePickMap() {
  mapMode.value = 'pick'
  mapTitle.value = '门店选点'
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

async function getList() {
  loading.value = true
  try {
    const res = await getOutletList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取门店列表失败:', error)
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
  queryParams.type = null
  queryParams.status = null
  queryParams.page = 1
  getList()
}

function handleAdd() {
  dialogTitle.value = '新增门店'
  isEdit.value = false
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogTitle.value = '编辑门店'
  isEdit.value = true
  try {
    const res = await getOutletById(row.id)
    Object.assign(formData, {
      id: res.id,
      name: res.name,
      code: res.code,
      type: res.type,
      longitude: res.longitude ?? null,
      latitude: res.latitude ?? null,
      address: res.address,
      phone: res.phone,
      businessHours: res.businessHours,
      province: res.province,
      city: res.city,
      district: res.district,
      status: res.status
    })
    formData.regionCodes = findRegionPathByNames(regionOptions, res.province, res.city, res.district)
    dialogVisible.value = true
  } catch (error) {
    console.error('获取门店信息失败:', error)
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除门店"${row.name}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteOutlet(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

async function handleStatusChange(row) {
  const nextStatus = row.status
  const prevStatus = nextStatus === 1 ? 0 : 1
  const text = nextStatus === 1 ? '启用' : '禁用'
  try {
    await updateOutletStatus(nextStatus, row.id)
    ElMessage.success(`${text}成功`)
  } catch (error) {
    row.status = prevStatus
    console.error('状态更新失败:', error)
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
      type: formData.type,
      longitude: formData.longitude,
      latitude: formData.latitude,
      address: formData.address,
      phone: formData.phone,
      businessHours: formData.businessHours,
      province: formData.province,
      city: formData.city,
      district: formData.district,
      status: formData.status
    }

    if (isEdit.value) {
      await updateOutlet(submitData)
      ElMessage.success('修改成功')
    } else {
      await addOutlet(submitData)
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

function handleDialogClose() {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: null,
    name: '',
    code: '',
    type: 1,
    longitude: null,
    latitude: null,
    address: '',
    phone: '',
    businessHours: '',
    province: '',
    city: '',
    district: '',
    regionCodes: [],
    status: 1
  })
}

watch(
  () => formData.regionCodes,
  (codes) => {
    applyRegionFromCodes(codes)
  },
  { deep: true }
)

getList()
</script>

<style lang="scss" scoped>
.outlet-container {
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
</style>
