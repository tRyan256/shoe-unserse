import type {
  User,
  Address,
  Shoe,
  ShoeSpu,
  ShoeSpuDetail,
  ShoeSku,
  CartItem,
  Coupon,
  Draw,
  DrawRecord,
  Bundle,
  Category,
  Comment,
  MyComment,
  Logistics,
  Outlet,
  Message,
  OrderSubmitParams,
  OrderAsyncSubmitResult,
  OrderAsyncStatus,
  OrderPaymentParams,
  OrderPaymentResult,
  OrderListParams,
  PageResult,
  OrderVO,
  ActivityBanner,
  BundleVO,
  ExperiencePost,
  ExperiencePostDetail,
  ExperienceComment,
  ExperienceUserProfile,
  ExperiencePostDTO,
  ExperienceCommentDTO,
  ExperienceReplyDTO,
  ExperienceReply,
} from '@/types';
import { get, post, put, del } from './request';
import request from './request';
import { listNotifications, markAsRead } from './user/notificationApi';
import type { ExperienceNotificationVO } from './user/notificationApi';

interface RawAddressBook {
  id: number;
  consignee: string;
  phone: string;
  sex?: string;
  provinceCode?: string;
  provinceName?: string;
  cityCode?: string;
  cityName?: string;
  districtCode?: string;
  districtName?: string;
  detail: string;
  label?: string;
  isDefault?: number;
}

interface RawCoupon {
  id: number;
  name: string;
  type?: number;
  value?: number;
  minAmount?: number;
  startTime: string;
  endTime: string;
  status?: number;
}

interface RawUserCouponVO {
  id: number;
  couponId: number;
  couponName: string;
  couponType: number;
  couponValue: number;
  minAmount: number;
  startTime: string;
  endTime: string;
  status: number;
  useTime?: string;
  orderId?: number;
  createTime: string;
}

interface RawDraw {
  id: number;
  title: string;
  targetType: number;
  skuId?: number;
  bundleId?: number;
  startTime: string;
  endTime: string;
  drawTime?: string;
  status: number;
  description?: string;
}

interface RawDrawShoe {
  id: number;
  name: string;
  image: string;
  sizes?: string[];
}

interface RawDrawBundleItem {
  skuId: number;
  name: string;
  image: string;
  copies?: number;
  sizes?: string[];
}

interface RawDrawBundle {
  id: number;
  name: string;
  image: string;
  items?: RawDrawBundleItem[];
}

interface RawDrawDetailVO extends RawDraw {
  price?: number;
  totalStock?: number;
  maxParticipants?: number;
  winnerCount?: number;
  shoe?: RawDrawShoe | null;
  bundle?: RawDrawBundle | null;
}

interface RawDrawResult {
  id?: number;
  drawId: number;
  shoeSize?: string;
  status?: number;
  orderNo?: string;
  createTime?: string;
}

interface RawDrawHistoryRecord {
  id: number;
  drawId: number;
  drawTitle: string;
  targetType: number;
  shoeSize: string;
  status: number;
  orderNo?: string;
  createTime: string;
}

interface RawDrawSizeOption {
  size: string;
  stock: number;
}

interface RawDrawBundleOption {
  skuId: number;
  copies?: number;
  options: RawDrawSizeOption[];
}

interface RawDrawWinOptions {
  targetType: number;
  skuId?: number;
  bundleId?: number;
  shoeOptions?: RawDrawSizeOption[];
  bundleOptions?: RawDrawBundleOption[];
}

interface RawDrawWinner {
  userId: number;
  userName: string;
  userAvatar?: string;
  shoeSize: string;
  joinTime?: string;
}

interface RawLogisticsEntity {
  orderNo: string;
  expressCompany: string;
  expressNo: string;
  status: number;
}

interface RawLogisticsTrace {
  operateTime: string;
  description: string;
}

interface RawLogisticsDetailVO {
  logistics: RawLogisticsEntity;
  traces: RawLogisticsTrace[];
}

interface RawOutletNearbyVO {
  id: number;
  name: string;
  address: string;
  phone: string;
  longitude: number | string;
  latitude: number | string;
  businessHours: string;
  distanceMeters?: number;
}

interface RawActivityBanner {
  id: number;
  type: string;
  title: string;
  description: string;
  image: string;
  startTime?: string;
  endTime?: string;
  drawTime?: string;
  bannerStatus?: string;
}

export type DrawDetailVO = RawDrawDetailVO;
export type DrawWinOptionsVO = RawDrawWinOptions;

const toIsoDateTime = (value?: string): string => (value ? value.replace(' ', 'T') : '');
const toNumber = (value: number | string | undefined): number =>
  value === undefined ? 0 : Number(value);

