import { api } from './client'

export const parameterApi = {
  resources: () => api.get('/api/parameter-resources').then((r) => r.data),
  metadata: (code) => api.get(`/api/parameter-resources/${code}/metadata`).then((r) => r.data),
  options: (code, field) =>
    api.get(`/api/parameter-resources/${code}/fields/${field}/options`).then((r) => r.data),
  search: (code, body) =>
    api.post(`/api/parameter-resources/${code}/search`, body).then((r) => r.data),
}

export const requestApi = {
  create: (body) => api.post('/api/change-requests', body).then((r) => r.data),
  list: (status, page = 0, size = 20) =>
    api.get('/api/change-requests', { params: { status: status || undefined, page, size } }).then((r) => r.data),
  detail: (id) => api.get(`/api/change-requests/${id}`).then((r) => r.data),
  approve: (id, note) => api.post(`/api/change-requests/${id}/approve`, { note }).then((r) => r.data),
  reject: (id, note) => api.post(`/api/change-requests/${id}/reject`, { note }).then((r) => r.data),
  withdraw: (id) => api.post(`/api/change-requests/${id}/withdraw`).then((r) => r.data),
}

export const customQueryApi = {
  all: () => api.get('/api/custom-queries').then((r) => r.data),
  metadata: (code) => api.get(`/api/custom-queries/${code}/metadata`).then((r) => r.data),
  search: (code, body) => api.post(`/api/custom-queries/${code}/search`, body).then((r) => r.data),
}

