import { format, isValid, parseISO } from 'date-fns';
import { zhCN } from 'date-fns/locale';

// 格式化价格
export const formatPrice = (price: number | undefined | null) => {
  if (price === undefined || price === null) {
    return '¥0.00';
  }
  return `¥${price.toFixed(2)}`;
};

// 格式化日期
export const formatDate = (date: string | Date, pattern = 'yyyy-MM-dd HH:mm') => {
  if (!date) return '--';
  const parsed = typeof date === 'string' ? parseISO(date) : date;
  if (!isValid(parsed)) return '--';
  return format(parsed, pattern, { locale: zhCN });
};

// 格式化手机号
export const formatPhone = (phone: string) => {
  if (phone.length !== 11) return phone;
  return `${phone.slice(0, 3)}****${phone.slice(7)}`;
};

// 验证手机号
export const isValidPhone = (phone: string) => {
  return /^1[3-9]\d{9}$/.test(phone);
};

// 验证验证码
export const isValidCode = (code: string) => {
  return /^\d{6}$/.test(code);
};

// 倒计时格式化
export const formatCountdown = (seconds: number) => {
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  if (days > 0) {
    return `${days}天${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

// 订单状态映射
// 1待付款 2待发货 3已发货 4运输中 5派送中 6已签收 7已取消 8已评价
export const orderStatusMap: Record<number, { text: string; color: string }> = {
  1: { text: '待付款', color: 'text-orange-500' },
  2: { text: '待发货', color: 'text-amber-500' },
  3: { text: '已发货', color: 'text-amber-500' },
  4: { text: '运输中', color: 'text-rose-500' },
  5: { text: '派送中', color: 'text-rose-500' },
  6: { text: '已签收', color: 'text-green-500' },
  7: { text: '已取消', color: 'text-gray-500' },
  8: { text: '已评价', color: 'text-green-600' },
};

// 抽签状态映射
export const drawStatusMap: Record<string, { text: string; color: string }> = {
  upcoming: { text: '即将开始', color: 'text-amber-500' },
  ongoing: { text: '进行中', color: 'text-green-500' },
  ended: { text: '已结束', color: 'text-gray-500' },
};

// 抽签参与状态映射
export const drawParticipantStatusMap: Record<string, { text: string; color: string }> = {
  not_joined: { text: '未参与', color: 'text-gray-500' },
  joined: { text: '已参与', color: 'text-amber-500' },
  won: { text: '已中奖', color: 'text-green-500' },
  not_won: { text: '未中奖', color: 'text-gray-500' },
};

// 抽签记录状态映射
export const drawRecordStatusMap: Record<string, { text: string; color: string }> = {
  pending: { text: '待开奖', color: 'text-amber-500' },
  won: { text: '已中奖', color: 'text-green-500' },
  not_won: { text: '未中奖', color: 'text-gray-500' },
  abandoned: { text: '已放弃', color: 'text-gray-500' },
  purchased: { text: '已购买', color: 'text-green-500' },
};

// 生成唯一ID
export const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2);
};

// 防抖函数
export const debounce = <T extends (...args: any[]) => void>(
  fn: T,
  delay: number
) => {
  let timer: ReturnType<typeof setTimeout>;
  return (...args: Parameters<T>) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), delay);
  };
};

// 节流函数
export const throttle = <T extends (...args: any[]) => void>(
  fn: T,
  delay: number
) => {
  let lastTime = 0;
  return (...args: Parameters<T>) => {
    const now = Date.now();
    if (now - lastTime >= delay) {
      lastTime = now;
      fn(...args);
    }
  };
};

// 深拷贝
export const deepClone = <T>(obj: T): T => {
  return JSON.parse(JSON.stringify(obj));
};

// 本地存储封装
export const storage = {
  get: <T>(key: string): T | null => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch {
      return null;
    }
  },
  set: (key: string, value: any) => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (e) {
      console.error('Storage set error:', e);
    }
  },
  remove: (key: string) => {
    try {
      localStorage.removeItem(key);
    } catch (e) {
      console.error('Storage remove error:', e);
    }
  },
  clear: () => {
    try {
      localStorage.clear();
    } catch (e) {
      console.error('Storage clear error:', e);
    }
  },
};




