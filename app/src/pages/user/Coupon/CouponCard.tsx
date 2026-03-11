import { formatDate, formatPrice } from '@/utils';
import type { UserCouponVO } from '@/api/user/couponApi';

interface CouponCardProps {
  coupon: UserCouponVO;
}

const couponTypeMap: Record<number, string> = {
  0: '满减',
  1: '折扣',
};

export default function CouponCard({ coupon }: CouponCardProps) {
  const isExpired = coupon.status === 2;
  const isUsed = coupon.status === 1;
  const isUnused = coupon.status === 0;

  const formatCouponValue = () => {
    if (coupon.couponType === 0) {
      return formatPrice(coupon.couponValue);
    } else if (coupon.couponType === 1) {
      return `${coupon.couponValue}折`;
    }
    return formatPrice(coupon.couponValue);
  };

  const getStatusText = () => {
    if (isUsed) return '已使用';
    if (isExpired) return '已过期';
    return '可使用';
  };

  return (
    <div
      className={`bg-white rounded-lg overflow-hidden shadow-sm ${
        isExpired || isUsed ? 'opacity-50' : ''
      }`}
    >
      <div className="flex">
        <div
          className={`w-24 flex flex-col items-center justify-center py-4 px-2 shrink-0 ${
            isUnused
              ? 'bg-emerald-600 text-white'
              : 'bg-gray-100 text-gray-400'
          }`}
        >
          <div className="text-xl font-bold leading-none">{formatCouponValue()}</div>
          <div className="text-[10px] mt-1 opacity-80">
            {couponTypeMap[coupon.couponType] || '券'}
          </div>
        </div>

        <div className="flex-1 p-3 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-medium text-sm text-gray-800 truncate">
              {coupon.couponName}
            </h3>
            <span
              className={`text-[10px] px-1.5 py-0.5 rounded shrink-0 ${
                isUnused
                  ? 'bg-emerald-50 text-emerald-700'
                  : isUsed
                  ? 'bg-gray-100 text-gray-500'
                  : 'bg-gray-100 text-gray-400'
              }`}
            >
              {getStatusText()}
            </span>
          </div>

          <div className="mt-2 space-y-1">
            <p className="text-xs text-gray-500">
              满{formatPrice(coupon.minAmount)}可用
            </p>
            <p className="text-[10px] text-gray-400">
              {formatDate(coupon.startTime, 'MM.dd')}-{formatDate(coupon.endTime, 'MM.dd')}
            </p>
            {isUsed && coupon.useTime && (
              <p className="text-[10px] text-gray-400">
                使用于 {formatDate(coupon.useTime, 'MM.dd HH:mm')}
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

