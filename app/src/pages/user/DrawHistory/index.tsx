import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';
import { Spinner } from '@/components/ui/spinner';
import { listDrawRecords, type DrawRecordVO } from '@/api/user/drawHistoryApi';
import DrawRecordCard from './DrawRecordCard';

export default function DrawHistoryPage() {
  const navigate = useNavigate();
  const [records, setRecords] = useState<DrawRecordVO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    loadRecords();
  }, []);

  const loadRecords = async (pageNum = 1) => {
    try {
      setIsLoading(true);
      const params = {
        page: pageNum,
        size: 20,
      };
      const result = await listDrawRecords(params);
      
      if (pageNum === 1) {
        setRecords(result.records);
      } else {
        setRecords((prev) => [...prev, ...result.records]);
      }
      
      setPage(pageNum);
      setHasMore(result.records.length === 20);
    } catch (error) {
      console.error('Failed to load draw records:', error);
      toast.error('加载抽签记录失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLoadMore = () => {
    if (!isLoading && hasMore) {
      loadRecords(page + 1);
    }
  };

  if (isLoading && page === 1) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner className="w-8 h-8" />
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
        <h1 className="flex-1 text-center text-sm font-bold">抽签记录</h1>
        <div className="w-6" />
      </header>

      {/* 抽签记录列表 */}
      <div className="flex-1 p-4 space-y-3">
        {records.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500">暂无抽签记录</p>
          </div>
        ) : (
          <>
            {records.map((record, index) => (
              <motion.div
                key={record.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05 }}
              >
                <DrawRecordCard record={record} />
              </motion.div>
            ))}
            
            {/* 加载更多 */}
            {hasMore && (
              <div className="text-center py-4">
                <button
                  onClick={handleLoadMore}
                  disabled={isLoading}
                  className="text-sm text-gray-500 hover:text-gray-700 disabled:opacity-50"
                >
                  {isLoading ? '加载中...' : '加载更多'}
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
