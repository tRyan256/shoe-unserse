<template>
  <div class="experience-post-container">
    <el-card>
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="用户名">
          <el-input 
            v-model="queryParams.userName" 
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleQuery"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="内容关键词">
          <el-input 
            v-model="queryParams.contentKeyword" 
            placeholder="请输入内容关键词"
            clearable
            @keyup.enter="handleQuery"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="商品类型">
          <el-select 
            v-model="queryParams.productType" 
            placeholder="请选择商品类型" 
            clearable
            style="width: 150px;"
          >
            <el-option label="鞋款" :value="1" />
            <el-option label="组合包" :value="2" />
            <el-option label="抽签" :value="3" />
            <el-option label="空投" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input 
            v-model="queryParams.productName" 
            placeholder="请输入商品名称"
            clearable
            @keyup.enter="handleQuery"
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 280px;"
          />
        </el-form-item>
        <el-form-item label="隐藏状态">
          <el-select 
            v-model="queryParams.hidden" 
            placeholder="请选择状态" 
            clearable
            style="width: 120px;"
          >
            <el-option label="未隐藏" :value="0" />
            <el-option label="已隐藏" :value="1" />
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
        <span v-if="selectedIds.length > 0" class="selection-tip">
          已选择 <span class="selection-count">{{ selectedIds.length }}</span> 条
        </span>
      </div>

      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="userName" label="用户" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.userAvatar" :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contentSummary" label="内容摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="productTypeName" label="商品类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getProductTypeTag(row.productType)">
              {{ row.productTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <div v-if="row.images && row.images.length > 0" class="image-cell">
              <el-image 
                :src="row.images[0]" 
                :preview-src-list="row.images"
                fit="cover"
                class="table-image"
                preview-teleported
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <span v-if="row.images.length > 1" class="image-count">+{{ row.images.length - 1 }}</span>
            </div>
            <span v-else style="color: #909399;">无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="likeCount" label="点赞数" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="danger" size="small">
              <el-icon><Star /></el-icon>
              {{ row.likeCount || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="commentCount" label="评论数" width="90" align="center">
          <template #default="{ row }">
            <el-link 
              type="primary" 
              :underline="false"
              @click="handleViewComments(row)"
              class="comment-link"
            >
              <el-tag type="info" size="small">
                <el-icon><ChatDotRound /></el-icon>
                {{ row.commentCount || 0 }}
              </el-tag>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="hidden" label="隐藏状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hidden === 1 ? 'danger' : 'success'">
              {{ row.hidden === 1 ? '已隐藏' : '未隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button type="primary" link @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button type="info" link @click="handleViewComments(row)">
              评论
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
      v-model="detailDialogVisible"
      title="心得详情"
      width="700px"
      destroy-on-close
    >
      <div v-if="currentPost" class="post-detail">
        <div class="detail-section">
          <div class="section-title">用户信息</div>
          <div class="user-detail">
            <el-avatar :src="currentPost.userAvatar" :size="50">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-detail-info">
              <div class="user-name">{{ currentPost.userName }}</div>
              <div class="user-stats">
                <span class="stat-tag">{{ currentPost.followingCount || 0 }} 关注</span>
                <span class="stat-tag">{{ currentPost.followerCount || 0 }} 粉丝</span>
                <span class="stat-tag">{{ currentPost.likedCount || 0 }} 获赞</span>
              </div>
              <div class="post-time">{{ currentPost.createTime }}</div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">商品信息</div>
          <div class="product-info">
            <el-tag :type="getProductTypeTag(currentPost.productType)">
              {{ currentPost.productTypeName }}
            </el-tag>
            <span class="product-name">{{ currentPost.productName }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">心得内容</div>
          <div class="post-content">{{ currentPost.content }}</div>
        </div>

        <div v-if="currentPost.images && currentPost.images.length > 0" class="detail-section">
          <div class="section-title">图片 ({{ currentPost.images.length }}张)</div>
          <div class="post-images">
            <el-image 
              v-for="(img, index) in currentPost.images" 
              :key="index"
              :src="img" 
              :preview-src-list="currentPost.images"
              :initial-index="index"
              fit="cover"
              class="post-image"
              preview-teleported
            />
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">统计信息</div>
          <div class="post-stats">
            <div class="stat-item">
              <el-icon color="#f56c6c"><Star /></el-icon>
              <span>点赞数：{{ currentPost.likeCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon color="#909399"><ChatDotRound /></el-icon>
              <span>评论数：{{ currentPost.commentCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-tag :type="currentPost.hidden === 1 ? 'danger' : 'success'">
                {{ currentPost.hidden === 1 ? '已隐藏' : '未隐藏' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="commentDialogVisible"
      :title="commentDialogTitle"
      width="1000px"
      destroy-on-close
      @closed="handleCommentDialogClosed"
    >
      <div class="comment-dialog-content">
        <div class="comment-operations">
          <el-button 
            type="danger" 
            :disabled="selectedCommentIds.length === 0"
            @click="handleBatchDeleteComments"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <span v-if="selectedCommentIds.length > 0" class="selection-tip">
            已选择 <span class="selection-count">{{ selectedCommentIds.length }}</span> 条
          </span>
        </div>

        <el-table 
          :data="commentTableData" 
          v-loading="commentLoading" 
          border 
          stripe
          max-height="500"
          @selection-change="handleCommentSelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="userName" label="评论用户" width="140">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :src="row.userAvatar" :size="32">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="user-name">{{ row.userName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="评论内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="likeCount" label="点赞数" width="80" align="center">
            <template #default="{ row }">
              <el-tag type="danger" size="small">
                <el-icon><Star /></el-icon>
                {{ row.likeCount || 0 }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="replyCount" label="回复数" width="90" align="center">
            <template #default="{ row }">
              <el-link 
                type="primary" 
                :underline="false"
                @click="handleViewReplies(row)"
                class="reply-link"
              >
                <el-tag type="info" size="small">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ row.replyCount || 0 }}
                </el-tag>
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评论时间" width="160" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="danger" 
                link 
                @click="handleDeleteComment(row)"
              >
                删除
              </el-button>
              <el-button type="primary" link @click="handleViewCommentDetail(row)">
                详情
              </el-button>
              <el-button type="info" link @click="handleViewReplies(row)">
                回复
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="commentQueryParams.page"
          v-model:page-size="commentQueryParams.pageSize"
          :total="commentTotal"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getCommentList"
          @current-change="getCommentList"
        />
      </div>
    </el-dialog>

    <el-dialog
      v-model="commentDetailDialogVisible"
      title="评论详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentComment" class="comment-detail">
        <div class="detail-section">
          <div class="section-title">评论用户</div>
          <div class="user-detail">
            <el-avatar :src="currentComment.userAvatar" :size="50">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-detail-info">
              <div class="user-name">{{ currentComment.userName }}</div>
              <div class="comment-time">{{ currentComment.createTime }}</div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">评论内容</div>
          <div class="comment-content">{{ currentComment.content }}</div>
        </div>

        <div class="detail-section">
          <div class="section-title">统计信息</div>
          <div class="comment-stats">
            <div class="stat-item">
              <el-icon color="#f56c6c"><Star /></el-icon>
              <span>点赞数：{{ currentComment.likeCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon color="#909399"><ChatDotRound /></el-icon>
              <span>回复数：{{ currentComment.replyCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-tag :type="currentComment.hidden === 1 ? 'danger' : 'success'">
                {{ currentComment.hidden === 1 ? '已隐藏' : '未隐藏' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="replyDialogVisible"
      :title="replyDialogTitle"
      width="900px"
      destroy-on-close
      @closed="handleReplyDialogClosed"
    >
      <div class="reply-dialog-content">
        <div class="reply-operations">
          <el-button 
            type="danger" 
            :disabled="selectedReplyIds.length === 0"
            @click="handleBatchDeleteReplies"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <span v-if="selectedReplyIds.length > 0" class="selection-tip">
            已选择 <span class="selection-count">{{ selectedReplyIds.length }}</span> 条
          </span>
        </div>

        <el-table 
          :data="replyTableData" 
          v-loading="replyLoading" 
          border 
          stripe
          max-height="500"
          @selection-change="handleReplySelectionChange"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column prop="userName" label="回复用户" width="140">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :src="row.userAvatar" :size="32">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="user-name">{{ row.userName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="targetUserName" label="被回复用户" width="140">
            <template #default="{ row }">
              <span v-if="row.targetUserName" class="target-user">{{ row.targetUserName }}</span>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="回复内容" min-width="250" show-overflow-tooltip />
          <el-table-column prop="likeCount" label="点赞数" width="80" align="center">
            <template #default="{ row }">
              <el-tag type="danger" size="small">
                <el-icon><Star /></el-icon>
                {{ row.likeCount || 0 }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isAuthor" label="作者回复" width="90" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.isAuthor === 1" type="success" size="small">是</el-tag>
              <el-tag v-else type="info" size="small">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="回复时间" width="160" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="danger" 
                link 
                @click="handleDeleteReply(row)"
              >
                删除
              </el-button>
              <el-button type="primary" link @click="handleViewReplyDetail(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog
      v-model="replyDetailDialogVisible"
      title="回复详情"
      width="600px"
      destroy-on-close
    >
      <div v-if="currentReply" class="reply-detail">
        <div class="detail-section">
          <div class="section-title">回复用户</div>
          <div class="user-detail">
            <el-avatar :src="currentReply.userAvatar" :size="50">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="user-detail-info">
              <div class="user-name">{{ currentReply.userName }}</div>
              <div class="reply-time">{{ currentReply.createTime }}</div>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="currentReply.targetUserName">
          <div class="section-title">被回复用户</div>
          <div class="target-user-info">
            <span>{{ currentReply.targetUserName }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">回复内容</div>
          <div class="reply-content">{{ currentReply.content }}</div>
        </div>

        <div class="detail-section">
          <div class="section-title">其他信息</div>
          <div class="reply-stats">
            <div class="stat-item">
              <el-icon color="#f56c6c"><Star /></el-icon>
              <span>点赞数：{{ currentReply.likeCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-tag :type="currentReply.isAuthor === 1 ? 'success' : 'info'">
                {{ currentReply.isAuthor === 1 ? '作者回复' : '普通回复' }}
              </el-tag>
            </div>
            <div class="stat-item">
              <el-tag :type="currentReply.hidden === 1 ? 'danger' : 'success'">
                {{ currentReply.hidden === 1 ? '已隐藏' : '未隐藏' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { 
  getExperiencePostList,
  deleteExperiencePost,
  batchDeleteExperiencePosts,
  getExperienceCommentList,
  deleteExperienceComment,
  batchDeleteExperienceComments,
  getExperienceReplyList,
  deleteExperienceReply,
  batchDeleteExperienceReplies
} from '@/api/experience'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Delete, 
  User, 
  Picture, 
  Star, 
  ChatDotRound 
} from '@element-plus/icons-vue'

const loading = ref(false)
const dateRange = ref([])

const queryParams = reactive({
  userName: '',
  contentKeyword: '',
  productType: null,
  productName: '',
  startTime: '',
  endTime: '',
  hidden: null,
  page: 1,
  pageSize: 10
})

const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const detailDialogVisible = ref(false)
const currentPost = ref(null)

const commentDialogVisible = ref(false)
const commentLoading = ref(false)
const currentCommentPost = ref(null)
const commentQueryParams = reactive({
  postId: null,
  page: 1,
  pageSize: 10
})
const commentTableData = ref([])
const commentTotal = ref(0)
const selectedCommentIds = ref([])

const commentDetailDialogVisible = ref(false)
const currentComment = ref(null)

const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const currentReplyComment = ref(null)
const replyTableData = ref([])
const selectedReplyIds = ref([])

const replyDetailDialogVisible = ref(false)
const currentReply = ref(null)

const commentDialogTitle = computed(() => {
  if (currentCommentPost.value) {
    return `评论管理 - 心得ID: ${currentCommentPost.value.id}`
  }
  return '评论管理'
})

const replyDialogTitle = computed(() => {
  if (currentReplyComment.value) {
    return `回复管理 - 评论ID: ${currentReplyComment.value.id}`
  }
  return '回复管理'
})

async function getList() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }

    const res = await getExperiencePostList(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取心得列表失败:', error)
    ElMessage.error('获取心得列表失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.page = 1
  getList()
}

function handleReset() {
  queryParams.userName = ''
  queryParams.contentKeyword = ''
  queryParams.productType = null
  queryParams.productName = ''
  queryParams.hidden = null
  dateRange.value = []
  queryParams.page = 1
  getList()
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(item => item.id)
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除用户"${row.userName}"的这条心得吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExperiencePost(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要删除的心得')
    return
  }
  
  ElMessageBox.confirm(`确定要批量删除选中的 ${selectedIds.value.length} 条心得吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchDeleteExperiencePosts(selectedIds.value)
      if (res.successCount > 0) {
        ElMessage.success(`成功删除 ${res.successCount} 条心得`)
      }
      if (res.failureCount > 0) {
        ElMessage.warning(`${res.failureCount} 条心得删除失败`)
      }
      selectedIds.value = []
      getList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

function handleViewDetail(row) {
  currentPost.value = row
  detailDialogVisible.value = true
}

function handleViewComments(row) {
  currentCommentPost.value = row
  commentQueryParams.postId = row.id
  commentQueryParams.page = 1
  commentDialogVisible.value = true
  getCommentList()
}

async function getCommentList() {
  commentLoading.value = true
  try {
    const res = await getExperienceCommentList(commentQueryParams)
    commentTableData.value = res.records || []
    commentTotal.value = res.total || 0
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    commentLoading.value = false
  }
}

function handleCommentDialogClosed() {
  currentCommentPost.value = null
  commentTableData.value = []
  commentTotal.value = 0
  selectedCommentIds.value = []
  getList()
}

function handleCommentSelectionChange(selection) {
  selectedCommentIds.value = selection.map(item => item.id)
}

function handleDeleteComment(row) {
  ElMessageBox.confirm(`确定要删除用户"${row.userName}"的这条评论吗？删除后将同时删除该评论下的所有回复，且无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExperienceComment(row.id)
      ElMessage.success('删除成功')
      getCommentList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

function handleBatchDeleteComments() {
  if (selectedCommentIds.value.length === 0) {
    ElMessage.warning('请选择要删除的评论')
    return
  }
  
  ElMessageBox.confirm(`确定要批量删除选中的 ${selectedCommentIds.value.length} 条评论吗？删除后将同时删除所有评论下的回复，且无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchDeleteExperienceComments(selectedCommentIds.value)
      if (res.successCount > 0) {
        ElMessage.success(`成功删除 ${res.successCount} 条评论`)
      }
      if (res.failureCount > 0) {
        ElMessage.warning(`${res.failureCount} 条评论删除失败`)
      }
      selectedCommentIds.value = []
      getCommentList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

function handleViewCommentDetail(row) {
  currentComment.value = row
  commentDetailDialogVisible.value = true
}

function handleViewReplies(row) {
  currentReplyComment.value = row
  replyDialogVisible.value = true
  getReplyList()
}

async function getReplyList() {
  replyLoading.value = true
  try {
    const res = await getExperienceReplyList(currentReplyComment.value.id)
    replyTableData.value = res || []
  } catch (error) {
    console.error('获取回复列表失败:', error)
    ElMessage.error('获取回复列表失败')
  } finally {
    replyLoading.value = false
  }
}

function handleReplyDialogClosed() {
  currentReplyComment.value = null
  replyTableData.value = []
  selectedReplyIds.value = []
  getCommentList()
}

function handleReplySelectionChange(selection) {
  selectedReplyIds.value = selection.map(item => item.id)
}

function handleDeleteReply(row) {
  ElMessageBox.confirm(`确定要删除用户"${row.userName}"的这条回复吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExperienceReply(row.id)
      ElMessage.success('删除成功')
      getReplyList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

function handleBatchDeleteReplies() {
  if (selectedReplyIds.value.length === 0) {
    ElMessage.warning('请选择要删除的回复')
    return
  }
  
  ElMessageBox.confirm(`确定要批量删除选中的 ${selectedReplyIds.value.length} 条回复吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchDeleteExperienceReplies(selectedReplyIds.value)
      if (res.successCount > 0) {
        ElMessage.success(`成功删除 ${res.successCount} 条回复`)
      }
      if (res.failureCount > 0) {
        ElMessage.warning(`${res.failureCount} 条回复删除失败`)
      }
      selectedReplyIds.value = []
      getReplyList()
    } catch (error) {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

function handleViewReplyDetail(row) {
  currentReply.value = row
  replyDetailDialogVisible.value = true
}

function getProductTypeTag(type) {
  const tagMap = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return tagMap[type] || 'info'
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.experience-post-container {
  .search-form {
    margin-bottom: 15px;
  }
  
  .table-operations {
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 15px;

    .selection-tip {
      color: #606266;
      font-size: 14px;

      .selection-count {
        color: #409eff;
        font-weight: 600;
        margin: 0 2px;
      }
    }
  }
  
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .user-name {
      font-size: 14px;
      color: #303133;
    }
  }

  .image-placeholder {
    width: 80px;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f7fa;
    color: #909399;
    font-size: 24px;
    border-radius: 4px;
  }

  .image-cell {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;

    .table-image {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      cursor: pointer;
    }

    .image-count {
      position: absolute;
      right: 2px;
      bottom: 2px;
      background-color: rgba(0, 0, 0, 0.6);
      color: #fff;
      font-size: 12px;
      padding: 1px 4px;
      border-radius: 2px;
      line-height: 1;
    }
  }

  .comment-link, .reply-link {
    cursor: pointer;
    
    &:hover {
      opacity: 0.8;
    }
  }
}

.post-detail {
  .detail-section {
    margin-bottom: 20px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
      padding-bottom: 8px;
      border-bottom: 1px solid #e4e7ed;
    }

    .user-detail {
      display: flex;
      align-items: center;
      gap: 15px;

      .user-detail-info {
        .user-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 5px;
        }

        .user-stats {
          display: flex;
          gap: 10px;
          margin-bottom: 5px;

          .stat-tag {
            font-size: 13px;
            color: #606266;
            background-color: #f4f4f5;
            padding: 2px 8px;
            border-radius: 4px;
          }
        }

        .post-time {
          font-size: 13px;
          color: #909399;
        }
      }
    }

    .product-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .product-name {
        font-size: 14px;
        color: #303133;
      }
    }

    .post-content {
      font-size: 14px;
      line-height: 1.8;
      color: #606266;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .post-images {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .post-image {
        width: 96px;
        height: 96px;
        border-radius: 4px;
        cursor: pointer;
      }
    }

    .post-stats {
      display: flex;
      gap: 30px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.comment-dialog-content {
  .comment-operations {
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 15px;

    .selection-tip {
      color: #606266;
      font-size: 14px;

      .selection-count {
        color: #409eff;
        font-weight: 600;
        margin: 0 2px;
      }
    }
  }

  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .user-name {
      font-size: 14px;
      color: #303133;
    }
  }
}

.comment-detail {
  .detail-section {
    margin-bottom: 20px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
      padding-bottom: 8px;
      border-bottom: 1px solid #e4e7ed;
    }

    .user-detail {
      display: flex;
      align-items: center;
      gap: 15px;

      .user-detail-info {
        .user-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 5px;
        }

        .comment-time {
          font-size: 13px;
          color: #909399;
        }
      }
    }

    .comment-content {
      font-size: 14px;
      line-height: 1.8;
      color: #606266;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .comment-stats {
      display: flex;
      gap: 30px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.reply-dialog-content {
  .reply-operations {
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 15px;

    .selection-tip {
      color: #606266;
      font-size: 14px;

      .selection-count {
        color: #409eff;
        font-weight: 600;
        margin: 0 2px;
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;

    .user-name {
      font-size: 14px;
      color: #303133;
    }
  }

  .target-user {
    font-size: 14px;
    color: #606266;
  }
}

.reply-detail {
  .detail-section {
    margin-bottom: 20px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 10px;
      padding-bottom: 8px;
      border-bottom: 1px solid #e4e7ed;
    }

    .user-detail {
      display: flex;
      align-items: center;
      gap: 15px;

      .user-detail-info {
        .user-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 5px;
        }

        .reply-time {
          font-size: 13px;
          color: #909399;
        }
      }
    }

    .target-user-info {
      font-size: 14px;
      color: #606266;
    }

    .reply-content {
      font-size: 14px;
      line-height: 1.8;
      color: #606266;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .reply-stats {
      display: flex;
      gap: 30px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

/* 图片预览样式 - 确保图片完整显示 */
:deep(.el-image-viewer__img) {
  max-width: 90vw !important;
  max-height: 90vh !important;
  width: auto !important;
  height: auto !important;
  object-fit: contain !important;
}

:deep(.el-image-viewer__canvas) {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
