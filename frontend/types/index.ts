export type Role = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'

export type DatasetStatus = 'PROCESSING' | 'READY' | 'ERROR'

export interface Organization {
  id: string
  name: string
  slug: string
  createdAt: string
}

export interface User {
  id: string
  email: string
  fullName: string
  role: Role
  createdAt: string
}

export interface AuthResponse {
  userId: string
  email: string
  fullName: string
  role: Role
  organization: Organization
}

export interface Dataset {
  id: string
  name: string
  description: string | null
  fileName: string
  rowCount: number | null
  status: DatasetStatus
  uploadedBy: string
  createdAt: string
  updatedAt: string
}

export interface DatasetColumn {
  id: string
  columnName: string
  dataType: string
  sampleValue: string | null
  columnIndex: number
}

export interface DatasetDetail extends Dataset {
  columns: DatasetColumn[]
}

export interface AiInsight {
  id: string
  datasetId: string
  datasetName: string
  title: string
  summary: string
  details: Record<string, unknown>
  modelUsed: string
  generatedBy: string
  createdAt: string
}

export interface UserMember {
  id: string
  email: string
  fullName: string
  role: Role
  joinedAt: string
}

export interface AuditLog {
  id: string
  userId: string
  action: string
  resource: string | null
  resourceId: string | null
  metadata: Record<string, unknown> | null
  ipAddress: string | null
  createdAt: string
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}
