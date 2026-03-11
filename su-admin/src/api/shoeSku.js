import request from '@/utils/request'

// 新增SKU
export function addSku(data) {
  return request({
    url: '/admin/shoe/sku',
    method: 'post',
    data
  })
}

// 修改SKU
export function updateSku(data) {
  return request({
    url: '/admin/shoe/sku',
    method: 'put',
    data
  })
}

// 根据ID查询SKU详情
export function getSkuById(id) {
  return request({
    url: `/admin/shoe/sku/${id}`,
    method: 'get'
  })
}

// 删除SKU
export function deleteSku(id) {
  return request({
    url: `/admin/shoe/sku/${id}`,
    method: 'delete'
  })
}

// 根据SPU ID查询SKU列表
export function getSkuListBySpuId(spuId) {
  return request({
    url: `/admin/shoe/sku/list/${spuId}`,
    method: 'get'
  })
}

// 更新SKU状态
export function updateSkuStatus(status, id) {
  return request({
    url: `/admin/shoe/sku/status/${status}`,
    method: 'post',
    params: { id }
  })
}

// 设置默认SKU
export function setDefaultSku(spuId, skuId) {
  return request({
    url: `/admin/shoe/sku/default/${skuId}`,
    method: 'post',
    params: { spuId }
  })
}
