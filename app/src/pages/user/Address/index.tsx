import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Plus } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { listAddresses, deleteAddress, setDefaultAddress, type AddressBook } from '@/api/user/addressApi';
import AddressCard from './AddressCard';
import AddressForm from './AddressForm';

export default function AddressPage() {
  const navigate = useNavigate();
  const [addresses, setAddresses] = useState<AddressBook[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState<AddressBook | null>(null);

  useEffect(() => {
    loadAddresses();
  }, []);

  const loadAddresses = async () => {
    try {
      setIsLoading(true);
      const data = await listAddresses();
      setAddresses(data);
    } catch (error) {
      console.error('Failed to load addresses:', error);
      toast.error('加载地址失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingAddress(null);
    setIsFormOpen(true);
  };

  const handleEdit = (address: AddressBook) => {
    setEditingAddress(address);
    setIsFormOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteAddress(id);
      toast.success('删除成功');
      await loadAddresses();
    } catch (error) {
      console.error('Failed to delete address:', error);
      toast.error('删除失败，请重试');
    }
  };

  const handleSetDefault = async (id: number) => {
    try {
      await setDefaultAddress(id);
      toast.success('设置成功');
      await loadAddresses();
    } catch (error) {
      console.error('Failed to set default address:', error);
      toast.error('设置失败，请重试');
    }
  };

  const handleFormSuccess = () => {
    setIsFormOpen(false);
    setEditingAddress(null);
    loadAddresses();
  };

  if (isLoading) {
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
        <h1 className="flex-1 text-center text-lg font-bold">收货地址</h1>
        <Button
          variant="ghost"
          size="sm"
          onClick={handleAdd}
          className="text-emerald-700 hover:text-emerald-800"
        >
          <Plus className="w-4 h-4 mr-1" />
          添加
        </Button>
      </header>

      {/* 地址列表 */}
      <div className="flex-1 p-4 space-y-3">
        {addresses.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500 mb-4">暂无收货地址</p>
            <Button onClick={handleAdd} className="bg-emerald-700 hover:bg-emerald-800">
              <Plus className="w-4 h-4 mr-2" />
              添加地址
            </Button>
          </div>
        ) : (
          addresses.map((address, index) => (
            <motion.div
              key={address.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
            >
              <AddressCard
                address={address}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onSetDefault={handleSetDefault}
              />
            </motion.div>
          ))
        )}
      </div>

      {/* 添加/编辑表单 */}
      <AddressForm
        open={isFormOpen}
        onOpenChange={setIsFormOpen}
        address={editingAddress}
        onSuccess={handleFormSuccess}
      />
    </div>
  );
}


