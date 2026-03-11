import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { OrderVO, OrderStatus } from '@/types';

/**
 * 默认缓存过期时间：5分钟（毫秒）
 */
const DEFAULT_CACHE_EXPIRES_IN = 5 * 60 * 1000;

interface OrderStore {
  // 缓存的订单列表
  orders: OrderVO[];
  // 缓存更新时间
  lastUpdated: number;
  // 缓存过期时间（毫秒）
  cacheExpiresIn: number;

  // 添加订单到缓存
  addOrder: (order: OrderVO) => void;
  // 更新订单状态
  updateOrderStatus: (orderNumber: string, status: OrderStatus) => void;
  // 获取订单（从缓存获取）
  getOrderByNumber: (orderNumber: string) => OrderVO | undefined;
  // 根据状态获取订单列表
  getOrdersByStatus: (status: OrderStatus | 'all') => OrderVO[];
  // 设置订单列表（并更新缓存时间）
  setOrders: (orders: OrderVO[]) => void;
  // 更新单个订单数据
  updateOrder: (order: OrderVO) => void;
  // 清除缓存
  clearCache: () => void;
  // 检查缓存是否过期
  isCacheExpired: () => boolean;
  // 设置缓存过期时间
  setCacheExpiresIn: (ms: number) => void;
}

export const useOrderStore = create<OrderStore>()(
  persist(
    (set, get) => ({
      orders: [],
      lastUpdated: 0,
      cacheExpiresIn: DEFAULT_CACHE_EXPIRES_IN,

      addOrder: (order) => {
        const { orders } = get();
        // 避免重复添加
        const exists = orders.some((o) => o.number === order.number);
        if (exists) {
          set({
            orders: orders.map((o) => (o.number === order.number ? order : o)),
            lastUpdated: Date.now(),
          });
        } else {
          set({
            orders: [order, ...orders],
            lastUpdated: Date.now(),
          });
        }
      },

      updateOrderStatus: (orderNumber, status) => {
        const { orders } = get();
        set({
          orders: orders.map((o) =>
            o.number === orderNumber ? { ...o, status } : o
          ),
          lastUpdated: Date.now(),
        });
      },

      getOrderByNumber: (orderNumber) => {
        const { orders } = get();
        return orders.find((o) => o.number === orderNumber);
      },

      getOrdersByStatus: (status) => {
        const { orders } = get();
        if (status === 'all') return orders;
        return orders.filter((o) => o.status === status);
      },

      setOrders: (orders) => {
        set({
          orders,
          lastUpdated: Date.now(),
        });
      },

      updateOrder: (order) => {
        const { orders } = get();
        const index = orders.findIndex((o) => o.number === order.number);
        if (index >= 0) {
          const newOrders = [...orders];
          newOrders[index] = order;
          set({
            orders: newOrders,
            lastUpdated: Date.now(),
          });
        } else {
          // 如果订单不存在，添加到列表开头
          set({
            orders: [order, ...orders],
            lastUpdated: Date.now(),
          });
        }
      },

      clearCache: () => {
        set({
          orders: [],
          lastUpdated: 0,
        });
      },

      isCacheExpired: () => {
        const { lastUpdated, cacheExpiresIn } = get();
        // 如果从未更新过，视为已过期
        if (lastUpdated === 0) return true;
        return Date.now() - lastUpdated > cacheExpiresIn;
      },

      setCacheExpiresIn: (ms) => {
        set({ cacheExpiresIn: ms });
      },
    }),
    {
      name: 'order-storage',
      // 只持久化必要的数据
      partialize: (state) => ({
        orders: state.orders,
        lastUpdated: state.lastUpdated,
        cacheExpiresIn: state.cacheExpiresIn,
      }),
    }
  )
);
