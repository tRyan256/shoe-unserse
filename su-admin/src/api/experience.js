import request from '@/utils/request'

// ==================== 心得管理 ====================

// 分页查询心得列表
export function getExperiencePostList(params) {
    return request({
        url: '/admin/experience/posts',
        method: 'get',
        params
    })
}

// 删除心得
export function deleteExperiencePost(id) {
    return request({
        url: `/admin/experience/posts/${id}`,
        method: 'delete'
    })
}

// 批量删除心得
export function batchDeleteExperiencePosts(ids) {
    return request({
        url: '/admin/experience/posts/batch',
        method: 'delete',
        data: ids
    })
}

// 隐藏心得
export function hideExperiencePost(id) {
    return request({
        url: `/admin/experience/posts/${id}/hide`,
        method: 'put'
    })
}

// 取消隐藏心得
export function unhideExperiencePost(id) {
    return request({
        url: `/admin/experience/posts/${id}/unhide`,
        method: 'put'
    })
}

// ==================== 评论管理 ====================

// 分页查询评论列表
export function getExperienceCommentList(params) {
    return request({
        url: '/admin/experience/comments',
        method: 'get',
        params
    })
}

// 删除评论
export function deleteExperienceComment(id) {
    return request({
        url: `/admin/experience/comments/${id}`,
        method: 'delete'
    })
}

// 批量删除评论
export function batchDeleteExperienceComments(ids) {
    return request({
        url: '/admin/experience/comments/batch',
        method: 'delete',
        data: ids
    })
}

// ==================== 回复管理 ====================

// 根据评论ID查询回复列表
export function getExperienceReplyList(commentId) {
    return request({
        url: `/admin/experience/comments/${commentId}/replies`,
        method: 'get'
    })
}

// 删除回复
export function deleteExperienceReply(id) {
    return request({
        url: `/admin/experience/replies/${id}`,
        method: 'delete'
    })
}

// 批量删除回复
export function batchDeleteExperienceReplies(ids) {
    return request({
        url: '/admin/experience/replies/batch',
        method: 'delete',
        data: ids
    })
}
