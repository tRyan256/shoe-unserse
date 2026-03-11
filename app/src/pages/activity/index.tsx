import { useState, useEffect, useCallback } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Gift, Ticket, Clock, Sparkles, Calendar, Bell } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { drawApi, activityApi, airdropApi } from '@/api';
import { useUserStore } from '@/stores';
import type { Draw, ActivityBanner } from '@/types';
import { formatDate } from '@/utils';
import { ScrollToTop } from '@/components/shared';
import { toast } from 'sonner';

const validTabs = new Set(['draw', 'airdrop', 'preview']);

export default function ActivityPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = searchParams.get('tab');
  const [activeTab, setActiveTab] = useState(
    initialTab && validTabs.has(initialTab) ? initialTab : 'draw'
  );
  const [draws, setDraws] = useState<Draw[]>([]);
  const [banners, setBanners] = useState<ActivityBanner[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [claimingId, setClaimingId] = useState<string | null>(null);
  const [claimedAirdropIds, setClaimedAirdropIds] = useState<Set<string>>(new Set());
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);

  const loadData = useCallback(async () => {
    try {
      setIsLoading(true);
      const [drawData, bannerData] = await Promise.all([
        drawApi.getDrawList(),
        activityApi.getBannerActivities(),
      ]);
      const sortedDraws = (drawData || []).sort((a, b) => {
        const timeA = a.startTime ? new Date(a.startTime).getTime() : 0;
        const timeB = b.startTime ? new Date(b.startTime).getTime() : 0;
        return timeB - timeA;
      });
      const sortedBanners = ((bannerData || []).filter((item) => item.type !== 'default')).sort((a, b) => {
        const timeA = a.startTime ? new Date(a.startTime).getTime() : 0;
        const timeB = b.startTime ? new Date(b.startTime).getTime() : 0;
        return timeB - timeA;
      });
      setDraws(sortedDraws);
      setBanners(sortedBanners);
      
      // 加载已领取的空投记录
      if (isLoggedIn()) {
        try {
          const claimedIds = await airdropApi.getMyAirdrops();
          setClaimedAirdropIds(new Set(claimedIds));
        } catch (error) {
          console.error('Failed to load claimed airdrops:', error);
        }
      }
    } catch (error) {
      console.error('Failed to load activity data:', error);
      toast.error('活动数据加载失败');
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    setSearchParams({ tab: activeTab }, { replace: true });
  }, [activeTab, setSearchParams]);

  const airdropActivities = banners.filter((item) => item.type === 'airdrop');
  const previewActivities = banners.filter((item) => item.bannerStatus === 'warmup');

  const handleClaimAirdrop = async (id: string) => {
    if (!isLoggedIn()) {
      toast.info('请先登录');
      return;
    }

    try {
      setClaimingId(id);
      await airdropApi.receiveAirdrop(Number(id));
      toast.success('领取成功，奖励已发放');
      // 更新已领取状态
      setClaimedAirdropIds(prev => new Set(prev).add(id));
      await loadData();
    } catch (error) {
      console.error('Failed to claim airdrop:', error);
      toast.error(error instanceof Error ? error.message : '领取失败，请稍后重试');
    } finally {
      setClaimingId(null);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="w-8 h-8 border-2 border-emerald-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 pb-20">
      <header className="flex items-center px-4 h-14 bg-white sticky top-0 z-40 shadow-sm">
        <div className="w-10" />
        <h1 className="flex-1 text-center text-lg font-bold">活动中心</h1>
        <div className="w-10" />
      </header>

      <div className="bg-white border-b border-gray-200 sticky top-14 z-30">
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="w-full justify-start h-12 bg-transparent rounded-none px-2">
            <TabsTrigger
              value="draw"
              className="flex-1 text-sm data-[state=active]:bg-emerald-50 data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-lg gap-1.5 text-gray-500 transition-colors"
            >
              <Ticket className="w-4 h-4" />
              抽签
            </TabsTrigger>
            <TabsTrigger
              value="airdrop"
              className="flex-1 text-sm data-[state=active]:bg-emerald-50 data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-lg gap-1.5 text-gray-500 transition-colors"
            >
              <Gift className="w-4 h-4" />
              空投
            </TabsTrigger>
            <TabsTrigger
              value="preview"
              className="flex-1 text-sm data-[state=active]:bg-emerald-50 data-[state=active]:shadow-none data-[state=active]:text-emerald-600 rounded-lg gap-1.5 text-gray-500 transition-colors"
            >
              <Sparkles className="w-4 h-4" />
              预热
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      {activeTab === 'draw' && (
        <div className="flex-1 p-3 space-y-3">
          <div className="bg-gradient-to-r from-emerald-500 to-teal-500 rounded-lg p-4 text-white">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
                <Ticket className="w-4 h-4" />
              </div>
              <div>
                <span className="font-bold text-sm">限量抽签</span>
                <p className="text-white/70 text-[10px]">公平公正，每个人都有机会</p>
              </div>
            </div>
          </div>

          <div>
            <h2 className="text-sm font-bold mb-2 flex items-center gap-1.5">
              <span className="w-0.5 h-4 bg-gradient-to-b from-green-400 to-emerald-500 rounded-full" />
              <span className="text-gray-700">火热进行中</span>
            </h2>
            {draws.filter((d) => d.status === 'ongoing').length === 0 ? (
              <div className="bg-white rounded-lg p-4 text-center text-xs text-gray-500">暂无进行中的抽签活动</div>
            ) : (
              draws
                .filter((d) => d.status === 'ongoing')
                .map((draw, index) => (
                  <motion.div
                    key={draw.id}
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.05 }}
                  >
                    <Link to={`/draw/detail/${draw.id}`}>
                      <div className="bg-white rounded-lg overflow-hidden shadow-sm mb-3">
                        <div className="relative h-40">
                          <img src={draw.image} alt={draw.name} className="w-full h-full object-cover" />
                          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
                          <div className="absolute top-2 left-2">
                            <Badge className="bg-emerald-500 text-white border-0 text-[10px]">进行中</Badge>
                          </div>
                          <div className="absolute bottom-3 left-3 right-3">
                            <h3 className="text-white font-bold text-base">{draw.name}</h3>
                            <p className="text-white/80 text-xs mt-0.5">{draw.shoeName}</p>
                          </div>
                        </div>
                        <div className="p-3 flex items-center justify-between bg-gray-50">
                          <div className="flex items-center gap-1.5 text-[10px] text-gray-500">
                            <Clock className="w-3 h-3 text-emerald-600" strokeWidth={1.5} />
                            <span>截止: {formatDate(draw.endTime, 'MM-dd HH:mm')}</span>
                          </div>
                          <Button size="sm" className="bg-emerald-500 text-white border-0 text-[10px] h-6 px-3">立即参与</Button>
                        </div>
                      </div>
                    </Link>
                  </motion.div>
                ))
            )}
          </div>

          {draws.filter((d) => d.status === 'upcoming').length > 0 && (
            <div>
              <h2 className="text-sm font-bold mb-2 flex items-center gap-1.5">
                <span className="w-0.5 h-4 bg-gradient-to-b from-amber-400 to-orange-500 rounded-full" />
                <span className="text-gray-700">即将开始</span>
              </h2>
              <div className="space-y-2">
                {draws
                  .filter((d) => d.status === 'upcoming')
                  .map((draw, index) => (
                    <motion.div
                      key={draw.id}
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.05 }}
                    >
                      <Link to={`/draw/detail/${draw.id}`}>
                        <div className="bg-white rounded-lg p-3 flex gap-3 shadow-sm">
                          <div className="w-20 h-20 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
                            <img src={draw.shoeImage} alt={draw.name} className="w-full h-full object-cover" />
                          </div>
                          <div className="flex-1">
                            <Badge className="bg-amber-500 text-white border-0 text-[10px] mb-1">即将开始</Badge>
                            <h3 className="text-xs font-medium text-gray-800">{draw.name}</h3>
                            <p className="text-[10px] text-gray-500 mt-0.5">{draw.shoeName}</p>
                            <p className="text-[10px] text-gray-400 mt-1 flex items-center gap-1">
                              <Calendar className="w-3 h-3" />
                              {formatDate(draw.startTime, 'MM-dd HH:mm')}
                            </p>
                          </div>
                        </div>
                      </Link>
                    </motion.div>
                  ))}
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'airdrop' && (
        <div className="flex-1 p-3 space-y-3">
          <div className="bg-gradient-to-r from-teal-500 to-cyan-500 rounded-lg p-4 text-white">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
                <Gift className="w-4 h-4" />
              </div>
              <div>
                <span className="font-bold text-sm">空投福利</span>
                <p className="text-white/70 text-[10px]">先到先得</p>
              </div>
            </div>
          </div>

          {airdropActivities.length === 0 ? (
            <div className="bg-white rounded-lg p-4 text-center text-xs text-gray-500">暂无空投活动</div>
          ) : (
            airdropActivities.map((airdrop, index) => {
              const isWarmup = airdrop.bannerStatus === 'warmup';
              const isEnded = airdrop.bannerStatus === 'drawn';
              const isActive = airdrop.bannerStatus === 'active';
              const isClaiming = claimingId === airdrop.id;
              const isClaimed = claimedAirdropIds.has(airdrop.id);

              return (
                <motion.div
                  key={airdrop.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05 }}
                  className="bg-white rounded-lg p-3 shadow-sm"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-14 h-14 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
                      <img src={airdrop.image} alt={airdrop.title} className="w-full h-full object-cover" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 mb-0.5">
                        <h3 className="text-xs font-medium text-gray-800 line-clamp-1">{airdrop.title}</h3>
                        <Badge
                          className={
                            isEnded
                              ? 'bg-gray-400 text-white text-[10px]'
                              : isClaimed
                                ? 'bg-blue-500 text-white text-[10px]'
                                : isWarmup
                                  ? 'bg-amber-500 text-white text-[10px]'
                                  : 'bg-emerald-500 text-white text-[10px]'
                          }
                        >
                          {isEnded ? '已结束' : isClaimed ? '已领取' : isWarmup ? '预热中' : '可领取'}
                        </Badge>
                      </div>
                      <p className="text-[10px] text-gray-500 line-clamp-2">{airdrop.description || '限时空投活动'}</p>
                      <p className="text-[10px] text-gray-400 mt-1">
                        {isWarmup ? '开始' : '截止'}: {formatDate(isWarmup ? airdrop.startTime : airdrop.endTime, 'MM-dd HH:mm')}
                      </p>
                    </div>
                    <Button
                      size="sm"
                      disabled={isWarmup || isEnded || !isActive || isClaiming || isClaimed}
                      onClick={() => handleClaimAirdrop(airdrop.id)}
                      className={
                        isClaimed
                          ? 'bg-blue-500 text-white border-0 disabled:bg-blue-500 text-[10px] h-6 px-2'
                          : 'bg-emerald-500 text-white border-0 disabled:bg-gray-300 text-[10px] h-6 px-2'
                      }
                    >
                      {isClaiming ? '领取中' : isClaimed ? '已领取' : isEnded ? '结束' : isWarmup ? '预热' : '领取'}
                    </Button>
                  </div>
                </motion.div>
              );
            })
          )}
        </div>
      )}

      {activeTab === 'preview' && (
        <div className="flex-1 p-3 space-y-3">
          <div className="bg-gradient-to-r from-amber-500 to-orange-500 rounded-lg p-4 text-white">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
                <Bell className="w-4 h-4" />
              </div>
              <div>
                <span className="font-bold text-sm">活动预热</span>
                <p className="text-white/70 text-[10px]">关注即将开始的活动</p>
              </div>
            </div>
          </div>

          {previewActivities.length === 0 ? (
            <div className="bg-white rounded-lg p-4 text-center text-xs text-gray-500">暂无预热活动</div>
          ) : (
            <div className="space-y-3">
              {previewActivities.map((activity, index) => (
                <motion.div
                  key={activity.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05 }}
                  className="bg-white rounded-lg overflow-hidden shadow-sm"
                >
                  <div className="relative h-36">
                    <img src={activity.image} alt={activity.title} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent" />
                    <div className="absolute top-2 left-2">
                      <Badge className="bg-amber-500 text-white border-0 text-[10px]">预热中</Badge>
                    </div>
                    <div className="absolute bottom-3 left-3 right-3">
                      <h3 className="text-white font-bold text-sm">{activity.title}</h3>
                      <p className="text-white/80 text-[10px]">{activity.description || '限时活动即将开始'}</p>
                    </div>
                  </div>
                  <div className="p-3 bg-gray-50">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-1.5 text-[10px] text-gray-500">
                        <Clock className="w-3 h-3 text-amber-600" strokeWidth={1.5} />
                        <span>{formatDate(activity.startTime, 'MM-dd HH:mm')} 开始</span>
                      </div>
                      <Button variant="outline" size="sm" className="border-amber-300 text-amber-600 hover:bg-amber-50 text-[10px] h-6 px-2">
                        <Bell className="w-3 h-3 mr-1" strokeWidth={1.5} />
                        关注
                      </Button>
                    </div>
                  </div>
                </motion.div>
              ))}
            </div>
          )}
        </div>
      )}

      <ScrollToTop />
    </div>
  );
}

