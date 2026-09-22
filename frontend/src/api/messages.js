import api from './client'

export async function fetchMessages(requestId) {
  const { data } = await api.get(`/messages/request/${requestId}`)
  return data
}

export async function sendMessage(requestId, text) {
  const { data } = await api.post(`/messages/request/${requestId}`, { text })
  return data
}
