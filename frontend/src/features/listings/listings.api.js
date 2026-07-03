import { api } from '../../lib/api'

const BASE = import.meta.env.VITE_API_BASE ?? '/api/v1'

function getCsrfToken() {
  const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/)
  return match ? decodeURIComponent(match[1]) : null
}

export const listingsApi = {
  getCategories:   ()             => api.get('/categories'),
  getMyListings:   (status)       => api.get(`/me/listings${status ? `?status=${status}` : ''}`),
  createListing:   (body)         => api.post('/me/listings', body),
  updateListing:   (id, body)     => api.put(`/me/listings/${id}`, body),
  patchStatus:     (id, status)   => api.patch(`/me/listings/${id}/status`, { status }),
  deleteImage:     (id, imgId)    => api.delete(`/me/listings/${id}/images/${imgId}`),

  uploadImage: async (id, file) => {
    const formData = new FormData()
    formData.append('image', file)
    const csrf = getCsrfToken()
    const res = await fetch(`${BASE}/me/listings/${id}/images`, {
      method: 'POST',
      headers: csrf ? { 'X-XSRF-TOKEN': csrf } : {},
      credentials: 'same-origin',
      body: formData,
    })
    if (!res.ok) {
      const data = await res.json().catch(() => ({}))
      const err = data?.error ?? {}
      const e = new Error(err.message ?? 'Upload failed')
      e.code = err.code
      throw e
    }
    return res.json()
  },

  adminGetCategories:       ()           => api.get('/admin/categories'),
  adminCreateCategory:      (body)       => api.post('/admin/categories', body),
  adminUpdateCategory:      (id, body)   => api.put(`/admin/categories/${id}`, body),
  adminDeleteCategory:      (id)         => api.delete(`/admin/categories/${id}`),
  adminGetProducts:         (page = 0)   => api.get(`/admin/products?page=${page}&size=20`),
  adminPatchProductStatus:  (id, status) => api.patch(`/admin/products/${id}/status`, { status }),
}
