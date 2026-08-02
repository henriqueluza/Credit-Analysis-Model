import { useEffect, useState } from 'react'
import { listarHistorico } from '../api/analises'
import { mensagemDeErro } from '../api/client'
import type { PaginaResponse, SolicitacaoAnaliseResponse } from '../api/types'

const TAMANHO_PAGINA = 10

const situacaoMoradiaLabels = {
  OWN: 'Própria',
  RENT: 'Aluguel',
  FREE: 'Graça',
}

function formatarMoeda(valor: number): string {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatarData(iso: string): string {
  return new Date(iso).toLocaleString('pt-BR')
}

export function HistoricoPage() {
  const [pagina, setPagina] = useState(0)
  const [dados, setDados] = useState<PaginaResponse<SolicitacaoAnaliseResponse> | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    let cancelado = false

    async function carregar() {
      setCarregando(true)
      setErro(null)
      try {
        const resposta = await listarHistorico(pagina, TAMANHO_PAGINA)
        if (!cancelado) {
          setDados(resposta)
        }
      } catch (error) {
        if (!cancelado) {
          setErro(mensagemDeErro(error))
        }
      } finally {
        if (!cancelado) {
          setCarregando(false)
        }
      }
    }

    carregar()
    return () => {
      cancelado = true
    }
  }, [pagina])

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-900">Histórico de Análises</h1>

      {erro && <p className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{erro}</p>}

      {carregando && <p className="text-sm text-slate-500">Carregando...</p>}

      {!carregando && dados && dados.content.length === 0 && (
        <p className="text-sm text-slate-500">Nenhuma análise realizada ainda.</p>
      )}

      {!carregando && dados && dados.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white">
            <table className="min-w-full divide-y divide-slate-200 text-sm">
              <thead className="bg-slate-50 text-left text-xs font-medium uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">Data</th>
                  <th className="px-4 py-3">Empréstimo</th>
                  <th className="px-4 py-3">Prazo</th>
                  <th className="px-4 py-3">Moradia</th>
                  <th className="px-4 py-3">Resultado</th>
                  <th className="px-4 py-3">Prob. risco</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {dados.content.map((analise) => (
                  <tr key={analise.id}>
                    <td className="px-4 py-3">#{analise.id}</td>
                    <td className="px-4 py-3">{formatarData(analise.criadoEm)}</td>
                    <td className="px-4 py-3">{formatarMoeda(analise.valorEmprestimo)}</td>
                    <td className="px-4 py-3">{analise.prazoMeses} meses</td>
                    <td className="px-4 py-3">{situacaoMoradiaLabels[analise.situacaoMoradia]}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                          analise.resultado === 'APROVADO'
                            ? 'bg-green-100 text-green-700'
                            : 'bg-red-100 text-red-700'
                        }`}
                      >
                        {analise.resultado === 'APROVADO' ? 'Aprovado' : 'Reprovado'}
                      </span>
                    </td>
                    <td className="px-4 py-3">{(analise.probabilidadeRisco * 100).toFixed(2)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between text-sm text-slate-600">
            <span>
              Página {dados.page + 1} de {Math.max(dados.totalPages, 1)} — {dados.totalElements} análises no total
            </span>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setPagina((p) => Math.max(p - 1, 0))}
                disabled={dados.page === 0}
                className="rounded-md border border-slate-300 px-3 py-1.5 disabled:opacity-40"
              >
                Anterior
              </button>
              <button
                type="button"
                onClick={() => setPagina((p) => p + 1)}
                disabled={dados.page + 1 >= dados.totalPages}
                className="rounded-md border border-slate-300 px-3 py-1.5 disabled:opacity-40"
              >
                Próxima
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
