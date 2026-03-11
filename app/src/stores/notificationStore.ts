import { create } from 'zustand';
import { getUnreadCount } from '@/api/user/notificationApi';

interface NotificationStore {
  unreadCount: number;
  setUnreadCount: (count: number) => void;
  fetchUnreadCount: () => Promise<void>;
  incrementUnreadCount: (delta?: number) => void;
  decrementUnreadCount: () => void;
  clearUnreadCount: () => void;
}

export const useNotificationStore = create<NotificationStore>((set) => ({
  unreadCount: 0,
  setUnreadCount: (count) => set({ unreadCount: count }),
  fetchUnreadCount: async () => {
    try {
      const count = await getUnreadCount();
      set({ unreadCount: count });
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
    }
  },
  incrementUnreadCount: (delta = 1) =>
    set((state) => ({ unreadCount: Math.max(0, state.unreadCount + delta) })),
  decrementUnreadCount: () => set((state) => ({ unreadCount: Math.max(0, state.unreadCount - 1) })),
  clearUnreadCount: () => set({ unreadCount: 0 }),
}));
