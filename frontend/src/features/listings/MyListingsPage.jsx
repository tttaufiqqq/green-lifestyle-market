import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { listingsApi } from './listings.api'

const ALL_STATUSES = ['', 'DRAFT', 'ACTIVE', 'SOLD_OUT', 'SUSPENDED', 'DELETED']

export default function MyListingsPage() {
  const [statusFilter, setStatusFilter] = useState('')
  const qc = useQueryClient()

  const { data: listings = [], isLoading } = useQuery({
    queryKey: ['myListings', statusFilter],
    queryFn: () => listingsApi.getMyListings(statusFilter || null),
  })

  const patchStatus = useMutation({
    mutationFn: ({ id, status }) => listingsApi.patchStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['myListings'] }),
    meta: {
      successMessage: (data, vars) => ({
        ACTIVE: 'Listing activated', DRAFT: 'Listing moved to draft', DELETED: 'Listing deleted',
      }[vars.status] ?? 'Listing updated'),
    },
  })

  if (isLoading) return <p className="p-6">Loading…</p>

  return (
    <main className="max-w-4xl mx-auto p-6">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">My Listings</h1>
        <Link to="/listings/new" className="btn-primary">+ New Listing</Link>
      </div>
      <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)}
        className="input mb-4">
        {ALL_STATUSES.map(s => (
          <option key={s} value={s}>{s || 'All statuses'}</option>
        ))}
      </select>
      {listings.length === 0 && <p className="text-zinc-500">No listings yet.</p>}
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b text-left">
            <th className="py-2">Title</th>
            <th>Price</th>
            <th>Qty</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {listings.map(l => (
            <tr key={l.id} className="border-b hover:bg-zinc-50">
              <td className="py-2">{l.title}</td>
              <td>RM {Number(l.price).toFixed(2)}</td>
              <td>{l.quantity}</td>
              <td><span className="px-2 py-0.5 bg-zinc-100 rounded text-xs">{l.status}</span></td>
              <td className="space-x-2 whitespace-nowrap">
                <Link to={`/listings/${l.id}/edit`} className="text-green-700 hover:underline">Edit</Link>
                {l.status === 'DRAFT' && (
                  <button onClick={() => patchStatus.mutate({ id: l.id, status: 'ACTIVE' })}
                    className="text-blue-600 hover:underline">Activate</button>
                )}
                {l.status === 'ACTIVE' && (
                  <button onClick={() => patchStatus.mutate({ id: l.id, status: 'DRAFT' })}
                    className="text-zinc-600 hover:underline">Draft</button>
                )}
                {l.status !== 'DELETED' && (
                  <button onClick={() => patchStatus.mutate({ id: l.id, status: 'DELETED' })}
                    className="text-red-600 hover:underline">Delete</button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}
