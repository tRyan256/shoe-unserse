import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Badge } from '@/components/ui/badge';
import { activityApi } from '@/api';
import { useUserStore } from '@/stores';
import type { ActivityBanner } from '@/types';
import { formatDate } from '@/utils';
import { toast } from 'sonner';

export function ActivityCarousel() {
  const [activities, setActivities] = useState<ActivityBanner[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    loadActivities();
  }, []);

  useEffect(() => {
    if (activities.length <= 1) return;

    const timer = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % activities.length);
    }, 5000);

    return () => clearInterval(timer);
  }, [activities.length]);

  const loadActivities = async () => {
    try {
      const data = await activityApi.getBannerActivities();
      const sortedData = [...data].sort((a, b) => 
        new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
      );
      setActivities(sortedData);
    } catch (error) {
      console.error('Failed to load banner activities:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleIndicatorClick = useCallback((index: number) => {
    setCurrentIndex(index);
  }, []);

  if (!loading && activities.length === 0) {
    return null;
  }

  if (loading) {
    return (
      <div className="relative h-48 overflow-hidden bg-gray-100 animate-pulse">
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin" />
        </div>
      </div>
    );
  }

  return (
    <div className="relative h-48 overflow-hidden">
      <AnimatePresence mode="wait">
        {activities.map((activity, index) =>
          index === currentIndex ? (
            <motion.div
              key={`${activity.type}-${activity.id}`}
              initial={{ opacity: 0, scale: 1.05 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              transition={{ duration: 0.5 }}
              className="absolute inset-0"
            >
              <ActivityBannerCard activity={activity} />
            </motion.div>
          ) : null
        )}
      </AnimatePresence>

      {activities.length > 1 && (
        <div className="absolute bottom-4 right-4 flex gap-1.5 z-10">
          {activities.map((_, index) => (
            <button
              key={index}
              onClick={() => handleIndicatorClick(index)}
              className={`w-2 h-2 rounded-full transition-all duration-300 ${
                index === currentIndex
                  ? 'bg-white w-4'
                  : 'bg-white/50 hover:bg-white/70'
              }`}
              aria-label={`切换到第 ${index + 1} 张`}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function ActivityBannerCard({ activity }: { activity: ActivityBanner }) {
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);

  if (activity.bannerStatus === 'default') {
    return <DefaultBannerCard activity={activity} />;
  }

  const linkPath =
    activity.type === 'draw'
      ? `/draw/detail/${activity.id}`
      : `/activity?tab=airdrop`;

  const getLabel = () => {
    if (activity.bannerStatus === 'warmup') return '预热中';
    if (activity.bannerStatus === 'active') return '进行中';
    if (activity.bannerStatus === 'drawn') {
      return activity.type === 'draw' ? '开奖了' : '已结束';
    }
    return '';
  };

  const statusConfig = {
    warmup: {
      bgColor: 'bg-gradient-to-r from-orange-500 to-amber-500',
      badgeClass: 'bg-orange-500',
    },
    active: {
      bgColor: 'bg-gradient-to-r from-green-500 to-teal-500',
      badgeClass: 'bg-green-500',
    },
    drawn: {
      bgColor: 'bg-gradient-to-r from-emerald-500 to-lime-500',
      badgeClass: 'bg-emerald-500',
    },
    default: {
      bgColor: 'bg-gradient-to-br from-sky-400 via-sky-300 to-sky-200',
      badgeClass: '',
    },
  };

  const config = statusConfig[activity.bannerStatus as keyof typeof statusConfig];

  if (!config) {
    return null;
  }

  const label = getLabel();

  const handleClick = (e: React.MouseEvent) => {
    if (!isLoggedIn()) {
      e.preventDefault();
      toast.info('请先登录');
    }
  };

  return (
    <Link to={linkPath} className="block h-full" onClick={handleClick}>
      <motion.div
        className="relative h-48 overflow-hidden"
        whileHover={{ scale: 1.02 }}
        transition={{ duration: 0.3 }}
      >
        {activity.image ? (
          <img
            src={activity.image}
            alt={activity.title}
            className="w-full h-full object-cover"
            loading="lazy"
          />
        ) : (
          <div className={`w-full h-full ${config.bgColor}`} />
        )}

        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />

        <div className="absolute bottom-4 left-4 right-16 text-white">
          {label && (
            <Badge className={`mb-2 ${config.badgeClass} text-white border-0`}>
              {label}
            </Badge>
          )}
          <h3 className="text-lg font-bold line-clamp-1">{activity.title}</h3>
          {activity.description && (
            <p className="text-sm text-white/80 line-clamp-1 mt-0.5">
              {activity.description}
            </p>
          )}
          <p className="text-xs text-white/60 mt-1">
            {formatDate(activity.startTime, 'MM-dd HH:mm')} 开始
          </p>
        </div>
      </motion.div>
    </Link>
  );
}

function DefaultBannerCard({ activity }: { activity: ActivityBanner }) {
  return (
    <div className="block h-full">
      <motion.div
        className="relative h-48 overflow-hidden bg-gradient-to-br from-sky-400 via-sky-300 to-sky-200"
        whileHover={{ scale: 1.02 }}
        transition={{ duration: 0.3 }}
      >
        {/* 背景装饰线条 */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-10 left-0 w-full h-px bg-gradient-to-r from-transparent via-red-500/30 to-transparent transform -rotate-6" />
          <div className="absolute top-32 right-0 w-full h-px bg-gradient-to-r from-transparent via-white/20 to-transparent transform rotate-3" />
          <div className="absolute bottom-20 left-0 w-full h-px bg-gradient-to-r from-transparent via-red-500/40 to-transparent transform -rotate-2" />
        </div>

        {/* Logo */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-white text-center px-8">
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.1 }}
            className="mb-4"
          >
            <img
              src="/logo.png"
              alt="Shoe Universe Logo"
              className="w-24 h-24 object-contain drop-shadow-2xl"
            />
          </motion.div>
          
          <motion.h2
            className="text-3xl font-bold mb-2 tracking-wide"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            {activity.title}
          </motion.h2>
          
          <motion.p
            className="text-base text-white/80 max-w-md"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            {activity.description}
          </motion.p>
          
          {/* 装饰线 */}
          <motion.div
            className="mt-4 w-16 h-1 bg-gradient-to-r from-red-500 to-red-400 rounded-full"
            initial={{ opacity: 0, scaleX: 0 }}
            animate={{ opacity: 1, scaleX: 1 }}
            transition={{ delay: 0.4 }}
          />
        </div>

        {/* 角落装饰 */}
        <div className="absolute top-4 left-4 w-8 h-8 border-l-2 border-t-2 border-red-500/40" />
        <div className="absolute top-4 right-4 w-8 h-8 border-r-2 border-t-2 border-red-500/40" />
        <div className="absolute bottom-4 left-4 w-8 h-8 border-l-2 border-b-2 border-red-500/40" />
        <div className="absolute bottom-4 right-4 w-8 h-8 border-r-2 border-b-2 border-red-500/40" />
      </motion.div>
    </div>
  );
}

