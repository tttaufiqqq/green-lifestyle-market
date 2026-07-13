import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { articleApi } from './article.api'

export default function ArticlesPage() {
  const { data: articles = [], isLoading } = useQuery({
    queryKey: ['articles'],
    queryFn: articleApi.list,
  })

  if (isLoading) return <p className="p-6">Loading…</p>

  return (
    <main className="max-w-4xl mx-auto px-4 md:px-8 py-8 flex-1 w-full">
      <h1 className="text-2xl font-bold text-zinc-900 mb-6">Articles</h1>

      {articles.length === 0 && (
        <p className="text-zinc-400 text-center py-16">No articles yet</p>
      )}

      <div className="grid gap-6 sm:grid-cols-2">
        {articles.map((a) => (
          <Link
            key={a.id}
            to={`/articles/${a.slug}`}
            className="block border border-zinc-200 rounded-xl overflow-hidden hover:shadow-md transition-shadow bg-white"
          >
            {a.coverImage && (
              <img src={`/uploads/${a.coverImage}`} alt="" className="w-full h-40 object-cover" />
            )}
            <div className="p-4">
              <h2 className="font-semibold text-zinc-900">{a.title}</h2>
              <p className="text-sm text-zinc-500 mt-1 line-clamp-2">{a.excerpt}</p>
              <p className="text-xs text-zinc-400 mt-3">
                {a.authorName} &middot; {a.publishedAt ? new Date(a.publishedAt).toLocaleDateString() : ''}
              </p>
            </div>
          </Link>
        ))}
      </div>
    </main>
  )
}
