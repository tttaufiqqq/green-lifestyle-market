import { useState, useEffect } from 'react'
import { authApi } from './auth.api'
import { useAuthStore } from '../../stores/auth'

export default function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [bank, setBank] = useState(null)
  const [msg, setMsg] = useState(null)
  const setUser = useAuthStore(s => s.setUser)

  useEffect(() => {
    authApi.getProfile().then(setProfile)
    authApi.getBankAccount().then(setBank).catch(() => {})
  }, [])

  const saveProfile = async (e) => {
    e.preventDefault()
    const fd = Object.fromEntries(new FormData(e.target))
    const updated = await authApi.updateProfile(fd)
    setProfile(updated)
    setUser({ ...updated, emailVerified: updated.emailVerified })
    setMsg('Profile saved')
  }

  const saveBank = async (e) => {
    e.preventDefault()
    const fd = Object.fromEntries(new FormData(e.target))
    const updated = await authApi.upsertBankAccount(fd)
    setBank(updated)
    setMsg('Bank account saved')
  }

  if (!profile) return <div className="p-8">Loading…</div>

  return (
    <main className="max-w-2xl mx-auto p-6 space-y-8">
      <h1 className="text-2xl font-bold">My Profile</h1>
      {msg && <p className="text-green-700 text-sm">{msg}</p>}

      <form onSubmit={saveProfile} className="bg-white p-6 rounded-xl shadow space-y-3">
        <h2 className="font-semibold text-lg">Personal info</h2>
        <input name="name" defaultValue={profile.name} placeholder="Full name" className="input w-full" required />
        <input name="phone" defaultValue={profile.phone ?? ''} placeholder="Phone" className="input w-full" />
        <select name="affiliation" defaultValue={profile.affiliation} className="input w-full">
          {['PUBLIC','UTEM_STUDENT','UTEM_STAFF'].map(a => <option key={a} value={a}>{a.replace('_',' ')}</option>)}
        </select>
        <button className="btn-primary">Save profile</button>
      </form>

      <form onSubmit={saveBank} className="bg-white p-6 rounded-xl shadow space-y-3">
        <h2 className="font-semibold text-lg">Bank account</h2>
        {bank?.verified === false && <p className="text-amber-600 text-sm">Pending admin verification</p>}
        <input name="bankName" defaultValue={bank?.bankName ?? ''} placeholder="Bank name" className="input w-full" required />
        <input name="accountNo" defaultValue={bank?.accountNo ?? ''} placeholder="Account number" className="input w-full" required />
        <input name="holderName" defaultValue={bank?.holderName ?? ''} placeholder="Account holder name" className="input w-full" required />
        <button className="btn-primary">Save bank account</button>
      </form>
    </main>
  )
}
