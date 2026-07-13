import { NavLink, Outlet } from 'react-router-dom'

const NAV = [
  { to: '/admin',               label: 'Dashboard',     end: true },
  { to: '/admin/orders',        label: 'Orders'         },
  { to: '/admin/refunds',       label: 'Refunds'        },
  { to: '/admin/payouts',       label: 'Payouts'        },
  { to: '/admin/reconciliation',label: 'Reconciliation' },
  { to: '/admin/users',         label: 'Users'          },
  { to: '/admin/listings',      label: 'Listings'       },
  { to: '/admin/categories',    label: 'Categories'     },
  { to: '/admin/articles',      label: 'Articles'       },
]

export default function AdminLayout() {
  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      {/* Sidebar */}
      <aside className="w-52 shrink-0 border-r border-zinc-200 bg-zinc-50 py-6 px-3">
        <p className="text-xs font-bold text-zinc-400 uppercase tracking-wider px-3 mb-3">Admin</p>
        <nav className="space-y-0.5">
          {NAV.map(({ to, label, end }) => (
            <NavLink key={to} to={to} end={end}
              className={({ isActive }) =>
                `block px-3 py-2 rounded-lg text-sm font-medium transition-colors
                 ${isActive
                   ? 'bg-emerald-100 text-emerald-700'
                   : 'text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900'}`}>
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>

      {/* Main content */}
      <main className="flex-1 px-8 py-8 min-w-0">
        <Outlet />
      </main>
    </div>
  )
}
