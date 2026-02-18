'use client'

import { useState, useEffect } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { api } from '@/lib/api'
import type { Dataset, AiInsight } from '@/types'

interface GenerateInsightModalProps {
  open: boolean
  onClose: () => void
  onGenerated: (insight: AiInsight) => void
  preselectedDatasetId?: string
}

export function GenerateInsightModal({
  open,
  onClose,
  onGenerated,
  preselectedDatasetId,
}: GenerateInsightModalProps) {
  const [datasets, setDatasets] = useState<Dataset[]>([])
  const [selectedId, setSelectedId] = useState(preselectedDatasetId ?? '')
  const [loading, setLoading] = useState(false)
  const [fetching, setFetching] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setFetching(true)
    api.datasets
      .list()
      .then((page) => setDatasets(page.content.filter((d) => d.status === 'READY')))
      .catch(() => setError('Failed to load datasets.'))
      .finally(() => setFetching(false))
  }, [open])

  useEffect(() => {
    if (preselectedDatasetId) setSelectedId(preselectedDatasetId)
  }, [preselectedDatasetId])

  async function handleGenerate() {
    if (!selectedId) {
      setError('Please select a dataset.')
      return
    }
    setLoading(true)
    setError('')
    try {
      const insight = await api.insights.generate({ datasetId: selectedId })
      onGenerated(insight)
      onClose()
    } catch {
      setError('Failed to generate insight. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  function handleClose() {
    setError('')
    onClose()
  }

  return (
    <Dialog open={open} onOpenChange={(o) => !o && handleClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Generate AI Insight</DialogTitle>
        </DialogHeader>

        <div className="py-2 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Select Dataset
            </label>
            {fetching ? (
              <div className="h-9 bg-gray-100 rounded-md animate-pulse" />
            ) : (
              <select
                value={selectedId}
                onChange={(e) => setSelectedId(e.target.value)}
                className="w-full rounded-md border border-gray-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-300"
              >
                <option value="">Choose a dataset…</option>
                {datasets.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.rowCount?.toLocaleString() ?? '–'} rows)
                  </option>
                ))}
              </select>
            )}
            {datasets.length === 0 && !fetching && (
              <p className="text-xs text-gray-500 mt-1">
                No ready datasets found. Upload and process a dataset first.
              </p>
            )}
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={handleClose} disabled={loading}>
            Cancel
          </Button>
          <Button onClick={handleGenerate} disabled={loading || fetching || !selectedId}>
            {loading ? 'Generating…' : 'Generate'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
