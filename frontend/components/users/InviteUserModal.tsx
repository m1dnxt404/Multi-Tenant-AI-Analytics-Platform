'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RoleSelector } from './RoleSelector'
import { api } from '@/lib/api'
import type { Role } from '@/types'

const schema = z.object({
  email: z.string().email('Enter a valid email address'),
})

type FormData = z.infer<typeof schema>

interface InviteUserModalProps {
  open: boolean
  onClose: () => void
  onInvited: () => void
}

export function InviteUserModal({ open, onClose, onInvited }: InviteUserModalProps) {
  const [role, setRole] = useState<Role>('MEMBER')
  const [loading, setLoading] = useState(false)
  const [serverError, setServerError] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  async function onSubmit(data: FormData) {
    setLoading(true)
    setServerError('')
    try {
      await api.users.invite({ email: data.email, role })
      reset()
      setRole('MEMBER')
      onInvited()
      onClose()
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Failed to send invitation. Please try again.'
      setServerError(message)
    } finally {
      setLoading(false)
    }
  }

  function handleClose() {
    reset()
    setServerError('')
    setRole('MEMBER')
    onClose()
  }

  return (
    <Dialog open={open} onOpenChange={(o) => !o && handleClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Invite Team Member</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          <div className="space-y-1.5">
            <Label htmlFor="invite-email">Email address</Label>
            <Input
              id="invite-email"
              type="email"
              placeholder="colleague@company.com"
              {...register('email')}
            />
            {errors.email && (
              <p className="text-xs text-red-600">{errors.email.message}</p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label>Role</Label>
            <RoleSelector value={role} onChange={setRole} exclude={['OWNER']} />
          </div>

          {serverError && <p className="text-sm text-red-600">{serverError}</p>}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={handleClose} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Sending…' : 'Send Invite'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
