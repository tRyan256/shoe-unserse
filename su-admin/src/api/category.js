import request from '@/utils/request'

// 分类列表分页查询
export function getCategoryList(params) {
  return request({
    url: '/admin/category/page',
    method: 'get',
    params
  })
}

// 新增分类
export function addCategory(data) {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

// 修改分类
export function updateCategory(data) {
  return request({
    url: '/admin/category',
    method: 'put',
    data
  })
}

// 根据ID查询分类
export function getCategoryById(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'get'
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/admin/category`,
    method: 'delete',
    params: { id }
  })
}

// 启用/禁用分类
export function updateCategoryStatus(status, id) {
  return request({
    url: `/admin/category/status/${status}`,
    method: 'post',
    params: { id }
  })
}

// 获取分类列表(不分页)
export function getCategoryOptions(type) {
  return request({
    url: '/admin/category/list',
    method: 'get',
    params: { type }
  })
}
