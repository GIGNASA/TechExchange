import { useEffect, useState } from 'react'
import { Mail, Star, UserRound } from 'lucide-react'
import { updateCurrentUser } from '../api/auth'
import { getErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function Profile() {
  const { user, setUserProfile } = useAuth()
  const [fullName, setFullName] = useState(user?.fullName || '')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    setFullName(user?.fullName || '')
  }, [user?.fullName])

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setNotice('')

    try {
      const updatedProfile = await updateCurrentUser({ fullName })
      setUserProfile(updatedProfile)
      setNotice('Профіль успішно оновлено.')
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="profile-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Кабінет користувача</p>
          <h1>Редагування профілю</h1>
        </div>
      </div>

      <div className="details-grid">
        <aside className="details-panel profile-summary">
          <h2>Поточні дані</h2>
          <div className="profile-meta">
            <span>
              <Mail size={14} />
              {user?.email || '—'}
            </span>
            <span>
              <Star size={14} />
              {user?.averageRating?.toFixed(2) || '0.00'} ({user?.reviewsCount || 0} відгуків)
            </span>
          </div>
        </aside>

        <section className="details-panel exchange-panel">
          <h2>Змінити ім&apos;я</h2>
          <form className="auth-form profile-form" onSubmit={handleSubmit}>
            <label>
              Ім&apos;я
              <span className="input-with-icon">
                <UserRound size={18} />
                <input
                  value={fullName}
                  onChange={(event) => setFullName(event.target.value)}
                  maxLength={255}
                  required
                />
              </span>
            </label>
            <label>
              Email
              <span className="input-with-icon">
                <Mail size={18} />
                <input value={user?.email || ''} disabled readOnly />
              </span>
            </label>

            {error && <div className="form-error">{error}</div>}
            {notice && <div className="notice-box">{notice}</div>}

            <div className="form-actions">
              <button
                className="ghost-button"
                onClick={() => setFullName(user?.fullName || '')}
                type="button"
              >
                Скасувати
              </button>
              <button className="primary-button" disabled={loading} type="submit">
                {loading ? 'Збереження...' : 'Зберегти'}
              </button>
            </div>
          </form>
        </section>
      </div>
    </section>
  )
}
