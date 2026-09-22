export const CATEGORY_LABELS = {
  SMARTPHONE: 'Смартфони',
  LAPTOP: 'Ноутбуки',
  TABLET: 'Планшети',
  CAMERA: 'Камери',
  AUDIO: 'Аудіо',
  GAMING: 'Ігри',
  ACCESSORY: 'Аксесуари',
  OTHER: 'Інше',
}

export const CONDITION_LABELS = {
  NEW: 'Нова',
  EXCELLENT: 'Відмінний',
  GOOD: 'Добрий',
  FAIR: 'З помітним зносом',
  NEEDS_REPAIR: 'Потребує ремонту',
}

export const DEVICE_STATUS_LABELS = {
  AVAILABLE: 'Доступна',
  RESERVED: 'У домовленості',
  EXCHANGED: 'Обміняна',
}

export const EXCHANGE_STATUS_LABELS = {
  PENDING: 'Очікує',
  ACCEPTED: 'Погоджено',
  ISSUED: 'Передано',
  RETURNED: 'Завершено',
  DECLINED: 'Відхилено',
  CANCELED: 'Скасовано',
}

export const CATEGORY_OPTIONS = Object.entries(CATEGORY_LABELS).map(([value, label]) => ({ value, label }))
export const CONDITION_OPTIONS = Object.entries(CONDITION_LABELS).map(([value, label]) => ({ value, label }))

export function compactDeviceName(device) {
  return [device?.brand, device?.model].filter(Boolean).join(' ') || device?.title || 'Техніка'
}
