import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { login as loginRequest } from '../api/auth'
import { mensagemDeErro } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function LoginPage() {
  const { token, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  if (token) {
    const destino = (location.state as { from?: string } | null)?.from ?? '/nova-analise'
    return <Navigate to={destino} replace />
  }

  async function aoEnviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setCarregando(true)
    try {
      const resposta = await loginRequest({ email, senha })
      login(resposta.token)
      navigate('/nova-analise', { replace: true })
    } catch (error) {
      setErro(mensagemDeErro(error))
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="mb-6 text-center text-xl font-semibold text-slate-900">Análise de Crédito</h1>
        <form onSubmit={aoEnviar} className="space-y-4">
          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-700">
              Email
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
            />
          </div>
          <div>
            <label htmlFor="senha" className="mb-1 block text-sm font-medium text-slate-700">
              Senha
            </label>
            <input
              id="senha"
              type="password"
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none"
            />
          </div>

          {erro && <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{erro}</p>}

          <button
            type="submit"
            disabled={carregando}
            className="w-full rounded-md bg-indigo-600 px-3 py-2 text-sm font-medium text-white hover:bg-indigo-500 disabled:opacity-60"
          >
            {carregando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}
