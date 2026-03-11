import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, CheckCheck, Bell } from 'lucide-react';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { toast } from 'sonner';
import { listNotifications, markAsRead, markAllAsRead } from '@/api/user/notificationApi';
import type { ExperienceNotificationVO } from '@/api/user/notificationApi';
import NotificationItem from './NotificationItem';
import { useNotificationStore } from '@/stores';

const tabs = [
  { value: 'all', label: '全部', type: undefined },
  { value: 'like', label: '点赞', type: 1 },
  { value: 'comment', label: '评论', type: 2 },
  { value: 'reply', label: '回复', type: 3 },
  { value: 'follow', label: '关注', type: 5 },
  { value: 'order', label: '订单', type: 6 },
];

const getNotificationKey = (notification: ExperienceNotificationVO): string | null => {
  if (notification.id != null) {
    return `id:${notification.id}`;
  }
  if (notification.clientId) {
    return `client:${notification.clientId}`;
  }
  return null;
};

const mergeNotifications = (
  base: ExperienceNotificationVO[],
  incoming: ExperienceNotificationVO[]
): ExperienceNotificationVO[] => {
  const merged: ExperienceNotificationVO[] = [];
  const seen = new Set<string>();
  const pushUnique = (item: ExperienceNotificationVO) => {
    const key = getNotificationKey(item);
    if (!key || seen.has(key)) {
      return;
    }
    seen.add(key);
    merged.push(item);
  };
  base.forEach(pushUnique);
  incoming.forEach(pushUnique);
  return merged;
};

