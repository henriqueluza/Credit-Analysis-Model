import axios, { AxiosError } from 'axios'
import type { ErroApi } from './types'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.dispatchEvent(new Event('unauthorized'))
    }
    return Promise.reject(error)
  },
)

export function mensagemDeErro(error: unknown): string {
  if (axios.isAxiosError<ErroApi>(error)) {
    return error.response?.data?.message ?? error.message
  }
  return 'Erro inesperado. Tente novamente.'
}
