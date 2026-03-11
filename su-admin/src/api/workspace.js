import request from '@/utils/request'

// 获取今日营业数据
export function getBusinessData() {
  return request({
    url: '/admin/workspace/businessData',
    method: 'get'
  })
}

// 获取订单概览
export function getOrderOverview() {
  return request({
    url: '/admin/workspace/overviewOrders',
    method: 'get'
  })
}

// 获取鞋款概览
export function getShoeOverview() {
  return request({
    url: '/admin/workspace/overviewShoes',
    method: 'get'
  })
}

// 获取组合包概览
export function getBundleOverview() {
  return request({
    url: '/admin/workspace/overviewBundles',
    method: 'get'
  })
}
