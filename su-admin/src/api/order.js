import request from '@/utils/request'

// 订单列表分页查询
export function getOrderList(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/order/conditionSearch',
    method: 'get',
    params: p
  })
}

// 订单详情
export function getOrderDetail(id) {
  return request({
    url: `/admin/order/details/${id}`,
    method: 'get'
  })
}

// 取消订单
export function cancelOrder(data) {
  return request({
    url: '/admin/order/cancel',
    method: 'put',
    data
  })
}

// 接单
export function confirmOrder(data) {
  return request({
    url: `/admin/order/confirm`,
    method: 'put',
    data
  })
}

// 拒单
export function rejectOrder(data) {
  return request({
    url: '/admin/order/rejection',
    method: 'put',
    data
  })
}

// 派送
export function deliverOrder(id) {
  return request({
    url: `/admin/order/delivery/${id}`,
    method: 'put'
  })
}

export function outForDeliveryOrder(id) {
  return request({
    url: `/admin/order/outForDelivery/${id}`,
    method: 'put'
  })
}

// 完成订单
export function completeOrder(id) {
  return request({
    url: `/admin/order/complete/${id}`,
    method: 'put'
  })
}

// 各状态订单数量统计
export function getOrderStatistics() {
  return request({
    url: '/admin/order/statistics',
    method: 'get'
  })
}
