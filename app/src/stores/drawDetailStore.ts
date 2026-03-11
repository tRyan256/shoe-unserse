import { create } from 'zustand';
import type { Draw } from '@/types';
import { drawApi } from '@/api';

interface DrawDetailCache {
  data: Draw;
  cachedAt: number;
}

interface DrawDetailStore {
  // 缓存的抽签详情数据
  cache: Record<string, DrawDetailCache>;
  // 当前正在查看的详情
  currentDraw: Draw | null;
  isLoading: boolean;

  // 获取抽签详情（先查缓存，再查数据库）
  getDrawDetail: (drawId: number | string) => Promise<Draw | null>;
  // 强制从服务器获取（不使用缓存）
  fetchDrawDetailFromServer: (drawId: number | string) => Promise<Draw | null>;
  // 清空缓存
  clearCache: () => void;
  // 清除指定缓存
  removeCache: (drawId: number | string) => void;
}

// 缓存有效期：7天
const CACHE_TTL = 7 * 24 * 60 * 60 * 1000;

export const useDrawDetailStore = create<DrawDetailStore>()(
  (set, get) => ({
    cache: {},
    currentDraw: null,
    isLoading: false,

    // 获取抽签详情：先查缓存，缓存没有或过期则查数据库
    getDrawDetail: async (drawId: string | number) => {
      const id = String(drawId);
      const { cache } = get();
      const cachedItem = cache[id];

      // 检查缓存是否存在且未过期
      if (cachedItem && cachedItem.data) {
        const isExpired = Date.now() - cachedItem.cachedAt > CACHE_TTL;
        if (!isExpired) {
          // 缓存命中，直接返回缓存数据
          set({ currentDraw: cachedItem.data });
          return cachedItem.data;
        }
      }

      // 缓存未命中或已过期，从服务器获取
      return get().fetchDrawDetailFromServer(drawId);
    },

    // 从服务器获取详情（不写入缓存）
    fetchDrawDetailFromServer: async (drawId: string | number) => {
      set({ isLoading: true });

      try {
        const data = await drawApi.getDrawDetail(Number(drawId));
        if (data) {
          set({ currentDraw: data });
          // 注意：根据需求，从数据库查询的数据不回写缓存
          // 因为活动可能已经结束，缓存过期数据没有意义
        }
        return data;
      } catch (error) {
        console.error('Failed to fetch draw detail:', error);
        return null;
      } finally {
        set({ isLoading: false });
      }
    },

    // 清空所有缓存
    clearCache: () => {
      set({ cache: {} });
    },

    // 移除指定缓存
    removeCache: (drawId: string | number) => {
      const id = String(drawId);
      const { cache } = get();
      const newCache = { ...cache };
      delete newCache[id];
      set({ cache: newCache });
    },
  })
);
