import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Package, Sparkles, ChevronRight } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { bundleApi } from '@/api';
import type { Bundle } from '@/types';
import { formatPrice } from '@/utils';

const Skeleton = ({ className }: { className?: string }) => (
  <div className={`animate-pulse bg-gray-200 rounded ${className}`} />
);

const BundleCardSkeleton = () => (
  <div className="bg-white rounded-xl overflow-hidden shadow-sm">
    <Skeleton className="w-full h-32" />
    <div className="p-3 space-y-2">
      <Skeleton className="w-3/4 h-4" />
      <Skeleton className="w-full h-3" />
      <Skeleton className="w-16 h-5" />
    </div>
  </div>
);

export default function BundleListPage() {
  const navigate = useNavigate();
  const [bundles, setBundles] = useState<Bundle[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadBundles();
  }, []);

  const loadBundles = async () => {
    try {
      setIsLoading(true);
      const data = await bundleApi.getBundleList();
      setBundles(data);
    } catch (error) {
      console.error('Failed to load bundles:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white sticky top-0 z-40 shadow-sm">
        <button 
          onClick={() => navigate(-1)}
          className="w-9 h-9 flex items-center justify-center -ml-2 rounded-full hover:bg-gray-100 transition-colors"
        >
          <ArrowLeft className="w-5 h-5 text-gray-700" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold text-gray-900">组合包</h1>
        <div className="w-9" />
      </header>

      <motion.div 
        className="relative h-40 m-4 rounded-2xl overflow-hidden"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
      >
        <img
          src="https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&h=400&fit=crop"
          alt="bundle banner"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-r from-stone-900/80 via-orange-800/60 to-transparent flex items-center">
          <div className="p-6 text-white">
            <div className="flex items-center gap-2 mb-2">
              <Sparkles className="w-5 h-5" />
              <h2 className="text-2xl font-bold">超值组合包</h2>
            </div>
            <p className="text-sm text-white/80">精选球鞋组合</p>
            <p className="text-sm text-white/80">享受更多优惠</p>
          </div>
        </div>
      </motion.div>

      <div className="flex-1 px-3 pb-4">
        {isLoading ? (
          <div className="grid grid-cols-2 gap-3">
            <BundleCardSkeleton />
            <BundleCardSkeleton />
            <BundleCardSkeleton />
            <BundleCardSkeleton />
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {bundles.map((bundle, index) => (
              <motion.div
                key={bundle.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <Link to={`/bundle/${bundle.id}`}>
                  <div className="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow duration-300">
                    <div className="relative h-32">
                      <img
                        src={bundle.image}
                        alt={bundle.name}
                        className="w-full h-full object-cover"
                      />
                      <div className="absolute top-2 left-2">
                        <Badge className="bg-rose-500/90 text-white border-0 text-[10px] px-2 py-0.5">
                          <Package className="w-2.5 h-2.5 mr-0.5" />
                          组合
                        </Badge>
                      </div>
                      <div className="absolute inset-x-0 bottom-0 h-12 bg-gradient-to-t from-black/30 to-transparent" />
                    </div>

                    <div className="p-2.5">
                      <h3 className="font-medium text-sm text-gray-900 mb-1 line-clamp-1">{bundle.name}</h3>
                      <p className="text-gray-400 text-xs mb-2 line-clamp-1">
                        {bundle.description}
                      </p>

                      <div className="flex items-center justify-between">
                        <span className="text-base font-bold text-red-500">
                          {formatPrice(bundle.price)}
                        </span>
                        <ChevronRight className="w-4 h-4 text-gray-300" />
                      </div>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))}
          </div>
        )}

        {!isLoading && bundles.length === 0 && (
          <motion.div 
            className="text-center py-16"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
          >
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Package className="w-10 h-10 text-gray-300" />
            </div>
            <p className="text-gray-400 mb-2">暂无组合包</p>
            <p className="text-gray-300 text-sm">敬请期待更多精彩组合</p>
          </motion.div>
        )}
      </div>
    </div>
  );
}


