export type UserRole = 'admin' | 'user' | 'merchant'

export interface User {
  id: string
  role: UserRole
  name?: string
  email?: string
}

export function getCurrentUser(): User | null {
  if (typeof window === 'undefined') return null
  
  const role = localStorage.getItem('userRole') as UserRole | null
  const id = localStorage.getItem('userId')
  const name = localStorage.getItem('userName') || undefined
  const email = localStorage.getItem('userEmail') || undefined
  
  if (!role || !id) return null
  
  return {
    id,
    role,
    name: name || (role === 'admin' ? 'Admin User' : role === 'user' ? 'User' : 'Merchant'),
    email: email || (role === 'admin' ? 'admin@example.com' : undefined),
  }
}

export function logout() {
  localStorage.removeItem('userRole')
  localStorage.removeItem('userId')
  localStorage.removeItem('userName')
  localStorage.removeItem('userEmail')
  window.location.href = '/login'
}

export function isAuthenticated(): boolean {
  return !!getCurrentUser()
}
