import request from '@/utils/request'

export function getWarehousePage(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/warehouse/page',
    method: 'get',
    params: p
  })
}

export function getWarehouseById(id) {
  return request({
    url: `/admin/warehouse/${id}`,
    method: 'get'
  })
}

export function addWarehouse(data) {
  return request({
    url: '/admin/warehouse',
    method: 'post',
    data
  })
}

export function updateWarehouse(data) {
  return request({
    url: '/admin/warehouse',
    method: 'put',
    data
  })
}

export function deleteWarehouse(id) {
  return request({
    url: `/admin/warehouse/${id}`,
    method: 'delete'
  })
}

export function updateWarehouseStatus(status, id) {
  return request({
    url: `/admin/warehouse/status/${status}`,
    method: 'post',
    params: { id }
  })
}

export function getWarehouseStockPage(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/warehouse/stock/page',
    method: 'get',
    params: p
  })
}

