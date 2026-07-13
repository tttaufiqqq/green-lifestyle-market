import { createBrowserRouter, Navigate } from 'react-router-dom'
import App from './App'
import BrowsePage from './features/catalog/BrowsePage'
import ProductDetailPage from './features/catalog/ProductDetailPage'
import LoginPage from './features/auth/LoginPage'
import RegisterPage from './features/auth/RegisterPage'
import VerifyEmailPage from './features/auth/VerifyEmailPage'
import ForgotPasswordPage from './features/auth/ForgotPasswordPage'
import ResetPasswordPage from './features/auth/ResetPasswordPage'
import ProfilePage from './features/auth/ProfilePage'
import MyListingsPage from './features/listings/MyListingsPage'
import ListingFormPage from './features/listings/ListingFormPage'
import CartPage from './features/cart/CartPage'
import CheckoutPage from './features/checkout/CheckoutPage'
import PaymentResultPage from './features/checkout/PaymentResultPage'
import MyOrdersPage from './features/orders/MyOrdersPage'
import OrderDetailPage from './features/orders/OrderDetailPage'
import MySalesPage from './features/orders/MySalesPage'
import SaleDetailPage from './features/orders/SaleDetailPage'
import MyPayoutsPage from './features/payouts/MyPayoutsPage'
import NotificationsPage from './features/notifications/NotificationsPage'
import ArticlesPage from './features/articles/ArticlesPage'
import ArticleDetailPage from './features/articles/ArticleDetailPage'
import AdminArticlesPage from './features/admin/articles/AdminArticlesPage'
import ArticleFormPage from './features/admin/articles/ArticleFormPage'
import HelpPage from './features/help/HelpPage'
import AdminLayout from './features/admin/AdminLayout'
import AdminDashboardPage from './features/admin/AdminDashboardPage'
import AdminUsersPage from './features/admin/AdminUsersPage'
import AdminOrdersPage from './features/admin/AdminOrdersPage'
import AdminRefundsPage from './features/admin/AdminRefundsPage'
import AdminPayoutsPage from './features/admin/AdminPayoutsPage'
import AdminReconciliationPage from './features/admin/AdminReconciliationPage'
import AdminCategoriesPage from './features/admin/categories/AdminCategoriesPage'
import AdminProductsPage from './features/admin/products/AdminProductsPage'
import { useAuthStore } from './stores/auth'

function RequireAuth({ children }) {
  const user = useAuthStore(s => s.user)
  const loading = useAuthStore(s => s.loading)
  if (loading) return null
  if (!user) return <Navigate to="/login" replace />
  return children
}

function RequireAdmin({ children }) {
  const user = useAuthStore(s => s.user)
  const loading = useAuthStore(s => s.loading)
  if (loading) return null
  if (!user || user.role !== 'ADMIN') return <Navigate to="/" replace />
  return children
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    children: [
      { index: true,              element: <BrowsePage /> },
      { path: 'products/:slug',   element: <ProductDetailPage /> },
      { path: 'login',            element: <LoginPage /> },
      { path: 'register',         element: <RegisterPage /> },
      { path: 'verify-email',     element: <VerifyEmailPage /> },
      { path: 'forgot-password',  element: <ForgotPasswordPage /> },
      { path: 'reset-password',   element: <ResetPasswordPage /> },
      { path: 'profile',          element: <RequireAuth><ProfilePage /></RequireAuth> },
      { path: 'listings',         element: <RequireAuth><MyListingsPage /></RequireAuth> },
      { path: 'listings/new',     element: <RequireAuth><ListingFormPage /></RequireAuth> },
      { path: 'listings/:id/edit',element: <RequireAuth><ListingFormPage /></RequireAuth> },
      { path: 'cart',             element: <RequireAuth><CartPage /></RequireAuth> },
      { path: 'checkout',         element: <RequireAuth><CheckoutPage /></RequireAuth> },
      { path: 'payment/result/:paymentNo', element: <RequireAuth><PaymentResultPage /></RequireAuth> },
      { path: 'orders',           element: <RequireAuth><MyOrdersPage /></RequireAuth> },
      { path: 'orders/:orderNo',  element: <RequireAuth><OrderDetailPage /></RequireAuth> },
      { path: 'sales',            element: <RequireAuth><MySalesPage /></RequireAuth> },
      { path: 'sales/:orderNo',   element: <RequireAuth><SaleDetailPage /></RequireAuth> },
      { path: 'payouts',          element: <RequireAuth><MyPayoutsPage /></RequireAuth> },
      { path: 'notifications',    element: <RequireAuth><NotificationsPage /></RequireAuth> },
      { path: 'articles',         element: <ArticlesPage /> },
      { path: 'articles/:slug',   element: <ArticleDetailPage /> },
      { path: 'help',             element: <HelpPage /> },
      { path: 'help/:slug',       element: <HelpPage /> },
      {
        path: 'admin',
        element: <RequireAdmin><AdminLayout /></RequireAdmin>,
        children: [
          { index: true,              element: <AdminDashboardPage /> },
          { path: 'orders',           element: <AdminOrdersPage /> },
          { path: 'refunds',          element: <AdminRefundsPage /> },
          { path: 'payouts',          element: <AdminPayoutsPage /> },
          { path: 'reconciliation',   element: <AdminReconciliationPage /> },
          { path: 'users',            element: <AdminUsersPage /> },
          { path: 'listings',         element: <AdminProductsPage /> },
          { path: 'categories',       element: <AdminCategoriesPage /> },
          { path: 'articles',         element: <AdminArticlesPage /> },
          { path: 'articles/new',     element: <ArticleFormPage /> },
          { path: 'articles/:id/edit',element: <ArticleFormPage /> },
        ],
      },
    ],
  },
])

export default router
