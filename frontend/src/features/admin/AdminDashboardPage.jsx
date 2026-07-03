import { useQuery } from '@tanstack/react-query'
import { adminApi } from './admin.api'

export default function AdminDashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['admin', 'dashboard'],
    queryFn: adminApi.getDashboard,
    refetchInterval: 60_000,
  })

  if (isLoading) return <p className="text-zinc-500">Loading dashboard…</p>

  const d = data ?? {}

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold">Dashboard</h1>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Users"             value={d.userCount} />
        <StatCard label="Active Listings"   value={d.activeListings} />
        <StatCard label="Escrow Held"       value={`RM ${Number(d.escrowHeld ?? 0).toFixed(2)}`} />
        <StatCard label="Platform Fees MTD" value={`RM ${Number(d.platformFeesMtd ?? 0).toFixed(2)}`} />
      </div>

      {/* Orders by status */}
      {d.ordersByStatus && (
        <div className="bg-white border border-zinc-200 rounded-xl p-5">
          <h2 className="font-semibold text-zinc-700 mb-3">Orders by Status</h2>
          <div className="flex flex-wrap gap-3">
            {Object.entries(d.ordersByStatus).map(([status, count]) => (
              <div key={status} className="text-sm">
                <span className="text-zinc-500">{status.replace(/_/g, ' ')}</span>
                <span className="ml-1.5 font-bold text-zinc-800">{count}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Alerts */}
      <div className="bg-white border border-zinc-200 rounded-xl p-5 space-y-3">
        <h2 className="font-semibold text-zinc-700">Alerts</h2>
        {d.reviewPayments > 0 && (
          <Alert level="warn" text={`${d.reviewPayments} payment(s) flagged for review — check Reconciliation`} />
        )}
        {d.pendingRefunds > 0 && (
          <Alert level="info" text={`${d.pendingRefunds} refund request(s) pending your action`} />
        )}
        {d.pendingPayouts > 0 && (
          <Alert level="info" text={`${d.pendingPayouts} payout(s) pending bank transfer`} />
        )}
        {!d.reviewPayments && !d.pendingRefunds && !d.pendingPayouts && (
          <p className="text-sm text-emerald-600">All clear — no pending actions.</p>
        )}
      </div>
    </div>
  )
}

function StatCard({ label, value }) {
  return (
    <div className="bg-white border border-zinc-200 rounded-xl p-5">
      <p className="text-xs text-zinc-400 font-medium uppercase tracking-wide mb-1">{label}</p>
      <p className="text-2xl font-bold text-zinc-800">{value}</p>
    </div>
  )
}

function Alert({ level, text }) {
  const cls = level === 'warn'
    ? 'bg-amber-50 border-amber-200 text-amber-800'
    : 'bg-blue-50 border-blue-200 text-blue-800'
  return (
    <div className={`border rounded-lg px-4 py-2.5 text-sm ${cls}`}>
      {level === 'warn' ? '⚠ ' : 'ℹ '}{text}
    </div>
  )
}
