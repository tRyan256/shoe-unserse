import { useState } from 'react';
import { ArrowLeft, Send } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';

const faqList = [
  {
    question: '如何参与限量款抽签？',
    answer: '进入"活动"页面，切换到"抽签"标签，选择正在进行的抽签活动，点击"参与"即可。当参与人数达到上限或活动时间结束时，系统将自动开奖。中奖用户需在规定时间内选择尺码并完成支付。',
  },
  {
    question: '什么是空投福利？',
    answer: '空投是鞋宙为用户提供的免费福利，包括优惠券、积分等。进入"活动"页面，切换到"空投"标签，点击"领取"即可获得。部分空投有领取条件限制，请留意活动说明。',
  },
  {
    question: '如何设置发售提醒？',
    answer: '进入"活动"页面，切换到"预热"标签，找到感兴趣的即将发售商品，点击"提醒我"按钮，我们会在发售开始前通知您。',
  },
  {
    question: '订单支付后多久发货？',
    answer: '普通商品一般在支付成功后1-3个工作日内发货。抽签中签的限量款商品将在中签确认并支付后7个工作日内发货，具体以活动规则为准。',
  },
  {
    question: '如何修改收货地址？',
    answer: '进入"我的"页面，点击"我的地址"，可以添加、修改或删除收货地址。您也可以设置默认收货地址。',
  },
  {
    question: '支持哪些支付方式？',
    answer: '目前支持微信支付、支付宝等主流支付方式。抽签中签商品仅支持在线支付，请在规定时间内完成支付。',
  },
  {
    question: '如何申请退款？',
    answer: '进入"我的"页面，点击"我的订单"，找到需要退款的订单，进入订单详情页面点击"申请退款"，填写退款原因后提交申请，客服会在1-3个工作日内处理。',
  },
  {
    question: '抽签中签后可以取消吗？',
    answer: '抽签中签后24小时内未支付将视为自动放弃。放弃中签资格可能会影响后续抽签的中签概率，请谨慎参与。',
  },
];

export default function HelpPage() {
  const navigate = useNavigate();
  const [feedback, setFeedback] = useState('');
  const [expandedIndex, setExpandedIndex] = useState<number | null>(null);

  const handleSubmit = () => {
    if (!feedback.trim()) {
      toast.error('请输入反馈内容');
      return;
    }
    toast.success('感谢您的反馈，我们会尽快处理');
    setFeedback('');
  };

  return (
    <div className="min-h-screen bg-white">
      <header className="flex items-center px-4 h-12 border-b border-gray-100">
        <button onClick={() => navigate(-1)} className="p-1">
          <ArrowLeft className="w-5 h-5" />
        </button>
        <h1 className="flex-1 text-center font-medium pr-6">帮助与反馈</h1>
      </header>

      <div className="p-4">
        <h2 className="text-sm font-bold text-gray-800 mb-3">常见问题</h2>
        
        <div className="space-y-2 mb-6">
          {faqList.map((item, index) => (
            <div
              key={index}
              className="bg-gray-50 rounded-lg overflow-hidden"
            >
              <button
                className="w-full p-3 text-left text-sm font-medium text-gray-800 flex justify-between items-center"
                onClick={() => setExpandedIndex(expandedIndex === index ? null : index)}
              >
                <span>{item.question}</span>
                <span className="text-gray-400 text-xs">
                  {expandedIndex === index ? '收起' : '展开'}
                </span>
              </button>
              {expandedIndex === index && (
                <div className="px-3 pb-3 text-xs text-gray-600 leading-relaxed">
                  {item.answer}
                </div>
              )}
            </div>
          ))}
        </div>

        <h2 className="text-sm font-bold text-gray-800 mb-3">意见反馈</h2>
        
        <textarea
          value={feedback}
          onChange={(e) => setFeedback(e.target.value)}
          placeholder="请描述您遇到的问题或建议..."
          className="w-full h-32 p-3 text-sm border border-gray-200 rounded-lg resize-none focus:outline-none focus:ring-1 focus:ring-emerald-600"
        />

        <Button
          onClick={handleSubmit}
          className="w-full mt-3 h-10 bg-emerald-600 hover:bg-emerald-700"
        >
          <Send className="w-4 h-4 mr-2" />
          提交反馈
        </Button>

        <div className="mt-6 p-3 bg-gray-50 rounded-lg">
          <p className="text-xs text-gray-500 text-center">
            客服热线：400-888-8888<br/>
            服务时间：周一至周日 9:00-21:00
          </p>
        </div>
      </div>
    </div>
  );
}

