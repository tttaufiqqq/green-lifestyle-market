import { api } from '../../lib/api'

export const catalogApi = {
  getProducts: (filters) => {
    const q = new URLSearchParams()
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') q.set(k, v)
    })
    return api.get(`/products?${q}`)
  },
  getProduct: (slug) => api.get(`/products/${slug}`),
}
