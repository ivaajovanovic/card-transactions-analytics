'use client'

import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { TrendingUp, DollarSign } from 'lucide-react'

interface TrendData {
  month: string
  transactions: number
  volume: number
}

export function TrendsTab() {
  // Default: Last 12 months (from start of 2024 to end of 2025)
  const defaultFromDate = '2024-01-01T00:00:00Z'
  const defaultToDate = '2025-12-31T23:59:59Z'

  const { data: trendsData, isLoading, error } = useQuery({
    queryKey: ['admin-trends', defaultFromDate, defaultToDate],
    queryFn: () => apiClient.getTransactionTrends(defaultFromDate, defaultToDate),
    // Real-time-ish updates every 15s
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Transaction Trends</CardTitle>
            <CardDescription>Loading trends data...</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[400px] flex items-center justify-center">
              <div className="animate-pulse text-gray-400">Loading chart...</div>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Transaction Trends</CardTitle>
            <CardDescription className="text-red-500">
              Failed to load trends data
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[400px] flex items-center justify-center">
              <p className="text-gray-500">Error loading trends. Please try again.</p>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  const trends: TrendData[] = trendsData || []

  if (trends.length === 0) {
    return (
      <div className="space-y-4">
        <Card>
          <CardHeader>
            <CardTitle>Transaction Trends</CardTitle>
            <CardDescription>No trend data available for the selected period</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[400px] flex items-center justify-center">
              <p className="text-gray-500">No transactions found in the database.</p>
            </div>
          </CardContent>
        </Card>
      </div>
    )
  }

  // Calculate summary stats (for insights, not repeating KPIs)
  const totalTransactions = trends.reduce((sum, t) => sum + t.transactions, 0)
  const totalVolume = trends.reduce((sum, t) => sum + t.volume, 0)
  const avgTicketSize = totalTransactions > 0 ? totalVolume / totalTransactions : 0

  // Peak months
  const peakTxn = trends.reduce((max, t) => (t.transactions > max.transactions ? t : max), trends[0])
  const peakVol = trends.reduce((max, t) => (t.volume > max.volume ? t : max), trends[0])

  // Month-over-month change for the last month (transactions)
  const sorted = [...trends].sort((a, b) => a.month.localeCompare(b.month))
  const last = sorted[sorted.length - 1]
  const prev = sorted[sorted.length - 2]
  const lastMonthMoM = prev && prev.transactions > 0
    ? ((last.transactions - prev.transactions) / prev.transactions) * 100
    : 0

  // Helper: format YYYY-MM -> 'MMM YYYY'
  const fmtMonth = (m: string) => {
    try {
      const d = new Date(`${m}-01T00:00:00Z`)
      return d.toLocaleDateString(undefined, { month: 'short', year: 'numeric' })
    } catch {
      return m
    }
  }

  // Format data for chart
  const chartData = trends.map(item => ({
    month: item.month,
    transactions: item.transactions,
    volume: Math.round(item.volume * 100) / 100, // Round to 2 decimals
  }))

  return (
    <div className="space-y-6">
      {/* Insight Cards (no duplication of top KPIs) */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average spend per transaction</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${avgTicketSize.toFixed(2)}</div>
            <p className="text-xs text-muted-foreground">Average amount per transaction</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Busiest month (transactions)</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{peakTxn.transactions.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">{fmtMonth(peakTxn.month)}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Highest spending month</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">${(peakVol.volume).toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">{fmtMonth(peakVol.month)}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Change vs previous month</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className={`text-2xl font-bold ${lastMonthMoM >= 0 ? 'text-green-600' : 'text-red-600'}`}>
              {`${lastMonthMoM >= 0 ? '+' : ''}${lastMonthMoM.toFixed(1)}%`}
            </div>
            <p className="text-xs text-muted-foreground">{prev ? `${fmtMonth(prev.month)} → ${fmtMonth(last.month)}` : 'N/A'}</p>
          </CardContent>
        </Card>
      </div>

      {/* Transaction Count Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Transaction Count Over Time</CardTitle>
          <CardDescription>Monthly transaction volume</CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={350}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis 
                dataKey="month" 
                tick={{ fontSize: 12 }}
                angle={-45}
                textAnchor="end"
                height={80}
              />
              <YAxis 
                tick={{ fontSize: 12 }}
                label={{ value: 'Transactions', angle: -90, position: 'insideLeft' }}
              />
              <Tooltip 
                formatter={(value: number) => value.toLocaleString()}
                labelStyle={{ color: '#000' }}
              />
              <Legend />
              <Line 
                type="monotone" 
                dataKey="transactions" 
                stroke="#8884d8" 
                strokeWidth={2}
                dot={{ r: 4 }}
                activeDot={{ r: 6 }}
                name="Transactions"
              />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Transaction Volume Chart */}
      <Card>
        <CardHeader>
          <CardTitle>Transaction Volume Over Time</CardTitle>
          <CardDescription>Monthly dollar volume (USD)</CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={350}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis 
                dataKey="month" 
                tick={{ fontSize: 12 }}
                angle={-45}
                textAnchor="end"
                height={80}
              />
              <YAxis 
                tick={{ fontSize: 12 }}
                label={{ value: 'Volume ($)', angle: -90, position: 'insideLeft' }}
                tickFormatter={(value) => `$${(value / 1_000_000).toFixed(1)}M`}
              />
              <Tooltip 
                formatter={(value: number) => `$${value.toLocaleString()}`}
                labelStyle={{ color: '#000' }}
              />
              <Legend />
              <Line 
                type="monotone" 
                dataKey="volume" 
                stroke="#82ca9d" 
                strokeWidth={2}
                dot={{ r: 4 }}
                activeDot={{ r: 6 }}
                name="Volume ($)"
              />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </div>
  )
}
