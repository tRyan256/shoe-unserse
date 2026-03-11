import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { addAddress, updateAddress, type AddressBook, type AddressBookDTO } from '@/api/user/addressApi';
import { regions, getCitiesByProvince, getDistrictsByCity, type Region } from '@/data/regions';

interface AddressFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  address: AddressBook | null;
  onSuccess: () => void;
}

export default function AddressForm({ open, onOpenChange, address, onSuccess }: AddressFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState<AddressBookDTO>({
    consignee: '',
    phone: '',
    sex: '1',
    provinceCode: '',
    provinceName: '',
    cityCode: '',
    cityName: '',
    districtCode: '',
    districtName: '',
    detail: '',
    label: '',
    isDefault: 0,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [cities, setCities] = useState<Region[]>([]);
  const [districts, setDistricts] = useState<Region[]>([]);

  useEffect(() => {
    if (address) {
      setFormData({
        consignee: address.consignee,
        phone: address.phone,
        sex: address.sex,
        provinceCode: address.provinceCode,
        provinceName: address.provinceName,
        cityCode: address.cityCode,
        cityName: address.cityName,
        districtCode: address.districtCode,
        districtName: address.districtName,
        detail: address.detail,
        label: address.label || '',
        isDefault: address.isDefault,
      });
      if (address.provinceCode) {
        setCities(getCitiesByProvince(address.provinceCode));
      }
      if (address.provinceCode && address.cityCode) {
        setDistricts(getDistrictsByCity(address.provinceCode, address.cityCode));
      }
    } else {
      setFormData({
        consignee: '',
        phone: '',
        sex: '1',
        provinceCode: '',
        provinceName: '',
        cityCode: '',
        cityName: '',
        districtCode: '',
        districtName: '',
        detail: '',
        label: '',
        isDefault: 0,
      });
      setCities([]);
      setDistricts([]);
    }
    setErrors({});
  }, [address, open]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.consignee.trim()) {
      newErrors.consignee = '请输入收货人姓名';
    }

    if (!formData.phone.trim()) {
      newErrors.phone = '请输入手机号';
    } else if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
      newErrors.phone = '请输入正确的手机号';
    }

    if (!formData.provinceCode) {
      newErrors.provinceName = '请选择省份';
    }

    if (!formData.cityCode) {
      newErrors.cityName = '请选择城市';
    }

    if (!formData.districtCode) {
      newErrors.districtName = '请选择区县';
    }

    if (!formData.detail.trim()) {
      newErrors.detail = '请输入详细地址';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setIsSubmitting(true);
      
      const dataToSubmit = {
        ...formData,
        provinceCode: formData.provinceCode,
        cityCode: formData.cityCode,
        districtCode: formData.districtCode,
      };

      if (address) {
        await updateAddress(address.id, dataToSubmit);
        toast.success('更新成功');
      } else {
        await addAddress(dataToSubmit);
        toast.success('添加成功');
      }
      
      onSuccess();
    } catch (error) {
      console.error('Failed to save address:', error);
      toast.error('保存失败，请重试');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (field: keyof AddressBookDTO, value: string | number) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleProvinceChange = (provinceCode: string) => {
    const province = regions.find((p) => p.code === provinceCode);
    const newCities = getCitiesByProvince(provinceCode);
    
    setFormData((prev) => ({
      ...prev,
      provinceCode,
      provinceName: province?.name || '',
      cityCode: '',
      cityName: '',
      districtCode: '',
      districtName: '',
    }));
    setCities(newCities);
    setDistricts([]);
    
    if (errors.provinceName) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors.provinceName;
        delete newErrors.cityName;
        delete newErrors.districtName;
        return newErrors;
      });
    }
  };

  const handleCityChange = (cityCode: string) => {
    const city = cities.find((c) => c.code === cityCode);
    const newDistricts = getDistrictsByCity(formData.provinceCode, cityCode);
    
    setFormData((prev) => ({
      ...prev,
      cityCode,
      cityName: city?.name || '',
      districtCode: '',
      districtName: '',
    }));
    setDistricts(newDistricts);
    
    if (errors.cityName) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors.cityName;
        delete newErrors.districtName;
        return newErrors;
      });
    }
  };

  const handleDistrictChange = (districtCode: string) => {
    const district = districts.find((d) => d.code === districtCode);
    
    setFormData((prev) => ({
      ...prev,
      districtCode,
      districtName: district?.name || '',
    }));
    
    if (errors.districtName) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors.districtName;
        return newErrors;
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{address ? '编辑地址' : '添加地址'}</DialogTitle>
          <DialogDescription className="sr-only">填写并保存收货地址信息</DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* 收货人 */}
          <div>
            <Label htmlFor="consignee">收货人 *</Label>
            <Input
              id="consignee"
              value={formData.consignee}
              onChange={(e) => handleChange('consignee', e.target.value)}
              placeholder="请输入收货人姓名"
              className={errors.consignee ? 'border-red-500' : ''}
            />
            {errors.consignee && (
              <p className="text-xs text-red-500 mt-1">{errors.consignee}</p>
            )}
          </div>

          {/* 手机号 */}
          <div>
            <Label htmlFor="phone">手机号 *</Label>
            <Input
              id="phone"
              value={formData.phone}
              onChange={(e) => handleChange('phone', e.target.value)}
              placeholder="请输入手机号"
              maxLength={11}
              className={errors.phone ? 'border-red-500' : ''}
            />
            {errors.phone && (
              <p className="text-xs text-red-500 mt-1">{errors.phone}</p>
            )}
          </div>

          {/* 性别 */}
          <div>
            <Label>性别</Label>
            <RadioGroup
              value={formData.sex}
              onValueChange={(value) => handleChange('sex', value)}
              className="flex gap-4 mt-2"
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="1" id="male" />
                <Label htmlFor="male" className="font-normal cursor-pointer">
                  先生
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="2" id="female" />
                <Label htmlFor="female" className="font-normal cursor-pointer">
                  女士
                </Label>
              </div>
            </RadioGroup>
          </div>

          {/* 省份 */}
          <div>
            <Label>省份 *</Label>
            <Select value={formData.provinceCode} onValueChange={handleProvinceChange}>
              <SelectTrigger className={`w-full ${errors.provinceName ? 'border-red-500' : ''}`}>
                <SelectValue placeholder="请选择省份" />
              </SelectTrigger>
              <SelectContent>
                {regions.map((province) => (
                  <SelectItem key={province.code} value={province.code}>
                    {province.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.provinceName && (
              <p className="text-xs text-red-500 mt-1">{errors.provinceName}</p>
            )}
          </div>

          {/* 城市 */}
          <div>
            <Label>城市 *</Label>
            <Select 
              value={formData.cityCode} 
              onValueChange={handleCityChange}
              disabled={!formData.provinceCode}
            >
              <SelectTrigger className={`w-full ${errors.cityName ? 'border-red-500' : ''}`}>
                <SelectValue placeholder={formData.provinceCode ? '请选择城市' : '请先选择省份'} />
              </SelectTrigger>
              <SelectContent>
                {cities.map((city) => (
                  <SelectItem key={city.code} value={city.code}>
                    {city.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.cityName && (
              <p className="text-xs text-red-500 mt-1">{errors.cityName}</p>
            )}
          </div>

          {/* 区县 */}
          <div>
            <Label>区县 *</Label>
            <Select 
              value={formData.districtCode} 
              onValueChange={handleDistrictChange}
              disabled={!formData.cityCode}
            >
              <SelectTrigger className={`w-full ${errors.districtName ? 'border-red-500' : ''}`}>
                <SelectValue placeholder={formData.cityCode ? '请选择区县' : '请先选择城市'} />
              </SelectTrigger>
              <SelectContent>
                {districts.map((district) => (
                  <SelectItem key={district.code} value={district.code}>
                    {district.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.districtName && (
              <p className="text-xs text-red-500 mt-1">{errors.districtName}</p>
            )}
          </div>

          {/* 详细地址 */}
          <div>
            <Label htmlFor="detail">详细地址 *</Label>
            <Textarea
              id="detail"
              value={formData.detail}
              onChange={(e) => handleChange('detail', e.target.value)}
              placeholder="请输入详细地址（街道、门牌号等）"
              rows={3}
              className={errors.detail ? 'border-red-500' : ''}
            />
            {errors.detail && (
              <p className="text-xs text-red-500 mt-1">{errors.detail}</p>
            )}
          </div>

          {/* 地址标签 */}
          <div>
            <Label htmlFor="label">地址标签（可选）</Label>
            <Input
              id="label"
              value={formData.label}
              onChange={(e) => handleChange('label', e.target.value)}
              placeholder="如：家、公司、学校"
            />
          </div>

          {/* 提交按钮 */}
          <div className="flex gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="flex-1"
              disabled={isSubmitting}
            >
              取消
            </Button>
            <Button
              type="submit"
              className="flex-1 bg-emerald-700 hover:bg-emerald-800"
              disabled={isSubmitting}
            >
              {isSubmitting ? '保存中...' : '保存'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}


