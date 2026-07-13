import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi } from './admin.api'

export default function AdminPayoutsPage() {
  const [tab, setTab]     = useState('eligible') // 'eligible' | 'pending'
  const [modal, setModal] = useState(null) // { type: 'create'|'markpaid', seller?, payout? }
  const qc = useQueryClient()

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['admin', 'payouts'] })
    setModal(null)
  }

  const { data: eligible = [], isLoading: loadingEligible } = useQuery({
    queryKey: ['admin', 'payouts', 'eligible'],
    queryFn: adminApi.getEligible,
    enabled: tab === 'eligible',
  })

  const { data: pending = [], isLoading: loadingPending } = useQuery({
    queryKey: ['admin', 'payouts', 'pending'],
    queryFn: adminApi.getPending,
    enabled: tab === 'pending',
  })

  const createMut  = useMutation({ mutationFn: adminApi.createPayout, onSuccess: invalidate, meta: { successMessage: 'Payout created' } })
  const markPaidMut= useMutation({
    mutationFn: ({ id, bankRef }) => adminApi.markPaid(id, bankRef),
    onSuccess: invalidate,
    meta: { successMessage: 'Payout marked as paid' },
  })

  const isLoading = tab === 'eligible' ? loadingEligible : loadingPending

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Payouts</h1>

      <div className="flex gap-1">
        {['eligible', 'pending'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-2 rounded-lg text-sm font-medium capitalize transition-colors
              ${tab === t ? 'bg-emerald-600 text-white' : 'bg-zinc-100 text-zinc-600 hover:bg-zinc-200'}`}>
            {t === 'eligible' ? 'Eligible Sellers' : 'Pending Payouts'}
          </button>
        ))}
      </div>

      {isLoading && <p className="text-zinc-500">Loading…</p>}

      {tab === 'eligible' && !isLoading && (
        <div className="space-y-4">
          {eligible.length === 0 && (
            <p className="text-center text-zinc-400 text-sm py-8">No sellers with eligible orders.</p>
          )}
          {eligible.map(seller => (
            <div key={seller.sellerId} className="bg-white border border-zinc-200 rounded-xl p-4">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="font-semibold">{seller.sellerName}</p>
                  <p className="text-sm text-zinc-500">{seller.bankName} — {seller.accountNo}</p>
                  {!seller.bankVerified && (
                    <span className="inline-block mt-1 text-xs font-medium bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full">
                      Bank unverified
                    </span>
                  )}
                </div>
                <div className="text-right shrink-0">
                  <p className="text-lg font-bold text-emerald-700">RM {Number(seller.totalNet).toFixed(2)}</p>
                  <p className="text-xs text-zinc-400">{seller.orderCount} order{seller.orderCount !== 1 ? 's' : ''}</p>
                  <button
                    onClick={() => setModal({ type: 'create', seller })}
                    disabled={!seller.bankVerified}
                    className="mt-2 btn-primary text-xs px-3 py-1.5 disabled:opacity-40 disabled:cursor-not-allowed">
                    Create Payout
                  </button>
                </div>
              </div>
              <details className="mt-3">
                <summary className="text-xs text-zinc-400 cursor-pointer hover:text-zinc-600">
                  View {seller.orderCount} order{seller.orderCount !== 1 ? 's' : ''}
                </summary>
                <div className="mt-2 space-y-1">
                  {seller.orders.map(o => (
                    <div key={o.orderId} className="flex justify-between text-xs text-zinc-600 pl-2">
                      <span className="font-mono">{o.orderNo}</span>
                      <span>RM {Number(o.sellerNet).toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              </details>
            </div>
          ))}
        </div>
      )}

      {tab === 'pending' && !isLoading && (
        <div className="space-y-3">
          {pending.length === 0 && (
            <p className="text-center text-zinc-400 text-sm py-8">No pending payouts.</p>
          )}
          {pending.map(p => (
            <div key={p.id} className="bg-white border border-zinc-200 rounded-xl p-4 flex items-center gap-4">
              <div className="flex-1 min-w-0">
                <p className="font-mono text-sm font-medium">{p.payoutNo}</p>
                <p className="text-sm text-zinc-500">{p.sellerName}</p>
                <p className="text-sm font-semibold text-emerald-700 mt-0.5">RM {Number(p.amount).toFixed(2)}</p>
                <p className="text-xs text-zinc-400">{p.items.length} order{p.items.length !== 1 ? 's' : ''}</p>
              </div>
              <button onClick={() => setModal({ type: 'markpaid', payout: p })}
                className="btn-primary text-xs px-3 py-1.5 shrink-0">
                Mark Paid
              </button>
            </div>
          ))}
        </div>
      )}

      {modal?.type === 'create' && (
        <CreatePayoutModal
          seller={modal.seller}
          isPending={createMut.isPending}
          onConfirm={(orderIds) => createMut.mutate({ sellerId: modal.seller.sellerId, orderIds })}
          onClose={() => setModal(null)} />
      )}
      {modal?.type === 'markpaid' && (
        <MarkPaidModal
          payout={modal.payout}
          isPending={markPaidMut.isPending}
          onConfirm={(bankRef) => markPaidMut.mutate({ id: modal.payout.id, bankRef })}
          onClose={() => setModal(null)} />
      )}
    </div>
  )
}

function CreatePayoutModal({ seller, onConfirm, onClose, isPending }) {
  const [selected, setSelected] = useState(seller.orders.map(o => o.orderId))

  const toggle = (id) => setSelected(prev =>
    prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id])

  const totalNet = seller.orders
    .filter(o => selected.includes(o.orderId))
    .reduce((s, o) => s + Number(o.sellerNet), 0)

  return (
    <ModalShell title="Create Payout" onClose={onClose}>
      <p className="text-sm text-zinc-500 mb-3">
        Seller: <strong>{seller.sellerName}</strong> — {seller.bankName} {seller.accountNo}
      </p>
      <div className="space-y-1 max-h-52 overflow-y-auto mb-3">
        {seller.orders.map(o => (
          <label key={o.orderId} className="flex items-center gap-3 text-sm cursor-pointer p-1.5 rounded hover:bg-zinc-50">
            <input type="checkbox" checked={selected.includes(o.orderId)} onChange={() => toggle(o.orderId)}
              className="rounded border-zinc-300 text-emerald-600 focus:ring-emerald-500" />
            <span className="font-mono flex-1">{o.orderNo}</span>
            <span className="text-zinc-600">RM {Number(o.sellerNet).toFixed(2)}</span>
          </label>
        ))}
      </div>
      <div className="flex justify-between text-sm font-semibold border-t pt-2 mb-4">
        <span>Total</span>
        <span className="text-emerald-700">RM {totalNet.toFixed(2)}</span>
      </div>
      <div className="flex gap-3 justify-end">
        <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
        <button onClick={() => onConfirm(selected)}
          disabled={selected.length === 0 || isPending}
          className="btn-primary text-sm disabled:opacity-50">
          {isPending ? 'Creating…' : `Create Payout (${selected.length})`}
        </button>
      </div>
    </ModalShell>
  )
}

function MarkPaidModal({ payout, onConfirm, onClose, isPending }) {
  const [bankRef, setBankRef] = useState('')
  return (
    <ModalShell title="Mark Payout Paid" onClose={onClose}>
      <p className="text-sm text-zinc-500 mb-1">Payout: <strong>{payout.payoutNo}</strong></p>
      <p className="text-sm text-zinc-500 mb-3">Amount: <strong>RM {Number(payout.amount).toFixed(2)}</strong></p>
      <input value={bankRef} onChange={e => setBankRef(e.target.value)}
        placeholder="Bank transfer reference *"
        className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 mb-4" />
      <div className="flex gap-3 justify-end">
        <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
        <button onClick={() => onConfirm(bankRef)} disabled={!bankRef.trim() || isPending}
          className="btn-primary text-sm disabled:opacity-50">
          {isPending ? 'Processing…' : 'Confirm Paid'}
        </button>
      </div>
    </ModalShell>
  )
}

function ModalShell({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700 text-xl">&times;</button>
        </div>
        {children}
      </div>
    </div>
  )
}