const mapRawAddressToAddress = (raw: RawAddressBook): Address => ({
  id: raw.id,
  consignee: raw.consignee,
  phone: raw.phone,
  province: raw.provinceName || '',
  city: raw.cityName || '',
  district: raw.districtName || '',
  detail: raw.detail,
  label: raw.label,
  isDefault: raw.isDefault === 1,
});

const mapAddressToRawAddress = (address: Partial<Address> & { id?: number }): RawAddressBook => ({
  id: address.id || 0,
  consignee: address.consignee || '',
  phone: address.phone || '',
  sex: '1',
  provinceCode: address.province || '',
  provinceName: address.province || '',
  cityCode: address.city || '',
  cityName: address.city || '',
  districtCode: address.district || '',
  districtName: address.district || '',
  detail: address.detail || '',
  label: address.label || '',
  isDefault: address.isDefault ? 1 : 0,
});

const mapCouponStatus = (status?: number): Coupon['status'] => {
  if (status === 0) return 'available';
  if (status === 1) return 'used';
  return 'expired';
};

const mapRawCouponToCoupon = (raw: RawCoupon): Coupon => ({
  id: String(raw.id),
  name: raw.name,
  amount: Number(raw.value || 0),
  minSpend: Number(raw.minAmount || 0),
  startTime: toIsoDateTime(raw.startTime),
  endTime: toIsoDateTime(raw.endTime),
  status: mapCouponStatus(raw.status),
  description: raw.minAmount ? `满${raw.minAmount}可用` : undefined,
});

const mapRawUserCouponToCoupon = (raw: RawUserCouponVO): Coupon => ({
  id: String(raw.id),
  name: raw.couponName,
  amount: Number(raw.couponValue || 0),
  minSpend: Number(raw.minAmount || 0),
  startTime: toIsoDateTime(raw.startTime),
  endTime: toIsoDateTime(raw.endTime),
  status: mapCouponStatus(raw.status),
  description: raw.minAmount ? `满${raw.minAmount}可用` : undefined,
});

const mapDrawStatus = (status?: number): Draw['status'] => {
  if (status === 0) return 'upcoming';
  if (status === 1) return 'ongoing';
  return 'ended';
};

const toTimestamp = (value?: string): number | null => {
  if (!value) return null;
  const normalized = value.includes('T') ? value : value.replace(' ', 'T');
  const timestamp = Date.parse(normalized);
  return Number.isNaN(timestamp) ? null : timestamp;
};

const mapDrawStatusByTime = (startTime?: string, endTime?: string): Draw['status'] | null => {
  const now = Date.now();
  const start = toTimestamp(startTime);
  const end = toTimestamp(endTime);

  if (start !== null && now < start) {
    return 'upcoming';
  }
  if (end !== null && now >= end) {
    return 'ended';
  }
  if (start !== null && (end === null || now < end)) {
    return 'ongoing';
  }
  return null;
};

const resolveDrawStatus = (status?: number, startTime?: string, endTime?: string): Draw['status'] => {
  return mapDrawStatusByTime(startTime, endTime) ?? mapDrawStatus(status);
};

const mapDrawRecordStatus = (status?: number, orderNo?: string): DrawRecord['status'] => {
  if (status === 0) return 'pending';
  if (status === 1) return orderNo ? 'purchased' : 'won';
  if (status === 2) return 'not_won';
  if (status === 3) return 'abandoned';
  return 'pending';
};

const getDrawImage = (detail: RawDrawDetailVO): string => {
  if (detail.targetType === 1) {
    return detail.shoe?.image || '/images/default-banner.svg';
  }
  if (detail.targetType === 2) {
    return detail.bundle?.image || '/images/default-banner.svg';
  }
  return '/images/default-banner.svg';
};

const getDrawDisplayName = (detail: RawDrawDetailVO): string => {
  if (detail.targetType === 1) {
    return detail.shoe?.name || detail.title;
  }
  if (detail.targetType === 2) {
    return detail.bundle?.name || detail.title;
  }
  return detail.title;
};

const getDrawDisplaySizes = (detail: RawDrawDetailVO): string[] => {
  if (detail.targetType === 1) {
    return detail.shoe?.sizes || [];
  }
  if (detail.targetType === 2) {
    return detail.bundle?.items?.[0]?.sizes || [];
  }
  return [];
};

