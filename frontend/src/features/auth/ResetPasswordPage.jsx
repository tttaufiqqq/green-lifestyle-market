import { useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { authApi } from './auth.api'

export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await authApi.resetPassword({ token: params.get('token'), newPassword: password })
      navigate('/login')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex flex-1 items-center justify-center p-4">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4 bg-white p-8 rounded-xl shadow">
        <h1 className="text-2xl font-bold">Set new password</h1>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <input type="password" placeholder="New password (min 8)" value={password}
          onChange={e => setPassword(e.target.value)} className="input w-full" required minLength={8} />
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Saving…' : 'Set password'}
        </button>
      </form>
    </main>
  )
}
