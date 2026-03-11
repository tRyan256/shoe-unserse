<template>
  <div class="review-container">
    <el-card>
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="鞋款名称">
          <el-input
            v-model="queryParams.shoeName"
            placeholder="请输入鞋款名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="queryParams.userName"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleQuery"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="queryParams.rating" placeholder="请选择" clearable style="width: 140px;">
            <el-option v-for="n in 5" :key="n" :label="`${n} 星`" :value="n" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 140px;">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
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
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="鞋款/组合包" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link
              v-if="row.spuName"
              type="primary"
              :underline="false"
              @click="handleViewSpu(row)"
            >
              {{ row.spuName }}
            </el-link>
            <span v-else class="muted">未知</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link
              v-if="row.userName"
              type="primary"
              :underline="false"
              @click="handleViewUser(row)"
            >
              {{ row.userName }}
            </el-link>
            <span v-else class="muted">未知</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="230">
          <template #default="{ row }">
            <div v-if="row.rating" class="rating-cell">
              <el-rate :model-value="row.rating" disabled :max="5" />
              <el-tag :type="getRatingTagType(row.rating)" size="small">{{ row.rating }} 分</el-tag>
            </div>
            <el-tag v-else type="info" size="small">未评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="images" label="图片" min-width="160">
          <template #default="{ row }">
            <div class="image-list" v-if="row._imageList?.length">
              <el-image
                v-for="(src, idx) in row._imageList.slice(0, 3)"
                :key="idx"
                :src="src"
                :preview-src-list="row._imageList"
                :initial-index="idx"
                :preview-teleported="true"
                :z-index="3000"
                fit="cover"
                class="thumb"
              />
              <el-tag v-if="row._imageList.length > 3" type="info" class="more-tag">
                +{{ row._imageList.length - 3 }}
              </el-tag>
            </div>
            <span v-else class="muted">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(val, row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
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

    <el-dialog v-model="detailVisible" title="评价详情" width="720px" destroy-on-close>
      <div v-if="detailRow" class="detail">
        <div class="detail-head">
          <div class="kv">
            <div class="k">评价ID</div>
            <div class="v">{{ detailRow.id }}</div>
          </div>
          <div class="kv">
            <div class="k">鞋款/组合包</div>
            <div class="v">
              <el-link
                v-if="detailRow.spuName"
                type="primary"
                :underline="false"
                @click="handleViewSpu(detailRow)"
              >
                {{ detailRow.spuName }}
              </el-link>
              <span v-else>—</span>
            </div>
          </div>
          <div class="kv">
            <div class="k">用户</div>
            <div class="v">
              <el-link
                v-if="detailRow.userName"
                type="primary"
                :underline="false"
                @click="handleViewUser(detailRow)"
              >
                {{ detailRow.userName }}
              </el-link>
              <span v-else>—</span>
            </div>
          </div>
          <div class="kv">
            <div class="k">创建时间</div>
            <div class="v">{{ detailRow.createTime }}</div>
          </div>
        </div>
        <div class="detail-rate">
          <div v-if="detailRow.rating" class="rating-detail">
            <el-rate :model-value="detailRow.rating" disabled :max="5" />
            <el-tag :type="getRatingTagType(detailRow.rating)">{{ detailRow.rating }} 分</el-tag>
          </div>
          <el-tag v-else type="info">未评分</el-tag>
        </div>
        <div class="detail-content">
          {{ detailRow.content || '—' }}
        </div>
        <div class="detail-images" v-if="detailRow._imageList?.length">
          <el-image
            v-for="(src, idx) in detailRow._imageList"
            :key="idx"
            :src="src"
            :preview-src-list="detailRow._imageList"
            :initial-index="idx"
            :preview-teleported="true"
            :z-index="3000"
            fit="cover"
            class="detail-img"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="spuVisible" title="SPU详情" width="760px" destroy-on-close>
      <div v-if="spuDetail" class="target-detail">
        <div class="target-head">
          <el-image :src="String(spuDetail.defaultImage || '').trim()" fit="cover" class="target-img">
            <template #error>
              <div class="img-error">加载失败</div>
            </template>
          </el-image>
          <div class="target-meta">
            <div class="target-title">{{ spuDetail.name || '—' }}</div>
            <div class="target-sub">
              <span>{{ spuDetail.brand || '—' }}</span>
              <span class="dot">·</span>
              <span>{{ spuDetail.model || '—' }}</span>
            </div>
            <div class="target-sub">
              <span>价格区间：¥{{ spuDetail.minPrice ?? '—' }} - ¥{{ spuDetail.maxPrice ?? '—' }}</span>
              <span class="dot">·</span>
              <span>状态：{{ spuDetail.status === 1 ? '上架' : '下架' }}</span>
            </div>
          </div>
        </div>
        <div class="target-desc">
          {{ spuDetail.description || '—' }}
        </div>
      </div>
      <template #footer>
        <el-button @click="spuVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="userVisible" title="用户详情" width="620px" destroy-on-close>
      <div v-if="userDetail" class="target-detail">
        <div class="user-head">
          <el-image :src="String(userDetail.avatar || '').trim()" fit="cover" class="avatar-img">
            <template #error>
              <div class="img-error">加载失败</div>
            </template>
          </el-image>
          <div class="user-meta">
            <div class="target-title">{{ userDetail.name || '—' }}</div>
            <div class="target-sub">
              <span>手机号：{{ userDetail.phone || '—' }}</span>
            </div>
            <div class="target-sub">
              <span>注册时间：{{ userDetail.createTime || '—' }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="userVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Refresh, Search } from '@element-plus/icons-vue'
import { deleteComment, getCommentList, updateCommentStatus } from '@/api/comment'
import { getSpuById } from '@/api/shoeSpu'
import { getUserById } from '@/api/user'

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  shoeName: '',
  userName: '',
  rating: '',
  status: ''
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const detailVisible = ref(false)
const detailRow = ref(null)

const spuVisible = ref(false)
const spuDetail = ref(null)

const userVisible = ref(false)
const userDetail = ref(null)

function parseImages(images) {
  if (!images) return []
  const normalizeUrl = (url) => {
    if (url == null) return ''
    const s = String(url).trim()
    if (!s) return ''
    if (
      (s.startsWith('"') && s.endsWith('"')) ||
      (s.startsWith("'") && s.endsWith("'"))
    ) {
      return s.slice(1, -1).trim()
    }
    return s
  }

  if (Array.isArray(images)) return images.map(normalizeUrl).filter(Boolean)

  let raw = String(images).trim()
  if (!raw) return []

  if (
    (raw.startsWith('"') && raw.endsWith('"')) ||
    (raw.startsWith("'") && raw.endsWith("'"))
  ) {
    raw = raw.slice(1, -1).trim()
  }

  if (raw.startsWith('[')) {
    try {
      const arr = JSON.parse(raw)
      if (Array.isArray(arr)) return arr.map(normalizeUrl).filter(Boolean)
    } catch (e) {}
  }

  return raw
    .split(',')
    .map(normalizeUrl)
    .filter((s) => s.length > 0)
}

async function getList() {
  loading.value = true
  try {
    const res = await getCommentList(queryParams)
    total.value = res?.total || 0
    tableData.value = (res?.records || []).map((r) => ({
      ...r,
      _imageList: parseImages(r?.images)
    }))
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.page = 1
  getList()
}

function handleReset() {
  queryParams.page = 1
  queryParams.pageSize = 10
  queryParams.shoeName = ''
  queryParams.userName = ''
  queryParams.rating = ''
  queryParams.status = ''
  getList()
}

function getRatingTagType(rating) {
  const n = Number(rating)
  if (!Number.isFinite(n)) return 'info'
  if (n >= 4) return 'success'
  if (n >= 3) return 'warning'
  return 'danger'
}

function handleSelectionChange(selection) {
  selectedIds.value = (selection || []).map((r) => r?.id).filter((v) => v != null)
}

async function handleStatusChange(val, row) {
  const prev = val === 1 ? 0 : 1
  try {
    await updateCommentStatus(val, row.id)
    ElMessage.success('状态更新成功')
  } catch (e) {
    row.status = prev
  }
}

function handleView(row) {
  detailRow.value = {
    ...row,
    _imageList: row?._imageList || parseImages(row?.images)
  }
  detailVisible.value = true
}

async function handleViewSpu(row) {
  const spuId = row?.spuId
  if (!spuId) return
  try {
    spuDetail.value = await getSpuById(spuId)
    spuVisible.value = true
  } catch (e) {}
}

async function handleViewUser(row) {
  const userId = row?.userId
  if (!userId) return
  try {
    userDetail.value = await getUserById(userId)
    userVisible.value = true
  } catch (e) {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该评价？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteComment(row.id)
  ElMessage.success('删除成功')
  await getList()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条评价？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  for (const id of selectedIds.value) {
    await deleteComment(id)
  }
  ElMessage.success('删除成功')
  selectedIds.value = []
  await getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.review-container {
  padding: 0;
}

.table-operations {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.image-list {
  display: flex;
  align-items: center;
  gap: 8px;
}

.thumb {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.more-tag {
  height: 24px;
}

.muted {
  color: #909399;
}

.rating-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-detail {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-head {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.kv {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 10px 12px;
  background: #fafafa;
}

.k {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.v {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.detail-content {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}

.detail-images {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

.detail-img {
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.target-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.target-head {
  display: flex;
  gap: 14px;
}

.target-img {
  width: 88px;
  height: 88px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.avatar-img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.target-meta,
.user-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.target-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.target-sub {
  font-size: 13px;
  color: #606266;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.dot {
  color: #c0c4cc;
}

.target-desc {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}

.user-head {
  display: flex;
  gap: 14px;
  align-items: center;
}

.img-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #fafafa;
  border-radius: inherit;
}
</style>
