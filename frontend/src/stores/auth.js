import { create } from 'zustand'
import { api } from '../lib/api'
import { useCartStore } from './cart'

function syncCart(data) {
  useCartStore.getState().setCount(data?.cartCount ?? 0)
}

export const useAuthStore = create((set) => ({
  user: null,
  loading: true,

  init: async () => {
    try {
      const data = await api.get('/auth/me')
      set({ user: data, loading: false })
      syncCart(data)
    } catch {
      set({ user: null, loading: false })
    }
  },

  login: async (email, password) => {
    const data = await api.post('/auth/login', { email, password })
    set({ user: data })
    syncCart(data)
    return data
  },

  logout: async () => {
    await api.post('/auth/logout')
    set({ user: null })
    useCartStore.getState().setCount(0)
  },

  setUser: (user) => set({ user }),
}))
