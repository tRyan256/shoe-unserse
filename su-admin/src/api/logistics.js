import request from '@/utils/request'

export function getLogisticsDetail(orderNo) {
  return request({
    url: `/admin/logistics/${orderNo}`,
    method: 'get'
  })
}

