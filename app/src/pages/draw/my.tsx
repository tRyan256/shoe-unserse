import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Gift } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { drawApi } from '@/api';
import type { DrawRecord } from '@/types';
import { drawRecordStatusMap, formatDate } from '@/utils';

export default function MyDrawPage() {
  const navigate = useNavigate();
  const [records, setRecords] = useState<DrawRecord[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadRecords();
  }, []);

  const loadRecords = async () => {
    try {
      setIsLoading(true);
      const data = await drawApi.getMyDraws();
      setRecords(data);
    } catch (error) {
      console.error('Failed to load draw records:', error);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-black border-t-transparent rounded-full" />
      </div>
    );
  }

  if (records.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <header className="flex items-center px-4 h-14 bg-white">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="flex-1 text-center text-lg font-bold">我的抽签</h1>
          <div className="w-6" />
        </header>

        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-6">
            <Gift className="w-10 h-10 text-gray-300" />
          </div>
          <h2 className="text-lg font-medium text-gray-900 mb-2">暂无抽签记录</h2>
          <p className="text-sm text-gray-500 mb-6">快去参与限量抽签吧</p>
          <Button onClick={() => navigate('/draw/list')}>去抽签</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 头部 */}
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">我的抽签</h1>
        <div className="w-6" />
      </header>

      {/* 记录列表 */}
      <div className="flex-1 p-4 space-y-4">
        {records.map((record, index) => {
          const status = drawRecordStatusMap[record.status];
          return (
            <motion.div
              key={record.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              className="bg-white rounded-xl p-4"
            >
              <div className="flex gap-4">
                <img
                  src={record.shoeImage}
                  alt={record.drawName}
                  className="w-20 h-20 object-cover rounded-lg bg-gray-50"
                />
                <div className="flex-1">
                  <div className="flex items-start justify-between">
                    <h3 className="font-medium line-clamp-1">{record.drawName}</h3>
                    <Badge className={status.color}>{status.text}</Badge>
                  </div>
                  <p className="text-sm text-gray-500 mt-1">
                    尺码: {record.shoeSize}
                  </p>
                  <p className="text-xs text-gray-400 mt-1">
                    参与时间: {formatDate(record.joinTime, 'MM-dd HH:mm')}
                  </p>
                </div>
              </div>

              {record.status === 'won' && (
                <div className="mt-4 pt-4 border-t border-gray-100">
                  <Button
                    className="w-full"
                    onClick={() => navigate(`/draw/win-confirm/${record.drawId}`)}
                  >
                    确认购买
                  </Button>
                </div>
              )}
            </motion.div>
          );
        })}
      </div>
    </div>
  );
}
