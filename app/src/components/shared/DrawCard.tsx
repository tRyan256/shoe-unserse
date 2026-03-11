import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import type { Draw } from '@/types';
import { drawStatusMap, drawParticipantStatusMap } from '@/utils';
import { formatDate } from '@/utils';

interface DrawCardProps {
  draw: Draw;
  showButton?: boolean;
}

export function DrawCard({ draw, showButton = true }: DrawCardProps) {
  const status = drawStatusMap[draw.status];
  const participantStatus = draw.participantStatus
    ? drawParticipantStatusMap[draw.participantStatus]
    : null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-white rounded-xl overflow-hidden shadow-sm"
    >
      {/* 活动图片 */}
      <div className="relative aspect-video overflow-hidden">
        <img
          src={draw.image}
          alt={draw.name}
          className="w-full h-full object-cover"
        />
        <div className="absolute top-3 left-3">
          <Badge className={`${status.color.replace('text-', 'bg-')} text-white`}>
            {status.text}
          </Badge>
        </div>
        {participantStatus && (
          <div className="absolute top-3 right-3">
            <Badge variant="secondary" className={participantStatus.color}>
              {participantStatus.text}
            </Badge>
          </div>
        )}
      </div>

      {/* 活动信息 */}
      <div className="p-4">
        <h3 className="font-medium text-gray-900 line-clamp-1 mb-2">
          {draw.name}
        </h3>

        <div className="flex items-center gap-3 mb-4">
          <img
            src={draw.shoeImage}
            alt={draw.shoeName}
            className="w-16 h-16 object-cover rounded-lg bg-gray-50"
          />
          <div className="flex-1 min-w-0">
            <p className="text-sm text-gray-600 line-clamp-1">{draw.shoeName}</p>
            <p className="text-xs text-gray-400 mt-1">
              {formatDate(draw.startTime, 'MM-dd HH:mm')} 开始
            </p>
            <p className="text-xs text-gray-400">
              {formatDate(draw.endTime, 'MM-dd HH:mm')} 结束
            </p>
          </div>
        </div>

        {showButton && (
          <Link to={`/draw/detail/${draw.id}`}>
            <Button className="w-full">
              {draw.participantStatus === 'joined' ? '查看详情' : '立即参与'}
            </Button>
          </Link>
        )}
      </div>
    </motion.div>
  );
}
