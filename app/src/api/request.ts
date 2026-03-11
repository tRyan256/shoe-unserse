import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios';
import { toast } from 'sonner';

/**
 * 后端统一响应格式
 */
export interface ApiResponse<T = unknown> {
  code: number;
  msg: string;
  data: T;
}

/**
 * 业务错误码常量
 */
export const ErrorCode = {
  SUCCESS: 1,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
} as const;

/**
 * 错误消息映射
 */
const ERROR_MESSAGES: Record<number, string> = {
  [ErrorCode.UNAUTHORIZED]: '登录已过期，请重新登录',
  [ErrorCode.FORBIDDEN]: '没有权限访问',
  [ErrorCode.NOT_FOUND]: '请求的资源不存在',
  [ErrorCode.SERVER_ERROR]: '服务器错误，请稍后重试',
};

/**
 * 创建 axios 实例
 */
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  paramsSerializer: {
    // Use repeated keys for arrays: statusList=3&statusList=4
    indexes: null,
  },
});

/**
 * 请求拦截器
 * 自动添加认证 Token
 */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const storageStr = localStorage.getItem('user-storage');
    if (storageStr) {
      try {
        const storage = JSON.parse(storageStr);
        const token = storage?.state?.token;
        if (token) {
          config.headers.Authorization = token;
        }
      } catch {
        // ignore parse error
      }
    }
    return config;
  },
  (error) => {
    console.error('请求拦截器错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 响应拦截器
 * 统一处理响应数据和错误码
 */
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response;

    // code === 1 表示成功，直接返回 data
    if (data.code === ErrorCode.SUCCESS) {
      return data as unknown as AxiosResponse<ApiResponse>;
    }

    // 业务错误处理
    handleBusinessError(data.code, data.msg);
    return Promise.reject(new Error(data.msg || '请求失败'));
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status } = error.response;
      handleHttpError(status);
    } else if (error.code === 'ECONNABORTED') {
      toast.error('请求超时，请检查网络连接');
    } else {
      toast.error('网络错误，请检查网络连接');
    }
    return Promise.reject(error);
  }
);

/**
 * 处理业务错误
 */
function handleBusinessError(code: number, msg?: string): void {
  switch (code) {
    case ErrorCode.UNAUTHORIZED:
      localStorage.removeItem('user-storage');
      toast.error(msg || ERROR_MESSAGES[ErrorCode.UNAUTHORIZED]);
      setTimeout(() => {
        window.location.href = '/login';
      }, 1500);
      break;
    default:
      break;
  }
}

/**
 * 处理 HTTP 错误
 */
function handleHttpError(status: number): void {
  const message = ERROR_MESSAGES[status] || `请求错误 (${status})`;
  toast.error(message);

  if (status === ErrorCode.UNAUTHORIZED) {
    localStorage.removeItem('user-storage');
    setTimeout(() => {
      window.location.href = '/login';
    }, 1500);
  }
}

/**
 * 通用请求方法封装
 */

// GET 请求
export async function get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.get<unknown, ApiResponse<T>>(url, config);
  return response.data;
}

// POST 请求
export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.post<unknown, ApiResponse<T>>(url, data, config);
  return response.data;
}

// PUT 请求
export async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.put<unknown, ApiResponse<T>>(url, data, config);
  return response.data;
}

// DELETE 请求
export async function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.delete<unknown, ApiResponse<T>>(url, config);
  return response.data;
}

// PATCH 请求
export async function patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const response = await request.patch<unknown, ApiResponse<T>>(url, data, config);
  return response.data;
}

/**
 * 导出 axios 实例和请求方法
 */
export default request;
