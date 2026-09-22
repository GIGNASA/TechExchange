import { Link, NavLink, useNavigate } from 'react-router-dom'
import { LogOut, Repeat2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="topbar">
      <NavLink to="/" className="brand">
        <span className="brand-mark">
          <Repeat2 size={18} />
        </span>
        <span>TechExchange</span>
      </NavLink>

      <nav className="topnav">
        {isAuthenticated ? (
          <>
            <NavLink to="/dashboard">Каталог</NavLink>
            <NavLink to="/requests">Пропозиції</NavLink>
            <Link className="user-pill user-pill-link" to="/profile">
              {user?.fullName || user?.email || 'Профіль'}
            </Link>
            <button className="ghost-button icon-label" onClick={handleLogout} type="button">
              <LogOut size={16} />
              <span>Вийти</span>
            </button>
          </>
        ) : (
          <>
            <NavLink to="/login">Вхід</NavLink>
            <NavLink to="/register" className="nav-cta">
              Реєстрація
            </NavLink>
          </>
        )}
      </nav>
    </header>
  )
}
