import { Outlet } from 'react-router-dom'
import { useEffect } from 'react'
import Navbar from './components/shared/Navbar'
import Footer from './components/shared/Footer'
import { useAuthStore } from './stores/auth'

export default function App() {
  const init = useAuthStore(s => s.init)
  useEffect(() => { init() }, [])

  return (
    <div className="min-h-screen bg-zinc-50 flex flex-col">
      <Navbar />
      <Outlet />
      <Footer />
    </div>
  )
}
