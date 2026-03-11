import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Star, MessageSquare } from 'lucide-react';
import { toast } from 'sonner';
import { Spinner } from '@/components/ui/spinner';
import { commentApi } from '@/api';
import type { MyComment } from '@/types';
import { formatDate } from '@/utils';

export default function ReviewsPage() {
  const navigate = useNavigate();
  const [reviews, setReviews] = useState<MyComment[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadReviews();
  }, []);

  const loadReviews = async () => {
    try {
      setIsLoading(true);
      const data = await commentApi.getMyComments();
      setReviews(data || []);
    } catch (error) {
      console.error('Failed to load reviews:', error);
      toast.error('加载评价失败');
    } finally {
      setIsLoading(false);
    }
  };

  const renderStars = (rating: number) => {
    return (
      <div className="flex gap-0.5">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={`w-3 h-3 ${
              star <= rating ? 'fill-amber-400 text-amber-400' : 'text-gray-300'
            }`}
          />
        ))}
      </div>
    );
  };

  const parseImages = (images: string): string[] => {
    if (!images || images === '[]') return [];
    try {
      const parsed = JSON.parse(images);
      return Array.isArray(parsed) ? parsed.filter(Boolean) : [];
    } catch {
      return images.split(',').filter(Boolean);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner className="w-8 h-8" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white sticky top-0 z-10">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-sm font-bold">我的评价</h1>
        <div className="w-6" />
      </header>

      <div className="flex-1 p-3 space-y-3">
        {reviews.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-3">
              <MessageSquare className="w-8 h-8 text-gray-300" />
            </div>
            <p className="text-gray-500 text-sm">暂无评价记录</p>
            <p className="text-gray-400 text-xs mt-1">购买商品后可以进行评价</p>
          </div>
        ) : (
          reviews.map((review, index) => {
            const images = parseImages(review.images);
            return (
              <motion.div
                key={review.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
                className="bg-white rounded-lg p-3 shadow-sm"
              >
                <div className="flex gap-3">
                  <div
                    className="w-16 h-16 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0 cursor-pointer"
                    onClick={() => navigate(`/shoe/${review.spuId}`)}
                  >
                    <img
                      src={review.spuImage || '/images/default-banner.svg'}
                      alt={review.spuName}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3
                      className="text-xs font-medium text-gray-800 line-clamp-1 cursor-pointer hover:text-emerald-600"
                      onClick={() => navigate(`/shoe/${review.spuId}`)}
                    >
                      {review.spuName}
                    </h3>
                    <div className="flex items-center gap-2 mt-1">
                      {renderStars(review.rating)}
                      <span className="text-[10px] text-gray-400">
                        {formatDate(review.createTime, 'yyyy-MM-dd')}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="mt-2 pt-2 border-t border-gray-100">
                  <p className="text-xs text-gray-600 leading-relaxed">{review.content}</p>
                  {images.length > 0 && (
                    <div className="flex gap-2 mt-2 flex-wrap">
                      {images.map((img, imgIndex) => (
                        <div
                          key={imgIndex}
                          className="w-16 h-16 rounded-lg overflow-hidden bg-gray-100"
                        >
                          <img
                            src={img}
                            alt={`评价图片 ${imgIndex + 1}`}
                            className="w-full h-full object-cover"
                          />
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </motion.div>
            );
          })
        )}
      </div>
    </div>
  );
}
