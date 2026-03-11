import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Bell, ChevronRight, Flame, Clock, Ticket, MapPin, Package, Sparkles, Gift } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ProductCard, ScrollToTop, ActivityCarousel } from '@/components/shared';
import { shoeApi, categoryApi, drawApi } from '@/api';
import { useScrollRestoration } from '@/hooks/use-scroll-restoration';
import { useUserStore } from '@/stores';
import type { ShoeSpu, Category, Draw } from '@/types';
import { formatDate } from '@/utils';
import { APP_SLOGAN } from '@/constants';

// 功能入口
const quickEntries = [
  { icon: Flame, label: '限量抽签', path: '/activity?tab=draw', color: 'text-orange-500 bg-orange-50' },
  { icon: Gift, label: '空投福利', path: '/activity?tab=airdrop', color: 'text-pink-500 bg-pink-50' },
  { icon: Ticket, label: '优惠券', path: '/coupon/my', color: 'text-amber-500 bg-amber-50' },
  { icon: MapPin, label: '附近门店', path: '/outlet/nearby', color: 'text-amber-500 bg-amber-50' },
  { icon: Package, label: '我的订单', path: '/order/list', color: 'text-emerald-500 bg-emerald-50' },
  { icon: Sparkles, label: '活动预热', path: '/activity?tab=preview', color: 'text-rose-500 bg-rose-50' },
];

// 预热活动
const previewActivities = [
  {
    id: '1',
    title: 'Travis Scott x Air Jordan 1',
    subtitle: '即将开启抽签',
    startTime: '2024-01-25 10:00',
    image: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&h=300&fit=crop',
  },
  {
    id: '2',
    title: 'Off-White x Nike Dunk',
    subtitle: '限量发售倒计时',
    startTime: '2024-01-28 12:00',
    image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=300&fit=crop',
  },
];

