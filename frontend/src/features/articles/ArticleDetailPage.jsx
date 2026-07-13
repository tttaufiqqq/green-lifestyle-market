import { useQuery } from '@tanstack/react-query'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { articleApi } from './article.api'

export default function ArticleDetailPage() {
  const { slug } = useParams()
  const { data: article, isLoading, error } = useQuery({
    queryKey: ['article', slug],
    queryFn: () => articleApi.get(slug),
  })

  if (isLoading) return <p className="p-6">Loading…</p>
  if (error) return <p className="p-6 text-red-600">Article not found.</p>

  return (
    <main className="max-w-3xl mx-auto px-4 md:px-8 py-8 flex-1 w-full">
      <Link to="/articles" className="text-sm text-emerald-600 hover:underline">&larr; All articles</Link>

      <h1 className="text-3xl font-bold text-zinc-900 mt-4">{article.title}</h1>
      <p className="text-sm text-zinc-400 mt-2">
        {article.authorName} &middot; {article.publishedAt ? new Date(article.publishedAt).toLocaleDateString() : ''}
      </p>

      {article.coverImage && (
        <img src={`/uploads/${article.coverImage}`} alt="" className="w-full rounded-xl mt-6" />
      )}

      <article className="prose prose-zinc max-w-none mt-8">
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{article.bodyMd}</ReactMarkdown>
      </article>
    </main>
  )
}
