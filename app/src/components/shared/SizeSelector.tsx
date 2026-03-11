import { motion } from 'framer-motion';
import type { ShoeSkuSize } from '@/types';

interface SizeSelectorProps {
  sizes: ShoeSkuSize[];
  selectedSize: string | null;
  onSelect: (size: string) => void;
}

export function SizeSelector({ sizes, selectedSize, onSelect }: SizeSelectorProps) {
  if (!sizes || sizes.length === 0) {
    return null;
  }

  const sortedSizes = [...sizes].sort((a, b) => {
    const sizeA = parseFloat(a.size);
    const sizeB = parseFloat(b.size);
    return sizeA - sizeB;
  });

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <span className="text-sm text-gray-400">尺码</span>
        <span className="text-sm text-white">
          {selectedSize || '请选择'}
        </span>
      </div>
      <div className="grid grid-cols-4 gap-2">
        {sortedSizes.map((size) => {
          const isSelected = size.size === selectedSize;
          const isOutOfStock = size.stock === 0;
          
          return (
            <motion.button
              key={size.id}
              onClick={() => !isOutOfStock && onSelect(size.size)}
              disabled={isOutOfStock}
              className={`relative py-3 rounded-lg border text-center transition-all ${
                isSelected
                  ? 'border-white bg-white text-black font-medium'
                  : 'border-white/20 text-white hover:border-white/40'
              } ${isOutOfStock ? 'opacity-50 cursor-not-allowed line-through' : 'cursor-pointer'}`}
              whileHover={!isOutOfStock ? { scale: 1.02 } : {}}
              whileTap={!isOutOfStock ? { scale: 0.98 } : {}}
            >
              <span className="text-sm">{size.size}</span>
              {isSelected && (
                <motion.div
                  layoutId="sizeSelector"
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
