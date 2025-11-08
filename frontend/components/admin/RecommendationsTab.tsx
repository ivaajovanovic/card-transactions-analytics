'use client'

import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { apiClient } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

interface SpendGroup {
  groupKey: string
  totalAmount: number
  txnCount: number
}

interface TopMerchant {
  merchantId: string
  merchantName?: string
  category?: string
  totalAmount: number
  txnCount: number
}

export function RecommendationsTab() {
  const [userId, setUserId] = useState<string>('USR00179')
  const [from, setFrom] = useState<string>(new Date(Date.now() - 1000 * 60 * 60 * 24 * 90).toISOString()) // last 90d
  const [to, setTo] = useState<string>(new Date().toISOString())

  const { data: topMerchants = [] } = useQuery<TopMerchant[]>({
    queryKey: ['admin-reco-top-merchants', userId, from, to],
    queryFn: () => apiClient.getUserTopMerchants(userId, from, to, 10),
    enabled: !!userId,
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })



  const { data: channelMix = [] } = useQuery<SpendGroup[]>({
    queryKey: ['admin-reco-channel-mix', userId, from, to],
    queryFn: () => apiClient.getUserChannelMix(userId, from, to),
    enabled: !!userId,
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  const { data: timeOfDay = [] } = useQuery<SpendGroup[]>({
    queryKey: ['admin-reco-time-of-day', userId, from, to],
    queryFn: () => apiClient.getUserTimeOfDay(userId, from, to),
    enabled: !!userId,
    refetchInterval: 15000,
    refetchOnWindowFocus: true,
  })

  const channelColors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6',"#00b8d9", // cyan
  "#ff6f61"]

    const allowedChannels = ["APP", "ATM", "IN_STORE", "MOBILE", "ONLINE", "POS", "WEB"];
const filteredChannelMix = channelMix.filter(c => allowedChannels.includes(c.groupKey));

  return (
    <div className="space-y-6">
      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
          <CardDescription>Select user and time window</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-4 items-end">
            <div>
              <label className="text-sm font-medium">User External ID</label>
              <input
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="e.g. user-123"
                className="mt-1 w-full rounded-md border px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="text-sm font-medium">From</label>
              <input
                type="datetime-local"
                value={new Date(from).toISOString().slice(0,16)}
                onChange={(e) => setFrom(new Date(e.target.value).toISOString())}
                className="mt-1 w-full rounded-md border px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="text-sm font-medium">To</label>
              <input
                type="datetime-local"
                value={new Date(to).toISOString().slice(0,16)}
                onChange={(e) => setTo(new Date(e.target.value).toISOString())}
                className="mt-1 w-full rounded-md border px-3 py-2 text-sm"
              />
            </div>
            <div>
              <Button onClick={() => { /* triggers react-query by changing state */ }}>Refresh</Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Top Merchants */}
      <Card>
        <CardHeader>
          <CardTitle>Top Merchants</CardTitle>
          <CardDescription>Most frequent merchants for the selected user</CardDescription>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={topMerchants.map(m => ({
              merchant: m.merchantName || m.merchantId,
              amount: m.totalAmount,
              count: m.txnCount
            }))}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="merchant" interval={0} angle={-30} textAnchor="end" height={80} />
              <YAxis />
              <Tooltip />
              <Legend />
              <Bar dataKey="count" fill="#8b5cf6" name="Transactions" />
              <Bar dataKey="amount" fill="#10b981" name="Amount ($)" />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Channel Mix */}
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Channel Mix</CardTitle>
            <CardDescription>Distribution by channel</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
  data={filteredChannelMix.map((c) => ({ name: c.groupKey, value: c.txnCount }))}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                >
                  {filteredChannelMix.map((_, idx) => (
                    <Cell key={idx} fill={channelColors[idx % channelColors.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Optimal Time of Day */}
        <Card>
          <CardHeader>
            <CardTitle>Optimal Time of Day</CardTitle>
            <CardDescription>Hours with the highest activity</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={timeOfDay.map(t => ({ hour: t.groupKey, count: t.txnCount }))}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="hour" 
                  interval={0}
                  tick={{ fontSize: 11 }}
                  angle={0}
                />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="count" fill="#3b82f6" name="Transactions" />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
