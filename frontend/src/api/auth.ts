import { apiClient } from './client'
import type { LoginRequest, TokenResponse } from './types'

export async function login(dados: LoginRequest): Promise<TokenResponse> {
  const response = await apiClient.post<TokenResponse>('/auth/login', dados)
  return response.data
}
