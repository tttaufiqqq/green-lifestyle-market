import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { catalogApi } from './catalog.api'

export default function ProductDetailPage() {
  const { slug } = useParams()
  const { data: p, isLoading, error } = useQuery({
    queryKey: ['product', slug],
    queryFn: () => catalogApi.getProduct(slug),
  })

  const [imgIdx, setImgIdx] = useState(0)

  if (isLoading) return <p className="p-6">Loading…</p>
  if (error) return <p className="p-6 text-red-600">Product not found.</p>

  const primaryImg = p.images?.[imgIdx]?.path ?? p.images?.find(i => i.isPrimary)?.path
  const outOfStock = p.availability <= 0

  return (
    <main className="max-w-5xl mx-auto px-4 py-8">
      <Link to="/" className="text-sm text-green-700 hover:underline mb-4 inline-block">← Back to browse</Link>
      <div className="grid md:grid-cols-2 gap-8">
        {/* Images */}
        <div className="space-y-3">
          <div className="aspect-square bg-zinc-100 rounded-xl overflow-hidden">
            {primaryImg
              ? <img src={`/uploads/${primaryImg}`} alt={p.title} className="w-full h-full object-cover" />
              : <div className="w-full h-full flex items-center justify-center text-6xl text-zinc-300">🌿</div>
            }
          </div>
          {p.images?.length > 1 && (
            <div className="flex gap-2 overflow-x-auto">
              {p.images.map((img, i) => (
                <button key={img.id} onClick={() => setImgIdx(i)}
                  className={`w-16 h-16 rounded-lg overflow-hidden border-2 shrink-0 ${i === imgIdx ? 'border-green-600' : 'border-transparent'}`}>
                  <img src={`/uploads/${img.path}`} alt="" className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Info */}
        <div className="space-y-4">
          <div>
            <p className="text-sm text-zinc-400">{p.categoryName}</p>
            <h1 className="text-2xl font-bold mt-1">{p.title}</h1>
            <p className="text-3xl font-semibold text-green-700 mt-2">RM {Number(p.price).toFixed(2)}</p>
          </div>

          <div className="flex gap-2 flex-wrap text-sm">
            <span className="px-2 py-1 bg-zinc-100 rounded">{p.itemCondition.replace('_', ' ')}</span>
            {outOfStock
              ? <span className="px-2 py-1 bg-red-100 text-red-700 rounded">Out of stock</span>
              : <span className="px-2 py-1 bg-green-100 text-green-700 rounded">{p.availability} available</span>
            }
          </div>

          {p.sustainabilityNote && (
            <p className="text-sm text-green-800 bg-green-50 p-3 rounded-lg">🌱 {p.sustainabilityNote}</p>
          )}

          <div className="text-sm whitespace-pre-wrap text-zinc-700">{p.description}</div>

          <div className="space-y-2 text-sm border-t pt-4">
            <p className="font-semibold">Fulfilment</p>
            {p.allowMeetup && (
              <p>📍 Meetup — {p.meetupLocation}</p>
            )}
            {p.allowShipping && (
              <p>📦 Shipping — RM {p.shippingFee != null ? Number(p.shippingFee).toFixed(2) : '0.00'}</p>
            )}
          </div>

          <div className="text-sm border-t pt-4 text-zinc-500">
            <p className="font-semibold text-zinc-700 mb-1">Seller</p>
            <p>{p.seller.name}</p>
            <p>{p.seller.activeListingCount} active listing{p.seller.activeListingCount !== 1 ? 's' : ''}</p>
            <p>Member since {new Date(p.seller.joinedAt).toLocaleDateString('en-MY', { year: 'numeric', month: 'long' })}</p>
          </div>
        </div>
      </div>
    </main>
  )
}
