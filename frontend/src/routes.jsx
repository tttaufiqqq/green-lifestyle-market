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
import AdminCategoriesPage from './features/admin/categories/AdminCategoriesPage'
import AdminProductsPage from './features/admin/products/AdminProductsPage'
import CartPage from './features/cart/CartPage'
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
      { path: 'admin/categories', element: <RequireAdmin><AdminCategoriesPage /></RequireAdmin> },
      { path: 'admin/products',   element: <RequireAdmin><AdminProductsPage /></RequireAdmin> },
    ],
  },
])

export default router
