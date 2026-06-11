import api from './axios'

export const supplierApi = {
  getAll: (type) => api.get('/api/suppliers', { params: type ? { type } : {} }),
  getById: (id) => api.get(`/api/suppliers/${id}`),
  create: (data) => api.post('/api/suppliers', data),
  update: (id, data) => api.put(`/api/suppliers/${id}`, data),
  delete: (id) => api.delete(`/api/suppliers/${id}`),
}