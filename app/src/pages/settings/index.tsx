import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, ChevronRight, Trash2, Bell, Shield, FileText, HelpCircle } from 'lucide-react';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import { useUserStore } from '@/stores';
import { storage } from '@/utils';

const menuItems = [
  { icon: Bell, label: '消息通知', path: '/settings/notifications', hasSwitch: true },
  { icon: Shield, label: '账号安全', path: '/settings/security' },
  { icon: FileText, label: '隐私政策', path: '/privacy' },
  { icon: HelpCircle, label: '帮助与反馈', path: '/help' },
];

export default function SettingsPage() {
  const navigate = useNavigate();
  const { logout } = useUserStore();

  const handleClearCache = () => {
    // 清除缓存逻辑
    alert('缓存已清除');
  };

  const handleLogout = () => {
    logout();
    storage.clear();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* 头部 */}
      <header className="flex items-center px-4 h-14 bg-white">
        <button onClick={() => navigate(-1)}>
          <ArrowLeft className="w-6 h-6" />
        </button>
        <h1 className="flex-1 text-center text-lg font-bold">设置</h1>
        <div className="w-6" />
      </header>

      {/* 菜单列表 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="m-4 bg-white rounded-xl overflow-hidden"
      >
        {menuItems.map((item, index) => (
          <div key={item.path}>
            <button
              onClick={() => navigate(item.path)}
              className="w-full flex items-center justify-between p-4 hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-center gap-3">
                <item.icon className="w-5 h-5 text-gray-600" />
                <span>{item.label}</span>
              </div>
              {item.hasSwitch ? (
                <Switch defaultChecked />
              ) : (
                <ChevronRight className="w-5 h-5 text-gray-400" />
              )}
            </button>
            {index < menuItems.length - 1 && <Separator />}
          </div>
        ))}
      </motion.div>

      {/* 清除缓存 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="mx-4 bg-white rounded-xl overflow-hidden"
      >
        <button
          onClick={handleClearCache}
          className="w-full flex items-center justify-between p-4 hover:bg-gray-50 transition-colors"
        >
          <div className="flex items-center gap-3">
            <Trash2 className="w-5 h-5 text-gray-600" />
            <span>清除缓存</span>
          </div>
          <span className="text-sm text-gray-400">12.5MB</span>
        </button>
      </motion.div>

      {/* 关于 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="mx-4 mt-4 bg-white rounded-xl p-4 text-center"
      >
        <p className="text-lg font-bold">鞋宙</p>
        <p className="text-sm text-gray-400 mt-1">版本 1.0.0</p>
      </motion.div>

      {/* 退出登录 */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="mx-4 mt-4"
      >
        <button
          onClick={handleLogout}
          className="w-full p-4 bg-white rounded-xl text-red-500 font-medium hover:bg-red-50 transition-colors"
        >
          退出登录
        </button>
      </motion.div>
    </div>
  );
}
