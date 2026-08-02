import { apiClient } from './client'
import type { PaginaResponse, SolicitacaoAnaliseRequest, SolicitacaoAnaliseResponse } from './types'

export async function criarAnalise(dados: SolicitacaoAnaliseRequest): Promise<SolicitacaoAnaliseResponse> {
  const response = await apiClient.post<SolicitacaoAnaliseResponse>('/api/analises', dados)
  return response.data
}

export async function listarHistorico(page: number, size: number): Promise<PaginaResponse<SolicitacaoAnaliseResponse>> {
  const response = await apiClient.get<PaginaResponse<SolicitacaoAnaliseResponse>>('/api/analises', {
    params: { page, size },
  })
  return response.data
}
