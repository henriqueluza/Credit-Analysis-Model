import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'

interface Usuario {
  email: string
  role: string
}

interface AuthContextValue {
  usuario: Usuario | null
  token: string | null
  login: (token: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function decodificarToken(token: string): Usuario | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1])) as { sub: string; role: string }
    return { email: payload.sub, role: payload.role }
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  const [usuario, setUsuario] = useState<Usuario | null>(() => {
    const armazenado = localStorage.getItem('token')
    return armazenado ? decodificarToken(armazenado) : null
  })

  useEffect(() => {
    function aoFicarNaoAutenticado() {
      setToken(null)
      setUsuario(null)
    }
    window.addEventListener('unauthorized', aoFicarNaoAutenticado)
    return () => window.removeEventListener('unauthorized', aoFicarNaoAutenticado)
  }, [])

  function login(novoToken: string) {
    localStorage.setItem('token', novoToken)
    setToken(novoToken)
    setUsuario(decodificarToken(novoToken))
  }

  function logout() {
    localStorage.removeItem('token')
    setToken(null)
    setUsuario(null)
  }

  return <AuthContext.Provider value={{ usuario, token, login, logout }}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider')
  }
  return context
}
