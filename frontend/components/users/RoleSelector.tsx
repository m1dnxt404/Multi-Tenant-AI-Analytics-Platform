'use client'

import type { Role } from '@/types'

const ROLES: { value: Role; label: string; description: string }[] = [
  { value: 'VIEWER', label: 'Viewer', description: 'Read-only access' },
  { value: 'MEMBER', label: 'Member', description: 'Upload datasets, generate insights' },
  { value: 'ADMIN', label: 'Admin', description: 'Invite users, manage datasets' },
  { value: 'OWNER', label: 'Owner', description: 'Full access' },
]

interface RoleSelectorProps {
  value: Role
  onChange: (role: Role) => void
  disabled?: boolean
  exclude?: Role[]
}

export function RoleSelector({ value, onChange, disabled, exclude = [] }: RoleSelectorProps) {
  const options = ROLES.filter((r) => !exclude.includes(r.value))

  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value as Role)}
      disabled={disabled}
      className="w-full rounded-md border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
    >
      {options.map((r) => (
        <option key={r.value} value={r.value}>
          {r.label} — {r.description}
        </option>
      ))}
    </select>
  )
}
