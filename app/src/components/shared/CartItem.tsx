import { motion } from 'framer-motion';
import { Minus, Plus, Trash2, Loader2 } from 'lucide-react';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import type { CartItem as CartItemType } from '@/types';
import { formatPrice } from '@/utils';

interface CartItemProps {
  item: CartItemType;
  selected?: boolean;
  onSelect: (id: string) => void;
  onUpdateQuantity: (id: string, quantity: number) => void;
  onRemove: (id: string) => void;
  isUpdating?: boolean;
  isRemoving?: boolean;
}

export function CartItemCard({
  item,
  selected = item.selected,
  onSelect,
  onUpdateQuantity,
  onRemove,
  isUpdating = false,
  isRemoving = false,
}: CartItemProps) {
  const disabled = isUpdating || isRemoving;

  return (
    <motion.div
      layout
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: 20 }}
      className={`flex gap-2 p-2 bg-white rounded-lg ${isRemoving ? 'opacity-50' : ''}`}
    >
      <div className="flex items-center">
        <Checkbox
          checked={selected}
          onCheckedChange={() => onSelect(item.id)}
          disabled={disabled}
        />
      </div>

      <div className="w-14 h-14 flex-shrink-0 bg-gray-50 rounded-md overflow-hidden">
        <img
          src={item.shoeImage}
          alt={item.shoeName}
          className="w-full h-full object-cover"
        />
      </div>

      <div className="flex-1 min-w-0">
        <h3 className="text-xs font-medium text-gray-900 line-clamp-1">
          {item.shoeName}
        </h3>
        <p className="text-[10px] text-gray-500 mt-0.5">尺码: {item.size}</p>
        <div className="flex items-center justify-between mt-1.5">
          <span className="text-xs font-bold text-black">{formatPrice(item.price)}</span>

          <div className="flex items-center gap-0.5">
            <Button
              variant="outline"
              size="icon"
              className="w-5 h-5"
              onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
              disabled={disabled}
            >
              {isUpdating ? (
                <Loader2 className="w-2 h-2 animate-spin" />
              ) : (
                <Minus className="w-2 h-2" />
              )}
            </Button>
            <span className="w-5 text-center text-[10px]">{item.quantity}</span>
            <Button
              variant="outline"
              size="icon"
              className="w-5 h-5"
              onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
              disabled={disabled}
            >
              {isUpdating ? (
                <Loader2 className="w-2 h-2 animate-spin" />
              ) : (
                <Plus className="w-2 h-2" />
              )}
            </Button>
          </div>
        </div>
      </div>

      <motion.button
        whileTap={{ scale: 0.9 }}
        onClick={() => onRemove(item.id)}
        disabled={disabled}
        className="flex items-center justify-center w-5 h-5 text-gray-400 hover:text-red-500 transition-colors disabled:opacity-50"
      >
        {isRemoving ? (
          <Loader2 className="w-2.5 h-2.5 animate-spin" />
        ) : (
          <Trash2 className="w-2.5 h-2.5" />
        )}
      </motion.button>
    </motion.div>
  );
}
