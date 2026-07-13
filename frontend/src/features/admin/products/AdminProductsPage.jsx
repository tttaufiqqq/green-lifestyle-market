import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { listingsApi } from '../../listings/listings.api'

export default function AdminProductsPage() {
  const [page, setPage] = useState(0)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['adminProducts', page],
    queryFn: () => listingsApi.adminGetProducts(page),
  })

  const patchStatus = useMutation({
    mutationFn: ({ id, status }) => listingsApi.adminPatchProductStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['adminProducts'] }),
    meta: { successMessage: (data, vars) => vars.status === 'SUSPENDED' ? 'Product suspended' : 'Product restored' },
  })

  if (isLoading) return <p className="p-6">Loading…</p>

  const { content = [], totalPages = 1 } = data ?? {}

  return (
    <main className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">Admin — Product Moderation</h1>
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b text-left">
            <th className="py-2">Title</th>
            <th>Seller</th>
            <th>Status</th>
            <th>Price</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {content.map(p => (
            <tr key={p.id} className="border-b hover:bg-zinc-50">
              <td className="py-2 max-w-xs truncate">{p.title}</td>
              <td>{p.sellerName}</td>
              <td>
                <span className="px-2 py-0.5 bg-zinc-100 rounded text-xs">{p.status}</span>
              </td>
              <td>RM {Number(p.price).toFixed(2)}</td>
              <td className="space-x-2 whitespace-nowrap">
                {p.status !== 'SUSPENDED' && (
                  <button onClick={() => patchStatus.mutate({ id: p.id, status: 'SUSPENDED' })}
                    className="text-red-600 hover:underline">Suspend</button>
                )}
                {p.status === 'SUSPENDED' && (
                  <button onClick={() => patchStatus.mutate({ id: p.id, status: 'ACTIVE' })}
                    className="text-green-700 hover:underline">Restore</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="flex gap-2 mt-4 justify-center items-center">
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
          className="btn-secondary disabled:opacity-40">&#8249; Prev</button>
        <span className="text-sm text-zinc-600">{page + 1} / {totalPages}</span>
        <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}
          className="btn-secondary disabled:opacity-40">Next &#8250;</button>
      </div>
    </main>
  )
}
