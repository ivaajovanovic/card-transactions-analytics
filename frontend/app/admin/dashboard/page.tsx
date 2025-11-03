'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useQuery } from '@tanstack/react-query'
import { getCurrentUser, logout } from '@/lib/auth'
import { apiClient } from '@/lib/api'
import type { AdminKPIResponse, UserSegmentDTO, CategoryStatsDTO } from '@/lib/types'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Users, Store, CreditCard, DollarSign, TrendingUp, LogOut, BarChart3, Compass, ShieldAlert } from 'lucide-react'
import { BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { TrendsTab } from '@/components/admin/TrendsTab'
import { RecommendationsTab } from '@/components/admin/RecommendationsTab'

export default function AdminDashboard() {
  const router = useRouter()
  const [user, setUser] = useState(getCurrentUser())

  useEffect(() => {
    if (!user || user.role !== 'admin') {
      router.push('/login')
    }
  }, [user, router])

  // Fetch real data from backend
  const { data: kpiData, isLoading: kpiLoading } = useQuery<AdminKPIResponse>({
    queryKey: ['admin-kpis'],
    queryFn: () => apiClient.getKPIMetrics(),
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  const { data: segmentData, isLoading: segmentLoading } = useQuery<UserSegmentDTO[]>({
    queryKey: ['user-segments'],
    queryFn: () => apiClient.getUserSegments(),
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  const { data: categoryData, isLoading: categoryLoading } = useQuery<CategoryStatsDTO[]>({
    queryKey: ['top-categories'],
    queryFn: () => apiClient.getTopCategories(),
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  // Performance data (last 30 days)
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
  const { data: failureReasonsData, isLoading: failureReasonsLoading } = useQuery<any[]>({
    queryKey: ['failure-reasons'],
    queryFn: () => apiClient.getFailureReasons(thirtyDaysAgo.toISOString(), new Date().toISOString()),
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  const { data: failedMerchantsData, isLoading: failedMerchantsLoading } = useQuery<any[]>({
    queryKey: ['failed-merchants'],
    queryFn: () => apiClient.getFailedByMerchant(thirtyDaysAgo.toISOString(), new Date().toISOString(), 10),
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  const { data: cardTypeFailuresData, isLoading: cardTypeFailuresLoading } = useQuery<any[]>({
    queryKey: ['card-type-failures'],
    queryFn: () => apiClient.getCardTypeFailures(thirtyDaysAgo.toISOString(), new Date().toISOString()),
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  // Network data
  const { data: acceptanceCoverageData, isLoading: acceptanceCoverageLoading } = useQuery<any[]>({
    queryKey: ['acceptance-coverage'],
    queryFn: () => apiClient.getAcceptanceCoverage(),
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
  })

  const { data: acceptanceGapsData, isLoading: acceptanceGapsLoading } = useQuery<any[]>({
    queryKey: ['acceptance-gaps'],
    queryFn: () => apiClient.getAcceptanceGaps(thirtyDaysAgo.toISOString(), new Date().toISOString(), 10),
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
  })

  // Fallbacks: basic counts if analytics endpoints are empty
  const { data: usersList } = useQuery<any[]>({
    queryKey: ['users-list'],
    queryFn: () => apiClient.listUsers(),
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
  })
  const { data: merchantsList } = useQuery<any[]>({
    queryKey: ['merchants-list'],
    queryFn: () => apiClient.listMerchants(),
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
  })

  if (!user) return null

  const isLoading = kpiLoading || segmentLoading || categoryLoading

  // Format failure reasons for pie chart
  const failureReasonsChartData = failureReasonsData?.map((reason, idx) => ({
    name: reason.groupKey || 'Unknown',
    value: reason.txnCount,
    color: ['#ef4444', '#f59e0b', '#eab308', '#84cc16', '#22c55e', '#14b8a6'][idx % 6]
  })) || []

  // Format failed merchants for bar chart
  const failedMerchantsChartData = failedMerchantsData?.map((merchant) => ({
    merchant: merchant.groupKey || 'Unknown',
    failures: merchant.txnCount,
    amount: merchant.totalAmount
  })) || []

  // Format card type failures for bar chart (success rate)
  const cardTypePerformanceData = cardTypeFailuresData?.map((ct) => ({
    cardType: ct.groupKey || 'Unknown',
    successRate: (ct.totalAmount * 100).toFixed(1),
    transactions: ct.txnCount
  })) || []

  // Format acceptance coverage for bar chart
  const acceptanceCoverageChartData = acceptanceCoverageData
    ?.map((acc) => ({
      network: acc.groupKey || 'Unknown',
      merchants: Number(acc.txnCount) || 0
    }))
    .sort((a, b) => b.merchants - a.merchants) // Sort by merchant count descending
    || []

  // Format segment data for pie chart
  const userSegmentData = segmentData?.map((seg, idx) => ({
    name: seg.name,
    value: seg.value,
    color: ['#8b5cf6', '#3b82f6', '#10b981', '#f59e0b', '#ef4444'][idx % 5]
  })) || []

  // Format category data for bar chart
  const topCategoriesData = categoryData?.map((cat) => ({
    category: cat.category,
    amount: cat.amount,
    count: cat.transactions
  })) || []

  // Note: Locations and Trends disabled for now

  // Format KPI cards data
  const hasKpi = !!kpiData && !Number.isNaN(kpiData.totalUsers)
  const fallbackUsers = usersList?.length ?? 0
  const fallbackMerchants = merchantsList?.length ?? 0

  const kpiCards = hasKpi ? [
    { 
      title: 'Total Users', 
      value: kpiData.totalUsers.toLocaleString(), 
      change: `+${kpiData.userGrowth.toFixed(1)}%`, 
      icon: Users, 
      color: 'text-blue-600', 
      bgColor: 'bg-blue-50' 
    },
    { 
      title: 'Total Merchants', 
      value: kpiData.totalMerchants.toLocaleString(), 
      change: `+${kpiData.merchantGrowth.toFixed(1)}%`, 
      icon: Store, 
      color: 'text-green-600', 
      bgColor: 'bg-green-50' 
    },
    { 
      title: 'Transactions', 
      value: `${Math.round(kpiData.totalTransactions / 1000)}K`, 
      change: `+${kpiData.transactionGrowth.toFixed(1)}%`, 
      icon: CreditCard, 
      color: 'text-purple-600', 
      bgColor: 'bg-purple-50' 
    },
    { 
      title: 'Total Volume', 
      value: `$${(kpiData.totalVolume / 1000000).toFixed(1)}M`, 
      change: `+${kpiData.volumeGrowth.toFixed(1)}%`, 
      icon: DollarSign, 
      color: 'text-yellow-600', 
      bgColor: 'bg-yellow-50' 
    },
  ] : [
    { 
      title: 'Users (live)', 
      value: fallbackUsers.toLocaleString(), 
      change: '', 
      icon: Users, 
      color: 'text-blue-600', 
      bgColor: 'bg-blue-50' 
    },
    { 
      title: 'Merchants (live)', 
      value: fallbackMerchants.toLocaleString(), 
      change: '', 
      icon: Store, 
      color: 'text-green-600', 
      bgColor: 'bg-green-50' 
    },
  ]

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="border-b bg-white shadow-sm">
        <div className="container mx-auto flex items-center justify-between px-4 py-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <p className="text-sm text-gray-600">Card Transactions Analytics</p>
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
        <Tabs defaultValue="analytics" className="w-full">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="analytics">
              <BarChart3 className="mr-2 h-4 w-4" />
              Analytics & Dashboard
            </TabsTrigger>
            <TabsTrigger value="recommendations">
              <Compass className="mr-2 h-4 w-4" />
              Recommendations & Predictions
            </TabsTrigger>
            <TabsTrigger value="detection">
              <ShieldAlert className="mr-2 h-4 w-4" />
              Detection & Admin
            </TabsTrigger>
          </TabsList>

          {/* ANALYTICS TAB */}
          <TabsContent value="analytics" className="space-y-6">
            {/* KPI Cards */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
              {kpiCards.map((kpi) => {
                const Icon = kpi.icon
                return (
                  <Card key={kpi.title}>
                    <CardContent className="p-6">
                      <div className="flex items-center justify-between">
                        <div>
                          <p className="text-sm font-medium text-gray-600">{kpi.title}</p>
                          <p className="mt-2 text-3xl font-bold">{kpi.value}</p>
                          <p className="mt-1 text-sm text-green-600 flex items-center">
                            <TrendingUp className="mr-1 h-4 w-4" />
                            {kpi.change} from last month
                          </p>
                        </div>
                        <div className={`flex h-12 w-12 items-center justify-center rounded-full ${kpi.bgColor}`}>
                          <Icon className={`h-6 w-6 ${kpi.color}`} />
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )
              })}
            </div>

            {/* User Segments & Success Rate */}
            <div className="grid gap-6 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>User Segments</CardTitle>
                  <CardDescription>Distribution of users by ownership</CardDescription>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={userSegmentData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }: any) => `${name} ${((percent as number) * 100).toFixed(0)}%`}
                        outerRadius={100}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {userSegmentData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.color} />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Transaction Success Rate</CardTitle>
                  <CardDescription>Overall success vs failure distribution</CardDescription>
                </CardHeader>
                <CardContent>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={[
                          { name: 'Success', value: 21500, color: '#10b981' },
                          { name: 'Failed', value: 1802, color: '#ef4444' }
                        ]}
                        cx="50%"
                        cy="50%"
                        labelLine={true}
                        label={({ name, percent }: any) => {
                          const pct = ((percent as number) * 100).toFixed(1);
                          return `${name} ${pct}%`;
                        }}
                        outerRadius={110}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        <Cell fill="#10b981" />
                        <Cell fill="#ef4444" />
                      </Pie>
                      <Tooltip formatter={(value: number) => value.toLocaleString()} />
                    </PieChart>
                  </ResponsiveContainer>
                </CardContent>
              </Card>
            </div>

            {/* Top Categories - full width */}
            <Card>
              <CardHeader>
                <CardTitle>Top Transaction Categories</CardTitle>
                <CardDescription>Highest performing merchant categories</CardDescription>
              </CardHeader>
              <CardContent>
                {topCategoriesData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={350}>
                    <BarChart data={topCategoriesData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis 
                        dataKey="category" 
                        interval={0}
                        angle={-30}
                        textAnchor="end"
                        height={100}
                        tick={{ fontSize: 11 }}
                      />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="amount" fill="#8b5cf6" name="Amount ($)" />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="text-sm text-gray-500">No category analytics yet.</div>
                )}
              </CardContent>
            </Card>

            {/* Trends within Analytics */}
            <TrendsTab />
          </TabsContent>

          {/* RECOMMENDATIONS TAB */}
          <TabsContent value="recommendations" className="space-y-6">
            <RecommendationsTab />
          </TabsContent>

          {/* DETECTION & ADMIN TAB */}
          <TabsContent value="detection" className="space-y-6">
            <div className="grid gap-6 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <CardTitle>Failure Reasons</CardTitle>
                  <CardDescription>Distribution of decline reasons (last 30 days)</CardDescription>
                </CardHeader>
                <CardContent>
                  {failureReasonsChartData.length > 0 ? (
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={failureReasonsChartData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={({ name, percent }: any) => `${name} ${((percent as number) * 100).toFixed(0)}%`}
                          outerRadius={100}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {failureReasonsChartData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={entry.color} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="text-sm text-gray-500">No failure data available.</div>
                  )}
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Card Type Performance</CardTitle>
                  <CardDescription>Success rate by card type (last 30 days)</CardDescription>
                </CardHeader>
                <CardContent>
                  {cardTypePerformanceData.length > 0 ? (
                    <ResponsiveContainer width="100%" height={300}>
                      <BarChart data={cardTypePerformanceData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="cardType" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Bar dataKey="successRate" fill="#10b981" name="Success Rate (%)" />
                      </BarChart>
                    </ResponsiveContainer>
                  ) : (
                    <div className="text-sm text-gray-500">No card type data available.</div>
                  )}
                </CardContent>
              </Card>
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Top Failed Merchants</CardTitle>
                <CardDescription>Merchants with highest failure counts (last 30 days)</CardDescription>
              </CardHeader>
              <CardContent>
                {failedMerchantsChartData.length > 0 ? (
                  <ResponsiveContainer width="100%" height={350}>
                    <BarChart data={failedMerchantsChartData} layout="vertical">
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis type="number" />
                      <YAxis dataKey="merchant" type="category" width={150} />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="failures" fill="#ef4444" name="Failed Transactions" />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="text-sm text-gray-500">No merchant failure data available.</div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Move Network content under Detection & Admin */}
          <TabsContent value="detection" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Suspicious Transaction Indicators</CardTitle>
                <CardDescription>Potential fraud patterns and high-risk transactions</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 md:grid-cols-3">
                  {/* High-value Failed Transactions */}
                  <div className="rounded-lg border p-4 bg-red-50">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-600">High-Value Failures</p>
                        <p className="text-2xl font-bold text-red-600">
                          {failedMerchantsData?.slice(0, 3).reduce((sum, m) => sum + (m.totalAmount || 0), 0).toLocaleString('en-US', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 })}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {failedMerchantsData?.slice(0, 3).reduce((sum, m) => sum + (m.txnCount || 0), 0)} failed attempts
                        </p>
                      </div>
                      <div className="h-12 w-12 rounded-full bg-red-100 flex items-center justify-center">
                        <span className="text-2xl">⚠️</span>
                      </div>
                    </div>
                  </div>

                  {/* Unusual Decline Patterns */}
                  <div className="rounded-lg border p-4 bg-yellow-50">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-600">Decline Reasons</p>
                        <p className="text-2xl font-bold text-yellow-700">
                          {failureReasonsData?.length || 0}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {failureReasonsData?.[0]?.groupKey || 'N/A'} (top reason)
                        </p>
                      </div>
                      <div className="h-12 w-12 rounded-full bg-yellow-100 flex items-center justify-center">
                        <span className="text-2xl">🔍</span>
                      </div>
                    </div>
                  </div>

                  {/* At-Risk Merchants */}
                  <div className="rounded-lg border p-4 bg-orange-50">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-600">At-Risk Merchants</p>
                        <p className="text-2xl font-bold text-orange-600">
                          {failedMerchantsData?.filter(m => m.txnCount > 50)?.length || 0}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">
                          {'>'}50 failures each
                        </p>
                      </div>
                      <div className="h-12 w-12 rounded-full bg-orange-100 flex items-center justify-center">
                        <span className="text-2xl">🚨</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Suspicious Transactions Table */}
                <div className="mt-6">
                  <h3 className="text-sm font-semibold mb-3">High-Risk Merchants (Last 30 Days)</h3>
                  {failedMerchantsData && failedMerchantsData.length > 0 ? (
                    <div className="border rounded-lg overflow-hidden">
                      <table className="w-full text-sm">
                        <thead className="bg-gray-50">
                          <tr>
                            <th className="px-4 py-3 text-left font-medium text-gray-600">Merchant</th>
                            <th className="px-4 py-3 text-right font-medium text-gray-600">Failed Txns</th>
                            <th className="px-4 py-3 text-right font-medium text-gray-600">Amount</th>
                            <th className="px-4 py-3 text-center font-medium text-gray-600">Risk Level</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y">
                          {failedMerchantsData.slice(0, 8).map((merchant, idx) => {
                            const riskLevel = merchant.txnCount > 100 ? 'High' : merchant.txnCount > 50 ? 'Medium' : 'Low';
                            const riskColor = merchant.txnCount > 100 ? 'text-red-600 bg-red-50' : merchant.txnCount > 50 ? 'text-orange-600 bg-orange-50' : 'text-yellow-600 bg-yellow-50';
                            return (
                              <tr key={idx} className="hover:bg-gray-50">
                                <td className="px-4 py-3 font-medium">{merchant.groupKey}</td>
                                <td className="px-4 py-3 text-right text-red-600 font-semibold">{merchant.txnCount}</td>
                                <td className="px-4 py-3 text-right">${(merchant.totalAmount || 0).toLocaleString()}</td>
                                <td className="px-4 py-3 text-center">
                                  <span className={`px-2 py-1 rounded text-xs font-semibold ${riskColor}`}>
                                    {riskLevel}
                                  </span>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  ) : (
                    <div className="text-sm text-gray-500 text-center py-8 border rounded-lg">No suspicious activity detected.</div>
                  )}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Acceptance Gaps</CardTitle>
                <CardDescription>Merchants with limited card network support</CardDescription>
              </CardHeader>
              <CardContent>
                {acceptanceGapsData && acceptanceGapsData.length > 0 ? (
                  <div className="space-y-2">
                    {acceptanceGapsData.map((gap, idx) => (
                      <div key={idx} className="flex items-center justify-between border-b pb-2">
                        <span className="text-sm font-medium">{gap.groupKey}</span>
                        <div className="flex gap-4 text-sm text-gray-600">
                          <span>Networks: {gap.totalAmount}</span>
                          <span className="text-red-600">Failures: {gap.txnCount}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-sm text-gray-500">No acceptance gap data available.</div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>
    </div>
  )
}
