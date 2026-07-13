import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi } from './admin.api'

const TABS = ['REQUESTED', 'APPROVED', 'PROCESSED', 'REJECTED']

export default function AdminRefundsPage() {
  const [tab, setTab] = useState('REQUESTED')
  const [modal, setModal] = useState(null) // { refund, action: 'approve'|'reject'|'process' }
  const qc = useQueryClient()

  const { data: refunds = [], isLoading } = useQuery({
    queryKey: ['admin', 'refunds', tab],
    queryFn: () => adminApi.getRefunds(tab),
  })

  const invalidate = () => { qc.invalidateQueries({ queryKey: ['admin', 'refunds'] }); setModal(null) }
  const approveMut = useMutation({ mutationFn: (id) => adminApi.approveRefund(id), onSuccess: invalidate, meta: { successMessage: 'Refund approved' } })
  const rejectMut  = useMutation({ mutationFn: ({ id, note }) => adminApi.rejectRefund(id, note), onSuccess: invalidate, meta: { successMessage: 'Refund rejected' } })
  const processMut = useMutation({ mutationFn: ({ id, bankRef, note }) => adminApi.processRefund(id, bankRef, note), onSuccess: invalidate, meta: { successMessage: 'Refund marked as processed' } })

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Refunds</h1>

      <div className="flex gap-1">
        {TABS.map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors
              ${tab === t ? 'bg-emerald-600 text-white' : 'bg-zinc-100 text-zinc-600 hover:bg-zinc-200'}`}>
            {t}
          </button>
        ))}
      </div>

      {isLoading && <p className="text-zinc-500">Loading…</p>}

      <div className="space-y-3">
        {refunds.map(r => (
          <div key={r.id} className="bg-white border border-zinc-200 rounded-xl p-4 flex items-center gap-4">
            <div className="flex-1 min-w-0">
              <p className="font-mono text-sm font-medium">{r.orderNo}</p>
              <p className="text-sm text-zinc-500">{r.buyerName} → {r.sellerName}</p>
              <p className="text-sm font-semibold text-zinc-700 mt-0.5">RM {Number(r.amount).toFixed(2)}</p>
              {r.reason && <p className="text-xs text-zinc-400 mt-0.5 italic">"{r.reason}"</p>}
            </div>
            <div className="flex gap-2 shrink-0">
              {r.status === 'REQUESTED' && (
                <>
                  <button onClick={() => approveMut.mutate(r.id)}
                    className="btn-primary text-xs px-3 py-1.5 disabled:opacity-50"
                    disabled={approveMut.isPending}>Approve</button>
                  <button onClick={() => setModal({ refund: r, action: 'reject' })}
                    className="text-xs border border-red-300 text-red-600 hover:bg-red-50 px-3 py-1.5 rounded-lg">
                    Reject
                  </button>
                </>
              )}
              {r.status === 'APPROVED' && (
                <button onClick={() => setModal({ refund: r, action: 'process' })}
                  className="btn-primary text-xs px-3 py-1.5">
                  Record Transfer
                </button>
              )}
              {r.bankRef && <p className="text-xs text-zinc-400">Ref: {r.bankRef}</p>}
            </div>
          </div>
        ))}
        {!isLoading && refunds.length === 0 && (
          <p className="text-center text-zinc-400 text-sm py-8">No refunds in this category.</p>
        )}
      </div>

      {modal?.action === 'reject' && (
        <RejectModal refund={modal.refund}
          onConfirm={(note) => rejectMut.mutate({ id: modal.refund.id, note })}
          onClose={() => setModal(null)} isPending={rejectMut.isPending} />
      )}
      {modal?.action === 'process' && (
        <ProcessModal refund={modal.refund}
          onConfirm={(bankRef, note) => processMut.mutate({ id: modal.refund.id, bankRef, note })}
          onClose={() => setModal(null)} isPending={processMut.isPending} />
      )}
    </div>
  )
}

function RejectModal({ refund, onConfirm, onClose, isPending }) {
  const [note, setNote] = useState('')
  return (
    <ModalShell title="Reject Refund" onClose={onClose}>
      <p className="text-sm text-zinc-500 mb-3">Order: <strong>{refund.orderNo}</strong></p>
      <textarea value={note} onChange={e => setNote(e.target.value)} rows={3}
        placeholder="Admin note (required)…"
        className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-400 resize-none" />
      <div className="flex gap-3 justify-end mt-4">
        <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
        <button onClick={() => onConfirm(note)} disabled={!note.trim() || isPending}
          className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-4 py-2 rounded-lg disabled:opacity-50">
          {isPending ? 'Rejecting…' : 'Confirm Reject'}
        </button>
      </div>
    </ModalShell>
  )
}

function ProcessModal({ refund, onConfirm, onClose, isPending }) {
  const [bankRef, setBankRef] = useState('')
  const [note, setNote]       = useState('')
  return (
    <ModalShell title="Record Refund Transfer" onClose={onClose}>
      <p className="text-sm text-zinc-500 mb-3">
        Order: <strong>{refund.orderNo}</strong> — RM {Number(refund.amount).toFixed(2)}
      </p>
      <input value={bankRef} onChange={e => setBankRef(e.target.value)}
        placeholder="Bank transfer reference *"
        className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 mb-2" />
      <input value={note} onChange={e => setNote(e.target.value)}
        placeholder="Admin note (optional)"
        className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
      <div className="flex gap-3 justify-end mt-4">
        <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
        <button onClick={() => onConfirm(bankRef, note)} disabled={!bankRef.trim() || isPending}
          className="btn-primary text-sm disabled:opacity-50">
          {isPending ? 'Processing…' : 'Mark Processed'}
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
