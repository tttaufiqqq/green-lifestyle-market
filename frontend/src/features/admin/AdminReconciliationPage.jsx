import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { adminApi } from './admin.api'

function today() {
  return new Date().toISOString().slice(0, 10)
}

const ISSUE_LABELS = {
  OK:               { label: 'OK',               cls: 'bg-emerald-100 text-emerald-700' },
  MISSED_CALLBACK:  { label: 'Missed Callback',   cls: 'bg-amber-100 text-amber-700' },
  AMOUNT_MISMATCH:  { label: 'Amount Mismatch',   cls: 'bg-red-100 text-red-700' },
  GATEWAY_ERROR:    { label: 'Gateway Error',     cls: 'bg-zinc-100 text-zinc-600' },
  CRITICAL:         { label: 'Critical',          cls: 'bg-red-200 text-red-800 font-bold' },
}

export default function AdminReconciliationPage() {
  const [date, setDate] = useState(today)
  const [enabled, setEnabled] = useState(false)

  const { data: rows = [], isLoading, isFetching, error } = useQuery({
    queryKey: ['admin', 'reconciliation', date],
    queryFn: () => adminApi.reconcile(date),
    enabled,
    staleTime: 0,
  })

  const run = () => {
    if (!enabled) setEnabled(true)
    else {
      // already enabled — refetch by temporarily disabling / re-enabling handled by staleTime:0
      setEnabled(false)
      setTimeout(() => setEnabled(true), 0)
    }
  }

  const issues  = rows.filter(r => r.issue !== 'OK')
  const okCount = rows.length - issues.length

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Reconciliation</h1>

      <div className="flex gap-3 items-end">
        <div>
          <label className="block text-xs text-zinc-500 mb-1">Date</label>
          <input type="date" value={date} onChange={e => { setDate(e.target.value); setEnabled(false) }}
            max={today()}
            className="border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
        <button onClick={run} disabled={isLoading || isFetching}
          className="btn-primary text-sm disabled:opacity-50">
          {(isLoading || isFetching) ? 'Running…' : 'Run Reconciliation'}
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-4 text-sm">
          Failed to run reconciliation: {error.message}
        </div>
      )}

      {enabled && !isLoading && !isFetching && rows.length === 0 && !error && (
        <p className="text-center text-zinc-400 text-sm py-8">No payments found for this date.</p>
      )}

      {rows.length > 0 && (
        <>
          <div className="flex gap-4 text-sm">
            <div className="bg-white border border-zinc-200 rounded-xl px-4 py-3">
              <p className="text-zinc-500 text-xs">Total checked</p>
              <p className="text-2xl font-bold">{rows.length}</p>
            </div>
            <div className="bg-white border border-zinc-200 rounded-xl px-4 py-3">
              <p className="text-zinc-500 text-xs">OK</p>
              <p className="text-2xl font-bold text-emerald-600">{okCount}</p>
            </div>
            <div className="bg-white border border-zinc-200 rounded-xl px-4 py-3">
              <p className="text-zinc-500 text-xs">Issues</p>
              <p className={`text-2xl font-bold ${issues.length > 0 ? 'text-red-600' : 'text-zinc-700'}`}>
                {issues.length}
              </p>
            </div>
          </div>

          <div className="overflow-x-auto rounded-xl border border-zinc-200">
            <table className="w-full text-sm">
              <thead className="bg-zinc-50 border-b border-zinc-200">
                <tr>
                  <th className="text-left px-4 py-3 font-medium text-zinc-600">Payment No</th>
                  <th className="text-left px-4 py-3 font-medium text-zinc-600">Bill Code</th>
                  <th className="text-left px-4 py-3 font-medium text-zinc-600">Local Status</th>
                  <th className="text-center px-4 py-3 font-medium text-zinc-600">Gateway Paid</th>
                  <th className="text-right px-4 py-3 font-medium text-zinc-600">Amount (RM)</th>
                  <th className="text-left px-4 py-3 font-medium text-zinc-600">Issue</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-zinc-100">
                {rows.map((r, i) => {
                  const { label, cls } = ISSUE_LABELS[r.issue] ?? { label: r.issue, cls: 'bg-zinc-100 text-zinc-600' }
                  const rowBg = r.issue === 'OK' ? '' : r.issue === 'CRITICAL' ? 'bg-red-50' : 'bg-amber-50/50'
                  return (
                    <tr key={i} className={rowBg}>
                      <td className="px-4 py-3 font-mono text-xs">{r.paymentNo}</td>
                      <td className="px-4 py-3 font-mono text-xs text-zinc-500">{r.billCode}</td>
                      <td className="px-4 py-3 text-zinc-700">{r.localStatus}</td>
                      <td className="px-4 py-3 text-center">
                        {r.gatewayPaid
                          ? <span className="text-emerald-600 font-medium">Yes</span>
                          : <span className="text-zinc-400">No</span>}
                      </td>
                      <td className="px-4 py-3 text-right font-mono">
                        {r.localAmountRm != null ? Number(r.localAmountRm).toFixed(2) : '—'}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`inline-block text-xs px-2 py-0.5 rounded-full ${cls}`}>{label}</span>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  )
}
