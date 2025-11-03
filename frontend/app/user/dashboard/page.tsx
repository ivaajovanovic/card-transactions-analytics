'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { apiClient } from '@/lib/api'
import { BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

const COLORS = ['#3b82f6', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#6366f1', '#f97316', '#14b8a6']

export default function UserDashboard() {
  const router = useRouter()
  const [userId, setUserId] = useState<number | null>(null)
  const [userEmail, setUserEmail] = useState<string>('')
  const [userName, setUserName] = useState<string>('')
  const [activeTab, setActiveTab] = useState<'spending' | 'cards' | 'insights'>('spending')
  
  // Spending tab data
  const [spendingByCategory, setSpendingByCategory] = useState<any[]>([])
  const [topMerchants, setTopMerchants] = useState<any[]>([])
  const [monthlySpending, setMonthlySpending] = useState<any[]>([])
  
  // Cards tab data
  const [cardUsage, setCardUsage] = useState<any[]>([])
  
  // Insights tab data
  const [recurringExpenses, setRecurringExpenses] = useState<any[]>([])
  const [collaborativeRecommendations, setCollaborativeRecommendations] = useState<any[]>([])
  const [optimalPurchaseTime, setOptimalPurchaseTime] = useState<any[]>([])
  
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Check if user is logged in
    const role = localStorage.getItem('userRole')
    const id = localStorage.getItem('userId')
    const email = localStorage.getItem('userEmail')
    const name = localStorage.getItem('userName')
    
    if (role !== 'user' || !email) {
      router.push('/login')
      return
    }
    
    setUserId(Number(id))
    setUserEmail(email)
    setUserName(name || 'User')
    
    loadAnalytics(email)
  }, [router])

  const loadAnalytics = async (email: string) => {
    try {
      setLoading(true)
      
      // Load all analytics in parallel using email-based endpoints
      const [categoryData, merchantData, monthlyData, cardData, recurringData, collaborativeData, optimalTimeData] = await Promise.all([
        apiClient.getUserSpendingByCategoryByEmail(email),
        apiClient.getUserTopMerchantsListByEmail(email, 10),
        apiClient.getUserMonthlySpendingByEmail(email),
        apiClient.getUserCardUsageByEmail(email),
        apiClient.getUserRecurringExpensesByEmail(email),
        apiClient.getUserCollaborativeRecommendationsByEmail(email),
        apiClient.getUserOptimalPurchaseTimeByEmail(email),
      ])
      
      setSpendingByCategory(categoryData)
      setTopMerchants(merchantData)
      setMonthlySpending(monthlyData)
      setCardUsage(cardData)
      setRecurringExpenses(recurringData)
      setCollaborativeRecommendations(collaborativeData)
      setOptimalPurchaseTime(optimalTimeData)
    } catch (error) {
      console.error('Failed to load user analytics:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('userRole')
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
    localStorage.removeItem('userEmail')
    router.push('/login')
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading your dashboard...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">My Dashboard</h1>
            <p className="text-sm text-gray-500">Welcome back, {userName}</p>
          </div>
          <button
            onClick={handleLogout}
            className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors"
          >
            Logout
          </button>
        </div>
      </header>

      {/* Tabs */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <nav className="flex space-x-8" aria-label="Tabs">
            {[
              { id: 'spending', label: 'My Spending' },
              { id: 'cards', label: 'My Cards' },
              { id: 'insights', label: 'Insights' },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`
                  py-4 px-1 border-b-2 font-medium text-sm transition-colors
                  ${activeTab === tab.id
                    ? 'border-blue-500 text-blue-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }
                `}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </div>
      </div>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'spending' && <SpendingTab data={{ spendingByCategory, topMerchants, monthlySpending }} />}
        {activeTab === 'cards' && <CardsTab data={{ cardUsage }} />}
  {activeTab === 'insights' && <InsightsTab userEmail={userEmail} data={{ spendingByCategory, cardUsage, monthlySpending, recurringExpenses, collaborativeRecommendations, optimalPurchaseTime }} />}
      </main>
    </div>
  )
}

// Spending Tab Component
function SpendingTab({ data }: { data: any }) {
  const { spendingByCategory, topMerchants, monthlySpending } = data
  
  const totalSpent = spendingByCategory.reduce((sum: number, cat: any) => sum + (cat.totalAmount || 0), 0)

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <p className="text-sm font-medium text-gray-500">Total Spent (All Time)</p>
          <p className="text-3xl font-bold text-gray-900 mt-2">${totalSpent.toFixed(2)}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <p className="text-sm font-medium text-gray-500">Total Transactions</p>
          <p className="text-3xl font-bold text-gray-900 mt-2">
            {spendingByCategory.reduce((sum: number, cat: any) => sum + (cat.transactionCount || 0), 0)}
          </p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <p className="text-sm font-medium text-gray-500">Favorite Merchants</p>
          <p className="text-3xl font-bold text-gray-900 mt-2">{topMerchants.length}</p>
        </div>
      </div>

      {/* Charts Row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Spending by Category */}
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Spending by Category</h3>
          {spendingByCategory.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={spendingByCategory}
                  dataKey="totalAmount"
                  nameKey="categoryName"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label
                >
                  {spendingByCategory.map((entry: any, index: number) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value: any) => `$${value.toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-gray-500 text-center py-12">No spending data available</p>
          )}
        </div>

        {/* Monthly Spending Trend */}
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Monthly Spending Trend</h3>
          {monthlySpending.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={[...monthlySpending].reverse()}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="yearMonth" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value: any) => `$${value.toFixed(2)}`} />
                <Legend />
                <Line type="monotone" dataKey="totalAmount" stroke="#3b82f6" strokeWidth={2} name="Total Spent" />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-gray-500 text-center py-12">No monthly data available</p>
          )}
        </div>
      </div>

      {/* Top Merchants Table */}
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <div className="p-6 border-b">
          <h3 className="text-lg font-semibold text-gray-900">Top Merchants</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Category</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Transactions</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Spent</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg Amount</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {topMerchants.map((merchant: any, idx: number) => (
                <tr key={idx}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{merchant.merchantName}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{merchant.categoryName || 'N/A'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{merchant.transactionCount}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${merchant.totalSpent?.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${merchant.avgAmount?.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

// Cards Tab Component
function CardsTab({ data }: { data: any }) {
  const { cardUsage } = data

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-lg shadow-sm border">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Card Usage Overview</h3>
        {cardUsage.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={cardUsage}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="cardId" tick={{ fontSize: 12 }} angle={-45} textAnchor="end" height={80} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Legend />
              <Bar dataKey="transactionCount" fill="#3b82f6" name="Transactions" />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <p className="text-gray-500 text-center py-12">No card usage data available</p>
        )}
      </div>

      {/* Card Details Table */}
      <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
        <div className="p-6 border-b">
          <h3 className="text-lg font-semibold text-gray-900">Card Details</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Card ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Network</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Transactions</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Spent</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Contactless</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Installments</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {cardUsage.map((card: any, idx: number) => (
                <tr key={idx}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{card.cardId}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{card.network}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{card.type}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{card.transactionCount}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${card.totalSpent?.toFixed(2)}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{card.contactlessCount}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{card.installmentsCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

// Insights Tab Component
function InsightsTab({ data, userEmail }: { data: any, userEmail: string }) {
  const { spendingByCategory, cardUsage, monthlySpending, recurringExpenses, collaborativeRecommendations, optimalPurchaseTime } = data
  const [selectedCategory, setSelectedCategory] = useState<string>('5411')
  const [bestCardBenefits, setBestCardBenefits] = useState<any[]>([])
  const [loadingBenefits, setLoadingBenefits] = useState<boolean>(false)

  const CATEGORY_OPTIONS = [
    { code: '5411', name: 'Grocery Stores' },
    { code: '5812', name: 'Restaurants' },
    { code: '5999', name: 'Misc. Retail' },
    { code: '7832', name: 'Movies' },
    { code: '7011', name: 'Hotels & Motels' },
    { code: '5541', name: 'Service Stations' },
    { code: '5912', name: 'Pharmacies' },
    { code: '4900', name: 'Utilities' },
    { code: '5732', name: 'Electronics' },
    { code: '5651', name: 'Clothing Stores' },
    { code: '5200', name: 'Home Supply' },
    { code: '7399', name: 'Business Services' },
  ]

  const loadBenefits = async () => {
    try {
      setLoadingBenefits(true)
      const res = await apiClient.getUserBestCardBenefitsByEmail(userEmail, selectedCategory)
      setBestCardBenefits(res || [])
    } catch (e) {
      console.error('Failed to load card benefits', e)
      setBestCardBenefits([])
    } finally {
      setLoadingBenefits(false)
    }
  }
  
  // Calculate insights
  const topCategory = spendingByCategory.length > 0 
    ? spendingByCategory.reduce((prev: any, curr: any) => (curr.totalAmount > prev.totalAmount ? curr : prev))
    : null
  
  const mostUsedCard = cardUsage.length > 0
    ? cardUsage.reduce((prev: any, curr: any) => (curr.transactionCount > prev.transactionCount ? curr : prev))
    : null
  
  const totalContactless = cardUsage.reduce((sum: number, card: any) => sum + (card.contactlessCount || 0), 0)
  const totalTransactions = cardUsage.reduce((sum: number, card: any) => sum + (card.transactionCount || 0), 0)
  const contactlessPercentage = totalTransactions > 0 ? ((totalContactless / totalTransactions) * 100).toFixed(1) : '0'
  
  const recentMonths = monthlySpending.slice(0, 2)
  const spendingTrend = recentMonths.length === 2 
    ? recentMonths[0].totalAmount > recentMonths[1].totalAmount ? 'increasing' : 'decreasing'
    : 'stable'

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-br from-blue-50 to-indigo-50 p-6 rounded-lg border border-blue-200">
        <h3 className="text-xl font-semibold text-gray-900 mb-4">📊 Your Spending Insights</h3>
        <div className="space-y-4">
          {topCategory && (
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <p className="text-sm font-medium text-gray-500">Top Spending Category</p>
              <p className="text-2xl font-bold text-blue-600 mt-1">{topCategory.categoryName}</p>
              <p className="text-sm text-gray-600 mt-1">
                ${topCategory.totalAmount.toFixed(2)} spent across {topCategory.transactionCount} transactions
              </p>
            </div>
          )}
          
          {mostUsedCard && (
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <p className="text-sm font-medium text-gray-500">Most Used Card</p>
              <p className="text-2xl font-bold text-purple-600 mt-1">{mostUsedCard.network} {mostUsedCard.type}</p>
              <p className="text-sm text-gray-600 mt-1">
                Used {mostUsedCard.transactionCount} times, total: ${mostUsedCard.totalSpent?.toFixed(2)}
              </p>
            </div>
          )}
          
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <p className="text-sm font-medium text-gray-500">Contactless Adoption</p>
            <p className="text-2xl font-bold text-green-600 mt-1">{contactlessPercentage}%</p>
            <p className="text-sm text-gray-600 mt-1">
              {totalContactless} out of {totalTransactions} transactions used contactless payment
            </p>
          </div>
          
          <div className="bg-white p-4 rounded-lg shadow-sm">
            <p className="text-sm font-medium text-gray-500">Spending Trend</p>
            <p className="text-2xl font-bold text-orange-600 mt-1 capitalize">{spendingTrend}</p>
            <p className="text-sm text-gray-600 mt-1">
              {spendingTrend === 'increasing' && 'Your spending has gone up compared to last month'}
              {spendingTrend === 'decreasing' && 'Your spending has gone down compared to last month'}
              {spendingTrend === 'stable' && 'Your spending is relatively stable'}
            </p>
          </div>
        </div>
      </div>

      {/* Recurring Expenses Section */}
      {recurringExpenses && recurringExpenses.length > 0 && (
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">🔄 Recurring Monthly Expenses</h3>
          <p className="text-sm text-gray-600 mb-4">
            Merchants where you have regular spending patterns (3+ months)
          </p>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Avg Monthly Spend</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Months Active</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {recurringExpenses.map((expense: any, idx: number) => (
                  <tr key={idx}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{expense.merchantName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${expense.avgMonthlySpend?.toFixed(2)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                        {expense.months} months
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Collaborative Filtering Recommendations */}
      {collaborativeRecommendations && collaborativeRecommendations.length > 0 && (
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">🎯 Recommended Merchants</h3>
          <p className="text-sm text-gray-600 mb-4">
            Merchants that users with similar spending habits also visit
          </p>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Merchant</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Relevance Score</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {collaborativeRecommendations.slice(0, 10).map((rec: any, idx: number) => (
                  <tr key={idx}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {rec.merchantName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                        {rec.score}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Best Card Benefits by Category */}
      <div className="bg-white p-6 rounded-lg shadow-sm border">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">🏆 Best Card Benefits by Category</h3>
        <div className="flex flex-col md:flex-row md:items-end gap-4 mb-4">
          <div>
            <label className="text-sm text-gray-600">Category (MCC)</label>
            <select
              className="mt-1 block w-full md:w-64 border rounded-md px-3 py-2"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              {CATEGORY_OPTIONS.map((opt) => (
                <option key={opt.code} value={opt.code}>
                  {opt.code} — {opt.name}
                </option>
              ))}
            </select>
          </div>
          <button
            onClick={loadBenefits}
            className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            {loadingBenefits ? 'Loading…' : 'Find Best Card'}
          </button>
        </div>

        {bestCardBenefits.length === 0 ? (
          <div className="text-sm text-gray-500">No reward programs found for the selected category.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Card</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Program</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Reward Rate</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cap</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Conditions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {bestCardBenefits.map((b: any, idx: number) => (
                  <tr key={idx}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{b.card}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{b.program}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{(b.rate * 100).toFixed(1)}%</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{b.cap ?? '—'}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{b.conditions || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Optimal Purchase Time */}
      {optimalPurchaseTime && optimalPurchaseTime.length > 0 && (
        <div className="bg-white p-6 rounded-lg shadow-sm border">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">⏰ Your Best Shopping Times</h3>
          <p className="text-sm text-gray-600 mb-4">
            When you typically get better deals or shop more efficiently
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {optimalPurchaseTime.slice(0, 3).map((time: any, idx: number) => (
              <div key={idx} className="bg-gradient-to-br from-indigo-50 to-purple-50 p-4 rounded-lg border border-indigo-200">
                <p className="text-sm font-medium text-gray-500">
                  {time.dayOfWeek} at {time.hour}:00
                </p>
                <p className="text-xl font-bold text-indigo-600 mt-2">
                  {time.transactionCount} transactions
                </p>
                <p className="text-sm text-gray-600 mt-1">
                  Avg: ${time.avgAmount?.toFixed(2) || '0.00'}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recommendations */}
      <div className="bg-white p-6 rounded-lg shadow-sm border">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">💡 Recommendations</h3>
        <ul className="space-y-3">
          <li className="flex items-start">
            <span className="text-blue-500 mr-2">•</span>
            <span className="text-gray-700">
              Consider using contactless payments more often for faster checkout and added security
            </span>
          </li>
          {topCategory && (
            <li className="flex items-start">
              <span className="text-blue-500 mr-2">•</span>
              <span className="text-gray-700">
                Review your spending in {topCategory.categoryName} to identify potential savings
              </span>
            </li>
          )}
          {recurringExpenses && recurringExpenses.length > 0 && (
            <li className="flex items-start">
              <span className="text-blue-500 mr-2">•</span>
              <span className="text-gray-700">
                You have {recurringExpenses.length} recurring expense(s). Consider setting up automatic payment alerts for these merchants.
              </span>
            </li>
          )}
          {collaborativeRecommendations && collaborativeRecommendations.length > 0 && (
            <li className="flex items-start">
              <span className="text-blue-500 mr-2">•</span>
              <span className="text-gray-700">
                Based on similar users, we found {collaborativeRecommendations.length} new merchants you might be interested in.
              </span>
            </li>
          )}
          {optimalPurchaseTime && optimalPurchaseTime.length > 0 && (
            <li className="flex items-start">
              <span className="text-blue-500 mr-2">•</span>
              <span className="text-gray-700">
                Your optimal shopping time is {optimalPurchaseTime[0]?.dayOfWeek} at {optimalPurchaseTime[0]?.hour}:00 - consider scheduling regular purchases then.
              </span>
            </li>
          )}
          <li className="flex items-start">
            <span className="text-blue-500 mr-2">•</span>
            <span className="text-gray-700">
              Set up alerts for large transactions to monitor your spending habits
            </span>
          </li>
        </ul>
      </div>
    </div>
  )
}
