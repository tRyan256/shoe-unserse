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
