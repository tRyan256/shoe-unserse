import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { ArrowLeft, AlertCircle, Clock, MapPin, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { addressApi, drawApi, type DrawDetailVO, type DrawWinOptionsVO } from '@/api';
import { formatDate, formatPrice } from '@/utils';
import type { Address } from '@/types';
import { toast } from 'sonner';

export default function DrawWinConfirmPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const drawId = Number(id);
  const selectedAddressId = searchParams.get('addressId');

  const [detail, setDetail] = useState<DrawDetailVO | null>(null);
  const [options, setOptions] = useState<DrawWinOptionsVO | null>(null);
  const [address, setAddress] = useState<Address | null>(null);
  const [selectedShoeSize, setSelectedShoeSize] = useState('');
  const [selectedBundleSizes, setSelectedBundleSizes] = useState<Record<number, string>>({});
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!Number.isFinite(drawId)) {
      navigate('/draw/my', { replace: true });
      return;
    }

    const load = async () => {
      try {
        setIsLoading(true);
        const [detailData, optionsData] = await Promise.all([
          drawApi.getDrawDetailVO(drawId),
          drawApi.getWinOptions(drawId),
        ]);
        setDetail(detailData);
        setOptions(optionsData);

        if (optionsData.targetType === 1 && optionsData.shoeOptions && optionsData.shoeOptions.length > 0) {
          const firstAvailable = optionsData.shoeOptions.find((item) => item.stock > 0);
          setSelectedShoeSize(firstAvailable?.size || '');
        }
      } catch (error) {
        console.error('Failed to load win confirm data:', error);
        toast.error(error instanceof Error ? error.message : '加载失败');
        navigate('/draw/my', { replace: true });
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, [drawId, navigate]);

  useEffect(() => {
    if (!Number.isFinite(drawId)) {
      return;
    }

    const loadAddress = async () => {
      try {
        if (selectedAddressId) {
          const data = await addressApi.getAddressDetail(Number(selectedAddressId));
          setAddress(data);
          return;
        }
        const data = await addressApi.getDefaultAddress();
        setAddress(data || null);
      } catch (error) {
        console.error('Failed to load draw address:', error);
        setAddress(null);
      }
    };

    loadAddress();
  }, [drawId, selectedAddressId]);

  const targetType = options?.targetType ?? detail?.targetType;

  const bundleOptionMap = useMemo(() => {
    const map = new Map<number, { name?: string; image?: string }>();
    if (detail?.bundle?.items) {
      detail.bundle.items.forEach((item) => {
        map.set(item.skuId, {
          name: item.name,
          image: item.image,
        });
      });
    }
    return map;
  }, [detail]);

  const deadlineText = useMemo(() => {
    if (!detail?.drawTime) return '请在开奖后 24 小时内完成确认';
    const deadline = new Date(detail.drawTime);
    deadline.setHours(deadline.getHours() + 24);
    return `确认截止：${formatDate(deadline, 'MM-dd HH:mm')}`;
  }, [detail?.drawTime]);

  const canSubmit = useMemo(() => {
    if (!options || !address) return false;
    if (options.targetType === 1) {
      return !!selectedShoeSize;
    }
    if (options.targetType === 2) {
      const bundleOptions = options.bundleOptions || [];
      if (bundleOptions.length === 0) return false;
      return bundleOptions.every((item) => {
        const selected = selectedBundleSizes[item.skuId];
        return !!selected && item.options.some((option) => option.size === selected && option.stock > 0);
      });
    }
    return false;
  }, [address, options, selectedShoeSize, selectedBundleSizes]);

  const handleConfirm = async () => {
    if (!Number.isFinite(drawId) || !options || !address) return;

    try {
      setIsSubmitting(true);
      if (options.targetType === 1) {
        await drawApi.confirmWin(drawId, {
          addressBookId: address.id,
          shoeSize: selectedShoeSize,
        });
      } else {
        const items = (options.bundleOptions || []).map((item) => ({
          skuId: item.skuId,
          shoeSize: selectedBundleSizes[item.skuId],
        }));
        await drawApi.confirmWin(drawId, {
          addressBookId: address.id,
          items,
        });
      }
      toast.success('确认成功，订单已生成');
      navigate('/order/list', { replace: true });
    } catch (error) {
      console.error('Failed to confirm draw win:', error);
      toast.error(error instanceof Error ? error.message : '确认失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-emerald-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!detail || !options) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center text-sm text-gray-500">
        加载失败
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">中奖确认</h1>
        <div className="w-6" />
      </header>

      <div className="p-4 space-y-4 flex-1 overflow-auto">
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4">
          <div className="flex items-start gap-2">
            <AlertCircle className="w-5 h-5 text-amber-500 mt-0.5" />
            <div className="text-sm text-amber-700">
              <p className="font-medium">恭喜你已中签，请确认购买信息</p>
              <p className="mt-1 flex items-center gap-1 text-xs">
                <Clock className="w-3 h-3" />
                {deadlineText}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl p-4">
          <h2 className="text-base font-semibold mb-3">商品信息</h2>
          <div className="flex gap-3">
            <img
              src={detail.targetType === 1 ? detail.shoe?.image : detail.bundle?.image}
              alt={detail.title}
              className="w-20 h-20 rounded-lg object-cover bg-gray-100"
            />
            <div className="min-w-0 flex-1">
              <p className="font-medium text-gray-800 line-clamp-2">{detail.title}</p>
              <p className="text-xs text-gray-500 mt-1">
                {detail.targetType === 1 ? detail.shoe?.name : detail.bundle?.name}
              </p>
              <p className="text-sm font-semibold text-emerald-700 mt-2">{formatPrice(detail.price || 0)}</p>
              <button
                onClick={() => navigate(`/draw/info/${drawId}?action=confirm`)}
                className="mt-2 text-xs text-emerald-700 hover:text-emerald-800"
              >
                查看活动商品详情
              </button>
            </div>
          </div>
        </div>

        <div
          className="bg-white rounded-xl p-4"
          onClick={() => navigate(`/address/list?mode=select&returnUrl=/draw/win-confirm/${drawId}`)}
        >
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-base font-semibold">收货地址</h2>
            <ChevronRight className="w-4 h-4 text-gray-400" />
          </div>
          {address ? (
            <div className="flex items-start gap-3">
              <MapPin className="w-5 h-5 text-gray-400 mt-0.5" />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-medium text-gray-900">{address.consignee}</span>
                  <span className="text-sm text-gray-500">{address.phone}</span>
                  {address.isDefault && (
                    <span className="px-2 py-0.5 rounded-full bg-gray-100 text-[10px] text-gray-500">默认地址</span>
                  )}
                </div>
                <p className="text-sm text-gray-600 line-clamp-2">
                  {address.province} {address.city} {address.district} {address.detail}
                </p>
              </div>
            </div>
          ) : (
            <div className="text-sm text-gray-500">暂无默认地址，点击选择收货地址</div>
          )}
        </div>

        {targetType === 1 && (
          <div className="bg-white rounded-xl p-4">
            <h2 className="text-base font-semibold mb-3">尺码与库存</h2>
            <div className="grid grid-cols-3 gap-3">
              {(options.shoeOptions || []).map((item) => {
                const disabled = item.stock <= 0;
                const selected = selectedShoeSize === item.size;
                return (
                  <button
                    key={item.size}
                    onClick={() => !disabled && setSelectedShoeSize(item.size)}
                    disabled={disabled}
                    className={`rounded-xl border px-3 py-3 text-left transition-colors ${
                      disabled
                        ? 'border-gray-100 bg-gray-50 text-gray-300 cursor-not-allowed'
                        : selected
                          ? 'border-black bg-black text-white'
                          : 'border-gray-200 bg-white text-gray-700'
                    }`}
                  >
                    <div className="text-sm font-medium">{item.size}</div>
                    <div className={`text-[11px] mt-1 ${selected ? 'text-white/80' : 'text-gray-500'}`}>
                      库存 {item.stock}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {targetType === 2 && (
          <div className="bg-white rounded-xl p-4 space-y-4">
            <h2 className="text-base font-semibold">可购买鞋款与尺码库存</h2>
            {(options.bundleOptions || []).map((bundleOption) => {
              const extra = bundleOptionMap.get(bundleOption.skuId);
              return (
                <div key={bundleOption.skuId} className="border border-gray-100 rounded-lg p-3">
                  <div className="flex items-center gap-3 mb-3">
                    {extra?.image && (
                      <img
                        src={extra.image}
                        alt={extra.name || String(bundleOption.skuId)}
                        className="w-12 h-12 rounded-md object-cover bg-gray-100"
                      />
                    )}
                    <div>
                      <p className="text-sm font-medium text-gray-800">{extra?.name || `SKU ${bundleOption.skuId}`}</p>
                      <p className="text-xs text-gray-500">数量 x{bundleOption.copies || 1}</p>
                    </div>
                  </div>
                  <div className="grid grid-cols-3 gap-2">
                    {bundleOption.options.map((sizeOption) => {
                      const disabled = sizeOption.stock <= 0;
                      const selected = selectedBundleSizes[bundleOption.skuId] === sizeOption.size;
                      return (
                        <button
                          key={`${bundleOption.skuId}-${sizeOption.size}`}
                          onClick={() =>
                            !disabled &&
                            setSelectedBundleSizes((prev) => ({
                              ...prev,
                              [bundleOption.skuId]: sizeOption.size,
                            }))
                          }
                          disabled={disabled}
                          className={`rounded-lg border px-2 py-3 text-left transition-colors ${
                            disabled
                              ? 'border-gray-100 bg-gray-50 text-gray-300 cursor-not-allowed'
                              : selected
                                ? 'bg-black text-white border-black'
                                : 'bg-white text-gray-700 border-gray-200'
                          }`}
                        >
                          <div className="text-sm font-medium">{sizeOption.size}</div>
                          <div className={`text-[11px] mt-1 ${selected ? 'text-white/80' : 'text-gray-500'}`}>
                            库存 {sizeOption.stock}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div className="bg-white border-t border-gray-100 p-4 safe-area-bottom">
        <Button className="w-full" onClick={handleConfirm} disabled={!canSubmit || isSubmitting}>
          {isSubmitting ? '提交中...' : !address ? '请选择地址' : '确认购买'}
        </Button>
      </div>
    </div>
  );
}
