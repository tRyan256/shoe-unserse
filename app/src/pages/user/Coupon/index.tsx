import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Ticket } from 'lucide-react';
import { toast } from 'sonner';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Spinner } from '@/components/ui/spinner';
import { listCoupons, type UserCouponVO } from '@/api/user/couponApi';
import CouponCard from './CouponCard';

const tabs = [
  { value: 'all', label: '全部' },
  { value: '0', label: '未使用' },
  { value: '1', label: '已使用' },
  { value: '2', label: '已过期' },
];

export default function CouponPage() {
  const navigate = useNavigate();
  const [coupons, setCoupons] = useState<UserCouponVO[]>([]);
  const [activeTab, setActiveTab] = useState('all');
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    loadCoupons();
  }, [activeTab]);

  const loadCoupons = async (pageNum = 1) => {
    try {
      setIsLoading(true);
      const params = {
        status: activeTab === 'all' ? undefined : Number(activeTab),
        page: pageNum,
        size: 20,
      };
      const result = await listCoupons(params);
      
      if (pageNum === 1) {
        setCoupons(result.records);
      } else {
        setCoupons((prev) => [...prev, ...result.records]);
      }
      
      setPage(pageNum);
      setHasMore(result.records.length === 20);
    } catch (error) {
      console.error('Failed to load coupons:', error);
      toast.error('加载优惠券失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleTabChange = (value: string) => {
    setActiveTab(value);
    setPage(1);
    setHasMore(true);
  };

  const handleLoadMore = () => {
    if (!isLoading && hasMore) {
      loadCoupons(page + 1);
    }
  };

  const groupedCoupons = () => {
    if (activeTab !== 'all') {
      return coupons;
    }
    
    const unused = coupons.filter((c) => c.status === 0);
    const used = coupons.filter((c) => c.status === 1);
    const expired = coupons.filter((c) => c.status === 2);
    
    return [...unused, ...used, ...expired];
  };

  const displayCoupons = groupedCoupons();

  const unusedCount = coupons.filter((c) => c.status === 0).length;

  if (isLoading && page === 1) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner className="w-8 h-8" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white sticky top-0 z-10">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">我的优惠券</h1>
        <div className="w-6" />
      </header>

      <div className="bg-white border-b border-gray-100 sticky top-14 z-10">
        <Tabs value={activeTab} onValueChange={handleTabChange}>
          <TabsList className="w-full justify-start h-11 bg-transparent rounded-none px-2 gap-1">
            {tabs.map((tab) => (
              <TabsTrigger
                key={tab.value}
                value={tab.value}
                className="flex-1 h-9 data-[state=active]:bg-emerald-600 data-[state=active]:text-white data-[state=active]:shadow-none rounded-md text-sm transition-colors"
              >
                {tab.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      </div>

      {unusedCount > 0 && activeTab === 'all' && (
        <div className="px-4 py-2 bg-emerald-50 flex items-center gap-2">
          <Ticket className="w-4 h-4 text-emerald-700" />
          <span className="text-xs text-emerald-700">
            您有 {unusedCount} 张优惠券可使用
          </span>
        </div>
      )}

      <div className="flex-1 p-3 space-y-2">
        {displayCoupons.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
              <Ticket className="w-8 h-8 text-gray-300" />
            </div>
            <p className="text-sm text-gray-500">暂无优惠券</p>
          </div>
        ) : (
          <>
            {displayCoupons.map((coupon, index) => (
              <motion.div
                key={coupon.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.03 }}
              >
                <CouponCard coupon={coupon} />
              </motion.div>
            ))}
            
            {hasMore && (
              <div className="text-center py-4">
                <button
                  onClick={handleLoadMore}
                  disabled={isLoading}
                  className="text-sm text-gray-400 hover:text-gray-600 disabled:opacity-50"
                >
                  {isLoading ? '加载中...' : '加载更多'}
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}


