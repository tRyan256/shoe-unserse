import request from '@/utils/request'

export function getCommentList(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/comment/page',
    method: 'get',
    params: p
  })
}

export function deleteComment(id) {
  return request({
    url: `/admin/comment/${id}`,
    method: 'delete'
  })
}

export function updateCommentStatus(status, id) {
  return request({
    url: `/admin/comment/status/${status}`,
    method: 'post',
    params: { id }
  })
}
