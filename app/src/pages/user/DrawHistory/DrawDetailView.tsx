import { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Clock, AlertCircle } from 'lucide-react';
import { Spinner } from '@/components/ui/spinner';
import { Button } from '@/components/ui/button';
import { drawApi, type DrawDetailVO } from '@/api';
import { formatDate } from '@/utils';

type DrawDisplayStatus = 'upcoming' | 'ongoing' | 'ended';

const drawStatusMap: Record<DrawDisplayStatus, { text: string; color: string }> = {
  upcoming: { text: '即将开始', color: 'text-amber-500' },
  ongoing: { text: '进行中', color: 'text-green-500' },
  ended: { text: '已结束', color: 'text-gray-500' },
};

const targetTypeMap: Record<number, string> = {
  1: '单品',
  2: '组合包',
};

const toTimestamp = (value?: string): number | null => {
  if (!value) return null;
  const normalized = value.includes('T') ? value : value.replace(' ', 'T');
  const timestamp = Date.parse(normalized);
  return Number.isNaN(timestamp) ? null : timestamp;
};

const resolveDrawDisplayStatus = (draw: DrawDetailVO): DrawDisplayStatus => {
  const now = Date.now();
  const start = toTimestamp(draw.startTime);
  const end = toTimestamp(draw.endTime);

  if (start !== null && now < start) {
    return 'upcoming';
  }
  if (end !== null && now >= end) {
    return 'ended';
  }
  return 'ongoing';
};

