import { useEffect } from 'react'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notifications'
import { notificationApi } from './notification.api'
import { connectNotifications, disconnectNotifications } from '../../lib/ws'

export function useNotifications() {
  const user = useAuthStore((s) => s.user)
  const setItems = useNotificationStore((s) => s.setItems)
  const addLive = useNotificationStore((s) => s.addLive)
  const reset = useNotificationStore((s) => s.reset)

  useEffect(() => {
    if (!user) {
      disconnectNotifications()
      reset()
      return
    }
    notificationApi.list().then(setItems).catch(() => {})
    connectNotifications(addLive)
    return () => disconnectNotifications()
  }, [user])
}
