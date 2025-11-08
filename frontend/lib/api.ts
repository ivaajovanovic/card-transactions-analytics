const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:9001'

export class ApiClient {
  async getUserTransactionsByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/transactions`)
  }
  private baseUrl: string

  constructor() {
    this.baseUrl = API_BASE_URL
  }

  private async request<T>(endpoint: string, options?: RequestInit): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`
    
    try {
      const response = await fetch(url, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...options?.headers,
        },
      })

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      return await response.json()
    } catch (error) {
      console.error('API request failed:', error)
      throw error
    }
  }

  // User endpoints
    async loginByEmail(email: string, password: string) {
      const qp = new URLSearchParams({ email, password }).toString()
      return this.request<any>(`/api/users/login?${qp}`)
    }
    async loginMerchantByEmail(email: string, password: string) {
      const qp = new URLSearchParams({ email, password }).toString()
      return this.request<any>(`/api/merchants/login?${qp}`)
    }

    async loginAdminByEmail(email: string, password: string) {
      const qp = new URLSearchParams({ email, password }).toString()
      return this.request<any>(`/api/admin/login?${qp}`)
    }

  async getUser(externalId: string) {
    return this.request(`/users/external/${externalId}`)
  }

  async getUsersByDemographics(params: {
    incomeLevel?: string
    lifestage?: string
    occupation?: string
  }) {
    const queryParams = new URLSearchParams(params as any).toString()
    return this.request(`/users/demographics?${queryParams}`)
  }

  async getUserTravelPattern(externalId: string) {
    return this.request(`/users/external/${externalId}/travel-pattern`)
  }

  async getFrequentTravelers() {
    return this.request('/users/frequent-travelers')
  }

  // Merchant endpoints
  async getMerchant(externalId: string) {
    return this.request(`/merchants/external/${externalId}`)
  }

  async getMerchantsByLocation(city: string) {
    return this.request(`/merchants/location/city/${city}`)
  }

  async getMerchantSequences(merchantId: string, minCount: number = 5) {
    return this.request(`/merchants/external/${merchantId}/sequences?minCount=${minCount}`)
  }

  async getCrossRegionMerchants() {
    return this.request('/merchants/cross-region')
  }

  // User Preference endpoints
  async getUserPreference(userId: string) {
    return this.request(`/user-preferences/user/${userId}`)
  }

  async getHighValueUsers(minSpend: number = 1000) {
    return this.request(`/user-preferences/high-value?minSpend=${minSpend}`)
  }

  async getSimilarUsers(userId: string, limit: number = 10) {
    return this.request(`/user-preferences/similar/${userId}?limit=${limit}`)
  }

  async getTopLoyaltyUsers(tier: string, limit: number = 10) {
    return this.request(`/user-preferences/loyalty/top/${tier}?limit=${limit}`)
  }

  // Transaction endpoints
  async getTransactions(params?: { limit?: number; userId?: string }) {
    const queryParams = params ? `?${new URLSearchParams(params as any).toString()}` : ''
    return this.request(`/transactions${queryParams}`)
  }

  async getTransactionById(id: string) {
    return this.request(`/transactions/${id}`)
  }

  // Basic lists (fallbacks when analytics are empty)
  async listUsers(): Promise<any[]> {
    return this.request('/api/users')
  }

  async listMerchants(): Promise<any[]> {
    return this.request('/api/merchants')
  }

  // Analytics endpoints (mock data for demo)
  // Analytics endpoints - Dashboard KPIs
  async getKPIMetrics(): Promise<any> {
    return this.request('/api/analytics/admin/kpis')
  }

  async getUserSegments(): Promise<any[]> {
    return this.request('/api/analytics/admin/segments')
  }

  async getTopCategories(): Promise<any[]> {
    return this.request('/api/analytics/admin/categories')
  }

  async getTopLocations(): Promise<any[]> {
    return this.request('/api/analytics/admin/locations')
  }

  async getTransactionTrends(fromDate?: string, toDate?: string): Promise<any[]> {
    const params = new URLSearchParams()
    if (fromDate) params.append('fromDate', fromDate)
    if (toDate) params.append('toDate', toDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return this.request(`/api/analytics/admin/trends${query}`)
  }

  async getFailureReasons(from: string, to: string): Promise<any[]> {
    return this.request(`/api/analytics/admin/failure-reasons?from=${from}&to=${to}`)
  }

  async getFailedByMerchant(from: string, to: string, limit: number = 10): Promise<any[]> {
    return this.request(`/api/analytics/admin/failed-by-merchant?from=${from}&to=${to}&limit=${limit}`)
  }

  async getCardTypeFailures(from: string, to: string): Promise<any[]> {
    return this.request(`/api/analytics/admin/card-type-failures?from=${from}&to=${to}`)
  }

  async getAcceptanceCoverage(): Promise<any[]> {
    return this.request(`/api/analytics/admin/acceptance-coverage`)
  }

  async getAcceptanceGaps(from: string, to: string, limit: number = 10): Promise<any[]> {
    return this.request(`/api/analytics/admin/acceptance-gaps?from=${from}&to=${to}&limit=${limit}`)
  }

  async getUserSpendAnalytics(userId: string, from: string, to: string, groupBy: string = 'category'): Promise<any[]> {
    return this.request(`/api/analytics/users/${userId}/spend?from=${from}&to=${to}&groupBy=${groupBy}`)
  }

  async getUserTopMerchants(userId: string, from: string, to: string, limit: number = 10): Promise<any[]> {
    return this.request(`/api/analytics/users/${userId}/top-merchants?from=${from}&to=${to}&limit=${limit}`)
  }

  async getUserChannelMix(userId: string, from: string, to: string): Promise<any[]> {
    return this.request(`/api/analytics/users/${userId}/channel-mix?from=${from}&to=${to}`)
  }

  async getUserTimeOfDay(userId: string, from: string, to: string): Promise<any[]> {
    return this.request(`/api/analytics/users/${userId}/time-of-day?from=${from}&to=${to}`)
  }

  // User-specific analytics (ID-based - deprecated)
  async getUserSpendingByCategory(userId: number): Promise<any[]> {
    return this.request(`/api/users/${userId}/analytics/spending-by-category`)
  }

  async getUserTopMerchantsList(userId: number, limit: number = 10): Promise<any[]> {
    return this.request(`/api/users/${userId}/analytics/top-merchants?limit=${limit}`)
  }

  async getUserCardUsage(userId: number): Promise<any[]> {
    return this.request(`/api/users/${userId}/analytics/card-usage`)
  }

  async getUserMonthlySpending(userId: number): Promise<any[]> {
    return this.request(`/api/users/${userId}/analytics/monthly-spending`)
  }

  // User-specific analytics (email-based - preferred)
  async getUserSpendingByCategoryByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/analytics/spending-by-category`)
  }

  async getUserTopMerchantsListByEmail(email: string, limit: number = 10): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/analytics/top-merchants?limit=${limit}`)
  }

  async getUserCardUsageByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/analytics/card-usage`)
  }

  async getUserMonthlySpendingByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/analytics/monthly-spending`)
  }

  async getUserRecurringExpensesByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/analytics/recurring-expenses`)
  }

  async getUserCollaborativeRecommendationsByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/recommendations/collaborative`)
  }

  async getUserOptimalPurchaseTimeByEmail(email: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/analytics/optimal-purchase-time`)
  }

  async getUserBestCardBenefitsByEmail(email: string, categoryCode: string): Promise<any[]> {
    return this.request(`/api/users/by-email/${encodeURIComponent(email)}/best-card-benefits/${encodeURIComponent(categoryCode)}`)
  }

  // Merchant-specific analytics (email-based)
  async getMerchantOverviewByEmail(email: string): Promise<any> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/overview`)
  }

  async getMerchantCardNetworksByEmail(email: string): Promise<any[]> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/card-networks`)
  }

  async getMerchantPaymentTypesByEmail(email: string): Promise<any[]> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/payment-types`)
  }

  async getMerchantMonthlyRevenueByEmail(email: string): Promise<any[]> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/monthly-revenue`)
  }

  async getMerchantTopCustomersByEmail(email: string, limit: number = 10): Promise<any[]> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/top-customers?limit=${limit}`)
  }

  async getMerchantTransactionChannelsByEmail(email: string): Promise<any[]> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/transaction-channels`)
  }

  async getMerchantHourlyDistributionByEmail(email: string): Promise<any[]> {
    return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/analytics/hourly-distribution`)
  }

  async getMerchantTransactionsByEmail(email: string, limit: number = 50, offset: number = 0): Promise<any[]> {
  return this.request(`/api/merchants/by-email/${encodeURIComponent(email)}/transactions`)
  }

  async getAdminSuspiciousTransactions(minMultiplier: number = 2.5): Promise<any[]> {
  return this.request(`/api/admin/anomalies/suspicious-transactions?minMultiplier=${minMultiplier}`)
}

async getAllUsers(): Promise<any[]> {
  return this.request('/api/admin/users')
}

async getPurchaseProbability(merchantId: string, limit: number = 20): Promise<any[]> {
  return this.request(`/api/admin/predictions/merchant-purchase-probability/${merchantId}?limit=${limit}`)
}

async getRegionalPerformance(): Promise<any[]> {
  return this.request('/api/admin/analytics/regional-performance')
}

async getShoppingAffinity(): Promise<any[]> {
  return this.request('/api/admin/analytics/shopping-affinity')
}

async getCrossRegionPatterns(limit: number = 10): Promise<any[]> {
  return this.request(`/api/admin/analytics/cross-region-patterns?limit=${limit}`)
}

async getAdminOptimalPurchaseTime(): Promise<any[]> {
  return this.request('/api/admin/analytics/optimal-purchase-time')
}

async listAllMerchants() : Promise<any[]> {
  return this.request('/api/merchants/all')
}


}

export const apiClient = new ApiClient()
