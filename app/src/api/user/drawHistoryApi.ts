import { get } from '../request';
import type { PageResult } from '@/types';

/**
 * 抽签记录 VO
 */
export interface DrawRecordVO {
  id: number;
  drawId: number;
  drawTitle: string;
  targetType: number;
  shoeSize: string;
  status: number;
  orderNo?: string;
  createTime: string;
}

/**
 * 抽签记录列表查询参数
 */
export interface DrawRecordListParams {
  page?: number;
  size?: number;
}

/**
 * 获取当前用户抽签记录列表
 */
export async function listDrawRecords(params?: DrawRecordListParams): Promise<PageResult<DrawRecordVO>> {
  return get<PageResult<DrawRecordVO>>('/user/profile/draws', { params });
}
