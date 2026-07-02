import { Outlet } from 'react-router-dom'
import Navbar from './components/shared/Navbar'
import Footer from './components/shared/Footer'

export default function App() {
  return (
    <div className="min-h-screen bg-zinc-50 flex flex-col">
      <Navbar />
      <Outlet />
      <Footer />
    </div>
  )
}
