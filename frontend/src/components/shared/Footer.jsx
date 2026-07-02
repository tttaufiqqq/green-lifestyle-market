import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="border-t border-zinc-200 bg-white mt-auto">
      <div className="max-w-7xl mx-auto px-4 md:px-8 py-8 flex flex-col md:flex-row items-center justify-between gap-4">
        <span className="text-emerald-600 font-bold">GreenMarket</span>

        <nav className="flex gap-6 text-sm text-zinc-500">
          <Link to="/help" className="hover:text-zinc-900 transition-colors">Help</Link>
          <Link to="/articles" className="hover:text-zinc-900 transition-colors">Articles</Link>
        </nav>

        <p className="text-sm text-zinc-400">
          &copy; {new Date().getFullYear()} GreenMarket. All rights reserved.
        </p>
      </div>
    </footer>
  )
}
