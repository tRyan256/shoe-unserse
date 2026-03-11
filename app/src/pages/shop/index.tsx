import { useState, useEffect, useMemo, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, SlidersHorizontal, ShoppingCart, Ticket, MapPin, Gift, X, RotateCcw, ChevronDown } from 'lucide-react';
import { toast } from 'sonner';
import { Badge } from '@/components/ui/badge';
import { ScrollToTop, ActivityCarousel } from '@/components/shared';
import { shoeApi, categoryApi } from '@/api';
import { useScrollRestoration } from '@/hooks/use-scroll-restoration';
import { useUserStore, useCartStore } from '@/stores';
import type { ShoeSpu, Category } from '@/types';
import { formatPrice } from '@/utils';

const SEARCH_STATE_KEY = 'shop_search_state';

interface SearchState {
  searchMode: boolean;
  searchKeyword: string;
  selectedBrandIds: number[];
  selectedStyleIds: number[];
  selectedSortCode: string;
}

function getSearchState(): SearchState | null {
  try {
    const stored = sessionStorage.getItem(SEARCH_STATE_KEY);
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null;
  }
}

function saveSearchState(state: SearchState) {
  try {
    sessionStorage.setItem(SEARCH_STATE_KEY, JSON.stringify(state));
  } catch (e) {
    console.error('Failed to save search state:', e);
  }
}

function clearSearchState() {
  try {
    sessionStorage.removeItem(SEARCH_STATE_KEY);
  } catch (e) {
    console.error('Failed to clear search state:', e);
  }
}

const quickEntries = [
  { icon: ShoppingCart, label: '购物车', path: '/cart', color: '#FF6B6B' },
  { icon: Ticket, label: '优惠券', path: '/coupon/my', color: '#2f855a' },
  { icon: MapPin, label: '全国门店', path: '/outlet/nearby', color: '#45B7D1' },
  { icon: Gift, label: '组合包', path: '/bundle/list', color: '#96CEB4' },
];

const sortOptions = [
  { code: 'release_date_desc', label: '最新发布' },
  { code: 'release_date_asc', label: '最早发布' },
  { code: 'price_desc', label: '价格从高到低' },
  { code: 'price_asc', label: '价格从低到高' },
  { code: 'sales_desc', label: '销量最高' },
];

