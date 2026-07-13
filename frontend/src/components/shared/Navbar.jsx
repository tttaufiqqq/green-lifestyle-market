import { Link } from 'react-router-dom'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import NotificationBell from '../../features/notifications/NotificationBell'

export default function Navbar() {
  const user  = useAuthStore(s => s.user)
  const logout = useAuthStore(s => s.logout)
  const count = useCartStore(s => s.count)

  return (
    <header className="sticky top-0 z-50 bg-white border-b border-zinc-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 md:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="text-emerald-600 font-bold text-xl tracking-tight">
          GreenMarket
        </Link>

        <nav className="hidden md:flex items-center gap-6 text-sm text-zinc-600">
          <Link to="/" className="hover:text-zinc-900 transition-colors">Browse</Link>
          {user && (
            <>
              <Link to="/listings" className="hover:text-zinc-900 transition-colors">My Listings</Link>
              <Link to="/orders"   className="hover:text-zinc-900 transition-colors">My Orders</Link>
              <Link to="/sales"    className="hover:text-zinc-900 transition-colors">My Sales</Link>
            </>
          )}
          {user && (
            <Link to="/payouts" className="hover:text-zinc-900 transition-colors">Payouts</Link>
          )}
          {user?.role === 'ADMIN' && (
            <Link to="/admin" className="hover:text-zinc-900 transition-colors">Admin</Link>
          )}
        </nav>

        <div className="flex items-center gap-3 text-sm">
          {user ? (
            <>
              <NotificationBell />
              <Link to="/cart" className="relative p-1 text-zinc-600 hover:text-zinc-900 transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                {count > 0 && (
                  <span className="absolute -top-1 -right-1 bg-emerald-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                    {count > 99 ? '99+' : count}
                  </span>
                )}
              </Link>
              <Link to="/profile" className="text-zinc-600 hover:text-zinc-900 transition-colors">
                {user.name}
              </Link>
              <button onClick={logout}
                className="text-zinc-400 hover:text-zinc-700 transition-colors">
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="text-zinc-600 hover:text-zinc-900 transition-colors">
                Sign in
              </Link>
              <Link to="/register"
                className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium transition-colors">
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
