'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import Link from 'next/link'
import { ArrowLeft, Sparkles, Database } from 'lucide-react'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { api } from '@/lib/api'
import { formatDateTime } from '@/lib/utils'
import type { AiInsight } from '@/types'

export default function InsightDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()

  const [insight, setInsight] = useState<AiInsight | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api.insights
      .get(id)
      .then(setInsight)
      .catch(() => setError('Insight not found.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <>
        <Header title="Loading…" />
        <PageContainer>
          <div className="space-y-4">
            <div className="h-8 w-1/3 bg-gray-100 rounded animate-pulse" />
            <div className="h-32 bg-gray-100 rounded-lg animate-pulse" />
            <div className="h-48 bg-gray-100 rounded-lg animate-pulse" />
          </div>
        </PageContainer>
      </>
    )
  }

  if (error || !insight) {
    return (
      <>
        <Header title="Not Found" />
        <PageContainer>
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <p className="text-sm text-gray-500 mb-4">{error || 'Insight not found.'}</p>
            <Button variant="outline" onClick={() => router.push('/insights')}>
              Back to Insights
            </Button>
          </div>
        </PageContainer>
      </>
    )
  }

  const details = insight.details as Record<string, unknown>

  return (
    <>
      <Header
        title={insight.title}
        action={
          <Link href="/insights">
            <Button variant="outline" size="sm">
              <ArrowLeft className="h-4 w-4 mr-1.5" />
              Back
            </Button>
          </Link>
        }
      />
      <PageContainer>
        <div className="space-y-6">
          {/* Meta bar */}
          <div className="flex flex-wrap items-center gap-3 text-xs text-gray-500">
            <span className="flex items-center gap-1">
              <Database className="h-3.5 w-3.5" />
              <Link
                href={`/datasets/${insight.datasetId}`}
                className="hover:text-gray-700 underline underline-offset-2"
              >
                {insight.datasetName}
              </Link>
            </span>
            <span>·</span>
            <span>{formatDateTime(insight.createdAt)}</span>
            <Badge variant="secondary" className="font-mono text-xs">
              {insight.modelUsed}
            </Badge>
          </div>

          {/* Summary card */}
          <div className="bg-white border border-gray-200 rounded-lg p-6">
            <div className="flex items-center gap-2 mb-3">
              <div className="h-7 w-7 rounded-md bg-gray-100 flex items-center justify-center">
                <Sparkles className="h-4 w-4 text-gray-600" />
              </div>
              <h2 className="text-sm font-semibold text-gray-900">Summary</h2>
            </div>
            <p className="text-sm text-gray-700 leading-relaxed">{insight.summary}</p>
          </div>

          {/* Details */}
          {details && Object.keys(details).length > 0 && (
            <div className="bg-white border border-gray-200 rounded-lg p-6">
              <h2 className="text-sm font-semibold text-gray-900 mb-4">Details</h2>
              <div className="space-y-4">
                {Object.entries(details).map(([key, value]) => (
                  <DetailSection key={key} label={key} value={value} />
                ))}
              </div>
            </div>
          )}
        </div>
      </PageContainer>
    </>
  )
}

function DetailSection({ label, value }: { label: string; value: unknown }) {
  const formatted = label
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (c) => c.toUpperCase())

  if (Array.isArray(value)) {
    return (
      <div>
        <p className="text-xs font-medium text-gray-500 mb-2">{formatted}</p>
        <ul className="space-y-1">
          {value.map((item, i) => (
            <li key={i} className="text-sm text-gray-700 flex items-start gap-2">
              <span className="mt-1.5 h-1 w-1 rounded-full bg-gray-400 shrink-0" />
              <span>{typeof item === 'object' ? JSON.stringify(item) : String(item)}</span>
            </li>
          ))}
        </ul>
      </div>
    )
  }

  if (typeof value === 'object' && value !== null) {
    return (
      <div>
        <p className="text-xs font-medium text-gray-500 mb-2">{formatted}</p>
        <pre className="text-xs text-gray-600 bg-gray-50 rounded-md p-3 overflow-auto font-mono">
          {JSON.stringify(value, null, 2)}
        </pre>
      </div>
    )
  }

  return (
    <div className="flex gap-4">
      <p className="text-xs font-medium text-gray-500 w-32 shrink-0 pt-0.5">{formatted}</p>
      <p className="text-sm text-gray-700">{String(value)}</p>
    </div>
  )
}
