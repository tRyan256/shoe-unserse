import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Ticket, Plus } from 'lucide-react';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { CouponCard } from '@/components/shared';
import { couponApi } from '@/api';
import type { Coupon } from '@/types';

export default function MyCouponPage() {
  const navigate = useNavigate();
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [activeTab, setActiveTab] = useState('available');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadCoupons();
  }, []);

  const loadCoupons = async () => {
    try {
      setIsLoading(true);
      const data = await couponApi.getMyCoupons();
      setCoupons(data);
    } catch (error) {
      console.error('Failed to load coupons:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredCoupons = coupons.filter((c) => {
    if (activeTab === 'available') return c.status === 'available';
    if (activeTab === 'used') return c.status === 'used';
    if (activeTab === 'expired') return c.status === 'expired';
    return true;
  });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-6 h-6 border-2 border-emerald-500 border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-3 h-11 bg-white border-b border-gray-100">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5 text-gray-700" />
        </button>
        <h1 className="flex-1 text-center text-sm font-bold text-gray-800">我的优惠券</h1>
        <button onClick={() => navigate('/coupon/claim')}>
          <Plus className="w-5 h-5 text-emerald-600" />
        </button>
      </header>

      <div className="bg-white border-b border-gray-100">
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="w-full justify-start h-10 bg-transparent rounded-none px-2">
            <TabsTrigger
              value="available"
              className="flex-1 text-xs data-[state=active]:bg-emerald-50 data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-lg text-gray-500 transition-colors"
            >
              可用
            </TabsTrigger>
            <TabsTrigger
              value="used"
              className="flex-1 text-xs data-[state=active]:bg-emerald-50 data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-lg text-gray-500 transition-colors"
            >
              已使用
            </TabsTrigger>
            <TabsTrigger
              value="expired"
              className="flex-1 text-xs data-[state=active]:bg-emerald-50 data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-lg text-gray-500 transition-colors"
            >
              已过期
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      <div className="flex-1 p-3">
        {filteredCoupons.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="w-16 h-16 bg-emerald-50 rounded-full flex items-center justify-center mb-4">
              <Ticket className="w-8 h-8 text-emerald-300" />
            </div>
            <h2 className="text-sm font-medium text-gray-700 mb-1.5">
              {activeTab === 'available' && '暂无可用优惠券'}
              {activeTab === 'used' && '暂无已使用优惠券'}
              {activeTab === 'expired' && '暂无已过期优惠券'}
            </h2>
            {activeTab === 'available' && (
              <button
                onClick={() => navigate('/coupon/claim')}
                className="text-xs text-emerald-600 underline"
              >
                去领取优惠券
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-2">
            {filteredCoupons.map((coupon, index) => (
              <motion.div
                key={coupon.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <CouponCard
                  coupon={coupon}
                  showActions={activeTab === 'available'}
                  onUse={() => navigate('/')}
                />
              </motion.div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
