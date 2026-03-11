import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { ArrowLeft, Trash2, ShoppingBag, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { CartItemCard } from '@/components/shared';
import { useCartStore, useUserStore } from '@/stores';
import { formatPrice } from '@/utils';
import { toast } from 'sonner';

export default function CartPage() {
  const navigate = useNavigate();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const {
    items,
    selectedIds,
    isLoading,
    selectAll,
    toggleSelect,
    fetchCart,
    syncUpdateQuantity,
    syncRemoveItem,
    syncClearCart,
    getSelectedItems,
    getTotalPrice,
    getSelectedCount,
  } = useCartStore();
  const [showClearConfirm, setShowClearConfirm] = useState(false);
  const [clearing, setClearing] = useState(false);
  const [removingIds, setRemovingIds] = useState<Set<string>>(new Set());
  const [updatingIds, setUpdatingIds] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (isLoggedIn()) {
      fetchCart();
    }
  }, [isLoggedIn, fetchCart]);

  const selectedItems = getSelectedItems();
  const totalPrice = getTotalPrice();
  const selectedCount = getSelectedCount();
  const allSelected = items.length > 0 && selectedIds.length === items.length;

  const handleUpdateQuantity = async (id: string, quantity: number) => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }
    setUpdatingIds((prev) => new Set(prev).add(id));
    try {
      await syncUpdateQuantity(id, quantity);
    } catch {
      toast.error('修改失败');
    } finally {
      setUpdatingIds((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    }
  };

  const handleRemove = async (id: string) => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }
    setRemovingIds((prev) => new Set(prev).add(id));
    try {
      await syncRemoveItem(id);
      toast.success('已删除');
    } catch {
      toast.error('删除失败');
    } finally {
      setRemovingIds((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    }
  };

  const handleClearCart = async () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }
    setClearing(true);
    try {
      await syncClearCart();
      setShowClearConfirm(false);
      toast.success('购物车已清空');
    } catch {
      toast.error('清空失败');
    } finally {
      setClearing(false);
    }
  };

  const handleCheckout = () => {
    if (selectedItems.length === 0) return;
    navigate('/order/confirm');
  };

  if (isLoading && items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <header className="flex items-center justify-between px-3 h-10 bg-white">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-4 h-4" />
          </button>
          <h1 className="text-sm font-bold">购物车</h1>
          <div className="w-4" />
        </header>
        <div className="flex-1 flex items-center justify-center">
          <Loader2 className="w-5 h-5 animate-spin text-emerald-600" />
        </div>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <header className="flex items-center justify-between px-3 h-10 bg-white">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-4 h-4" />
          </button>
          <h1 className="text-sm font-bold">购物车</h1>
          <div className="w-4" />
        </header>

        <div className="flex-1 flex flex-col items-center justify-center">
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            className="text-center"
          >
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-3">
              <ShoppingBag className="w-10 h-10 text-gray-300" />
            </div>
            <h2 className="text-sm font-medium text-gray-900 mb-1">购物车是空的</h2>
            <p className="text-[10px] text-gray-500 mb-3">快去挑选心仪的球鞋吧</p>
            <Link to="/shop">
              <Button size="sm" className="text-xs">去逛逛</Button>
            </Link>
          </motion.div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center justify-between px-3 h-10 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-4 h-4" />
        </button>
        <h1 className="text-sm font-bold">购物车 ({items.length})</h1>
        <button
          onClick={() => setShowClearConfirm(true)}
          className="text-[10px] text-gray-500"
        >
          清空
        </button>
      </header>

      <div className="flex-1 overflow-auto p-2 space-y-2">
        <AnimatePresence mode="popLayout">
          {items.map((item) => (
            <CartItemCard
              key={item.id}
              item={item}
              selected={selectedIds.includes(item.id)}
              onSelect={toggleSelect}
              onUpdateQuantity={handleUpdateQuantity}
              onRemove={handleRemove}
              isUpdating={updatingIds.has(item.id)}
              isRemoving={removingIds.has(item.id)}
            />
          ))}
        </AnimatePresence>
      </div>

      <div className="bg-white border-t border-gray-100 p-2 safe-area-bottom">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <Checkbox
              checked={allSelected}
              onCheckedChange={(checked) => selectAll(checked as boolean)}
            />
            <span className="text-[10px] text-gray-600">全选</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="text-right">
              <span className="text-[10px] text-gray-500">合计: </span>
              <span className="text-sm font-bold text-black">
                {formatPrice(totalPrice)}
              </span>
            </div>
            <Button
              size="sm"
              onClick={handleCheckout}
              disabled={selectedCount === 0}
              className="px-3 text-xs h-7"
            >
              结算 ({selectedCount})
            </Button>
          </div>
        </div>
      </div>

      <Sheet open={showClearConfirm} onOpenChange={setShowClearConfirm}>
        <SheetContent side="bottom" className="rounded-t-2xl">
          <SheetHeader>
            <SheetTitle className="text-sm">确认清空购物车？</SheetTitle>
            <SheetDescription className="sr-only">确认后将删除购物车中的全部商品</SheetDescription>
          </SheetHeader>
          <div className="flex gap-2 mt-3">
            <Button
              variant="outline"
              size="sm"
              className="flex-1 text-xs h-8"
              onClick={() => setShowClearConfirm(false)}
              disabled={clearing}
            >
              取消
            </Button>
            <Button
              variant="destructive"
              size="sm"
              className="flex-1 text-xs h-8"
              onClick={handleClearCart}
              disabled={clearing}
            >
              {clearing ? <Loader2 className="w-3 h-3 animate-spin" /> : '确认清空'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}

