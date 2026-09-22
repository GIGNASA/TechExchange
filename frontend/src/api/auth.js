import api from './client'

export async function loginUser(payload) {
  const { data } = await api.post('/auth/login', payload)
  return data
}

export async function registerUser(payload) {
  const { data } = await api.post('/auth/register', payload)
  return data
}

export async function fetchCurrentUser() {
  const { data } = await api.get('/users/me')
  return data
}

export async function updateCurrentUser(payload) {
  const { data } = await api.put('/users/me', payload)
  return data
}