const mapDrawDetailToDraw = (detail: RawDrawDetailVO): Draw => {
  const image = getDrawImage(detail);
  const shoeName = getDrawDisplayName(detail);

  return {
    id: String(detail.id),
    name: detail.title || shoeName,
    image,
    shoeName,
    shoeImage: image,
    startTime: toIsoDateTime(detail.startTime),
    endTime: toIsoDateTime(detail.endTime),
    status: resolveDrawStatus(detail.status, detail.startTime, detail.endTime),
    sizes: getDrawDisplaySizes(detail),
  };
};

const mapRawDrawToDraw = (raw: RawDraw): Draw => ({
  id: String(raw.id),
  name: raw.title || `抽签活动 #${raw.id}`,
  image: '/images/default-banner.svg',
  shoeName: raw.title || `抽签活动 #${raw.id}`,
  shoeImage: '/images/default-banner.svg',
  startTime: toIsoDateTime(raw.startTime),
  endTime: toIsoDateTime(raw.endTime),
  status: resolveDrawStatus(raw.status, raw.startTime, raw.endTime),
  sizes: [],
});

const mapLogisticsStatusText = (status?: number): string => {
  if (status === 2) return '待发货';
  if (status === 3) return '已发货';
  if (status === 4) return '运输中';
  if (status === 5) return '派送中';
  if (status === 6) return '已签收';
  if (status === 8) return '已评价';
  if (status === 7) return '已取消';
  return '物流处理中';
};

const mapNotificationToMessageType = (type?: number): Message['type'] => {
  if (type === 6) return 'order';
  if (type === 1 || type === 2 || type === 3 || type === 5) return 'activity';
  return 'system';
};

const mapNotificationTitle = (notification: ExperienceNotificationVO): string => {
  if (notification.type === 1) return '收到点赞';
  if (notification.type === 2) return '收到评论';
  if (notification.type === 3) return '收到回复';
  if (notification.type === 4) return '评论获赞';
  if (notification.type === 5) return '新增关注';
  if (notification.type === 6) return '订单通知';
  return '系统通知';
};

const mapNotificationContent = (notification: ExperienceNotificationVO): string =>
  notification.content ||
  notification.commentContent ||
  notification.postContent ||
  '您有一条新消息';

const fetchDrawDetailVO = async (drawId: number): Promise<RawDrawDetailVO> => {
  return get(`/user/draw/detail/${drawId}`);
};

// 用户相关API
export const userApi = {
  // 发送验证码 - POST /user/user/sendCode
  sendCode: async (phone: string): Promise<void> => {
    return post('/user/user/sendCode', null, { params: { phone } });
  },

  // 登录 - POST /user/user/login
  login: async (phone: string, code: string): Promise<{ id: number; phone: string; token: string }> => {
    return post('/user/user/login', { phone, code });
  },

  // 获取用户信息 - GET /user/user/info
  getUserInfo: async (): Promise<User> => {
    return get('/user/user/info');
  },
};

// 地址相关API
export const addressApi = {
  // 获取地址列表 - GET /user/addressBook/list
  getAddressList: async (): Promise<Address[]> => {
    const data = await get<RawAddressBook[]>('/user/addressBook/list');
    return (data || []).map(mapRawAddressToAddress);
  },

  // 获取地址详情 - GET /user/addressBook/{id}
  getAddressDetail: async (id: number): Promise<Address> => {
    const data = await get<RawAddressBook>(`/user/addressBook/${id}`);
    return mapRawAddressToAddress(data);
  },

  // 获取默认地址 - GET /user/addressBook/default
  getDefaultAddress: async (): Promise<Address> => {
    const data = await get<RawAddressBook>('/user/addressBook/default');
    return mapRawAddressToAddress(data);
  },

  // 新增地址 - POST /user/addressBook
  addAddress: async (data: Omit<Address, 'id'>): Promise<{ id: number }> => {
    await post('/user/addressBook', mapAddressToRawAddress(data));
    return { id: -1 };
  },

  // 更新地址 - PUT /user/addressBook
  updateAddress: async (data: Address): Promise<void> => {
    return put('/user/addressBook', mapAddressToRawAddress(data));
  },

  // 删除地址 - DELETE /user/addressBook
  deleteAddress: async (id: number): Promise<void> => {
    return del(`/user/addressBook?id=${id}`);
  },

  // 设置默认地址 - PUT /user/addressBook/default
  setDefaultAddress: async (id: number): Promise<void> => {
    return put('/user/addressBook/default', { id });
  },
};

