import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Clock, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { CountdownTimer } from '@/components/shared';
import { drawApi } from '@/api';
import { useUserStore } from '@/stores';
import type { Draw } from '@/types';
import { drawStatusMap } from '@/utils';
import { formatDate } from '@/utils';
import { toast } from 'sonner';

export default function DrawDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  const [draw, setDraw] = useState<Draw | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isJoining, setIsJoining] = useState(false);

  useEffect(() => {
    if (id) {
      loadDrawDetail(id);
    }
  }, [id]);

  const loadDrawDetail = async (drawId: string) => {
    try {
      setIsLoading(true);
      const data = await drawApi.getDrawDetail(Number(drawId));
      setDraw(data || null);
    } catch (error) {
      console.error('Failed to load draw detail:', error);
      toast.error('加载抽签详情失败');
    } finally {
      setIsLoading(false);
    }
  };


  const handleJoin = async () => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      navigate('/login');
      return;
    }

    if (!draw || !id) {
      return;
    }

    setIsJoining(true);
    try {
      await drawApi.joinDraw(Number(id));
      toast.success('参与成功，等待开奖');
      navigate('/draw/my');
    } catch (error) {
      console.error('Failed to join draw:', error);
      toast.error(error instanceof Error ? error.message : '参与失败，请稍后重试');
    } finally {
      setIsJoining(false);
    }
  };

  if (isLoading || !draw) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-black border-t-transparent rounded-full" />
      </div>
    );
  }

  const status = drawStatusMap[draw.status];
  const isOngoing = draw.status === 'ongoing';
  const isEnded = draw.status === 'ended';
  const hasJoined = draw.participantStatus === 'joined';

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">抽签详情</h1>
        <div className="w-6" />
      </header>

      <div className="relative h-56">
        <img src={draw.image} alt={draw.name} className="w-full h-full object-cover" />
        <div className="absolute top-4 left-4">
          <span className={`px-3 py-1 rounded-full text-sm text-white ${status.color.replace('text-', 'bg-')}`}>
            {status.text}
          </span>
        </div>
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex-1 px-4 py-6 pb-24"
      >
        <h1 className="text-xl font-bold mb-4">{draw.name}</h1>

        <div className="bg-white rounded-xl p-4 mb-4">
          <div className="flex gap-4">
            <img src={draw.shoeImage} alt={draw.shoeName} className="w-24 h-24 object-cover rounded-lg bg-gray-50" />
            <div>
              <h3 className="font-medium">{draw.shoeName}</h3>
              <p className="text-sm text-gray-500 mt-1">可购尺码（仅中签后选择）</p>
              <div className="flex flex-wrap gap-2 mt-2">
                {draw.sizes.slice(0, 5).map((size) => (
                  <span key={size} className="px-2 py-1 bg-gray-100 rounded text-xs">
                    {size}
                  </span>
                ))}
                {draw.sizes.length > 5 && (
                  <span className="px-2 py-1 text-xs text-gray-500">+{draw.sizes.length - 5}</span>
                )}
                {draw.sizes.length === 0 && <span className="text-xs text-gray-500">中签后按活动规则确认购买</span>}
              </div>
            </div>
          </div>
          <button
            onClick={() => navigate(`/draw/info/${id}?action=join`)}
            className="mt-3 text-xs text-emerald-700 hover:text-emerald-800"
          >
            查看活动商品详情
          </button>
        </div>

        <div className="bg-white rounded-xl p-4 mb-4">
          <div className="flex items-center gap-2 mb-3">
            <Clock className="w-5 h-5 text-gray-400" />
            <span className="font-medium">活动时间</span>
          </div>
          <div className="space-y-2 text-sm text-gray-600">
            <div className="flex justify-between">
              <span>开始时间</span>
              <span>{formatDate(draw.startTime, 'yyyy-MM-dd HH:mm')}</span>
            </div>
            <div className="flex justify-between">
              <span>结束时间</span>
              <span>{formatDate(draw.endTime, 'yyyy-MM-dd HH:mm')}</span>
            </div>
          </div>
          {isOngoing && (
            <div className="mt-4 pt-4 border-t border-gray-100">
              <p className="text-sm text-gray-500 mb-2">距离结束还有</p>
              <CountdownTimer targetTime={draw.endTime} />
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl p-4">
          <div className="flex items-center gap-2 mb-3">
            <AlertCircle className="w-5 h-5 text-gray-400" />
            <span className="font-medium">抽签规则</span>
          </div>
          <ol className="text-sm text-gray-600 space-y-2 list-decimal list-inside">
            <li>每位用户仅限参与一次抽签</li>
            <li>参与时只需点击参与，不需要预先选择地址和尺码</li>
            <li>中签后在确认购买时再选择尺码</li>
            <li>中签后需在24小时内完成支付</li>
            <li>最终解释权归鞋宙所有</li>
          </ol>
        </div>
      </motion.div>

      <div
        className="bg-white border-t border-gray-100 p-4"
        style={{ paddingBottom: 'calc(env(safe-area-inset-bottom, 0px) + 5rem)' }}
      >
        {isEnded ? (
          <Button disabled className="w-full">活动已结束</Button>
        ) : hasJoined ? (
          <Button disabled className="w-full">已参与</Button>
        ) : (
          <Button className="w-full" onClick={handleJoin} disabled={!isOngoing || isJoining}>
            {isJoining ? '提交中...' : isOngoing ? '立即参与' : '即将开始'}
          </Button>
        )}
      </div>
    </div>
  );
}
