import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { authApi } from './auth.api'

export default function VerifyEmailPage() {
  const [params] = useSearchParams()
  const [status, setStatus] = useState('verifying')

  useEffect(() => {
    const token = params.get('token')
    if (!token) { setStatus('error'); return }
    authApi.verifyEmail(token)
      .then(() => setStatus('success'))
      .catch(() => setStatus('error'))
  }, [])

  return (
    <main className="flex flex-1 items-center justify-center p-4">
      <div className="text-center space-y-3">
        {status === 'verifying' && <p>Verifying your email…</p>}
        {status === 'success' && <>
          <h1 className="text-2xl font-bold text-green-700">Email verified!</h1>
          <Link to="/login" className="btn-primary inline-block">Sign in</Link>
        </>}
        {status === 'error' && <>
          <h1 className="text-2xl font-bold text-red-600">Invalid or expired link</h1>
          <p className="text-zinc-600">Please register again or contact support.</p>
        </>}
      </div>
    </main>
  )
}
