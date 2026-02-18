import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatDate(dateString: string): string {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(dateString))
}

export function formatDateTime(dateString: string): string {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateString))
}

export function formatNumber(n: number): string {
  return new Intl.NumberFormat('en-US').format(n)
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

export function getInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

export function roleBadgeColor(role: string): string {
  switch (role) {
    case 'OWNER':  return 'bg-gray-900 text-white'
    case 'ADMIN':  return 'bg-gray-700 text-white'
    case 'MEMBER': return 'bg-gray-200 text-gray-800'
    case 'VIEWER': return 'bg-gray-100 text-gray-600'
    default:       return 'bg-gray-100 text-gray-600'
  }
}

export function statusColor(status: string): string {
  switch (status) {
    case 'READY':      return 'bg-green-50 text-green-700 border-green-200'
    case 'PROCESSING': return 'bg-yellow-50 text-yellow-700 border-yellow-200'
    case 'ERROR':      return 'bg-red-50 text-red-700 border-red-200'
    default:           return 'bg-gray-50 text-gray-600 border-gray-200'
  }
}
