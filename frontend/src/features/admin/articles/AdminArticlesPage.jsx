import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { articleApi } from '../../articles/article.api'

export default function AdminArticlesPage() {
  const qc = useQueryClient()
  const { data: articles = [], isLoading } = useQuery({
    queryKey: ['adminArticles'],
    queryFn: articleApi.adminList,
  })

  const remove = useMutation({
    mutationFn: (id) => articleApi.adminDelete(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['adminArticles'] }),
    meta: { successMessage: 'Article deleted' },
  })

  if (isLoading) return <p>Loading…</p>

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold">Admin — Articles</h1>
        <Link to="/admin/articles/new" className="btn-primary">New Article</Link>
      </div>
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b text-left">
            <th className="py-2">Title</th>
            <th>Author</th>
            <th>Status</th>
            <th>Updated</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {articles.map((a) => (
            <tr key={a.id} className="border-b hover:bg-zinc-50">
              <td className="py-2 max-w-xs truncate">{a.title}</td>
              <td>{a.authorName}</td>
              <td>
                <span className="px-2 py-0.5 bg-zinc-100 rounded text-xs">{a.status}</span>
              </td>
              <td>{new Date(a.updatedAt).toLocaleDateString()}</td>
              <td className="space-x-2 whitespace-nowrap">
                <Link to={`/admin/articles/${a.id}/edit`} className="text-green-700 hover:underline">Edit</Link>
                <button onClick={() => remove.mutate(a.id)} className="text-red-600 hover:underline">Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
