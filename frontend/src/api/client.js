import axios from 'axios'
import { TOKEN_KEY } from './token'

export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  const isAuthRequest = config.url?.startsWith('/auth/login') || config.url?.startsWith('/auth/register')
  if (token && !isAuthRequest) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new Event('auth:unauthorized'))
    }
    return Promise.reject(error)
  },
)

export function getErrorMessage(error) {
  return (
    error.response?.data?.detail ||
    error.response?.data?.title ||
    error.response?.data?.message ||
    'Не вдалося виконати запит. Перевірте підключення до сервера.'
  )
}

export default api
