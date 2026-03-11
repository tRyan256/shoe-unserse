// 应用配置
export const APP_NAME = '鞋宙';
export const APP_SLOGAN = '发现下一双传奇球鞋';

// 分页配置
export const PAGE_SIZE = 10;

// 验证码倒计时
export const CODE_COUNTDOWN = 60;

// 订单状态
export const ORDER_STATUS = {
  PENDING_PAYMENT: 1,
  PENDING_ACCEPT: 2,
  PENDING_SHIP: 3,
  PENDING_RECEIVE: 4,
  COMPLETED: 5,
  CANCELLED: 6,
} as const;

// 支付方式
export const PAY_METHOD = {
  WECHAT: 1,
  ALIPAY: 2,
} as const;

// 地址标签
export const ADDRESS_LABELS = [
  { value: '家', label: '家', color: 'bg-amber-100 text-amber-600' },
  { value: '公司', label: '公司', color: 'bg-green-100 text-green-600' },
  { value: '学校', label: '学校', color: 'bg-rose-100 text-rose-600' },
] as const;

// TabBar配置
export const TAB_BAR_ITEMS = [
  { path: '/', icon: 'Home', label: '首页' },
  { path: '/category', icon: 'Grid3X3', label: '分类' },
  { path: '/cart', icon: 'ShoppingCart', label: '购物车' },
  { path: '/user', icon: 'User', label: '我的' },
] as const;

// 轮播图配置
export const CAROUSEL_CONFIG = {
  autoplay: true,
  interval: 5000,
  loop: true,
};

// 动画配置
export const ANIMATION_CONFIG = {
  duration: {
    fast: 0.2,
    normal: 0.3,
    slow: 0.5,
    slower: 0.8,
  },
  easing: {
    easeOut: [0.16, 1, 0.3, 1],
    easeIn: [0.7, 0, 0.84, 0],
    elastic: [0.68, -0.55, 0.265, 1.55],
  },
};

// 图片配置
export const IMAGE_CONFIG = {
  placeholder: '/images/placeholder.png',
  maxSize: 5 * 1024 * 1024, // 5MB
  allowedTypes: ['image/jpeg', 'image/png', 'image/webp'],
};

// 消息类型
export const MESSAGE_TYPE = {
  SYSTEM: 'system',
  ORDER: 'order',
  ACTIVITY: 'activity',
} as const;

// 物流状态
export const LOGISTICS_STATUS = {
  SHIPPED: '已发货',
  IN_TRANSIT: '运输中',
  OUT_FOR_DELIVERY: '派送中',
  DELIVERED: '已签收',
} as const;

