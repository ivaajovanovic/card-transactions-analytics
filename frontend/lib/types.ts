// Enums
export type IncomeLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'PREMIUM'
export type Lifestage = 'STUDENT' | 'YOUNG_PROFESSIONAL' | 'FAMILY' | 'ESTABLISHED_PROFESSIONAL' | 'RETIRED'
export type PriceRange = 'BUDGET' | 'MODERATE' | 'UPSCALE' | 'LUXURY'
export type LoyaltyTier = 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM'
export type TimeOfDay = 'EARLY_MORNING' | 'MORNING' | 'AFTERNOON' | 'EVENING' | 'NIGHT'
export type SeasonName = 'SPRING' | 'SUMMER' | 'FALL' | 'WINTER'

// User Node
export interface UserNode {
  id?: number
  externalUserId: string
  name: string
  email?: string
  age?: number
  gender?: string
  occupation?: string
  incomeLevel?: IncomeLevel
  lifestage?: Lifestage
  isFrequentTraveler?: boolean
  memberSince?: string
  homeCity?: string
  homeCountry?: string
}

// Merchant Node
export interface MerchantNode {
  id?: number
  externalMerchantId: string
  name: string
  category?: string
  city?: string
  country?: string
  tags?: string[]
  avgTicketSize?: number
  priceRange?: PriceRange
  popularityScore?: number
}

// User Preference Node
export interface UserPreferenceNode {
  id?: number
  userExternalId: string
  preferredCategories?: string[]
  favoriteTimeSlots?: TimeOfDay[]
  avgMonthlySpend?: number
  txnFrequency?: string
  loyaltyTier?: LoyaltyTier
  loyaltyPoints?: number
  favoriteMerchantIds?: string[]
  avoidedMerchantIds?: string[]
  preferredChannels?: string[]
}

// Transaction
export interface Transaction {
  id?: number
  txnId: string
  amount: number
  currency?: string
  timestamp: string
  merchantName?: string
  merchantCategory?: string
  location?: string
  status?: string
}

// Time Bucket
export interface TimeBucket {
  id?: number
  bucketType: string
  bucketValue: string
  dayOfWeek?: string
  timeOfDay?: TimeOfDay
  isWeekend?: boolean
  isHoliday?: boolean
  seasonName?: SeasonName
}

// Analytics Response Types
export interface UserDemographics {
  occupation?: string
  incomeLevel?: IncomeLevel
  lifestage?: Lifestage
  homeCity?: string
  homeCountry?: string
}

export interface TravelPattern {
  userId: string
  homeCity?: string
  homeCountry?: string
  visitedCities: string[]
  visitedCountries: string[]
  transactionCount: number
  isFrequentTraveler: boolean
}

export interface MerchantSequence {
  firstMerchant: string
  secondMerchant: string
  sequenceCount: number
  avgTimeDelta?: number
  confidenceScore?: number
}

export interface SpendingAnalytics {
  totalSpent: number
  avgTransaction: number
  transactionCount: number
  topCategories: Array<{
    category: string
    amount: number
    count: number
  }>
  monthlyTrend: Array<{
    month: string
    amount: number
  }>
}

export interface AdminKPIResponse {
  totalUsers: number
  totalMerchants: number
  totalTransactions: number
  totalVolume: number
  userGrowth: number
  merchantGrowth: number
  transactionGrowth: number
  volumeGrowth: number
  avgTransactionAmount: number
}

export interface UserSegmentDTO {
  name: string
  value: number
  percentage: number
}

export interface CategoryStatsDTO {
  category: string
  transactions: number
  amount: number
}

export interface LocationStatsDTO {
  location: string
  users: number
  transactions: number
  volume: number
}

export interface TransactionTrendDTO {
  month: string
  transactions: number
  volume: number
}

// Legacy type for backwards compatibility
export interface KPIMetrics {
  totalUsers: number
  totalMerchants: number
  totalTransactions: number
  totalVolume: number
  avgTransactionValue: number
  activeUsersToday: number
}
