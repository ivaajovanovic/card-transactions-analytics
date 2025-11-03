'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { UserCircle, Shield, Store } from 'lucide-react'
import { apiClient } from '@/lib/api'

type UserRole = 'admin' | 'user' | 'merchant'

export default function LoginPage() {
  const router = useRouter()
  const [selectedRole, setSelectedRole] = useState<UserRole | null>(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleLogin = async (role: UserRole) => {
    setError(null)
    if (role === 'admin' && !password) { setError('Please enter your password'); return }

  if (!email) { setError('Please enter your email'); return }
  if ((role === 'user' || role === 'merchant') && !password) { setError('Please enter your password'); return }
    setLoading(true)
    try {
      if (role === 'user') {
        const user = await apiClient.loginByEmail(email, password)
        localStorage.setItem('userRole', 'user')
        localStorage.setItem('userId', user.externalId)
        localStorage.setItem('userName', user.fullName || user.externalId)
        localStorage.setItem('userEmail', user.email || email)
        router.push('/user/dashboard')
      } else if (role === 'merchant') {
        const merchant = await apiClient.loginMerchantByEmail(email, password)
        localStorage.setItem('userRole', 'merchant')
        localStorage.setItem('userId', merchant.merchantId)
        localStorage.setItem('userName', merchant.name || merchant.merchantId)
        localStorage.setItem('userEmail', merchant.email || email)
        router.push('/merchant/dashboard')
      } else if (role === 'admin') {
        const admin = await apiClient.loginAdminByEmail(email, password)
        localStorage.setItem('userRole', 'admin')
        // keep id as numeric id if present
        localStorage.setItem('userId', admin.id?.toString?.() || 'admin')
        localStorage.setItem('userName', admin.fullName || 'Admin')
        localStorage.setItem('userEmail', admin.email || email)
        router.push('/admin/dashboard')
      }
    } catch (e) {
      setError('Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  const roles = [
    {
      id: 'admin' as UserRole,
      title: 'Administrator',
      description: 'Full system access and analytics',
      icon: Shield,
      color: 'text-red-600',
      bgColor: 'bg-red-50 hover:bg-red-100',
    },
    {
      id: 'user' as UserRole,
      title: 'User',
      description: 'Personal transaction dashboard',
      icon: UserCircle,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50 hover:bg-blue-100',
    },
    {
      id: 'merchant' as UserRole,
      title: 'Merchant',
      description: 'Business analytics and insights',
      icon: Store,
      color: 'text-green-600',
      bgColor: 'bg-green-50 hover:bg-green-100',
    },
  ]

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="w-full max-w-4xl">
        <div className="mb-8 text-center">
          <h1 className="text-4xl font-bold text-gray-900">Card Transactions Analytics</h1>
          <p className="mt-2 text-lg text-gray-600">Choose your role to continue</p>
        </div>

        <div className="grid gap-6 md:grid-cols-3">
          {roles.map((role) => {
            const Icon = role.icon
            return (
              <Card
                key={role.id}
                className={`cursor-pointer transition-all hover:shadow-lg ${
                  selectedRole === role.id ? 'ring-2 ring-primary' : ''
                }`}
                onClick={() => setSelectedRole(role.id)}
              >
                <CardHeader className="text-center">
                  <div className={`mx-auto mb-4 flex h-20 w-20 items-center justify-center rounded-full ${role.bgColor}`}>
                    <Icon className={`h-10 w-10 ${role.color}`} />
                  </div>
                  <CardTitle>{role.title}</CardTitle>
                  <CardDescription>{role.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  {selectedRole === role.id ? (
                    <div className="space-y-3">
                      <input
                        type="email"
                        placeholder={`Enter ${role.id} email`}
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                      <input
                        type="password"
                        placeholder="Enter password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                      />
                      <Button className="w-full" onClick={(e) => { e.stopPropagation(); handleLogin(role.id) }} disabled={loading}>
                        {loading ? 'Signing in…' : `Login as ${role.title}`}
                      </Button>
                      {error && <p className="text-sm text-red-600">{error}</p>}
                    </div>
                  ) : (
                    <Button
                      className="w-full"
                      onClick={(e) => { e.stopPropagation(); handleLogin(role.id) }}
                      variant={selectedRole === role.id ? 'default' : 'outline'}
                    >
                      Login as {role.title}
                    </Button>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>

        <div className="mt-8 text-center text-sm text-gray-500">
          <p>Demo Mode - Click any role to access the dashboard</p>
        </div>
      </div>
    </div>
  )
}
