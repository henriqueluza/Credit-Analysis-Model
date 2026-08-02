import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { NovaAnalisePage } from './pages/NovaAnalisePage'
import { HistoricoPage } from './pages/HistoricoPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/nova-analise" replace />} />
          <Route path="/nova-analise" element={<NovaAnalisePage />} />
          <Route path="/historico" element={<HistoricoPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
