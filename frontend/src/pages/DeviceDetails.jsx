import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Cpu, MapPin, Repeat2, Send } from 'lucide-react'
import { fetchDevice, fetchMyDevices } from '../api/devices'
import { createExchangeRequest } from '../api/exchangeRequests'
import { getErrorMessage } from '../api/client'
import {
  CATEGORY_LABELS,
  CONDITION_LABELS,
  DEVICE_STATUS_LABELS,
  compactDeviceName,
} from '../utils/labels'

export default function DeviceDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [device, setDevice] = useState(null)
  const [myDevices, setMyDevices] = useState([])
  const [form, setForm] = useState({ offeredDeviceId: '', message: '' })
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  useEffect(() => {
    let ignore = false

    async function loadDevice() {
      setLoading(true)
      setError('')
      try {
        const [deviceData, ownDevices] = await Promise.all([fetchDevice(id), fetchMyDevices()])
        if (!ignore) {
          setDevice(deviceData)
          setMyDevices(ownDevices.filter((item) => String(item.id) !== String(id) && item.status === 'AVAILABLE'))
        }
      } catch (err) {
        if (!ignore) {
          setError(getErrorMessage(err))
        }
      } finally {
        if (!ignore) {
          setLoading(false)
        }
      }
    }

    loadDevice()
    return () => {
      ignore = true
    }
  }, [id])

  async function handleExchangeSubmit(event) {
    event.preventDefault()
    setSending(true)
    setError('')
    setNotice('')
    try {
      await createExchangeRequest({
        targetDeviceId: Number(id),
        offeredDeviceId: form.offeredDeviceId ? Number(form.offeredDeviceId) : null,
        message: form.message,
      })
      setNotice('Пропозицію відправлено. Вона зʼявиться на сторінці домовленостей.')
      setForm({ offeredDeviceId: '', message: '' })
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSending(false)
    }
  }

  if (loading) {
    return <div className="page-loader">Завантаження оголошення...</div>
  }

  if (error && !device) {
    return (
      <section className="details-page">
        <Link className="back-link" to="/dashboard">
          <ArrowLeft size={16} /> До каталогу
        </Link>
        <div className="form-error wide">{error}</div>
      </section>
    )
  }

  if (!device) {
    return null
  }

  return (
    <section className="details-page">
      <button className="back-link" onClick={() => navigate('/dashboard')} type="button">
        <ArrowLeft size={16} /> До каталогу
      </button>

      <article className="details-hero">
        <div className="details-photo">
          {device.imageUrl ? <img alt={device.title} src={device.imageUrl} /> : <Cpu size={72} />}
        </div>

        <div className="details-content">
          <span className={`status-badge ${device.status.toLowerCase()}`}>
            {DEVICE_STATUS_LABELS[device.status]}
          </span>
          <h1>{device.title}</h1>
          <p className="details-author">{compactDeviceName(device)}</p>

          <div className="details-meta">
            <span>{CATEGORY_LABELS[device.category]}</span>
            <span>{CONDITION_LABELS[device.condition]}</span>
            <span><MapPin size={14} /> {device.city || 'Місто не вказано'}</span>
          </div>

          <section className="exchange-summary">
            <div>
              <span>Власник</span>
              <strong>{device.ownerName}</strong>
            </div>
            <div>
              <span>Бажаний обмін</span>
              <strong>{device.desiredExchange || 'Відкритий до пропозицій'}</strong>
            </div>
          </section>

          <p className="details-description">
            {device.description || 'Власник ще не додав детальний опис пристрою.'}
          </p>
        </div>
      </article>

      <section className="details-grid">
        <article className="details-panel">
          <h2>Характеристики</h2>
          <div className="spec-list">
            <span>Бренд</span><strong>{device.brand || 'Не вказано'}</strong>
            <span>Модель</span><strong>{device.model || 'Не вказано'}</strong>
            <span>Категорія</span><strong>{CATEGORY_LABELS[device.category]}</strong>
            <span>Стан</span><strong>{CONDITION_LABELS[device.condition]}</strong>
          </div>
        </article>

        <article className="details-panel exchange-panel">
          <h2>Запропонувати обмін</h2>
          {device.ownDevice ? (
            <p>Це ваше оголошення. Пропозиції від інших користувачів будуть у розділі домовленостей.</p>
          ) : (
            <form className="exchange-form" onSubmit={handleExchangeSubmit}>
              <label>
                Ваша техніка
                <select
                  value={form.offeredDeviceId}
                  onChange={(event) => setForm({ ...form, offeredDeviceId: event.target.value })}
                >
                  <option value="">Без конкретного пристрою</option>
                  {myDevices.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.title}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Повідомлення
                <textarea
                  rows="4"
                  value={form.message}
                  onChange={(event) => setForm({ ...form, message: event.target.value })}
                  placeholder="Опишіть вашу пропозицію або умови зустрічі"
                />
              </label>
              {error && <div className="form-error">{error}</div>}
              {notice && <div className="notice-box">{notice}</div>}
              <button className="primary-button icon-label" disabled={sending} type="submit">
                {sending ? <Repeat2 size={18} /> : <Send size={18} />}
                <span>{sending ? 'Відправлення...' : 'Відправити пропозицію'}</span>
              </button>
            </form>
          )}
        </article>
      </section>
    </section>
  )
}
