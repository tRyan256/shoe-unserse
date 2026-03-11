import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Edit2, User, Phone, IdCard, Image as ImageIcon, ChevronRight } from 'lucide-react';
import { toast } from 'sonner';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { useUserStore } from '@/stores';
import { getPersonalInfo, updatePersonalInfo, type UserPersonalInfoVO, type UserPersonalInfoUpdateDTO } from '@/api/user/personalInfoApi';
import EditForm from './EditForm';

export default function PersonalInfoPage() {
  const navigate = useNavigate();
  const { user, setUser } = useUserStore();
  const [userInfo, setUserInfo] = useState<UserPersonalInfoVO | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    loadPersonalInfo();
  }, []);

  const loadPersonalInfo = async () => {
    try {
      setIsLoading(true);
      const data = await getPersonalInfo();
      setUserInfo(data);
    } catch (error) {
      console.error('Failed to load personal info:', error);
      toast.error('加载个人信息失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = () => {
    setIsEditOpen(true);
  };

  const handleSave = async (data: UserPersonalInfoUpdateDTO) => {
    try {
      setIsSubmitting(true);
      await updatePersonalInfo(data);
      toast.success('保存成功');
      setIsEditOpen(false);
      await loadPersonalInfo();
      if (data.avatar && user) {
        setUser({ ...user, avatar: data.avatar, nickname: data.name || user.nickname });
      } else if (data.name && user) {
        setUser({ ...user, nickname: data.name });
      }
    } catch (error) {
      console.error('Failed to update personal info:', error);
      toast.error('保存失败，请重试');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner className="w-8 h-8" />
      </div>
    );
  }

  if (!userInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-500">加载失败</p>
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
        <h1 className="flex-1 text-center text-lg font-bold">个人信息</h1>
        <Button
          variant="ghost"
          size="sm"
          onClick={handleEdit}
          className="text-emerald-700 hover:text-emerald-800"
        >
          <Edit2 className="w-4 h-4 mr-1" />
          编辑
        </Button>
      </header>

      {/* 内容 */}
      <div className="flex-1 p-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-lg overflow-hidden"
        >
          {/* 头像 */}
          <div 
            className="flex items-center justify-between p-4 border-b border-gray-100 cursor-pointer hover:bg-gray-50 active:bg-gray-100 transition-colors"
            onClick={handleEdit}
          >
            <div className="flex items-center gap-3">
              <ImageIcon className="w-5 h-5 text-gray-400" />
              <span className="text-sm text-gray-700">头像</span>
            </div>
            <div className="flex items-center gap-2">
              <Avatar className="w-16 h-16">
                <AvatarImage src={userInfo.avatar} />
                <AvatarFallback className="text-lg bg-emerald-600 text-white">
                  {userInfo.name?.[0] || '👤'}
                </AvatarFallback>
              </Avatar>
              <ChevronRight className="w-4 h-4 text-gray-400" />
            </div>
          </div>

          {/* 姓名 */}
          <div className="flex items-center justify-between p-4 border-b border-gray-100">
            <div className="flex items-center gap-3">
              <User className="w-5 h-5 text-gray-400" />
              <span className="text-sm text-gray-700">姓名</span>
            </div>
            <span className="text-sm text-gray-900">{userInfo.name || '-'}</span>
          </div>

          {/* 手机号 */}
          <div className="flex items-center justify-between p-4 border-b border-gray-100">
            <div className="flex items-center gap-3">
              <Phone className="w-5 h-5 text-gray-400" />
              <span className="text-sm text-gray-700">手机号</span>
            </div>
            <span className="text-sm text-gray-900">{userInfo.phone || '-'}</span>
          </div>

          {/* 性别 */}
          <div className="flex items-center justify-between p-4 border-b border-gray-100">
            <div className="flex items-center gap-3">
              <User className="w-5 h-5 text-gray-400" />
              <span className="text-sm text-gray-700">性别</span>
            </div>
            <span className="text-sm text-gray-900">
              {userInfo.sex === '1' ? '男' : userInfo.sex === '2' ? '女' : '-'}
            </span>
          </div>

          {/* 身份证号 */}
          <div className="flex items-center justify-between p-4">
            <div className="flex items-center gap-3">
              <IdCard className="w-5 h-5 text-gray-400" />
              <span className="text-sm text-gray-700">身份证号</span>
            </div>
            <span className="text-sm text-gray-900">{userInfo.idNumber || '-'}</span>
          </div>
        </motion.div>
      </div>

      {/* 编辑表单对话框 */}
      <EditForm
        open={isEditOpen}
        onOpenChange={setIsEditOpen}
        initialData={userInfo}
        onSave={handleSave}
        isSubmitting={isSubmitting}
      />
    </div>
  );
}


