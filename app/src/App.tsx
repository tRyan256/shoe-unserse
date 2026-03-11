import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { MobileNav } from '@/components/shared';
import { Toaster } from '@/components/ui/sonner';
import { useUserStore } from '@/stores';
import { useNotificationSocket } from '@/hooks/use-notification-socket';

// 页面导入
import CommunityPage from '@/pages/community';
import CommunityDetailPage from '@/pages/community/detail';
import CommunityPublishPage from '@/pages/community/publish';
import CommunityEditPage from '@/pages/community/edit';
import ShopPage from '@/pages/shop';
import ActivityPage from '@/pages/activity';
import UserPage from '@/pages/user';
import LoginPage from '@/pages/login';
import CartPage from '@/pages/cart';
import OrderConfirmPage from '@/pages/order/confirm';
import OrderPaymentPage from '@/pages/order/payment';
import OrderListPage from '@/pages/order/list';
import OrderDetailPage from '@/pages/order/detail';
import OrderReviewPage from '@/pages/order/review';
import DrawListPage from '@/pages/draw/list';
import DrawDetailPage from '@/pages/draw/detail';
import MyDrawPage from '@/pages/draw/my';
import DrawWinConfirmPage from '@/pages/draw/win-confirm';
import MyCouponPage from '@/pages/coupon/my';
import NearbyOutletPage from '@/pages/outlet/nearby';
import AddressListPage from '@/pages/address/list';
import AddressEditPage from '@/pages/address/edit';
import CategoryPage from '@/pages/category';
import ShoeDetailPage from '@/pages/shoe/detail';
import BundleListPage from '@/pages/bundle/list';
import BundleDetailPage from '@/pages/bundle/detail';
import SettingsPage from '@/pages/settings';
import PersonalInfoPage from '@/pages/user/PersonalInfo';
import AddressPage from '@/pages/user/Address';
import CouponPage from '@/pages/user/Coupon';
import DrawHistoryPage from '@/pages/user/DrawHistory';
import DrawDetailViewPage from '@/pages/user/DrawHistory/DrawDetailView';
import AirdropHistoryPage from '@/pages/user/AirdropHistory';
import ReviewsPage from '@/pages/user/Reviews';
import MessagePage from '@/pages/message';
import MessageCenterPage from '@/pages/user/MessageCenter';
import AgreementPage from '@/pages/agreement';
import HelpPage from '@/pages/help';

// 受保护路由组件
function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isLoggedIn = useUserStore((state) => state.isLoggedIn);
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function App() {
  useNotificationSocket();

  return (
    <BrowserRouter>
      <div className="app-shell relative">
        <Routes>
          {/* 公开路由 - 新导航结构 */}
          {/* 默认首页重定向到购买页面 */}
          <Route path="/" element={<Navigate to="/shop" replace />} />
          
          {/* 鞋宙 - 社区页面 */}
          <Route path="/community" element={<CommunityPage />} />
          <Route path="/community/detail/:id" element={<CommunityDetailPage />} />
          <Route
            path="/community/publish"
            element={
              <ProtectedRoute>
                <CommunityPublishPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/community/edit/:id"
            element={
              <ProtectedRoute>
                <CommunityEditPage />
              </ProtectedRoute>
            }
          />
          
          {/* 购买 - 商城页面 */}
          <Route path="/shop" element={<ShopPage />} />
          
          {/* 活动 - 抽签/空投/预热 */}
          <Route
            path="/activity"
            element={
              <ProtectedRoute>
                <ActivityPage />
              </ProtectedRoute>
            }
          />
          
          {/* 商品详情 */}
          <Route path="/shoe/:id" element={<ShoeDetailPage />} />
          
          {/* 分类 */}
          <Route path="/category" element={<CategoryPage />} />

          {/* 组合包 */}
          <Route path="/bundle/list" element={<BundleListPage />} />
          <Route path="/bundle/:id" element={<BundleDetailPage />} />

          {/* 抽签详情 */}
          <Route
            path="/draw/list"
            element={
              <ProtectedRoute>
                <DrawListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/draw/detail/:id"
            element={
              <ProtectedRoute>
                <DrawDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/draw/info/:id"
            element={
              <ProtectedRoute>
                <DrawDetailViewPage />
              </ProtectedRoute>
            }
          />

          {/* 登录 */}
          <Route path="/login" element={<LoginPage />} />

          {/* 用户协议 */}
          <Route path="/agreement" element={<AgreementPage />} />

          {/* 帮助与反馈 */}
          <Route path="/help" element={<HelpPage />} />

          {/* 受保护路由 */}
          <Route path="/user" element={<UserPage />} />
          <Route
            path="/cart"
            element={
              <ProtectedRoute>
                <CartPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/order/confirm"
            element={
              <ProtectedRoute>
                <OrderConfirmPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/order/payment"
            element={
              <ProtectedRoute>
                <OrderPaymentPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/order/list"
            element={
              <ProtectedRoute>
                <OrderListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/order/detail/:orderNumber"
            element={
              <ProtectedRoute>
                <OrderDetailPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/order/review/:orderNumber"
            element={
              <ProtectedRoute>
                <OrderReviewPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/draw/my"
            element={
              <ProtectedRoute>
                <MyDrawPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/draw/win-confirm/:id"
            element={
              <ProtectedRoute>
                <DrawWinConfirmPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/coupon/my"
            element={
              <ProtectedRoute>
                <MyCouponPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/outlet/nearby"
            element={
              <ProtectedRoute>
                <NearbyOutletPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/address/list"
            element={
              <ProtectedRoute>
                <AddressListPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/address/edit/:id?"
            element={
              <ProtectedRoute>
                <AddressEditPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <SettingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/personal-info"
            element={
              <ProtectedRoute>
                <PersonalInfoPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/address"
            element={
              <ProtectedRoute>
                <AddressPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/coupon"
            element={
              <ProtectedRoute>
                <CouponPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/draw-history"
            element={
              <ProtectedRoute>
                <DrawHistoryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/draw-history/detail/:id"
            element={
              <ProtectedRoute>
                <DrawDetailViewPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/airdrop-history"
            element={
              <ProtectedRoute>
                <AirdropHistoryPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/reviews"
            element={
              <ProtectedRoute>
                <ReviewsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/message"
            element={
              <ProtectedRoute>
                <MessagePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user/message-center"
            element={
              <ProtectedRoute>
                <MessageCenterPage />
              </ProtectedRoute>
            }
          />

          {/* 404 重定向 */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
        <MobileNav />
        <Toaster />
      </div>
    </BrowserRouter>
  );
}

export default App;




