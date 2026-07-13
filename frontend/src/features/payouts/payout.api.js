import { api } from '../../lib/api'

export const payoutApi = {
  getMyPayouts: () => api.get('/me/payouts'),
}
