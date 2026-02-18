'use client'

import { useAuthStore } from '@/store/useAuthStore'
import { roleBadgeColor } from '@/lib/utils'

interface HeaderProps {
  title: string
  description?: string
  action?: React.ReactNode
}

export function Header({ title, description, action }: HeaderProps) {
  const user = useAuthStore((s) => s.user)

  return (
    <div className="h-14 border-b border-gray-200 bg-white px-6 flex items-center justify-between shrink-0">
      <div>
        <h1 className="text-sm font-semibold text-gray-900">{title}</h1>
        {description && <p className="text-xs text-gray-500">{description}</p>}
      </div>
      <div className="flex items-center gap-3">
        {user && (
          <span
            className={`text-xs font-medium px-2 py-0.5 rounded-full ${roleBadgeColor(user.role)}`}
          >
            {user.role}
          </span>
        )}
        {action}
      </div>
    </div>
  )
}
