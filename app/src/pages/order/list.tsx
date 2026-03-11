import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, ShoppingBag, Loader2, Package, CreditCard, Truck, CheckCircle, Clock } from 'lucide-react';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { OrderCard, ScrollToTop } from '@/components/shared';
import { orderApi } from '@/api';
import type { OrderVO, OrderStatus, OrderListParams } from '@/types';

const tabs = [
  { value: 'all', label: '全部', icon: Package },
  { value: '1', label: '待付款', icon: CreditCard },
  { value: '2', label: '待发货', icon: Package },
  { value: 'receiving', label: '待收货', icon: Truck },
  { value: '6', label: '已完成', icon: CheckCircle },
];

const PAGE_SIZE = 10;

const RECEIVING_STATUSES = [3, 4, 5];

export default function OrderListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [orders, setOrders] = useState<OrderVO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [activeTab, setActiveTab] = useState(searchParams.get('status') || 'all');

  const loadOrders = useCallback(async (pageNum: number, isRefresh = false) => {
    try {
      if (isRefresh) {
        setIsRefreshing(true);
      } else if (pageNum === 1) {
        setIsLoading(true);
      } else {
        setIsLoadingMore(true);
      }

      let status: OrderStatus | undefined;
      let statusList: OrderStatus[] | undefined;

      if (activeTab === 'receiving') {
        statusList = RECEIVING_STATUSES as OrderStatus[];
      } else if (activeTab === '6') {
        statusList = [6, 8] as OrderStatus[];
      } else if (activeTab !== 'all') {
        status = parseInt(activeTab) as OrderStatus;
      }

      const params: OrderListParams = {
        page: pageNum,
        pageSize: PAGE_SIZE,
        ...(status && { status }),
        ...(statusList && { statusList }),
      };

      const result = await orderApi.getOrderList(params);
      
      if (isRefresh || pageNum === 1) {
        setOrders(result.records);
      } else {
        setOrders((prev) => [...prev, ...result.records]);
      }
      
      setTotal(result.total);
      setHasMore(result.records.length === PAGE_SIZE);
      setPage(pageNum);
    } catch (error) {
      console.error('Failed to load orders:', error);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
      setIsLoadingMore(false);
    }
  }, [activeTab]);

  useEffect(() => {
    loadOrders(1);
  }, [activeTab]);

  const handleRefresh = () => {
    loadOrders(1, true);
  };

  const handleLoadMore = () => {
    if (!isLoadingMore && hasMore) {
      loadOrders(page + 1);
    }
  };

  const handleScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
    const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
    if (scrollHeight - scrollTop - clientHeight < 100 && hasMore && !isLoadingMore) {
      handleLoadMore();
    }
  }, [hasMore, isLoadingMore]);

  const handleTabChange = (value: string) => {
    setActiveTab(value);
    if (value === 'all') {
      searchParams.delete('status');
    } else {
      searchParams.set('status', value);
    }
    setSearchParams(searchParams);
  };

  const handleCancel = async (orderNumber: string) => {
    try {
      await orderApi.cancelOrder(orderNumber);
      loadOrders(1);
    } catch (error) {
      console.error('Failed to cancel order:', error);
    }
  };

  const handlePay = (orderNumber: string) => {
    navigate(`/order/payment?orderNumber=${orderNumber}`);
  };

  const handleConfirm = async (orderNumber: string) => {
    try {
      await orderApi.confirmOrder(orderNumber);
      loadOrders(1);
    } catch (error) {
      console.error('Failed to confirm order:', error);
    }
  };

  const handleReorder = async (orderNumber: string) => {
    try {
      await orderApi.reorder(orderNumber);
      navigate('/cart');
    } catch (error) {
      console.error('Failed to reorder:', error);
    }
  };

  const handleReminder = async (orderNumber: string) => {
    try {
      await orderApi.reminderOrder(orderNumber);
    } catch (error) {
      console.error('Failed to reminder:', error);
    }
  };

  const handleReview = (orderNumber: string) => {
    navigate(`/order/review/${orderNumber}`);
  };

  if (isLoading && !isRefreshing) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-10 h-10 border-2 border-emerald-200 rounded-full animate-spin border-t-emerald-600" />
          <span className="text-xs text-gray-400">加载中...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white flex flex-col">
      <header className="bg-white border-b border-gray-100">
        <div className="flex items-center px-4 h-12">
          <button onClick={() => navigate('/user')} className="text-gray-800">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="flex-1 text-center text-base font-bold text-gray-800">我的订单</h1>
          <div className="w-5" />
        </div>
        <div className="px-4 pb-2">
          <div className="flex items-center justify-center gap-1.5 text-gray-500 text-xs">
            <Clock className="w-3 h-3" />
            <span>共 {total} 笔订单</span>
          </div>
        </div>
      </header>

      <div className="bg-white shadow-sm sticky top-0 z-10 border-b border-gray-100">
        <Tabs value={activeTab} onValueChange={handleTabChange}>
          <TabsList className="w-full justify-start h-8 bg-transparent rounded-none px-2 gap-0">
            {tabs.map((tab) => {
              const IconComponent = tab.icon;
              return (
                <TabsTrigger
                  key={tab.value}
                  value={tab.value}
                  className="flex-1 data-[state=active]:bg-transparent data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-none text-[10px] text-gray-500 px-1"
                >
                  <div className="flex items-center gap-0.5">
                    <IconComponent className="w-3 h-3" strokeWidth={1.5} />
                    <span>{tab.label}</span>
                  </div>
                </TabsTrigger>
              );
            })}
          </TabsList>
        </Tabs>
      </div>

      <div 
        className="flex-1 overflow-auto p-3" 
        onScroll={handleScroll}
      >
        {isRefreshing && (
          <div className="flex items-center justify-center py-3">
            <Loader2 className="w-4 h-4 animate-spin text-emerald-600" />
            <span className="ml-2 text-xs text-emerald-600">刷新中...</span>
          </div>
        )}

        {orders.length === 0 && !isLoading ? (
          <div className="flex flex-col items-center justify-center py-16">
            <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-3">
              <ShoppingBag className="w-7 h-7 text-gray-300" strokeWidth={1.5} />
            </div>
            <h2 className="text-sm font-medium text-gray-700 mb-1">暂无订单</h2>
            <p className="text-xs text-gray-400 mb-4">快去挑选心仪的球鞋吧</p>
            <button
              onClick={() => navigate('/')}
              className="px-6 py-2 bg-emerald-600 text-white rounded-full text-xs hover:bg-emerald-700 transition-colors"
            >
              去逛逛
            </button>
          </div>
        ) : (
          <div className="space-y-3">
            {orders.map((order) => (
              <OrderCard
                key={order.number}
                order={order}
                onCancel={handleCancel}
                onPay={handlePay}
                onConfirm={handleConfirm}
                onReorder={handleReorder}
                onReminder={handleReminder}
                onReview={handleReview}
              />
            ))}

            {isLoadingMore && (
              <div className="flex items-center justify-center py-3">
                <Loader2 className="w-4 h-4 animate-spin text-gray-400" />
                <span className="ml-2 text-xs text-gray-400">加载更多...</span>
              </div>
            )}

            {!hasMore && orders.length > 0 && (
              <div className="text-center py-3 text-xs text-gray-400">
                没有更多订单了
              </div>
            )}

            {orders.length > 0 && !isLoadingMore && hasMore && (
              <div className="text-center py-2 text-xs text-gray-400">
                已加载 {orders.length} / {total} 条
              </div>
            )}
          </div>
        )}
      </div>

      <ScrollToTop />
    </div>
  );
}