export default function ShopPage() {
  const navigate = useNavigate();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const cartCount = useCartStore((state) => state.getTotalCount());
  
  const savedState = useMemo(() => getSearchState(), []);
  
  const [shoes, setShoes] = useState<ShoeSpu[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [showFilter, setShowFilter] = useState(false);
  const [selectedBrandIds, setSelectedBrandIds] = useState<number[]>(savedState?.selectedBrandIds || []);
  const [selectedStyleIds, setSelectedStyleIds] = useState<number[]>(savedState?.selectedStyleIds || []);
  const [tempBrandIds, setTempBrandIds] = useState<number[]>([]);
  const [tempStyleIds, setTempStyleIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(!savedState?.searchMode);
  const [selectedSort, setSelectedSort] = useState(() => {
    if (savedState?.selectedSortCode) {
      return sortOptions.find(o => o.code === savedState.selectedSortCode) || sortOptions[0];
    }
    return sortOptions[0];
  });
  const [showSortMenu, setShowSortMenu] = useState(false);
  const [searchMode, setSearchMode] = useState(savedState?.searchMode || false);
  const [searchKeyword, setSearchKeyword] = useState(savedState?.searchKeyword || '');
  const [searching, setSearching] = useState(false);

  const dataLoaded = !loading && shoes.length > 0;
  useScrollRestoration(true, dataLoaded);

  const brandCategories = useMemo(() => 
    categories.filter(c => c.type === 1), [categories]);
  
  const styleCategories = useMemo(() => 
    categories.filter(c => c.type === 2), [categories]);

  const persistSearchState = useCallback(() => {
    if (searchMode || selectedBrandIds.length > 0 || selectedStyleIds.length > 0 || selectedSort.code !== sortOptions[0].code) {
      saveSearchState({
        searchMode,
        searchKeyword,
        selectedBrandIds,
        selectedStyleIds,
        selectedSortCode: selectedSort.code,
      });
    } else {
      clearSearchState();
    }
  }, [searchMode, searchKeyword, selectedBrandIds, selectedStyleIds, selectedSort]);

  useEffect(() => {
    persistSearchState();
  }, [persistSearchState]);

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    if (searchMode) {
      handleSearch();
    } else {
      loadShoes();
    }
  }, [selectedBrandIds, selectedStyleIds, selectedSort]);

  const loadCategories = async () => {
    try {
      const categoriesData = await categoryApi.getCategoryList();
      setCategories(categoriesData || []);
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const loadShoes = async () => {
    setLoading(true);
    try {
      const allCategoryIds = [...selectedBrandIds, ...selectedStyleIds];
      const shoesData = await shoeApi.getShoeList(
        allCategoryIds.length > 0 ? allCategoryIds : undefined,
        selectedSort.code
      );
      setShoes(shoesData || []);
    } catch (error) {
      console.error('Failed to load shop data:', error);
      toast.error('加载数据失败，请稍后重试');
      setShoes([]);
    } finally {
      setLoading(false);
    }
  };

  const openFilter = () => {
    setTempBrandIds([...selectedBrandIds]);
    setTempStyleIds([...selectedStyleIds]);
    setShowFilter(true);
  };

  const closeFilter = () => {
    setShowFilter(false);
  };

  const toggleBrand = (id: number) => {
    setTempBrandIds(prev => 
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const toggleStyle = (id: number) => {
    setTempStyleIds(prev => 
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const resetFilter = () => {
    setTempBrandIds([]);
    setTempStyleIds([]);
  };

  const applyFilter = () => {
    setSelectedBrandIds([...tempBrandIds]);
    setSelectedStyleIds([...tempStyleIds]);
    setShowFilter(false);
  };

  const hasActiveFilter = selectedBrandIds.length > 0 || selectedStyleIds.length > 0;
  const filterCount = selectedBrandIds.length + selectedStyleIds.length;

  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      return;
    }
    setSearching(true);
    setSearchMode(true);
    try {
      const results = await shoeApi.searchShoes(searchKeyword.trim());
      setShoes(results || []);
    } catch (error) {
      console.error('Search failed:', error);
      toast.error('搜索失败，请稍后重试');
      setShoes([]);
    } finally {
      setSearching(false);
    }
  };

  const handleClearSearch = () => {
    setSearchMode(false);
    setSearchKeyword('');
    clearSearchState();
    loadShoes();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 pb-20">
      <header className="sticky top-0 z-40 bg-white shadow-sm">
        <div className="flex items-center gap-3 px-4 h-12">
          <div 
            className="flex items-center flex-1 min-w-0 bg-gray-100 px-3 py-1 cursor-pointer"
            onClick={() => {
              const input = document.getElementById('search-input');
              input?.focus();
            }}
          >
            <input
              id="search-input"
              type="text"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="搜索商品"
              className="flex-1 min-w-0 bg-transparent text-sm outline-none placeholder-gray-400 truncate"
            />
            {searchMode && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleClearSearch();
                }}
                className="flex-shrink-0 p-0.5 text-gray-400 hover:text-gray-600"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
            <button
              onClick={(e) => {
                e.stopPropagation();
                if (searchKeyword.trim()) {
                  handleSearch();
                }
              }}
              disabled={!searchKeyword.trim() || searching}
              className={`flex-shrink-0 p-1 transition-colors ${
                searchKeyword.trim() 
                  ? 'text-emerald-600 hover:text-emerald-700' 
                  : 'text-gray-400'
              }`}
            >
              <Search className="w-4 h-4" />
            </button>
          </div>
          <button 
            onClick={openFilter}
            className={`flex items-center gap-1.5 px-3 py-1.5 transition-colors ${
              hasActiveFilter 
                ? 'text-emerald-600' 
                : 'text-gray-600 hover:text-gray-800'
            }`}
          >
            <SlidersHorizontal className="w-4 h-4" strokeWidth={2} />
            <span className="text-sm font-medium">
              {hasActiveFilter ? `分类(${filterCount})` : '分类'}
            </span>
          </button>
        </div>
      </header>

      {/* 活动轮播图 */}
      <div className="mx-4 mt-3 rounded-xl overflow-hidden shadow">
        <ActivityCarousel />
      </div>

      <div className="grid grid-cols-4 gap-2 p-4">
        {quickEntries.map((entry, index) => {
          const IconComponent = entry.icon;
          const handleEntryClick = (e: React.MouseEvent) => {
            if ((entry.path === '/cart' || entry.path === '/coupon/my' || entry.path === '/outlet/nearby') && !isLoggedIn()) {
              e.preventDefault();
              toast.error('请先登录');
            }
          };
          const showBadge = entry.path === '/cart' && cartCount > 0;
          return (
            <motion.div
              key={entry.path}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
            >
              <Link to={entry.path} className="flex flex-col items-center group" onClick={handleEntryClick}>
                <div className="w-10 h-10 flex items-center justify-center relative">
                  <IconComponent 
                    className="w-6 h-6 transition-all duration-300 group-hover:scale-110" 
                    style={{ color: entry.color }}
                    strokeWidth={1.5} 
                  />
                  {showBadge && (
                    <span className="absolute -top-0.5 -right-0.5 flex items-center justify-center min-w-[14px] h-3.5 text-[8px] font-medium text-white bg-red-500 rounded-full px-0.5">
                      {cartCount > 99 ? '99+' : cartCount}
                    </span>
                  )}
                </div>
                <span className="text-[11px] text-gray-600 -mt-1">{entry.label}</span>
              </Link>
            </motion.div>
          );
        })}
      </div>

      <div className="p-4">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-bold text-lg flex items-center gap-2">
            <span className="w-1 h-5 bg-emerald-600 rounded-full" />
            {searchMode ? (
              <span className="text-sm text-gray-600 font-normal">
                搜索"{searchKeyword}"的结果
                <span className="text-xs text-gray-400 ml-1">
                  共{shoes.length}件商品
                </span>
              </span>
            ) : (
              <span className="text-xs text-gray-400 font-normal">
                共{shoes.length}件商品
              </span>
            )}
          </h3>
          {!searchMode && (
            <div className="relative">
              <button
                onClick={() => setShowSortMenu(!showSortMenu)}
                className="flex items-center gap-1 px-3 py-1.5 text-sm text-gray-600 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors"
              >
                <span>{selectedSort.label}</span>
                <ChevronDown className={`w-4 h-4 transition-transform ${showSortMenu ? 'rotate-180' : ''}`} />
              </button>
              
              {showSortMenu && (
                <div className="absolute top-full right-0 mt-1 bg-white rounded-lg shadow-lg border z-10 min-w-[140px]">
                  {sortOptions.map((option) => (
                    <button
                      key={option.code}
                      onClick={() => {
                        setSelectedSort(option);
                        setShowSortMenu(false);
                      }}
                      className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-50 ${
                        selectedSort.code === option.code ? 'text-emerald-700 font-medium' : 'text-gray-600'
                      }`}
                    >
                      {option.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {loading || searching ? (
          <div className="flex items-center justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-emerald-600"></div>
          </div>
        ) : shoes.length === 0 ? (
          <div className="text-center py-12 text-gray-400">
            {searchMode ? `未找到"${searchKeyword}"相关的商品` : '暂无符合条件的商品'}
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {shoes.map((shoe, index) => (
              <motion.div
                key={shoe.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <Link to={`/shoe/${shoe.id}`}>
                  <div className="bg-white rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow duration-300">
                    <div className="relative aspect-[4/3] bg-gray-100/30">
                      <img
                        src={shoe.defaultImage || '/placeholder-shoe.png'}
                        alt={shoe.name}
                        className="w-full h-full object-cover"
                      />
                      {shoe.isLimited === 1 && (
                        <Badge className="absolute top-2 left-2 bg-red-500 text-white text-[10px] border-0">
                          限量
                        </Badge>
                      )}
                    </div>

                    <div className="p-2">
                      <h4 className="text-xs font-medium line-clamp-1 text-gray-800">
                        {shoe.name}
                      </h4>
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-bold text-red-500">
                          {formatPrice(shoe.minPrice)}
                        </span>
                        {shoe.salesCount !== undefined && (
                          <span className="text-[10px] text-gray-400">
                            已购买 {shoe.salesCount}
                          </span>
                        )}
                      </div>
                      <div className="flex items-center gap-2 flex-wrap">
                        {shoe.brand && (
                          <span className="text-[10px] text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded">
                            {shoe.brand}
                          </span>
                        )}
                        {shoe.totalStock !== undefined && (
                          <span className="text-[10px] text-gray-400">
                            库存 {shoe.totalStock}
                          </span>
                        )}
                        {shoe.releaseDate && (
                          <span className="text-[10px] text-gray-400">
                            {shoe.releaseDate}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                </Link>
              </motion.div>
            ))}
          </div>
        )}
      </div>

      <ScrollToTop />

      <AnimatePresence>
        {showFilter && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/50 z-50"
              onClick={closeFilter}
            />
            <motion.div
              initial={{ y: '-100%' }}
              animate={{ y: 0 }}
              exit={{ y: '-100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 300 }}
              className="fixed left-0 right-0 top-0 bg-white z-50 shadow-xl overflow-y-auto max-h-[40vh] rounded-b-2xl"
            >
              <div className="p-3">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-base font-bold">筛选条件</h3>
                  <button onClick={closeFilter} className="p-1">
                    <X className="w-4 h-4 text-gray-400" />
                  </button>
                </div>

                {brandCategories.length > 0 && (
                  <div className="mb-4">
                    <h4 className="text-xs font-medium text-gray-700 mb-2">品牌</h4>
                    <div className="flex flex-wrap gap-1.5">
                      {brandCategories.map(cat => (
                        <button
                          key={cat.id}
                          onClick={() => toggleBrand(cat.id)}
                          className={`px-2.5 py-1 rounded-full text-xs transition-colors ${
                            tempBrandIds.includes(cat.id)
                              ? 'bg-emerald-600 text-white'
                              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                          }`}
                        >
                          {cat.name}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {styleCategories.length > 0 && (
                  <div className="mb-4">
                    <h4 className="text-xs font-medium text-gray-700 mb-2">风格</h4>
                    <div className="flex flex-wrap gap-1.5">
                      {styleCategories.map(cat => (
                        <button
                          key={cat.id}
                          onClick={() => toggleStyle(cat.id)}
                          className={`px-2.5 py-1 rounded-full text-xs transition-colors ${
                            tempStyleIds.includes(cat.id)
                              ? 'bg-emerald-600 text-white'
                              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                          }`}
                        >
                          {cat.name}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                <div className="flex gap-2 mt-4">
                  <button
                    onClick={resetFilter}
                    className="flex-1 flex items-center justify-center gap-1 py-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50 text-xs"
                  >
                    <RotateCcw className="w-3 h-3" />
                    <span>重置</span>
                  </button>
                  <button
                    onClick={applyFilter}
                    className="flex-1 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 text-xs"
                  >
                    确定
                  </button>
                </div>
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}


