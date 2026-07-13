import { create } from 'zustand'

let nextId = 0

export const useToastStore = create((set) => ({
  toasts: [], // { id, type: 'success'|'error', message }
  dismiss: (id) => set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),
}))

function push(type, message) {
  const id = ++nextId
  useToastStore.setState((s) => ({ toasts: [...s.toasts, { id, type, message }] }))
  setTimeout(() => useToastStore.getState().dismiss(id), type === 'error' ? 5000 : 3000)
}

export const toast = {
  success: (message) => push('success', message),
  error: (message) => push('error', message),
}
