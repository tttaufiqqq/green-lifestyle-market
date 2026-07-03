import { useState } from 'react'
import { authApi } from './auth.api'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setLoading(true)
    await authApi.forgotPassword(email).catch(() => {})
    setDone(true)
    setLoading(false)
  }

  if (done) return (
    <main className="flex flex-1 items-center justify-center p-4">
      <div className="text-center space-y-2">
        <h1 className="text-xl font-bold">Check your email</h1>
        <p className="text-zinc-600">If an account exists for {email}, we sent a reset link.</p>
      </div>
    </main>
  )

  return (
    <main className="flex flex-1 items-center justify-center p-4">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4 bg-white p-8 rounded-xl shadow">
        <h1 className="text-2xl font-bold">Forgot password</h1>
        <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)}
          className="input w-full" required />
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Sending…' : 'Send reset link'}
        </button>
      </form>
    </main>
  )
}