export default function MessageCenter() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<ExperienceNotificationVO[]>([]);
  const [activeTab, setActiveTab] = useState('all');
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const unreadCount = useNotificationStore((state) => state.unreadCount);
  const fetchUnreadCount = useNotificationStore((state) => state.fetchUnreadCount);
  const decrementUnreadCount = useNotificationStore((state) => state.decrementUnreadCount);
  const clearUnreadCount = useNotificationStore((state) => state.clearUnreadCount);
  const pendingRealtimeRef = useRef<ExperienceNotificationVO[]>([]);
  const isResetLoadingRef = useRef(false);

  const loadNotifications = async (reset = false, pageNum = 1) => {
    try {
      if (reset) {
        setIsLoading(true);
        isResetLoadingRef.current = true;
      } else {
        setIsLoadingMore(true);
      }

      const currentTab = tabs.find((t) => t.value === activeTab);

      const result = await listNotifications({
        type: currentTab?.type,
        page: pageNum,
        size: 20,
      });

      if (reset) {
        const records = result.records || [];
        setNotifications(() => {
          const pending = pendingRealtimeRef.current.filter((item) => {
            if (currentTab?.type && item.type !== currentTab.type) {
              return false;
            }
            return true;
          });
          pendingRealtimeRef.current = [];
          return mergeNotifications(pending, records);
        });
      } else {
        setNotifications((prev) => mergeNotifications(prev, result.records || []));
      }

      setHasMore((result.records || []).length === 20);
      setPage(pageNum + 1);
    } catch (error) {
      console.error('Failed to load notifications:', error);
      toast.error('加载通知失败');
    } finally {
      setIsLoading(false);
      setIsLoadingMore(false);
      isResetLoadingRef.current = false;
    }
  };

  useEffect(() => {
    loadNotifications(true, 1);
    fetchUnreadCount();
  }, [activeTab]);

  useEffect(() => {
    const handler = (event: Event) => {
      const customEvent = event as CustomEvent<ExperienceNotificationVO>;
      const notification = customEvent.detail;
      if (!notification) {
        return;
      }
      const notificationKey = getNotificationKey(notification);
      if (!notificationKey) {
        return;
      }
      const currentTab = tabs.find((t) => t.value === activeTab);
      if (currentTab?.type && notification.type !== currentTab.type) {
        return;
      }
      if (isResetLoadingRef.current) {
        pendingRealtimeRef.current = mergeNotifications(
          [notification],
          pendingRealtimeRef.current
        );
      }
      setNotifications((prev) => {
        if (prev.some((item) => getNotificationKey(item) === notificationKey)) {
          return prev;
        }
        return [notification, ...prev];
      });
    };

    window.addEventListener('notification:received', handler);
    return () => window.removeEventListener('notification:received', handler);
  }, [activeTab]);

  const handleNotificationClick = async (notification: ExperienceNotificationVO) => {
    const notificationKey = getNotificationKey(notification);
    if (notification.isRead === 0) {
      if (notification.id == null) {
        if (notificationKey) {
          setNotifications((prev) =>
            prev.map((item) =>
              getNotificationKey(item) === notificationKey ? { ...item, isRead: 1 } : item
            )
          );
        }
        decrementUnreadCount();
        fetchUnreadCount();
        loadNotifications(true, 1);
        return;
      }
      try {
        await markAsRead(notification.id);

        setNotifications((prev) =>
          prev.map((n) =>
            n.id === notification.id ? { ...n, isRead: 1 } : n
          )
        );

        decrementUnreadCount();
      } catch (error) {
        console.error('Failed to mark notification as read:', error);
        toast.error('标记已读失败');
      }
    }
  };

  const handleLoadMore = () => {
    if (!isLoadingMore && hasMore) {
      loadNotifications(false, page);
    }
  };

  const handleMarkAllAsRead = async () => {
    if (unreadCount === 0) {
      toast.info('没有未读消息');
      return;
    }

    try {
      await markAllAsRead();

      setNotifications((prev) =>
        prev.map((n) => ({ ...n, isRead: 1 }))
      );

      clearUnreadCount();

      toast.success('已全部标记为已读');
    } catch (error) {
      console.error('Failed to mark all as read:', error);
      toast.error('标记已读失败');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-emerald-600 border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white sticky top-0 z-10">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-base font-bold">消息中心</h1>
        <div className="flex items-center gap-2">
          {unreadCount > 0 && (
            <>
              <button
                onClick={handleMarkAllAsRead}
                className="flex items-center gap-1 text-xs text-emerald-700 hover:text-emerald-800 transition-colors"
              >
                <CheckCheck className="w-4 h-4" />
                <span>全部已读</span>
              </button>
              <span className="bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full min-w-[18px] text-center">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            </>
          )}
        </div>
      </header>

      <div className="bg-white border-b border-gray-100 sticky top-14 z-10">
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="w-full justify-start h-11 bg-transparent rounded-none px-2 gap-1">
            {tabs.map((tab) => (
              <TabsTrigger
                key={tab.value}
                value={tab.value}
                className="flex-1 h-9 data-[state=active]:bg-transparent data-[state=active]:text-emerald-600 data-[state=active]:shadow-none rounded-md text-sm text-gray-500 transition-colors"
              >
                {tab.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      </div>

      <div className="flex-1 p-3 space-y-2">
        {notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <Bell className="w-8 h-8 text-gray-300" />
            </div>
            <p className="text-sm text-gray-500">暂无消息</p>
          </div>
        ) : (
          <>
            {notifications.map((notification, index) => (
              <motion.div
                key={getNotificationKey(notification) ?? `${notification.type}-${notification.createTime}-${index}`}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.03 }}
              >
                <NotificationItem
                  notification={notification}
                  onClick={() => handleNotificationClick(notification)}
                />
              </motion.div>
            ))}

            {hasMore && (
              <div className="text-center py-4">
                <button
                  onClick={handleLoadMore}
                  disabled={isLoadingMore}
                  className="text-sm text-gray-400 hover:text-gray-600 disabled:opacity-50"
                >
                  {isLoadingMore ? '加载中...' : '加载更多'}
                </button>
              </div>
            )}

            {!hasMore && notifications.length > 0 && (
              <div className="text-center py-4">
                <p className="text-xs text-gray-400">没有更多了</p>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}



