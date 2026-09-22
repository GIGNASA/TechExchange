import api from './client'

export async function fetchDevices(params = {}) {
  const { data } = await api.get('/devices', { params })
  return data
}

export async function fetchMyDevices() {
  const { data } = await api.get('/devices/my')
  return data
}

export async function fetchDevice(id) {
  const { data } = await api.get(`/devices/${id}`)
  return data
}

export async function createDevice(payload) {
  const { data } = await api.post('/devices', payload)
  return data
}

export async function updateDevice(id, payload) {
  const { data } = await api.put(`/devices/${id}`, payload)
  return data
}

export async function deleteDevice(id) {
  await api.delete(`/devices/${id}`)
}

export async function fetchMarketplaceStats() {
  const { data } = await api.get('/devices/stats')
  return data
}
