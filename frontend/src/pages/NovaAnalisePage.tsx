import { useState, type FormEvent } from 'react'
import { criarAnalise } from '../api/analises'
import { mensagemDeErro } from '../api/client'
import type { SituacaoMoradia, SolicitacaoAnaliseRequest, SolicitacaoAnaliseResponse } from '../api/types'

const situacaoMoradiaLabels: Record<SituacaoMoradia, string> = {
  OWN: 'Casa própria',
  RENT: 'Aluguel',
  FREE: 'Mora de graça / com os pais',
}

const valoresIniciais: SolicitacaoAnaliseRequest = {
  idade: 25,
  salarioAnual: 50000,
  situacaoMoradia: 'OWN',
  saldoContaCorrente: 1500,
  saldoContaPoupanca: 5000,
  valorEmprestimo: 10000,
  prazoMeses: 24,
}

export function NovaAnalisePage() {
  const [dados, setDados] = useState<SolicitacaoAnaliseRequest>(valoresIniciais)
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [resultado, setResultado] = useState<SolicitacaoAnaliseResponse | null>(null)

  function atualizarCampo<K extends keyof SolicitacaoAnaliseRequest>(campo: K, valor: SolicitacaoAnaliseRequest[K]) {
    setDados((atual) => ({ ...atual, [campo]: valor }))
  }

  async function aoEnviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setResultado(null)
    setCarregando(true)
    try {
      const resposta = await criarAnalise(dados)
      setResultado(resposta)
    } catch (error) {
      setErro(mensagemDeErro(error))
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-semibold text-slate-900">Nova Análise de Crédito</h1>

      <form onSubmit={aoEnviar} className="grid grid-cols-1 gap-4 rounded-lg border border-slate-200 bg-white p-6 sm:grid-cols-2">
        <Campo label="Idade" htmlFor="idade">
          <input
            id="idade"
            type="number"
            min={18}
            max={120}
            required
            value={dados.idade}
            onChange={(e) => atualizarCampo('idade', Number(e.target.value))}
            className={inputClasses}
          />
        </Campo>

        <Campo label="Situação de moradia" htmlFor="situacaoMoradia">
          <select
            id="situacaoMoradia"
            value={dados.situacaoMoradia}
            onChange={(e) => atualizarCampo('situacaoMoradia', e.target.value as SituacaoMoradia)}
            className={inputClasses}
          >
            {Object.entries(situacaoMoradiaLabels).map(([valor, label]) => (
              <option key={valor} value={valor}>
                {label}
              </option>
            ))}
          </select>
        </Campo>

        <Campo label="Salário anual (R$)" htmlFor="salarioAnual">
          <input
            id="salarioAnual"
            type="number"
            min={0}
            step="0.01"
            required
            value={dados.salarioAnual}
            onChange={(e) => atualizarCampo('salarioAnual', Number(e.target.value))}
            className={inputClasses}
          />
        </Campo>

        <Campo label="Prazo (meses)" htmlFor="prazoMeses">
          <input
            id="prazoMeses"
            type="number"
            min={1}
            max={360}
            required
            value={dados.prazoMeses}
            onChange={(e) => atualizarCampo('prazoMeses', Number(e.target.value))}
            className={inputClasses}
          />
        </Campo>

        <Campo label="Saldo em conta corrente (R$)" htmlFor="saldoContaCorrente">
          <input
            id="saldoContaCorrente"
            type="number"
            min={0}
            step="0.01"
            required
            value={dados.saldoContaCorrente}
            onChange={(e) => atualizarCampo('saldoContaCorrente', Number(e.target.value))}
            className={inputClasses}
          />
        </Campo>

        <Campo label="Saldo em poupança (R$)" htmlFor="saldoContaPoupanca">
          <input
            id="saldoContaPoupanca"
            type="number"
            min={0}
            step="0.01"
            required
            value={dados.saldoContaPoupanca}
            onChange={(e) => atualizarCampo('saldoContaPoupanca', Number(e.target.value))}
            className={inputClasses}
          />
        </Campo>

        <Campo label="Valor do empréstimo (R$)" htmlFor="valorEmprestimo">
          <input
            id="valorEmprestimo"
            type="number"
            min={0.01}
            step="0.01"
            required
            value={dados.valorEmprestimo}
            onChange={(e) => atualizarCampo('valorEmprestimo', Number(e.target.value))}
            className={inputClasses}
          />
        </Campo>

        <div className="flex items-end sm:col-span-2">
          <button
            type="submit"
            disabled={carregando}
            className="w-full rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60 sm:w-auto"
          >
            {carregando ? 'Avaliando...' : 'Avaliar crédito'}
          </button>
        </div>
      </form>

      {erro && <p className="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{erro}</p>}

      {resultado && (
        <div
          className={`rounded-lg border p-6 ${
            resultado.resultado === 'APROVADO' ? 'border-green-200 bg-green-50' : 'border-red-200 bg-red-50'
          }`}
        >
          <p className={`text-lg font-semibold ${resultado.resultado === 'APROVADO' ? 'text-green-700' : 'text-red-700'}`}>
            {resultado.resultado === 'APROVADO' ? 'Aprovado' : 'Reprovado'}
          </p>
          <dl className="mt-3 grid grid-cols-2 gap-2 text-sm text-slate-700 sm:grid-cols-4">
            <div>
              <dt className="text-slate-500">Probabilidade de risco</dt>
              <dd>{(resultado.probabilidadeRisco * 100).toFixed(2)}%</dd>
            </div>
            <div>
              <dt className="text-slate-500">Threshold utilizado</dt>
              <dd>{(resultado.thresholdUtilizado * 100).toFixed(2)}%</dd>
            </div>
            <div>
              <dt className="text-slate-500">Versão do modelo</dt>
              <dd>{resultado.versaoModelo}</dd>
            </div>
            <div>
              <dt className="text-slate-500">ID da análise</dt>
              <dd>#{resultado.id}</dd>
            </div>
          </dl>
        </div>
      )}
    </div>
  )
}

const inputClasses = 'w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none'

function Campo({ label, htmlFor, children }: { label: string; htmlFor: string; children: React.ReactNode }) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-1 block text-sm font-medium text-slate-700">
        {label}
      </label>
      {children}
    </div>
  )
}
