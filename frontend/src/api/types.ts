export type SituacaoMoradia = 'OWN' | 'RENT' | 'FREE'

export type StatusAnalise = 'APROVADO' | 'REPROVADO'

export interface LoginRequest {
  email: string
  senha: string
}

export interface TokenResponse {
  token: string
  tipo: string
  expiraEm: string
}

export interface SolicitacaoAnaliseRequest {
  idade: number
  salarioAnual: number
  situacaoMoradia: SituacaoMoradia
  saldoContaCorrente: number
  saldoContaPoupanca: number
  valorEmprestimo: number
  prazoMeses: number
}

export interface SolicitacaoAnaliseResponse {
  id: number
  idade: number
  salarioAnual: number
  situacaoMoradia: SituacaoMoradia
  saldoContaCorrente: number
  saldoContaPoupanca: number
  valorEmprestimo: number
  prazoMeses: number
  criadoEm: string
  resultado: StatusAnalise
  probabilidadeRisco: number
  thresholdUtilizado: number
  versaoModelo: string
  analisadoEm: string
}

export interface PaginaResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ErroApi {
  timestamp?: string
  status?: number
  error?: string
  message?: string
}
