import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, MapPin, ChevronRight, Ticket, Loader2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { useCartStore, useUserStore } from '@/stores';
import { addressApi, orderApi, couponApi } from '@/api';
import { formatPrice } from '@/utils';
import type { Address, Coupon, OrderAsyncSubmitResult, OrderAsyncStatus } from '@/types';

// 骨架屏组件
const Skeleton = ({ className }: { className?: string }) => (
  <div className={`animate-pulse bg-gray-200 rounded ${className}`} />
);

// 地址骨架屏
const AddressSkeleton = () => (
  <div className="bg-white m-4 p-4 rounded-xl">
    <div className="flex items-start gap-3">
      <Skeleton className="w-5 h-5 rounded-full" />
      <div className="flex-1 space-y-2">
        <div className="flex gap-2">
          <Skeleton className="w-16 h-4" />
          <Skeleton className="w-24 h-4" />
        </div>
        <Skeleton className="w-full h-4" />
        <Skeleton className="w-3/4 h-4" />
      </div>
    </div>
  </div>
);

// 商品列表骨架屏
const GoodsSkeleton = () => (
  <div className="bg-white m-4 p-4 rounded-xl">
    <Skeleton className="w-20 h-5 mb-4" />
    <div className="space-y-4">
      {[1, 2].map((i) => (
        <div key={i} className="flex gap-4">
          <Skeleton className="w-20 h-20 rounded-lg" />
          <div className="flex-1 space-y-2">
            <Skeleton className="w-3/4 h-4" />
            <Skeleton className="w-1/4 h-3" />
            <div className="flex justify-between">
              <Skeleton className="w-16 h-4" />
              <Skeleton className="w-8 h-4" />
            </div>
          </div>
        </div>
      ))}
    </div>
  </div>
);

