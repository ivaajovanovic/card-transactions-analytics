'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { getCurrentUser, logout } from '@/lib/auth'
import { apiClient } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { DollarSign, Users, TrendingUp, ShoppingBag, LogOut, Award } from 'lucide-react'
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

export default function MerchantDashboard() {
  const router = useRouter()
  const [user, setUser] = useState(getCurrentUser())
  const [merchantEmail, setMerchantEmail] = useState<string>('')
  const [loading, setLoading] = useState(true)
  
  const [overview, setOverview] = useState<any>(null)
  const [monthlyRevenue, setMonthlyRevenue] = useState<any[]>([])
  const [cardNetworks, setCardNetworks] = useState<any[]>([])
  const [paymentTypes, setPaymentTypes] = useState<any[]>([])
  const [topCustomers, setTopCustomers] = useState<any[]>([])
  const [channels, setChannels] = useState<any[]>([])
  const [hourlyData, setHourlyData] = useState<any[]>([])

  useEffect(() => {
    if (!user || user.role !== 'merchant') {
      router.push('/login')
      return
    }
    
    const email = localStorage.getItem('userEmail')
    if (email) {
      setMerchantEmail(email)
      loadAnalytics(email)
    } else {
      router.push('/login')
    }
  }, [user, router])

  const loadAnalytics = async (email: string) => {
    setLoading(true)
    try {
      const [overviewRes, revenueRes, networksRes, typesRes, customersRes, channelsRes, hourlyRes] = await Promise.all([
        apiClient.getMerchantOverviewByEmail(email),
        apiClient.getMerchantMonthlyRevenueByEmail(email),
        apiClient.getMerchantCardNetworksByEmail(email),
        apiClient.getMerchantPaymentTypesByEmail(email),
        apiClient.getMerchantTopCustomersByEmail(email, 10),
        apiClient.getMerchantTransactionChannelsByEmail(email),
        apiClient.getMerchantHourlyDistributionByEmail(email)
      ])
      
      setOverview(overviewRes)
      setMonthlyRevenue(revenueRes)
      setCardNetworks(networksRes)
      setPaymentTypes(typesRes)
      setTopCustomers(customersRes)
      setChannels(channelsRes)
      setHourlyData(hourlyRes)
    } catch (error) {
      console.error('Failed to load merchant analytics:', error)
    } finally {
      setLoading(false)
    }
  }

  if (!user || loading) return (
    <div className="flex items-center justify-center min-h-screen">
      <p className="text-lg">Loading...</p>
    </div>
  )

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8']

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b bg-white shadow-sm">
        <div className="container mx-auto flex items-center justify-between px-4 py-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Merchant Dashboard</h1>
            <p className="text-sm text-gray-600">Business Analytics</p>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium">{user.name}</p>
              <p className="text-xs text-gray-500">{user.email}</p>
            </div>
            <Button variant="outline" size="sm" onClick={logout}>
              <LogOut className="mr-2 h-4 w-4" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="container mx-auto p-6 space-y-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Total Revenue</p>
                  <p className="mt-2 text-3xl font-bold">${overview?.totalRevenue?.toFixed(2) || '0.00'}</p>
                  <p className="mt-1 text-sm text-gray-500">{overview?.totalTransactions || 0} transactions</p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-green-50">
                  <DollarSign className="h-6 w-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Avg Transaction</p>
                  <p className="mt-2 text-3xl font-bold">${overview?.avgTransactionAmount?.toFixed(2) || '0.00'}</p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50">
                  <TrendingUp className="h-6 w-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Min / Max</p>
                  <p className="mt-2 text-2xl font-bold">${overview?.minAmount?.toFixed(2)} / ${overview?.maxAmount?.toFixed(2)}</p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-purple-50">
                  <ShoppingBag className="h-6 w-6 text-purple-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="cursor-pointer hover:shadow-lg transition-shadow" onClick={() => router.push('/merchant/transactions')}>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">Transactions</p>
                  <p className="mt-2 text-3xl font-bold">{overview?.totalTransactions || 0}</p>
                  <p className="mt-1 text-sm text-blue-600 hover:underline">View all </p>
                </div>
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-yellow-50">
                  <Award className="h-6 w-6 text-yellow-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Monthly Revenue</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={monthlyRevenue}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip formatter={(value: any) => `$${Number(value).toFixed(2)}`} />
                <Legend />
                <Line type="monotone" dataKey="totalRevenue" stroke="#10b981" strokeWidth={2} name="Revenue ($)" />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Card Networks</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={cardNetworks}
                    dataKey="count"
                    nameKey="cardNetwork"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    label={(entry) => `${entry.cardNetwork}: ${entry.count}`}
                  >
                    {cardNetworks.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Payment Types</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={paymentTypes}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="paymentType" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="count" fill="#3b82f6" name="Total" />
                  <Bar dataKey="contactlessCount" fill="#10b981" name="Contactless" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Top Customers</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {topCustomers.map((customer, index) => (
                  <div key={index} className="flex items-center justify-between border-b pb-3 last:border-0">
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-50">
                        <Users className="h-5 w-5 text-blue-600" />
                      </div>
                      <div>
                        <p className="font-medium">{customer.userName}</p>
                        <p className="text-sm text-gray-500">{customer.userEmail}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold">{customer.transactionCount}</p>
                      <p className="text-xs text-gray-500">transactions</p>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Channels</CardTitle>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={250}>
                <BarChart data={channels}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="channel" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="count" fill="#8b5cf6" name="Transactions" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Hourly Distribution</CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={hourlyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="hour" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="#f59e0b" name="Transactions" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </main>
    </div>
  )
}