// 购物车相关API
export const cartApi = {
  // 获取购物车列表 - GET /user/shoppingCart/list
  getCartList: async (): Promise<CartItem[]> => {
    return get('/user/shoppingCart/list');
  },

  // 添加商品 - POST /user/shoppingCart/add
  addToCart: async (data: {
    spuId?: number;
    skuId?: number;
    shoeId?: number;  // 保持兼容
    bundleId?: number;
    shoeSize: string;
    selected?: number;
  }): Promise<void> => {
    return post('/user/shoppingCart/add', data);
  },

  // 减少商品 - POST /user/shoppingCart/sub
  subFromCart: async (data: {
    spuId?: number;
    skuId?: number;
    shoeId?: number;  // 保持兼容
    bundleId?: number;
    shoeSize: string;
  }): Promise<void> => {
    return post('/user/shoppingCart/sub', data);
  },

  // 清空购物车 - DELETE /user/shoppingCart/clean
  clearCart: async (): Promise<void> => {
    return del('/user/shoppingCart/clean');
  },
};

// 订单相关API
export const orderApi = {
  // 提交订单 - POST /user/order/submit
  submitOrder: async (data: OrderSubmitParams): Promise<OrderAsyncSubmitResult> => {
    return post('/user/order/submit', data);
  },

  // 查询订单提交状态 - GET /user/order/submit/status/{orderNumber}
  getSubmitStatus: async (orderNumber: string): Promise<OrderAsyncStatus> => {
    return get(`/user/order/submit/status/${orderNumber}`);
  },

  // 支付订单 - PUT /user/order/payment
  payOrder: async (data: OrderPaymentParams): Promise<OrderPaymentResult> => {
    return put('/user/order/payment', data);
  },

  // 获取订单列表 - GET /user/order/historyOrders
  getOrderList: async (params?: OrderListParams): Promise<PageResult<OrderVO>> => {
    return get('/user/order/historyOrders', { params });
  },

  // 获取订单详情 - GET /user/order/orderDetail/{orderNumber}
  getOrderDetail: async (orderNumber: string): Promise<OrderVO> => {
    return get(`/user/order/orderDetail/${orderNumber}`);
  },

  // 取消订单 - PUT /user/order/cancel/{orderNumber}
  cancelOrder: async (orderNumber: string): Promise<void> => {
    return put(`/user/order/cancel/${orderNumber}`);
  },

  // 确认收货 - PUT /user/order/confirm/{orderNumber}
  confirmOrder: async (orderNumber: string): Promise<void> => {
    return put(`/user/order/confirm/${orderNumber}`);
  },

  // 再来一单 - POST /user/order/repetition/{orderNumber}
  reorder: async (orderNumber: string): Promise<void> => {
    return post(`/user/order/repetition/${orderNumber}`);
  },

  // 催单 - GET /user/order/reminder/{orderNumber}
  reminderOrder: async (orderNumber: string): Promise<string> => {
    return get(`/user/order/reminder/${orderNumber}`);
  },

  // 获取订单统计 - GET /user/order/statistics
  getOrderStatistics: async (): Promise<{
    toBePaid: number;
    toBeShipped: number;
    toBeReceived: number;
    toBeReviewed: number;
  }> => {
    return get('/user/order/statistics');
  },
};

