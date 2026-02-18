'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import {
  LayoutDashboard,
  Database,
  Sparkles,
  Users,
  Settings,
  LogOut,
  BarChart2,
} from 'lucide-react'
import { cn, getInitials } from '@/lib/utils'
import { useAuthStore } from '@/store/useAuthStore'
import { api } from '@/lib/api'

const navItems = [
  { href: '/',          label: 'Dashboard',   icon: LayoutDashboard, roles: ['OWNER','ADMIN','MEMBER','VIEWER'] },
  { href: '/datasets',  label: 'Datasets',    icon: Database,        roles: ['OWNER','ADMIN','MEMBER','VIEWER'] },
  { href: '/insights',  label: 'AI Insights', icon: Sparkles,        roles: ['OWNER','ADMIN','MEMBER','VIEWER'] },
  { href: '/users',     label: 'Users',       icon: Users,           roles: ['OWNER','ADMIN'] },
  { href: '/settings',  label: 'Settings',    icon: Settings,        roles: ['OWNER','ADMIN'] },
]

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()
  const { user, organization, clearAuth, hasRole } = useAuthStore()

  async function handleLogout() {
    try { await api.auth.logout() } catch { /* ignore */ }
    clearAuth()
    router.push('/login')
  }

  return (
    <aside className="w-60 shrink-0 h-screen flex flex-col bg-gray-50 border-r border-gray-200">
      {/* Logo */}
      <div className="h-14 flex items-center px-4 border-b border-gray-200">
        <div className="flex items-center gap-2">
          <BarChart2 className="h-5 w-5 text-gray-700" />
          <span className="font-semibold text-gray-900 text-sm truncate">
            {organization?.name ?? 'Analytics'}
          </span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-2 space-y-0.5 overflow-y-auto">
        {navItems
          .filter((item) => hasRole(item.roles as any))
          .map((item) => {
            const active = item.href === '/'
              ? pathname === '/'
              : pathname.startsWith(item.href)

            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  'flex items-center gap-2.5 px-3 py-2 rounded-md text-sm transition-colors',
                  active
                    ? 'bg-white border border-gray-200 text-gray-900 font-medium'
                    : 'text-gray-600 hover:bg-white hover:text-gray-900'
                )}
              >
                <item.icon className="h-4 w-4 shrink-0" />
                {item.label}
              </Link>
            )
          })}
      </nav>

      {/* User footer */}
      <div className="p-3 border-t border-gray-200">
        <div className="flex items-center gap-2.5 mb-2">
          <div className="h-7 w-7 rounded-full bg-gray-200 flex items-center justify-center text-xs font-medium text-gray-700 shrink-0">
            {user ? getInitials(user.fullName) : '?'}
          </div>
          <div className="min-w-0">
            <p className="text-xs font-medium text-gray-900 truncate">{user?.fullName}</p>
            <p className="text-xs text-gray-500 truncate">{user?.email}</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-xs text-gray-500 hover:text-gray-900 transition-colors px-1 py-1 w-full rounded"
        >
          <LogOut className="h-3.5 w-3.5" />
          Sign out
        </button>
      </div>
    </aside>
  )
}
