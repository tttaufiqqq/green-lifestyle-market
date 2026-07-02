import { Link } from 'react-router-dom'

export default function Navbar() {
  return (
    <header className="sticky top-0 z-50 bg-white border-b border-zinc-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 md:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="text-emerald-600 font-bold text-xl tracking-tight">
          GreenMarket
        </Link>

        <nav className="hidden md:flex items-center gap-6 text-sm text-zinc-600">
          <Link to="/browse" className="hover:text-zinc-900 transition-colors">Browse</Link>
        </nav>

        <div className="flex items-center gap-3 text-sm">
          <Link
            to="/login"
            className="text-zinc-600 hover:text-zinc-900 transition-colors"
          >
            Sign in
          </Link>
          <Link
            to="/register"
            className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-medium transition-colors"
          >
            Sign up
          </Link>
        </div>
      </div>
    </header>
  )
}
