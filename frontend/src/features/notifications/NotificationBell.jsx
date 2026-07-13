import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Bell } from 'lucide-react'
import { useNotificationStore } from '../../stores/notifications'
import { notificationApi } from './notification.api'
import { useNotifications } from './useNotifications'

export default function NotificationBell() {
  useNotifications()
  const [open, setOpen] = useState(false)
  const items = useNotificationStore((s) => s.items)
  const unreadCount = useNotificationStore((s) => s.unreadCount)
  const markReadLocal = useNotificationStore((s) => s.markReadLocal)
  const markAllReadLocal = useNotificationStore((s) => s.markAllReadLocal)

  async function handleItemClick(n) {
    if (!n.readAt) {
      markReadLocal(n.id)
      try { await notificationApi.markRead(n.id) } catch { /* best-effort */ }
    }
    setOpen(false)
  }

  async function handleMarkAllRead() {
    markAllReadLocal()
    try { await notificationApi.markAllRead() } catch { /* best-effort */ }
  }

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="relative p-1 text-zinc-600 hover:text-zinc-900 transition-colors"
        aria-label="Notifications"
      >
        <Bell className="h-6 w-6" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 bg-emerald-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 bg-white border border-zinc-200 rounded-lg shadow-lg z-50">
          <div className="flex items-center justify-between px-4 py-2 border-b border-zinc-100">
            <span className="font-medium text-sm text-zinc-900">Notifications</span>
            <button onClick={handleMarkAllRead} className="text-xs text-emerald-600 hover:underline">
              Mark all read
            </button>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {items.length === 0 && (
              <p className="px-4 py-6 text-sm text-zinc-400 text-center">No notifications yet</p>
            )}
            {items.slice(0, 8).map((n) => (
              <button
                key={n.id}
                onClick={() => handleItemClick(n)}
                className={`block w-full text-left px-4 py-3 border-b border-zinc-50 hover:bg-zinc-50 transition-colors ${!n.readAt ? 'bg-emerald-50/50' : ''}`}
              >
                <p className="text-sm font-medium text-zinc-900">{n.title}</p>
                <p className="text-xs text-zinc-500 mt-0.5">{n.body}</p>
                <p className="text-xs text-zinc-400 mt-1">{new Date(n.createdAt).toLocaleString()}</p>
              </button>
            ))}
          </div>
          <Link
            to="/notifications"
            onClick={() => setOpen(false)}
            className="block text-center text-sm text-emerald-600 hover:underline px-4 py-2 border-t border-zinc-100"
          >
            View all
          </Link>
        </div>
      )}
    </div>
  )
}
