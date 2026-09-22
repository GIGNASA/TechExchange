import api from './client'

export async function fetchExchangeRequests() {
  const { data } = await api.get('/exchange-requests')
  return data
}

export async function createExchangeRequest(payload) {
  const { data } = await api.post('/exchange-requests', payload)
  return data
}

export async function updateExchangeStatus(id, status) {
  const { data } = await api.put(`/exchange-requests/${id}/status`, { status })
  return data
}
