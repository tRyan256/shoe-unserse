import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, MapPin } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { addressApi } from '@/api';
import type { Address } from '@/types';
import { ADDRESS_LABELS } from '@/constants';

export default function AddressEditPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = Boolean(id);

  const [form, setForm] = useState<Partial<Address>>({
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    label: '',
    isDefault: false,
  });
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (id) {
      loadAddress(id);
    }
  }, [id]);

  const loadAddress = async (addressId: string) => {
    try {
      const data = await addressApi.getAddressDetail(Number(addressId));
      if (data) {
        setForm(data);
      }
    } catch (error) {
      console.error('Failed to load address:', error);
    }
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!form.consignee?.trim()) {
      newErrors.consignee = '请输入收货人姓名';
    }
    if (!form.phone?.trim()) {
      newErrors.phone = '请输入手机号';
    } else if (!/^1[3-9]\d{9}$/.test(form.phone)) {
      newErrors.phone = '请输入正确的手机号';
    }
    if (!form.province?.trim()) {
      newErrors.province = '请输入省份';
    }
    if (!form.city?.trim()) {
      newErrors.city = '请输入城市';
    }
    if (!form.district?.trim()) {
      newErrors.district = '请输入区/县';
    }
    if (!form.detail?.trim()) {
      newErrors.detail = '请输入详细地址';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validate()) return;

    setIsLoading(true);
    try {
      if (isEdit && id) {
        await addressApi.updateAddress({ ...form, id: Number(id) } as Address);
      } else {
        await addressApi.addAddress(form as Omit<Address, 'id'>);
      }
      navigate('/address/list');
    } catch (error) {
      console.error('Failed to save address:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (field: keyof Address, value: any) => {
    setForm((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: '' }));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 头部 */}
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">
          {isEdit ? '编辑地址' : '新增地址'}
        </h1>
        <div className="w-6" />
      </header>

      {/* 表单 */}
      <div className="flex-1 p-4 space-y-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-xl p-4 space-y-4"
        >
          {/* 收货人 */}
          <div className="space-y-2">
            <Label>收货人</Label>
            <Input
              placeholder="请输入收货人姓名"
              value={form.consignee}
              onChange={(e) => handleChange('consignee', e.target.value)}
            />
            {errors.consignee && (
              <p className="text-sm text-red-500">{errors.consignee}</p>
            )}
          </div>

          {/* 手机号 */}
          <div className="space-y-2">
            <Label>手机号</Label>
            <Input
              type="tel"
              placeholder="请输入手机号"
              value={form.phone}
              onChange={(e) => handleChange('phone', e.target.value.replace(/\D/g, '').slice(0, 11))}
              maxLength={11}
            />
            {errors.phone && (
              <p className="text-sm text-red-500">{errors.phone}</p>
            )}
          </div>

          {/* 省市区 */}
          <div className="grid grid-cols-3 gap-3">
            <div className="space-y-2">
              <Label>省份</Label>
              <Input
                placeholder="省"
                value={form.province}
                onChange={(e) => handleChange('province', e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>城市</Label>
              <Input
                placeholder="市"
                value={form.city}
                onChange={(e) => handleChange('city', e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>区/县</Label>
              <Input
                placeholder="区/县"
                value={form.district}
                onChange={(e) => handleChange('district', e.target.value)}
              />
            </div>
          </div>
          {(errors.province || errors.city || errors.district) && (
            <p className="text-sm text-red-500">请填写完整的省市区信息</p>
          )}

          {/* 详细地址 */}
          <div className="space-y-2">
            <Label>详细地址</Label>
            <Input
              placeholder="请输入街道、门牌号等详细地址"
              value={form.detail}
              onChange={(e) => handleChange('detail', e.target.value)}
            />
            {errors.detail && (
              <p className="text-sm text-red-500">{errors.detail}</p>
            )}
          </div>

          {/* 地址标签 */}
          <div className="space-y-2">
            <Label>地址标签</Label>
            <div className="flex gap-3">
              {ADDRESS_LABELS.map((label) => (
                <button
                  key={label.value}
                  onClick={() => handleChange('label', label.value)}
                  className={`px-4 py-2 rounded-full text-sm transition-colors ${
                    form.label === label.value
                      ? 'bg-black text-white'
                      : 'bg-gray-100 text-gray-600'
                  }`}
                >
                  {label.label}
                </button>
              ))}
            </div>
          </div>

          {/* 默认地址 */}
          <div className="flex items-center justify-between pt-2">
            <Label>设为默认地址</Label>
            <Switch
              checked={form.isDefault}
              onCheckedChange={(checked) => handleChange('isDefault', checked)}
            />
          </div>
        </motion.div>
      </div>

      {/* 底部按钮 */}
      <div className="bg-white border-t border-gray-100 p-4 safe-area-bottom">
        <Button
          className="w-full"
          onClick={handleSubmit}
          disabled={isLoading}
        >
          {isLoading ? '保存中...' : '保存'}
        </Button>
      </div>
    </div>
  );
}
