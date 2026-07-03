const STEPS = [
  { key: 'createdAt',   label: 'Order placed',    statuses: ['PENDING_PAYMENT','PAID','CONFIRMED','SHIPPED','READY_FOR_MEETUP','COMPLETED','CANCELLED','EXPIRED','REFUND_REQUESTED','REFUNDED'] },
  { key: 'confirmedAt', label: 'Confirmed',        statuses: ['CONFIRMED','SHIPPED','READY_FOR_MEETUP','COMPLETED','REFUND_REQUESTED','REFUNDED'] },
  { key: 'shippedAt',   label: 'Shipped / Ready',  statuses: ['SHIPPED','READY_FOR_MEETUP','COMPLETED','REFUND_REQUESTED','REFUNDED'] },
  { key: 'completedAt', label: 'Completed',        statuses: ['COMPLETED'] },
]

function fmt(iso) {
  if (!iso) return null
  return new Date(iso).toLocaleString('en-MY', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit', hour12: true,
    timeZone: 'Asia/Kuala_Lumpur',
  })
}

export default function OrderTimeline({ order }) {
  const isCancelled = ['CANCELLED', 'EXPIRED', 'REFUND_REQUESTED', 'REFUNDED'].includes(order.status)

  return (
    <div className="space-y-2">
      <h3 className="text-sm font-semibold text-zinc-700 mb-3">Timeline</h3>
      <ol className="relative border-l border-zinc-200 pl-6 space-y-4">
        {STEPS.map(step => {
          const active = order[step.key] || step.statuses.includes(order.status)
          const ts = order[step.key]
          if (!active && !isCancelled) return null
          if (!active) return null
          return (
            <li key={step.key} className="relative">
              <span className={`absolute -left-[1.4rem] top-0.5 w-3 h-3 rounded-full border-2
                ${ts ? 'bg-emerald-500 border-emerald-500' : 'bg-white border-zinc-300'}`} />
              <p className={`text-sm font-medium ${ts ? 'text-zinc-800' : 'text-zinc-400'}`}>{step.label}</p>
              {ts && <p className="text-xs text-zinc-400 mt-0.5">{fmt(ts)}</p>}
            </li>
          )
        })}
        {isCancelled && (
          <li className="relative">
            <span className="absolute -left-[1.4rem] top-0.5 w-3 h-3 rounded-full bg-red-400 border-2 border-red-400" />
            <p className="text-sm font-medium text-red-600">
              {order.status === 'EXPIRED' ? 'Expired' :
               order.status === 'REFUND_REQUESTED' ? 'Refund requested' :
               order.status === 'REFUNDED' ? 'Refunded' : 'Cancelled'}
            </p>
            {order.cancelledAt && <p className="text-xs text-zinc-400 mt-0.5">{fmt(order.cancelledAt)}</p>}
            {order.cancelledReason && (
              <p className="text-xs text-zinc-500 italic mt-0.5">Reason: {order.cancelledReason}</p>
            )}
          </li>
        )}
      </ol>
      {order.autoCompleteAt && (
        <p className="text-xs text-zinc-400 mt-3">
          Auto-completes on {fmt(order.autoCompleteAt)} if no action taken.
        </p>
      )}
    </div>
  )
}
