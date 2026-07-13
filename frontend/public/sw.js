self.addEventListener('push', (event) => {
  const data = event.data ? event.data.json() : {}
  const title = data.title || 'Green Lifestyle Market'

  event.waitUntil(
    self.registration.showNotification(title, {
      body: data.body || '',
      data: { type: data.type, ref: data.data },
    })
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  event.waitUntil(clients.openWindow('/notifications'))
})
