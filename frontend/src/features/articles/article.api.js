import { api } from '../../lib/api'

export const articleApi = {
  list: () => api.get('/articles'),
  get: (slug) => api.get(`/articles/${slug}`),

  adminList: () => api.get('/admin/articles'),
  adminGet: (id) => api.get(`/admin/articles/${id}`),
  adminCreate: (body) => api.post('/admin/articles', body),
  adminUpdate: (id, body) => api.put(`/admin/articles/${id}`, body),
  adminDelete: (id) => api.delete(`/admin/articles/${id}`),
}
