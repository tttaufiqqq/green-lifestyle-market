import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { adminApi } from './admin.api'

const STATUSES = ['', 'PAID', 'CONFIRMED', 'SHIPPED', 'READY_FOR_MEETUP',
                  'COMPLETED', 'CANCELLED', 'REFUND_REQUESTED', 'REFUNDED']

export default function AdminOrdersPage() {
  const [status, setStatus] = useState('')

  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['admin', 'orders', status],
    queryFn: () => adminApi.getOrders(status || undefined),
  })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Orders</h1>
        <select value={status} onChange={e => setStatus(e.target.value)}
          className="border border-zinc-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
          {STATUSES.map(s => <option key={s} value={s}>{s || 'All statuses'}</option>)}
        </select>
      </div>

      {isLoading && <p className="text-zinc-500">Loading…</p>}

      <div className="bg-white border border-zinc-200 rounded-xl overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-zinc-100 text-left text-zinc-400 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Order No</th>
              <th className="px-4 py-3">Seller / Buyer</th>
              <th className="px-4 py-3">Total</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Fulfilment</th>
              <th className="px-4 py-3">Date</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-50">
            {orders.map(o => (
              <tr key={o.orderNo} className="hover:bg-zinc-50">
                <td className="px-4 py-3 font-mono text-xs">{o.orderNo}</td>
                <td className="px-4 py-3 text-zinc-600">{o.counterpartName}</td>
                <td className="px-4 py-3 font-medium">RM {Number(o.total).toFixed(2)}</td>
                <td className="px-4 py-3">
                  <span className="text-xs px-2 py-0.5 rounded-full bg-zinc-100 text-zinc-600">
                    {o.status.replace(/_/g, ' ')}
                  </span>
                </td>
                <td className="px-4 py-3 text-zinc-500">{o.fulfilmentMethod}</td>
                <td className="px-4 py-3 text-zinc-400 text-xs">
                  {new Date(o.createdAt).toLocaleDateString('en-MY')}
                </td>
                <td className="px-4 py-3">
                  <Link to={`/admin/orders/${o.orderNo}`}
                    className="text-xs text-emerald-600 hover:underline">View</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!isLoading && orders.length === 0 && (
          <p className="text-center text-zinc-400 text-sm py-8">No orders found.</p>
        )}
      </div>
    </div>
  )
}
