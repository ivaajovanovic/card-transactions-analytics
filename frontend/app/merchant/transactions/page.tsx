'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { getCurrentUser, logout } from '@/lib/auth'
import { apiClient } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { ArrowLeft, LogOut, CreditCard, User, Calendar, DollarSign } from 'lucide-react'

interface Transaction {
  transactionId: string
  amount: number
  timestamp: string
  paymentType: string
  channel: string
  contactless: boolean
  status: string
  cardNumber: string
  cardNetwork: string
  userName: string
  userEmail: string
}

export default function MerchantTransactions() {
  const router = useRouter()
  const [user, setUser] = useState<any>(null)
  const [merchantEmail, setMerchantEmail] = useState<string>('')
  const [loading, setLoading] = useState(true)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [limit] = useState(50)
  const [offset, setOffset] = useState(0)

  useEffect(() => {
    const currentUser = getCurrentUser()
    setUser(currentUser)
    
    if (!currentUser || currentUser.role !== 'merchant') {
      router.push('/login')
      return
    }
    
    const email = localStorage.getItem('userEmail')
    if (email) {
      setMerchantEmail(email)
      loadTransactions(email, limit, offset)
    } else {
      router.push('/login')
    }
  }, [router, offset])

  const loadTransactions = async (email: string, limit: number, offset: number) => {
    setLoading(true)
    try {
      const data = await apiClient.getMerchantTransactionsByEmail(email, limit, offset)
      setTransactions(data)
    } catch (error) {
      console.error('Failed to load transactions:', error)
    } finally {
      setLoading(false)
    }
  }

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'text-green-600 bg-green-50'
      case 'PENDING': return 'text-yellow-600 bg-yellow-50'
      case 'FAILED': return 'text-red-600 bg-red-50'
      default: return 'text-gray-600 bg-gray-50'
    }
  }

  if (!user || loading) return (
    <div className="flex items-center justify-center min-h-screen">
      <p className="text-lg">Loading...</p>
    </div>
  )

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b bg-white shadow-sm">
        <div className="container mx-auto flex items-center justify-between px-4 py-4">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="sm" onClick={() => router.push('/merchant/dashboard')}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back
            </Button>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
              <p className="text-sm text-gray-600">{user.name}</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <p className="text-sm font-medium">{user.email}</p>
            </div>
            <Button variant="outline" size="sm" onClick={logout}>
              <LogOut className="mr-2 h-4 w-4" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="container mx-auto p-6">
        <Card>
          <CardHeader>
            <CardTitle>Recent Transactions ({transactions.length})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {transactions.map((tx, index) => (
                <div key={index} className="border rounded-lg p-4 hover:bg-gray-50 transition-colors">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-4 flex-1">
                      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-blue-50">
                        <CreditCard className="h-6 w-6 text-blue-600" />
                      </div>
                      
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <span className="font-semibold text-lg">${tx.amount.toFixed(2)}</span>
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(tx.status)}`}>
                            {tx.status}
                          </span>
                          {tx.contactless && (
                            <span className="px-2 py-1 rounded-full text-xs font-medium bg-purple-50 text-purple-600">
                              Contactless
                            </span>
                          )}
                        </div>
                        
                        <div className="grid grid-cols-2 gap-4 text-sm">
                          <div className="flex items-center gap-2 text-gray-600">
                            <User className="h-4 w-4" />
                            <div>
                              <p className="font-medium text-gray-900">{tx.userName}</p>
                              <p className="text-xs">{tx.userEmail}</p>
                            </div>
                          </div>
                          
                          <div className="flex items-center gap-2 text-gray-600">
                            <CreditCard className="h-4 w-4" />
                            <div>
                              <p className="font-medium text-gray-900">{tx.cardNetwork}</p>
                              <p className="text-xs">**** {tx.cardNumber ? tx.cardNumber.slice(-4) : 'XXXX'}</p>
                            </div>
                          </div>
                          
                          <div className="flex items-center gap-2 text-gray-600">
                            <Calendar className="h-4 w-4" />
                            <p>{formatDate(tx.timestamp)}</p>
                          </div>
                          
                          <div className="flex items-center gap-2 text-gray-600">
                            <DollarSign className="h-4 w-4" />
                            <p>{tx.paymentType} • {tx.channel}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <div className="text-right text-xs text-gray-500">
                      ID: {tx.transactionId}
                    </div>
                  </div>
                </div>
              ))}
            </div>
            
            {transactions.length === 0 && (
              <div className="text-center py-12 text-gray-500">
                No transactions found
              </div>
            )}
            
            <div className="flex justify-between items-center mt-6 pt-4 border-t">
              <Button
                variant="outline"
                disabled={offset === 0}
                onClick={() => setOffset(Math.max(0, offset - limit))}
              >
                Previous
              </Button>
              <span className="text-sm text-gray-600">
                Showing {offset + 1} - {offset + transactions.length}
              </span>
              <Button
                variant="outline"
                disabled={transactions.length < limit}
                onClick={() => setOffset(offset + limit)}
              >
                Next
              </Button>
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  )
}
