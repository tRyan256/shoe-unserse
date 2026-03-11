import { get, put } from '../request';

/**
 * 用户个人信息 VO
 */
export interface UserPersonalInfoVO {
  id: number;
  name: string;
  phone: string;
  sex: string;
  idNumber: string;
  avatar: string;
  createTime: string;
}

/**
 * 用户个人信息更新 DTO
 */
export interface UserPersonalInfoUpdateDTO {
  name?: string;
  phone?: string;
  sex?: string;
  idNumber?: string;
  avatar?: string;
}

/**
 * 获取当前用户个人信息
 */
export async function getPersonalInfo(): Promise<UserPersonalInfoVO> {
  return get<UserPersonalInfoVO>('/user/profile/personal');
}

/**
 * 更新当前用户个人信息
 */
export async function updatePersonalInfo(data: UserPersonalInfoUpdateDTO): Promise<void> {
  return put<void>('/user/profile/personal', data);
}
