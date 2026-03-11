import { Badge } from '@/components/ui/badge';
import { formatDate, formatPrice } from '@/utils';
import type { AirdropRecordVO } from '@/api/user/airdropHistoryApi';

interface AirdropRecordCardProps {
  record: AirdropRecordVO;
}

export default function AirdropRecordCard({ record }: AirdropRecordCardProps) {
  return (
    <div className="bg-white rounded-xl overflow-hidden">
      <div className="p-4">
        {/* 头部：活动标题和状态 */}
        <div className="flex items-start justify-between mb-3">
          <h3 className="font-medium text-sm flex-1 pr-2">{record.airdropTitle}</h3>
          <Badge variant="default" className="bg-green-500 hover:bg-green-600 text-white">
            已领取
          </Badge>
        </div>

        {/* 详细信息 */}
        <div className="space-y-2 text-xs text-gray-600">
          {/* 优惠券名称 */}
          <div className="flex items-center">
            <span className="text-gray-500 w-20">优惠券：</span>
            <span className="font-medium text-gray-900">{record.couponName}</span>
          </div>

          {/* 优惠券价值 */}
          <div className="flex items-center">
            <span className="text-gray-500 w-20">价值：</span>
            <span className="text-red-600 font-medium">{formatPrice(record.couponValue)}</span>
          </div>

          {/* 领取时间 */}
          <div className="flex items-center">
            <span className="text-gray-500 w-20">领取时间：</span>
            <span>{formatDate(record.createTime, 'yyyy-MM-dd HH:mm')}</span>
          </div>
        </div>
      </div>

      {/* 底部装饰线 */}
      <div className="h-px bg-gradient-to-r from-transparent via-gray-200 to-transparent" />
    </div>
  );
}
