import { useQuery } from '@tanstack/react-query'
import { payoutApi } from './payout.api'

const STATUS_BADGE = {
  PENDING: 'bg-amber-100 text-amber-700',
  PAID:    'bg-emerald-100 text-emerald-700',
}

function fmtMYT(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('en-MY', {
    timeZone: 'Asia/Kuala_Lumpur',
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

export default function MyPayoutsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['me', 'payouts'],
    queryFn: payoutApi.getMyPayouts,
  })

  const eligible = data?.eligible ?? []
  const payouts  = data?.payouts  ?? []

  if (isLoading) return <p className="text-zinc-500 p-6">Loading…</p>

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 space-y-8">
      <h1 className="text-2xl font-bold">My Payouts</h1>

      {/* Eligible orders waiting for payout */}
      <section>
        <h2 className="text-base font-semibold mb-3">Pending Settlement</h2>
        {eligible.length === 0 ? (
          <p className="text-zinc-400 text-sm">No orders awaiting payout.</p>
        ) : (
          <div className="bg-white border border-zinc-200 rounded-xl divide-y divide-zinc-100">
            {eligible.map(o => (
              <div key={o.orderId} className="flex items-center justify-between px-4 py-3">
                <div>
                  <p className="font-mono text-sm font-medium">{o.orderNo}</p>
                  <p className="text-xs text-zinc-400">Completed {fmtMYT(o.completedAt)}</p>
                </div>
                <p className="text-sm font-semibold text-emerald-700">RM {Number(o.sellerNet).toFixed(2)}</p>
              </div>
            ))}
            <div className="flex justify-between px-4 py-3 bg-zinc-50 rounded-b-xl">
              <span className="text-sm font-semibold text-zinc-600">Total pending</span>
              <span className="text-sm font-bold text-emerald-700">
                RM {eligible.reduce((s, o) => s + Number(o.sellerNet), 0).toFixed(2)}
              </span>
            </div>
          </div>
        )}
      </section>

      {/* Payout history */}
      <section>
        <h2 className="text-base font-semibold mb-3">Payout History</h2>
        {payouts.length === 0 ? (
          <p className="text-zinc-400 text-sm">No payouts yet.</p>
        ) : (
          <div className="space-y-3">
            {payouts.map(p => (
              <div key={p.id} className="bg-white border border-zinc-200 rounded-xl p-4">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="font-mono text-sm font-medium">{p.payoutNo}</p>
                    <p className="text-xs text-zinc-400 mt-0.5">Created {fmtMYT(p.createdAt)}</p>
                    {p.paidAt && (
                      <p className="text-xs text-zinc-400">Paid {fmtMYT(p.paidAt)}</p>
                    )}
                    {p.bankRef && (
                      <p className="text-xs text-zinc-500 mt-1">Ref: <span className="font-mono">{p.bankRef}</span></p>
                    )}
                  </div>
                  <div className="text-right shrink-0">
                    <p className="text-lg font-bold text-zinc-800">RM {Number(p.amount).toFixed(2)}</p>
                    <span className={`inline-block text-xs font-medium px-2 py-0.5 rounded-full mt-1 ${STATUS_BADGE[p.status] ?? 'bg-zinc-100 text-zinc-600'}`}>
                      {p.status}
                    </span>
                  </div>
                </div>
                {p.items.length > 0 && (
                  <details className="mt-3">
                    <summary className="text-xs text-zinc-400 cursor-pointer hover:text-zinc-600">
                      {p.items.length} order{p.items.length !== 1 ? 's' : ''}
                    </summary>
                    <div className="mt-2 space-y-1">
                      {p.items.map((it, i) => (
                        <div key={i} className="flex justify-between text-xs text-zinc-600 pl-2">
                          <span className="font-mono">{it.orderNo}</span>
                          <span>RM {Number(it.amount).toFixed(2)}</span>
                        </div>
                      ))}
                    </div>
                  </details>
                )}
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
