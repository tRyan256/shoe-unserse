import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { formatCountdown } from '@/utils';

interface CountdownTimerProps {
  targetTime: string;
  onEnd?: () => void;
}

export function CountdownTimer({ targetTime, onEnd }: CountdownTimerProps) {
  const [remaining, setRemaining] = useState(0);

  useEffect(() => {
    const target = new Date(targetTime).getTime();

    const updateRemaining = () => {
      const now = Date.now();
      const diff = Math.max(0, Math.floor((target - now) / 1000));
      setRemaining(diff);

      if (diff === 0) {
        onEnd?.();
      }
    };

    updateRemaining();
    const interval = setInterval(updateRemaining, 1000);

    return () => clearInterval(interval);
  }, [targetTime, onEnd]);

  const timeStr = formatCountdown(remaining);
  const parts = timeStr.split(':');

  return (
    <div className="flex items-center gap-1 font-mono">
      {parts.map((part, index) => (
        <div key={index} className="flex items-center">
          <motion.span
            key={part}
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            className="inline-flex items-center justify-center min-w-[2ch] px-2 py-1 bg-black text-white rounded text-lg font-bold"
          >
            {part}
          </motion.span>
          {index < parts.length - 1 && (
            <span className="mx-1 text-black font-bold">:</span>
          )}
        </div>
      ))}
    </div>
  );
}