// 抽签相关API
export const drawApi = {
  // 获取抽签列表 - GET /user/draw/list
  getDrawList: async (): Promise<Draw[]> => {
    const list = await get<RawDraw[]>('/user/draw/list');
    if (!list || list.length === 0) {
      return [];
    }

    const details = await Promise.all(
      list.map(async (item) => {
        try {
          const detail = await fetchDrawDetailVO(item.id);
          return mapDrawDetailToDraw(detail);
        } catch {
          return mapRawDrawToDraw(item);
        }
      })
    );

    return details;
  },

  // 获取抽签详情（兼容旧页面展示）- GET /user/draw/detail/{drawId}
  getDrawDetail: async (drawId: number): Promise<Draw> => {
    const detail = await fetchDrawDetailVO(drawId);
    return mapDrawDetailToDraw(detail);
  },

  // 获取抽签详情VO（新页面使用）- GET /user/draw/detail/{drawId}
  getDrawDetailVO: async (drawId: number): Promise<DrawDetailVO> => {
    return fetchDrawDetailVO(drawId);
  },

  // 参与抽签 - POST /user/draw/join
  joinDraw: async (drawId: number): Promise<void> => {
    return post('/user/draw/join', { drawId });
  },

  // 获取我的抽签记录 - GET /user/profile/draws
  getMyDraws: async (): Promise<DrawRecord[]> => {
    const result = await get<PageResult<RawDrawHistoryRecord>>('/user/profile/draws', {
      params: { page: 1, size: 100 },
    });
    const records = result?.records || [];
    if (records.length === 0) {
      return [];
    }

    const drawIds = Array.from(new Set(records.map((item) => item.drawId).filter(Boolean)));
    const detailMap = new Map<number, Draw>();

    await Promise.all(
      drawIds.map(async (drawId) => {
        try {
          const detail = await fetchDrawDetailVO(drawId);
          detailMap.set(drawId, mapDrawDetailToDraw(detail));
        } catch {
          // ignore
        }
      })
    );

    return records.map((record) => {
      const draw = detailMap.get(record.drawId);
      return {
        id: String(record.id),
        drawId: String(record.drawId),
        drawName: record.drawTitle || draw?.name || `抽签活动 #${record.drawId}`,
        shoeImage: draw?.shoeImage || '/images/default-banner.svg',
        joinTime: toIsoDateTime(record.createTime),
        shoeSize: record.shoeSize || '-',
        status: mapDrawRecordStatus(record.status, record.orderNo),
      };
    });
  },

  // 获取抽签结果 - GET /user/draw/result/{drawId}
  getDrawResult: async (drawId: number): Promise<DrawRecord> => {
    const result = await get<RawDrawResult>(`/user/draw/result/${drawId}`);
    let drawName = `抽签活动 #${drawId}`;
    let shoeImage = '/images/default-banner.svg';

    try {
      const detail = await fetchDrawDetailVO(drawId);
      const draw = mapDrawDetailToDraw(detail);
      drawName = draw.name;
      shoeImage = draw.shoeImage;
    } catch {
      // ignore
    }

    return {
      id: String(result.id || `${drawId}-${Date.now()}`),
      drawId: String(result.drawId),
      drawName,
      shoeImage,
      joinTime: toIsoDateTime(result.createTime) || new Date().toISOString(),
      shoeSize: result.shoeSize || '-',
      status: mapDrawRecordStatus(result.status, result.orderNo),
    };
  },

  // 获取中奖选项 - GET /user/draw/win/{drawId}/options
  getWinOptions: async (drawId: number): Promise<DrawWinOptionsVO> => {
    return get(`/user/draw/win/${drawId}/options`);
  },

  // 确认购买 - POST /user/draw/win/{drawId}/confirm
  confirmWin: async (
    drawId: number,
    data: { addressBookId?: number; shoeSize?: string; items?: { skuId: number; shoeSize: string }[] }
  ): Promise<string> => {
    return post(`/user/draw/win/${drawId}/confirm`, data);
  },


  // 获取中奖名单 - GET /user/draw/winners/{drawId}
  getWinners: async (drawId: number): Promise<{ nickname: string; shoeSize: string }[]> => {
    const winners = await get<RawDrawWinner[]>(`/user/draw/winners/${drawId}`);
    return (winners || []).map((winner) => ({
      nickname: winner.userName || `用户${winner.userId}`,
      shoeSize: winner.shoeSize,
    }));
  },
};

// 优惠券相关API
export const couponApi = {
  // 获取我的优惠券 - GET /user/profile/coupons
  getMyCoupons: async (): Promise<Coupon[]> => {
    const result = await get<PageResult<RawUserCouponVO>>('/user/profile/coupons', {
      params: { page: 1, size: 200 },
    });
    return (result?.records || []).map(mapRawUserCouponToCoupon);
  },

  // 获取可用优惠券 - GET /user/coupon/available
  getAvailableCoupons: async (amount: number): Promise<Coupon[]> => {
    const data = await get<RawCoupon[]>('/user/coupon/available', { params: { amount } });
    return (data || []).map(mapRawCouponToCoupon);
  },

  // 领取优惠券 - POST /user/coupon/claim/{couponId}
  claimCoupon: async (couponId: number): Promise<void> => {
    return post(`/user/coupon/claim/${couponId}`);
  },
};

// 物流相关API
export const logisticsApi = {
  // 获取物流详情 - GET /user/logistics/{orderNo}
  getLogistics: async (orderNo: string): Promise<Logistics> => {
    const data = await get<RawLogisticsDetailVO>(`/user/logistics/${orderNo}`);
    if (!data || !data.logistics) {
      return {
        orderNo,
        company: '',
        trackingNo: '',
        status: '物流处理中',
        traces: [],
      };
    }

    return {
      orderNo: data.logistics.orderNo,
      company: data.logistics.expressCompany,
      trackingNo: data.logistics.expressNo,
      status: mapLogisticsStatusText(data.logistics.status),
      traces: (data.traces || []).map((trace) => ({
        time: toIsoDateTime(trace.operateTime),
        content: trace.description,
      })),
    };
  },
};

