import { Link, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ShoppingBag, Flame, User } from 'lucide-react';
import { useUserStore, useNotificationStore } from '@/stores';
import { toast } from 'sonner';

const navItems = [
  { path: '/community', label: '鞋宙', icon: 'logo' },
  { path: '/shop', label: '购买', icon: 'shopping' },
  { path: '/activity', label: '活动', icon: 'activity' },
  { path: '/user', label: '我的', icon: 'user' },
];

export function MobileNav() {
  const location = useLocation();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const unreadCount = useNotificationStore((state) => state.unreadCount);

  const hiddenPaths = [
    '/login',
    '/cart',
    '/order/confirm',
    '/order/payment',
    '/shoe/',
    '/bundle/',
    '/agreement',
    '/help',
    '/community/publish',
    '/community/edit',
    '/draw/win-confirm',
    '/user/draw-history',
    '/user/airdrop-history',
    '/user/reviews',
  ];
  if (hiddenPaths.some((path) => location.pathname.startsWith(path))) {
    return null;
  }

  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-50 border-t border-amber-100/70 bg-[#fffaf2]/95 backdrop-blur-xl shadow-[0_-10px_28px_rgba(109,83,42,0.12)]"
      style={{ paddingBottom: 'env(safe-area-inset-bottom, 0)' }}
    >
      <div className="mx-auto flex h-14 max-w-[430px] items-center justify-around px-2">
        {navItems.map((item) => {
          const active = isActive(item.path);

          const handleClick = (e: React.MouseEvent) => {
            if (item.path === '/activity' && !isLoggedIn()) {
              e.preventDefault();
              toast.info('请先登录');
            }
          };

          return (
            <Link
              key={item.path}
              to={item.path}
              className="relative flex h-full flex-1 flex-col items-center justify-center"
              onClick={handleClick}
            >
              <motion.div whileTap={{ scale: 0.92 }} className="relative flex flex-col items-center gap-0.5">
                <div className="flex h-6 w-6 items-center justify-center">
                  {item.icon === 'logo' && (
                    <img
                      src="/logo.png"
                      alt="鞋宙"
                      className={`h-full w-full scale-[1.35] object-contain transition-all duration-300 ${
                        active ? 'opacity-100' : 'grayscale opacity-65'
                      }`}
                    />
                  )}
                  {item.icon === 'shopping' && (
                    <ShoppingBag
                      className={`h-[21px] w-[21px] transition-colors duration-300 ${
                        active ? 'text-emerald-700' : 'text-stone-400'
                      }`}
                    />
                  )}
                  {item.icon === 'activity' && (
                    <Flame
                      className={`h-[21px] w-[21px] transition-colors duration-300 ${
                        active ? 'text-orange-600' : 'text-stone-400'
                      }`}
                    />
                  )}
                  {item.icon === 'user' && (
                    <div className="relative flex h-full w-full items-center justify-center">
                      <User
                        className={`h-[21px] w-[21px] transition-colors duration-300 ${
                          active ? 'text-amber-700' : 'text-stone-400'
                        }`}
                      />
                      {isLoggedIn() && unreadCount > 0 && (
                        <span className="absolute -right-1 -top-1 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-red-500 px-1 text-[9px] font-semibold text-white">
                          {unreadCount > 99 ? '99+' : unreadCount}
                        </span>
                      )}
                    </div>
                  )}
                </div>

                <span
                  className={`text-[11px] tracking-[0.01em] transition-colors duration-300 ${
                    active ? 'font-semibold text-stone-900' : 'text-stone-400'
                  }`}
                >
                  {item.label}
                </span>

                {active && (
                  <motion.div
                    layoutId="activeTab"
                    className="absolute -bottom-1 h-1.5 w-6 rounded-full bg-gradient-to-r from-amber-500 to-emerald-600"
                    transition={{ type: 'spring', stiffness: 500, damping: 30 }}
                  />
                )}
              </motion.div>
            </Link>
          );
        })}
      </div>
    </nav>
  );
}
