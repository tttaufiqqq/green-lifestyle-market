import { api } from '../../lib/api'

export const notificationApi = {
  list: () => api.get('/me/notifications'),
  markRead: (id) => api.patch(`/me/notifications/${id}/read`),
  markAllRead: () => api.patch('/me/notifications/read-all'),
}
