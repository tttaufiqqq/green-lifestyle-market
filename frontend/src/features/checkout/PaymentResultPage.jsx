import { useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { paymentApi } from './checkout.api'
import { useCartStore } from '../../stores/cart'

const TERMINAL = new Set(['SUCCESS', 'FAILED', 'EXPIRED', 'REVIEW'])

export default function PaymentResultPage() {
  const { paymentNo } = useParams()
  const setCount = useCartStore(s => s.setCount)

  const { data, isLoading } = useQuery({
    queryKey: ['payment', paymentNo],
    queryFn: () => paymentApi.getStatus(paymentNo),
    refetchInterval: (data) => (!data || !TERMINAL.has(data?.status)) ? 3000 : false,
  })

  useEffect(() => {
    if (data?.status === 'SUCCESS') setCount(0)
  }, [data?.status, setCount])

  if (isLoading || !data) {
    return (
      <main className="max-w-md mx-auto px-4 py-16 text-center">
        <p className="text-zinc-500">Checking payment status…</p>
      </main>
    )
  }

  const { status, orderNos } = data

  if (status === 'SUCCESS') {
    return (
      <main className="max-w-md mx-auto px-4 py-16 text-center space-y-4">
        <div className="text-5xl">✓</div>
        <h1 className="text-2xl font-bold text-emerald-700">Payment Successful</h1>
        <p className="text-zinc-500">
          Your payment has been received. Orders have been confirmed.
        </p>
        {orderNos?.length > 0 && (
          <div className="text-sm text-zinc-600 bg-zinc-50 rounded-lg p-3 space-y-1">
            {orderNos.map(no => <p key={no} className="font-mono">{no}</p>)}
          </div>
        )}
        <Link to="/" className="btn-primary inline-block mt-2">Continue Shopping</Link>
      </main>
    )
  }

  if (status === 'FAILED' || status === 'EXPIRED') {
    return (
      <main className="max-w-md mx-auto px-4 py-16 text-center space-y-4">
        <div className="text-5xl">✗</div>
        <h1 className="text-2xl font-bold text-red-600">
          Payment {status === 'EXPIRED' ? 'Expired' : 'Failed'}
        </h1>
        <p className="text-zinc-500">
          {status === 'EXPIRED'
            ? 'Your payment session has expired. Please try again.'
            : 'Payment was not completed. Please try again.'}
        </p>
        <div className="flex justify-center gap-3 mt-2">
          <Link to="/cart" className="btn-primary">Back to Cart</Link>
          <Link to="/" className="btn-outline">Browse Products</Link>
        </div>
      </main>
    )
  }

  if (status === 'REVIEW') {
    return (
      <main className="max-w-md mx-auto px-4 py-16 text-center space-y-4">
        <div className="text-5xl">?</div>
        <h1 className="text-2xl font-bold text-amber-600">Payment Under Review</h1>
        <p className="text-zinc-500">
          We received your payment but need to verify it. We will update your orders shortly.
        </p>
        <Link to="/" className="btn-primary inline-block mt-2">Go Home</Link>
      </main>
    )
  }

  // Still PENDING — keep polling
  return (
    <main className="max-w-md mx-auto px-4 py-16 text-center space-y-4">
      <div className="animate-spin text-4xl">⟳</div>
      <h1 className="text-xl font-semibold">Awaiting payment confirmation…</h1>
      <p className="text-sm text-zinc-500">This page will update automatically. Do not close.</p>
    </main>
  )
}
