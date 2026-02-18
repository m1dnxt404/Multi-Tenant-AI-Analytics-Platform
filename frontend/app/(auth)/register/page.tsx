'use client'

import { useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { api } from '@/lib/api'
import { useAuthStore } from '@/store/useAuthStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import type { ApiError } from '@/types'

const schema = z.object({
  fullName: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  organizationName: z.string().min(2, 'Organisation name is required'),
  organizationSlug: z
    .string()
    .min(3, 'Slug must be at least 3 characters')
    .max(50, 'Slug must be 50 characters or less')
    .regex(/^[a-z0-9-]+$/, 'Slug: lowercase letters, numbers, hyphens only'),
})
type FormData = z.infer<typeof schema>

export default function RegisterPage() {
  const router = useRouter()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [error, setError] = useState<string | null>(null)

  const { register, handleSubmit, formState: { errors, isSubmitting }, watch, setValue } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  function handleOrgNameChange(value: string) {
    const slug = value
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-|-$/g, '')
    setValue('organizationSlug', slug, { shouldValidate: true })
  }

  async function onSubmit(data: FormData) {
    setError(null)
    try {
      const auth = await api.auth.register(data)
      setAuth(auth)
      router.push('/')
    } catch (e) {
      const err = e as ApiError
      setError(err?.message ?? 'Registration failed. Please try again.')
    }
  }

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-8">
      <div className="mb-8">
        <h1 className="text-2xl font-semibold text-gray-900">Create account</h1>
        <p className="text-sm text-gray-500 mt-1">Set up your organisation and admin account</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="fullName">Full name</Label>
          <Input id="fullName" placeholder="Jane Smith" {...register('fullName')} />
          {errors.fullName && <p className="text-xs text-red-600">{errors.fullName.message}</p>}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="email">Work email</Label>
          <Input id="email" type="email" placeholder="jane@company.com" {...register('email')} />
          {errors.email && <p className="text-xs text-red-600">{errors.email.message}</p>}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="password">Password</Label>
          <Input id="password" type="password" placeholder="8+ characters" {...register('password')} />
          {errors.password && <p className="text-xs text-red-600">{errors.password.message}</p>}
        </div>

        <div className="pt-2 border-t border-gray-100">
          <p className="text-xs text-gray-500 mb-3 font-medium">Organisation details</p>

          <div className="space-y-1.5">
            <Label htmlFor="organizationName">Organisation name</Label>
            <Input
              id="organizationName"
              placeholder="Acme Corp"
              {...register('organizationName', {
                onChange: (e) => handleOrgNameChange(e.target.value),
              })}
            />
            {errors.organizationName && (
              <p className="text-xs text-red-600">{errors.organizationName.message}</p>
            )}
          </div>

          <div className="space-y-1.5 mt-3">
            <Label htmlFor="organizationSlug">URL slug</Label>
            <div className="flex items-center gap-1">
              <span className="text-sm text-gray-400 shrink-0">app/</span>
              <Input
                id="organizationSlug"
                placeholder="acme-corp"
                {...register('organizationSlug')}
              />
            </div>
            {errors.organizationSlug && (
              <p className="text-xs text-red-600">{errors.organizationSlug.message}</p>
            )}
          </div>
        </div>

        {error && (
          <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-md p-3">
            {error}
          </p>
        )}

        <Button type="submit" className="w-full" disabled={isSubmitting}>
          {isSubmitting ? 'Creating account...' : 'Create account'}
        </Button>
      </form>

      <p className="text-sm text-gray-500 text-center mt-6">
        Already have an account?{' '}
        <Link href="/login" className="text-gray-900 font-medium hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  )
}
