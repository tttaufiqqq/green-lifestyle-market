import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/auth'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const login = useAuthStore(s => s.login)
  const navigate = useNavigate()

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login(form.email, form.password)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex flex-1 items-center justify-center p-4">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4 bg-white p-8 rounded-xl shadow">
        <h1 className="text-2xl font-bold">Sign in</h1>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <input name="email" type="email" placeholder="Email" value={form.email}
          onChange={handle} className="input w-full" required />
        <input name="password" type="password" placeholder="Password" value={form.password}
          onChange={handle} className="input w-full" required />
        <div className="text-right text-sm">
          <Link to="/forgot-password" className="text-green-700 hover:underline">Forgot password?</Link>
        </div>
        <button type="submit" disabled={loading}
          className="btn-primary w-full">{loading ? 'Signing in…' : 'Sign in'}</button>
        <p className="text-center text-sm">No account?{' '}
          <Link to="/register" className="text-green-700 hover:underline">Register</Link></p>
      </form>
    </main>
  )
}
