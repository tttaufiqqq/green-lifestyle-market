import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi } from './admin.api'

export default function AdminUsersPage() {
  const qc = useQueryClient()
  const { data: users = [], isLoading } = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: adminApi.getUsers,
  })

  const statusMut = useMutation({
    mutationFn: ({ id, status }) => adminApi.updateUserStatus(id, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'users'] }),
    meta: { successMessage: (data, vars) => vars.status === 'ACTIVE' ? 'User reactivated' : 'User suspended' },
  })
  const bankMut = useMutation({
    mutationFn: ({ id, verified }) => adminApi.verifyBank(id, verified),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'users'] }),
    meta: { successMessage: 'Bank verified' },
  })

  if (isLoading) return <p className="text-zinc-500">Loading…</p>

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Users</h1>
      <div className="bg-white border border-zinc-200 rounded-xl overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-zinc-100 text-left text-zinc-400 text-xs uppercase tracking-wide">
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Email Verified</th>
              <th className="px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-50">
            {users.map(u => (
              <tr key={u.id} className="hover:bg-zinc-50">
                <td className="px-4 py-3 font-medium">{u.name}</td>
                <td className="px-4 py-3 text-zinc-500">{u.email}</td>
                <td className="px-4 py-3">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium
                    ${u.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-zinc-100 text-zinc-600'}`}>
                    {u.role}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium
                    ${u.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-600'}`}>
                    {u.status}
                  </span>
                </td>
                <td className="px-4 py-3 text-center">{u.emailVerified ? '✓' : '—'}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    {u.status === 'ACTIVE' ? (
                      <button onClick={() => statusMut.mutate({ id: u.id, status: 'SUSPENDED' })}
                        className="text-xs text-red-600 hover:underline">Suspend</button>
                    ) : (
                      <button onClick={() => statusMut.mutate({ id: u.id, status: 'ACTIVE' })}
                        className="text-xs text-emerald-600 hover:underline">Reactivate</button>
                    )}
                    <button onClick={() => bankMut.mutate({ id: u.id, verified: true })}
                      className="text-xs text-blue-600 hover:underline">Verify Bank</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
