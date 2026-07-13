import { Client } from '@stomp/stompjs'

let client = null

export function connectNotifications(onMessage) {
  if (client) return

  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
  client = new Client({
    brokerURL: `${protocol}://${window.location.host}/ws`,
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe('/user/queue/notifications', (message) => {
        onMessage(JSON.parse(message.body))
      })
    },
  })
  client.activate()
}

export function disconnectNotifications() {
  client?.deactivate()
  client = null
}
