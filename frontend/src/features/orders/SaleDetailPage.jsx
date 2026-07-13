import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { orderApi } from './order.api'
import OrderTimeline from './OrderTimeline'
import { RejectModal, ShipModal, MeetupModal } from './SaleActionModals'

export default function SaleDetailPage() {
  const { orderNo } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [modal, setModal] = useState(null) // 'reject' | 'ship' | 'meetup'

  const { data: order, isLoading } = useQuery({
    queryKey: ['sale', orderNo],
    queryFn: () => orderApi.getSale(orderNo),
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['sale', orderNo] })

  const confirmMut = useMutation({ mutationFn: () => orderApi.confirm(orderNo), onSuccess: invalidate, meta: { successMessage: 'Order confirmed' } })
  const rejectMut  = useMutation({
    mutationFn: (req) => orderApi.reject(orderNo, req),
    onSuccess: () => { invalidate(); setModal(null) },
    meta: { successMessage: 'Order rejected' },
  })
  const shipMut    = useMutation({
    mutationFn: (req) => orderApi.ship(orderNo, req),
    onSuccess: () => { invalidate(); setModal(null) },
    meta: { successMessage: 'Marked as shipped' },
  })
  const meetupMut  = useMutation({
    mutationFn: (req) => orderApi.readyMeetup(orderNo, req),
    onSuccess: () => { invalidate(); setModal(null) },
    meta: { successMessage: 'Ready for meetup' },
  })

  if (isLoading) return <p className="p-8 text-zinc-500">Loading…</p>
  if (!order) return <p className="p-8 text-zinc-500">Sale not found.</p>

  const isShipping = order.fulfilmentMethod === 'SHIPPING'

  return (
    <main className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      <button onClick={() => navigate('/sales')} className="text-sm text-zinc-500 hover:text-zinc-800">
        &larr; My Sales
      </button>

      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold font-mono">{order.orderNo}</h1>
        <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-zinc-100 text-zinc-600">
          {order.status.replace(/_/g, ' ')}
        </span>
      </div>

      {/* Buyer info */}
      <div className="bg-white border border-zinc-200 rounded-xl p-4 text-sm space-y-1">
        <p className="font-medium text-zinc-700 mb-2">Buyer Information</p>
        <Row label="Buyer"       val={order.buyerName} />
        {order.buyerPhone && <Row label="Phone" val={order.buyerPhone} />}
        {!order.buyerPhone && order.status === 'PAID' && (
          <p className="text-xs text-zinc-400 italic">Phone revealed after confirming</p>
        )}
        <Row label="Fulfilment" val={order.fulfilmentMethod} />
        {isShipping && order.shipAddress1 && (
          <Row label="Ship to"
            val={`${order.shipName} — ${order.shipAddress1}, ${order.shipCity} ${order.shipPostcode}`} />
        )}
      </div>

      {/* Earnings */}
      <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-4 text-sm space-y-1">
        <p className="font-medium text-emerald-800 mb-2">Your Earnings</p>
        <Row label="Subtotal"     val={`RM ${Number(order.subtotal).toFixed(2)}`} />
        <Row label="Platform fee" val={`− RM ${Number(order.platformFee).toFixed(2)}`} />
        {Number(order.shippingFee) > 0 && <Row label="Shipping collected" val={`+ RM ${Number(order.shippingFee).toFixed(2)}`} />}
        <div className="flex justify-between font-bold border-t border-emerald-200 pt-2 mt-1">
          <span>Seller net</span>
          <span className="text-emerald-700">RM {Number(order.sellerNet).toFixed(2)}</span>
        </div>
      </div>

      {/* Items */}
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

      <OrderTimeline order={order} />

      {/* Actions */}
      <div className="flex flex-wrap gap-3 pt-2">
        {order.status === 'PAID' && (
          <>
            <button onClick={() => confirmMut.mutate()}
              disabled={confirmMut.isPending}
              className="btn-primary disabled:opacity-50">
              {confirmMut.isPending ? 'Confirming…' : 'Confirm Order'}
            </button>
            <button onClick={() => setModal('reject')} className="text-sm text-red-600 hover:text-red-800 underline">
              Reject
            </button>
          </>
        )}
        {order.status === 'CONFIRMED' && isShipping && (
          <button onClick={() => setModal('ship')} className="btn-primary">Mark Shipped</button>
        )}
        {order.status === 'CONFIRMED' && !isShipping && (
          <button onClick={() => setModal('meetup')} className="btn-primary">Ready for Meetup</button>
        )}
      </div>

      {confirmMut.error && <p className="text-sm text-red-600">{confirmMut.error.message}</p>}

      {modal === 'reject'  && <RejectModal  onConfirm={r => rejectMut.mutate({ reason: r })} onClose={() => setModal(null)} isPending={rejectMut.isPending} />}
      {modal === 'ship'    && <ShipModal    onConfirm={r => shipMut.mutate(r)}                onClose={() => setModal(null)} isPending={shipMut.isPending} />}
      {modal === 'meetup'  && <MeetupModal  onConfirm={r => meetupMut.mutate(r)}              onClose={() => setModal(null)} isPending={meetupMut.isPending} />}
    </main>
  )
}

function Row({ label, val }) {
  return (
    <div className="flex justify-between">
      <span className="text-zinc-500">{label}</span>
      <span className="font-medium text-right max-w-xs truncate">{val}</span>
    </div>
  )
}
