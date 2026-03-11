import type {
  User,
  Address,
  Shoe,
  CartItem,
  Order,
  Coupon,
  Draw,
  DrawRecord,
  Bundle,
  Category,
  Comment,
  Logistics,
  Outlet,
  Message,
  ShopStatus,
} from '@/types';

// 模拟用户
export const mockUser: User = {
  id: '1',
  nickname: '球鞋达人',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=sneaker',
  phone: '138****8888',
};

// 模拟地址
export const mockAddresses: Address[] = [
  {
    id: 1,
    consignee: '张三',
    phone: '13888888888',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    detail: '建国路88号SOHO现代城A座1201',
    label: '家',
    isDefault: true,
  },
  {
    id: 2,
    consignee: '张三',
    phone: '13888888888',
    province: '北京市',
    city: '北京市',
    district: '海淀区',
    detail: '中关村大街1号海龙大厦1502',
    label: '公司',
    isDefault: false,
  },
];

// 模拟鞋款
export const mockShoes: Shoe[] = [
  {
    id: 1,
    name: 'Air Jordan 1 High OG 黑脚趾',
    description: '经典黑红配色，传奇再现。采用优质皮革材质，舒适透气，是球鞋收藏家的必备之选。',
    price: 1499,
    image: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=800&fit=crop',
    categoryIds: [1],
    categoryNames: ['篮球鞋'],
    sizes: [
      { size: '40', stock: 5 },
      { size: '41', stock: 8 },
      { size: '42', stock: 12 },
      { size: '43', stock: 6 },
      { size: '44', stock: 3 },
    ],
    salesCount: 256,
    isLimited: 1,
  },
  {
    id: 2,
    name: 'Nike Dunk Low 熊猫',
    description: '黑白经典配色，简约百搭。低帮设计，日常穿搭的首选。',
    price: 799,
    image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop',
    categoryIds: [1],
    categoryNames: ['篮球鞋'],
    sizes: [
      { size: '38', stock: 10 },
      { size: '39', stock: 15 },
      { size: '40', stock: 20 },
      { size: '41', stock: 18 },
      { size: '42', stock: 12 },
    ],
    salesCount: 512,
  },
  {
    id: 3,
    name: 'Air Force 1 \'07 纯白',
    description: '永恒经典，纯白传奇。耐克最具标志性的鞋款，永不过时。',
    price: 749,
    image: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800&h=800&fit=crop',
    categoryIds: [2],
    categoryNames: ['休闲鞋'],
    sizes: [
      { size: '36', stock: 8 },
      { size: '37', stock: 12 },
      { size: '38', stock: 15 },
      { size: '39', stock: 20 },
      { size: '40', stock: 18 },
    ],
    salesCount: 1024,
  },
  {
    id: 4,
    name: 'Yeezy Boost 350 V2 黑芝麻',
    description: '侃爷经典设计，Boost缓震科技。舒适与潮流的完美结合。',
    price: 1899,
    image: 'https://images.unsplash.com/photo-1584735175315-9d5df23860e6?w=800&h=800&fit=crop',
    categoryIds: [3],
    categoryNames: ['跑步鞋'],
    sizes: [
      { size: '40', stock: 3 },
      { size: '41', stock: 5 },
      { size: '42', stock: 4 },
      { size: '43', stock: 2 },
    ],
    salesCount: 128,
    isLimited: 1,
  },
];

// 模拟购物车
export const mockCartItems: CartItem[] = [
  {
    id: '1',
    shoeId: '1',
    shoeName: 'Air Jordan 1 High OG 黑脚趾',
    shoeImage: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=400&h=400&fit=crop',
    size: '42',
    price: 1499,
    quantity: 1,
    selected: true,
  },
  {
    id: '2',
    shoeId: '2',
    shoeName: 'Nike Dunk Low 熊猫',
    shoeImage: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop',
    size: '41',
    price: 799,
    quantity: 2,
    selected: false,
  },
];

// 模拟订单
export const mockOrders: Order[] = [
  {
    number: 'SN202401150001',
    status: 1,
    orderTime: '2024-01-15 10:30:00',
    payMethod: 1,
    amount: 1499,
    phone: '13888888888',
    address: '北京市北京市朝阳区建国路88号SOHO现代城A座1201',
    consignee: '张三',
    orderDetailList: [
      {
        name: 'Air Jordan 1 High OG 黑脚趾',
        shoeSize: '42',
        number: 1,
        amount: 1499,
        image: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=400&h=400&fit=crop',
      },
    ],
  },
  {
    number: 'SN202401100002',
    status: 6,
    orderTime: '2024-01-10 14:20:00',
    checkoutTime: '2024-01-10 14:25:00',
    payMethod: 1,
    payStatus: 1,
    amount: 749,
    phone: '13888888888',
    address: '北京市北京市朝阳区建国路88号SOHO现代城A座1201',
    consignee: '张三',
    orderDetailList: [
      {
        name: 'Nike Dunk Low 熊猫',
        shoeSize: '41',
        number: 1,
        amount: 799,
        image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop',
      },
    ],
  },
];

// 模拟优惠券
export const mockCoupons: Coupon[] = [
  {
    id: '1',
    name: '新用户专享券',
    amount: 100,
    minSpend: 500,
    startTime: '2024-01-01',
    endTime: '2024-12-31',
    status: 'available',
    description: '满500元可用',
  },
  {
    id: '2',
    name: '限时特惠券',
    amount: 50,
    minSpend: 300,
    startTime: '2024-01-01',
    endTime: '2024-01-31',
    status: 'available',
    description: '满300元可用',
  },
  {
    id: '3',
    name: '会员专享券',
    amount: 200,
    minSpend: 1000,
    startTime: '2024-01-01',
    endTime: '2024-06-30',
    status: 'used',
    description: '满1000元可用',
  },
];