export default function OrderConfirmPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const cartItems = useCartStore((state) => state.items);
  const selectedIds = useCartStore((state) => state.selectedIds);
  const fetchCart = useCartStore((state) => state.fetchCart);
  const selectedItems = useMemo(
    () => cartItems.filter((item) => selectedIds.includes(item.id)),
    [cartItems, selectedIds]
  );

  // 状态
  const [isLoading, setIsLoading] = useState(true);
  const [address, setAddress] = useState<Address | null>(null);
  const [remark, setRemark] = useState('');
  const [selectedCoupon, setSelectedCoupon] = useState<Coupon | null>(null);
  const [availableCoupons, setAvailableCoupons] = useState<Coupon[]>([]);
  const [showCouponSheet, setShowCouponSheet] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // 从URL获取选中的地址ID
  const selectedAddressId = searchParams.get('addressId');

  // 计算费用
  const goodsAmount = selectedItems.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0
  );
  const discountAmount = selectedCoupon?.amount || 0;
  const freightAmount = goodsAmount >= 299 ? 0 : 15;
  const payAmount = goodsAmount - discountAmount + freightAmount;

  // 加载地址
  const loadAddress = useCallback(async () => {
    try {
      if (selectedAddressId) {
        // 如果有选中的地址ID，获取该地址详情
        const data = await addressApi.getAddressDetail(Number(selectedAddressId));
        setAddress(data);
      } else {
        // 否则获取默认地址
        const data = await addressApi.getDefaultAddress();
        setAddress(data || null);
      }
    } catch (error) {
      console.error('Failed to load address:', error);
      setAddress(null);
    }
  }, [selectedAddressId]);

  // 加载可用优惠券
  const loadAvailableCoupons = useCallback(async () => {
    try {
      const data = await couponApi.getAvailableCoupons(goodsAmount);
      setAvailableCoupons(data || []);
    } catch (error) {
      console.error('Failed to load coupons:', error);
      setAvailableCoupons([]);
    }
  }, [goodsAmount]);

  // 初始化加载
  useEffect(() => {
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }

    const initData = async () => {
      setIsLoading(true);
      if (selectedItems.length === 0) {
        await fetchCart();
      }
      if (useCartStore.getState().getSelectedItems().length === 0) {
        setIsLoading(false);
        navigate('/cart');
        return;
      }
      await Promise.all([loadAddress(), loadAvailableCoupons()]);
      setIsLoading(false);
    };

    initData();
  }, [isLoggedIn, selectedItems.length, fetchCart, loadAddress, loadAvailableCoupons, navigate]);

  // 轮询订单状态
  const pollOrderStatus = async (orderNumber: string): Promise<OrderAsyncStatus> => {
    const maxAttempts = 30; // 最多轮询30次
    const interval = 1000; // 每秒轮询一次

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
      try {
        const status = await orderApi.getSubmitStatus(orderNumber);
        if (status.status === 'SUCCESS' || status.status === 'FAILED') {
          return status;
        }
      } catch (error) {
        console.error('Failed to poll order status:', error);
      }
      await new Promise((resolve) => setTimeout(resolve, interval));
    }

    return {
      orderNumber,
      status: 'TIMEOUT',
      error: '订单处理超时，请稍后在订单列表查看',
    };
  };

  // 提交订单
  const handleSubmit = async () => {
    if (!address) {
      navigate('/address/list?mode=select&returnUrl=/order/confirm');
      return;
    }

    setIsSubmitting(true);
    setSubmitError(null);

    try {
      // 构建商品列表
      const orderItems = selectedItems.map(item => ({
        spuId: item.spuId,
        skuId: item.skuId,
        bundleId: item.bundleId ? Number(item.bundleId) : undefined,
        shoeSize: item.size,
        quantity: item.quantity,
      }));

      // 提交订单
      const submitResult: OrderAsyncSubmitResult = await orderApi.submitOrder({
        addressBookId: address.id,
        payMethod: 1,
        remark,
        couponId: selectedCoupon?.id ? Number(selectedCoupon.id) : undefined,
        amount: payAmount,
        items: orderItems,  // 新增：商品列表
      });

      // 轮询订单状态
      const statusResult = await pollOrderStatus(submitResult.orderNumber);

      if (statusResult.status === 'SUCCESS') {
        // 跳转支付页面，传递订单号
        navigate(`/order/payment?orderNumber=${statusResult.orderNumber}`);
      } else {
        // 提交失败
        setSubmitError(statusResult.error || '订单提交失败，请重试');
      }
    } catch (error: any) {
      console.error('Failed to submit order:', error);
      setSubmitError(error?.message || '订单提交失败，请重试');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 加载中状态
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        {/* 头部 */}
        <header className="flex items-center px-4 h-14 bg-white">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="flex-1 text-center text-lg font-bold">确认订单</h1>
          <div className="w-6" />
        </header>

        {/* 骨架屏内容 */}
        <div className="flex-1 overflow-auto">
          <AddressSkeleton />
          <GoodsSkeleton />
          <div className="bg-white m-4 p-4 rounded-xl">
            <div className="flex items-center justify-between">
              <Skeleton className="w-16 h-5" />
              <Skeleton className="w-20 h-5" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (selectedItems.length === 0) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 头部 */}
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">确认订单</h1>
        <div className="w-6" />
      </header>

      {/* 内容 */}
      <div className="flex-1 overflow-auto">
        {/* 收货地址 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white m-4 p-4 rounded-xl"
          onClick={() => navigate('/address/list?mode=select')}
        >
          {address ? (
            <div className="flex items-start gap-3">
              <MapPin className="w-5 h-5 text-gray-400 mt-0.5" />
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-medium">{address.consignee}</span>
                  <span className="text-sm text-gray-500">{address.phone}</span>
                </div>
                <p className="text-sm text-gray-600">
                  {address.province} {address.city} {address.district} {address.detail}
                </p>
              </div>
              <ChevronRight className="w-5 h-5 text-gray-400" />
            </div>
          ) : (
            <div className="flex items-center justify-center gap-2 text-gray-500">
              <MapPin className="w-5 h-5" />
              <span>请选择收货地址</span>
              <ChevronRight className="w-5 h-5" />
            </div>
          )}
        </motion.div>

        {/* 商品列表 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white m-4 p-4 rounded-xl"
        >
          <h3 className="font-medium mb-4">商品信息</h3>
          <div className="space-y-4">
            {selectedItems.map((item) => (
              <div key={item.id} className="flex gap-4">
                <img
                  src={item.shoeImage}
                  alt={item.shoeName}
                  className="w-20 h-20 object-cover rounded-lg bg-gray-50"
                />
                <div className="flex-1">
                  <h4 className="font-medium line-clamp-1">{item.shoeName}</h4>
                  <p className="text-sm text-gray-500 mt-1">尺码: {item.size}</p>
                  <div className="flex items-center justify-between mt-2">
                    <span className="font-bold">{formatPrice(item.price)}</span>
                    <span className="text-sm text-gray-500">x{item.quantity}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </motion.div>

        {/* 优惠券 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="bg-white m-4 p-4 rounded-xl"
          onClick={() => setShowCouponSheet(true)}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Ticket className="w-5 h-5 text-gray-400" />
              <span>优惠券</span>
            </div>
            <div className="flex items-center gap-2">
              {selectedCoupon ? (
                <span className="text-red-500">- {formatPrice(selectedCoupon.amount)}</span>
              ) : availableCoupons.length > 0 ? (
                <span className="text-gray-500">{availableCoupons.length}张可用</span>
              ) : (
                <span className="text-gray-400">暂无可用</span>
              )}
              <ChevronRight className="w-5 h-5 text-gray-400" />
            </div>
          </div>
        </motion.div>

        {/* 备注 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="bg-white m-4 p-4 rounded-xl"
        >
          <textarea
            placeholder="订单备注（选填）"
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
            className="w-full resize-none outline-none text-sm"
            rows={2}
          />
        </motion.div>

        {/* 费用明细 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="bg-white m-4 p-4 rounded-xl"
        >
          <h3 className="font-medium mb-4">费用明细</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-500">商品总额</span>
              <span>{formatPrice(goodsAmount)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">运费</span>
              <span>{freightAmount === 0 ? '免运费' : formatPrice(freightAmount)}</span>
            </div>
            {discountAmount > 0 && (
              <div className="flex justify-between">
                <span className="text-gray-500">优惠</span>
                <span className="text-red-500">- {formatPrice(discountAmount)}</span>
              </div>
            )}
            <div className="flex justify-between pt-2 border-t border-gray-100">
              <span className="font-medium">应付总额</span>
              <span className="text-lg font-bold text-black">{formatPrice(payAmount)}</span>
            </div>
          </div>
        </motion.div>
      </div>

      {/* 错误提示 */}
      {submitError && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="fixed top-20 left-4 right-4 bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3 z-50"
        >
          <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
          <p className="text-sm text-red-600 flex-1">{submitError}</p>
          <button
            onClick={() => setSubmitError(null)}
            className="text-red-400 hover:text-red-600"
          >
            <span className="sr-only">关闭</span>
            ×
          </button>
        </motion.div>
      )}

      {/* 底部结算栏 */}
      <div className="bg-white border-t border-gray-100 p-4 safe-area-bottom">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-sm text-gray-500">应付: </span>
            <span className="text-xl font-bold text-black">{formatPrice(payAmount)}</span>
          </div>
          <Button
            onClick={handleSubmit}
            disabled={isSubmitting || !address}
            className="px-8 min-w-[120px]"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                提交中
              </>
            ) : !address ? (
              '请选择地址'
            ) : (
              '提交订单'
            )}
          </Button>
        </div>
        {!address && !isSubmitting && (
          <p className="text-xs text-gray-400 mt-2 text-center">请先选择收货地址</p>
        )}
      </div>

      {/* 优惠券选择 */}
      <Sheet open={showCouponSheet} onOpenChange={setShowCouponSheet}>
        <SheetContent side="bottom" className="rounded-t-2xl h-[70vh]">
          <SheetHeader>
            <SheetTitle>选择优惠券</SheetTitle>
            <SheetDescription className="sr-only">选择可用于本订单的优惠券</SheetDescription>
          </SheetHeader>
          <div className="mt-4 space-y-3 overflow-auto">
            <button
              onClick={() => {
                setSelectedCoupon(null);
                setShowCouponSheet(false);
              }}
              className={`w-full p-4 rounded-xl border-2 transition-colors ${
                !selectedCoupon ? 'border-black' : 'border-gray-100'
              }`}
            >
              <span>不使用优惠券</span>
            </button>
            {availableCoupons.map((coupon) => (
              <button
                key={coupon.id}
                onClick={() => {
                  setSelectedCoupon(coupon);
                  setShowCouponSheet(false);
                }}
                className={`w-full p-4 rounded-xl border-2 transition-colors text-left ${
                  selectedCoupon?.id === coupon.id ? 'border-black' : 'border-gray-100'
                }`}
              >
                <div className="flex justify-between items-start">
                  <div>
                    <p className="font-medium">{coupon.name}</p>
                    <p className="text-sm text-gray-500">{coupon.description}</p>
                    <p className="text-xs text-gray-400 mt-1">
                      有效期至 {coupon.endTime}
                    </p>
                  </div>
                  <span className="text-xl font-bold text-red-500">
                    ¥{coupon.amount}
                  </span>
                </div>
              </button>
            ))}
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}
