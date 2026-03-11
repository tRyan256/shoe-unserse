import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft } from 'lucide-react';
import { DrawCard } from '@/components/shared';
import { drawApi } from '@/api';
import type { Draw } from '@/types';

export default function DrawListPage() {
  const navigate = useNavigate();
  const [draws, setDraws] = useState<Draw[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadDraws();
  }, []);

  const loadDraws = async () => {
    try {
      setIsLoading(true);
      const data = await drawApi.getDrawList();
      setDraws(data);
    } catch (error) {
      console.error('Failed to load draws:', error);
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

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 头部 */}
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">限量抽签</h1>
        <div className="w-6" />
      </header>

      {/*  banner */}
      <div className="relative h-40 m-4 rounded-2xl overflow-hidden">
        <img
          src="https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=400&fit=crop"
          alt="draw banner"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-r from-black/70 to-transparent flex items-center">
          <div className="p-6 text-white">
            <h2 className="text-2xl font-bold mb-2">限量抽签</h2>
            <p className="text-sm text-white/80">公平公正的抽签机制</p>
            <p className="text-sm text-white/80">让你有机会获得限量球鞋</p>
          </div>
        </div>
      </div>

      {/* 抽签列表 */}
      <div className="flex-1 px-4 pb-4 space-y-4">
        {draws.map((draw, index) => (
          <motion.div
            key={draw.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <DrawCard draw={draw} />
          </motion.div>
        ))}
      </div>
    </div>
  );
}
