import request from '@/utils/request'

// 组合包列表分页查询
export function getBundleList(params) {
  return request({
    url: '/admin/bundle/page',
    method: 'get',
    params
  })
}

// 新增组合包
export function addBundle(data) {
  return request({
    url: '/admin/bundle',
    method: 'post',
    data
  })
}

// 修改组合包
export function updateBundle(data) {
  return request({
    url: '/admin/bundle',
    method: 'put',
    data
  })
}

// 根据ID查询组合包
export function getBundleById(id) {
  return request({
    url: `/admin/bundle/${id}`,
    method: 'get'
  })
}

// 删除组合包
export function deleteBundle(ids) {
  const idsParam = Array.isArray(ids) ? ids : [ids]
  const idsStr = idsParam.map(id => `ids=${id}`).join('&')
  return request({
    url: `/admin/bundle?${idsStr}`,
    method: 'delete'
  })
}

// 启用/禁用组合包（上下架）
export function updateBundleStatus(status, id) {
  return request({
    url: `/admin/bundle/status/${status}`,
    method: 'post',
    params: { id }
  })
}

// 上传组合包图片
export function uploadBundleImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
