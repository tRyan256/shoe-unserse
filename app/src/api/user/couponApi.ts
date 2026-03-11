import { get } from '../request';
import type { PageResult } from '@/types';

/**
 * 用户优惠券 VO
 */
export interface UserCouponVO {
  id: number;
  couponId: number;
  couponName: string;
  couponType: number;
  couponValue: number;
  minAmount: number;
  startTime: string;
  endTime: string;
  status: number;
  useTime?: string;
  orderId?: number;
  createTime: string;
}

/**
 * 优惠券列表查询参数
 */
export interface CouponListParams {
  status?: number;
  page?: number;
  size?: number;
}

/**
 * 获取当前用户优惠券列表
 */
export async function listCoupons(params?: CouponListParams): Promise<PageResult<UserCouponVO>> {
  return get<PageResult<UserCouponVO>>('/user/profile/coupons', { params });
}
