// ==================== 通用API响应类型 ====================

/**
 * 通用API响应类型
 */
export interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data: T;
}

/**
 * 分页结果类型
 */
export interface PageResult<T = any> {
  total: number;
  records: T[];
}

// ==================== 用户类型 ====================

// 用户类型
export interface User {
  id: string;
  name?: string;
  nickname: string;
  avatar: string;
  phone: string;
  followerCount?: number;
  followingCount?: number;
  likedCount?: number;
}

// 地址类型
export interface Address {
  id: number;
  consignee: string;
  phone: string;
  province: string;
  city: string;
  district: string;
  detail: string;
  label?: string;
  isDefault: boolean;
}

// ==================== SPU-SKU 类型 ====================

/**
 * SPU（标准化产品单元）列表展示
 */
export interface ShoeSpu {
  id: number;
  name: string;
  brand?: string;
  model?: string;
  description?: string;
  isLimited?: number;
  releaseDate?: string;
  status?: number;
  minPrice: number;
  maxPrice: number;
  defaultImage: string;
  colorCount: number;
  totalStock?: number;
  salesCount?: number;
  colorNames?: string;
  categoryNames?: string[];  // 后端返回数组
}

/**
 * SPU 详情（含SKU列表）
 */
export interface ShoeSpuDetail extends ShoeSpu {
  categoryIds?: number[];
  skus: ShoeSku[];
}

/**
 * SKU（库存量单元）
 */
export interface ShoeSku {
  id: number;
  spuId: number;
  spuName?: string;
  colorName: string;
  colorCode?: string;
  price: number;
  image: string;
  images?: string[];
  skuCode?: string;
  isDefault: number;
  status?: number;
  stock?: number;
  salesCount?: number;
  sizes: ShoeSkuSize[];
}

/**
 * SKU 尺码库存
 */
export interface ShoeSkuSize {
  id: number;
  skuId: number;
  size: string;
  stock: number;
}

// ==================== 旧版商品类型（兼容） ====================

// 商品类型（对应后端 ShoeVO）
export interface Shoe {
  id: number;
  name: string;
  brand?: string;
  model?: string;
  color?: string;
  colorName?: string;
  releaseDate?: string;
  isLimited?: number;
  categoryIds?: number[];
  categoryNames?: string[];
  price: number;
  image?: string;
  description?: string;
  status?: number;
  stock?: number;
  salesCount?: number;
  sizes: ShoeSize[];
  createTime?: string;
  updateTime?: string;
}

export interface ShoeSize {
  id?: number;
  shoeId?: number;
  size: string;
  stock: number;
}

// 购物车类型
export interface CartItem {
  id: string;
  shoeId: string;      // 保持兼容，后续可移除
  spuId?: number;      // 新增：SPU ID
  skuId?: number;      // 新增：SKU ID
  bundleId?: string;   // 新增：组合包ID
  shoeName: string;
  shoeImage: string;
  size: string;
  price: number;
  quantity: number;
  selected?: boolean;
}

// ==================== 订单类型 ====================

/**
 * 订单状态枚举
 * 1待付款 2待发货 3已发货 4运输中 5派送中 6已签收 7已取消 8已评价
 */
export type OrderStatus = 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8;

/**
 * 支付状态枚举
 * 0未支付 1已支付 2退款
 */
export type PayStatus = 0 | 1 | 2;

/**
 * 订单类型枚举
 * 1普通订单 2抽签订单
 */
export type OrderType = 1 | 2;

/**
 * 订单详情（对应后端 OrderDetail 实体）
 */
export interface OrderDetail {
  name: string;
  spuId?: number;
  skuId?: number;
  bundleId?: number;
  shoeSize: string;
  number: number;
  amount: number;
  image: string;
}

/**
 * 订单实体（对应后端 Orders 实体）
 */
export interface Order {
  number: string;
  orderType?: OrderType;
  status: OrderStatus;
  orderTime: string;
  checkoutTime?: string;
  payMethod: number;
  payStatus?: PayStatus;
  amount: number;
  remark?: string;
  phone: string;
  address: string;
  consignee: string;
  cancelReason?: string;
  cancelTime?: string;
  orderDetailList?: OrderDetail[];
  logistics?: Logistics;
  logisticsTraceList?: LogisticsTrace[];
}

