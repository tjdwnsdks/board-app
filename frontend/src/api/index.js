import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// 요청 인터셉터: JWT 토큰 자동 첨부
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 응답 인터셉터: 401 시 로그아웃
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// Auth API
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data)
}

// Post API
export const postApi = {
  getList: (page = 0, size = 10, keyword = '') =>
    api.get('/posts', { params: { page, size, keyword } }),
  getDetail: (id) => api.get(`/posts/${id}`),
  create: (data) => api.post('/posts', data),
  update: (id, data) => api.put(`/posts/${id}`, data),
  delete: (id) => api.delete(`/posts/${id}`)
}

// Comment API
export const commentApi = {
  add: (postId, data) => api.post(`/posts/${postId}/comments`, data),
  delete: (commentId) => api.delete(`/posts/comments/${commentId}`)
}

export default api
