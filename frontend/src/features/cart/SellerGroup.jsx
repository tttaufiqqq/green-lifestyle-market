import CartItemRow from './CartItemRow'

export default function SellerGroup({ group, onUpdateQty, onRemove, updating }) {
  return (
    <div className="bg-white rounded-xl border border-zinc-200 overflow-hidden">
      <div className="px-4 py-2 bg-zinc-50 border-b border-zinc-100">
        <p className="text-xs font-semibold text-zinc-500 uppercase tracking-wide">
          Sold by {group.sellerName}
        </p>
      </div>
      <div className="divide-y divide-zinc-100 px-4">
        {group.items.map(item => (
          <CartItemRow
            key={item.id}
            item={item}
            onUpdateQty={onUpdateQty}
            onRemove={onRemove}
            updating={updating}
          />
        ))}
      </div>
    </div>
  )
}
