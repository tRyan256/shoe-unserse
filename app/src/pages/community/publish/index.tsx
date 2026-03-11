import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Camera, X, Search, Loader2, AlertCircle } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { experienceApi, shoeApi, commonApi } from '@/api';
import { useUserStore } from '@/stores';
import { toast } from 'sonner';
import type { ShoeSpu } from '@/types';

interface ImageItem {
  id: string;
  localUrl: string;
  remoteUrl?: string;
  status: 'uploading' | 'success' | 'error';
  error?: string;
}

export default function CommunityPublishPage() {
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [content, setContent] = useState('');
  const [images, setImages] = useState<ImageItem[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<ShoeSpu | null>(null);
  const [showProductSearch, setShowProductSearch] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState<ShoeSpu[]>([]);
  const [searching, setSearching] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleImageSelect = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const remainingSlots = 9 - images.length;
    const filesToProcess = Array.from(files).slice(0, remainingSlots);

    for (const file of filesToProcess) {
      const id = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      const localUrl = URL.createObjectURL(file);

      const newImage: ImageItem = {
        id,
        localUrl,
        status: 'uploading',
      };

      setImages((prev) => [...prev, newImage]);

      try {
        const remoteUrl = await commonApi.uploadImage(file);
        setImages((prev) =>
          prev.map((img) =>
            img.id === id
              ? { ...img, remoteUrl, status: 'success' }
              : img
          )
        );
      } catch (err) {
        setImages((prev) =>
          prev.map((img) =>
            img.id === id
              ? { ...img, status: 'error', error: '上传失败' }
              : img
          )
        );
      }
    }

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = (id: string) => {
    setImages((prev) => {
      const img = prev.find((i) => i.id === id);
      if (img?.localUrl) {
        URL.revokeObjectURL(img.localUrl);
      }
      return prev.filter((i) => i.id !== id);
    });
  };

  const handleRetryUpload = async (id: string) => {
    const img = images.find((i) => i.id === id);
    if (!img) return;

    setImages((prev) =>
      prev.map((i) =>
        i.id === id ? { ...i, status: 'uploading', error: undefined } : i
      )
    );

    try {
      const response = await fetch(img.localUrl);
      const blob = await response.blob();
      const file = new File([blob], 'image.jpg', { type: blob.type });
      const remoteUrl = await commonApi.uploadImage(file);
      setImages((prev) =>
        prev.map((i) =>
          i.id === id ? { ...i, remoteUrl, status: 'success' } : i
        )
      );
    } catch {
      setImages((prev) =>
        prev.map((i) =>
          i.id === id ? { ...i, status: 'error', error: '上传失败' } : i
        )
      );
    }
  };

  const handleSearchProduct = async () => {
    if (!searchKeyword.trim()) {
      setSearchResults([]);
      return;
    }

    setSearching(true);
    try {
      const results = await shoeApi.searchShoes(searchKeyword);
      setSearchResults(results);
    } catch {
      toast.error('搜索失败');
    } finally {
      setSearching(false);
    }
  };

  const handleSelectProduct = (product: ShoeSpu) => {
    setSelectedProduct(product);
    setShowProductSearch(false);
    setSearchKeyword('');
    setSearchResults([]);
  };

  const handleRemoveProduct = () => {
    setSelectedProduct(null);
  };

  const handleSubmit = async () => {
    if (!content.trim()) {
      toast.info('请输入心得内容');
      return;
    }

    if (!selectedProduct) {
      toast.info('请选择关联商品');
      return;
    }

    const uploadingImages = images.filter((img) => img.status === 'uploading');
    if (uploadingImages.length > 0) {
      toast.info('请等待图片上传完成');
      return;
    }

    const errorImages = images.filter((img) => img.status === 'error');
    if (errorImages.length > 0) {
      toast.info('有图片上传失败，请重试或删除');
      return;
    }

    const remoteUrls = images
      .filter((img) => img.status === 'success' && img.remoteUrl)
      .map((img) => img.remoteUrl!);

    setSubmitting(true);
    try {
      await experienceApi.createPost({
        content: content.trim(),
        productType: 1,
        productId: selectedProduct.id,
        images: remoteUrls.length > 0 ? remoteUrls : undefined,
      });
      toast.success('发布成功');
      navigate('/community');
    } catch {
      toast.error('发布失败，请重试');
    } finally {
      setSubmitting(false);
    }
  };

  const hasUploading = images.some((img) => img.status === 'uploading');
  const hasError = images.some((img) => img.status === 'error');
  const canSubmit = content.trim().length > 0 && selectedProduct && !submitting && !hasUploading && !hasError;

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="sticky top-0 z-40 bg-white border-b border-gray-100">
        <div className="flex items-center justify-between h-12 px-4">
          <button onClick={() => navigate(-1)} className="p-1.5 -ml-1.5">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <span className="text-sm font-medium">发布心得</span>
          <button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className={`px-3 py-1 text-xs rounded-full ${
              canSubmit
                ? 'bg-emerald-600 text-white'
                : 'bg-gray-100 text-gray-400'
            }`}
          >
            {submitting ? '发布中...' : '发布'}
          </button>
        </div>
      </header>

      <div className="bg-white p-3">
        <div className="flex items-center gap-2 mb-3">
          <Avatar className="w-8 h-8">
            <AvatarImage src={user?.avatar} />
            <AvatarFallback className="text-xs">{user?.nickname?.[0]}</AvatarFallback>
          </Avatar>
          <span className="text-xs font-medium text-gray-800">{user?.nickname}</span>
        </div>

        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="分享你的穿搭心得、开箱体验..."
          className="w-full h-32 text-sm outline-none resize-none placeholder:text-gray-400"
          maxLength={500}
        />

        <div className="flex justify-end text-[10px] text-gray-400">
          {content.length}/500
        </div>
      </div>

      <div className="bg-white mt-1.5 p-3">
        <p className="text-xs font-medium text-gray-800 mb-2">添加图片</p>
        <div className="grid grid-cols-3 gap-1.5">
          {images.map((img) => (
            <div key={img.id} className="relative aspect-square">
              <img
                src={img.localUrl}
                alt=""
                className="w-full h-full object-cover rounded-lg"
              />
              {img.status === 'uploading' && (
                <div className="absolute inset-0 bg-black/50 rounded-lg flex items-center justify-center">
                  <Loader2 className="w-5 h-5 text-white animate-spin" />
                </div>
              )}
              {img.status === 'error' && (
                <div
                  className="absolute inset-0 bg-black/50 rounded-lg flex flex-col items-center justify-center cursor-pointer"
                  onClick={() => handleRetryUpload(img.id)}
                >
                  <AlertCircle className="w-5 h-5 text-red-400" />
                  <span className="text-[10px] text-white mt-0.5">点击重试</span>
                </div>
              )}
              <button
                onClick={() => handleRemoveImage(img.id)}
                className="absolute -top-1 -right-1 w-4 h-4 bg-black/60 rounded-full flex items-center justify-center"
              >
                <X className="w-2.5 h-2.5 text-white" />
              </button>
            </div>
          ))}
          {images.length < 9 && (
            <button
              onClick={handleImageSelect}
              className="aspect-square bg-gray-100 rounded-lg flex flex-col items-center justify-center gap-0.5"
            >
              <Camera className="w-5 h-5 text-gray-400" />
              <span className="text-[10px] text-gray-400">{images.length}/9</span>
            </button>
          )}
        </div>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          multiple
          onChange={handleFileChange}
          className="hidden"
        />
      </div>

      <div className="bg-white mt-1.5 p-3">
        <div className="flex items-center justify-between mb-2">
          <p className="text-xs font-medium text-gray-800">关联商品</p>
          {!selectedProduct && (
            <button
              onClick={() => setShowProductSearch(true)}
              className="flex items-center gap-0.5 text-xs text-emerald-600"
            >
              <Search className="w-3 h-3" />
              搜索商品
            </button>
          )}
        </div>

        {selectedProduct ? (
          <div className="flex items-center gap-2 bg-gray-50 rounded-xl p-2">
            <img
              src={selectedProduct.defaultImage || '/placeholder.png'}
              alt={selectedProduct.name}
              className="w-12 h-12 rounded-lg object-cover"
            />
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium text-gray-800 line-clamp-2">{selectedProduct.name}</p>
              <p className="text-[10px] text-gray-400 mt-0.5">
                ¥{selectedProduct.minPrice}
                {selectedProduct.maxPrice > selectedProduct.minPrice && ` - ¥${selectedProduct.maxPrice}`}
              </p>
            </div>
            <button
              onClick={handleRemoveProduct}
              className="p-1.5 text-gray-400 hover:text-gray-600"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        ) : (
          <button
            onClick={() => setShowProductSearch(true)}
            className="w-full py-6 border-2 border-dashed border-gray-200 rounded-xl flex flex-col items-center justify-center gap-1.5 text-gray-400"
          >
            <Search className="w-6 h-6" />
            <span className="text-xs">点击搜索关联商品</span>
          </button>
        )}
      </div>

      {showProductSearch && (
        <div className="fixed inset-0 bg-black/50 z-50">
          <motion.div
            initial={{ translateY: '100%' }}
            animate={{ translateY: 0 }}
            exit={{ translateY: '100%' }}
            className="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl max-h-[80vh] flex flex-col"
          >
            <div className="flex items-center justify-between p-3 border-b border-gray-100">
              <span className="text-sm font-medium">搜索商品</span>
              <button
                onClick={() => {
                  setShowProductSearch(false);
                  setSearchKeyword('');
                  setSearchResults([]);
                }}
                className="p-1"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-3">
              <div className="flex items-center gap-2">
                <div className="flex-1 flex items-center gap-2 bg-gray-100 rounded-full px-3 py-2">
                  <Search className="w-3.5 h-3.5 text-gray-400" />
                  <input
                    type="text"
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearchProduct()}
                    placeholder="搜索鞋款名称"
                    className="flex-1 bg-transparent text-xs outline-none"
                  />
                </div>
                <button
                  onClick={handleSearchProduct}
                  disabled={searching}
                  className="px-3 py-2 bg-emerald-600 text-white text-xs rounded-full"
                >
                  {searching ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : '搜索'}
                </button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto px-3 pb-3">
              {searchResults.length === 0 && searchKeyword && !searching && (
                <div className="py-10 text-center text-gray-400">
                  <Search className="w-10 h-10 mx-auto mb-2 opacity-50" />
                  <p className="text-xs">未找到相关商品</p>
                </div>
              )}

              <div className="space-y-1.5">
                {searchResults.map((product) => (
                  <button
                    key={product.id}
                    onClick={() => handleSelectProduct(product)}
                    className="w-full flex items-center gap-2 p-2 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors"
                  >
                    <img
                      src={product.defaultImage || '/placeholder.png'}
                      alt={product.name}
                      className="w-11 h-11 rounded-lg object-cover"
                    />
                    <div className="flex-1 text-left min-w-0">
                      <p className="text-xs font-medium text-gray-800 line-clamp-2">{product.name}</p>
                      <p className="text-[10px] text-gray-400 mt-0.5">
                        ¥{product.minPrice}
                        {product.maxPrice > product.minPrice && ` - ¥${product.maxPrice}`}
                      </p>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
}

