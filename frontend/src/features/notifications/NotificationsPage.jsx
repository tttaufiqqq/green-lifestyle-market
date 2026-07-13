import { useEffect, useState } from 'react'
import { useNotificationStore } from '../../stores/notifications'
import { notificationApi } from './notification.api'
import { isPushSubscribed, subscribeToPush, unsubscribeFromPush } from '../../lib/push'

export default function NotificationsPage() {
  const items = useNotificationStore((s) => s.items)
  const markReadLocal = useNotificationStore((s) => s.markReadLocal)
  const [pushEnabled, setPushEnabled] = useState(false)
  const [pushBusy, setPushBusy] = useState(false)

  useEffect(() => {
    isPushSubscribed().then(setPushEnabled)
  }, [])

  async function handleTogglePush() {
    setPushBusy(true)
    try {
      if (pushEnabled) {
        await unsubscribeFromPush()
        setPushEnabled(false)
      } else {
        const ok = await subscribeToPush()
        setPushEnabled(ok)
      }
    } finally {
      setPushBusy(false)
    }
  }

  async function handleClick(n) {
    if (!n.readAt) {
      markReadLocal(n.id)
      try { await notificationApi.markRead(n.id) } catch { /* best-effort */ }
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 md:px-8 py-8 flex-1 w-full">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-zinc-900">Notifications</h1>
        <button
          onClick={handleTogglePush}
          disabled={pushBusy}
          className="text-sm px-4 py-2 rounded-lg border border-zinc-300 hover:bg-zinc-50 transition-colors disabled:opacity-50"
        >
          {pushEnabled ? 'Disable push notifications' : 'Enable push notifications'}
        </button>
      </div>

      {items.length === 0 && (
        <p className="text-zinc-400 text-center py-16">No notifications yet</p>
      )}

      <div className="space-y-2">
        {items.map((n) => (
          <button
            key={n.id}
            onClick={() => handleClick(n)}
            className={`block w-full text-left px-4 py-4 rounded-lg border transition-colors ${
              n.readAt ? 'bg-white border-zinc-200' : 'bg-emerald-50 border-emerald-200'
            }`}
          >
            <p className="font-medium text-zinc-900">{n.title}</p>
            <p className="text-sm text-zinc-600 mt-1">{n.body}</p>
            <p className="text-xs text-zinc-400 mt-2">{new Date(n.createdAt).toLocaleString()}</p>
          </button>
        ))}
      </div>
    </div>
  )
}
