import { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams, Link, useLocation } from 'react-router-dom';
import { motion, useScroll, useTransform } from 'framer-motion';
import { 
  ArrowLeft, 
  Share2, 
  Heart, 
  Package,
  ShoppingCart,
  ChevronRight,
  Check,
  Sparkles,
  Info
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { bundleApi, cartApi } from '@/api';
import { useCartStore, useUserStore } from '@/stores';
import type { BundleVO, Shoe, ShoeSkuSize } from '@/types';
import { formatPrice } from '@/utils';

const FROM_COMMUNITY_DETAIL_KEY = 'from_community_detail';

const fadeInUp = {
  initial: { opacity: 0, y: 16 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1] }
};

export default function BundleDetailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams();
  const { isLoggedIn } = useUserStore();
  const addToCart = useCartStore((state) => state.addItem);
  const containerRef = useRef<HTMLDivElement>(null);
  
  const { scrollY } = useScroll();
  const imageScale = useTransform(scrollY, [0, 200], [1, 1.08]);
  const imageOpacity = useTransform(scrollY, [0, 150], [1, 0.85]);

  const [bundle, setBundle] = useState<BundleVO | null>(null);
  const [shoes, setShoes] = useState<Shoe[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLiked, setIsLiked] = useState(false);
  const [showShoesSheet, setShowShoesSheet] = useState(false);
  const [showSizeSheet, setShowSizeSheet] = useState(false);
  const [isAddingToCart, setIsAddingToCart] = useState(false);
  const [selectedSizes, setSelectedSizes] = useState<Record<number, string>>({});

  useEffect(() => {
    if (id) {
      loadBundleDetail(id);
    }
  }, [id]);

  const handleGoBack = () => {
    const fromCommunityDetail = sessionStorage.getItem(FROM_COMMUNITY_DETAIL_KEY);
    if (fromCommunityDetail) {
      sessionStorage.removeItem(FROM_COMMUNITY_DETAIL_KEY);
      navigate('/community', { state: { fromDetail: true } });
    } else {
      navigate(-1);
    }
  };

  const loadBundleDetail = async (bundleId: string) => {
    try {
      setIsLoading(true);
      const bundleData = await bundleApi.getBundleDetail(Number(bundleId));
      if (bundleData) {
        setBundle(bundleData);
        setShoes(bundleData.shoeItems || []);
      }
    } catch (error) {
      console.error('Failed to load bundle detail:', error);
      toast.error('加载组合包详情失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddToCart = async () => {
    if (!isLoggedIn()) {
      navigate('/login');
      return;
    }
    if (!bundle) return;

    const unselectedShoes = shoes.filter(shoe => !selectedSizes[shoe.id]);
    if (unselectedShoes.length > 0) {
      toast.error('请先选择所有鞋款的尺码');
      setShowSizeSheet(true);
      return;
    }

    setIsAddingToCart(true);
    try {
      for (const shoe of shoes) {
        const selectedSize = selectedSizes[shoe.id] || shoe.sizes?.[0]?.size || '42';
        await cartApi.addToCart({
          bundleId: Number(bundle.id),
          shoeId: Number(shoe.id),
          shoeSize: selectedSize,
          selected: 1,
        });

        addToCart({
          id: `${bundle.id}-${shoe.id}`,
          shoeId: shoe.id.toString(),
          bundleId: bundle.id,
          shoeName: shoe.name,
          shoeImage: shoe.image || '',
          size: selectedSize,
          price: shoe.price,
          quantity: 1,
          selected: true,
        });
      }

      toast.success('已添加到购物车');
    } catch (error) {
      console.error('Failed to add to cart:', error);
      toast.error('添加到购物车失败');
    } finally {
      setIsAddingToCart(false);
    }
  };

  const handleBuyNow = () => {
    if (!isLoggedIn()) {
      toast.error('请先登录');
      return;
    }
    handleAddToCart().then(() => {
      navigate('/order/confirm');
    });
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
        <motion.div 
          className="w-8 h-8 border-2 border-white/20 border-t-white rounded-full"
          animate={{ rotate: 360 }}
          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
        />
      </div>
    );
  }

  if (!bundle) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
        <div className="text-center">
          <Package className="w-16 h-16 text-white/20 mx-auto mb-4" />
          <p className="text-white/60">组合包不存在</p>
          <Button 
            variant="outline" 
            className="mt-4 border-white/20 text-white hover:bg-white/10"
            onClick={() => navigate('/bundle/list')}
          >
            返回列表
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div ref={containerRef} className="min-h-screen bg-neutral-950 text-white">
      <motion.header 
        className="fixed top-0 left-0 right-0 z-50 px-3 py-2"
        initial={{ y: -100 }}
        animate={{ y: 0 }}
        transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="flex items-center justify-between">
          <motion.button
            onClick={handleGoBack}
            className="w-9 h-9 bg-white/10 backdrop-blur-xl rounded-full flex items-center justify-center border border-white/10"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <ArrowLeft className="w-4 h-4" />
          </motion.button>
          
          <div className="flex gap-2">
            <motion.button
              onClick={() => setIsLiked(!isLiked)}
              className="w-9 h-9 bg-white/10 backdrop-blur-xl rounded-full flex items-center justify-center border border-white/10"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              <Heart
                className={`w-4 h-4 transition-all duration-300 ${
                  isLiked ? 'fill-red-500 text-red-500 scale-110' : ''
                }`}
              />
            </motion.button>
            <motion.button 
              className="w-9 h-9 bg-white/10 backdrop-blur-xl rounded-full flex items-center justify-center border border-white/10"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
            >
              <Share2 className="w-4 h-4" />
            </motion.button>
          </div>
        </div>
      </motion.header>

      <div className="relative h-[50vh] overflow-hidden">
        <motion.div 
          className="absolute inset-0"
          style={{ scale: imageScale }}
        >
          <img
            src={bundle.image}
            alt={bundle.name}
            className="w-full h-full object-cover"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-neutral-950 via-neutral-950/20 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-b from-neutral-950/20 via-transparent to-transparent" />
        </motion.div>

        <motion.div 
          className="absolute top-20 left-3"
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.3 }}
        >
          <div className="flex items-center gap-1.5 bg-gradient-to-r from-rose-500 to-pink-500 px-3 py-1.5 rounded-full">
            <Sparkles className="w-3 h-3 text-white" />
            <span className="text-xs font-medium text-white">超值组合</span>
          </div>
        </motion.div>

        <motion.div 
          className="absolute bottom-6 left-4 right-4"
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <p className="text-white/50 text-xs font-medium tracking-wider uppercase mb-1">
            精选组合
          </p>
          <h1 className="text-xl md:text-2xl font-bold text-white leading-tight">
            {bundle.name}
          </h1>
        </motion.div>
      </div>

      <motion.div 
        className="relative -mt-4 bg-neutral-950 rounded-t-2xl px-4 pt-5 pb-28"
        initial={{ y: 80 }}
        animate={{ y: 0 }}
        transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
      >
        <motion.div 
          className="flex items-end justify-between mb-4"
          variants={fadeInUp}
          initial="initial"
          animate="animate"
        >
          <div>
            <p className="text-white/40 text-xs mb-0.5">组合价</p>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl font-bold text-white">
                {formatPrice(bundle.price)}
              </span>
            </div>
          </div>
        </motion.div>

        {bundle.description && (
          <motion.p 
            className="text-white/60 text-sm leading-relaxed mb-5"
            variants={fadeInUp}
            initial="initial"
            animate="animate"
            transition={{ delay: 0.1 }}
          >
            {bundle.description}
          </motion.p>
        )}

        <motion.div 
          className="mb-5"
          variants={fadeInUp}
          initial="initial"
          animate="animate"
          transition={{ delay: 0.15 }}
        >
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-white text-sm font-medium">
              包含 {shoes.length} 款球鞋
            </h3>
            <button 
              onClick={() => setShowShoesSheet(true)}
              className="text-white/50 text-xs flex items-center gap-0.5 hover:text-white/70 transition-colors"
            >
              查看全部
              <ChevronRight className="w-3 h-3" />
            </button>
          </div>
          
          <div className="flex gap-3 overflow-x-auto scrollbar-hide pb-2">
            {shoes.slice(0, 4).map((shoe, index) => (
              <motion.div
                key={shoe.id}
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.2 + index * 0.05 }}
                className="flex-shrink-0"
              >
                <div className="w-20 h-20 rounded-xl overflow-hidden bg-white/5 border border-white/10">
                  <img
                    src={shoe.image || '/placeholder-shoe.png'}
                    alt={shoe.name}
                    className="w-full h-full object-cover"
                  />
                </div>
                <p className="text-white/60 text-[10px] mt-1.5 line-clamp-1 w-20">
                  {shoe.name}
                </p>
                {shoe.colorName && (
                  <p className="text-white/40 text-[9px] line-clamp-1 w-20">
                    {shoe.colorName}
                  </p>
                )}
                <p className="text-white text-xs font-medium">
                  {formatPrice(shoe.price)}
                </p>
              </motion.div>
            ))}
            {shoes.length > 4 && (
              <motion.button
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.4 }}
                onClick={() => setShowShoesSheet(true)}
                className="flex-shrink-0 w-20 h-20 rounded-xl bg-white/5 border border-white/10 flex flex-col items-center justify-center"
              >
                <span className="text-white/60 text-lg font-medium">+{shoes.length - 4}</span>
                <span className="text-white/40 text-[10px]">更多</span>
              </motion.button>
            )}
          </div>
        </motion.div>

        <motion.div 
          className="flex items-center justify-between gap-2 mb-5 py-3 px-3 bg-white/[0.03] rounded-xl border border-white/5"
          variants={fadeInUp}
          initial="initial"
          animate="animate"
          transition={{ delay: 0.2 }}
        >
          {[
            { icon: Check, text: '正品保证' },
            { icon: Package, text: '组合优惠' },
            { icon: ShoppingCart, text: '一键购买' },
          ].map((item, index) => (
            <div 
              key={index}
              className="flex items-center gap-1.5"
            >
              <item.icon className="w-3.5 h-3.5 text-white/50" />
              <span className="text-[10px] text-white/50">{item.text}</span>
            </div>
          ))}
        </motion.div>

        <motion.div 
          className="bg-white/[0.03] rounded-xl p-4 border border-white/5"
          variants={fadeInUp}
          initial="initial"
          animate="animate"
          transition={{ delay: 0.25 }}
        >
          <h3 className="text-white text-sm font-medium mb-3 flex items-center gap-2">
            <Info className="w-4 h-4 text-white/50" />
            组合包说明
          </h3>
          <ul className="space-y-2 text-white/50 text-xs">
            <li className="flex items-start gap-2">
              <span className="w-1 h-1 rounded-full bg-white/30 mt-1.5 flex-shrink-0" />
              <span>组合包内所有鞋款将一起发货</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="w-1 h-1 rounded-full bg-white/30 mt-1.5 flex-shrink-0" />
              <span>组合包价格已包含所有优惠，不可与其他优惠券叠加</span>
            </li>
            <li className="flex items-start gap-2">
              <span className="w-1 h-1 rounded-full bg-white/30 mt-1.5 flex-shrink-0" />
              <span>支持7天无理由退换货</span>
            </li>
          </ul>
        </motion.div>
      </motion.div>

      <motion.div 
        className="fixed bottom-0 left-0 right-0 bg-neutral-950/95 backdrop-blur-xl border-t border-white/10 px-4 py-3 safe-area-bottom z-40"
        initial={{ y: 100 }}
        animate={{ y: 0 }}
        transition={{ delay: 0.4, duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="flex items-center gap-3 max-w-lg mx-auto">
          <div className="flex-1">
            <div className="flex items-baseline gap-2">
              <span className="text-xl font-bold text-white">
                {formatPrice(bundle.price)}
              </span>
            </div>
            <p className="text-white/40 text-xs">
              共 {shoes.length} 款球鞋
            </p>
          </div>
          
          <motion.button
            onClick={() => setShowSizeSheet(true)}
            className="flex items-center gap-1.5 px-3 py-2.5 bg-white/10 rounded-lg border border-white/10 min-w-[72px] justify-center"
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            <span className="text-xs text-white/80">
              {Object.keys(selectedSizes).length === shoes.length ? '已选' : '选尺码'}
            </span>
          </motion.button>
          
          <motion.div 
            whileHover={{ scale: 1.02 }} 
            whileTap={{ scale: 0.98 }}
          >
            <Button 
              className="h-11 px-8 rounded-lg bg-white text-black font-medium hover:bg-white/90 text-sm"
              onClick={handleBuyNow}
              disabled={isAddingToCart}
            >
              {isAddingToCart ? (
                <motion.div
                  className="w-4 h-4 border-2 border-black/20 border-t-black rounded-full"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                />
              ) : (
                '立即购买'
              )}
            </Button>
          </motion.div>
        </div>
      </motion.div>

      <Sheet open={showShoesSheet} onOpenChange={setShowShoesSheet}>
        <SheetContent
          side="bottom"
          className="bg-neutral-950/95 border-white/10 rounded-t-2xl px-4 max-h-[70vh]"
        >
          <SheetHeader className="pb-4 pt-2">
            <SheetTitle className="text-white text-sm font-medium">
              组合包包含 {shoes.length} 款球鞋
            </SheetTitle>
            <SheetDescription className="sr-only">查看组合包中所有鞋款的信息</SheetDescription>
          </SheetHeader>

          <div className="py-2 space-y-3 overflow-y-auto scrollbar-hide max-h-[50vh]">
            {shoes.map((shoe, index) => (
              <motion.div
                key={shoe.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                className="flex gap-3 p-3 bg-white/[0.03] rounded-xl border border-white/5"
              >
                <Link
                  to={`/shoe/${shoe.id}`}
                  className="w-20 h-20 rounded-lg overflow-hidden bg-white/5 flex-shrink-0"
                >
                  <img
                    src={shoe.image || '/placeholder-shoe.png'}
                    alt={shoe.name}
                    className="w-full h-full object-cover"
                  />
                </Link>
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between">
                    <Link to={`/shoe/${shoe.id}`} className="flex-1 min-w-0">
                      <h4 className="text-white text-sm font-medium line-clamp-1">
                        {shoe.name}
                      </h4>
                    </Link>
                    <Link
                      to={`/shoe/${shoe.id}`}
                      className="ml-2 p-1 text-white/40 hover:text-white/70 transition-colors"
                    >
                      <ChevronRight className="w-5 h-5" />
                    </Link>
                  </div>
                  <div className="flex items-center gap-2 mt-0.5">
                    {shoe.brand && (
                      <p className="text-white/40 text-xs">{shoe.brand}</p>
                    )}
                    {shoe.colorName && (
                      <>
                        <span className="text-white/20">·</span>
                        <p className="text-white/40 text-xs">{shoe.colorName}</p>
                      </>
                    )}
                  </div>
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-white font-bold">
                      {formatPrice(shoe.price)}
                    </span>
                    {shoe.sizes && shoe.sizes.length > 0 && (
                      <span className="text-white/40 text-xs">
                        {shoe.sizes.length} 个尺码可选
                      </span>
                    )}
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </SheetContent>
      </Sheet>

      <Sheet open={showSizeSheet} onOpenChange={setShowSizeSheet}>
        <SheetContent
          side="bottom"
          className="bg-neutral-950/95 border-white/10 rounded-t-2xl px-4 max-h-[40vh]"
        >
          <SheetHeader className="pb-1 pt-0.5">
            <SheetTitle className="text-white text-xs font-medium">
              选择尺码
            </SheetTitle>
            <SheetDescription className="sr-only">为组合包内每双鞋选择尺码</SheetDescription>
          </SheetHeader>

          <div className="py-1 space-y-2 overflow-y-auto scrollbar-hide">
            {shoes.map((shoe, index) => (
              <motion.div
                key={shoe.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                className="pb-3 border-b border-white/5 last:border-0"
              >
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-10 h-10 rounded-lg overflow-hidden bg-white/5 flex-shrink-0">
                    <img
                      src={shoe.image || '/placeholder-shoe.png'}
                      alt={shoe.name}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="text-white text-xs font-medium line-clamp-1">
                      {shoe.name}
                    </h4>
                    {shoe.brand && (
                      <p className="text-white/40 text-[10px] mt-0.5">{shoe.brand}</p>
                    )}
                  </div>
                  {selectedSizes[shoe.id] && (
                    <span className="text-[10px] text-white bg-white/10 px-1.5 py-0.5 rounded">
                      {selectedSizes[shoe.id]}
                    </span>
                  )}
                </div>
                
                {shoe.sizes && shoe.sizes.length > 0 ? (
                  <div className="grid grid-cols-5 gap-1">
                    {shoe.sizes.map((sizeInfo, idx) => {
                      const isLowStock = sizeInfo.stock > 0 && sizeInfo.stock < 5;
                      const isOutOfStock = sizeInfo.stock === 0;
                      const isSelected = selectedSizes[shoe.id] === sizeInfo.size;
                      
                      return (
                        <motion.button
                          key={sizeInfo.id}
                          disabled={isOutOfStock}
                          onClick={() => {
                            setSelectedSizes(prev => ({
                              ...prev,
                              [shoe.id]: sizeInfo.size
                            }));
                          }}
                          initial={{ opacity: 0, y: 16 }}
                          animate={{ opacity: 1, y: 0 }}
                          transition={{ delay: idx * 0.03 }}
                          whileHover={!isOutOfStock ? { scale: 1.03 } : {}}
                          whileTap={!isOutOfStock ? { scale: 0.97 } : {}}
                          className={`
                            relative py-1.5 px-1 rounded-md border transition-all duration-200 min-h-[28px]
                            ${isSelected
                              ? 'border-white bg-white text-black font-medium'
                              : isOutOfStock
                              ? 'border-white/5 bg-white/[0.02] cursor-not-allowed'
                              : 'border-white/10 bg-white/[0.03] hover:border-white/25 hover:bg-white/[0.06]'
                            }
                          `}
                        >
                          <span className={`text-[10px] leading-tight ${
                            isSelected
                              ? 'text-black'
                              : isLowStock
                              ? 'text-red-400'
                              : isOutOfStock
                              ? 'text-white/20'
                              : 'text-white'
                          }`}>
                            {sizeInfo.size}
                          </span>
                        </motion.button>
                      );
                    })}
                  </div>
                ) : (
                  <p className="text-white/40 text-[10px]">暂无尺码信息</p>
                )}
              </motion.div>
            ))}
          </div>

          <div className="pt-2 pb-2">
            <Button
              className="w-full h-11 rounded-lg bg-white text-black font-medium hover:bg-white/90 text-sm"
              onClick={() => setShowSizeSheet(false)}
            >
              确认选择
            </Button>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  );
}

