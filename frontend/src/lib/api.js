const BASE = import.meta.env.VITE_API_BASE ?? '/api/v1'

function getCsrfToken() {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : null
}

async function request(method, path, body) {
  const headers = { 'Content-Type': 'application/json' }
  const csrf = getCsrfToken()
  if (csrf) headers['X-XSRF-TOKEN'] = csrf

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    credentials: 'same-origin',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (res.status === 204) return null

  const data = await res.json()

  if (!res.ok) {
    const err = data?.error ?? {}
    const e = new Error(err.message ?? 'Request failed')
    e.code = err.code
    e.details = err.details ?? []
    e.errorId = err.errorId ?? null
    e.status = res.status
    throw e
  }

  return data
}

export const api = {
  get:    (path)        => request('GET',    path),
  post:   (path, body)  => request('POST',   path, body),
  put:    (path, body)  => request('PUT',    path, body),
  patch:  (path, body)  => request('PATCH',  path, body),
  delete: (path)        => request('DELETE', path),
}
