import request from '@/utils/request'

// 员工登录
export function login(data) {
  return request({
    url: '/admin/employee/login',
    method: 'post',
    data
  })
}

// 员工登出
export function logout() {
  return request({
    url: '/admin/employee/logout',
    method: 'post'
  })
}

// 获取员工信息
export function getEmployeeInfo() {
  return request({
    url: '/admin/employee/info',
    method: 'get'
  })
}

// 员工列表分页查询
export function getEmployeeList(params) {
  return request({
    url: '/admin/employee/page',
    method: 'get',
    params
  })
}

// 新增员工
export function addEmployee(data) {
  return request({
    url: '/admin/employee',
    method: 'post',
    data
  })
}

// 修改员工
export function updateEmployee(data) {
  return request({
    url: '/admin/employee',
    method: 'put',
    data
  })
}

// 根据ID查询员工
export function getEmployeeById(id) {
  return request({
    url: `/admin/employee/${id}`,
    method: 'get'
  })
}

// 启用/禁用员工账号
export function updateEmployeeStatus(status, id) {
  return request({
    url: `/admin/employee/status/${status}`,
    method: 'post',
    params: { id }
  })
}
