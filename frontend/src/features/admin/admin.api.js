import { api } from '../../lib/api'

export const adminApi = {
  // Dashboard
  getDashboard: ()                       => api.get('/admin/dashboard'),

  // Users
  getUsers:     ()                       => api.get('/admin/users'),
  updateUserStatus: (id, status)         => api.patch(`/admin/users/${id}/status`, { status }),
  verifyBank:   (id, verified)           => api.patch(`/admin/users/${id}/bank-account/verify?verified=${verified}`),

  // Orders
  getOrders:    (status)                 => api.get(`/admin/orders${status ? `?status=${status}` : ''}`),
  getOrder:     (orderNo)                => api.get(`/admin/orders/${orderNo}`),

  // Refunds
  getRefunds:   (status)                 => api.get(`/admin/refunds${status ? `?status=${status}` : ''}`),
  approveRefund:(id)                     => api.post(`/admin/refunds/${id}/approve`),
  rejectRefund: (id, adminNote)          => api.post(`/admin/refunds/${id}/reject`, { adminNote }),
  processRefund:(id, bankRef, adminNote) => api.post(`/admin/refunds/${id}/process`, { bankRef, adminNote }),

  // Payouts
  getEligible:  ()                       => api.get('/admin/payouts/eligible'),
  getPending:   ()                       => api.get('/admin/payouts/pending'),
  createPayout: (req)                    => api.post('/admin/payouts', req),
  markPaid:     (id, bankRef)            => api.post(`/admin/payouts/${id}/mark-paid`, { bankRef }),

  // Reconciliation
  reconcile:    (date)                   => api.get(`/admin/reconciliation?date=${date}`),

  // Listings moderation (uses existing product admin endpoints)
  getListings:  ()                       => api.get('/admin/products'),
  setListingStatus: (id, status)         => api.patch(`/admin/products/${id}/status`, { status }),
}
