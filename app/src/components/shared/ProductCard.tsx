import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Badge } from '@/components/ui/badge';
import type { ShoeSpu } from '@/types';
import { formatPrice } from '@/utils';

interface ProductCardProps {
  shoe: ShoeSpu;
  index?: number;
}

export function ProductCard({ shoe, index = 0 }: ProductCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{
        duration: 0.5,
        delay: index * 0.1,
        ease: [0.16, 1, 0.3, 1],
      }}
    >
      <Link to={`/shoe/${shoe.id}`}>
        <motion.div
          whileHover={{ y: -8 }}
          transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
          className="group relative bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-shadow"
        >
          {/* 图片区域 */}
          <div className="relative aspect-[4/3] bg-gray-100/30 overflow-hidden">
            <motion.img
              src={shoe.defaultImage}
              alt={shoe.name}
              className="w-full h-full object-cover"
              whileHover={{ scale: 1.05 }}
              transition={{ duration: 0.4 }}
            />
          </div>
          
          {/* 标签 */}
          <div className="absolute top-3 left-3 flex gap-2">
            {shoe.isLimited === 1 && (
              <Badge className="bg-black text-white text-xs">限量</Badge>
            )}
          </div>

          {/* 内容区域 */}
          <div className="p-4">
            <h3 className="font-medium text-gray-900 line-clamp-1 mb-1">
              {shoe.name}
            </h3>
            <p className="text-sm text-gray-500 line-clamp-1 mb-3">
              {shoe.description}
            </p>
            <div className="flex items-center justify-between">
              <div className="flex items-baseline gap-2">
                <span className="text-lg font-bold text-black">
                  {formatPrice(shoe.minPrice)}
                </span>
              </div>
              <div className="flex items-center gap-3">
                {shoe.releaseDate && (
                  <span className="text-xs text-gray-400">
                    {shoe.releaseDate}
                  </span>
                )}
                {shoe.salesCount !== undefined && (
                  <span className="text-xs text-gray-400">
                    已购买 {shoe.salesCount}
                  </span>
                )}
              </div>
            </div>
          </div>
        </motion.div>
      </Link>
    </motion.div>
  );
}
