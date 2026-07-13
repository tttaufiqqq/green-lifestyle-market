import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useArticleForm } from './useArticleForm'
import { articleApi } from '../../articles/article.api'

export default function ArticleFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [initial, setInitial] = useState(id ? undefined : null)

  useEffect(() => {
    if (!id) return
    articleApi.adminGet(id).then(setInitial).catch(() => setInitial(null))
  }, [id])

  if (initial === undefined) return <p>Loading…</p>
  if (id && initial === null) return <p className="text-red-600">Article not found.</p>

  return <ArticleForm initial={initial} id={id} navigate={navigate} />
}

function ArticleForm({ initial, id, navigate }) {
  const { form, handle, submit, error, loading } = useArticleForm(initial)

  const handleSubmit = (e) => {
    e.preventDefault()
    submit((res) => navigate(`/admin/articles/${res.id}/edit`))
  }

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold">{id ? 'Edit Article' : 'New Article'}</h1>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <form onSubmit={handleSubmit} className="space-y-4">
        <input name="title" placeholder="Title" value={form.title} onChange={handle}
          className="input w-full" required maxLength={150} />
        <textarea name="excerpt" placeholder="Excerpt" value={form.excerpt} onChange={handle}
          className="input w-full h-20" required maxLength={300} />
        <textarea name="bodyMd" placeholder="Body (Markdown)" value={form.bodyMd} onChange={handle}
          className="input w-full h-72 font-mono text-sm" required />
        <input name="coverImage" placeholder="Cover image path (optional)" value={form.coverImage}
          onChange={handle} className="input w-full" />
        <select name="status" value={form.status} onChange={handle} className="input w-full">
          <option value="DRAFT">Draft</option>
          <option value="PUBLISHED">Published</option>
        </select>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Saving…' : id ? 'Update Article' : 'Create Article'}
        </button>
      </form>
    </div>
  )
}
