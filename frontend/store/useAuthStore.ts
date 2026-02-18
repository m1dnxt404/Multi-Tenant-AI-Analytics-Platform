'use client'

import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import type { AuthResponse, Organization, Role } from '@/types'

interface AuthUser {
  id: string
  email: string
  fullName: string
  role: Role
}

interface AuthStore {
  user: AuthUser | null
  organization: Organization | null
  isAuthenticated: boolean
  setAuth: (auth: AuthResponse) => void
  clearAuth: () => void
  hasRole: (roles: Role[]) => boolean
  canEdit: () => boolean
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      user: null,
      organization: null,
      isAuthenticated: false,

      setAuth: (auth: AuthResponse) =>
        set({
          user: {
            id: auth.userId,
            email: auth.email,
            fullName: auth.fullName,
            role: auth.role,
          },
          organization: auth.organization,
          isAuthenticated: true,
        }),

      clearAuth: () =>
        set({ user: null, organization: null, isAuthenticated: false }),

      hasRole: (roles: Role[]) => {
        const { user } = get()
        return user ? roles.includes(user.role) : false
      },

      canEdit: () => {
        const { user } = get()
        return user ? ['OWNER', 'ADMIN', 'MEMBER'].includes(user.role) : false
      },
    }),
    {
      name: 'analytics-auth',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
)
