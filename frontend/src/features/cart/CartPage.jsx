import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { cartApi } from './cart.api'
import { useCartStore } from '../../stores/cart'
import SellerGroup from './SellerGroup'

function computeGrandTotal(groups) {
  return groups.reduce((sum, g) =>
    sum + g.items.reduce((s, i) => s + Number(i.price) * i.quantity, 0), 0)
}

export default function CartPage() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const setCount = useCartStore(s => s.setCount)

  const { data: cart, isLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: cartApi.getCart,
    onSuccess: (data) => setCount(data.totalItems),
  })

  const mutOpts = {
    onSuccess: (data) => {
      qc.setQueryData(['cart'], data)
      setCount(data.totalItems)
    },
  }

  const updateMut = useMutation({ mutationFn: ({ id, qty }) => cartApi.updateQuantity(id, qty), ...mutOpts })
  const removeMut = useMutation({ mutationFn: (id) => cartApi.removeItem(id), ...mutOpts })

  const updating = updateMut.isPending || removeMut.isPending

  const handleUpdateQty = (id, qty) => {
    if (qty < 1) return removeMut.mutate(id)
    updateMut.mutate({ id, qty })
  }

  if (isLoading) return <p className="p-8 text-zinc-500">Loading cart…</p>

  const groups = cart?.groups ?? []
  const hasWarnings = cart?.hasWarnings ?? false
  const grandTotal = computeGrandTotal(groups)

  if (groups.length === 0) {
    return (
      <main className="max-w-2xl mx-auto px-4 py-16 text-center">
        <p className="text-4xl mb-4">🛒</p>
        <h1 className="text-xl font-semibold mb-2">Your cart is empty</h1>
        <p className="text-zinc-500 mb-6">Browse products and add them to your cart.</p>
        <Link to="/" className="btn-primary">Browse products</Link>
      </main>
    )
  }

  return (
    <main className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">Your Cart</h1>

      {hasWarnings && (
        <div className="mb-4 p-3 bg-amber-50 border border-amber-200 rounded-lg text-sm text-amber-800">
          Some items in your cart have changed. Please review before proceeding.
        </div>
      )}

      <div className="space-y-4">
        {groups.map(group => (
          <SellerGroup
            key={group.sellerId}
            group={group}
            onUpdateQty={handleUpdateQty}
            onRemove={(id) => removeMut.mutate(id)}
            updating={updating}
          />
        ))}
      </div>

      {/* Summary */}
      <div className="mt-6 bg-white rounded-xl border border-zinc-200 p-4">
        <div className="flex justify-between text-sm mb-1">
          <span className="text-zinc-500">Items</span>
          <span className="font-medium">{cart.totalItems}</span>
        </div>
        <div className="flex justify-between font-semibold text-base border-t border-zinc-100 pt-2 mt-2">
          <span>Subtotal</span>
          <span className="text-emerald-700">RM {grandTotal.toFixed(2)}</span>
        </div>
        <p className="text-xs text-zinc-400 mt-1">Shipping calculated at checkout</p>
        <button
          disabled={hasWarnings}
          onClick={() => navigate('/checkout')}
          className="btn-primary w-full mt-4 disabled:opacity-50 disabled:cursor-not-allowed">
          Proceed to Checkout
        </button>
        {hasWarnings && (
          <p className="text-xs text-amber-600 text-center mt-2">
            Resolve warnings above before checking out.
          </p>
        )}
      </div>
    </main>
  )
}
