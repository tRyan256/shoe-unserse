import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import type { OrderVO } from '@/types';
import { formatPrice, orderStatusMap, formatDate } from '@/utils';

interface OrderCardProps {
  order: OrderVO;
  onCancel?: (orderNumber: string) => void;
  onPay?: (orderNumber: string) => void;
  onConfirm?: (orderNumber: string) => void;
  onReorder?: (orderNumber: string) => void;
  onReminder?: (orderNumber: string) => void;
  onReview?: (orderNumber: string) => void;
}

export function OrderCard({
  order,
  onCancel,
  onPay,
  onConfirm,
  onReorder,
  onReminder,
  onReview,
}: OrderCardProps) {
  const status = orderStatusMap[order.status];
  const orderDetailList = order.orderDetailList || [];
  const firstItem = orderDetailList[0];
  const itemCount = orderDetailList.length;
  const hasReviewableItem = orderDetailList.some((item) => !!item.spuId || !!item.skuId);

  const getStatusStyles = () => {
    switch (order.status) {
      case 1:
        return 'bg-amber-50 text-amber-600';
      case 2:
      case 3:
        return 'bg-amber-50 text-amber-600';
      case 4:
      case 5:
        return 'bg-rose-50 text-rose-600';
      case 6:
      case 8:
        return 'bg-green-50 text-green-600';
      case 7:
        return 'bg-gray-50 text-gray-500';
      default:
        return 'bg-gray-50 text-gray-500';
    }
  };

  return (
    <div className="bg-white rounded-lg overflow-hidden shadow-sm border border-gray-100">
      <div className="flex items-center justify-between px-3 py-2 border-b border-gray-50">
        <span className="text-xs text-gray-400">{order.number}</span>
        <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${getStatusStyles()}`}>
          {status?.text || '未知状态'}
        </span>
      </div>

      <Link to={`/order/detail/${order.number}`}>
        <div className="p-3">
          <div className="flex gap-3">
            <div className="w-16 h-16 flex-shrink-0 bg-gray-50 rounded-lg overflow-hidden">
              {firstItem && (
                <img
                  src={firstItem.image}
                  alt={firstItem.name}
                  className="w-full h-full object-cover"
                />
              )}
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="text-xs font-medium text-gray-800 line-clamp-1">
                {firstItem?.name || '商品'}
              </h3>
              <p className="text-xs text-gray-400 mt-0.5">
                尺码: {firstItem?.shoeSize} x{firstItem?.number}
              </p>
              {itemCount > 1 && (
                <p className="text-xs text-emerald-600 mt-0.5">
                  共{itemCount}件商品
                </p>
              )}
            </div>
            <div className="text-right flex flex-col justify-center">
              <span className="text-xs font-bold text-gray-900">
                {formatPrice(order.amount)}
              </span>
            </div>
          </div>
        </div>
      </Link>

      <div className="flex items-center justify-between px-3 py-2 border-t border-gray-50">
        <span className="text-xs text-gray-400">
          {order.orderTime && formatDate(order.orderTime, 'MM-dd HH:mm')}
        </span>
        <div className="flex gap-2">
          {order.status === 1 && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => onCancel?.(order.number)}
                className="h-6 text-xs px-2"
              >
                取消订单
              </Button>
              <Button size="sm" onClick={() => onPay?.(order.number)} className="h-6 text-xs px-2 bg-emerald-600 hover:bg-emerald-700">
                去支付
              </Button>
            </>
          )}
          {order.status === 2 && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onReminder?.(order.number)}
              className="h-6 text-xs px-2"
            >
              催单
            </Button>
          )}
          {order.status === 3 && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onReminder?.(order.number)}
              className="h-6 text-xs px-2"
            >
              催单
            </Button>
          )}
          {(order.status === 4 || order.status === 5) && (
            <Button size="sm" onClick={() => onConfirm?.(order.number)} className="h-6 text-xs px-2 bg-emerald-600 hover:bg-emerald-700">
              确认收货
            </Button>
          )}
          {order.status === 6 && (
            <>
              {hasReviewableItem && (
                <Button
                  size="sm"
                  onClick={() => onReview?.(order.number)}
                  className="h-6 text-xs px-2 bg-emerald-600 hover:bg-emerald-700"
                >
                  去评价
                </Button>
              )}
              <Button
                variant="outline"
                size="sm"
                onClick={() => onReorder?.(order.number)}
                className="h-6 text-xs px-2"
              >
                再来一单
              </Button>
            </>
          )}
          {(order.status === 7 || order.status === 8) && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => onReorder?.(order.number)}
              className="h-6 text-xs px-2"
            >
              再来一单
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}



