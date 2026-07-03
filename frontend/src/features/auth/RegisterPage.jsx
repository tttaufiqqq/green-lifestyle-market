import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from './auth.api'

const AFFILIATIONS = ['PUBLIC', 'UTEM_STUDENT', 'UTEM_STAFF']

export default function RegisterPage() {
  const [form, setForm] = useState({ name:'', email:'', password:'', phone:'', affiliation:'PUBLIC' })
  const [error, setError] = useState(null)
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(false)

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await authApi.register(form)
      setDone(true)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  if (done) return (
    <main className="flex flex-1 items-center justify-center p-4">
      <div className="text-center space-y-2">
        <h1 className="text-2xl font-bold text-green-700">Check your email!</h1>
        <p className="text-zinc-600">We sent a verification link to <strong>{form.email}</strong>.</p>
      </div>
    </main>
  )

  return (
    <main className="flex flex-1 items-center justify-center p-4">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4 bg-white p-8 rounded-xl shadow">
        <h1 className="text-2xl font-bold">Create account</h1>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <input name="name" placeholder="Full name" value={form.name} onChange={handle} className="input w-full" required />
        <input name="email" type="email" placeholder="Email" value={form.email} onChange={handle} className="input w-full" required />
        <input name="password" type="password" placeholder="Password (min 8)" value={form.password} onChange={handle} className="input w-full" required minLength={8} />
        <input name="phone" placeholder="Phone (optional)" value={form.phone} onChange={handle} className="input w-full" />
        <select name="affiliation" value={form.affiliation} onChange={handle} className="input w-full">
          {AFFILIATIONS.map(a => <option key={a} value={a}>{a.replace('_', ' ')}</option>)}
        </select>
        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? 'Creating…' : 'Create account'}
        </button>
        <p className="text-center text-sm">Have an account?{' '}
          <Link to="/login" className="text-green-700 hover:underline">Sign in</Link></p>
      </form>
    </main>
  )
}
