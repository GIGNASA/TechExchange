import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Cpu, Filter, MapPin, Pencil, Plus, Search, Trash2 } from 'lucide-react'
import {
  createDevice,
  deleteDevice,
  fetchDevices,
  fetchMarketplaceStats,
  updateDevice,
} from '../api/devices'
import { getErrorMessage } from '../api/client'
import EmptyState from '../components/EmptyState'
import {
  CATEGORY_LABELS,
  CATEGORY_OPTIONS,
  CONDITION_LABELS,
  CONDITION_OPTIONS,
  DEVICE_STATUS_LABELS,
  compactDeviceName,
} from '../utils/labels'

const EMPTY_FORM = {
  title: '',
  category: 'SMARTPHONE',
  condition: 'GOOD',
  status: 'AVAILABLE',
  brand: '',
  model: '',
  city: '',
  desiredExchange: '',
  description: '',
  imageUrl: '',
}

export default function Marketplace() {
  const [devices, setDevices] = useState([])
  const [stats, setStats] = useState(null)
  const [filters, setFilters] = useState({
    query: '',
    category: 'ALL',
    condition: 'ALL',
    city: '',
    mine: false,
  })
  const [editingDevice, setEditingDevice] = useState(null)
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadMarketplace()
  }, [])

  async function loadMarketplace() {
    setLoading(true)
    setError('')
    try {
      const [deviceData, statData] = await Promise.all([
        fetchDevices(buildParams(filters)),
        fetchMarketplaceStats(),
      ])
      setDevices(deviceData)
      setStats(statData)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function applyFilters(nextFilters) {
    setFilters(nextFilters)
    setLoading(true)
    setError('')
    try {
      const deviceData = await fetchDevices(buildParams(nextFilters))
      setDevices(deviceData)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  const cities = useMemo(() => {
    const values = devices.map((device) => device.city).filter(Boolean)
    return Array.from(new Set(values))
  }, [devices])

  function openCreateForm() {
    setEditingDevice(null)
    setIsFormOpen(true)
  }

  function openEditForm(device) {
    setEditingDevice(device)
    setIsFormOpen(true)
  }

  async function handleSave(payload) {
    if (editingDevice) {
      await updateDevice(editingDevice.id, payload)
    } else {
      await createDevice(payload)
    }
    setIsFormOpen(false)
    setEditingDevice(null)
    await loadMarketplace()
  }

  async function handleDelete(device) {
    const confirmed = window.confirm(`Видалити оголошення "${device.title}"?`)
    if (!confirmed) {
      return
    }
    await deleteDevice(device.id)
    await loadMarketplace()
  }

  return (
    <section className="marketplace-page">
      <div className="market-hero">
        <div className="hero-copy">
          <p className="eyebrow">TechExchange</p>
          <h1>Обмін технікою без зайвих чатів та загублених домовленостей</h1>
          <p>
            Публікуйте пристрої, фільтруйте пропозиції та ведіть переговори в одному робочому просторі.
          </p>
        </div>
        <button className="primary-button icon-label" onClick={openCreateForm} type="button">
          <Plus size={18} />
          <span>Додати техніку</span>
        </button>
      </div>

      {error && <div className="form-error wide">{error}</div>}

      <section className="market-workbench">
        <aside className="filter-panel">
          <div className="panel-heading">
            <Filter size={18} />
            <strong>Фільтри</strong>
          </div>

          <label className="search-box">
            <span>Пошук</span>
            <span className="input-with-icon">
              <Search size={18} />
              <input
                value={filters.query}
                onChange={(event) => applyFilters({ ...filters, query: event.target.value })}
                placeholder="iPhone, MacBook, Sony..."
              />
            </span>
          </label>

          <label>
            Категорія
            <select
              value={filters.category}
              onChange={(event) => applyFilters({ ...filters, category: event.target.value })}
            >
              <option value="ALL">Усі категорії</option>
              {CATEGORY_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Стан
            <select
              value={filters.condition}
              onChange={(event) => applyFilters({ ...filters, condition: event.target.value })}
            >
              <option value="ALL">Будь-який стан</option>
              {CONDITION_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Місто
            <input
              list="market-cities"
              value={filters.city}
              onChange={(event) => applyFilters({ ...filters, city: event.target.value })}
              placeholder="Київ"
            />
            <datalist id="market-cities">
              {cities.map((city) => (
                <option key={city} value={city} />
              ))}
            </datalist>
          </label>

          <button
            className={`filter-toggle ${filters.mine ? 'active' : ''}`}
            onClick={() => applyFilters({ ...filters, mine: !filters.mine })}
            type="button"
          >
            <Filter size={17} />
            <span>Показати мої оголошення</span>
          </button>
        </aside>

        <section className="catalog-panel">
          <div className="catalog-topline">
            <div>
              <p className="eyebrow">Каталог</p>
              <h2>Пристрої для обміну</h2>
            </div>
            <span>{devices.length} знайдено</span>
          </div>

          <section className="stats-grid compact-stats">
            <StatTile label="Оголошень" value={stats?.totalDevices || 0} tone="ink" />
            <StatTile label="Доступно" value={stats?.availableDevices || 0} tone="green" />
            <StatTile label="Мої пристрої" value={stats?.myDevices || 0} tone="blue" />
            <StatTile label="Пропозицій" value={stats?.activeRequests || 0} tone="coral" />
          </section>

          {loading ? (
            <div className="page-loader">Завантаження каталогу...</div>
          ) : devices.length ? (
            <section className="device-grid">
              {devices.map((device) => (
                <DeviceCard
                  device={device}
                  key={device.id}
                  onDelete={handleDelete}
                  onEdit={openEditForm}
                />
              ))}
            </section>
          ) : (
            <EmptyState
              title="Оголошень не знайдено"
              text="Змініть фільтри або додайте першу техніку для обміну."
              action={
                <button className="primary-button icon-label" onClick={openCreateForm} type="button">
                  <Plus size={18} />
                  <span>Додати техніку</span>
                </button>
              }
            />
          )}
        </section>
      </section>

      {isFormOpen && (
        <DeviceForm
          device={editingDevice}
          onClose={() => setIsFormOpen(false)}
          onSave={handleSave}
        />
      )}
    </section>
  )
}

function buildParams(filters) {
  return {
    query: filters.query || undefined,
    category: filters.category === 'ALL' ? undefined : filters.category,
    condition: filters.condition === 'ALL' ? undefined : filters.condition,
    city: filters.city || undefined,
    mine: filters.mine || undefined,
  }
}

function StatTile({ label, value, tone }) {
  return (
    <article className={`stat-tile ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function DeviceCard({ device, onDelete, onEdit }) {
  return (
    <article className="device-card">
      <Link className="device-photo" to={`/devices/${device.id}`}>
        {device.imageUrl ? (
          <img alt={device.title} src={device.imageUrl} />
        ) : (
          <Cpu size={46} />
        )}
        <span className={`status-badge ${device.status.toLowerCase()}`}>
          {DEVICE_STATUS_LABELS[device.status]}
        </span>
      </Link>

      <div className="device-content">
        <div className="device-title-row">
          <div>
            <p className="device-category">{CATEGORY_LABELS[device.category]}</p>
            <h2>{device.title}</h2>
            <p className="device-model">{compactDeviceName(device)}</p>
          </div>
          {device.ownDevice && (
            <div className="icon-actions">
              <button aria-label="Редагувати" onClick={() => onEdit(device)} type="button">
                <Pencil size={16} />
              </button>
              <button aria-label="Видалити" onClick={() => onDelete(device)} type="button">
                <Trash2 size={16} />
              </button>
            </div>
          )}
        </div>

        <div className="device-meta">
          <span>{CONDITION_LABELS[device.condition]}</span>
          <span><MapPin size={14} /> {device.city || 'Місто не вказано'}</span>
        </div>

        <p className="exchange-wish">{device.desiredExchange || 'Власник відкритий до пропозицій.'}</p>

        <div className="card-footer">
          <span>{device.ownerName}</span>
          <Link className="details-button" to={`/devices/${device.id}`}>
            Деталі
          </Link>
        </div>
      </div>
    </article>
  )
}

function DeviceForm({ device, onClose, onSave }) {
  const [form, setForm] = useState(() => ({ ...EMPTY_FORM, ...device }))
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      await onSave(form)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="modal-backdrop">
      <form className="device-form" onSubmit={handleSubmit}>
        <div className="form-head">
          <h2>{device ? 'Редагувати оголошення' : 'Нове оголошення'}</h2>
          <button aria-label="Закрити форму" onClick={onClose} type="button">
            Закрити
          </button>
        </div>

        <div className="form-grid">
          <label className="form-field-wide">
            Назва
            <input
              value={form.title}
              onChange={(event) => setForm({ ...form, title: event.target.value })}
              placeholder="iPhone 13 128GB"
              required
            />
          </label>

          <label>
            Категорія
            <select
              value={form.category}
              onChange={(event) => setForm({ ...form, category: event.target.value })}
            >
              {CATEGORY_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Стан
            <select
              value={form.condition}
              onChange={(event) => setForm({ ...form, condition: event.target.value })}
            >
              {CONDITION_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Бренд
            <input value={form.brand || ''} onChange={(event) => setForm({ ...form, brand: event.target.value })} />
          </label>

          <label>
            Модель
            <input value={form.model || ''} onChange={(event) => setForm({ ...form, model: event.target.value })} />
          </label>

          <label>
            Місто
            <input value={form.city || ''} onChange={(event) => setForm({ ...form, city: event.target.value })} />
          </label>

          <label>
            Статус
            <select
              value={form.status}
              onChange={(event) => setForm({ ...form, status: event.target.value })}
            >
              <option value="AVAILABLE">Доступна</option>
              <option value="RESERVED">У домовленості</option>
              <option value="EXCHANGED">Обміняна</option>
            </select>
          </label>

          <label className="form-field-wide">
            Що хочете отримати
            <input
              value={form.desiredExchange || ''}
              onChange={(event) => setForm({ ...form, desiredExchange: event.target.value })}
              placeholder="Планшет, смартфон, навушники..."
            />
          </label>

          <label className="form-field-wide">
            Фото URL
            <input
              value={form.imageUrl || ''}
              onChange={(event) => setForm({ ...form, imageUrl: event.target.value })}
              placeholder="https://..."
            />
          </label>

          <label className="form-field-wide">
            Опис
            <textarea
              rows="4"
              value={form.description || ''}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
            />
          </label>
        </div>

        {error && <div className="form-error">{error}</div>}

        <div className="form-actions">
          <button className="ghost-button" onClick={onClose} type="button">
            Скасувати
          </button>
          <button className="primary-button" disabled={saving} type="submit">
            {saving ? 'Збереження...' : 'Зберегти'}
          </button>
        </div>
      </form>
    </div>
  )
}
