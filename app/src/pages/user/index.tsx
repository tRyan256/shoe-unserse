import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { toast } from 'sonner';
import { useEffect, useState } from 'react';
import {
  ChevronRight,
  MapPin,
  Ticket,
  Gift,
  Sparkles,
  User,
  MessageSquare,
  HelpCircle,
  FileText,
  ShoppingBag,
  CreditCard,
  Truck,
  Package,
  Star,
  LogIn,
} from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { useUserStore, useOrderStore, useNotificationStore } from '@/stores';
import { formatPhone } from '@/utils';
import { userApi, experienceApi, orderApi } from '@/api';

const orderStatuses = [
  { status: 1, label: '待付款', icon: CreditCard },
  { status: 2, label: '待发货', icon: Package },
  { status: 4, label: '待收货', icon: Truck },
  { status: 6, label: '待评价', icon: Star },
];

const menuItems = [
  { icon: User, label: '个人信息', path: '/user/personal-info', needLogin: true },
  { icon: MapPin, label: '我的地址', path: '/user/address', needLogin: true },
  { icon: Star, label: '我的评价', path: '/user/reviews', needLogin: true },
  { icon: Gift, label: '我的抽签', path: '/user/draw-history', needLogin: true },
  { icon: Sparkles, label: '我的空投', path: '/user/airdrop-history', needLogin: true },
  { icon: MessageSquare, label: '消息中心', path: '/user/message-center', needLogin: true },
  { icon: FileText, label: '用户协议', path: '/agreement', needLogin: false },
  { icon: HelpCircle, label: '帮助与反馈', path: '/help', needLogin: false },
];