/**
 * 订单提交商品项
 */
export interface OrderSubmitItem {
  spuId?: number;
  skuId?: number;
  bundleId?: number;
  shoeSize: string;
  quantity: number;
}

/**
 * 订单提交参数（对应后端 OrdersSubmitDTO）
 */
export interface OrderSubmitParams {
  addressBookId: number;
  payMethod: number;
  remark?: string;
  estimatedDeliveryTime?: string;
  deliveryStatus?: number;
  tablewareNumber?: number;
  tablewareStatus?: number;
  packAmount?: number;
  amount: number;
  couponId?: number;
  items?: OrderSubmitItem[];  // 新增：商品列表
}

/**
 * 订单支付参数（对应后端 OrdersPaymentDTO）
 */
export interface OrderPaymentParams {
  orderNumber: string;
  payMethod: number;
}

/**
 * 订单列表查询参数
 */
export interface OrderListParams {
  page: number;
  pageSize: number;
  status?: OrderStatus;
  statusList?: OrderStatus[];
}

/**
 * 异步提交订单结果（对应后端 OrderAsyncSubmitVO）
 */
export interface OrderAsyncSubmitResult {
  orderNumber: string;
  status: string;
}

/**
 * 异步状态查询结果（对应后端 OrderAsyncStatusVO）
 */
export interface OrderAsyncStatus {
  orderNumber: string;
  status: string;
  error?: string;
}

/**
 * 支付结果（对应后端 OrderPaymentVO）
 */
export interface OrderPaymentResult {
  nonceStr: string;
  paySign: string;
  timeStamp: string;
  signType: string;
  packageStr: string;
}

/**
 * 订单提交结果（对应后端 OrderSubmitVO）
 */
export interface OrderSubmitResult {
  orderNumber: string;
  orderAmount: number;
  orderTime: string;
}

/**
 * 订单分页结果
 */
export interface OrderPageResult extends PageResult<Order> {}

/**
 * 订单VO（包含详情和物流信息）
 */
export interface OrderVO extends Order {
  orderDishes?: string;
  orderDetailList: OrderDetail[];
  logistics?: Logistics;
  logisticsTraceList?: LogisticsTrace[];
}

// 优惠券类型
export interface Coupon {
  id: string;
  name: string;
  amount: number;
  minSpend?: number;
  startTime: string;
  endTime: string;
  status: 'available' | 'used' | 'expired';
  description?: string;
}

// 抽签类型
export interface Draw {
  id: string;
  name: string;
  image: string;
  shoeName: string;
  shoeImage: string;
  startTime: string;
  endTime: string;
  status: 'upcoming' | 'ongoing' | 'ended';
  participantStatus?: 'not_joined' | 'joined' | 'won' | 'not_won';
  sizes: string[];
}

export interface DrawRecord {
  id: string;
  drawId: string;
  drawName: string;
  shoeImage: string;
  joinTime: string;
  shoeSize: string;
  status: 'pending' | 'won' | 'not_won' | 'abandoned' | 'purchased';
}

// 组合包列表项类型（对应后端 Bundle 实体）
export interface Bundle {
  id: string;
  name: string;
  description: string;
  image: string;
  price: number;
  status?: number;
  createTime?: string;
  updateTime?: string;
}

// 组合包列表项带鞋款预览类型
export interface BundleWithPreview extends Bundle {
  shoeCount?: number;
  previewImages?: string[];
}

export interface BundleShoe {
  shoeId: string;
  shoeName: string;
  shoeImage: string;
}

// 组合包详情VO（对应后端 BundleVO）
export interface BundleVO {
  id: string;
  name: string;
  description: string;
  image: string;
  price: number;
  status: number;
  createTime: string;
  updateTime: string;
  shoeCount: number;
  bundleShoes: BundleShoe[];
  shoeItems: Shoe[];
}

// 分类类型
export interface Category {
  id: number;
  name: string;
  icon?: string;
  image?: string;
  sort?: number;
  type?: number;
  status?: number;
}

// 评论类型（对应后端 CommentVO）
export interface Comment {
  id: number;
  spuId: number;
  userId: number;
  userName: string;
  userAvatar: string;
  content: string;
  images: string;
  rating: number;
  createTime: string;
}

