import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../../api/authApi'
import PasswordInput from '../../components/common/PasswordInput'

const STEPS = [
  { id: 1, label: 'Email' },
  { id: 2, label: 'Verify' },
  { id: 3, label: 'Reset' },
]

const StepIndicator = ({ current }) => (
  <div className="flex items-center justify-center mb-8">
    {STEPS.map((s, i) => (
      <div key={s.id} className="flex items-center">
        <div className="flex flex-col items-center">
          <div
            className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold transition-colors
              ${current > s.id
                ? 'bg-accent-600 text-white'
                : current === s.id
                ? 'bg-accent-100 text-accent-700 border-2 border-accent-600'
                : 'bg-gray-100 text-gray-400 border-2 border-gray-200'
              }`}
          >
            {current > s.id ? (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
              </svg>
            ) : s.id}
          </div>
          <span className={`text-[11px] mt-1.5 font-medium ${current >= s.id ? 'text-accent-700' : 'text-gray-400'}`}>
            {s.label}
          </span>
        </div>
        {i < STEPS.length - 1 && (
          <div className={`w-12 h-0.5 mx-2 -mt-4 ${current > s.id ? 'bg-accent-600' : 'bg-gray-200'}`} />
        )}
      </div>
    ))}
  </div>
)

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [token, setToken] = useState('')
  const [showDevToken, setShowDevToken] = useState(false)
  const [form, setForm] = useState({ newPassword: '', confirmPassword: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const passwordsMismatch =
    form.confirmPassword.length > 0 && form.newPassword !== form.confirmPassword

  const handleForgot = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const res = await authApi.forgotPassword({ email })
      setToken(res.data.resetToken)
      setStep(2)
    } catch (err) {
      setError(err.response?.data?.message || 'We could not find an account with that email address.')
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyContinue = (e) => {
    e.preventDefault()
    setStep(3)
  }

  const handleReset = async (e) => {
    e.preventDefault()
    setError('')
    if (form.newPassword !== form.confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    setLoading(true)
    try {
      await authApi.resetPassword({ token, ...form })
      setStep(4)
    } catch (err) {
      setError(err.response?.data?.message || 'We could not reset your password. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-900 via-primary-700 to-primary-900 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">

        {/* Logo — consistent with Login page */}
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-14 h-14 bg-gradient-to-br from-accent-500 to-accent-600 rounded-2xl mb-3 shadow-lg">
            <svg className="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
            </svg>
          </div>
          <h1 className="text-lg font-bold text-primary-900">Reset Your Password</h1>
          <p className="text-primary-400 text-xs mt-1">RxPharma Account Recovery</p>
        </div>

        {step < 4 && <StepIndicator current={step} />}

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm flex items-start gap-2">
            <svg className="w-4 h-4 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
            <span>{error}</span>
          </div>
        )}

        {step === 1 && (
          <form onSubmit={handleForgot} className="space-y-4">
            <p className="text-sm text-gray-500 text-center">
              Enter the email address associated with your account, and we&apos;ll send you a link to reset your password.
            </p>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email Address</label>
              <input
                type="email" required value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@company.com"
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-accent-500 focus:border-transparent text-sm"
              />
            </div>
            <button type="submit" disabled={loading}
              className="w-full bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-700 hover:to-accent-600 disabled:opacity-60 text-white font-medium py-2.5 rounded-lg text-sm shadow-md transition-all">
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Sending instructions...
                </span>
              ) : 'Send Reset Instructions'}
            </button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleVerifyContinue} className="space-y-5">
            <div className="text-center space-y-3">
              <div className="w-14 h-14 bg-accent-50 rounded-full flex items-center justify-center mx-auto">
                <svg className="w-7 h-7 text-accent-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
              </div>
              <div>
                <p className="text-gray-800 font-medium text-sm">Check your email</p>
                <p className="text-gray-500 text-sm mt-1">
                  We&apos;ve sent password reset instructions to<br/>
                  <span className="font-medium text-gray-700">{email}</span>
                </p>
              </div>
              <p className="text-xs text-gray-400">
                Didn&apos;t receive anything? Check your spam folder, or
                {' '}
                <button type="button" onClick={() => setStep(1)} className="text-accent-600 hover:underline font-medium">
                  try a different email
                </button>.
              </p>
            </div>

            {/* Dev/testing mode token — collapsed by default, only relevant until real email sending is wired up */}
            <div className="border border-dashed border-gray-200 rounded-lg">
              <button
                type="button"
                onClick={() => setShowDevToken(s => !s)}
                className="w-full flex items-center justify-between px-3 py-2 text-xs text-gray-400 hover:text-gray-600"
              >
                <span>Developer mode — show reset code</span>
                <svg className={`w-3.5 h-3.5 transition-transform ${showDevToken ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7"/>
                </svg>
              </button>
              {showDevToken && (
                <div className="px-3 pb-3 text-xs text-gray-500 break-all">
                  <code className="bg-gray-50 px-2 py-1 rounded">{token}</code>
                  <p className="mt-1 text-gray-400">This code expires in 30 minutes.</p>
                </div>
              )}
            </div>

            <button type="submit"
              className="w-full bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-700 hover:to-accent-600 text-white font-medium py-2.5 rounded-lg text-sm shadow-md transition-all">
              Continue
            </button>
          </form>
        )}

        {step === 3 && (
          <form onSubmit={handleReset} className="space-y-4">
            <p className="text-sm text-gray-500 text-center mb-2">
              Choose a strong new password for your account.
            </p>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">New Password</label>
              <PasswordInput
                value={form.newPassword}
                onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
                placeholder="At least 8 characters"
                minLength={8}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Confirm New Password</label>
              <PasswordInput
                value={form.confirmPassword}
                onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
                placeholder="Re-enter your new password"
                minLength={8}
              />
              {passwordsMismatch && (
                <p className="text-xs text-red-500 mt-1">Passwords do not match.</p>
              )}
            </div>
            <button type="submit" disabled={loading}
              className="w-full bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-700 hover:to-accent-600 disabled:opacity-60 text-white font-medium py-2.5 rounded-lg text-sm shadow-md transition-all">
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                  Updating password...
                </span>
              ) : 'Update Password'}
            </button>
          </form>
        )}

        {step === 4 && (
          <div className="text-center space-y-4">
            <div className="w-16 h-16 bg-green-50 rounded-full flex items-center justify-center mx-auto">
              <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <div>
              <p className="text-gray-900 font-semibold">Password reset successful</p>
              <p className="text-gray-500 text-sm mt-1">
                Your password has been updated. You can now sign in with your new password.
              </p>
            </div>
            <button onClick={() => navigate('/login')}
              className="w-full bg-gradient-to-r from-accent-600 to-accent-500 hover:from-accent-700 hover:to-accent-600 text-white font-medium py-2.5 rounded-lg text-sm shadow-md transition-all">
              Continue to Sign In
            </button>
          </div>
        )}

        {step < 4 && (
          <button onClick={() => navigate('/login')}
            className="w-full text-center text-sm text-gray-400 hover:text-gray-600 mt-6 flex items-center justify-center gap-1">
            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18"/>
            </svg>
            Back to Sign In
          </button>
        )}
      </div>
    </div>
  )
}