// 门店相关API
export const outletApi = {
  // 获取附近门店 - GET /user/outlet/nearby
  getNearbyOutlets: async (params: {
    longitude: number;
    latitude: number;
    radiusMeters?: number;
    limit?: number;
  }): Promise<Outlet[]> => {
    const data = await get<RawOutletNearbyVO[]>('/user/outlet/nearby', { params });
    return (data || []).map((item) => ({
      id: String(item.id),
      name: item.name,
      address: item.address,
      phone: item.phone,
      longitude: toNumber(item.longitude),
      latitude: toNumber(item.latitude),
      distance: item.distanceMeters,
      businessHours: item.businessHours,
      images: [],
    }));
  },
};

// 鞋款相关API
export const shoeApi = {
  // 获取鞋款列表 - GET /user/shoe/list
  getShoeList: async (categoryIds?: number[], sort?: string): Promise<ShoeSpu[]> => {
    const params: Record<string, unknown> = {};
    if (categoryIds && categoryIds.length > 0) {
      params.categoryIds = categoryIds;
    }
    if (sort) {
      params.sort = sort;
    }
    if (Object.keys(params).length > 0) {
      return get('/user/shoe/list', {
        params,
        paramsSerializer: {
          indexes: null,
        },
      });
    }
    return get('/user/shoe/list');
  },

  // 获取鞋款详情 - GET /user/shoe/{id}
  getShoeDetail: async (id: number): Promise<ShoeSpuDetail> => {
    return get(`/user/shoe/${id}`);
  },

  // 搜索鞋款
  searchShoes: async (keyword: string): Promise<ShoeSpu[]> => {
    return get('/user/shoe/list', { params: { keyword } });
  },
};

// SPU-SKU 相关API
export const spuApi = {
  // 获取SPU列表 - GET /user/shoe/list
  getSpuList: async (categoryIds?: number[], sort?: string): Promise<ShoeSpu[]> => {
    const params: Record<string, unknown> = {};
    if (categoryIds && categoryIds.length > 0) {
      params.categoryIds = categoryIds;
    }
    if (sort) {
      params.sort = sort;
    }
    if (Object.keys(params).length > 0) {
      return get('/user/shoe/list', {
        params,
        paramsSerializer: {
          indexes: null,
        },
      });
    }
    return get('/user/shoe/list');
  },

  // 获取SPU详情 - GET /user/shoe/{id}
  getSpuDetail: async (id: number): Promise<ShoeSpuDetail> => {
    return get(`/user/shoe/${id}`);
  },

  // 获取SKU详情 - GET /user/shoe/sku/{skuId}
  getSkuDetail: async (skuId: number): Promise<ShoeSku> => {
    return get(`/user/shoe/sku/${skuId}`);
  },
};

// 分类相关API
export const categoryApi = {
  // 获取分类列表 - GET /user/category/list
  getCategoryList: async (type?: number): Promise<Category[]> => {
    const params = type !== undefined ? { type } : {};
    return get('/user/category/list', { params });
  },
};

// 评论相关API
export const commentApi = {
  // 获取评论列表 - GET /user/comment/list
  getCommentList: async (spuId: number): Promise<Comment[]> => {
    return get('/user/comment/list', { params: { spuId } });
  },

  // 发表评论 - POST /user/comment
  postComment: async (data: {
    spuId: number;
    orderNumber?: string;
    content: string;
    rating: number;
    images?: string | string[];
  }): Promise<void> => {
    const payload = {
      ...data,
      images: Array.isArray(data.images) ? JSON.stringify(data.images) : data.images,
    };
    return post('/user/comment', payload);
  },

  // 获取我的评论列表 - GET /user/comment/my
  getMyComments: async (): Promise<MyComment[]> => {
    return get('/user/comment/my');
  },
};

// 组合包相关API
export const bundleApi = {
  // 获取组合包列表 - GET /user/bundle/list
  getBundleList: async (): Promise<Bundle[]> => {
    return get('/user/bundle/list');
  },

  // 获取组合包详情 - GET /user/bundle/detail/{id}
  getBundleDetail: async (id: number): Promise<BundleVO> => {
    return get(`/user/bundle/detail/${id}`);
  },

  // 获取组合包鞋款 - GET /user/bundle/shoe/{id}
  getBundleShoes: async (id: number): Promise<Shoe[]> => {
    return get(`/user/bundle/shoe/${id}`);
  },
};

// 店铺相关API
export const shopApi = {
  // 获取店铺状态 - GET /user/shop/status
  getShopStatus: async (): Promise<number> => {
    return get('/user/shop/status');
  },
};

