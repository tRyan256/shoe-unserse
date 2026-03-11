import request from '@/utils/request'

export function getOutletList(params) {
  const p = { ...(params || {}) }
  Object.keys(p).forEach((k) => {
    if (p[k] === '' || p[k] === null || p[k] === undefined) {
      delete p[k]
    }
  })
  return request({
    url: '/admin/outlet/page',
    method: 'get',
    params: p
  })
}

export function addOutlet(data) {
  return request({
    url: '/admin/outlet',
    method: 'post',
    data
  })
}

export function updateOutlet(data) {
  return request({
    url: '/admin/outlet',
    method: 'put',
    data
  })
}

export function getOutletById(id) {
  return request({
    url: `/admin/outlet/${id}`,
    method: 'get'
  })
}

export function deleteOutlet(id) {
  return request({
    url: `/admin/outlet/${id}`,
    method: 'delete'
  })
}

export function updateOutletStatus(status, id) {
  return request({
    url: `/admin/outlet/status/${status}`,
    method: 'post',
    params: { id }
  })
}