// 模拟抽签
export const mockDraws: Draw[] = [
  {
    id: '1',
    name: 'Air Jordan 1 黑脚趾 限量抽签',
    image: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=800&h=600&fit=crop',
    shoeName: 'Air Jordan 1 High OG 黑脚趾',
    shoeImage: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=400&h=400&fit=crop',
    startTime: '2024-01-20 10:00:00',
    endTime: '2024-01-22 22:00:00',
    status: 'ongoing',
    participantStatus: 'not_joined',
    sizes: ['40', '41', '42', '43', '44'],
  },
  {
    id: '2',
    name: 'Travis Scott x Air Jordan 1',
    image: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800&h=600&fit=crop',
    shoeName: 'Travis Scott x Air Jordan 1 Low',
    shoeImage: 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400&h=400&fit=crop',
    startTime: '2024-01-25 10:00:00',
    endTime: '2024-01-27 22:00:00',
    status: 'upcoming',
    sizes: ['40', '41', '42', '43'],
  },
];

// 模拟抽签记录
export const mockDrawRecords: DrawRecord[] = [
  {
    id: '1',
    drawId: '1',
    drawName: 'Air Jordan 1 黑脚趾 限量抽签',
    shoeImage: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=400&h=400&fit=crop',
    joinTime: '2024-01-20 10:30:00',
    shoeSize: '42',
    status: 'pending',
  },
];

// 模拟组合包
export const mockBundles: Bundle[] = [
  {
    id: '1',
    name: '新手入门套装',
    description: '包含两双经典款球鞋，适合新手收藏',
    image: 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=800&h=600&fit=crop',
    price: 1999,
  },
];

// 模拟分类
export const mockCategories: Category[] = [
  { id: 1, name: '篮球鞋', sort: 1 },
  { id: 2, name: '休闲鞋', sort: 2 },
  { id: 3, name: '跑步鞋', sort: 3 },
  { id: 4, name: '限量款', sort: 4 },
  { id: 5, name: '联名款', sort: 5 },
  { id: 6, name: '经典款', sort: 6 },
];

// 模拟评论
export const mockComments: Comment[] = [
  {
    id: 1,
    userId: 1,
    spuId: 1,
    userName: '用户1',
    userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
    rating: 5,
    content: '太帅了！这配色绝对是经典中的经典，做工也很精致，值得入手！',
    images: JSON.stringify([
      'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=400&h=400&fit=crop',
    ]),
    createTime: '2024-01-10 15:30:00',
  },
  {
    id: 2,
    userId: 2,
    spuId: 1,
    userName: '用户2',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop',
    rating: 4,
    content: '鞋子很棒，就是发货稍微慢了一点，总体来说很满意。',
    images: '',
    createTime: '2024-01-08 10:20:00',
  },
];

// 模拟物流
export const mockLogistics: Logistics = {
  orderNo: 'SN202401100002',
  company: '顺丰速运',
  trackingNo: 'SF1234567890',
  status: '已签收',
  traces: [
    { time: '2024-01-13 16:30:00', content: '您的快件已签收，感谢使用顺丰速运' },
    { time: '2024-01-13 09:00:00', content: '快件到达【北京朝阳营业部】' },
    { time: '2024-01-12 18:00:00', content: '快件离开【北京顺义集散中心】，发往【北京朝阳营业部】' },
    { time: '2024-01-11 14:00:00', content: '快件到达【北京顺义集散中心】' },
    { time: '2024-01-11 09:00:00', content: '顺丰速运已收取快件' },
  ],
};

// 模拟门店
export const mockOutlets: Outlet[] = [
  {
    id: '1',
    name: '鞋宙三里屯旗舰店',
    address: '北京市朝阳区三里屯路19号三里屯太古里南区S2-11',
    phone: '010-64178888',
    longitude: 116.4551,
    latitude: 39.9354,
    distance: 2.5,
    businessHours: '10:00-22:00',
    images: [
      'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800&h=600&fit=crop',
    ],
  },
  {
    id: '2',
    name: '鞋宙西单店',
    address: '北京市西城区西单北大街110号老佛爷百货B1',
    phone: '010-66018888',
    longitude: 116.3721,
    latitude: 39.9109,
    distance: 5.8,
    businessHours: '10:00-22:00',
    images: [
      'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?w=800&h=600&fit=crop',
    ],
  },
];

// 模拟消息
export const mockMessages: Message[] = [
  {
    id: '1',
    type: 'system',
    title: '欢迎来到鞋宙',
    content: '感谢您注册鞋宙APP，开启您的球鞋收藏之旅！',
    createTime: '2024-01-15 10:00:00',
    isRead: false,
  },
  {
    id: '2',
    type: 'order',
    title: '订单发货提醒',
    content: '您的订单 SN202401100002 已发货，请留意物流信息',
    createTime: '2024-01-11 09:30:00',
    isRead: true,
  },
  {
    id: '3',
    type: 'activity',
    title: '限量抽签即将开始',
    content: 'Air Jordan 1 黑脚趾限量抽签将于1月20日开启，敬请期待！',
    createTime: '2024-01-14 18:00:00',
    isRead: false,
  },
];

// 模拟店铺状态
export const mockShopStatus: ShopStatus = {
  isOpen: true,
  openTime: '09:00',
  closeTime: '23:00',
};
