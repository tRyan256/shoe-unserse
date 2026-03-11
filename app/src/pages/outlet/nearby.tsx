import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, MapPin, Phone, Clock, Navigation, Loader2, RefreshCw } from 'lucide-react';
import { outletApi } from '@/api';
import { toast } from 'sonner';
import type { Outlet } from '@/types';

export default function NearbyOutletPage() {
  const navigate = useNavigate();
  const [outlets, setOutlets] = useState<Outlet[]>([]);
  const [loading, setLoading] = useState(true);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [currentLocation, setCurrentLocation] = useState<{ longitude: number; latitude: number } | null>(null);

  const getLocationAndFetchOutlets = async () => {
    setLoading(true);
    setLocationError(null);

    if (!navigator.geolocation) {
      fetchOutletsWithoutLocation();
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const { longitude, latitude } = position.coords;
        setCurrentLocation({ longitude, latitude });

        try {
          const data = await outletApi.getNearbyOutlets({
            longitude,
            latitude,
            radiusMeters: 5000000,
            limit: 100,
          });
          setOutlets(data || []);
        } catch (error) {
          console.error('Failed to fetch outlets:', error);
          toast.error('获取门店列表失败');
        } finally {
          setLoading(false);
        }
      },
      () => {
        fetchOutletsWithoutLocation();
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000,
      }
    );
  };

  const fetchOutletsWithoutLocation = async () => {
    try {
      const data = await outletApi.getNearbyOutlets({
        longitude: 116.397128,
        latitude: 39.916527,
        radiusMeters: 5000000,
        limit: 100,
      });
      setOutlets(data || []);
    } catch (error) {
      console.error('Failed to fetch outlets:', error);
      toast.error('获取门店列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    getLocationAndFetchOutlets();
  }, []);

  const formatDistance = (distance?: number) => {
    if (distance === undefined) return '';
    if (distance < 1000) {
      return `${Math.round(distance)}m`;
    }
    return `${(distance / 1000).toFixed(1)}km`;
  };

  const openNavigation = (outlet: Outlet) => {
    const { longitude, latitude, name, address } = outlet;
    const url = `https://uri.amap.com/navigation?to=${longitude},${latitude},${name}&mode=car&policy=1&src=myapp&coordinate=gaode`;
    window.open(url, '_blank');
  };

  const callPhone = (phone: string) => {
    window.location.href = `tel:${phone}`;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="sticky top-0 z-40 bg-white border-b border-gray-100">
        <div className="flex items-center justify-between h-12 px-4">
          <button onClick={() => navigate(-1)} className="p-2 -ml-2">
            <ArrowLeft className="w-5 h-5" />
          </button>
          <span className="text-sm font-medium">全国门店</span>
          <button
            onClick={getLocationAndFetchOutlets}
            className="p-2 -mr-2"
            disabled={loading}
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </header>

      {loading && (
        <div className="flex flex-col items-center justify-center py-20">
          <Loader2 className="w-6 h-6 animate-spin text-emerald-600" />
          <p className="mt-3 text-xs text-gray-500">正在加载门店...</p>
        </div>
      )}

      {!loading && outlets.length === 0 && (
        <div className="flex flex-col items-center justify-center py-20 px-4">
          <div className="w-14 h-14 bg-gray-100 rounded-full flex items-center justify-center mb-3">
            <MapPin className="w-7 h-7 text-gray-300" />
          </div>
          <p className="text-sm text-gray-500">暂无门店信息</p>
        </div>
      )}

      {!loading && outlets.length > 0 && (
        <div className="p-3 space-y-2">
          {currentLocation && (
            <div className="flex items-center gap-1.5 text-[10px] text-gray-400 mb-1">
              <Navigation className="w-3 h-3" />
              <span>已获取您的位置，按距离排序</span>
            </div>
          )}

          {outlets.map((outlet, index) => (
            <motion.div
              key={outlet.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.03 }}
              className="bg-white rounded-lg p-3 shadow-sm"
            >
              <div className="flex justify-between items-start mb-1.5">
                <h3 className="text-sm font-medium text-gray-900">{outlet.name}</h3>
                {outlet.distance !== undefined && (
                  <span className="text-[10px] text-emerald-600 font-medium">
                    {formatDistance(outlet.distance)}
                  </span>
                )}
              </div>

              <div className="space-y-1.5 text-[10px] text-gray-500">
                <div className="flex items-start gap-1.5">
                  <MapPin className="w-3 h-3 mt-0.5 flex-shrink-0" />
                  <span>{outlet.address}</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <Clock className="w-3 h-3 flex-shrink-0" />
                  <span>{outlet.businessHours || '营业中'}</span>
                </div>
              </div>

              <div className="flex gap-2 mt-3">
                <button
                  onClick={() => callPhone(outlet.phone)}
                  className="flex-1 flex items-center justify-center gap-1 py-1.5 border border-gray-200 rounded-md text-[10px] text-gray-600 hover:bg-gray-50 transition-colors"
                >
                  <Phone className="w-3 h-3" />
                  <span>电话</span>
                </button>
                <button
                  onClick={() => openNavigation(outlet)}
                  className="flex-1 flex items-center justify-center gap-1 py-1.5 bg-emerald-600 text-white rounded-md text-[10px] hover:bg-emerald-700 transition-colors"
                >
                  <Navigation className="w-3 h-3" />
                  <span>导航</span>
                </button>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
