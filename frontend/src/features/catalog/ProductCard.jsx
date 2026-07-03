import { Link } from 'react-router-dom'

export default function ProductCard({ product: p }) {
  return (
    <Link to={`/products/${p.slug}`}
      className="block bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow overflow-hidden">
      <div className="aspect-square bg-zinc-100 overflow-hidden">
        {p.primaryImagePath
          ? <img src={`/uploads/${p.primaryImagePath}`} alt={p.title}
              className="w-full h-full object-cover" />
          : <div className="w-full h-full flex items-center justify-center text-zinc-300 text-4xl">🌿</div>
        }
      </div>
      <div className="p-3 space-y-1">
        <p className="text-xs text-zinc-400">{p.categoryName}</p>
        <h3 className="font-medium text-sm leading-tight line-clamp-2">{p.title}</h3>
        <p className="text-green-700 font-semibold">RM {Number(p.price).toFixed(2)}</p>
        <div className="flex gap-2 text-xs text-zinc-500">
          <span className="px-1.5 py-0.5 bg-zinc-100 rounded">{p.itemCondition.replace('_', ' ')}</span>
          {p.allowMeetup && <span>Meetup</span>}
          {p.allowShipping && <span>Shipping</span>}
        </div>
      </div>
    </Link>
  )
}
