'use client'

import { useEffect, useState } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { ArrowLeft, Sparkles } from 'lucide-react'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { Button } from '@/components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { api } from '@/lib/api'
import { formatDate, formatNumber, statusColor } from '@/lib/utils'
import { useAuthStore } from '@/store/useAuthStore'
import type { DatasetDetail } from '@/types'

export default function DatasetDetailPage() {
  const { id } = useParams<{ id: string }>()
  const router = useRouter()
  const [dataset, setDataset] = useState<DatasetDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [generating, setGenerating] = useState(false)
  const canEdit = useAuthStore((s) => s.canEdit())

  useEffect(() => {
    api.datasets.get(id).then(setDataset).catch(() => router.push('/datasets')).finally(() => setLoading(false))
  }, [id])

  async function handleGenerateInsight() {
    if (!dataset) return
    setGenerating(true)
    try {
      const insight = await api.insights.generate({ datasetId: dataset.id })
      router.push(`/insights/${insight.id}`)
    } catch { /* ignore */ }
    finally { setGenerating(false) }
  }

  if (loading) return <PageContainer><p className="text-sm text-gray-400">Loading...</p></PageContainer>
  if (!dataset) return null

  return (
    <>
      <Header
        title={dataset.name}
        description={dataset.description ?? undefined}
        action={
          canEdit ? (
            <Button size="sm" onClick={handleGenerateInsight} disabled={generating || dataset.status !== 'READY'}>
              <Sparkles className="h-4 w-4 mr-1.5" />
              {generating ? 'Generating...' : 'Generate Insight'}
            </Button>
          ) : undefined
        }
      />
      <PageContainer>
        <button
          onClick={() => router.push('/datasets')}
          className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 mb-5"
        >
          <ArrowLeft className="h-3.5 w-3.5" /> Back to Datasets
        </button>

        {/* Metadata */}
        <div className="bg-white rounded-lg border border-gray-200 p-5 mb-4">
          <h2 className="text-sm font-medium text-gray-900 mb-3">Details</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
            <div>
              <p className="text-xs text-gray-400">Status</p>
              <span className={`text-xs font-medium px-2 py-0.5 rounded border mt-0.5 inline-block ${statusColor(dataset.status)}`}>
                {dataset.status}
              </span>
            </div>
            <div>
              <p className="text-xs text-gray-400">Rows</p>
              <p className="text-gray-900 font-medium">{dataset.rowCount != null ? formatNumber(dataset.rowCount) : '—'}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400">Columns</p>
              <p className="text-gray-900 font-medium">{dataset.columns.length}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400">Uploaded</p>
              <p className="text-gray-900 font-medium">{formatDate(dataset.createdAt)}</p>
            </div>
          </div>
        </div>

        {/* Column schema */}
        <div className="bg-white rounded-lg border border-gray-200">
          <div className="px-5 py-3 border-b border-gray-100">
            <h2 className="text-sm font-medium text-gray-900">Column Schema</h2>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>#</TableHead>
                <TableHead>Column name</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Sample value</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {dataset.columns.map((col) => (
                <TableRow key={col.id}>
                  <TableCell className="text-xs text-gray-400">{col.columnIndex + 1}</TableCell>
                  <TableCell className="font-medium text-sm text-gray-900">{col.columnName}</TableCell>
                  <TableCell>
                    <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded font-mono">
                      {col.dataType}
                    </span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-500">{col.sampleValue ?? '—'}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </PageContainer>
    </>
  )
}
