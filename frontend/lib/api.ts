import type {
  AiInsight,
  ApiError,
  AuditLog,
  AuthResponse,
  Dataset,
  DatasetDetail,
  Organization,
  PagedResponse,
  Role,
  UserMember,
} from '@/types'

const BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  })

  if (res.status === 204) return null as T

  const data = await res.json().catch(() => null)

  if (!res.ok) {
    throw data as ApiError ?? { status: res.status, message: 'Request failed' }
  }

  return data as T
}

async function upload<T>(path: string, formData: FormData): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  })
  const data = await res.json().catch(() => null)
  if (!res.ok) throw data as ApiError ?? { status: res.status, message: 'Upload failed' }
  return data as T
}

export const api = {
  auth: {
    register: (body: {
      email: string
      password: string
      fullName: string
      organizationName: string
      organizationSlug: string
    }) =>
      request<AuthResponse>('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    login: (body: { email: string; password: string }) =>
      request<AuthResponse>('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    logout: () =>
      request<void>('/api/auth/logout', { method: 'POST' }),

    me: () => request<AuthResponse>('/api/auth/me'),

    refresh: () =>
      request<void>('/api/auth/refresh', { method: 'POST' }),
  },

  organization: {
    getCurrent: () => request<Organization>('/api/organizations/current'),
    update: (body: { name: string }) =>
      request<Organization>('/api/organizations/current', {
        method: 'PUT',
        body: JSON.stringify(body),
      }),
  },

  datasets: {
    list: (page = 0, size = 20) =>
      request<PagedResponse<Dataset>>(`/api/datasets?page=${page}&size=${size}`),

    get: (id: string) => request<DatasetDetail>(`/api/datasets/${id}`),

    upload: (file: File, name?: string) => {
      const form = new FormData()
      form.append('file', file)
      if (name) form.append('name', name)
      return upload<DatasetDetail>('/api/datasets', form)
    },

    delete: (id: string) =>
      request<void>(`/api/datasets/${id}`, { method: 'DELETE' }),
  },

  insights: {
    list: (page = 0, size = 20) =>
      request<PagedResponse<AiInsight>>(`/api/insights?page=${page}&size=${size}`),

    get: (id: string) => request<AiInsight>(`/api/insights/${id}`),

    generate: (body: { datasetId: string }) =>
      request<AiInsight>('/api/insights/generate', {
        method: 'POST',
        body: JSON.stringify(body),
      }),
  },

  users: {
    list: (page = 0, size = 20) =>
      request<PagedResponse<UserMember>>(`/api/users?page=${page}&size=${size}`),

    invite: (body: { email: string; role: Role }) =>
      request<void>('/api/users/invite', {
        method: 'POST',
        body: JSON.stringify(body),
      }),

    updateRole: (id: string, body: { role: Role }) =>
      request<UserMember>(`/api/users/${id}/role`, {
        method: 'PUT',
        body: JSON.stringify(body),
      }),

    remove: (id: string) =>
      request<void>(`/api/users/${id}`, { method: 'DELETE' }),
  },

  audit: {
    list: (page = 0, size = 20) =>
      request<PagedResponse<AuditLog>>(`/api/audit-logs?page=${page}&size=${size}`),
  },
}
