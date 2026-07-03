import { api } from '../../lib/api'

export const checkoutApi = {
  preview:  (req) => api.post('/checkout/preview', req),
  checkout: (req) => api.post('/checkout', req),
}

export const paymentApi = {
  getStatus: (paymentNo) => api.get(`/payments/${paymentNo}`),
}
