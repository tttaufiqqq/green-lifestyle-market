import { create } from 'zustand'

export const useCartStore = create((set) => ({
  count: 0,
  setCount: (count) => set({ count }),
}))
