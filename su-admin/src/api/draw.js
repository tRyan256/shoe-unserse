import request from '@/utils/request'

export function getDrawList(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/draw/page',
    method: 'get',
    params: p
  })
}

export function addDraw(data) {
  return request({
    url: '/admin/draw',
    method: 'post',
    data
  })
}

export function deleteDraw(id) {
  return request({
    url: `/admin/draw/${id}`,
    method: 'delete',
  })
}

export function cancelDraw(id) {
  return request({
    url: `/admin/draw/cancel/${id}`,
    method: 'post'
  })
}

export function manualDraw(id) {
  return request({
    url: `/admin/draw/manualDraw/${id}`,
    method: 'post'
  })
}

export function getWinnerList(id) {
  return request({
    url: `/admin/draw/winners/${id}`,
    method: 'get'
  })
}

export function getSpuOptions() {
  return request({
    url: '/admin/shoe/spu/list',
    method: 'get'
  })
}

export function getSkuOptionsBySpuId(spuId) {
  return request({
    url: `/admin/shoe/sku/list/${spuId}`,
    method: 'get'
  })
}

export function getBundleOptions() {
  return request({
    url: '/admin/bundle/list',
    method: 'get',
    params: { includeDisabled: true }
  })
}

export function getSkuOptions(includeDisabled = false) {
  return request({
    url: '/admin/shoe/sku/list',
    method: 'get'
  })
}
