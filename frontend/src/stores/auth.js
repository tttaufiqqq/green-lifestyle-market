import { create } from 'zustand'
import { api } from '../lib/api'

export const useAuthStore = create((set, get) => ({
  user: null,
  loading: true,

  init: async () => {
    try {
      const data = await api.get('/auth/me')
      set({ user: data, loading: false })
    } catch {
      set({ user: null, loading: false })
    }
  },

  login: async (email, password) => {
    const data = await api.post('/auth/login', { email, password })
    set({ user: data })
    return data
  },

  logout: async () => {
    await api.post('/auth/logout')
    set({ user: null })
  },

  setUser: (user) => set({ user }),
}))
