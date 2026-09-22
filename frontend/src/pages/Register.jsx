import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { Mail, UserRound, LockKeyhole, Sparkles } from 'lucide-react'
import { registerUser } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '' })
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
      const auth = await registerUser(form)
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
          <p className="eyebrow">Новий учасник</p>
          <h1>Створіть профіль для обміну</h1>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Ім'я
            <span className="input-with-icon">
              <UserRound size={18} />
              <input
                value={form.fullName}
                onChange={(event) => setForm({ ...form, fullName: event.target.value })}
                required
              />
            </span>
          </label>
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
                minLength={6}
                required
              />
            </span>
          </label>
          {error && <div className="form-error">{error}</div>}
          <button className="primary-button" disabled={loading} type="submit">
            {loading ? 'Створення...' : 'Зареєструватися'}
          </button>
        </form>

        <p className="auth-switch">
          Вже є профіль? <Link to="/login">Увійти</Link>
        </p>
      </div>

      <div className="auth-art register-art" aria-hidden="true">
        <div className="trade-stack">
          <span><Sparkles size={20} /> Смартфон</span>
          <span>Ноутбук</span>
          <span>Камера</span>
        </div>
      </div>
    </section>
  )
}
