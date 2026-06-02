import { useState, useEffect } from 'react'
import DashboardLayout from '../../components/layout/DashboardLayout'
import { purchaseOrderApi } from '../../api/purchaseOrderApi'
import { supplierApi } from '../../api/supplierApi'
import { useAuth } from '../../context/AuthContext'

const Badge = ({ children, color }) => {
  const colors = {
    gray: 'bg-gray-100 text-gray-700',
    blue: 'bg-blue-100 text-blue-700',
    green: 'bg-green-100 text-green-700',
    red: 'bg-red-100 text-red-700',
  }
  return <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${colors[color]}`}>{children}</span>
}

const statusColor = (status) => {
  if (status === 'DRAFT') return 'gray'
  if (status === 'SENT') return 'blue'
  if (status === 'DELIVERED') return 'green'
  return 'red'
}

export default function PurchaseOrdersPage() {
  const { user, hasRole } = useAuth()
  const [orders, setOrders] = useState([])
  const [suppliers, setSuppliers] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [showDetailModal, setShowDetailModal] = useState(false)
  const [showDeliverModal, setShowDeliverModal] = useState(false)
  const [selectedOrder, setSelectedOrder] = useState(null)

  const [form, setForm] = useState({
    supplierId: '', totalCost: '', deliveryDate: ''
  })

  const [deliverForm, setDeliverForm] = useState({
    deliveryDate: '', notes: ''
  })

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const res = await purchaseOrderApi.getAll({ page, size: 10 })
      setOrders(res.data.content)
      setTotalPages(res.data.totalPages)
    } catch { setError('Failed to load purchase orders') }
    finally { setLoading(false) }
  }

  useEffect(() => { fetchOrders() }, [page])

  useEffect(() => {
    supplierApi.getAll()
      .then(res => setSuppliers(res.data))
      .catch(() => {})
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await purchaseOrderApi.create({
        supplierId: parseInt(form.supplierId),
        orderedById: user?.id || 4,
        totalCost: parseFloat(form.totalCost),
        deliveryDate: form.deliveryDate || null
      })
      setSuccess('Purchase order created successfully')
      setShowModal(false)
      setForm({ supplierId: '', totalCost: '', deliveryDate: '' })
      fetchOrders()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create order')
    }
  }

  const handleUpdateStatus = async (id, status) => {
    try {
      await purchaseOrderApi.updateStatus(id, status)
      setSuccess(`Order status updated to ${status}`)
      fetchOrders()
      setShowDetailModal(false)
    } catch (err) {
      setError(err.response?.data?.message || 'Status update failed')
    }
  }

  const handleDeliver = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await purchaseOrderApi.deliver(selectedOrder.id, deliverForm)
      setSuccess('Order marked as delivered')
      setShowDeliverModal(false)
      setShowDetailModal(false)
      fetchOrders()
    } catch (err) {
      setError(err.response?.data?.message || 'Delivery update failed')
    }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this purchase order?')) return
    try {
      await purchaseOrderApi.delete(id)
      setSuccess('Purchase order deleted')
      fetchOrders()
    } catch { setError('Delete failed') }
  }

  const openDetail = (order) => {
    setSelectedOrder(order)
    setShowDetailModal(true)
  }

  return (
    <DashboardLayout title="Purchase Orders">
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

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900">Purchase Orders</h2>
          <p className="text-sm text-gray-500">Manage supplier purchase orders</p>
        </div>
        <button onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-medium">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4"/>
          </svg>
          New Order
        </button>
      </div>

      {/* Status Legend */}
      <div className="flex items-center gap-3 mb-6 flex-wrap">
        <span className="text-xs text-gray-500 font-medium">Status:</span>
        {['DRAFT', 'SENT', 'DELIVERED', 'CANCELLED'].map(s => (
          <Badge key={s} color={statusColor(s)}>{s}</Badge>
        ))}
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                {['#', 'Supplier', 'Ordered By', 'Status', 'Total Cost', 'Order Date', 'Delivery Date', 'Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {loading ? (
                <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
              ) : orders.length === 0 ? (
                <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">No purchase orders found</td></tr>
              ) : orders.map(order => (
                <tr key={order.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3 text-gray-400 text-xs">#{order.id}</td>
                  <td className="px-4 py-3 font-medium text-gray-900">{order.supplierName}</td>
                  <td className="px-4 py-3 text-gray-500">{order.orderedBy || '—'}</td>
                  <td className="px-4 py-3">
                    <Badge color={statusColor(order.status)}>{order.status}</Badge>
                  </td>
                  <td className="px-4 py-3 font-semibold text-gray-900">
                    ETB {parseFloat(order.totalCost).toFixed(2)}
                  </td>
                  <td className="px-4 py-3 text-gray-400 text-xs">
                    {new Date(order.orderDate).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-gray-500">
                    {order.deliveryDate || '—'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button onClick={() => openDetail(order)}
                        className="text-xs px-2 py-1 bg-blue-50 text-blue-600 rounded hover:bg-blue-100">
                        View
                      </button>
                      {hasRole('ADMIN') && order.status === 'DRAFT' && (
                        <button onClick={() => handleDelete(order.id)}
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
                className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50">Previous</button>
              <button disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)}
                className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-50">Next</button>
            </div>
          </div>
        )}
      </div>

      {/* Create Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-900">New Purchase Order</h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <form onSubmit={handleCreate} className="p-6 space-y-4">
              {error && <p className="text-red-600 text-sm bg-red-50 px-3 py-2 rounded-lg">{error}</p>}
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Supplier</label>
                <select required value={form.supplierId}
                  onChange={e => setForm({...form, supplierId: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">Select supplier</option>
                  {suppliers.map(s => (
                    <option key={s.id} value={s.id}>{s.companyName}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Total Cost (ETB)</label>
                <input required type="number" step="0.01" min="0"
                  value={form.totalCost}
                  onChange={e => setForm({...form, totalCost: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="5000.00"/>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Expected Delivery Date</label>
                <input type="date" value={form.deliveryDate}
                  onChange={e => setForm({...form, deliveryDate: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"/>
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">Cancel</button>
                <button type="submit"
                  className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium">Create Order</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Detail Modal */}
      {showDetailModal && selectedOrder && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-900">Purchase Order #{selectedOrder.id}</h3>
              <button onClick={() => setShowDetailModal(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <div className="p-6 space-y-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-xs text-gray-400">Supplier</p>
                  <p className="font-medium text-gray-900">{selectedOrder.supplierName}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Status</p>
                  <Badge color={statusColor(selectedOrder.status)}>{selectedOrder.status}</Badge>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Ordered By</p>
                  <p className="font-medium text-gray-900">{selectedOrder.orderedBy || '—'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Total Cost</p>
                  <p className="font-bold text-blue-600">ETB {parseFloat(selectedOrder.totalCost).toFixed(2)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Order Date</p>
                  <p className="font-medium text-gray-900">{new Date(selectedOrder.orderDate).toLocaleDateString()}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400">Delivery Date</p>
                  <p className="font-medium text-gray-900">{selectedOrder.deliveryDate || '—'}</p>
                </div>
              </div>

              {/* Status Actions */}
              {selectedOrder.status !== 'DELIVERED' && selectedOrder.status !== 'CANCELLED' && (
                <div className="border-t border-gray-100 pt-4">
                  <p className="text-xs font-semibold text-gray-500 uppercase mb-3">Update Status</p>
                  <div className="flex flex-wrap gap-2">
                    {selectedOrder.status === 'DRAFT' && (
                      <button onClick={() => handleUpdateStatus(selectedOrder.id, 'SENT')}
                        className="px-3 py-1.5 bg-blue-50 text-blue-600 rounded-lg text-xs font-medium hover:bg-blue-100">
                        Mark as Sent
                      </button>
                    )}
                    {(selectedOrder.status === 'DRAFT' || selectedOrder.status === 'SENT') && (
                      <>
                        <button onClick={() => {
                          setDeliverForm({ deliveryDate: '', notes: '' })
                          setShowDeliverModal(true)
                        }}
                          className="px-3 py-1.5 bg-green-50 text-green-600 rounded-lg text-xs font-medium hover:bg-green-100">
                          Mark as Delivered
                        </button>
                        <button onClick={() => handleUpdateStatus(selectedOrder.id, 'CANCELLED')}
                          className="px-3 py-1.5 bg-red-50 text-red-600 rounded-lg text-xs font-medium hover:bg-red-100">
                          Cancel Order
                        </button>
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Deliver Modal */}
      {showDeliverModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-900">Confirm Delivery</h3>
              <button onClick={() => setShowDeliverModal(false)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <form onSubmit={handleDeliver} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Actual Delivery Date</label>
                <input required type="date" value={deliverForm.deliveryDate}
                  onChange={e => setDeliverForm({...deliverForm, deliveryDate: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"/>
              </div>
              <div>
                <label className="block text-xs font-medium text-gray-700 mb-1">Notes</label>
                <input value={deliverForm.notes}
                  onChange={e => setDeliverForm({...deliverForm, notes: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="All items received in good condition"/>
              </div>
              <div className="flex gap-3">
                <button type="button" onClick={() => setShowDeliverModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50">Cancel</button>
                <button type="submit"
                  className="flex-1 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg text-sm font-medium">Confirm Delivery</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}