export default function HomePage() {
  const navigate = useNavigate();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const [shoes, setShoes] = useState<ShoeSpu[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [draws, setDraws] = useState<Draw[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const dataLoaded = !isLoading && shoes.length > 0;
  useScrollRestoration(true, dataLoaded);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [shoesData, categoriesData, drawsData] = await Promise.all([
        shoeApi.getShoeList(),
        categoryApi.getCategoryList(),
        drawApi.getDrawList(),
      ]);
      setShoes(shoesData);
      setCategories(categoriesData);
      setDraws(drawsData.filter(d => d.status === 'ongoing'));
    } catch (error) {
      console.error('Failed to load home data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* 顶部导航 */}
      <motion.header
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-gray-100"
      >
        <div className="flex items-center justify-between px-4 h-14">
          <h1 className="text-xl font-bold">鞋宙</h1>
          <div className="flex items-center gap-3">
            <Link to="/search">
              <Search className="w-5 h-5" />
            </Link>
            <Link to="/message" className="relative">
              <Bell className="w-5 h-5" />
              <span className="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full" />
            </Link>
          </div>
        </div>
      </motion.header>

      {/* 活动轮播图 */}
      <ActivityCarousel />

      {/* 功能入口 */}
      <div className="grid grid-cols-6 gap-2 p-4 bg-white">
        {quickEntries.map((entry, index) => {
          const IconComponent = entry.icon;
          const handleEntryClick = (e: React.MouseEvent) => {
            if ((entry.path === '/coupon/my' || entry.path === '/outlet/nearby') && !isLoggedIn()) {
              e.preventDefault();
              toast.error('请先登录');
            }
          };
          return (
            <motion.div
              key={entry.path}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
            >
              <Link to={entry.path} className="flex flex-col items-center" onClick={handleEntryClick}>
                <div className={`w-6 h-6 rounded-md flex items-center justify-center ${entry.color}`}>
                  <IconComponent className="w-5 h-5" />
                </div>
                <span className="text-[10px] text-gray-600 -mt-2">{entry.label}</span>
              </Link>
            </motion.div>
          );
        })}
      </div>

      {/* 活动预热 */}
      <div className="mt-2 px-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-bold flex items-center gap-2">
            <Flame className="w-5 h-5 text-orange-500" />
            活动预热
          </h2>
          <Link to="/activity?tab=preview" className="flex items-center text-sm text-gray-500">
            全部 <ChevronRight className="w-4 h-4" />
          </Link>
        </div>
        <div className="flex gap-3 overflow-x-auto pb-2 -mx-4 px-4 scrollbar-hide">
          {previewActivities.map((activity, index) => (
            <motion.div
              key={activity.id}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.1 }}
              className="flex-shrink-0 w-48"
            >
              <Link to="/activity?tab=preview">
                <div className="relative h-28 rounded-xl overflow-hidden">
                  <img
                    src={activity.image}
                    alt={activity.title}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
                  <div className="absolute bottom-2 left-2 right-2">
                    <Badge className="bg-orange-500 text-[10px] mb-1">预热中</Badge>
                    <p className="text-white text-xs font-medium line-clamp-1">{activity.title}</p>
                  </div>
                </div>
                <div className="flex items-center gap-1 mt-1 text-xs text-gray-500">
                  <Clock className="w-3 h-3" />
                  <span>{formatDate(activity.startTime, 'MM-dd')} 开始</span>
                </div>
              </Link>
            </motion.div>
          ))}
        </div>
      </div>

      {/* 热门抽签 */}
      {draws.length > 0 && (
        <div className="mt-4 px-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-bold">🔥 热门抽签</h2>
            <Link to="/activity?tab=draw" className="flex items-center text-sm text-gray-500">
              全部 <ChevronRight className="w-4 h-4" />
            </Link>
          </div>
          <Link to={`/draw/detail/${draws[0].id}`}>
            <motion.div
              whileHover={{ scale: 1.02 }}
              className="relative h-40 rounded-2xl overflow-hidden"
            >
              <img
                src={draws[0].image}
                alt={draws[0].name}
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-r from-black/70 to-transparent" />
              <div className="absolute inset-0 flex flex-col justify-center p-6 text-white">
                <Badge className="w-fit mb-2 bg-green-500">进行中</Badge>
                <h3 className="text-xl font-bold mb-1">{draws[0].name}</h3>
                <p className="text-sm text-white/80 mb-3">限量抽签火热进行中</p>
                <Button size="sm" className="w-fit bg-white text-black hover:bg-white/90">
                  立即参与
                </Button>
              </div>
            </motion.div>
          </Link>
        </div>
      )}

      {/* 分类 */}
      <div className="mt-4 px-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-bold">热门分类</h2>
          <Link to="/category" className="flex items-center text-sm text-gray-500">
            全部 <ChevronRight className="w-4 h-4" />
          </Link>
        </div>
        <div className="flex gap-3 overflow-x-auto pb-2 -mx-4 px-4 scrollbar-hide">
          {categories.map((category, index) => (
            <motion.div
              key={category.id}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: index * 0.1 }}
            >
              <Link
                to={`/category`}
                className="flex flex-col items-center min-w-[72px]"
              >
                <div className="w-16 h-16 rounded-xl overflow-hidden bg-gray-100 mb-2">
                  <img
                    src={category.image}
                    alt={category.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                <span className="text-xs text-gray-600">{category.name}</span>
              </Link>
            </motion.div>
          ))}
        </div>
      </div>

      {/* 新品推荐 */}
      <div className="mt-4 px-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-lg font-bold">新品推荐</h2>
          <Link to="/category" className="flex items-center text-sm text-gray-500">
            更多 <ChevronRight className="w-4 h-4" />
          </Link>
        </div>
        <div className="grid grid-cols-2 gap-3">
          {shoes.slice(0, 4).map((shoe, index) => (
            <ProductCard key={shoe.id} shoe={shoe} index={index} />
          ))}
        </div>
      </div>

      {/* 热销商品 */}
      {shoes.length > 4 && (
        <div className="mt-4 px-4">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-lg font-bold">🔥 热销商品</h2>
            <Link to="/category" className="flex items-center text-sm text-gray-500">
              更多 <ChevronRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {shoes.slice(4, 8).map((shoe, index) => (
              <ProductCard key={shoe.id} shoe={shoe} index={index} />
            ))}
          </div>
        </div>
      )}

      {/* 品牌故事 */}
      <div className="mt-6 px-4 pb-4">
        <div className="bg-black rounded-2xl p-6 text-white text-center">
          <h2 className="text-2xl font-bold mb-2">{APP_SLOGAN}</h2>
          <p className="text-sm text-white/70 mb-4">
            加入鞋宙，率先获取独家球鞋发布、限量联名款
          </p>
          <Link to="/activity?tab=draw">
            <Button variant="outline" className="border-white text-white hover:bg-white hover:text-black">
              探索更多
            </Button>
          </Link>
        </div>
      </div>

      {/* 回到顶部 */}
      <ScrollToTop />
    </div>
  );
}


