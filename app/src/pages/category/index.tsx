import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, ChevronDown } from 'lucide-react';
import { categoryApi, shoeApi } from '@/api';
import { useScrollRestoration } from '@/hooks/use-scroll-restoration';
import type { Category, ShoeSpu } from '@/types';
import { ProductCard, ScrollToTop } from '@/components/shared';

type SortOption = {
  code: string;
  label: string;
};

const sortOptions: SortOption[] = [
  { code: 'release_date_desc', label: '最新发布' },
  { code: 'release_date_asc', label: '最早发布' },
  { code: 'price_desc', label: '价格从高到低' },
  { code: 'price_asc', label: '价格从低到高' },
  { code: 'sales_desc', label: '销量最高' },
];

export default function CategoryPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number>(0);
  const [shoes, setShoes] = useState<ShoeSpu[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedSort, setSelectedSort] = useState<SortOption>(sortOptions[0]);
  const [showSortMenu, setShowSortMenu] = useState(false);

  const dataLoaded = !isLoading && shoes.length > 0;
  useScrollRestoration(true, dataLoaded);

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    if (selectedCategory > 0) {
      loadShoesByCategory(selectedCategory, selectedSort.code);
    }
  }, [selectedCategory, selectedSort]);

  const loadCategories = async () => {
    try {
      setIsLoading(true);
      const data = await categoryApi.getCategoryList();
      setCategories(data);
      if (data.length > 0) {
        setSelectedCategory(data[0].id);
      }
    } catch (error) {
      console.error('Failed to load categories:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const loadShoesByCategory = async (categoryId: number, sort?: string) => {
    try {
      const data = await shoeApi.getShoeList([categoryId], sort);
      setShoes(data);
    } catch (error) {
      console.error('Failed to load shoes:', error);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-black border-t-transparent rounded-full" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col pb-20">
      {/* 头部搜索 */}
      <header className="flex items-center gap-3 px-4 h-14 bg-white">
        <Link to="/search" className="flex-1">
          <div className="flex items-center gap-2 bg-gray-100 rounded-full px-4 py-2">
            <Search className="w-4 h-4 text-gray-400" />
            <span className="text-sm text-gray-400">搜索商品</span>
          </div>
        </Link>
      </header>

      {/* 排序栏 */}
      <div className="bg-white border-b px-4 py-2">
        <div className="relative">
          <button
            onClick={() => setShowSortMenu(!showSortMenu)}
            className="flex items-center gap-1 text-sm text-gray-600"
          >
            <span>排序: {selectedSort.label}</span>
            <ChevronDown className={`w-4 h-4 transition-transform ${showSortMenu ? 'rotate-180' : ''}`} />
          </button>
          
          {showSortMenu && (
            <div className="absolute top-full left-0 mt-1 bg-white rounded-lg shadow-lg border z-10 min-w-[140px]">
              {sortOptions.map((option) => (
                <button
                  key={option.code}
                  onClick={() => {
                    setSelectedSort(option);
                    setShowSortMenu(false);
                  }}
                  className={`w-full text-left px-4 py-2 text-sm hover:bg-gray-50 ${
                    selectedSort.code === option.code ? 'text-black font-medium' : 'text-gray-600'
                  }`}
                >
                  {option.label}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* 商品列表 */}
        <div className="flex-1 overflow-y-auto p-3">
          {categories.find((c) => c.id === selectedCategory)?.image && (
            <div className="mb-4 rounded-xl overflow-hidden">
              <img
                src={categories.find((c) => c.id === selectedCategory)?.image}
                alt="category"
                className="w-full h-32 object-cover"
              />
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            {shoes.map((shoe, index) => (
              <ProductCard key={shoe.id} shoe={shoe} index={index} />
            ))}
          </div>

          {shoes.length === 0 && (
            <div className="text-center py-10">
              <p className="text-gray-500">该分类下暂无商品</p>
            </div>
          )}
        </div>

        {/* 回到顶部 */}
        <ScrollToTop />
    </div>
  );
}
