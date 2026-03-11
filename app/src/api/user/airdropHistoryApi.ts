import { get } from '../request';
import type { PageResult } from '@/types';

/**
 * 空投记录 VO
 */
export interface AirdropRecordVO {
  id: number;
  airdropId: number;
  airdropTitle: string;
  couponName: string;
  couponValue: number;
  createTime: string;
}

/**
 * 空投记录列表查询参数
 */
export interface AirdropRecordListParams {
  page?: number;
  size?: number;
}

/**
 * 获取当前用户空投记录列表
 */
export async function listAirdropRecords(params?: AirdropRecordListParams): Promise<PageResult<AirdropRecordVO>> {
  return get<PageResult<AirdropRecordVO>>('/user/profile/airdrops', { params });
}
