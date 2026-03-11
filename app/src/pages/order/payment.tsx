import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Check, X, Clock, Shield, AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { orderApi } from '@/api';
import { formatPrice } from '@/utils';
import type { OrderVO } from '@/types';

const payMethods = [
  { id: 1, name: '微信支付', icon: '💚', desc: '推荐使用' },
  { id: 2, name: '支付宝', icon: '💙', desc: '' },
];

type PaymentStatus = 'idle' | 'loading' | 'paying' | 'success' | 'failed' | 'cancelled' | 'timeout';

interface PaymentResultState {
  status: PaymentStatus;
  message?: string;
}

export default function OrderPaymentPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const orderNumber = searchParams.get('orderNumber');

  const [order, setOrder] = useState<OrderVO | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedMethod, setSelectedMethod] = useState(1);
  const [paymentResult, setPaymentResult] = useState<PaymentResultState>({ status: 'idle' });

  // 获取订单详情
  useEffect(() => {
    const fetchOrderDetail = async () => {
      if (!orderNumber) {
        navigate('/order/list');
        return;
      }

      try {
        setIsLoading(true);
        const orderDetail = await orderApi.getOrderDetail(orderNumber);
        setOrder(orderDetail);
      } catch (error) {
        console.error('获取订单详情失败:', error);
        navigate('/order/list');
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrderDetail();
  }, [orderNumber, navigate]);

  // 发起支付
  const handlePay = async () => {
    if (!order) return;

    setPaymentResult({ status: 'paying' });

    try {
      const res = await orderApi.payOrder({
        orderNumber: order.number,
        payMethod: selectedMethod,
      });

      // 模拟支付，请求成功即视为支付成功
      setPaymentResult({ status: 'success' });

      // 2秒后跳转订单列表
      setTimeout(() => {
        navigate('/order/list');
      }, 2000);
    } catch (error: any) {
      console.error('支付失败:', error);

      // 根据错误类型判断失败原因
      if (error?.message?.includes('timeout') || error?.code === 'TIMEOUT') {
        setPaymentResult({ status: 'timeout', message: '支付超时，请重试' });
      } else if (error?.message?.includes('cancel') || error?.code === 'CANCELLED') {
        setPaymentResult({ status: 'cancelled', message: '支付已取消' });
      } else {
        setPaymentResult({
          status: 'failed',
          message: error?.message || '网络请求失败，请检查网络后重试'
        });
      }
    }
  };

  // 重新支付
  const handleRetry = () => {
    setPaymentResult({ status: 'idle' });
  };

  // 取消支付
  const handleCancel = () => {
    navigate('/order/list');
  };

  // 加载中状态
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
        <div className="w-12 h-12 border-4 border-gray-200 border-t-black rounded-full animate-spin mb-4" />
        <p className="text-gray-500">加载订单信息...</p>
      </div>
    );
  }

  // 订单不存在
  if (!order) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-6">
        <AlertCircle className="w-16 h-16 text-gray-300 mb-4" />
        <p className="text-gray-500 mb-4">订单不存在</p>
        <Button onClick={() => navigate('/order/list')}>返回订单列表</Button>
      </div>
    );
  }

  // 支付成功页面
  if (paymentResult.status === 'success') {
    return (
      <div className="min-h-screen bg-white flex flex-col items-center justify-center p-6">
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ type: 'spring', stiffness: 200 }}
          className="w-24 h-24 bg-green-500 rounded-full flex items-center justify-center mb-6"
        >
          <Check className="w-12 h-12 text-white" />
        </motion.div>
        <h2 className="text-2xl font-bold mb-2">支付成功</h2>
        <p className="text-gray-500 mb-2">订单号: {order.number}</p>
        <p className="text-lg font-medium text-gray-700 mb-6">
          支付金额: {formatPrice(order.amount)}
        </p>
        <p className="text-sm text-gray-400">正在跳转订单列表...</p>
      </div>
    );
  }

  // 支付失败/取消/超时页面
  if (['failed', 'cancelled', 'timeout'].includes(paymentResult.status)) {
    const errorConfig = {
      failed: {
        icon: X,
        iconColor: 'bg-red-500',
        title: '支付失败',
        defaultMsg: '网络请求失败，请检查网络后重试',
      },
      cancelled: {
        icon: X,
        iconColor: 'bg-orange-500',
        title: '支付已取消',
        defaultMsg: '您已取消支付',
      },
      timeout: {
        icon: Clock,
        iconColor: 'bg-yellow-500',
        title: '支付超时',
        defaultMsg: '支付超时，请重新尝试',
      },
    };

    const config = errorConfig[paymentResult.status as 'failed' | 'cancelled' | 'timeout'];
    const IconComponent = config.icon;

    return (
      <div className="min-h-screen bg-white flex flex-col items-center justify-center p-6">
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: 1 }}
          transition={{ type: 'spring', stiffness: 200 }}
          className={`w-24 h-24 ${config.iconColor} rounded-full flex items-center justify-center mb-6`}
        >
          <IconComponent className="w-12 h-12 text-white" />
        </motion.div>
        <h2 className="text-2xl font-bold mb-2">{config.title}</h2>
        <p className="text-gray-500 mb-6">{paymentResult.message || config.defaultMsg}</p>
        <div className="flex gap-4">
          <Button variant="outline" onClick={handleCancel}>
            返回订单
          </Button>
          <Button onClick={handleRetry}>
            重新支付
          </Button>
        </div>
      </div>
    );
  }

  // 支付中状态
  const isPaying = paymentResult.status === 'paying';

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 头部 */}
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">订单支付</h1>
        <div className="w-6" />
      </header>

      {/* 订单信息 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white m-4 p-6 rounded-xl text-center"
      >
        <p className="text-sm text-gray-500 mb-2">应付金额</p>
        <p className="text-4xl font-bold text-black">
          {formatPrice(order.amount)}
        </p>
        <p className="text-sm text-gray-400 mt-2">
          订单号: {order.number}
        </p>
      </motion.div>

      {/* 支付方式 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="bg-white m-4 p-4 rounded-xl"
      >
        <h3 className="font-medium mb-4">选择支付方式</h3>
        <div className="space-y-3">
          {payMethods.map((method) => (
            <button
              key={method.id}
              onClick={() => !isPaying && setSelectedMethod(method.id)}
              disabled={isPaying}
              className={`w-full flex items-center gap-4 p-4 rounded-xl border-2 transition-colors ${
                selectedMethod === method.id
                  ? 'border-black'
                  : 'border-gray-100'
              } ${isPaying ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              <span className="text-2xl">{method.icon}</span>
              <div className="flex-1 text-left">
                <p className="font-medium">{method.name}</p>
                {method.desc && (
                  <p className="text-xs text-gray-500">{method.desc}</p>
                )}
              </div>
              <div
                className={`w-5 h-5 rounded-full border-2 flex items-center justify-center ${
                  selectedMethod === method.id
                    ? 'border-black bg-black'
                    : 'border-gray-300'
                }`}
              >
                {selectedMethod === method.id && (
                  <Check className="w-3 h-3 text-white" />
                )}
              </div>
            </button>
          ))}
        </div>
      </motion.div>

      {/* 安全提示 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="flex items-center justify-center gap-2 text-sm text-gray-400 mt-4"
      >
        <Shield className="w-4 h-4" />
        <span>支付安全保护中</span>
      </motion.div>

      {/* 底部按钮 */}
      <div className="flex-1" />
      <div className="bg-white border-t border-gray-100 p-4 safe-area-bottom">
        <Button
          className="w-full h-14 text-lg"
          onClick={handlePay}
          disabled={isPaying}
        >
          {isPaying ? (
            <span className="flex items-center justify-center gap-2">
              <RefreshCw className="w-5 h-5 animate-spin" />
              支付中...
            </span>
          ) : (
            `确认支付 ${formatPrice(order.amount)}`
          )}
        </Button>
      </div>
    </div>
  );
}
