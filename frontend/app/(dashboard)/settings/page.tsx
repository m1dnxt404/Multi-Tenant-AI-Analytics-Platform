'use client'

import { useState, useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { api } from '@/lib/api'
import { useAuthStore } from '@/store/useAuthStore'

const schema = z.object({
  name: z.string().min(2, 'Organization name must be at least 2 characters'),
})

type FormData = z.infer<typeof schema>

export default function SettingsPage() {
  const { organization, setAuth, user } = useAuthStore()
  const [saved, setSaved] = useState(false)
  const [serverError, setServerError] = useState('')

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { name: organization?.name ?? '' },
  })

  useEffect(() => {
    if (organization) reset({ name: organization.name })
  }, [organization, reset])

  async function onSubmit(data: FormData) {
    setSaved(false)
    setServerError('')
    try {
      const updated = await api.organization.update({ name: data.name })
      if (user) {
        setAuth({
          userId: user.id,
          email: user.email,
          fullName: user.fullName,
          role: user.role,
          organization: updated,
        })
      }
      reset({ name: updated.name })
      setSaved(true)
      setTimeout(() => setSaved(false), 3000)
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to save. Please try again.'
      setServerError(message)
    }
  }

  return (
    <>
      <Header title="Settings" description="Manage your organization settings" />
      <PageContainer>
      <div className="max-w-xl space-y-6">
        {/* Organization settings */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h2 className="text-sm font-semibold text-gray-900 mb-4">Organization</h2>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="org-name">Organization Name</Label>
              <Input id="org-name" {...register('name')} />
              {errors.name && (
                <p className="text-xs text-red-600">{errors.name.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label>Slug</Label>
              <Input
                value={organization?.slug ?? ''}
                readOnly
                className="bg-gray-50 text-gray-500 cursor-not-allowed font-mono"
              />
              <p className="text-xs text-gray-400">
                The slug is used for tenant isolation and cannot be changed after creation.
              </p>
            </div>

            {serverError && <p className="text-sm text-red-600">{serverError}</p>}

            <div className="flex items-center gap-3 pt-1">
              <Button type="submit" disabled={!isDirty || isSubmitting}>
                {isSubmitting ? 'Saving…' : 'Save Changes'}
              </Button>
              {saved && <p className="text-sm text-green-600">Changes saved.</p>}
            </div>
          </form>
        </div>

        {/* Account info */}
        <div className="bg-white border border-gray-200 rounded-lg p-6">
          <h2 className="text-sm font-semibold text-gray-900 mb-4">Account</h2>
          <div className="space-y-3">
            <div className="flex justify-between items-center py-2 border-b border-gray-100">
              <span className="text-sm text-gray-500">Full name</span>
              <span className="text-sm text-gray-900">{user?.fullName}</span>
            </div>
            <div className="flex justify-between items-center py-2 border-b border-gray-100">
              <span className="text-sm text-gray-500">Email</span>
              <span className="text-sm text-gray-900">{user?.email}</span>
            </div>
            <div className="flex justify-between items-center py-2">
              <span className="text-sm text-gray-500">Role</span>
              <span className="text-sm font-medium text-gray-900">{user?.role}</span>
            </div>
          </div>
        </div>

        {/* Danger zone — OWNER only */}
        {user?.role === 'OWNER' && (
          <div className="bg-white border border-red-200 rounded-lg p-6">
            <h2 className="text-sm font-semibold text-red-700 mb-1">Danger Zone</h2>
            <p className="text-xs text-gray-500 mb-4">
              These actions are irreversible. Proceed with caution.
            </p>
            <Button
              variant="outline"
              className="border-red-200 text-red-600 hover:bg-red-50 hover:border-red-300"
              onClick={() => alert('Organization deletion is not yet implemented.')}
            >
              Delete Organization
            </Button>
          </div>
        )}
      </div>
      </PageContainer>
    </>
  )
}
