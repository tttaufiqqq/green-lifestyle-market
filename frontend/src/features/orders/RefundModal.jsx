import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { orderApi } from './order.api'

export default function RefundModal({ orderNo, onClose }) {
  const [reason, setReason] = useState('')
  const qc = useQueryClient()

  const mut = useMutation({
    mutationFn: () => orderApi.refundRequest(orderNo, { reason }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['order', orderNo] })
      onClose()
    },
  })

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 space-y-4">
        <h2 className="text-lg font-semibold">Request Refund</h2>
        <p className="text-sm text-zinc-500">
          Describe the issue. Admin will review and contact you within 3 business days.
        </p>
        <textarea
          value={reason}
          onChange={e => setReason(e.target.value)}
          rows={4}
          maxLength={500}
          placeholder="Describe the reason for refund…"
          className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
        />
        {mut.error && (
          <p className="text-sm text-red-600">{mut.error.message ?? 'Failed to submit.'}</p>
        )}
        <div className="flex gap-3 justify-end">
          <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
          <button
            onClick={() => mut.mutate()}
            disabled={!reason.trim() || mut.isPending}
            className="btn-primary text-sm disabled:opacity-50">
            {mut.isPending ? 'Submitting…' : 'Submit Request'}
          </button>
        </div>
      </div>
    </div>
  )
}
