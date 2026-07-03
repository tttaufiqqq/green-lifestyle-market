import { useState } from 'react'

// ── Reject Modal ──────────────────────────────────────────────────────────────
export function RejectModal({ onConfirm, onClose, isPending }) {
  const [reason, setReason] = useState('')
  return (
    <Modal title="Reject Order" onClose={onClose}>
      <p className="text-sm text-zinc-500 mb-3">
        The order will be cancelled and a refund will be created for the buyer.
      </p>
      <textarea
        value={reason}
        onChange={e => setReason(e.target.value)}
        rows={3}
        placeholder="Reason for rejection (optional)…"
        className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-400 resize-none"
      />
      <div className="flex gap-3 justify-end mt-4">
        <button onClick={onClose} className="btn-outline text-sm">Back</button>
        <button onClick={() => onConfirm(reason)} disabled={isPending}
          className="bg-red-600 hover:bg-red-700 text-white text-sm font-medium px-4 py-2 rounded-lg disabled:opacity-50">
          {isPending ? 'Rejecting…' : 'Confirm Reject'}
        </button>
      </div>
    </Modal>
  )
}

// ── Ship Modal ────────────────────────────────────────────────────────────────
const COURIERS = ['J&T Express', 'Pos Laju', 'DHL Express', 'Shopee Express', 'Ninja Van', 'Other']

export function ShipModal({ onConfirm, onClose, isPending }) {
  const [courier, setCourier]     = useState('')
  const [trackingNo, setTracking] = useState('')
  const valid = courier && trackingNo.trim()
  return (
    <Modal title="Mark as Shipped" onClose={onClose}>
      <div className="space-y-3">
        <div>
          <label className="block text-xs font-medium text-zinc-600 mb-1">Courier</label>
          <select value={courier} onChange={e => setCourier(e.target.value)}
            className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500">
            <option value="">Select courier…</option>
            {COURIERS.map(c => <option key={c}>{c}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-zinc-600 mb-1">Tracking Number</label>
          <input value={trackingNo} onChange={e => setTracking(e.target.value)}
            placeholder="e.g. MY1234567890"
            className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500" />
        </div>
      </div>
      <div className="flex gap-3 justify-end mt-4">
        <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
        <button onClick={() => onConfirm({ courier, trackingNo })} disabled={!valid || isPending}
          className="btn-primary text-sm disabled:opacity-50">
          {isPending ? 'Updating…' : 'Confirm Shipped'}
        </button>
      </div>
    </Modal>
  )
}

// ── Meetup Modal ──────────────────────────────────────────────────────────────
export function MeetupModal({ onConfirm, onClose, isPending }) {
  const [note, setNote] = useState('')
  return (
    <Modal title="Set Meetup Details" onClose={onClose}>
      <textarea
        value={note}
        onChange={e => setNote(e.target.value)}
        rows={3}
        placeholder="e.g. Wed 3pm, FTMK lobby, block B entrance"
        className="w-full border border-zinc-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 resize-none"
      />
      <div className="flex gap-3 justify-end mt-4">
        <button onClick={onClose} className="btn-outline text-sm">Cancel</button>
        <button onClick={() => onConfirm({ meetupNote: note })} disabled={!note.trim() || isPending}
          className="btn-primary text-sm disabled:opacity-50">
          {isPending ? 'Updating…' : 'Confirm Ready'}
        </button>
      </div>
    </Modal>
  )
}

// ── Shared shell ──────────────────────────────────────────────────────────────
function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700 text-xl leading-none">&times;</button>
        </div>
        {children}
      </div>
    </div>
  )
}
