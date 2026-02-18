'use client'

import { useState, useEffect, useCallback } from 'react'
import { Users, UserPlus } from 'lucide-react'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { UserTable } from '@/components/users/UserTable'
import { InviteUserModal } from '@/components/users/InviteUserModal'
import { Button } from '@/components/ui/button'
import { api } from '@/lib/api'
import { useAuthStore } from '@/store/useAuthStore'
import type { UserMember } from '@/types'

export default function UsersPage() {
  const { hasRole } = useAuthStore()
  const canInvite = hasRole(['OWNER', 'ADMIN'])

  const [members, setMembers] = useState<UserMember[]>([])
  const [loading, setLoading] = useState(true)
  const [showInvite, setShowInvite] = useState(false)

  const fetchMembers = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.users.list()
      setMembers(res.content)
    } catch {
      // keep existing data
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMembers()
  }, [fetchMembers])

  return (
    <>
      <Header
        title="Team"
        description={`${members.length} member${members.length !== 1 ? 's' : ''}`}
        action={
          canInvite ? (
            <Button onClick={() => setShowInvite(true)} size="sm">
              <UserPlus className="h-4 w-4 mr-1.5" />
              Invite Member
            </Button>
          ) : undefined
        }
      />
      <PageContainer>
        {loading ? (
          <div className="space-y-2">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="h-14 bg-gray-100 rounded-lg animate-pulse" />
            ))}
          </div>
        ) : members.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="h-12 w-12 rounded-full bg-gray-100 flex items-center justify-center mb-3">
              <Users className="h-6 w-6 text-gray-400" />
            </div>
            <p className="text-sm font-medium text-gray-900">No team members</p>
            <p className="text-xs text-gray-500 mt-1">Invite your first team member to collaborate.</p>
          </div>
        ) : (
          <UserTable users={members} onChanged={fetchMembers} />
        )}
      </PageContainer>

      <InviteUserModal
        open={showInvite}
        onClose={() => setShowInvite(false)}
        onInvited={fetchMembers}
      />
    </>
  )
}
