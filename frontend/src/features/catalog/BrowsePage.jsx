import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { catalogApi } from './catalog.api'
import ProductCard from './ProductCard'
import FilterBar from './FilterBar'

const SORTS = [
  { value: 'newest',     label: 'Newest' },
  { value: 'price_asc',  label: 'Price: low to high' },
  { value: 'price_desc', label: 'Price: high to low' },
]

export default function BrowsePage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [draft, setDraft] = useState(searchParams.get('q') || '')

  const filters = {
    q:          searchParams.get('q')          || '',
    categoryId: searchParams.get('categoryId') || '',
    condition:  searchParams.get('condition')  || '',
    minPrice:   searchParams.get('minPrice')   || '',
    maxPrice:   searchParams.get('maxPrice')   || '',
    fulfilment: searchParams.get('fulfilment') || '',
    sort:       searchParams.get('sort')       || 'newest',
    page:       Number(searchParams.get('page') || 0),
    size:       12,
  }

  const { data, isLoading } = useQuery({
    queryKey: ['products', filters],
    queryFn: () => catalogApi.getProducts(filters),
  })

  const setFilter = (key, value) => {
    setSearchParams(prev => {
      const next = new URLSearchParams(prev)
      if (value !== '' && value != null) next.set(key, value)
      else next.delete(key)
      if (key !== 'page') next.delete('page')
      return next
    })
  }

  const handleSearch = (e) => {
    e.preventDefault()
    setFilter('q', draft)
  }

  const { content = [], totalPages = 0, totalElements = 0 } = data ?? {}

  return (
    <main className="max-w-6xl mx-auto px-4 py-6">
      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input value={draft} onChange={e => setDraft(e.target.value)}
          placeholder="Search products…"
          className="input flex-1 text-base" />
        <button type="submit" className="btn-primary">Search</button>
      </form>

      <div className="flex gap-6">
        <aside className="w-52 shrink-0">
          <FilterBar filters={filters} setFilter={setFilter} />
        </aside>

        <div className="flex-1 min-w-0">
          <div className="flex justify-between items-center mb-3">
            <p className="text-sm text-zinc-500">{totalElements} result{totalElements !== 1 ? 's' : ''}</p>
            <select value={filters.sort} onChange={e => setFilter('sort', e.target.value)} className="input text-sm">
              {SORTS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
            </select>
          </div>

          {isLoading && <p className="text-zinc-500 text-center py-12">Loading…</p>}
          {!isLoading && content.length === 0 && (
            <p className="text-zinc-500 text-center py-12">No products found.</p>
          )}
          {!isLoading && content.length > 0 && (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
              {content.map(p => <ProductCard key={p.id} product={p} />)}
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-6">
              <button disabled={filters.page === 0}
                onClick={() => setFilter('page', filters.page - 1)}
                className="btn-secondary disabled:opacity-40">&#8249; Prev</button>
              <span className="self-center text-sm text-zinc-600">
                {filters.page + 1} / {totalPages}
              </span>
              <button disabled={filters.page >= totalPages - 1}
                onClick={() => setFilter('page', filters.page + 1)}
                className="btn-secondary disabled:opacity-40">Next &#8250;</button>
            </div>
          )}
        </div>
      </div>
    </main>
  )
}
