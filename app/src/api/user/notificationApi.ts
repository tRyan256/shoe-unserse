import { get, put } from '../request';
import type { PageResult } from '@/types';

/**
 * 体验心得通知 VO
 */
export interface ExperienceNotificationVO {
  id?: number;
  clientId?: string;
  userId: number;
  type: number;
  sourceUserId: number;
  sourceUserName?: string;
  sourceUserAvatar?: string;
  postId?: number;
  postContent?: string;
  commentId?: number;
  commentContent?: string;
  content?: string;
  isRead: number;
  createTime: string;
}

/**
 * 通知列表查询参数
 */
export interface NotificationListParams {
  type?: number;
  page?: number;
  size?: number;
}

/**
 * 获取当前用户通知列表
 */
export async function listNotifications(params?: NotificationListParams): Promise<PageResult<ExperienceNotificationVO>> {
  return get<PageResult<ExperienceNotificationVO>>('/user/profile/notifications', { params });
}

/**
 * 标记通知为已读
 */
export async function markAsRead(id: number): Promise<void> {
  return put<void>(`/user/profile/notifications/${id}/read`);
}

/**
 * 获取未读通知数量
 */
export async function getUnreadCount(): Promise<number> {
  return get<number>('/user/profile/notifications/unread-count');
}

/**
 * 一键标记所有通知为已读
 */
export async function markAllAsRead(): Promise<void> {
  return put<void>('/user/profile/notifications/read-all');
}
