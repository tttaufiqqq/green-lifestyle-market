import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { listingsApi } from '../../listings/listings.api'

const emptyForm = { name: '', parentId: '', sortOrder: 0 }

export default function AdminCategoriesPage() {
  const qc = useQueryClient()
  const [form, setForm] = useState(emptyForm)
  const [editing, setEditing] = useState(null)

  const { data: categories = [] } = useQuery({
    queryKey: ['adminCategories'],
    queryFn: listingsApi.adminGetCategories,
  })

  const save = useMutation({
    mutationFn: (body) => editing
      ? listingsApi.adminUpdateCategory(editing.id, body)
      : listingsApi.adminCreateCategory(body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['adminCategories'] })
      setForm(emptyForm)
      setEditing(null)
    },
  })

  const remove = useMutation({
    mutationFn: (id) => listingsApi.adminDeleteCategory(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['adminCategories'] }),
  })

  const startEdit = (cat) => {
    setEditing(cat)
    setForm({ name: cat.name, parentId: cat.parentId ?? '', sortOrder: cat.sortOrder })
  }

  const cancelEdit = () => { setEditing(null); setForm(emptyForm) }

  const handleSubmit = (e) => {
    e.preventDefault()
    save.mutate({
      name: form.name,
      parentId: form.parentId ? Number(form.parentId) : null,
      sortOrder: Number(form.sortOrder),
    })
  }

  const topLevel = categories.filter(c => !c.parentId)

  return (
    <main className="max-w-3xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-6">Admin — Categories</h1>
      <form onSubmit={handleSubmit} className="flex flex-wrap gap-2 mb-6">
        <input placeholder="Name" value={form.name}
          onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
          className="input flex-1 min-w-40" required />
        <select value={form.parentId}
          onChange={e => setForm(f => ({ ...f, parentId: e.target.value }))}
          className="input">
          <option value="">Top level</option>
          {topLevel.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <input type="number" placeholder="Order" value={form.sortOrder}
          onChange={e => setForm(f => ({ ...f, sortOrder: e.target.value }))}
          className="input w-20" />
        <button type="submit" disabled={save.isPending} className="btn-primary">
          {editing ? 'Update' : 'Add'}
        </button>
        {editing && (
          <button type="button" onClick={cancelEdit} className="btn-secondary">Cancel</button>
        )}
      </form>
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b text-left">
            <th className="py-2">Name</th><th>Parent</th><th>Order</th><th>Active</th><th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {categories.map(c => (
            <tr key={c.id} className="border-b hover:bg-zinc-50">
              <td className="py-2">{c.name}</td>
              <td>{topLevel.find(p => p.id === c.parentId)?.name ?? '—'}</td>
              <td>{c.sortOrder}</td>
              <td>{c.isActive ? '✓' : '✗'}</td>
              <td className="space-x-2">
                <button onClick={() => startEdit(c)} className="text-green-700 hover:underline">Edit</button>
                <button onClick={() => remove.mutate(c.id)} className="text-red-600 hover:underline">Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}
