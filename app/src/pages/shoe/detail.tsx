import { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { motion, AnimatePresence, useScroll, useTransform } from 'framer-motion';
import { 
  ArrowLeft, 
  Share2, 
  Heart, 
  Star, 
  ChevronRight, 
  ShoppingCart, 
  Zap,
  Package,
  ShieldCheck,
  Sparkles,
  ChevronDown,
  Info,
  MoreHorizontal,
  ChevronRight as ChevronRightIcon,
  X
} from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { spuApi, commentApi, cartApi } from '@/api';
import { useCartStore, useUserStore } from '@/stores';
import type { ShoeSpuDetail, ShoeSku, Comment } from '@/types';
import { formatPrice } from '@/utils';

const FROM_COMMUNITY_DETAIL_KEY = 'from_community_detail';

// 动画配置
const fadeInUp = {
  initial: { opacity: 0, y: 16 },
  animate: { opacity: 1, y: 0 },
  transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1] }
};

const staggerContainer = {
  animate: {
    transition: {
      staggerChildren: 0.08
    }
  }
};

export default function ShoeDetailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams();
  const { isLoggedIn } = useUserStore();
  const addToCart = useCartStore((state) => state.addItem);
  const fetchCart = useCartStore((state) => state.fetchCart);
  const containerRef = useRef<HTMLDivElement>(null);
  
  // 滚动视差效果
  const { scrollY } = useScroll();
  const imageScale = useTransform(scrollY, [0, 200], [1, 1.08]);
  const imageOpacity = useTransform(scrollY, [0, 150], [1, 0.85]);

  const [shoe, setShoe] = useState<ShoeSpuDetail | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [selectedSku, setSelectedSku] = useState<ShoeSku | null>(null);
  const [selectedSize, setSelectedSize] = useState('');
  const [showSizeSheet, setShowSizeSheet] = useState(false);
  const [isAddingToCart, setIsAddingToCart] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [activeTab, setActiveTab] = useState<'details' | 'reviews'>('details');
  const [showDescriptionDialog, setShowDescriptionDialog] = useState(false);
  const [showAllComments, setShowAllComments] = useState(false);
  const [previewImage, setPreviewImage] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadShoeDetail(id);
      loadComments(id);
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

  const loadShoeDetail = async (shoeId: string) => {
    try {
      const data = await spuApi.getSpuDetail(Number(shoeId));
      setShoe(data || null);
      // 默认选中第一个SKU
      if (data?.skus && data.skus.length > 0) {
        setSelectedSku(data.skus[0]);
      }
    } catch (error) {
      console.error('Failed to load shoe detail:', error);
    }
  };

  const loadComments = async (shoeId: string) => {
    try {
      const data = await commentApi.getCommentList(Number(shoeId));
      setComments(data);
    } catch (error) {
      console.error('Failed to load comments:', error);
    }
  };

  const handleAddToCart = async () => {
    if (!isLoggedIn()) {
      toast.error('请先登录');
      return;
    }
    if (!selectedSize) {
      setShowSizeSheet(true);
      return;
    }
    if (!selectedSku) {
      return;
    }

    setIsAddingToCart(true);
    try {
      await cartApi.addToCart({
        spuId: Number(id),
        skuId: selectedSku.id,
        shoeSize: selectedSize,
        selected: 1,
      });

      addToCart({
        id: Date.now().toString(),
        shoeId: id!,
        spuId: Number(id),
        skuId: selectedSku.id,
        shoeName: shoe!.name,
        shoeImage: selectedSku.image || shoe!.defaultImage || '',
        size: selectedSize,
        price: selectedSku.price,
        quantity: 1,
        selected: true,
      });

      fetchCart();
      setShowSizeSheet(false);
      toast.success('已添加到购物车');
    } catch (error) {
      console.error('Failed to add to cart:', error);
      toast.error('添加失败，请重试');
    } finally {
      setIsAddingToCart(false);
    }
  };

  const handleBuyNow = () => {
    if (!isLoggedIn()) {
      toast.error('请先登录');
      return;
    }
    if (!selectedSize) {
      setShowSizeSheet(true);
      return;
    }
    handleAddToCart().then(() => {
      navigate('/order/confirm');
    });
  };

  if (!shoe) {
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

  const averageRating = comments.length > 0 
    ? (comments.reduce((sum, c) => sum + (c.rating || 0), 0) / comments.length).toFixed(1)
    : '0.0';

  // 计算总库存（从所有SKU的尺码中汇总）
  const totalStock = shoe.skus?.reduce((sum, sku) => {
    return sum + (sku.sizes?.reduce((sizeSum, size) => sizeSum + (size.stock || 0), 0) || 0);
  }, 0) || 0;

  // 获取当前选中SKU的尺码列表
  const currentSizes = selectedSku?.sizes || [];

  return (
    <div ref={containerRef} className="min-h-screen bg-neutral-950 text-white">
      {/* 固定顶部导航 */}
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

      {/* 主图区域 */}
      <div className="relative h-[55vh] overflow-hidden">
        <motion.div 
          className="absolute inset-0"
          style={{ scale: imageScale }}
        >
          <img
            src={selectedSku?.image || shoe.defaultImage}
            alt={shoe.name}
            className="w-full h-full object-cover brightness-110"
          />
          {/* 渐变遮罩 */}
          <div className="absolute inset-0 bg-gradient-to-t from-neutral-950 via-neutral-950/20 to-transparent" />
          <div className="absolute inset-0 bg-gradient-to-b from-neutral-950/20 via-transparent to-transparent" />
        </motion.div>

        {/* 限量标识 */}
        <AnimatePresence>
          {shoe.isLimited === 1 && (
            <motion.div 
              className="absolute top-20 left-3"
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
            >
              <div className="flex items-center gap-1.5 bg-gradient-to-r from-amber-500 to-orange-500 px-3 py-1.5 rounded-full">
                <Sparkles className="w-3 h-3 text-white" />
                <span className="text-xs font-medium text-white">限量发售</span>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* 品牌标识 */}
        <motion.div 
          className="absolute bottom-6 left-4 right-4"
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          {shoe.brand && (
            <p className="text-white/50 text-xs font-medium tracking-wider uppercase mb-1">
              {shoe.brand}
            </p>
          )}
          <h1 className="text-xl md:text-2xl font-bold text-white leading-tight">
            {shoe.name}
          </h1>
        </motion.div>
      </div>

      {/* 内容区域 */}
      <motion.div 
        className="relative -mt-4 bg-neutral-950 rounded-t-2xl px-4 pt-5 pb-28"
        initial={{ y: 80 }}
        animate={{ y: 0 }}
        transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
      >
        {/* 颜色缩略图选择器 */}
        {shoe.skus && shoe.skus.length > 1 && (
          <motion.div
            className="mb-4"
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
          >
            <div className="flex items-center gap-2">
              <div className="relative flex-1 overflow-hidden">
                <div className="flex items-center gap-2 overflow-x-auto scrollbar-hide py-1 pr-4 scroll-mask-right">
                  {shoe.skus.map((sku, index) => (
                    <motion.button
                      key={sku.id}
                      onClick={() => {
                        setSelectedSku(sku);
                        setSelectedSize('');
                      }}
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      transition={{ delay: 0.3 + index * 0.05 }}
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      className={`
                        relative flex-shrink-0 w-14 h-14 rounded-lg overflow-hidden border-2 transition-all duration-200
                        ${selectedSku?.id === sku.id
                          ? 'border-white shadow-lg shadow-white/20'
                          : 'border-white/20 hover:border-white/40 opacity-60 hover:opacity-100'
                        }
                      `}
                    >
                      <img
                        src={sku.image || shoe.defaultImage}
                        alt={sku.colorName}
                        className="w-full h-full object-cover brightness-75"
                      />
                      {selectedSku?.id === sku.id && (
                        <div className="absolute inset-0 bg-white/10" />
                      )}
                    </motion.button>
                  ))}
                </div>
              </div>

              {/* 颜色计数指示器 - 点击打开选择弹窗 */}
              <motion.button
                onClick={() => setShowSizeSheet(true)}
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                className="flex-shrink-0 flex items-center gap-1 px-2 py-1.5 bg-white/5 rounded-lg border border-white/10 hover:bg-white/10 transition-colors"
              >
                <div className="flex flex-col items-center">
                  <span className="text-[10px] text-white/50">配色</span>
                  <span className="text-xs font-medium text-white">{shoe.skus.length}</span>
                </div>
                <ChevronRightIcon className="w-3.5 h-3.5 text-white/40" />
              </motion.button>
            </div>
          </motion.div>
        )}

        {/* 价格与销量 */}
        <motion.div 
          className="flex items-end justify-between mb-4"
          variants={fadeInUp}
          initial="initial"
          animate="animate"
        >
          <div>
            <p className="text-white/40 text-xs mb-0.5">售价</p>
            <div className="flex items-baseline gap-2">
              <span className="text-2xl font-bold text-white">
                {formatPrice(selectedSku?.price || shoe.minPrice)}
              </span>
              {shoe.salesCount !== undefined && shoe.salesCount > 0 && (
                <span className="text-white/40 text-xs">
                  已售 {shoe.salesCount}
                </span>
              )}
            </div>
          </div>
          
          {/* 评分 */}
          {comments.length > 0 && (
            <div className="flex items-center gap-1 bg-white/5 px-2.5 py-1.5 rounded-full">
              <Star className="w-3 h-3 fill-amber-400 text-amber-400" />
              <span className="text-white text-sm font-medium">{averageRating}</span>
              <span className="text-white/40 text-xs">({comments.length})</span>
            </div>
          )}
        </motion.div>

        {/* 分类标签 */}
        {shoe.categoryNames && shoe.categoryNames.length > 0 && (
          <motion.div
            className="flex flex-wrap gap-1.5 mb-4"
            variants={staggerContainer}
            initial="initial"
            animate="animate"
          >
            {shoe.categoryNames.map((name, index) => (
              <motion.div
                key={name}
                variants={fadeInUp}
                custom={index}
              >
                <Badge
                  variant="secondary"
                  className="bg-white/5 text-white/60 border-white/10 hover:bg-white/10 px-2 py-0.5 text-xs"
                >
                  {name}
                </Badge>
              </motion.div>
            ))}
          </motion.div>
        )}

        {/* 服务保障 */}
        <motion.div 
          className="flex items-center justify-between gap-2 mb-5 py-3 px-3 bg-white/[0.03] rounded-xl border border-white/5"
          variants={fadeInUp}
          initial="initial"
          animate="animate"
          transition={{ delay: 0.1 }}
        >
          {[
            { icon: ShieldCheck, text: '正品保证' },
            { icon: Package, text: '极速发货' },
            { icon: Zap, text: '7天退换' },
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

        {/* 标签页切换 */}
        <div className="flex gap-5 mb-4 border-b border-white/10">
          {[
            { key: 'details', label: '商品详情' },
            { key: 'reviews', label: `评价 (${comments.length})` },
          ].map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key as 'details' | 'reviews')}
              className={`relative pb-2.5 text-xs font-medium transition-colors ${
                activeTab === tab.key ? 'text-white' : 'text-white/40'
              }`}
            >
              {tab.label}
              {activeTab === tab.key && (
                <motion.div
                  className="absolute bottom-0 left-0 right-0 h-[2px] bg-white rounded-full"
                  layoutId="activeTab"
                  transition={{ type: "spring", stiffness: 500, damping: 30 }}
                />
              )}
            </button>
          ))}
        </div>

        {/* 内容区域 */}
        <AnimatePresence mode="wait">
          {activeTab === 'details' ? (
            <motion.div
              key="details"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.2 }}
              className="space-y-4"
            >
              {/* 商品描述 */}
              {shoe.description && (
                <div>
                  <h3 className="text-white text-sm font-medium mb-2">商品介绍</h3>
                  <div 
                    className="relative max-h-24 overflow-hidden cursor-pointer"
                    onClick={() => shoe.description?.length && shoe.description.length > 60 && setShowDescriptionDialog(true)}
                  >
                    <p className="text-white/60 text-xs leading-relaxed line-clamp-3">
                      {shoe.description}
                    </p>
                    {(shoe.description?.length ?? 0) > 60 && (
                      <div className="absolute bottom-0 right-0 pl-6 bg-gradient-to-l from-neutral-950 via-neutral-950/95 to-transparent">
                        <MoreHorizontal className="w-4 h-4 text-white/50" />
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* 商品信息 */}
              <div>
                <h3 className="text-white text-sm font-medium mb-2">商品信息</h3>
                <div className="grid grid-cols-2 gap-2">
                  {[
                    { label: '品牌', value: shoe.brand || '-' },
                    { label: '型号', value: shoe.model || '-' },
                    { label: '颜色', value: selectedSku?.colorName || '-' },
                    { label: '库存', value: totalStock > 0 ? `${totalStock} 件` : '-' },
                  ].map((item, index) => (
                    <div
                      key={index}
                      className="bg-white/[0.03] rounded-lg p-2.5 border border-white/5"
                    >
                      <p className="text-white/40 text-[10px] mb-0.5">{item.label}</p>
                      <p className="text-white text-xs font-medium truncate">{item.value}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* 尺码表提示 */}
              <div className="flex items-center gap-2 text-white/40 text-xs">
                <Info className="w-3.5 h-3.5" />
                <span>尺码为标准码，建议按平时尺码选择</span>
              </div>
            </motion.div>
          ) : (
            <motion.div
              key="reviews"
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.2 }}
              className="space-y-3"
            >
              {comments.length > 0 ? (
                <>
                  {(showAllComments ? comments : comments.slice(0, 2)).map((comment, index) => (
                    <motion.div
                      key={comment.id}
                      initial={{ opacity: 0, y: 16 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.08 }}
                      className="bg-white/[0.03] rounded-xl p-3 border border-white/5"
                    >
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          {comment.userAvatar ? (
                            <img 
                              src={comment.userAvatar} 
                              alt={comment.userName}
                              className="w-7 h-7 rounded-full object-cover"
                            />
                          ) : (
                            <div className="w-7 h-7 rounded-full bg-gradient-to-br from-rose-500 to-pink-500 flex items-center justify-center text-[10px] font-bold">
                              {(comment.userName || comment.userId.toString()).slice(-2)}
                            </div>
                          )}
                          <div>
                            <p className="text-white text-xs font-medium">{comment.userName || `用户${comment.userId}`}</p>
                            <p className="text-white/40 text-[10px]">{comment.createTime}</p>
                          </div>
                        </div>
                        {comment.rating > 0 && (
                          <div className="flex items-center gap-0.5 bg-amber-500/20 px-1.5 py-0.5 rounded-full">
                            <Star className="w-2.5 h-2.5 fill-amber-400 text-amber-400" />
                            <span className="text-amber-400 text-[10px] font-medium">{comment.rating}</span>
                          </div>
                        )}
                      </div>
                      <p className="text-white/70 text-xs leading-relaxed">{comment.content}</p>
                      {comment.images && (
                        <div className="mt-2 flex gap-1.5 flex-wrap">
                          {(() => {
                            let imageList: string[] = [];
                            try {
                              if (comment.images.startsWith('[')) {
                                imageList = JSON.parse(comment.images);
                              } else if (comment.images.includes(',')) {
                                imageList = comment.images.split(',').map(s => s.trim()).filter(Boolean);
                              } else {
                                imageList = [comment.images];
                              }
                            } catch {
                              imageList = comment.images.split(',').map(s => s.trim()).filter(Boolean);
                            }
                            return imageList.map((img, imgIndex) => (
                              <img
                                key={imgIndex}
                                src={img}
                                alt={`评价图片${imgIndex + 1}`}
                                className="w-16 h-16 rounded-lg object-cover border border-white/10 cursor-pointer hover:opacity-80 transition-opacity"
                                onClick={() => setPreviewImage(img)}
                              />
                            ));
                          })()}
                        </div>
                      )}
                    </motion.div>
                  ))}
                  
                  {comments.length > 2 && (
                    <Button 
                      variant="ghost" 
                      className="w-full text-white/50 hover:text-white hover:bg-white/5 text-xs h-9"
                      onClick={() => setShowAllComments(!showAllComments)}
                    >
                      {showAllComments ? '收起评价' : `查看全部评价 (${comments.length})`}
                      <ChevronRight className={`w-3.5 h-3.5 ml-0.5 transition-transform ${showAllComments ? 'rotate-90' : ''}`} />
                    </Button>
                  )}
                </>
              ) : (
                <div className="text-center py-8">
                  <div className="w-12 h-12 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-3">
                    <Star className="w-5 h-5 text-white/20" />
                  </div>
                  <p className="text-white/40 text-xs">暂无评价</p>
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>

      {/* 底部操作栏 */}
      <motion.div 
        className="fixed bottom-0 left-0 right-0 bg-neutral-950/95 backdrop-blur-xl border-t border-white/10 px-3 py-1.5 safe-area-bottom z-40"
        initial={{ y: 100 }}
        animate={{ y: 0 }}
        transition={{ delay: 0.4, duration: 0.4, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="flex gap-1.5 max-w-sm mx-auto mb-1">
          {/* 购物车按钮 */}
          <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
            <Button 
              variant="outline" 
              size="icon" 
              className="w-9 h-9 rounded-lg bg-white/5 border-white/10 hover:bg-white/10"
              onClick={handleAddToCart}
              disabled={isAddingToCart}
            >
              {isAddingToCart ? (
                <motion.div
                  className="w-3.5 h-3.5 border-2 border-white/20 border-t-white/60 rounded-full"
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                />
              ) : (
                <ShoppingCart className="w-3.5 h-3.5 text-white/60" />
              )}
            </Button>
          </motion.div>
          
          {/* 颜色选择按钮（如果有多于一个SKU） */}
          {shoe.skus && shoe.skus.length > 1 && (
            <motion.button
              onClick={() => setShowSizeSheet(true)}
              className="flex-1 bg-white/5 border border-white/10 rounded-lg flex items-center justify-between px-2 hover:bg-white/10 transition-colors"
              whileHover={{ scale: 1.01 }}
              whileTap={{ scale: 0.99 }}
            >
              <span className="text-white/50 text-[9px]">颜色</span>
              <div className="flex items-center gap-0.5">
                <span className="text-[9px] font-medium text-white">
                  {selectedSku?.colorName || '请选择'}
                </span>
                <ChevronRight className="w-3 h-3 text-white/40" />
              </div>
            </motion.button>
          )}

          {/* 尺码选择按钮 */}
          <motion.button
            onClick={() => setShowSizeSheet(true)}
            className="flex-1 bg-white/5 border border-white/10 rounded-lg flex items-center justify-between px-2 hover:bg-white/10 transition-colors"
            whileHover={{ scale: 1.01 }}
            whileTap={{ scale: 0.99 }}
          >
            <span className="text-white/50 text-[9px]">尺码</span>
            <div className="flex items-center gap-0.5">
              <span className={`text-[9px] font-medium ${selectedSize ? 'text-white' : 'text-white/40'}`}>
                {selectedSize || '请选择'}
              </span>
              <ChevronRight className="w-3 h-3 text-white/40" />
            </div>
          </motion.button>
          
          {/* 购买按钮 */}
          <motion.div 
            className="flex-[1.5]"
            whileHover={{ scale: 1.02 }} 
            whileTap={{ scale: 0.98 }}
          >
            <Button 
              className="w-full h-9 rounded-lg bg-white text-black font-medium hover:bg-white/90 text-[10px]"
              onClick={handleBuyNow}
              disabled={isAddingToCart}
            >
              {isAddingToCart ? (
                <motion.div
                  className="w-3 h-3 border-2 border-black/20 border-t-black rounded-full"
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

      {/* 尺码选择弹窗 */}
      <Sheet open={showSizeSheet} onOpenChange={setShowSizeSheet}>
        <SheetContent
          side="bottom"
          className="bg-neutral-950/95 border-white/10 rounded-t-2xl px-4 max-h-[40vh]"
        >
          <SheetHeader className="pb-1 pt-0.5">
            <SheetTitle className="text-white text-xs font-medium">
              {shoe.skus && shoe.skus.length > 1 ? '选择颜色和尺码' : '选择尺码'}
            </SheetTitle>
            <SheetDescription className="sr-only">选择商品颜色与尺码</SheetDescription>
          </SheetHeader>

          <div className="py-1 space-y-2 overflow-y-auto scrollbar-hide">
            {/* SKU颜色选择（如果有多于一个SKU） */}
            {shoe.skus && shoe.skus.length > 1 && (
              <div>
                <p className="text-white/60 text-[10px] mb-1">颜色</p>
                <div className="grid grid-cols-5 gap-1">
                  {shoe.skus.map((sku, index) => (
                    <motion.button
                      key={sku.id}
                      onClick={() => {
                        setSelectedSku(sku);
                        setSelectedSize(''); // 重置尺码选择
                      }}
                      initial={{ opacity: 0, y: 16 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.03 }}
                      whileHover={{ scale: 1.03 }}
                      whileTap={{ scale: 0.97 }}
                      className={`
                        relative py-1.5 px-1 rounded-md border transition-all duration-200 min-h-[28px]
                        ${selectedSku?.id === sku.id
                          ? 'border-white bg-white text-black font-medium'
                          : 'border-white/10 bg-white/[0.03] text-white hover:border-white/25 hover:bg-white/[0.06]'
                        }
                      `}
                    >
                      <span className="text-[10px] leading-tight">{sku.colorName}</span>
                    </motion.button>
                  ))}
                </div>
              </div>
            )}

            {/* 尺码选择 */}
            <div>
              <p className="text-white/60 text-[10px] mb-1">尺码</p>
              <div className="grid grid-cols-5 gap-1">
                {currentSizes.map((sizeInfo, index) => {
                  const isLowStock = sizeInfo.stock > 0 && sizeInfo.stock < 5;
                  const isOutOfStock = sizeInfo.stock === 0;
                  
                  return (
                    <motion.button
                      key={sizeInfo.size}
                      disabled={isOutOfStock}
                      onClick={() => {
                        setSelectedSize(sizeInfo.size);
                        setShowSizeSheet(false);
                      }}
                      initial={{ opacity: 0, y: 16 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.03 }}
                      whileHover={!isOutOfStock ? { scale: 1.03 } : {}}
                      whileTap={!isOutOfStock ? { scale: 0.97 } : {}}
                      className={`
                        relative py-1.5 px-1 rounded-md border transition-all duration-200 min-h-[28px]
                        ${selectedSize === sizeInfo.size
                          ? 'border-white bg-white text-black font-medium'
                          : isOutOfStock
                          ? 'border-white/5 bg-white/[0.02] cursor-not-allowed'
                          : 'border-white/10 bg-white/[0.03] hover:border-white/25 hover:bg-white/[0.06]'
                        }
                      `}
                    >
                      <span className={`text-[10px] leading-tight ${
                        selectedSize === sizeInfo.size
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
            </div>

            {currentSizes.length === 0 && (
              <p className="text-center text-white/40 text-xs py-3">
                请先选择颜色
              </p>
            )}

            {/* 尺码提示 */}
            <div className="mt-2 flex items-center justify-center gap-4 text-[9px] text-white/40">
              <div className="flex items-center gap-1">
                <div className="w-1.5 h-1.5 rounded-full bg-white/20" />
                <span>有货</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-1.5 h-1.5 rounded-full bg-red-500" />
                <span>紧张</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-1.5 h-1.5 rounded-full bg-white/[0.03] border border-white/10" />
                <span>缺货</span>
              </div>
            </div>
          </div>
        </SheetContent>
      </Sheet>

      {/* 商品介绍弹窗 */}
      <Dialog open={showDescriptionDialog} onOpenChange={setShowDescriptionDialog}>
        <DialogContent className="bg-neutral-900 border-white/10 text-white max-w-[90vw] rounded-xl">
          <DialogHeader>
            <DialogTitle className="text-white text-base">商品介绍</DialogTitle>
            <DialogDescription className="sr-only">查看完整商品介绍信息</DialogDescription>
          </DialogHeader>
          <div className="max-h-[50vh] overflow-y-auto py-2">
            <p className="text-white/70 text-sm leading-relaxed whitespace-pre-wrap">
              {shoe?.description}
            </p>
          </div>
        </DialogContent>
      </Dialog>

      {/* 图片预览弹窗 */}
      <Dialog open={!!previewImage} onOpenChange={() => setPreviewImage(null)}>
        <DialogContent className="bg-neutral-900 border-white/10 text-white max-w-[90vw] rounded-xl p-0 overflow-hidden">
          <DialogDescription className="sr-only">查看用户评价图片的大图预览</DialogDescription>
          <div className="relative">
            <img
              src={previewImage || ''}
              alt="评价图片预览"
              className="w-full h-auto max-h-[70vh] object-contain"
            />
            <button
              onClick={() => setPreviewImage(null)}
              className="absolute top-2 right-2 w-8 h-8 bg-black/50 rounded-full flex items-center justify-center text-white/80 hover:text-white hover:bg-black/70 transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

