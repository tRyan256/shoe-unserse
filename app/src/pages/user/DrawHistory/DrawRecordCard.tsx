import { useNavigate } from 'react-router-dom';
import { Badge } from '@/components/ui/badge';
import { formatDate } from '@/utils';
import type { DrawRecordVO } from '@/api/user/drawHistoryApi';
import { ChevronRight } from 'lucide-react';

interface DrawRecordCardProps {
  record: DrawRecordVO;
}

// 目标类型映射
const targetTypeMap: Record<number, string> = {
  1: '单品',
  2: '组合包',
};

// 抽签状态映射
const drawStatusMap: Record<number, { text: string; variant: 'default' | 'secondary' | 'outline' | 'destructive' }> = {
  0: { text: '待开奖', variant: 'default' },
  1: { text: '已中签', variant: 'default' },
  2: { text: '未中签', variant: 'secondary' },
};

export default function DrawRecordCard({ record }: DrawRecordCardProps) {
  const navigate = useNavigate();
  const isWon = record.status === 1;
  const isPending = record.status === 0;
  const isNotWon = record.status === 2;

  // 点击卡片查看详情
  const handleClick = () => {
    // 跳转到抽签详情页，使用 drawId
    navigate(`/user/draw-history/detail/${record.drawId}`);
  };

  return (
    <div
      onClick={handleClick}
      className={`bg-white rounded-xl overflow-hidden cursor-pointer active:scale-[0.99] transition-transform ${
        isNotWon ? 'opacity-60' : ''
      }`}
    >
      <div className="p-4">
        {/* 头部：标题和状态 */}
        <div className="flex items-start justify-between mb-3">
          <h3 className="font-medium text-xs flex-1 pr-2">{record.drawTitle}</h3>
          <div className="flex items-center gap-1">
            <Badge
              variant={drawStatusMap[record.status].variant}
              className={
                isWon
                  ? 'bg-green-500 hover:bg-green-600 text-white'
                  : isPending
                  ? 'bg-yellow-500 hover:bg-yellow-600 text-white'
                  : ''
              }
            >
              {drawStatusMap[record.status].text}
            </Badge>
            <ChevronRight className="w-4 h-4 text-gray-400" />
          </div>
        </div>

        {/* 详细信息 */}
        <div className="space-y-2 text-xs text-gray-600">
          {/* 目标类型 */}
          <div className="flex items-center">
            <span className="text-gray-500 w-20">类型：</span>
            <span>{targetTypeMap[record.targetType] || '未知'}</span>
          </div>

          {/* 鞋码 */}
          {record.shoeSize && (
            <div className="flex items-center">
              <span className="text-gray-500 w-20">鞋码：</span>
              <span>{record.shoeSize}</span>
            </div>
          )}

          {/* 参与时间 */}
          <div className="flex items-center">
            <span className="text-gray-500 w-20">参与时间：</span>
            <span>{formatDate(record.createTime, 'yyyy-MM-dd HH:mm')}</span>
          </div>

          {/* 订单号（仅中签时显示） */}
          {isWon && record.orderNo && (
            <div className="flex items-center">
              <span className="text-gray-500 w-20">订单号：</span>
              <span className="text-green-600 font-medium">{record.orderNo}</span>
            </div>
          )}
        </div>
      </div>

      {/* 底部装饰线 */}
      <div className="h-px bg-gradient-to-r from-transparent via-gray-200 to-transparent" />
    </div>
  );
}
