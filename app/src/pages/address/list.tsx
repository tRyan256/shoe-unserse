import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Plus, MapPin } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { AddressCard } from '@/components/shared';
import { addressApi } from '@/api';
import type { Address } from '@/types';

export default function AddressListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const selectMode = searchParams.get('mode') === 'select';
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadAddresses();
  }, []);

  const loadAddresses = async () => {
    try {
      setIsLoading(true);
      const data = await addressApi.getAddressList();
      setAddresses(data);
    } catch (error) {
      console.error('Failed to load addresses:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelect = (id: number) => {
    if (selectMode) {
      const returnUrl = searchParams.get('returnUrl') || '/order/confirm';
      navigate(`${returnUrl}?addressId=${id}`, { replace: true });
    } else {
      navigate(`/address/edit/${id}`);
    }
  };

  const handleSetDefault = async (id: number) => {
    try {
      await addressApi.setDefaultAddress(id);
      loadAddresses();
    } catch (error) {
      console.error('Failed to set default address:', error);
    }
  };

  const handleEdit = (id: number) => {
    navigate(`/address/edit/${id}`);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-black border-t-transparent rounded-full" />
      </div>
    );
  }

  if (addresses.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        <header className="flex items-center px-4 h-14 bg-white">
          <button onClick={() => navigate(-1)}>
            <ArrowLeft className="w-6 h-6" />
          </button>
          <h1 className="flex-1 text-center text-lg font-bold">收货地址</h1>
          <div className="w-6" />
        </header>

        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-6">
            <MapPin className="w-10 h-10 text-gray-300" />
          </div>
          <h2 className="text-lg font-medium text-gray-900 mb-2">还没有收货地址</h2>
          <p className="text-sm text-gray-500 mb-6">添加地址，方便购物</p>
          <Button onClick={() => navigate('/address/edit')}>
            <Plus className="w-4 h-4 mr-2" />
            新增地址
          </Button>
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
        <h1 className="flex-1 text-center text-lg font-bold">收货地址</h1>
        <div className="w-6" />
      </header>

      {/* 地址列表 */}
      <div className="flex-1 p-4 space-y-3">
        {addresses.map((address, index) => (
          <motion.div
            key={address.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <AddressCard
              address={address}
              isSelected={false}
              showActions={!selectMode}
              onSelect={handleSelect}
              onEdit={handleEdit}
              onSetDefault={handleSetDefault}
            />
          </motion.div>
        ))}
      </div>

      {/* 底部添加按钮 */}
      <div className="bg-white border-t border-gray-100 p-4 safe-area-bottom">
        <Button
          className="w-full"
          onClick={() => navigate('/address/edit')}
        >
          <Plus className="w-4 h-4 mr-2" />
          新增地址
        </Button>
      </div>
    </div>
  );
}
