import { useState } from 'react';
import { Heart, MessageCircle, Reply, UserPlus, ShoppingBag, ChevronDown, ChevronUp } from 'lucide-react';
import type { ExperienceNotificationVO } from '@/api/user/notificationApi';
import { formatDate } from '@/utils';

interface NotificationItemProps {
  notification: ExperienceNotificationVO;
  onClick: () => void;
}

const iconMap: Record<number, React.ElementType> = {
  1: Heart,
  2: MessageCircle,
  3: Reply,
  4: Heart,
  5: UserPlus,
  6: ShoppingBag,
};

const typeLabels: Record<number, string> = {
  1: '点赞了你的心得',
  2: '评论了你的心得',
  3: '回复了你的评论',
  4: '点赞了你的评论',
  5: '关注了你',
  6: '订单状态更新',
};

const iconColorMap: Record<number, string> = {
  1: 'text-red-500',
  2: 'text-emerald-600',
  3: 'text-emerald-600',
  4: 'text-red-500',
  5: 'text-emerald-600',
  6: 'text-amber-600',
};

const MAX_CONTENT_LENGTH = 50;

export default function NotificationItem({ notification, onClick }: NotificationItemProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const Icon = iconMap[notification.type] || MessageCircle;
  const isUnread = notification.isRead === 0;
  const iconColor = iconColorMap[notification.type] || 'text-gray-400';

  const content = notification.content || '';
  const shouldTruncate = content.length > MAX_CONTENT_LENGTH;
  const displayContent = shouldTruncate && !isExpanded
    ? content.slice(0, MAX_CONTENT_LENGTH) + '...'
    : content;

  const handleToggleExpand = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsExpanded(!isExpanded);
  };

  return (
    <div
      onClick={onClick}
      className={`bg-white rounded-lg p-3 cursor-pointer active:bg-gray-50 ${
        isUnread ? 'border-l-2 border-emerald-600' : ''
      }`}
    >
      <div className="flex items-start gap-3">
        <div className="flex-shrink-0 relative">
          {notification.sourceUserAvatar ? (
            <img
              src={notification.sourceUserAvatar}
              alt={notification.sourceUserName || '用户'}
              className="w-10 h-10 rounded-full object-cover"
            />
          ) : (
            <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center">
              <Icon className={`w-5 h-5 ${iconColor}`} />
            </div>
          )}
          {isUnread && (
            <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-emerald-600 rounded-full" />
          )}
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium text-gray-800 truncate">
              {notification.sourceUserName || '未知用户'}
            </span>
            <span className="text-xs text-gray-500 shrink-0">
              {typeLabels[notification.type] || '新通知'}
            </span>
          </div>

          {content && (
            <div className="mt-1">
              <p className="text-xs text-gray-600 break-all">
                {displayContent}
              </p>
              {shouldTruncate && (
                <button
                  onClick={handleToggleExpand}
                  className="flex items-center gap-0.5 text-xs text-emerald-600 mt-1 hover:text-emerald-700 transition-colors"
                >
                  {isExpanded ? (
                    <>
                      <span>收起</span>
                      <ChevronUp className="w-3 h-3" />
                    </>
                  ) : (
                    <>
                      <span>展开</span>
                      <ChevronDown className="w-3 h-3" />
                    </>
                  )}
                </button>
              )}
            </div>
          )}

          <p className="text-[10px] text-gray-400 mt-1.5">
            {formatDate(notification.createTime, 'MM-dd HH:mm')}
          </p>
        </div>
      </div>
    </div>
  );
}