// 消息相关API
export const messageApi = {
  // 获取消息列表
  getMessageList: async (): Promise<Message[]> => {
    const result = await listNotifications({ page: 1, size: 50 });
    return (result.records || []).map((notification) => ({
      id: notification.id != null
        ? String(notification.id)
        : notification.clientId ?? `temp-${notification.type}-${notification.createTime}`,
      type: mapNotificationToMessageType(notification.type),
      title: mapNotificationTitle(notification),
      content: mapNotificationContent(notification),
      createTime: toIsoDateTime(notification.createTime),
      isRead: notification.isRead === 1,
    }));
  },

  // 标记消息已读
  markMessageRead: async (id: string): Promise<void> => {
    const messageId = Number(id);
    if (!Number.isNaN(messageId)) {
      await markAsRead(messageId);
    }
  },
};

// 空投相关 API
export const airdropApi = {
  // 领取空投 - POST /user/airdrop/receive/{airdropId}
  receiveAirdrop: async (airdropId: number): Promise<void> => {
    return post(`/user/airdrop/receive/${airdropId}`);
  },

  // 获取我的空投记录 - GET /user/profile/airdrops
  getMyAirdrops: async (): Promise<string[]> => {
    const result = await get<PageResult<AirdropRecordVO>>('/user/profile/airdrops', {
      params: { page: 1, size: 100 },
    });
    const records = result?.records || [];
    // 返回已领取的空投 ID 列表
    return Array.from(new Set(records.map((record) => String(record.airdropId || record.id))));
  },
};

