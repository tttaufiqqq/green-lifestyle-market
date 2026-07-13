import { useState } from 'react'
import { articleApi } from '../../articles/article.api'

export function useArticleForm(initial = null) {
  const [form, setForm] = useState({
    title: '', excerpt: '', bodyMd: '', coverImage: '', status: 'DRAFT',
    ...(initial ?? {}),
  })
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const handle = (e) => {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
  }

  const submit = async (onSuccess) => {
    setError(null)
    setLoading(true)
    try {
      const body = {
        title: form.title,
        excerpt: form.excerpt,
        bodyMd: form.bodyMd,
        coverImage: form.coverImage || null,
        status: form.status,
      }
      const res = initial?.id
        ? await articleApi.adminUpdate(initial.id, body)
        : await articleApi.adminCreate(body)
      onSuccess(res)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return { form, handle, submit, error, loading }
}
