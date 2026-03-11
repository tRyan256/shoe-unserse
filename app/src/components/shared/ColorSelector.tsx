import { motion } from 'framer-motion';
import type { ShoeSku } from '@/types';

interface ColorSelectorProps {
  skus: ShoeSku[];
  selectedSkuId: number | null;
  onSelect: (sku: ShoeSku) => void;
}

export function ColorSelector({ skus, selectedSkuId, onSelect }: ColorSelectorProps) {
  if (!skus || skus.length === 0) {
    return null;
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2">
        <span className="text-sm text-gray-400">颜色</span>
        <span className="text-sm text-white">
          {skus.find(s => s.id === selectedSkuId)?.colorName || '请选择'}
        </span>
      </div>
      <div className="flex flex-wrap gap-3">
        {skus.map((sku) => {
          const isSelected = sku.id === selectedSkuId;
          const isOutOfStock = sku.stock === 0;
          
          return (
            <motion.button
              key={sku.id}
              onClick={() => !isOutOfStock && onSelect(sku)}
              disabled={isOutOfStock}
              className={`relative flex items-center gap-2 px-4 py-2 rounded-lg border transition-all ${
                isSelected
                  ? 'border-white bg-white/10'
                  : 'border-white/20 hover:border-white/40'
              } ${isOutOfStock ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
              whileHover={!isOutOfStock ? { scale: 1.02 } : {}}
              whileTap={!isOutOfStock ? { scale: 0.98 } : {}}
            >
              {sku.colorCode && (
                <span
                  className="w-4 h-4 rounded-full border border-white/30"
                  style={{ backgroundColor: sku.colorCode }}
                />
              )}
              <span className="text-sm text-white">{sku.colorName}</span>
              {isOutOfStock && (
                <span className="text-xs text-red-400">缺货</span>
              )}
              {isSelected && (
                <motion.div
                  layoutId="colorSelector"
                  className="absolute inset-0 border-2 border-white rounded-lg"
                  transition={{ type: 'spring', bounce: 0.2, duration: 0.6 }}
                />
              )}
            </motion.button>
          );
        })}
      </div>
    </div>
  );
}
