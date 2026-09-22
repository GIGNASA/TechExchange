import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { LockKeyhole, Mail, Repeat2 } from 'lucide-react'
import { loginUser } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: 'dev1@techexchange.local', password: 'password123' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const auth = await loginUser(form)
      login(auth)
      navigate('/dashboard')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="auth-layout">
      <div className="auth-panel">
        <div>
          <p className="eyebrow">Платформа обміну технікою</p>
          <h1>Увійдіть до TechExchange</h1>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Email
            <span className="input-with-icon">
              <Mail size={18} />
              <input
                value={form.email}
                onChange={(event) => setForm({ ...form, email: event.target.value })}
                type="email"
                required
              />
            </span>
          </label>
          <label>
            Пароль
            <span className="input-with-icon">
              <LockKeyhole size={18} />
              <input
                value={form.password}
                onChange={(event) => setForm({ ...form, password: event.target.value })}
                type="password"
                required
              />
            </span>
          </label>
          {error && <div className="form-error">{error}</div>}
          <button className="primary-button" disabled={loading} type="submit">
            {loading ? 'Вхід...' : 'Увійти'}
          </button>
        </form>

        <p className="auth-switch">
          Немає акаунта? <Link to="/register">Створити профіль</Link>
        </p>
      </div>

      <div className="auth-art exchange-art" aria-hidden="true">
        <div className="device-stage">
          <div className="device-laptop">
            <span />
          </div>
          <div className="swap-core">
            <Repeat2 size={42} />
          </div>
          <div className="device-phone" />
        </div>
        <div className="floating-note">
          <span>6 оголошень</span>
          <strong>24/7</strong>
          <small>домовленості в одному місці</small>
        </div>
      </div>
    </section>
  )
}
