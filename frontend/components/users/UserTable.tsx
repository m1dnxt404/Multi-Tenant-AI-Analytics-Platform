'use client'

import { useState } from 'react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { RoleSelector } from './RoleSelector'
import { api } from '@/lib/api'
import { formatDateTime } from '@/lib/utils'
import { useAuthStore } from '@/store/useAuthStore'
import type { UserMember, Role } from '@/types'

interface UserTableProps {
  users: UserMember[]
  onChanged: () => void
}

const ROLE_COLORS: Record<Role, string> = {
  OWNER: 'bg-gray-900 text-white',
  ADMIN: 'bg-gray-700 text-white',
  MEMBER: 'bg-gray-100 text-gray-700',
  VIEWER: 'bg-gray-50 text-gray-500 border border-gray-200',
}

export function UserTable({ users, onChanged }: UserTableProps) {
  const { user: currentUser, hasRole } = useAuthStore()
  const canChangeRole = hasRole(['OWNER', 'ADMIN'])
  const canRemove = hasRole(['OWNER'])

  const [changingRole, setChangingRole] = useState<string | null>(null)
  const [removingId, setRemovingId] = useState<string | null>(null)

  async function handleRoleChange(userId: string, role: Role) {
    setChangingRole(userId)
    try {
      await api.users.updateRole(userId, { role })
      onChanged()
    } catch {
      // silently ignore — user sees no change
    } finally {
      setChangingRole(null)
    }
  }

  async function handleRemove(userId: string) {
    if (!confirm('Remove this user from the organization? This cannot be undone.')) return
    setRemovingId(userId)
    try {
      await api.users.remove(userId)
      onChanged()
    } catch {
      // silently ignore
    } finally {
      setRemovingId(null)
    }
  }

  return (
    <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>User</TableHead>
            <TableHead>Role</TableHead>
            <TableHead>Joined</TableHead>
            {(canChangeRole || canRemove) && <TableHead className="w-24" />}
          </TableRow>
        </TableHeader>
        <TableBody>
          {users.map((member) => {
            const isSelf = member.id === currentUser?.id
            const isOwner = member.role === 'OWNER'
            const canEditThis = canChangeRole && !isSelf && !isOwner
            const canRemoveThis = canRemove && !isSelf && !isOwner

            return (
              <TableRow key={member.id}>
                <TableCell>
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {member.fullName}
                      {isSelf && (
                        <span className="ml-2 text-xs text-gray-400 font-normal">(you)</span>
                      )}
                    </p>
                    <p className="text-xs text-gray-500">{member.email}</p>
                  </div>
                </TableCell>
                <TableCell>
                  {canEditThis ? (
                    <div className="w-48">
                      <RoleSelector
                        value={member.role}
                        onChange={(role) => handleRoleChange(member.id, role)}
                        disabled={changingRole === member.id}
                        exclude={['OWNER']}
                      />
                    </div>
                  ) : (
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${ROLE_COLORS[member.role]}`}
                    >
                      {member.role}
                    </span>
                  )}
                </TableCell>
                <TableCell>
                  <span className="text-sm text-gray-500">{formatDateTime(member.joinedAt)}</span>
                </TableCell>
                {(canChangeRole || canRemove) && (
                  <TableCell>
                    {canRemoveThis && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleRemove(member.id)}
                        disabled={removingId === member.id}
                        className="text-red-600 border-red-200 hover:bg-red-50 hover:border-red-300"
                      >
                        {removingId === member.id ? 'Removing…' : 'Remove'}
                      </Button>
                    )}
                  </TableCell>
                )}
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
