import { useState, useEffect, useRef } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Upload, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { commonApi } from '@/api';
import type { UserPersonalInfoVO, UserPersonalInfoUpdateDTO } from '@/api/user/personalInfoApi';

interface EditFormProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialData: UserPersonalInfoVO;
  onSave: (data: UserPersonalInfoUpdateDTO) => Promise<void>;
  isSubmitting: boolean;
}

export default function EditForm({ open, onOpenChange, initialData, onSave, isSubmitting }: EditFormProps) {
  const [formData, setFormData] = useState<UserPersonalInfoUpdateDTO>({
    name: initialData.name,
    phone: initialData.phone,
    sex: initialData.sex,
    idNumber: initialData.idNumber,
    avatar: initialData.avatar,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isUploadingAvatar, setIsUploadingAvatar] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (open) {
      setFormData({
        name: initialData.name,
        phone: initialData.phone,
        sex: initialData.sex,
        idNumber: initialData.idNumber,
        avatar: initialData.avatar,
      });
      setErrors({});
    }
  }, [open, initialData]);

  const handleAvatarClick = () => {
    fileInputRef.current?.click();
  };

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      toast.error('请选择图片文件');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      toast.error('图片大小不能超过5MB');
      return;
    }

    try {
      setIsUploadingAvatar(true);
      const avatarUrl = await commonApi.uploadImage(file);
      setFormData((prev) => ({ ...prev, avatar: avatarUrl }));
      toast.success('头像上传成功');
    } catch (error) {
      console.error('Failed to upload avatar:', error);
      toast.error('头像上传失败，请重试');
    } finally {
      setIsUploadingAvatar(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.name?.trim()) {
      newErrors.name = '请输入姓名';
    }

    if (!formData.phone?.trim()) {
      newErrors.phone = '请输入手机号';
    } else if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
      newErrors.phone = '请输入有效的手机号';
    }

    if (formData.idNumber && !/^[1-9]\d{16}[\dXx]$/.test(formData.idNumber)) {
      newErrors.idNumber = '请输入有效的身份证号';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    await onSave(formData);
  };

  const handleChange = (field: keyof UserPersonalInfoUpdateDTO, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>编辑个人信息</DialogTitle>
          <DialogDescription className="sr-only">修改个人资料并保存</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            {/* 头像 */}
            <div className="flex flex-col items-center gap-2">
              <Avatar className="w-20 h-20 cursor-pointer relative" onClick={handleAvatarClick}>
                <AvatarImage src={formData.avatar} />
                <AvatarFallback className="text-xl bg-emerald-600 text-white">
                  {formData.name?.[0] || '👤'}
                </AvatarFallback>
                {isUploadingAvatar && (
                  <div className="absolute inset-0 bg-black/50 rounded-full flex items-center justify-center">
                    <Loader2 className="w-6 h-6 text-white animate-spin" />
                  </div>
                )}
              </Avatar>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handleAvatarChange}
              />
              <Button
                type="button"
                variant="outline"
                size="sm"
                className="text-xs"
                onClick={handleAvatarClick}
                disabled={isUploadingAvatar || isSubmitting}
              >
                {isUploadingAvatar ? (
                  <>
                    <Loader2 className="w-3 h-3 mr-1 animate-spin" />
                    上传中...
                  </>
                ) : (
                  <>
                    <Upload className="w-3 h-3 mr-1" />
                    更换头像
                  </>
                )}
              </Button>
              <p className="text-xs text-gray-500">支持 jpg、png 格式，最大 5MB</p>
            </div>

            {/* 姓名 */}
            <div className="space-y-2">
              <Label htmlFor="name">
                姓名 <span className="text-red-500">*</span>
              </Label>
              <Input
                id="name"
                value={formData.name || ''}
                onChange={(e) => handleChange('name', e.target.value)}
                placeholder="请输入姓名"
                className={errors.name ? 'border-red-500' : ''}
              />
              {errors.name && (
                <p className="text-xs text-red-500">{errors.name}</p>
              )}
            </div>

            {/* 手机号 */}
            <div className="space-y-2">
              <Label htmlFor="phone">
                手机号 <span className="text-red-500">*</span>
              </Label>
              <Input
                id="phone"
                value={formData.phone || ''}
                onChange={(e) => handleChange('phone', e.target.value)}
                placeholder="请输入手机号"
                maxLength={11}
                className={errors.phone ? 'border-red-500' : ''}
              />
              {errors.phone && (
                <p className="text-xs text-red-500">{errors.phone}</p>
              )}
            </div>

            {/* 性别 */}
            <div className="space-y-2">
              <Label>性别</Label>
              <RadioGroup
                value={formData.sex || ''}
                onValueChange={(value) => handleChange('sex', value)}
              >
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem value="1" id="male" />
                    <Label htmlFor="male" className="font-normal cursor-pointer">
                      男
                    </Label>
                  </div>
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem value="2" id="female" />
                    <Label htmlFor="female" className="font-normal cursor-pointer">
                      女
                    </Label>
                  </div>
                </div>
              </RadioGroup>
            </div>

            {/* 身份证号 */}
            <div className="space-y-2">
              <Label htmlFor="idNumber">身份证号</Label>
              <Input
                id="idNumber"
                value={formData.idNumber || ''}
                onChange={(e) => handleChange('idNumber', e.target.value.toUpperCase())}
                placeholder="请输入身份证号"
                maxLength={18}
                className={errors.idNumber ? 'border-red-500' : ''}
              />
              {errors.idNumber && (
                <p className="text-xs text-red-500">{errors.idNumber}</p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSubmitting}
            >
              取消
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? '保存中...' : '保存'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

