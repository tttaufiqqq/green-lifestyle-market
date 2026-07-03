import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { orderApi } from './order.api'
import OrderTimeline from './OrderTimeline'
import RefundModal from './RefundModal'

export default function OrderDetailPage() {
  const { orderNo } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [showRefund, setShowRefund] = useState(false)

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', orderNo],
    queryFn: () => orderApi.getOrder(orderNo),
  })

  const cancelMut = useMutation({
    mutationFn: () => orderApi.cancel(orderNo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['order', orderNo] }),
  })
  const receiptMut = useMutation({
    mutationFn: () => orderApi.confirmReceipt(orderNo),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['order', orderNo] }),
  })

  if (isLoading) return <p className="p-8 text-zinc-500">Loading…</p>
  if (!order) return <p className="p-8 text-zinc-500">Order not found.</p>

  const canCancel    = ['PENDING_PAYMENT', 'PAID'].includes(order.status)
  const canReceipt   = ['SHIPPED', 'READY_FOR_MEETUP'].includes(order.status)
  const canRefund    = ['CONFIRMED', 'SHIPPED', 'READY_FOR_MEETUP'].includes(order.status)

  return (
    <main className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      <button onClick={() => navigate('/orders')} className="text-sm text-zinc-500 hover:text-zinc-800">
        &larr; My Orders
      </button>

      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold font-mono">{order.orderNo}</h1>
        <StatusBadge status={order.status} />
      </div>

      <OrderInfoCard order={order} />

      {order.courier && (
        <div className="bg-sky-50 border border-sky-200 rounded-xl p-4 text-sm">
          <p className="font-medium text-sky-800">Shipping Info</p>
          <p className="text-sky-700 mt-1">Courier: <strong>{order.courier}</strong></p>
          <p className="text-sky-700">Tracking: <strong>{order.trackingNo}</strong></p>
        </div>
      )}
      {order.meetupNote && (
        <div className="bg-teal-50 border border-teal-200 rounded-xl p-4 text-sm">
          <p className="font-medium text-teal-800">Meetup Info</p>
          <p className="text-teal-700 mt-1">{order.meetupNote}</p>
        </div>
      )}

      <ItemsCard order={order} />
      <TotalsCard order={order} />
      <OrderTimeline order={order} />

      {(canCancel || canReceipt || canRefund) && (
        <div className="flex flex-wrap gap-3 pt-2">
          {canReceipt && (
            <button onClick={() => receiptMut.mutate()}
              disabled={receiptMut.isPending}
              className="btn-primary disabled:opacity-50">
              {receiptMut.isPending ? 'Confirming…' : 'Confirm Receipt'}
            </button>
          )}
          {canRefund && (
            <button onClick={() => setShowRefund(true)} className="btn-outline">
              Request Refund
            </button>
          )}
          {canCancel && (
            <button onClick={() => cancelMut.mutate()}
              disabled={cancelMut.isPending}
              className="text-sm text-red-600 hover:text-red-800 underline">
              {cancelMut.isPending ? 'Cancelling…' : 'Cancel Order'}
            </button>
          )}
        </div>
      )}

      {cancelMut.error && <p className="text-sm text-red-600">{cancelMut.error.message}</p>}
      {receiptMut.error && <p className="text-sm text-red-600">{receiptMut.error.message}</p>}
      {showRefund && <RefundModal orderNo={orderNo} onClose={() => setShowRefund(false)} />}
    </main>
  )
}

function StatusBadge({ status }) {
  const cls = {
    PENDING_PAYMENT: 'bg-yellow-100 text-yellow-800', PAID: 'bg-blue-100 text-blue-800',
    CONFIRMED: 'bg-indigo-100 text-indigo-800', SHIPPED: 'bg-sky-100 text-sky-800',
    READY_FOR_MEETUP: 'bg-teal-100 text-teal-800', COMPLETED: 'bg-emerald-100 text-emerald-800',
    CANCELLED: 'bg-red-100 text-red-800', EXPIRED: 'bg-zinc-100 text-zinc-500',
    REFUND_REQUESTED: 'bg-orange-100 text-orange-800',
  }[status] ?? 'bg-zinc-100 text-zinc-500'
  return <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${cls}`}>{status.replace(/_/g, ' ')}</span>
}

function OrderInfoCard({ order }) {
  return (
    <div className="bg-white border border-zinc-200 rounded-xl p-4 text-sm space-y-1">
      <Row label="Seller"       val={order.sellerName} />
      <Row label="Fulfilment"   val={order.fulfilmentMethod} />
      {order.fulfilmentMethod === 'SHIPPING' && order.shipAddress1 && (
        <Row label="Deliver to"
          val={`${order.shipName}, ${order.shipAddress1}, ${order.shipCity} ${order.shipPostcode}`} />
      )}
    </div>
  )
}

function ItemsCard({ order }) {
  return (
    <div className="bg-white border border-zinc-200 rounded-xl divide-y divide-zinc-100">
      {order.items?.map((item, i) => (
        <div key={i} className="flex justify-between items-center p-4 text-sm">
          <div>
            <p className="font-medium">{item.titleSnapshot}</p>
            <p className="text-zinc-400 text-xs">{item.conditionSnapshot} × {item.quantity}</p>
          </div>
          <p className="font-medium">RM {Number(item.lineTotal).toFixed(2)}</p>
        </div>
      ))}
    </div>
  )
}

function TotalsCard({ order }) {
  return (
    <div className="bg-white border border-zinc-200 rounded-xl p-4 text-sm space-y-1">
      <Row label="Subtotal"  val={`RM ${Number(order.subtotal).toFixed(2)}`} />
      {Number(order.shippingFee) > 0 && <Row label="Shipping" val={`RM ${Number(order.shippingFee).toFixed(2)}`} />}
      <div className="flex justify-between font-bold border-t border-zinc-100 pt-2 mt-1">
        <span>Total</span><span className="text-emerald-700">RM {Number(order.total).toFixed(2)}</span>
      </div>
    </div>
  )
}

function Row({ label, val }) {
  return (
    <div className="flex justify-between">
      <span className="text-zinc-500">{label}</span>
      <span className="font-medium text-right max-w-xs">{val}</span>
    </div>
  )
}
