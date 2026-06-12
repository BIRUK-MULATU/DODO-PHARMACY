export const API_BASE_URL =
  import.meta.env.VITE_API_URL || 'http://localhost:8083'

export const GOOGLE_CLIENT_ID =
  import.meta.env.VITE_GOOGLE_CLIENT_ID ||
  '391385140157-h28t759ft80a25p4vgktdm6qo37q2p94.apps.googleusercontent.com'

export const DEFAULT_CATEGORIES = [
  'Antibiotics',
  'Analgesics',
  'Antihistamines',
  'Vitamins',
  'Antidiabetics',
  'Cardiovascular',
  'Dermatology',
  'Respiratory'
]

export const getCategories = () => {
  try {
    const saved = localStorage.getItem('drug_categories')
    return saved ? JSON.parse(saved) : DEFAULT_CATEGORIES
  } catch {
    return DEFAULT_CATEGORIES
  }
}

export const saveCategories = (categories) => {
  localStorage.setItem(
    'drug_categories',
    JSON.stringify(categories)
  )
}