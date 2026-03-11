import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { ArrowLeft, MapPin, Truck, Copy, Check, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { orderApi, logisticsApi } from '@/api';
import { toast } from 'sonner';
import type { OrderVO, Logistics, OrderDetail, OrderStatus } from '@/types';
import { orderStatusMap, formatPrice, formatDate } from '@/utils';

const orderStatusDescMap: Record<OrderStatus, string> = {
  1: '请在30分钟内完成支付',
  2: '商家正在处理您的订单',
  3: '商家正在为您备货',
  4: '商品正在配送中',
  5: '商品正在派送中',
  6: '交易已完成，期待您的评价',
  7: '订单已取消',
  8: '订单已评价，感谢反馈',
};

export default function OrderDetailPage() {
  const navigate = useNavigate();
  const { orderNumber } = useParams<{ orderNumber: string }>();
  const [order, setOrder] = useState<OrderVO | null>(null);
  const [logistics, setLogistics] = useState<Logistics | null>(null);
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    if (orderNumber) {
      loadOrderDetail(orderNumber);
    }
  }, [orderNumber]);

  const loadOrderDetail = async (orderNo: string) => {
    setLoading(true);
    try {
      const data = await orderApi.getOrderDetail(orderNo);
      setOrder(data || null);
      if (data && data.status >= 4 && data.number) {
        loadLogistics(data.number);
      }
    } catch (error) {
      console.error('加载订单详情失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadLogistics = async (orderNo: string) => {
    try {
      const data = await logisticsApi.getLogistics(orderNo);
      setLogistics(data);
    } catch (error) {
      console.error('加载物流信息失败:', error);
    }
  };

  const handleCopyOrderNo = () => {
    if (order?.number) {
      navigator.clipboard.writeText(order.number);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleCancel = async () => {
    if (!order || actionLoading) return;
    setActionLoading(true);
    try {
      await orderApi.cancelOrder(order.number);
      loadOrderDetail(order.number);
    } catch (error) {
      console.error('取消订单失败:', error);
    } finally {
      setActionLoading(false);
    }
  };

  const handlePay = () => {
    if (order) {
      navigate(`/order/payment?orderNumber=${order.number}`);
    }
  };

  const handleConfirm = async () => {
    if (!order || actionLoading) return;
    setActionLoading(true);
    try {
      await orderApi.confirmOrder(order.number);
      loadOrderDetail(order.number);
    } catch (error) {
      console.error('确认收货失败:', error);
    } finally {
      setActionLoading(false);
    }
  };

  const handleReview = () => {
    if (!order) return;
    if (order.status !== 6) {
      toast.error('仅已签收订单可评价');
      return;
    }
    navigate(`/order/review/${order.number}`);
  };

  const handleReorder = async () => {
    if (!order || actionLoading) return;
    setActionLoading(true);
    try {
      await orderApi.reorder(order.number);
      navigate('/cart');
    } catch (error) {
      console.error('再来一单失败:', error);
      toast.error('操作失败，请稍后重试');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <Loader2 className="w-6 h-6 animate-spin text-gray-400" />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-white flex flex-col pb-16">
        <header className="flex items-center px-4 h-12 bg-white border-b border-gray-100">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="flex-1 text-center text-sm font-bold">订单详情</h1>
          <div className="w-5" />
        </header>
        <div className="flex-1 flex items-center justify-center">
          <p className="text-xs text-gray-500">订单不存在</p>
        </div>
      </div>
    );
  }

  const status = orderStatusMap[order.status] || { text: '未知状态', color: 'text-gray-500' };
  const statusDesc = orderStatusDescMap[order.status] || '';
  const hasReviewableItem = (order.orderDetailList || []).some((item) => !!item.spuId || !!item.skuId);

  return (
    <div className="min-h-screen bg-white flex flex-col pb-16">
      <header className="flex items-center px-4 h-12 bg-white border-b border-gray-100">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="flex-1 text-center text-sm font-bold">订单详情</h1>
        <div className="w-5" />
      </header>

      <div className="m-3 p-4 bg-gray-900 text-white rounded-lg">
        <h2 className="text-lg font-bold mb-1">{status.text}</h2>
        <p className="text-white/70 text-xs">{statusDesc}</p>
      </div>

      {logistics && (
        <div className="mx-3 mb-3 p-3 bg-gray-50 rounded-lg">
          <Link to={`/logistics/${order.number}`}>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 bg-amber-50 rounded-full flex items-center justify-center">
                  <Truck className="w-4 h-4 text-amber-500" />
                </div>
                <div>
                  <p className="text-xs font-medium">{logistics.status}</p>
                  <p className="text-xs text-gray-500 line-clamp-1">
                    {logistics.traces?.[0]?.content || '暂无物流信息'}
                  </p>
                </div>
              </div>
              <span className="text-xs text-gray-400">&gt;</span>
            </div>
          </Link>
        </div>
      )}

      <div className="mx-3 mb-3 p-3 bg-gray-50 rounded-lg">
        <div className="flex items-start gap-2">
          <MapPin className="w-4 h-4 text-gray-400 mt-0.5" />
          <div>
            <div className="flex items-center gap-2 mb-0.5">
              <span className="text-xs font-medium">{order.consignee}</span>
              <span className="text-xs text-gray-500">{order.phone}</span>
            </div>
            <p className="text-xs text-gray-600">{order.address}</p>
          </div>
        </div>
      </div>

      <div className="mx-3 mb-3 p-3 bg-gray-50 rounded-lg">
        <h3 className="text-xs font-medium mb-3">商品信息</h3>
        <div className="space-y-3">
          {order.orderDetailList?.map((item: OrderDetail, index: number) => (
            <div key={`${item.skuId ?? item.bundleId ?? item.spuId ?? 'item'}-${item.shoeSize}-${index}`} className="flex gap-3">
              <img
                src={item.image}
                alt={item.name}
                className="w-16 h-16 object-cover rounded-lg bg-gray-100"
              />
              <div className="flex-1">
                <h4 className="text-xs font-medium line-clamp-1">{item.name}</h4>
                <p className="text-xs text-gray-500 mt-0.5">尺码: {item.shoeSize}</p>
                <div className="flex items-center justify-between mt-1">
                  <span className="text-xs font-bold">{formatPrice(item.amount)}</span>
                  <span className="text-xs text-gray-500">x{item.number}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="mx-3 mb-3 p-3 bg-gray-50 rounded-lg">
        <h3 className="text-xs font-medium mb-3">订单信息</h3>
        <div className="space-y-1.5 text-xs">
          <div className="flex justify-between">
            <span className="text-gray-500">订单编号</span>
            <div className="flex items-center gap-1">
              <span>{order.number}</span>
              <button onClick={handleCopyOrderNo} className="hover:opacity-70">
                {copied ? (
                  <Check className="w-3 h-3 text-green-500" />
                ) : (
                  <Copy className="w-3 h-3 text-gray-400" />
                )}
              </button>
            </div>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">下单时间</span>
            <span>{order.orderTime ? formatDate(order.orderTime) : '-'}</span>
          </div>
          {order.checkoutTime && (
            <div className="flex justify-between">
              <span className="text-gray-500">支付时间</span>
              <span>{formatDate(order.checkoutTime)}</span>
            </div>
          )}
          {order.remark && (
            <div className="flex justify-between">
              <span className="text-gray-500">订单备注</span>
              <span className="max-w-[160px] text-right">{order.remark}</span>
            </div>
          )}
          {order.cancelReason && (
            <div className="flex justify-between">
              <span className="text-gray-500">取消原因</span>
              <span className="text-red-500">{order.cancelReason}</span>
            </div>
          )}
        </div>
      </div>

      <div className="mx-3 mb-3 p-3 bg-gray-50 rounded-lg">
        <h3 className="text-xs font-medium mb-3">费用明细</h3>
        <div className="space-y-1.5 text-xs">
          <div className="flex justify-between">
            <span className="text-gray-500">商品总额</span>
            <span>{formatPrice(order.amount)}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">运费</span>
            <span>免运费</span>
          </div>
          <div className="flex justify-between pt-1.5 border-t border-gray-200">
            <span className="font-medium">实付金额</span>
            <span className="text-sm font-bold text-black">
              {formatPrice(order.amount)}
            </span>
          </div>
        </div>
      </div>

      <div className="flex-1" />
      <div className="sticky bottom-16 z-40 bg-white border-t border-gray-100 p-3 safe-area-bottom">
        <div className="flex justify-end gap-2">
          {order.status === 1 && (
            <>
              <Button variant="outline" size="sm" onClick={handleCancel} disabled={actionLoading} className="h-7 text-xs px-3">
                取消订单
              </Button>
              <Button size="sm" onClick={handlePay} disabled={actionLoading} className="h-7 text-xs px-3 bg-emerald-600 hover:bg-emerald-700">
                去支付
              </Button>
            </>
          )}
          {(order.status === 4 || order.status === 5) && (
            <Button size="sm" onClick={handleConfirm} disabled={actionLoading} className="h-7 text-xs px-3 bg-emerald-600 hover:bg-emerald-700">
              确认收货
            </Button>
          )}
          {order.status === 6 && (
            <>
              {hasReviewableItem && (
                <Button size="sm" onClick={handleReview} disabled={actionLoading} className="h-7 text-xs px-3 bg-emerald-600 hover:bg-emerald-700">
                  去评价
                </Button>
              )}
              <Button variant="outline" size="sm" onClick={handleReorder} disabled={actionLoading} className="h-7 text-xs px-3">
                再来一单
              </Button>
            </>
          )}
          {(order.status === 7 || order.status === 8) && (
            <Button variant="outline" size="sm" onClick={handleReorder} disabled={actionLoading} className="h-7 text-xs px-3">
              再来一单
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}