export default function UserPage() {
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const setUser = useUserStore((state) => state.setUser);
  const logout = useUserStore((state) => state.logout);
  const unreadCount = useNotificationStore((state) => state.unreadCount);
  const fetchUnreadCount = useNotificationStore((state) => state.fetchUnreadCount);

  const loggedIn = isLoggedIn();

  const [orderStatistics, setOrderStatistics] = useState<{
    toBePaid: number;
    toBeShipped: number;
    toBeReceived: number;
    toBeReviewed: number;
  } | null>(null);

  useEffect(() => {
    if (loggedIn && user?.id) {
      const fetchUserInfo = async () => {
        try {
          const userInfo = await userApi.getUserInfo();
          const profile = await experienceApi.getUserProfile(Number(userInfo.id));
          setUser({
            id: String(userInfo.id),
            nickname: userInfo.name || '',
            avatar: userInfo.avatar || '',
            phone: userInfo.phone || '',
            followerCount: profile.followerCount,
            followingCount: profile.followingCount,
            likedCount: profile.likedCount,
          });
        } catch (error) {
          console.error('获取用户信息失败:', error);
        }
      };

      const fetchOrderStatistics = async () => {
        try {
          const stats = await orderApi.getOrderStatistics();
          setOrderStatistics(stats);
        } catch (error) {
          console.error('获取订单统计失败:', error);
        }
      };

      fetchUserInfo();
      fetchUnreadCount();
      fetchOrderStatistics();
    }
  }, [loggedIn]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleOrderClick = (e: React.MouseEvent) => {
    if (!loggedIn) {
      e.preventDefault();
      toast.info('请先登录');
    }
  };

  const handleMenuClick = (e: React.MouseEvent, needLogin: boolean) => {
    if (needLogin && !loggedIn) {
      e.preventDefault();
      toast.info('请先登录');
    }
  };

  const getOrderCount = (status: number): number => {
    if (!orderStatistics) return 0;
    switch (status) {
      case 1:
        return orderStatistics.toBePaid || 0;
      case 2:
        return orderStatistics.toBeShipped || 0;
      case 4:
        return orderStatistics.toBeReceived || 0;
      case 6:
        return orderStatistics.toBeReviewed || 0;
      default:
        return 0;
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 pb-20">
      <div className="h-28 relative overflow-hidden bg-gradient-to-b from-sky-400 via-sky-300 to-sky-200">
        {/* 宇宙星空背景 */}
        <svg
          className="absolute inset-0 w-full h-full"
          viewBox="0 0 400 112"
          preserveAspectRatio="xMidYMid slice"
          xmlns="http://www.w3.org/2000/svg"
        >
          <defs>
            {/* 蓝天渐变 */}
            <linearGradient id="skyGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#0ea5e9" />
              <stop offset="50%" stopColor="#38bdf8" />
              <stop offset="100%" stopColor="#7dd3fc" />
            </linearGradient>
            {/* 太阳光晕 */}
            <radialGradient id="sunGlow" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stopColor="#fef3c7" stopOpacity="0.8" />
              <stop offset="50%" stopColor="#fcd34d" stopOpacity="0.4" />
              <stop offset="100%" stopColor="#f59e0b" stopOpacity="0" />
            </radialGradient>
            {/* 云朵渐变 */}
            <linearGradient id="cloudGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#ffffff" stopOpacity="0.9" />
              <stop offset="100%" stopColor="#ffffff" stopOpacity="0.5" />
            </linearGradient>
            {/* LOGO渐变 */}
            <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#14b8a6" />
              <stop offset="100%" stopColor="#0d9488" />
            </linearGradient>
          </defs>

          {/* 背景 */}
          <rect width="400" height="112" fill="url(#skyGradient)" />

          {/* 太阳/光晕 */}
          <circle cx="320" cy="30" r="25" fill="url(#sunGlow)">
            <animate
              attributeName="r"
              values="25;28;25"
              dur="4s"
              repeatCount="indefinite"
            />
          </circle>
          <circle cx="320" cy="30" r="8" fill="#fef3c7" opacity="0.9" />

          {/* 星星 */}
          <g fill="#ffffff" opacity="0.6">
            <circle cx="50" cy="20" r="1">
              <animate attributeName="opacity" values="0.6;1;0.6" dur="2s" repeatCount="indefinite" />
            </circle>
            <circle cx="120" cy="15" r="0.8">
              <animate attributeName="opacity" values="0.4;0.8;0.4" dur="3s" repeatCount="indefinite" />
            </circle>
            <circle cx="200" cy="25" r="1.2">
              <animate attributeName="opacity" values="0.5;1;0.5" dur="2.5s" repeatCount="indefinite" />
            </circle>
            <circle cx="280" cy="18" r="0.6">
              <animate attributeName="opacity" values="0.3;0.7;0.3" dur="2s" repeatCount="indefinite" />
            </circle>
            <circle cx="360" cy="22" r="0.9">
              <animate attributeName="opacity" values="0.5;0.9;0.5" dur="3.5s" repeatCount="indefinite" />
            </circle>
            <circle cx="80" cy="35" r="0.7">
              <animate attributeName="opacity" values="0.4;0.8;0.4" dur="2.2s" repeatCount="indefinite" />
            </circle>
            <circle cx="160" cy="12" r="0.5">
              <animate attributeName="opacity" values="0.3;0.6;0.3" dur="2.8s" repeatCount="indefinite" />
            </circle>
          </g>

          {/* 云朵 */}
          <g fill="url(#cloudGradient)" opacity="0.7">
            {/* 云朵1 */}
            <ellipse cx="60" cy="80" rx="30" ry="12">
              <animateTransform
                attributeName="transform"
                type="translate"
                values="0,0; 20,0; 0,0"
                dur="8s"
                repeatCount="indefinite"
              />
            </ellipse>
            <ellipse cx="75" cy="75" rx="20" ry="10">
              <animateTransform
                attributeName="transform"
                type="translate"
                values="0,0; 20,0; 0,0"
                dur="8s"
                repeatCount="indefinite"
              />
            </ellipse>
            <ellipse cx="45" cy="78" rx="18" ry="9">
              <animateTransform
                attributeName="transform"
                type="translate"
                values="0,0; 20,0; 0,0"
                dur="8s"
                repeatCount="indefinite"
              />
            </ellipse>

            {/* 云朵2 */}
            <ellipse cx="280" cy="90" rx="25" ry="10">
              <animateTransform
                attributeName="transform"
                type="translate"
                values="0,0; -15,0; 0,0"
                dur="10s"
                repeatCount="indefinite"
              />
            </ellipse>
            <ellipse cx="295" cy="85" rx="18" ry="8">
              <animateTransform
                attributeName="transform"
                type="translate"
                values="0,0; -15,0; 0,0"
                dur="10s"
                repeatCount="indefinite"
              />
            </ellipse>
            <ellipse cx="265" cy="88" rx="15" ry="7">
              <animateTransform
                attributeName="transform"
                type="translate"
                values="0,0; -15,0; 0,0"
                dur="10s"
                repeatCount="indefinite"
              />
            </ellipse>
          </g>

          {/* 飞翔的鞋宙LOGO */}
          <g transform="translate(180, 56)">
            <animateTransform
              attributeName="transform"
              type="translate"
              values="180,56; 190,51; 180,56"
              dur="3s"
              repeatCount="indefinite"
            />
            {/* LOGO外圈光晕 */}
            <circle cx="0" cy="0" r="28" fill="#14b8a6" opacity="0.3">
              <animate
                attributeName="r"
                values="28;32;28"
                dur="2s"
                repeatCount="indefinite"
              />
              <animate
                attributeName="opacity"
                values="0.3;0.5;0.3"
                dur="2s"
                repeatCount="indefinite"
              />
            </circle>
            {/* LOGO背景圆 */}
            <circle cx="0" cy="0" r="22" fill="url(#logoGradient)" opacity="0.95">
              <animateTransform
                attributeName="transform"
                type="rotate"
                values="0;360"
                dur="20s"
                repeatCount="indefinite"
              />
            </circle>
            {/* 鞋子图标 */}
            <g transform="translate(-16, -8) scale(0.35)">
              <path d="M5 35 Q8 28 18 30 L55 22 Q70 18 80 25 L88 32 Q92 38 85 42 L20 50 Q8 52 5 35" fill="#ffffff"/>
              <ellipse cx="25" cy="48" rx="12" ry="4" fill="#cbd5e1" opacity="0.8"/>
              <ellipse cx="70" cy="44" rx="10" ry="3" fill="#cbd5e1" opacity="0.8"/>
              <path d="M55 22 Q60 20 65 22 L68 28 Q60 26 52 28 Z" fill="#14b8a6" opacity="0.6"/>
            </g>
            {/* 尾焰 */}
            <g fill="#fbbf24" opacity="0.6">
              <ellipse cx="-28" cy="0" rx="10" ry="4">
                <animate
                  attributeName="rx"
                  values="10;14;10"
                  dur="0.5s"
                  repeatCount="indefinite"
                />
                <animate
                  attributeName="opacity"
                  values="0.6;0.3;0.6"
                  dur="0.5s"
                  repeatCount="indefinite"
                />
              </ellipse>
              <ellipse cx="-35" cy="0" rx="6" ry="3" fill="#f59e0b">
                <animate
                  attributeName="rx"
                  values="6;9;6"
                  dur="0.5s"
                  repeatCount="indefinite"
                />
              </ellipse>
              <ellipse cx="-40" cy="0" rx="4" ry="2" fill="#f97316">
                <animate
                  attributeName="rx"
                  values="4;6;4"
                  dur="0.5s"
                  repeatCount="indefinite"
                />
              </ellipse>
            </g>
          </g>

          {/* 流星 */}
          <line x1="350" y1="10" x2="320" y2="30" stroke="#ffffff" strokeWidth="1" opacity="0.6">
            <animate
              attributeName="x1"
              values="350;300;350"
              dur="5s"
              repeatCount="indefinite"
            />
            <animate
              attributeName="x2"
              values="320;270;320"
              dur="5s"
              repeatCount="indefinite"
            />
            <animate
              attributeName="opacity"
              values="0;0.6;0"
              dur="5s"
              repeatCount="indefinite"
            />
          </line>
        </svg>
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="relative mx-3 -mt-14 p-3 bg-white rounded-lg shadow"
      >
        <div className="flex items-center gap-3">
          <Avatar className="w-14 h-14 border-2 border-white shadow ring-1 ring-emerald-100">
            <AvatarImage src={user?.avatar} />
            <AvatarFallback className="text-lg bg-emerald-600 text-white">
              {user?.nickname?.[0] || '👤'}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1">
            {loggedIn ? (
              <>
                <h2 className="text-base font-bold text-gray-800">{user?.nickname || '用户'}</h2>
                <p className="text-xs text-gray-500">{formatPhone(user?.phone || '')}</p>
                <div className="flex items-center gap-3 mt-1">
                  <span className="text-xs text-gray-500">
                    <span className="font-medium text-gray-700">{user?.followingCount || 0}</span> 关注
                  </span>
                  <span className="text-xs text-gray-500">
                    <span className="font-medium text-gray-700">{user?.followerCount || 0}</span> 粉丝
                  </span>
                  <span className="text-xs text-gray-500">
                    <span className="font-medium text-gray-700">{user?.likedCount || 0}</span> 获赞
                  </span>
                </div>
              </>
            ) : (
              <Link to="/login" className="block">
                <h2 className="text-base font-bold text-gray-800">登录/注册</h2>
                <p className="text-xs text-gray-500 mt-0.5">点击登录，享受更多权益</p>
              </Link>
            )}
          </div>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="mx-3 mt-3 p-3 bg-white rounded-lg shadow-sm"
      >
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-sm font-bold text-gray-800 flex items-center gap-1.5">
            <ShoppingBag className="w-4 h-4 text-emerald-700" strokeWidth={1.5} />
            我的订单
          </h3>
          <Link
            to="/order/list"
            className="flex items-center text-xs text-gray-500 hover:text-emerald-600 transition-colors"
            onClick={handleOrderClick}
          >
            全部 <ChevronRight className="w-3 h-3" />
          </Link>
        </div>
        <div className="grid grid-cols-4 gap-1">
          {orderStatuses.map((item) => {
            const count = getOrderCount(item.status);
            return (
              <Link
                key={item.status}
                to={`/order/list?status=${item.status}`}
                className="flex flex-col items-center p-1.5 rounded-lg hover:bg-emerald-50 transition-colors"
                onClick={handleOrderClick}
              >
                <div className="relative">
                  <item.icon className="w-5 h-5 text-emerald-700" strokeWidth={1.5} />
                  {loggedIn && count > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 flex items-center justify-center min-w-[14px] h-3.5 px-0.5 text-[9px] font-medium text-white bg-red-500 rounded-full">
                      {count > 99 ? '99+' : count}
                    </span>
                  )}
                </div>
                <span className="text-[10px] text-gray-600 mt-1">{item.label}</span>
              </Link>
            );
          })}
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="mx-3 mt-3 bg-white rounded-lg overflow-hidden shadow-sm"
      >
        {menuItems.map((item, index) => (
          <div key={item.path}>
            <Link
              to={item.path}
              className="flex items-center justify-between p-2.5 hover:bg-gray-50 transition-colors"
              onClick={(e) => handleMenuClick(e, item.needLogin)}
            >
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 flex items-center justify-center">
                  <item.icon className="w-4 h-4 text-emerald-700" strokeWidth={1.5} />
                </div>
                <span className="text-xs text-gray-800">{item.label}</span>
                {item.path === '/user/message-center' && loggedIn && unreadCount > 0 && (
                  <span className="flex items-center justify-center min-w-[16px] h-4 px-1 text-[10px] font-medium text-white bg-red-500 rounded-full">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </div>
              <ChevronRight className="w-4 h-4 text-gray-300" />
            </Link>
            {index < menuItems.length - 1 && <Separator className="ml-11" />}
          </div>
        ))}
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="mx-3 mt-3"
      >
        {loggedIn ? (
          <button
            onClick={handleLogout}
            className="w-full p-2.5 bg-white rounded-lg text-red-500 text-sm font-medium hover:bg-red-50 transition-colors shadow-sm"
          >
            退出登录
          </button>
        ) : (
          <Link to="/login">
            <button className="w-full p-2.5 bg-emerald-600 rounded-lg text-white text-sm font-medium hover:bg-emerald-700 transition-colors shadow-sm flex items-center justify-center gap-1.5">
              <LogIn className="w-4 h-4" />
              登录 / 注册
            </button>
          </Link>
        )}
      </motion.div>

      <p className="text-center text-[10px] text-gray-400 mt-4 mb-2">鞋宙 v1.0.0</p>
    </div>
  );
}


