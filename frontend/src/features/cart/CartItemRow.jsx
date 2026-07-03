import { Link } from 'react-router-dom'

export default function CartItemRow({ item, onUpdateQty, onRemove, updating }) {
  return (
    <div className={`flex gap-3 py-3 ${item.outOfStock ? 'opacity-60' : ''}`}>
      {/* Thumbnail */}
      <div className="w-16 h-16 rounded-lg bg-zinc-100 shrink-0 overflow-hidden">
        {item.imagePath
          ? <img src={`/uploads/${item.imagePath}`} alt={item.title} className="w-full h-full object-cover" />
          : <div className="w-full h-full flex items-center justify-center text-2xl text-zinc-300">🌿</div>
        }
      </div>

      {/* Details */}
      <div className="flex-1 min-w-0">
        <Link to={`/products/${item.slug}`}
          className="text-sm font-medium text-zinc-800 hover:text-emerald-700 line-clamp-2">
          {item.title}
        </Link>

        {/* Warnings */}
        {item.priceChanged && (
          <p className="text-xs text-amber-600 mt-0.5">
            Price changed from RM {Number(item.priceSnapshot).toFixed(2)} to RM {Number(item.price).toFixed(2)}
          </p>
        )}
        {item.outOfStock && (
          <p className="text-xs text-red-600 mt-0.5">Out of stock</p>
        )}
        {!item.outOfStock && item.available < item.quantity && (
          <p className="text-xs text-amber-600 mt-0.5">Only {item.available} left — qty adjusted</p>
        )}

        <div className="flex items-center gap-3 mt-2">
          <p className="text-sm font-semibold text-emerald-700">RM {Number(item.price).toFixed(2)}</p>

          {/* Qty stepper */}
          <div className="flex items-center gap-1 border border-zinc-200 rounded-lg">
            <button
              onClick={() => onUpdateQty(item.id, item.quantity - 1)}
              disabled={updating || item.quantity <= 1}
              className="px-2 py-1 text-zinc-500 hover:text-zinc-900 disabled:opacity-40 text-sm">
              -
            </button>
            <span className="px-2 text-sm font-medium w-8 text-center">{item.quantity}</span>
            <button
              onClick={() => onUpdateQty(item.id, item.quantity + 1)}
              disabled={updating || item.quantity >= item.available}
              className="px-2 py-1 text-zinc-500 hover:text-zinc-900 disabled:opacity-40 text-sm">
              +
            </button>
          </div>

          <button
            onClick={() => onRemove(item.id)}
            disabled={updating}
            className="text-xs text-red-500 hover:text-red-700 disabled:opacity-40">
            Remove
          </button>
        </div>
      </div>

      {/* Line total */}
      <div className="shrink-0 text-right">
        <p className="text-sm font-semibold">RM {(Number(item.price) * item.quantity).toFixed(2)}</p>
      </div>
    </div>
  )
}
