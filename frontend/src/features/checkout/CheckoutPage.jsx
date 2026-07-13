import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { cartApi } from '../cart/cart.api'
import { checkoutApi } from './checkout.api'
import FulfilmentSelector from './FulfilmentSelector'

function initChoices(groups) {
  const map = {}
  for (const g of groups) {
    const method = g.allowMeetup ? 'MEETUP' : g.allowShipping ? 'SHIPPING' : null
    map[g.sellerId] = {
      method,
      shipName: '', shipPhone: '', shipAddress1: '', shipAddress2: '',
      shipPostcode: '', shipCity: '', shipState: '',
    }
  }
  return map
}

function buildRequest(choices) {
  return {
    fulfilments: Object.entries(choices).map(([sellerId, c]) => ({
      sellerId: Number(sellerId),
      method: c.method,
      ...(c.method === 'SHIPPING' ? {
        shipName: c.shipName, shipPhone: c.shipPhone,
        shipAddress1: c.shipAddress1, shipAddress2: c.shipAddress2,
        shipPostcode: c.shipPostcode, shipCity: c.shipCity, shipState: c.shipState,
      } : {}),
    })),
  }
}

export default function CheckoutPage() {
  const navigate = useNavigate()
  const [choices, setChoices] = useState(null)   // null = cart not loaded yet
  const [preview, setPreview] = useState(null)
  const [error, setError] = useState(null)

  const { isLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: cartApi.getCart,
    onSuccess: (data) => {
      if (!choices) setChoices(initChoices(data.groups ?? []))
    },
  })

  const previewMut = useMutation({
    mutationFn: () => checkoutApi.preview(buildRequest(choices)),
    onSuccess: (data) => { setPreview(data); setError(null) },
    onError: (e) => { setError(e.message ?? 'Preview failed'); setPreview(null) },
    meta: { suppressErrorToast: true },
  })

  const checkoutMut = useMutation({
    mutationFn: () => checkoutApi.checkout(buildRequest(choices)),
    onSuccess: (data) => { window.location.href = data.paymentUrl },
    onError: (e) => setError(e.message ?? 'Checkout failed'),
    meta: { suppressErrorToast: true },
  })

  const handleChoice = (sellerId, val) => {
    setChoices(prev => ({ ...prev, [sellerId]: val }))
    setPreview(null)
  }

  if (isLoading || !choices) return <p className="p-8 text-zinc-500">Loading…</p>

  const groups = Object.keys(choices)
  if (groups.length === 0) {
    return (
      <main className="max-w-2xl mx-auto px-4 py-16 text-center">
        <p className="text-zinc-500">Your cart is empty.</p>
      </main>
    )
  }

  return (
    <main className="max-w-2xl mx-auto px-4 py-8 space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/cart')}
          className="text-sm text-zinc-500 hover:text-zinc-800">
          &larr; Back to cart
        </button>
        <h1 className="text-2xl font-bold">Checkout</h1>
      </div>

      {error && (
        <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
          {error}
        </div>
      )}

      {/* Fulfilment choices per seller group */}
      <CartGroupsPanel choices={choices} onChange={handleChoice} />

      {/* Preview section */}
      {!preview ? (
        <button
          onClick={() => previewMut.mutate()}
          disabled={previewMut.isPending}
          className="btn-primary w-full disabled:opacity-50">
          {previewMut.isPending ? 'Calculating…' : 'Preview Order'}
        </button>
      ) : (
        <PreviewPanel preview={preview} />
      )}

      {preview && (
        <button
          onClick={() => checkoutMut.mutate()}
          disabled={checkoutMut.isPending}
          className="btn-primary w-full disabled:opacity-50">
          {checkoutMut.isPending ? 'Processing…' : `Confirm & Pay  RM ${preview.grandTotal}`}
        </button>
      )}
    </main>
  )
}

function CartGroupsPanel({ choices, onChange }) {
  // We need the group metadata from the cart query cache
  const { data: cart } = useQuery({ queryKey: ['cart'], queryFn: cartApi.getCart, staleTime: Infinity })
  const groups = cart?.groups ?? []

  return (
    <div className="space-y-4">
      {groups.map(g => (
        <div key={g.sellerId} className="bg-white border border-zinc-200 rounded-xl p-4">
          <FulfilmentSelector
            group={g}
            choice={choices[g.sellerId] ?? { method: null }}
            onChange={onChange}
          />
        </div>
      ))}
    </div>
  )
}

function PreviewPanel({ preview }) {
  return (
    <div className="bg-white border border-zinc-200 rounded-xl p-4 space-y-3">
      <h2 className="font-semibold text-zinc-700">Order Summary</h2>
      {preview.orders.map((o, i) => (
        <div key={i} className="text-sm space-y-1 pb-3 border-b border-zinc-100 last:border-0 last:pb-0">
          <p className="font-medium">{o.sellerName}</p>
          <Row label="Subtotal"      val={`RM ${o.subtotal}`} />
          {Number(o.shippingFee) > 0 && <Row label="Shipping" val={`RM ${o.shippingFee}`} />}
          <Row label="Platform fee"  val={`RM ${o.platformFee}`} />
          <Row label="Order total"   val={`RM ${o.total}`} bold />
        </div>
      ))}
      <div className="flex justify-between font-bold text-base pt-1">
        <span>Grand Total</span>
        <span className="text-emerald-700">RM {preview.grandTotal}</span>
      </div>
    </div>
  )
}

function Row({ label, val, bold }) {
  const cls = bold ? 'font-semibold' : 'text-zinc-500'
  return (
    <div className="flex justify-between">
      <span className={cls}>{label}</span>
      <span className={bold ? 'font-semibold' : ''}>{val}</span>
    </div>
  )
}
