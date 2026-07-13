import { create } from 'zustand'

export const useNotificationStore = create((set) => ({
  unreadCount: 0,
  items: [],

  setItems: (items) => set({
    items,
    unreadCount: items.filter((n) => !n.readAt).length,
  }),

  addLive: (notification) => set((s) => ({
    items: [notification, ...s.items],
    unreadCount: s.unreadCount + 1,
  })),

  markReadLocal: (id) => set((s) => ({
    items: s.items.map((n) => (n.id === id ? { ...n, readAt: n.readAt ?? new Date().toISOString() } : n)),
    unreadCount: Math.max(0, s.unreadCount - (s.items.find((n) => n.id === id && !n.readAt) ? 1 : 0)),
  })),

  markAllReadLocal: () => set((s) => ({
    items: s.items.map((n) => ({ ...n, readAt: n.readAt ?? new Date().toISOString() })),
    unreadCount: 0,
  })),

  reset: () => set({ unreadCount: 0, items: [] }),
}))
