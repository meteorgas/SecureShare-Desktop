import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api', // backend base URL
});

// Authorization header’a direkt token (Bearer dahil) ekleniyor
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token && config.headers) {
    config.headers.Authorization = token; // "Bearer ..." formatında burada ekleniyor

  }
  return config;
});

export default api;
