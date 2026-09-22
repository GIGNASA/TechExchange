import { useEffect, useMemo, useState } from 'react'
import { Check, MessageSquare, Package, RotateCcw, Send, X } from 'lucide-react'
import { fetchExchangeRequests, updateExchangeStatus } from '../api/exchangeRequests'
import { fetchMessages, sendMessage } from '../api/messages'
import { getErrorMessage } from '../api/client'
import EmptyState from '../components/EmptyState'
import { EXCHANGE_STATUS_LABELS, compactDeviceName } from '../utils/labels'

export default function Requests() {
  const [requests, setRequests] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [messages, setMessages] = useState([])
  const [messageText, setMessageText] = useState('')
  const [loading, setLoading] = useState(true)
  const [messageLoading, setMessageLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadRequests()
  }, [])

  useEffect(() => {
    if (selectedId) {
      loadMessages(selectedId)
    }
  }, [selectedId])

  const selectedRequest = useMemo(
    () => requests.find((request) => request.id === selectedId),
    [requests, selectedId],
  )

  async function loadRequests() {
    setLoading(true)
    setError('')
    try {
      const data = await fetchExchangeRequests()
      setRequests(data)
      setSelectedId((current) => current || data[0]?.id || null)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  async function loadMessages(requestId) {
    setMessageLoading(true)
    try {
      const data = await fetchMessages(requestId)
      setMessages(data)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setMessageLoading(false)
    }
  }

  async function handleStatus(id, status) {
    setError('')
    try {
      await updateExchangeStatus(id, status)
      await loadRequests()
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  async function handleMessageSubmit(event) {
    event.preventDefault()
    if (!messageText.trim() || !selectedId) {
      return
    }
    setError('')
    try {
      await sendMessage(selectedId, messageText.trim())
      setMessageText('')
      await loadMessages(selectedId)
    } catch (err) {
      setError(getErrorMessage(err))
    }
  }

  if (loading) {
    return <div className="page-loader">Завантаження пропозицій...</div>
  }

  return (
    <section className="requests-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Домовленості</p>
          <h1>Пропозиції обміну</h1>
        </div>
      </div>

      {error && <div className="form-error wide">{error}</div>}

      {!requests.length ? (
        <EmptyState
          title="Пропозицій поки немає"
          text="Коли ви надішлете або отримаєте пропозицію, вона зʼявиться тут разом із повідомленнями."
        />
      ) : (
        <section className="requests-layout">
          <div className="request-list">
            {requests.map((request) => (
              <button
                className={`request-item ${selectedId === request.id ? 'active' : ''}`}
                key={request.id}
                onClick={() => setSelectedId(request.id)}
                type="button"
              >
                <span className={`request-status ${request.status.toLowerCase()}`}>
                  {EXCHANGE_STATUS_LABELS[request.status]}
                </span>
                <strong>{request.targetDevice.title}</strong>
                <small>
                  {request.direction === 'INCOMING' ? 'Вхідна' : 'Вихідна'} пропозиція
                </small>
              </button>
            ))}
          </div>

          <div className="request-detail">
            {selectedRequest && (
              <>
                <div className="request-head">
                  <div>
                    <span className={`request-status ${selectedRequest.status.toLowerCase()}`}>
                      {EXCHANGE_STATUS_LABELS[selectedRequest.status]}
                    </span>
                    <h2>{selectedRequest.targetDevice.title}</h2>
                    <p>
                      {selectedRequest.direction === 'INCOMING'
                        ? `${selectedRequest.requesterName} хоче домовитись про обмін`
                        : `Пропозицію надіслано ${selectedRequest.ownerName}`}
                    </p>
                  </div>
                  <div className="request-actions">
                    {actionButtons(selectedRequest).map((action) => (
                      <button key={action.status} onClick={() => handleStatus(selectedRequest.id, action.status)} type="button">
                        {action.icon}
                        {action.label}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="swap-preview">
                  <DeviceMini device={selectedRequest.offeredDevice} fallback="Відкрита пропозиція" />
                  <div className="swap-mark">⇄</div>
                  <DeviceMini device={selectedRequest.targetDevice} />
                </div>

                {selectedRequest.message && (
                  <div className="initial-message">
                    <MessageSquare size={17} />
                    <p>{selectedRequest.message}</p>
                  </div>
                )}

                <section className="chat-panel">
                  <h3>Повідомлення</h3>
                  {messageLoading ? (
                    <div className="page-loader compact">Завантаження...</div>
                  ) : (
                    <div className="message-list">
                      {messages.length ? (
                        messages.map((message) => (
                          <article className={`message-bubble ${message.mine ? 'mine' : ''}`} key={message.id}>
                            <span>{message.senderName}</span>
                            <p>{message.text}</p>
                          </article>
                        ))
                      ) : (
                        <p className="muted-text">Напишіть перше повідомлення по цій домовленості.</p>
                      )}
                    </div>
                  )}

                  <form className="message-form" onSubmit={handleMessageSubmit}>
                    <input
                      value={messageText}
                      onChange={(event) => setMessageText(event.target.value)}
                      placeholder="Написати повідомлення..."
                    />
                    <button aria-label="Надіслати" type="submit">
                      <Send size={18} />
                    </button>
                  </form>
                </section>
              </>
            )}
          </div>
        </section>
      )}
    </section>
  )
}

function actionButtons(request) {
  if (request.direction === 'INCOMING') {
    if (request.status === 'PENDING') {
      return [
        { status: 'ACCEPTED', label: 'Прийняти', icon: <Check size={16} /> },
        { status: 'DECLINED', label: 'Відхилити', icon: <X size={16} /> },
      ]
    }
    if (request.status === 'ACCEPTED') {
      return [{ status: 'ISSUED', label: 'Позначити переданою', icon: <Package size={16} /> }]
    }
    if (request.status === 'ISSUED') {
      return [{ status: 'RETURNED', label: 'Завершити обмін', icon: <RotateCcw size={16} /> }]
    }
  }

  if (request.direction === 'OUTGOING') {
    if (request.status === 'PENDING' || request.status === 'ACCEPTED') {
      return [{ status: 'CANCELED', label: 'Скасувати', icon: <X size={16} /> }]
    }
    if (request.status === 'ISSUED') {
      return [{ status: 'RETURNED', label: 'Підтвердити завершення', icon: <RotateCcw size={16} /> }]
    }
  }

  return []
}

function DeviceMini({ device, fallback }) {
  if (!device) {
    return (
      <article className="device-mini empty">
        <strong>{fallback}</strong>
        <span>Без конкретного пристрою</span>
      </article>
    )
  }

  return (
    <article className="device-mini">
      {device.imageUrl && <img alt={device.title} src={device.imageUrl} />}
      <div>
        <strong>{device.title}</strong>
        <span>{compactDeviceName(device)}</span>
      </div>
    </article>
  )
}
