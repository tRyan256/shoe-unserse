import request from '@/utils/request'

// 优惠券列表分页查询
export function getCouponList(params) {
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

// 新增优惠券
export function addCoupon(data) {
  return request({
    url: '/admin/coupon',
    method: 'post',
    data
  })
}

// 修改优惠券
export function updateCoupon(data) {
  return request({
    url: '/admin/coupon',
    method: 'put',
    data
  })
}

// 根据ID查询优惠券
export function getCouponById(id) {
  return request({
    url: `/admin/coupon/${id}`,
    method: 'get'
  })
}

// 删除优惠券
export function deleteCoupon(id) {
  return request({
    url: `/admin/coupon/${id}`,
    method: 'delete',
  })
}

// 启用/禁用优惠券
export function updateCouponStatus(status, id) {
  return request({
    url: `/admin/coupon/status/${status}`,
    method: 'post',
    params: { id }
  })
}
