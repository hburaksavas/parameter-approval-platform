import axios from 'axios'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  if (import.meta.env.VITE_DEMO_AUTH === 'false') return config
  const stored = localStorage.getItem('parameter-demo-user')
  if (stored) {
    const user = JSON.parse(stored)
    config.headers['X-User-Id'] = user.id
    config.headers['X-User-Name'] = user.name
    config.headers['X-User-Roles'] = user.roles.join(',')
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || 'Beklenmeyen hata'
    return Promise.reject(new Error(message))
  },
)
