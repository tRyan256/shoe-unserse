/**
 * API 客户端
 * 基于统一请求层的 axios 实例
 * 保持向后兼容性
 */
import request, { get, post, put, del, patch, type ApiResponse } from './request';

// 导出请求方法
export { get, post, put, del, patch, type ApiResponse };

// 导出默认的 axios 实例
export default request;
