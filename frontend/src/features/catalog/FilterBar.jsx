import { useQuery } from '@tanstack/react-query'
import { listingsApi } from '../listings/listings.api'

const CONDITIONS = ['', 'NEW', 'LIKE_NEW', 'GOOD', 'FAIR']
const FULFILMENTS = [
  { value: '', label: 'Any' },
  { value: 'MEETUP', label: 'Meetup only' },
  { value: 'SHIPPING', label: 'Shipping only' },
]

export default function FilterBar({ filters, setFilter }) {
  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: listingsApi.getCategories,
    staleTime: 5 * 60 * 1000,
  })

  const flatCats = categories.flatMap(c => [
    { id: c.id, name: c.name },
    ...(c.children ?? []).map(ch => ({ id: ch.id, name: `\u00a0\u00a0${ch.name}` })),
  ])

  return (
    <div className="space-y-5 text-sm">
      <div>
        <p className="font-semibold mb-2">Category</p>
        <select value={filters.categoryId} onChange={e => setFilter('categoryId', e.target.value)}
          className="input w-full text-sm">
          <option value="">All categories</option>
          {flatCats.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>
      <div>
        <p className="font-semibold mb-2">Condition</p>
        <select value={filters.condition} onChange={e => setFilter('condition', e.target.value)}
          className="input w-full text-sm">
          {CONDITIONS.map(c => <option key={c} value={c}>{c ? c.replace('_', ' ') : 'Any'}</option>)}
        </select>
      </div>
      <div>
        <p className="font-semibold mb-2">Price (RM)</p>
        <div className="flex gap-2">
          <input type="number" min="0" placeholder="Min" value={filters.minPrice}
            onChange={e => setFilter('minPrice', e.target.value)} className="input w-full text-sm" />
          <input type="number" min="0" placeholder="Max" value={filters.maxPrice}
            onChange={e => setFilter('maxPrice', e.target.value)} className="input w-full text-sm" />
        </div>
      </div>
      <div>
        <p className="font-semibold mb-2">Fulfilment</p>
        {FULFILMENTS.map(f => (
          <label key={f.value} className="flex items-center gap-2 cursor-pointer mb-1">
            <input type="radio" name="fulfilment" value={f.value}
              checked={filters.fulfilment === f.value} onChange={() => setFilter('fulfilment', f.value)} />
            {f.label}
          </label>
        ))}
      </div>
      <button onClick={() => ['categoryId','condition','minPrice','maxPrice','fulfilment'].forEach(k => setFilter(k, ''))}
        className="text-xs text-zinc-500 hover:text-zinc-800 underline">Clear filters</button>
    </div>
  )
}
