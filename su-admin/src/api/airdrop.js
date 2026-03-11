import request from '@/utils/request'

// 空投活动列表分页查询
export function getAirdropList(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/airdrop/page',
    method: 'get',
    params: p
  })
}

// 新增空投活动
export function addAirdrop(data) {
  return request({
    url: '/admin/airdrop',
    method: 'post',
    data
  })
}

// 取消空投活动
export function cancelAirdrop(id) {
  return request({
    url: `/admin/airdrop/cancel/${id}`,
    method: 'post'
  })
}

// 获取优惠券分页(用于关联选择)
export function getCouponOptions(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/coupon/page',
    method: 'get',
    params: p
  })
}
