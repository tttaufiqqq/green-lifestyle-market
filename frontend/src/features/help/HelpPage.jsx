import { useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import Fuse from 'fuse.js'
import { helpSections } from './help-content'

export default function HelpPage() {
  const { slug } = useParams()
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [feedback, setFeedback] = useState(null)

  const active = helpSections.find((s) => s.slug === slug) ?? helpSections[0]

  const fuse = useMemo(
    () => new Fuse(helpSections, { keys: ['label', 'body'], threshold: 0.35 }),
    []
  )
  const results = query.trim() ? fuse.search(query).map((r) => r.item) : []

  function handleFeedback(value) {
    setFeedback(value)
    console.log('[help-feedback]', active.slug, value)
  }

  return (
    <main className="max-w-5xl mx-auto px-4 md:px-8 py-8 flex-1 w-full flex gap-8">
      <aside className="w-56 shrink-0 hidden md:block">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search help…"
          className="input w-full mb-4"
        />
        {results.length > 0 ? (
          <nav className="space-y-0.5">
            {results.map((s) => (
              <button
                key={s.slug}
                onClick={() => { navigate(`/help/${s.slug}`); setQuery('') }}
                className="block w-full text-left px-3 py-2 rounded-lg text-sm text-zinc-600 hover:bg-zinc-100"
              >
                {s.label}
              </button>
            ))}
          </nav>
        ) : (
          <nav className="space-y-0.5">
            {helpSections.map((s) => (
              <button
                key={s.slug}
                onClick={() => navigate(`/help/${s.slug}`)}
                className={`block w-full text-left px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                  s.slug === active.slug
                    ? 'bg-emerald-100 text-emerald-700'
                    : 'text-zinc-600 hover:bg-zinc-100 hover:text-zinc-900'
                }`}
              >
                {s.label}
              </button>
            ))}
          </nav>
        )}
      </aside>

      <article className="prose prose-zinc max-w-none flex-1 min-w-0">
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{active.body}</ReactMarkdown>

        <div className="not-prose mt-10 pt-6 border-t border-zinc-200 flex items-center gap-3">
          <span className="text-sm text-zinc-500">Was this helpful?</span>
          <button
            onClick={() => handleFeedback('yes')}
            className={`text-sm px-3 py-1 rounded-lg border ${feedback === 'yes' ? 'bg-emerald-100 border-emerald-300' : 'border-zinc-300 hover:bg-zinc-50'}`}
          >
            Yes
          </button>
          <button
            onClick={() => handleFeedback('no')}
            className={`text-sm px-3 py-1 rounded-lg border ${feedback === 'no' ? 'bg-emerald-100 border-emerald-300' : 'border-zinc-300 hover:bg-zinc-50'}`}
          >
            No
          </button>
          {feedback && <span className="text-sm text-zinc-400">Thanks for the feedback!</span>}
        </div>
      </article>
    </main>
  )
}
