import request from '@/utils/request'

// 营业额统计
export function getTurnoverStatistics(params) {
  return request({
    url: '/admin/report/turnoverStatistics',
    method: 'get',
    params
  })
}

// 用户统计
export function getUserStatistics(params) {
  return request({
    url: '/admin/report/userStatistics',
    method: 'get',
    params
  })
}

// 订单统计
export function getOrderStatistics(params) {
  return request({
    url: '/admin/report/ordersStatistics',
    method: 'get',
    params
  })
}

// 销量Top10
export function getTop10Statistics(params) {
  return request({
    url: '/admin/report/top10',
    method: 'get',
    params
  })
}

// 导出运营数据报表
export function exportReport() {
  return request({
    url: '/admin/report/export',
    method: 'get',
    responseType: 'blob'
  })
}

// 获取总营业额（所有已签收订单）
export function getTotalTurnover() {
  return request({
    url: '/admin/report/totalTurnover',
    method: 'get'
  })
}

// 获取总有效订单数（所有已签收订单）
export function getTotalValidOrderCount() {
  return request({
    url: '/admin/report/totalValidOrderCount',
    method: 'get'
  })
}