export default function DrawDetailViewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState<string | null>(null);
  const [drawDetail, setDrawDetail] = useState<DrawDetailVO | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (id) {
      loadDrawDetail(id);
    }
  }, [id]);

  const loadDrawDetail = async (drawId: string) => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await drawApi.getDrawDetailVO(Number(drawId));
      if (!data) {
        setError('未找到该抽签活动');
      } else {
        setDrawDetail(data);
      }
    } catch (err) {
      console.error('Failed to load draw detail:', err);
      setError('加载失败，请稍后重试');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner className="w-8 h-8" />
      </div>
    );
  }

  if (error || !drawDetail) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <header className="flex items-center px-4 h-14 bg-white">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="flex-1 text-center text-sm font-bold">抽签详情</h1>
          <div className="w-6" />
        </header>

        <div className="flex-1 flex items-center justify-center">
          <div className="text-center">
            <p className="text-gray-500">{error || '未找到该抽签活动'}</p>
            <button onClick={() => navigate(-1)} className="mt-4 text-amber-500 text-sm">
              返回
            </button>
          </div>
        </div>
      </div>
    );
  }

  const draw = drawDetail;
  const statusKey = resolveDrawDisplayStatus(draw);
  const status = drawStatusMap[statusKey];
  const isEnded = statusKey === 'ended';
  const action = searchParams.get('action');
  const canJoin = statusKey === 'ongoing';
  const isShoe = draw.targetType === 1 && draw.shoe;
  const isBundle = draw.targetType === 2 && draw.bundle;

  const displayImage = isShoe ? draw.shoe!.image : isBundle ? draw.bundle!.image : '';
  const displayName = isShoe ? draw.shoe!.name : isBundle ? draw.bundle!.name : draw.title;
  const displaySizes = isShoe ? draw.shoe!.sizes || [] : [];

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-sm font-bold">抽签详情</h1>
        <div className="w-6" />
      </header>

      <div className="relative h-56">
        <img src={displayImage} alt={draw.title} className="w-full h-full object-cover" />
        <div className="absolute top-4 left-4">
          <span className={`px-3 py-1 rounded-full text-xs text-white ${status.color.replace('text-', 'bg-')}`}>
            {status.text}
          </span>
        </div>
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex-1 px-4 py-6 pb-24"
      >
        <h1 className="text-lg font-bold mb-4">{draw.title}</h1>

        <div className="bg-white rounded-xl p-4 mb-4">
          <div className="flex gap-4">
            <img src={displayImage} alt={displayName} className="w-24 h-24 object-cover rounded-lg bg-gray-50" />
            <div className="flex-1">
              <h3 className="font-medium text-sm">{displayName}</h3>
              <p className="text-xs text-gray-500 mt-1">类型：{targetTypeMap[draw.targetType]}</p>
              {isShoe && displaySizes.length > 0 && (
                <>
                  <p className="text-xs text-gray-500 mt-1">可选尺码</p>
                  <div className="flex flex-wrap gap-2 mt-2">
                    {displaySizes.slice(0, 5).map((size) => (
                      <span key={size} className="px-2 py-1 bg-gray-100 rounded text-xs">
                        {size}
                      </span>
                    ))}
                    {displaySizes.length > 5 && (
                      <span className="px-2 py-1 text-xs text-gray-500">+{displaySizes.length - 5}</span>
                    )}
                  </div>
                </>
              )}
              {isBundle && draw.bundle?.items && (
                <div className="mt-2 space-y-1">
                  {draw.bundle.items.slice(0, 3).map((item) => (
                    <p key={`${item.skuId}-${item.name}`} className="text-xs text-gray-600">
                      {item.name} x{item.copies || 1}
                    </p>
                  ))}
                  {draw.bundle.items.length > 3 && (
                    <p className="text-xs text-gray-500">+{draw.bundle.items.length - 3} 件商品</p>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-xl p-4 mb-4">
          <div className="flex items-center gap-2 mb-3">
            <Clock className="w-4 h-4 text-gray-400" />
            <span className="font-medium text-sm">活动时间</span>
          </div>
          <div className="space-y-2 text-xs text-gray-600">
            <div className="flex justify-between">
              <span>开始时间</span>
              <span>{formatDate(draw.startTime, 'yyyy-MM-dd HH:mm')}</span>
            </div>
            <div className="flex justify-between">
              <span>结束时间</span>
              <span>{formatDate(draw.endTime, 'yyyy-MM-dd HH:mm')}</span>
            </div>
            {draw.drawTime && (
              <div className="flex justify-between">
                <span>开奖时间</span>
                <span>{formatDate(draw.drawTime, 'yyyy-MM-dd HH:mm')}</span>
              </div>
            )}
          </div>
          {isEnded && (
            <div className="mt-4 pt-4 border-t border-gray-100">
              <p className="text-xs text-gray-500">该活动已结束</p>
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl p-4">
          <div className="flex items-center gap-2 mb-3">
            <AlertCircle className="w-4 h-4 text-gray-400" />
            <span className="font-medium text-sm">抽签规则</span>
          </div>
          <ol className="text-xs text-gray-600 space-y-2 list-decimal list-inside">
            <li>每位用户仅限参与一次抽签</li>
            <li>请在活动结束前选择尺码并提交</li>
            <li>中签后需在24小时内完成支付</li>
            <li>未中签用户将收到通知</li>
            <li>最终解释权归鞋宙所有</li>
          </ol>
        </div>
      </motion.div>

      <div
        className="bg-white border-t border-gray-100 p-4"
        style={{ paddingBottom: 'calc(env(safe-area-inset-bottom, 0px) + 5rem)' }}
      >
        {action === 'join' ? (
          <Button className="w-full" onClick={() => navigate(`/draw/detail/${id}`)} disabled={!canJoin}>
            {canJoin ? '去参与抽签' : statusKey === 'upcoming' ? '活动未开始' : '活动已结束'}
          </Button>
        ) : action === 'confirm' ? (
          <Button className="w-full" onClick={() => navigate(`/draw/win-confirm/${id}`)}>
            去确认购买
          </Button>
        ) : (
          <p className="text-center text-xs text-gray-500">此页面仅供查看，如需参与抽签请前往活动页面</p>
        )}
      </div>
    </div>
  );
}

