import { type ChangeEvent, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { AlertCircle, ArrowLeft, Camera, Loader2, Star, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { commentApi, commonApi, orderApi, spuApi } from '@/api';
import type { OrderDetail, OrderVO } from '@/types';
import { formatDate } from '@/utils';
import { toast } from 'sonner';

type ReviewableOrderItem = OrderDetail & { resolvedSpuId: number; itemKey: string };

type ReviewImageStatus = 'uploading' | 'success' | 'error';

interface ReviewImageItem {
  id: string;
  file: File;
  localUrl: string;
  remoteUrl?: string;
  status: ReviewImageStatus;
  error?: string;
}

const MAX_REVIEW_IMAGES = 6;
const IMAGE_ACCEPT = 'image/jpeg,image/png,image/webp';

export default function OrderReviewPage() {
  const navigate = useNavigate();
  const { orderNumber } = useParams<{ orderNumber: string }>();

  const [order, setOrder] = useState<OrderVO | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [selectedItemKey, setSelectedItemKey] = useState<string | null>(null);
  const [rating, setRating] = useState(5);
  const [content, setContent] = useState('');
  const [skuSpuMap, setSkuSpuMap] = useState<Record<number, number>>({});
  const [images, setImages] = useState<ReviewImageItem[]>([]);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    if (!orderNumber) {
      navigate('/order/list?status=6', { replace: true });
      return;
    }

    const load = async () => {
      setLoading(true);
      try {
        const data = await orderApi.getOrderDetail(orderNumber);
        setOrder(data || null);
      } catch (error) {
        console.error('加载订单失败:', error);
        toast.error('加载订单失败');
        navigate('/order/list?status=6', { replace: true });
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [orderNumber, navigate]);

  useEffect(() => {
    if (!order?.orderDetailList || order.orderDetailList.length === 0) return;

    const missingSkuIds = Array.from(
      new Set(
        order.orderDetailList
          .filter((item) => !item.spuId && item.skuId && !skuSpuMap[item.skuId])
          .map((item) => item.skuId as number)
      )
    );

    if (missingSkuIds.length === 0) return;

    let cancelled = false;
    (async () => {
      const resolvedPairs = await Promise.all(
        missingSkuIds.map(async (skuId) => {
          try {
            const sku = await spuApi.getSkuDetail(skuId);
            return [skuId, sku?.spuId] as const;
          } catch {
            return [skuId, undefined] as const;
          }
        })
      );

      if (cancelled) return;

      setSkuSpuMap((prev) => {
        const next = { ...prev };
        resolvedPairs.forEach(([skuId, spuId]) => {
          if (spuId) {
            next[skuId] = spuId;
          }
        });
        return next;
      });
    })();

    return () => {
      cancelled = true;
    };
  }, [order, skuSpuMap]);

  useEffect(() => {
    return () => {
      images.forEach((image) => URL.revokeObjectURL(image.localUrl));
    };
  }, [images]);

  const reviewableItems = useMemo(() => {
    if (!order?.orderDetailList) return [];

    return order.orderDetailList.flatMap((item, index) => {
      const resolvedSpuId = item.spuId || (item.skuId ? skuSpuMap[item.skuId] : undefined);
      if (!resolvedSpuId) return [];
      const itemKey = `${item.skuId ?? item.bundleId ?? item.spuId ?? 'item'}-${item.shoeSize}-${index}`;
      return [{ ...item, resolvedSpuId, itemKey } satisfies ReviewableOrderItem];
    });
  }, [order, skuSpuMap]);

  const hasUnresolvedSku = useMemo(() => {
    if (!order?.orderDetailList) return false;
    return order.orderDetailList.some((item) => !item.spuId && !!item.skuId && !skuSpuMap[item.skuId]);
  }, [order, skuSpuMap]);

  useEffect(() => {
    if (reviewableItems.length === 0) {
      setSelectedItemKey(null);
      return;
    }
    if (!reviewableItems.some((item) => item.itemKey === selectedItemKey)) {
      setSelectedItemKey(reviewableItems[0].itemKey);
    }
  }, [reviewableItems, selectedItemKey]);

  const selectedItem = useMemo(
    () => reviewableItems.find((item) => item.itemKey === selectedItemKey) || null,
    [reviewableItems, selectedItemKey]
  );

  const uploadedImageUrls = useMemo(
    () => images.filter((item) => item.status === 'success' && item.remoteUrl).map((item) => item.remoteUrl as string),
    [images]
  );
  const hasUploading = images.some((item) => item.status === 'uploading');
  const hasUploadError = images.some((item) => item.status === 'error');

  const canSubmit =
    !!selectedItem &&
    content.trim().length >= 2 &&
    rating >= 1 &&
    rating <= 5 &&
    !hasUploading &&
    !hasUploadError;

  const openImagePicker = () => {
    fileInputRef.current?.click();
  };

  const uploadImage = async (file: File) => {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    const localUrl = URL.createObjectURL(file);
    const uploadingItem: ReviewImageItem = {
      id,
      file,
      localUrl,
      status: 'uploading',
    };

    setImages((prev) => [...prev, uploadingItem]);

    try {
      const remoteUrl = await commonApi.uploadImage(file);
      setImages((prev) =>
        prev.map((item) =>
          item.id === id
            ? {
                ...item,
                remoteUrl,
                status: 'success',
                error: undefined,
              }
            : item
        )
      );
    } catch {
      setImages((prev) =>
        prev.map((item) =>
          item.id === id
            ? {
                ...item,
                status: 'error',
                error: '上传失败',
              }
            : item
        )
      );
    }
  };

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const fileList = event.target.files;
    if (!fileList) return;

    const remaining = MAX_REVIEW_IMAGES - images.length;
    if (remaining <= 0) {
      toast.info(`最多上传 ${MAX_REVIEW_IMAGES} 张图片`);
      event.target.value = '';
      return;
    }

    const files = Array.from(fileList).slice(0, remaining);
    for (const file of files) {
      if (!IMAGE_ACCEPT.includes(file.type)) {
        toast.error('仅支持 JPG / PNG / WEBP 图片');
        continue;
      }
      await uploadImage(file);
    }

    event.target.value = '';
  };

  const handleRemoveImage = (imageId: string) => {
    setImages((prev) => {
      const target = prev.find((item) => item.id === imageId);
      if (target) {
        URL.revokeObjectURL(target.localUrl);
      }
      return prev.filter((item) => item.id !== imageId);
    });
  };

  const handleRetryUpload = async (imageId: string) => {
    const target = images.find((item) => item.id === imageId);
    if (!target) return;

    setImages((prev) =>
      prev.map((item) =>
        item.id === imageId
          ? {
              ...item,
              status: 'uploading',
              error: undefined,
            }
          : item
      )
    );

    try {
      const remoteUrl = await commonApi.uploadImage(target.file);
      setImages((prev) =>
        prev.map((item) =>
          item.id === imageId
            ? {
                ...item,
                remoteUrl,
                status: 'success',
                error: undefined,
              }
            : item
        )
      );
    } catch {
      setImages((prev) =>
        prev.map((item) =>
          item.id === imageId
            ? {
                ...item,
                status: 'error',
                error: '上传失败',
              }
            : item
        )
      );
    }
  };

  const handleSubmit = async () => {
    if (!order) return;
    if (order.status !== 6) {
      toast.error('仅已签收订单可评价');
      return;
    }
    if (!selectedItem) {
      toast.error('请选择要评价的商品');
      return;
    }
    const trimmed = content.trim();
    if (trimmed.length < 2) {
      toast.error('评价内容至少 2 个字');
      return;
    }
    if (hasUploading) {
      toast.info('请等待图片上传完成');
      return;
    }
    if (hasUploadError) {
      toast.info('有图片上传失败，请重试或删除');
      return;
    }

    setSubmitting(true);
    try {
      await commentApi.postComment({
        spuId: selectedItem.resolvedSpuId,
        orderNumber: order.number,
        content: trimmed,
        rating,
        images: uploadedImageUrls.length > 0 ? uploadedImageUrls : undefined,
      });
      toast.success('评价成功');
      navigate('/order/list?status=6', { replace: true });
    } catch (error) {
      console.error('评价失败:', error);
      toast.error(error instanceof Error ? error.message : '评价失败');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader2 className="w-6 h-6 animate-spin text-emerald-600" />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center text-sm text-gray-500">
        订单不存在
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col pb-16">
      <header className="flex items-center px-4 h-12 bg-white border-b border-gray-100">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="flex-1 text-center text-sm font-bold">商品评价</h1>
        <div className="w-5" />
      </header>

      <div className="p-3 space-y-3 flex-1 overflow-auto">
        <div className="bg-white rounded-lg p-3 border border-gray-100">
          <p className="text-xs text-gray-500">订单号：{order.number}</p>
          <p className="text-xs text-gray-500 mt-1">下单时间：{order.orderTime ? formatDate(order.orderTime) : '-'}</p>
        </div>

        {order.status !== 6 && (
          <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 text-xs text-amber-700">
            当前订单不是已签收状态，暂不可评价。
          </div>
        )}

        <div className="bg-white rounded-lg p-3 border border-gray-100">
          <h2 className="text-sm font-medium mb-2">选择商品</h2>
          {reviewableItems.length === 0 ? (
            <p className="text-xs text-gray-500">{hasUnresolvedSku ? '正在解析商品信息...' : '当前订单没有可评价的商品'}</p>
          ) : (
            <div className="space-y-2">
              {reviewableItems.map((item) => {
                const active = item.itemKey === selectedItemKey;
                return (
                  <button
                    key={item.itemKey}
                    onClick={() => setSelectedItemKey(item.itemKey)}
                    className={`w-full flex items-center gap-3 p-2 rounded-lg border text-left transition-colors ${
                      active ? 'border-emerald-500 bg-emerald-50' : 'border-gray-100 bg-white'
                    }`}
                  >
                    <img src={item.image} alt={item.name} className="w-14 h-14 rounded-md object-cover bg-gray-100" />
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-800 line-clamp-1">{item.name}</p>
                      <p className="text-xs text-gray-500 mt-1">尺码: {item.shoeSize} x{item.number}</p>
                    </div>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg p-3 border border-gray-100">
          <h2 className="text-sm font-medium mb-2">评分</h2>
          <div className="flex items-center gap-2">
            {Array.from({ length: 5 }).map((_, index) => {
              const value = index + 1;
              const active = value <= rating;
              return (
                <button
                  key={value}
                  onClick={() => setRating(value)}
                  className="p-1"
                  aria-label={`评分 ${value}`}
                >
                  <Star className={`w-6 h-6 ${active ? 'fill-amber-400 text-amber-400' : 'text-gray-300'}`} />
                </button>
              );
            })}
            <span className="text-sm text-gray-600">{rating} 分</span>
          </div>
        </div>

        <div className="bg-white rounded-lg p-3 border border-gray-100">
          <h2 className="text-sm font-medium mb-2">评价内容</h2>
          <Textarea
            placeholder="说说你的使用体验、尺码建议、上脚感受..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="min-h-[120px]"
            maxLength={300}
          />
          <p className="text-right text-xs text-gray-400 mt-1">{content.length}/300</p>
        </div>

        <div className="bg-white rounded-lg p-3 border border-gray-100">
          <h2 className="text-sm font-medium mb-2">评价图片（可选）</h2>
          <div className="grid grid-cols-3 gap-2">
            {images.map((image) => (
              <div key={image.id} className="relative aspect-square rounded-lg overflow-hidden bg-gray-100">
                <img src={image.localUrl} alt="评价图片" className="w-full h-full object-cover" />
                {image.status === 'uploading' && (
                  <div className="absolute inset-0 bg-black/45 flex items-center justify-center">
                    <Loader2 className="w-5 h-5 text-white animate-spin" />
                  </div>
                )}
                {image.status === 'error' && (
                  <button
                    type="button"
                    className="absolute inset-0 bg-black/55 flex flex-col items-center justify-center text-white"
                    onClick={() => handleRetryUpload(image.id)}
                  >
                    <AlertCircle className="w-5 h-5 text-red-300" />
                    <span className="text-[10px] mt-1">上传失败，点此重试</span>
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => handleRemoveImage(image.id)}
                  className="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/60 text-white flex items-center justify-center"
                  aria-label="删除图片"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ))}

            {images.length < MAX_REVIEW_IMAGES && (
              <button
                type="button"
                onClick={openImagePicker}
                className="aspect-square rounded-lg border border-dashed border-gray-300 bg-gray-50 flex flex-col items-center justify-center text-gray-500"
              >
                <Camera className="w-5 h-5" />
                <span className="text-[10px] mt-1">{images.length}/{MAX_REVIEW_IMAGES}</span>
              </button>
            )}
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept={IMAGE_ACCEPT}
            multiple
            className="hidden"
            onChange={handleFileChange}
          />
          <p className="text-[11px] text-gray-400 mt-2">支持 JPG/PNG/WEBP，最多 {MAX_REVIEW_IMAGES} 张</p>
        </div>
      </div>

      <div className="bg-white border-t border-gray-100 p-3 safe-area-bottom mb-16">
        <Button
          className="w-full bg-emerald-600 hover:bg-emerald-700"
          onClick={handleSubmit}
          disabled={!canSubmit || submitting || order.status !== 6}
        >
          {submitting ? '提交中...' : '提交评价'}
        </Button>
      </div>
    </div>
  );
}