// 我的评论类型（对应后端 MyCommentVO）
export interface MyComment {
  id: number;
  spuId: number;
  spuName: string;
  spuImage: string;
  content: string;
  images: string;
  rating: number;
  createTime: string;
}

// 物流类型
export interface Logistics {
  orderNo: string;
  company: string;
  trackingNo: string;
  status: string;
  traces: LogisticsTrace[];
}

export interface LogisticsTrace {
  time: string;
  content: string;
}

// 门店类型
export interface Outlet {
  id: string;
  name: string;
  address: string;
  phone: string;
  longitude: number;
  latitude: number;
  distance?: number;
  businessHours: string;
  images?: string[];
}

// 消息类型
export interface Message {
  id: string;
  type: 'system' | 'order' | 'activity';
  title: string;
  content: string;
  createTime: string;
  isRead: boolean;
}

// 空投类型
export interface Airdrop {
  id: string;
  name: string;
  description: string;
  type: 'coupon' | 'points';
  value: number;
  status: 'available' | 'claimed';
}

// 店铺状态
export interface ShopStatus {
  isOpen: boolean;
  openTime?: string;
  closeTime?: string;
}

// 活动轮播图类型
export interface ActivityBanner {
  id: string;
  type: 'draw' | 'airdrop' | 'default';
  title: string;
  description: string;
  image: string;
  startTime: string;
  endTime: string;
  drawTime?: string;
  bannerStatus: 'warmup' | 'active' | 'drawn' | 'default';
}

// 空投记录类型（后端 UserAirdropHistoryVO）
export interface AirdropRecordVO {
  id: number;
  airdropId?: number;
  userId?: number;
  status?: number;
  createTime?: string;
}

// ==================== 心得系统类型 ====================

/**
 * 心得帖子
 */
export interface ExperiencePost {
  id: number;
  userId: number;
  userName: string;
  userAvatar: string;
  content: string;
  productType: number;
  productId: number;
  productName: string;
  productImage: string;
  images: string[];
  likeCount: number;
  commentCount: number;
  isLiked: boolean;
  isFollowed: boolean;
  hidden?: number;
  createTime: string;
  updateTime: string;
}

/**
 * 心得详情
 */
export interface ExperiencePostDetail extends ExperiencePost {
  productType: number;
  productId: number;
  productName: string;
  productImage: string;
  product?: {
    id: number;
    name: string;
    image: string;
    price?: number;
  };
}

/**
 * 心得评论
 */
export interface ExperienceComment {
  id: number;
  postId: number;
  userId: number;
  userName: string;
  userAvatar: string;
  content: string;
  likeCount: number;
  replyCount: number;
  isLiked: boolean;
  isAuthor: boolean;
  hidden?: number;
  createTime: string;
  replies?: ExperienceReply[];
}

/**
 * 评论回复
 */
export interface ExperienceReply {
  id: number;
  commentId: number;
  userId: number;
  userName: string;
  userAvatar: string;
  content: string;
  targetUserId?: number;      // 被回复者ID
  targetUserName?: string;    // 被回复者昵称
  targetReplyId?: number;     // 被回复的回复ID
  likeCount: number;
  isLiked: boolean;
  isAuthor: boolean;
  hidden?: number;
  createTime: string;
  replies?: ExperienceReply[]; // 嵌套回复列表（用于展示多级回复）
}

/**
 * 用户资料（心得系统）
 */
export interface ExperienceUserProfile {
  userId: number;
  userName: string;
  userAvatar: string;
  followerCount: number;
  followingCount: number;
  likedCount: number;
  postCount: number;
  isFollowed: boolean;
}

/**
 * 发布心得参数
 */
export interface ExperiencePostDTO {
  content: string;
  productType: number;
  productId: number;
  images?: string[];
}

/**
 * 发表评论参数
 */
export interface ExperienceCommentDTO {
  postId: number;
  content: string;
}

/**
 * 回复评论参数
 */
export interface ExperienceReplyDTO {
  commentId: number;
  content: string;
  targetUserId: number;      // 被回复者ID（必填）
  targetReplyId?: number;    // 被回复的回复ID（可选，回复回复时传入）
}

/**
 * 心得列表查询参数
 */
export interface ExperiencePostQueryParams {
  productType?: number;
  productId?: number;
  sortBy?: 'time' | 'time_asc' | 'like';
  page?: number;
  size?: number;
}


