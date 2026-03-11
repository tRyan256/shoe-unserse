import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { CartItem } from '@/types';
import { cartApi } from '@/api';

interface CartStore {
  items: CartItem[];
  selectedIds: string[];
  isLoading: boolean;
  addItem: (item: CartItem) => void;
  removeItem: (id: string) => void;
  updateQuantity: (id: string, quantity: number) => void;
  toggleSelect: (id: string) => void;
  selectAll: (selected: boolean) => void;
  clearCart: () => void;
  clearSelected: () => void;
  getSelectedItems: () => CartItem[];
  getTotalPrice: () => number;
  getSelectedCount: () => number;
  getTotalCount: () => number;
  fetchCart: () => Promise<void>;
  syncRemoveItem: (id: string) => Promise<void>;
  syncUpdateQuantity: (id: string, quantity: number) => Promise<void>;
  syncClearCart: () => Promise<void>;
}

export const useCartStore = create<CartStore>()(
  persist(
    (set, get) => ({
      items: [],
      selectedIds: [],
      isLoading: false,

      addItem: (item) => {
        const { items } = get();
        const existingItem = items.find(
          (i) => i.skuId === item.skuId && i.size === item.size
        );
        if (existingItem) {
          set({
            items: items.map((i) =>
              i.id === existingItem.id
                ? { ...i, quantity: i.quantity + item.quantity }
                : i
            ),
          });
        } else {
          set({ items: [...items, item] });
        }
      },

      removeItem: (id) => {
        const { items, selectedIds } = get();
        set({
          items: items.filter((i) => i.id !== id),
          selectedIds: selectedIds.filter((sid) => sid !== id),
        });
      },

      updateQuantity: (id, quantity) => {
        const { items } = get();
        if (quantity <= 0) {
          get().removeItem(id);
        } else {
          set({
            items: items.map((i) =>
              i.id === id ? { ...i, quantity } : i
            ),
          });
        }
      },

      toggleSelect: (id) => {
        const { selectedIds } = get();
        if (selectedIds.includes(id)) {
          set({ selectedIds: selectedIds.filter((sid) => sid !== id) });
        } else {
          set({ selectedIds: [...selectedIds, id] });
        }
      },

      selectAll: (selected) => {
        const { items } = get();
        if (selected) {
          set({ selectedIds: items.map((i) => i.id) });
        } else {
          set({ selectedIds: [] });
        }
      },

      clearCart: () => set({ items: [], selectedIds: [] }),

      clearSelected: () => {
        const { items, selectedIds } = get();
        set({
          items: items.filter((i) => !selectedIds.includes(i.id)),
          selectedIds: [],
        });
      },

      getSelectedItems: () => {
        const { items, selectedIds } = get();
        return items.filter((i) => selectedIds.includes(i.id));
      },

      getTotalPrice: () => {
        const { items, selectedIds } = get();
        return items
          .filter((i) => selectedIds.includes(i.id))
          .reduce((sum, i) => sum + i.price * i.quantity, 0);
      },

      getSelectedCount: () => {
        const { selectedIds } = get();
        return selectedIds.length;
      },

      getTotalCount: () => {
        const { items } = get();
        return items.reduce((sum, i) => sum + i.quantity, 0);
      },

      fetchCart: async () => {
        set({ isLoading: true });
        try {
          const serverItems = await cartApi.getCartList();
          const mappedItems: CartItem[] = (serverItems || []).map((item: any, index: number) => ({
            id: item.id?.toString() || `server-${index}`,
            shoeId: item.shoeId || item.spuId,
            spuId: item.spuId,
            skuId: item.skuId,
            shoeName: item.shoeName || item.name || '',
            shoeImage: item.shoeImage || item.image || '',
            size: item.shoeSize || item.size || '',
            price: item.amount || 0,
            quantity: item.number || item.quantity || 1,
            selected: item.selected !== false,
          }));
          set({
            items: mappedItems,
            selectedIds: mappedItems.filter((i) => i.selected).map((i) => i.id),
          });
        } catch (error) {
          console.error('Failed to fetch cart:', error);
        } finally {
          set({ isLoading: false });
        }
      },

      syncRemoveItem: async (id) => {
        const { items } = get();
        const item = items.find((i) => i.id === id);
        if (!item) return;

        try {
          await cartApi.subFromCart({
            spuId: item.spuId,
            skuId: item.skuId,
            shoeSize: item.size,
          });
          get().removeItem(id);
        } catch (error) {
          console.error('Failed to remove item:', error);
          throw error;
        }
      },

      syncUpdateQuantity: async (id, quantity) => {
        const { items } = get();
        const item = items.find((i) => i.id === id);
        if (!item) return;

        const diff = quantity - item.quantity;

        try {
          if (diff > 0) {
            for (let i = 0; i < diff; i++) {
              await cartApi.addToCart({
                spuId: item.spuId,
                skuId: item.skuId,
                shoeSize: item.size,
                selected: 1,
              });
            }
          } else if (diff < 0) {
            for (let i = 0; i < Math.abs(diff); i++) {
              await cartApi.subFromCart({
                spuId: item.spuId,
                skuId: item.skuId,
                shoeSize: item.size,
              });
            }
          }
          get().updateQuantity(id, quantity);
        } catch (error) {
          console.error('Failed to update quantity:', error);
          throw error;
        }
      },

      syncClearCart: async () => {
        try {
          await cartApi.clearCart();
          get().clearCart();
        } catch (error) {
          console.error('Failed to clear cart:', error);
          throw error;
        }
      },
    }),
    {
      name: 'cart-storage',
    }
  )
);
