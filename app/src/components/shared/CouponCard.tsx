import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import type { Coupon } from '@/types';

interface CouponCardProps {
  coupon: Coupon;
  showActions?: boolean;
  onClaim?: (id: string) => void;
  onUse?: (id: string) => void;
}

export function CouponCard({
  coupon,
  showActions = true,
  onClaim,
  onUse,
}: CouponCardProps) {
  const isAvailable = coupon.status === 'available';
  const isUsed = coupon.status === 'used';
  const isExpired = coupon.status === 'expired';

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={`relative flex bg-white rounded-lg overflow-hidden shadow-sm ${
        isUsed || isExpired ? 'opacity-50' : ''
      }`}
    >
      <div className="flex flex-col items-center justify-center w-20 p-2 bg-gradient-to-br from-emerald-500 to-emerald-600 text-white">
        <span className="text-lg font-bold">¥{coupon.amount}</span>
        {coupon.minSpend && (
          <span className="text-[10px] text-emerald-100 mt-0.5">
            满{coupon.minSpend}可用
          </span>
        )}
      </div>

      <div className="flex-1 p-2.5 flex flex-col justify-between">
        <div>
          <h3 className="text-xs font-medium text-gray-800">{coupon.name}</h3>
          <p className="text-[10px] text-gray-400 mt-0.5">
            {coupon.startTime} - {coupon.endTime}
          </p>
          {coupon.description && (
            <p className="text-[10px] text-gray-400 mt-0.5">{coupon.description}</p>
          )}
        </div>

        {showActions && (
          <div className="flex justify-end mt-2">
            {isAvailable && onClaim && (
              <Button size="sm" className="text-[10px] h-6 px-2" onClick={() => onClaim(coupon.id)}>
                领取
              </Button>
            )}
            {isAvailable && onUse && (
              <Button size="sm" variant="outline" className="text-[10px] h-6 px-2 border-emerald-500 text-emerald-600 hover:bg-emerald-50" onClick={() => onUse(coupon.id)}>
                去使用
              </Button>
            )}
            {isUsed && (
              <span className="text-[10px] text-gray-400">已使用</span>
            )}
            {isExpired && (
              <span className="text-[10px] text-gray-400">已过期</span>
            )}
          </div>
        )}
      </div>

      <div className="absolute left-20 top-0 bottom-0 w-px border-l-2 border-dashed border-gray-200" />
    </motion.div>
  );
}
