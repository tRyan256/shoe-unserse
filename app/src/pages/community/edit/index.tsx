import { useState, useRef, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Camera, X, Loader2, AlertCircle } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { experienceApi, commonApi } from '@/api';
import { useUserStore } from '@/stores';
import { toast } from 'sonner';
import type { ExperiencePostDetail, ShoeSpu } from '@/types';

interface ImageItem {
  id: string;
  localUrl: string;
  remoteUrl?: string;
  status: 'uploading' | 'success' | 'error';
  error?: string;
}

export default function CommunityEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [loading, setLoading] = useState(true);
  const [content, setContent] = useState('');
  const [images, setImages] = useState<ImageItem[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<ShoeSpu | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchPost = async () => {
      if (!id) return;
      
      try {
        const post: ExperiencePostDetail = await experienceApi.getPostDetail(Number(id));
        
        if (post.userId !== Number(user?.id)) {
          toast.error('只能编辑自己的心得');
          navigate('/community');
          return;
        }
        
        setContent(post.content);
        
        if (post.images && post.images.length > 0) {
          const imageItems: ImageItem[] = post.images.map((url, index) => ({
            id: `existing-${index}`,
            localUrl: url,
            remoteUrl: url,
            status: 'success' as const,
          }));
          setImages(imageItems);
        }
        
        if (post.productId && post.productName) {
          setSelectedProduct({
            id: post.productId,
            name: post.productName,
            defaultImage: post.productImage,
            minPrice: 0,
            maxPrice: 0,
          } as ShoeSpu);
        }
      } catch {
        toast.error('加载失败');
        navigate('/community');
      } finally {
        setLoading(false);
      }
    };
    
    fetchPost();
  }, [id, user?.id, navigate]);

  const handleImageSelect = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files) return;

    const remainingSlots = 9 - images.length;
    const filesToProcess = Array.from(files).slice(0, remainingSlots);

    for (const file of filesToProcess) {
      const imageId = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      const localUrl = URL.createObjectURL(file);

      const newImage: ImageItem = {
        id: imageId,
        localUrl,
        status: 'uploading',
      };

      setImages((prev) => [...prev, newImage]);

      try {
        const remoteUrl = await commonApi.uploadImage(file);
        setImages((prev) =>
          prev.map((img) =>
            img.id === imageId
              ? { ...img, remoteUrl, status: 'success' }
              : img
          )
        );
      } catch {
        setImages((prev) =>
          prev.map((img) =>
            img.id === imageId
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

  const handleRemoveImage = (imageId: string) => {
    setImages((prev) => {
      const img = prev.find((i) => i.id === imageId);
      if (img?.localUrl && !img.localUrl.startsWith('http')) {
        URL.revokeObjectURL(img.localUrl);
      }
      return prev.filter((i) => i.id !== imageId);
    });
  };

  const handleRetryUpload = async (imageId: string) => {
    const img = images.find((i) => i.id === imageId);
    if (!img) return;

    setImages((prev) =>
      prev.map((i) =>
        i.id === imageId ? { ...i, status: 'uploading', error: undefined } : i
      )
    );

    try {
      const response = await fetch(img.localUrl);
      const blob = await response.blob();
      const file = new File([blob], 'image.jpg', { type: blob.type });
      const remoteUrl = await commonApi.uploadImage(file);
      setImages((prev) =>
        prev.map((i) =>
          i.id === imageId ? { ...i, remoteUrl, status: 'success' } : i
        )
      );
    } catch {
      setImages((prev) =>
        prev.map((i) =>
          i.id === imageId ? { ...i, status: 'error', error: '上传失败' } : i
        )
      );
    }
  };

  const handleSubmit = async () => {
    if (!content.trim()) {
      toast.info('请输入心得内容');
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
      await experienceApi.updatePost(Number(id), {
        content: content.trim(),
        images: remoteUrls,
      });
      toast.success('修改成功');
      navigate(`/community/detail/${id}`);
    } catch {
      toast.error('修改失败，请重试');
    } finally {
      setSubmitting(false);
    }
  };

  const hasUploading = images.some((img) => img.status === 'uploading');
  const hasError = images.some((img) => img.status === 'error');
  const canSubmit = content.trim().length > 0 && !submitting && !hasUploading && !hasError;

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader2 className="w-6 h-6 animate-spin text-emerald-600" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="sticky top-0 z-40 bg-white border-b border-gray-100">
        <div className="flex items-center justify-between h-12 px-4">
          <button onClick={() => navigate(-1)} className="p-1.5 -ml-1.5">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <span className="text-sm font-medium">编辑心得</span>
          <button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className={`px-3 py-1 text-xs rounded-full ${
              canSubmit
                ? 'bg-emerald-600 text-white'
                : 'bg-gray-100 text-gray-400'
            }`}
          >
            {submitting ? '保存中...' : '保存'}
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
        <p className="text-xs font-medium text-gray-800 mb-2">关联商品</p>
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
            <span className="text-[10px] text-gray-400">不可修改</span>
          </div>
        ) : (
          <div className="text-xs text-gray-400 py-4 text-center">
            无关联商品
          </div>
        )}
      </div>
    </div>
  );
}

