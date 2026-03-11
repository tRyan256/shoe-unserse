import { get, post, put, del } from '../request';

/**
 * 地址簿 DTO
 */
export interface AddressBookDTO {
  id?: number;
  consignee: string;
  phone: string;
  sex: string;
  provinceCode: string;
  provinceName: string;
  cityCode: string;
  cityName: string;
  districtCode: string;
  districtName: string;
  detail: string;
  label?: string;
  isDefault?: number;
}

/**
 * 地址簿实体
 */
export interface AddressBook {
  id: number;
  userId: number;
  consignee: string;
  phone: string;
  sex: string;
  provinceCode: string;
  provinceName: string;
  cityCode: string;
  cityName: string;
  districtCode: string;
  districtName: string;
  detail: string;
  label: string;
  isDefault: number;
}

/**
 * 获取当前用户所有地址
 */
export async function listAddresses(): Promise<AddressBook[]> {
  return get<AddressBook[]>('/user/profile/addresses');
}

/**
 * 添加新地址
 */
export async function addAddress(data: AddressBookDTO): Promise<void> {
  return post<void>('/user/profile/addresses', data);
}

/**
 * 更新地址
 */
export async function updateAddress(id: number, data: AddressBookDTO): Promise<void> {
  return put<void>(`/user/profile/addresses/${id}`, data);
}

/**
 * 删除地址
 */
export async function deleteAddress(id: number): Promise<void> {
  return del<void>(`/user/profile/addresses/${id}`);
}

/**
 * 设置默认地址
 */
export async function setDefaultAddress(id: number): Promise<void> {
  return put<void>(`/user/profile/addresses/${id}/default`);
}
