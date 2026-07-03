import { api } from '../../lib/api'

export const cartApi = {
  getCart:        ()                      => api.get('/cart'),
  addItem:        (productId, quantity)   => api.post('/cart/items', { productId, quantity }),
  updateQuantity: (itemId, quantity)      => api.patch(`/cart/items/${itemId}`, { quantity }),
  removeItem:     (itemId)                => api.delete(`/cart/items/${itemId}`),
}
