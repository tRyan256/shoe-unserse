import request from '@/utils/request'

// SPU列表分页查询
export function getSpuList(params) {
  return request({
    url: '/admin/shoe/spu/page',
    method: 'get',
    params
  })
}

// 新增SPU
export function addSpu(data) {
  return request({
    url: '/admin/shoe/spu',
    method: 'post',
    data
  })
}

// 修改SPU
export function updateSpu(data) {
  return request({
    url: '/admin/shoe/spu',
    method: 'put',
    data
  })
}

// 根据ID查询SPU详情
export function getSpuById(id) {
  return request({
    url: `/admin/shoe/spu/${id}`,
    method: 'get'
  })
}

// 删除SPU
export function deleteSpu(ids) {
  const idsParam = Array.isArray(ids) ? ids : [ids]
  const idsStr = idsParam.map(id => `ids=${id}`).join('&')
  return request({
    url: `/admin/shoe/spu?${idsStr}`,
    method: 'delete'
  })
}

// 启用/禁用SPU（上下架）
export function updateSpuStatus(status, id) {
  return request({
    url: `/admin/shoe/spu/status/${status}`,
    method: 'post',
    params: { id }
  })
}

// 获取SPU选项列表(不分页)
export function getSpuOptions() {
  return request({
    url: '/admin/shoe/spu/list',
    method: 'get'
  })
}
