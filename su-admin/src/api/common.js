import request from '@/utils/request'

// 公共上传接口
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/common/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
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
