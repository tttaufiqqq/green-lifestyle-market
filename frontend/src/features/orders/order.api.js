import { api } from '../../lib/api'

export const orderApi = {
  // Buyer
  getOrders:      (tab)          => api.get(`/me/orders${tab ? `?tab=${tab}` : ''}`),
  getOrder:       (orderNo)      => api.get(`/me/orders/${orderNo}`),
  cancel:         (orderNo, req) => api.post(`/me/orders/${orderNo}/cancel`, req ?? {}),
  confirmReceipt: (orderNo)      => api.post(`/me/orders/${orderNo}/confirm-receipt`),
  refundRequest:  (orderNo, req) => api.post(`/me/orders/${orderNo}/refund-request`, req),

  // Seller
  getSales:    (tab)          => api.get(`/me/sales${tab ? `?tab=${tab}` : ''}`),
  getSale:     (orderNo)      => api.get(`/me/sales/${orderNo}`),
  confirm:     (orderNo)      => api.post(`/me/sales/${orderNo}/confirm`),
  reject:      (orderNo, req) => api.post(`/me/sales/${orderNo}/reject`, req ?? {}),
  ship:        (orderNo, req) => api.post(`/me/sales/${orderNo}/ship`, req),
  readyMeetup: (orderNo, req) => api.post(`/me/sales/${orderNo}/ready-meetup`, req),
}
