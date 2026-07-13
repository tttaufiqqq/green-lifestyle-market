import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { orderApi } from './order.api'

const TABS = [
  { key: 'needs-action', label: 'Needs Action' },
  { key: 'confirmed',    label: 'Confirmed' },
  { key: 'in-progress',  label: 'Shipped/Meetup' },
  { key: 'completed',    label: 'Completed' },
  { key: 'all',          label: 'All' },
]

const STATUS_BADGE = {
  PAID: 'bg-blue-100 text-blue-800', CONFIRMED: 'bg-indigo-100 text-indigo-800',
  SHIPPED: 'bg-sky-100 text-sky-800', READY_FOR_MEETUP: 'bg-teal-100 text-teal-800',
  COMPLETED: 'bg-emerald-100 text-emerald-800', CANCELLED: 'bg-red-100 text-red-800',
  REFUND_REQUESTED: 'bg-orange-100 text-orange-800',
}

export default function MySalesPage() {
  const [tab, setTab] = useState('needs-action')
  const qc = useQueryClient()

  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['sales', tab],
    queryFn: () => orderApi.getSales(tab),
  })

  const confirmMut = useMutation({
    mutationFn: (orderNo) => orderApi.confirm(orderNo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['sales'] }),
    meta: { successMessage: 'Order confirmed' },
  })
  const rejectMut = useMutation({
    mutationFn: (orderNo) => orderApi.reject(orderNo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['sales'] }),
    meta: { successMessage: 'Order rejected' },
  })

  return (
    <main className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">My Sales</h1>

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
        <p className="text-zinc-400 text-sm text-center py-12">No sales found.</p>
      )}

      <div className="space-y-3">
        {orders.map(o => (
          <div key={o.orderNo}
            className="bg-white border border-zinc-200 rounded-xl p-4 flex flex-wrap items-center gap-3">
            <div className="min-w-0 flex-1">
              <p className="font-mono text-sm font-medium text-zinc-800">{o.orderNo}</p>
              <p className="text-sm text-zinc-500">Buyer: {o.counterpartName}</p>
              <p className="text-sm font-semibold text-emerald-700 mt-0.5">RM {Number(o.total).toFixed(2)}</p>
            </div>
            <div className="flex items-center gap-2 shrink-0 flex-wrap">
              <span className={`text-xs font-medium px-2 py-1 rounded-full ${STATUS_BADGE[o.status] ?? 'bg-zinc-100 text-zinc-500'}`}>
                {o.status.replace(/_/g, ' ')}
              </span>
              {o.status === 'PAID' && (
                <>
                  <button onClick={() => confirmMut.mutate(o.orderNo)}
                    disabled={confirmMut.isPending}
                    className="btn-primary text-xs px-3 py-1.5 disabled:opacity-50">
                    Confirm
                  </button>
                  <button onClick={() => rejectMut.mutate(o.orderNo)}
                    disabled={rejectMut.isPending}
                    className="text-xs border border-red-300 text-red-600 hover:bg-red-50 px-3 py-1.5 rounded-lg">
                    Reject
                  </button>
                </>
              )}
              <Link to={`/sales/${o.orderNo}`}
                className="text-xs border border-zinc-300 text-zinc-600 hover:bg-zinc-50 px-3 py-1.5 rounded-lg">
                View
              </Link>
            </div>
          </div>
        ))}
      </div>
    </main>
  )
}