// 活动相关API
export const activityApi = {
  // 获取轮播图活动列表 - GET /user/activity/banner
  getBannerActivities: async (): Promise<ActivityBanner[]> => {
    const data = await get<RawActivityBanner[]>('/user/activity/banner');
    return (data || []).map((item) => ({
      id: String(item.id),
      type: item.type === 'draw' || item.type === 'airdrop' ? item.type : 'default',
      title: item.title || '鞋宙',
      description: item.description || '',
      image: item.image || '/images/default-banner.svg',
      startTime: toIsoDateTime(item.startTime),
      endTime: toIsoDateTime(item.endTime),
      drawTime: toIsoDateTime(item.drawTime),
      bannerStatus:
        item.bannerStatus === 'warmup' || item.bannerStatus === 'active' || item.bannerStatus === 'drawn'
          ? item.bannerStatus
          : 'default',
    }));
  },
};
// 心得相关API
export const experienceApi = {
  // 发布心得 - POST /user/experience/posts
  createPost: async (data: ExperiencePostDTO): Promise<ExperiencePost> => {
    return post('/user/experience/posts', data);
  },

  // 按商品查询心得列表 - GET /user/experience/posts
  listPostsByProduct: async (params: {
    productType: number;
    productId: number;
    sortBy?: string;
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperiencePost>> => {
    return get('/user/experience/posts', { params });
  },

  // 查询所有公开心得列表 - GET /user/experience/posts/all
  listAllPosts: async (params?: {
    sortBy?: string;
    keyword?: string;
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperiencePost>> => {
    return get('/user/experience/posts/all', { params });
  },

  // 查询关注用户的心得列表 - GET /user/experience/posts/following
  listFollowingPosts: async (params?: {
    sortBy?: string;
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperiencePost>> => {
    return get('/user/experience/posts/following', { params });
  },

  // 查询点赞过的心得列表 - GET /user/experience/posts/liked
  listLikedPosts: async (params?: {
    sortBy?: string;
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperiencePost>> => {
    return get('/user/experience/posts/liked', { params });
  },

  // 查询用户发布的心得列表 - GET /user/experience/users/{userId}/posts
  listUserPosts: async (userId: number, params?: {
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperiencePost>> => {
    return get(`/user/experience/users/${userId}/posts`, { params });
  },

  // 查询心得详情 - GET /user/experience/posts/{id}
  getPostDetail: async (id: number): Promise<ExperiencePostDetail> => {
    return get(`/user/experience/posts/${id}`);
  },

  // 点赞心得 - POST /user/experience/posts/{id}/like
  likePost: async (id: number): Promise<void> => {
    return post(`/user/experience/posts/${id}/like`);
  },

  // 取消点赞心得 - DELETE /user/experience/posts/{id}/like
  unlikePost: async (id: number): Promise<void> => {
    return del(`/user/experience/posts/${id}/like`);
  },

  // 查询点赞用户列表 - GET /user/experience/posts/{id}/likes
  listLikeUsers: async (id: number, params?: {
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperienceUserProfile>> => {
    return get(`/user/experience/posts/${id}/likes`, { params });
  },

  // 查询评论列表 - GET /user/experience/posts/{postId}/comments
  listComments: async (postId: number, params?: {
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperienceComment>> => {
    return get(`/user/experience/posts/${postId}/comments`, { params });
  },

  // 发表评论 - POST /user/experience/comments
  createComment: async (data: ExperienceCommentDTO): Promise<ExperienceComment> => {
    return post('/user/experience/comments', data);
  },

  // 回复评论 - POST /user/experience/comments/{commentId}/reply
  replyComment: async (commentId: number, data: ExperienceReplyDTO): Promise<ExperienceReply> => {
    return post(`/user/experience/comments/${commentId}/reply`, data);
  },

  // 点赞评论 - POST /user/experience/comments/{commentId}/like
  likeComment: async (commentId: number): Promise<void> => {
    return post(`/user/experience/comments/${commentId}/like`);
  },

  // 取消点赞评论 - DELETE /user/experience/comments/{commentId}/like
  unlikeComment: async (commentId: number): Promise<void> => {
    return del(`/user/experience/comments/${commentId}/like`);
  },

  // 删除自己的评论 - DELETE /user/experience/comments/{commentId}
  deleteComment: async (commentId: number): Promise<void> => {
    return del(`/user/experience/comments/${commentId}`);
  },

  // 隐藏自己的评论 - PUT /user/experience/comments/{commentId}/hide
  hideOwnComment: async (commentId: number): Promise<void> => {
    return put(`/user/experience/comments/${commentId}/hide`);
  },

  // 取消隐藏自己的评论 - PUT /user/experience/comments/{commentId}/unhide
  unhideOwnComment: async (commentId: number): Promise<void> => {
    return put(`/user/experience/comments/${commentId}/unhide`);
  },

  // 修改自己的评论 - PUT /user/experience/comments/{commentId}
  updateComment: async (commentId: number, content: string): Promise<void> => {
    return put(`/user/experience/comments/${commentId}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    });
  },

  // 删除自己的回复 - DELETE /user/experience/comments/replies/{replyId}
  deleteReply: async (replyId: number): Promise<void> => {
    return del(`/user/experience/comments/replies/${replyId}`);
  },

  // 隐藏自己的回复 - PUT /user/experience/comments/replies/{replyId}/hide
  hideOwnReply: async (replyId: number): Promise<void> => {
    return put(`/user/experience/comments/replies/${replyId}/hide`);
  },

  // 取消隐藏自己的回复 - PUT /user/experience/comments/replies/{replyId}/unhide
  unhideOwnReply: async (replyId: number): Promise<void> => {
    return put(`/user/experience/comments/replies/${replyId}/unhide`);
  },

  // 修改自己的回复 - PUT /user/experience/comments/replies/{replyId}
  updateReply: async (replyId: number, content: string): Promise<void> => {
    return put(`/user/experience/comments/replies/${replyId}`, content, {
      headers: { 'Content-Type': 'text/plain' }
    });
  },

  // 关注用户 - POST /user/experience/follow/{userId}
  followUser: async (userId: number): Promise<void> => {
    return post(`/user/experience/follow/${userId}`);
  },

  // 取消关注用户 - DELETE /user/experience/follow/{userId}
  unfollowUser: async (userId: number): Promise<void> => {
    return del(`/user/experience/follow/${userId}`);
  },

  // 查询关注列表 - GET /user/experience/follow/following
  listFollowing: async (params?: {
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperienceUserProfile>> => {
    return get('/user/experience/follow/following', { params });
  },

  // 查询粉丝列表 - GET /user/experience/follow/followers
  listFollowers: async (params?: {
    page?: number;
    size?: number;
  }): Promise<PageResult<ExperienceUserProfile>> => {
    return get('/user/experience/follow/followers', { params });
  },

  // 查询用户资料 - GET /user/experience/users/{userId}/profile
  getUserProfile: async (userId: number): Promise<ExperienceUserProfile> => {
    return get(`/user/experience/users/${userId}/profile`);
  },

  // 修改心得 - PUT /user/experience/posts/{id}
  updatePost: async (id: number, data: { content: string; images?: string[] }): Promise<ExperiencePost> => {
    return put(`/user/experience/posts/${id}`, data);
  },

  // 删除心得 - DELETE /user/experience/posts/{id}
  deletePost: async (id: number): Promise<void> => {
    return del(`/user/experience/posts/${id}`);
  },

  // 隐藏心得 - PUT /user/experience/posts/{id}/hide
  hidePost: async (id: number): Promise<void> => {
    return put(`/user/experience/posts/${id}/hide`);
  },

  // 取消隐藏心得 - PUT /user/experience/posts/{id}/unhide
  unhidePost: async (id: number): Promise<void> => {
    return put(`/user/experience/posts/${id}/unhide`);
  },
};

export const commonApi = {
  uploadImage: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await request.post('/user/common/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return (response as { data: string }).data;
  },
};







