import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowLeft, Bell, ShoppingBag, Gift } from 'lucide-react';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { messageApi } from '@/api';
import type { Message } from '@/types';
import { formatDate } from '@/utils';

const tabs = [
  { value: 'all', label: '全部' },
  { value: 'system', label: '系统' },
  { value: 'order', label: '订单' },
  { value: 'activity', label: '活动' },
];

const iconMap: Record<string, React.ElementType> = {
  system: Bell,
  order: ShoppingBag,
  activity: Gift,
};

export default function MessagePage() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<Message[]>([]);
  const [activeTab, setActiveTab] = useState('all');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadMessages();
  }, []);

  const loadMessages = async () => {
    try {
      setIsLoading(true);
      const data = await messageApi.getMessageList();
      setMessages(data);
    } catch (error) {
      console.error('Failed to load messages:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredMessages = messages.filter((m) => {
    if (activeTab === 'all') return true;
    return m.type === activeTab;
  });

  const handleMessageClick = async (message: Message) => {
    if (!message.isRead) {
      try {
        await messageApi.markMessageRead(message.id);
        loadMessages();
      } catch (error) {
        console.error('Failed to mark message as read:', error);
      }
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
        <h1 className="flex-1 text-center text-base font-bold">消息中心</h1>
        <div className="w-6" />
      </header>

      {/* 标签页 */}
      <div className="bg-white border-b border-gray-100">
        <Tabs value={activeTab} onValueChange={setActiveTab}>
          <TabsList className="w-full justify-start h-12 bg-transparent rounded-none px-2">
            {tabs.map((tab) => (
              <TabsTrigger
                key={tab.value}
                value={tab.value}
                className="flex-1 data-[state=active]:bg-transparent data-[state=active]:shadow-none data-[state=active]:border-b-2 data-[state=active]:border-black rounded-none"
              >
                {tab.label}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
      </div>

      {/* 消息列表 */}
      <div className="flex-1 p-4 space-y-3">
        {filteredMessages.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500">暂无消息</p>
          </div>
        ) : (
          filteredMessages.map((message, index) => {
            const Icon = iconMap[message.type];
            return (
              <motion.div
                key={message.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                onClick={() => handleMessageClick(message)}
                className={`bg-white rounded-xl p-4 ${
                  !message.isRead ? 'border-l-4 border-black' : ''
                }`}
              >
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <Icon className="w-5 h-5 text-gray-600" />
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="text-sm font-medium">{message.title}</h3>
                      {!message.isRead && (
                        <Badge className="bg-red-500">未读</Badge>
                      )}
                    </div>
                    <p className="text-xs text-gray-600 line-clamp-2">
                      {message.content}
                    </p>
                    <p className="text-[10px] text-gray-400 mt-2">
                      {formatDate(message.createTime, 'MM-dd HH:mm')}
                    </p>
                  </div>
                </div>
              </motion.div>
            );
          })
        )}
      </div>
    </div>
  );
}
