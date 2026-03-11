import request from '@/utils/request'

export function getUserById(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'get'
  })
}

