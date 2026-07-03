import { api } from '../../lib/api'

export const authApi = {
  register:       (body) => api.post('/auth/register', body),
  login:          (body) => api.post('/auth/login', body),
  logout:         ()     => api.post('/auth/logout'),
  me:             ()     => api.get('/auth/me'),
  verifyEmail:    (token)=> api.post('/auth/verify-email', { token }),
  forgotPassword: (email)=> api.post('/auth/forgot-password', { email }),
  resetPassword:  (body) => api.post('/auth/reset-password', body),
  getProfile:     ()     => api.get('/me/profile'),
  updateProfile:  (body) => api.put('/me/profile', body),
  changePassword: (body) => api.put('/me/password', body),
  getBankAccount: ()     => api.get('/me/bank-account'),
  upsertBankAccount:(body)=> api.put('/me/bank-account', body),
}
