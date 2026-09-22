import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function PrivateRoute({ children }) {
  const { authReady, isAuthenticated } = useAuth()

  if (!authReady) {
    return <div className="page-loader">TechExchange</div>
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />
}
