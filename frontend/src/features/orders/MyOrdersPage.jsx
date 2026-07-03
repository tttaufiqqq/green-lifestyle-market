import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { orderApi } from './order.api'

const TABS = [
  { key: 'all',       label: 'All' },
  { key: 'to-pay',    label: 'To Pay' },
  { key: 'to-receive',label: 'To Receive' },
  { key: 'completed', label: 'Completed' },
  { key: 'cancelled', label: 'Cancelled/Refunds' },
]

const STATUS_BADGE = {
  PENDING_PAYMENT:  'bg-yellow-100 text-yellow-800',
  PAID:             'bg-blue-100 text-blue-800',
  CONFIRMED:        'bg-indigo-100 text-indigo-800',
  SHIPPED:          'bg-sky-100 text-sky-800',
  READY_FOR_MEETUP: 'bg-teal-100 text-teal-800',
  COMPLETED:        'bg-emerald-100 text-emerald-800',
  CANCELLED:        'bg-red-100 text-red-800',
  EXPIRED:          'bg-zinc-100 text-zinc-500',
  REFUND_REQUESTED: 'bg-orange-100 text-orange-800',
  REFUNDED:         'bg-purple-100 text-purple-800',
}

const STATUS_LABEL = {
  PENDING_PAYMENT: 'Pending Payment', PAID: 'Paid', CONFIRMED: 'Confirmed',
  SHIPPED: 'Shipped', READY_FOR_MEETUP: 'Ready for Meetup', COMPLETED: 'Completed',
  CANCELLED: 'Cancelled', EXPIRED: 'Expired',
  REFUND_REQUESTED: 'Refund Requested', REFUNDED: 'Refunded',
}

export default function MyOrdersPage() {
  const [tab, setTab] = useState('all')

  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['orders', tab],
    queryFn: () => orderApi.getOrders(tab),
  })

  return (
    <main className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Orders</h1>

      <div className="flex gap-1 overflow-x-auto mb-6 pb-1">
        {TABS.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`whitespace-nowrap px-4 py-2 rounded-lg text-sm font-medium transition-colors
              ${tab === t.key
                ? 'bg-emerald-600 text-white'
                : 'bg-zinc-100 text-zinc-600 hover:bg-zinc-200'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {isLoading && <p className="text-zinc-500">Loading…</p>}
      {!isLoading && orders.length === 0 && (
        <p className="text-zinc-400 text-sm text-center py-12">No orders found.</p>
      )}

      <div className="space-y-3">
        {orders.map(o => (
          <div key={o.orderNo}
            className="bg-white border border-zinc-200 rounded-xl p-4 flex items-center justify-between gap-3">
            <div className="min-w-0">
              <p className="font-mono text-sm font-medium text-zinc-800">{o.orderNo}</p>
              <p className="text-sm text-zinc-500 truncate">{o.counterpartName}</p>
              <p className="text-sm font-semibold text-emerald-700 mt-0.5">RM {Number(o.total).toFixed(2)}</p>
            </div>
            <div className="flex items-center gap-3 shrink-0">
              <span className={`text-xs font-medium px-2 py-1 rounded-full ${STATUS_BADGE[o.status] ?? 'bg-zinc-100 text-zinc-500'}`}>
                {STATUS_LABEL[o.status] ?? o.status}
              </span>
              <Link to={`/orders/${o.orderNo}`}
                className="btn-primary text-xs px-3 py-1.5">
                View
              </Link>
            </div>
          </div>
        ))}
      </div>
    </main>
  )
}
