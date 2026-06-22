import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import { drugApi } from '../../api/drugApi'
import { supplierApi } from '../../api/supplierApi'
import { useAuth } from '../../context/AuthContext'
import CategoriesManager from '../../components/common/CategoriesManager'
import { getCategories } from '../../utils/constants'

const Badge = ({ children, color }) => {
  const colors = {
    red: 'bg-red-100 text-red-700',
    yellow: 'bg-yellow-100 text-yellow-700',
    green: 'bg-green-100 text-green-700',
    blue: 'bg-blue-100 text-blue-700',
  }
  return (
    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${colors[color]}`}>
      {children}
    </span>
  )
}

export default function DrugsPage() {
  const { hasRole } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const lowStockFilter = searchParams.get('lowStock') === 'true'
  const expiringSoonFilter = searchParams.get('expiringSoon') === 'true'

  const [drugs, setDrugs] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [categories, setCategories] = useState(getCategories())
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [showModal, setShowModal] = useState(false)
  const [showStockModal, setShowStockModal] = useState(false)
  const [showCategoriesManager, setShowCategoriesManager] = useState(false)
  const [editDrug, setEditDrug] = useState(null)
  const [selectedDrug, setSelectedDrug] = useState(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const [form, setForm] = useState({
    name: '', sku: '', category: '', price: '',
    stockQty: '', expiryDate: '', supplierId: ''
  })

  const [stockForm, setStockForm] = useState({
    quantity: '', type: 'ADD', reason: ''
  })

  const fetchDrugs = async () => {
    setLoading(true)
    try {
      const params = { page, size: 50 }
      if (category) params.category = category
      const res = await drugApi.search(params)
      setDrugs(res.data.content)
      setTotalPages(res.data.totalPages)
    } catch { setError('Failed to load drugs') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetchDrugs() }, [page, category])

  useEffect(() => {
    supplierApi.getAll()
      .then(res => setSuppliers(res.data))
      .catch(() => {})
  }, [])

  const openCreate = () => {
    setEditDrug(null)
    setForm({ name: '', sku: '', category: '', price: '', stockQty: '', expiryDate: '', supplierId: '' })
    setShowModal(true)
  }

  const openEdit = (drug) => {
    setEditDrug(drug)
    setForm({
      name: drug.name, sku: drug.sku, category: drug.category,
      price: drug.price, stockQty: drug.stockQty,
      expiryDate: drug.expiryDate, supplierId: drug.supplierId ? String(drug.supplierId) : ''
    })
    setShowModal(true)
  }

  const openStock = (drug) => {
    setSelectedDrug(drug)
    setStockForm({ quantity: '', type: 'ADD', reason: '' })
    setShowStockModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const data = {
        ...form,
        price: parseFloat(form.price),
        stockQty: parseInt(form.stockQty),
        supplierId: form.supplierId ? parseInt(form.supplierId) : null
      }
      if (editDrug) {
        await drugApi.update(editDrug.id, data)
        setSuccess('Drug updated successfully')
      } else {
        await drugApi.create(data)
        setSuccess('Drug created successfully')
      }
      setShowModal(false)
      fetchDrugs()
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed')
    }
  }

  const handleStock = async (e) => {
    e.preventDefault()
    try {
      await drugApi.adjustStock(selectedDrug.id, {
        ...stockForm,
        quantity: parseInt(stockForm.quantity)
      })
      setSuccess('Stock adjusted successfully')
      setShowStockModal(false)
      fetchDrugs()
    } catch (err) {
      setError(err.response?.data?.message || 'Stock adjustment failed')
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this drug?')) return
    try {
      await drugApi.delete(id)
      setSuccess('Drug deleted')
      fetchDrugs()
    } catch { setError('Delete failed') }
  }

  const clearAlertFilter = () => {
    searchParams.delete('lowStock')
    searchParams.delete('expiringSoon')
    setSearchParams(searchParams)
  }

  const filtered = drugs.filter(d => {
    const matchSearch =
      d.name.toLowerCase().includes(search.toLowerCase()) ||
      d.sku.toLowerCase().includes(search.toLowerCase())
    const matchLowStock = lowStockFilter ? d.lowStock : true
    const matchExpiringSoon = expiringSoonFilter ? d.expiringSoon : true
    return matchSearch && matchLowStock && matchExpiringSoon
  })

  return (
    <DashboardLayout title="Drug Management">
      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm flex justify-between">
          <span>{error}</span><button onClick={() => setError('')}>✕</button>
        </div>
      )}
      {success && (
        <div className="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg text-sm flex justify-between">
          <span>{success}</span><button onClick={() => setSuccess('')}>✕</button>
        </div>
      )}

      {/* Active alert filter banner */}
      {(lowStockFilter || expiringSoonFilter) && (
        <div className="mb-4 bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded-lg text-sm flex justify-between items-center">
          <span>
            Showing only <strong>{lowStockFilter ? 'low stock' : 'expiring soon'}</strong> drugs
          </span>
          <button onClick={clearAlertFilter} className="text-blue-700 font-medium hover:underline">
            Clear filter ✕
          </button>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900">Drugs</h2>
          <p className="text-sm text-gray-500">Manage drug inventory</p>
        </div>
        <div className="flex items-center gap-2">
          {hasRole('ADMIN', 'PHARMACIST') && (
            <>
              <button onClick={() => {
                setShowCategoriesManager(true)
                setCategories(getCategories())
              }}
                className="flex items-center gap-2 border border-gray-300 text-gray-700 hover:bg-gray-50 px-4 py-2 rounded-lg text-sm font-medium">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
                </svg>
                Categories
              </button>
              <button onClick={openCreate}
                className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4"/>
                </svg>
                Add Drug
              </button>
            </>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <input
          type="text" placeholder="Search by name or SKU..."
          value={search} onChange={(e) => setSearch(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(0) }}
          className="px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
          <option value="">All Categories</option>
          {categories.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>

      {/* Category Pills */}
      <div className="flex flex-wrap gap-2 mb-6">
        <button onClick={() => setCategory('')}
          className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
            category === '' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}>
          All
        </button>
        {categories.slice(0, 8).map(c => (
          <button key={c} onClick={() => setCategory(c)}
            className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${
              category === c ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}>
            {c}
          </button>
        ))}
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['Name', 'SKU', 'Category', 'Price', 'Stock', 'Expiry', 'Supplier', 'Status', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {loading ? (
                <tr><td colSpan={9} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={9} className="px-4 py-8 text-center text-gray-400">No drugs found</td></tr>
              ) : filtered.map((drug) => (
                <tr key={drug.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-900">{drug.name}</td>
                  <td className="px-4 py-3 text-gray-500 font-mono text-xs">{drug.sku}</td>
                  <td className="px-4 py-3"><Badge color="blue">{drug.category}</Badge></td>
                  <td className="px-4 py-3 text-gray-700">ETB {parseFloat(drug.price).toFixed(2)}</td>
                  <td className="px-4 py-3">
                    <span className={drug.lowStock ? 'text-red-600 font-semibold' : 'text-gray-700'}>
                      {drug.stockQty}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-500">{drug.expiryDate}</td>
                  <td className="px-4 py-3 text-gray-500">{drug.supplierName || '—'}</td>
                  <td className="px-4 py-3">
                    {drug.lowStock ? <Badge color="red">Low Stock</Badge>
                      : drug.expiringSoon ? <Badge color="yellow">Expiring</Badge>
                      : <Badge color="green">OK</Badge>}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      {hasRole('ADMIN', 'PHARMACIST') && (
                        <>
                          <button onClick={() => openStock(drug)}
                            className="text-xs px-2 py-1 bg-blue-50 text-blue-600 rounded hover:bg-blue-100">
                            Stock
                          </button>
                          <button onClick={() => openEdit(drug)}
                            className="text-xs px-2 py-1 bg-gray-50 text-gray-600 rounded hover:bg-gray-100">
                            Edit
                          </button>
                        </>
                      )}
                      {hasRole('ADMIN') && (
                        <button onClick={() => handleDelete(drug.id)}
                          className="text-xs px-2 py-1 bg-red-50 text-red-600 rounded hover:bg-red-100">
                          Delete
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="px-4 py-3 border-t border-gray-100 flex items-center justify-between">
            <p className="text-sm text-gray-500">Page {page + 1} of {totalPages}</p>
            <div className="flex gap-2">
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50">
                Previous
              </button>
              <button disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}
                className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50">
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Categories Manager */}
      {showCategoriesManager && (
        <CategoriesManager
          onClose={() => {
            setShowCategoriesManager(false)
            setCategories(getCategories())
          }}
        />
      )}

      {/* Create/Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 sticky top-0 bg-white">
              <h3 className="font-semibold text-gray-900">{editDrug ? 'Edit Drug' : 'Add New Drug'}</h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {error && <p className="text-red-600 text-sm">{error}</p>}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Drug Name</label>
                  <input required value={form.name}
                    onChange={e => setForm({...form, name: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Amoxicillin 500mg"/>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">SKU</label>
                  <input required value={form.sku}
                    onChange={e => setForm({...form, sku: e.target.value})}
                    disabled={!!editDrug}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-50"
                    placeholder="AMX-500"/>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Category</label>
                  <select required value={form.category}
                    onChange={e => setForm({...form, category: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">Select category</option>
                    {categories.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Price (ETB)</label>
                  <input required type="number" step="0.01" min="0" value={form.price}
                    onChange={e => setForm({...form, price: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="25.00"/>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Stock Quantity</label>
                  <input required type="number" min="0" value={form.stockQty}
                    onChange={e => setForm({...form, stockQty: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="100"/>
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">Expiry Date</label>
                  <input required type="date" value={form.expiryDate}
                    onChange={e => setForm({...form, expiryDate: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"/>
                  <p className="text-xs text-gray-400 mt-1">You can enter past, current, or future dates.</p>
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Supplier</label>
                <select required value={form.supplierId}
                  onChange={e => setForm({...form, supplierId: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">Select supplier</option>
                  {suppliers.map(s => <option key={s.id} value={String(s.id)}>{s.companyName}</option>)}
                </select>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                  Cancel
                </button>
                <button type="submit"
                  className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium">
                  {editDrug ? 'Update Drug' : 'Create Drug'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Stock Modal */}
      {showStockModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-900">Adjust Stock</h3>
              <button onClick={() => setShowStockModal(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <form onSubmit={handleStock} className="p-6 space-y-4">
              <p className="text-sm text-gray-600">
                Current stock for <strong>{selectedDrug?.name}</strong>: <strong>{selectedDrug?.stockQty}</strong>
              </p>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Adjustment Type</label>
                <select value={stockForm.type}
                  onChange={e => setStockForm({...stockForm, type: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="ADD">Add Stock</option>
                  <option value="SUBTRACT">Subtract Stock</option>
                  <option value="SET">Set Stock</option>
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Quantity</label>
                <input required type="number" min="1" value={stockForm.quantity}
                  onChange={e => setStockForm({...stockForm, quantity: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="50"/>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Reason</label>
                <input value={stockForm.reason}
                  onChange={e => setStockForm({...stockForm, reason: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="New stock received"/>
              </div>
              <div className="flex gap-3">
                <button type="button" onClick={() => setShowStockModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">
                  Cancel
                </button>
                <button type="submit"
                  className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium">
                  Adjust
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}