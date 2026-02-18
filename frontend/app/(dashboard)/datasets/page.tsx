'use client'

import { useEffect, useState } from 'react'
import { Plus } from 'lucide-react'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { DatasetTable } from '@/components/datasets/DatasetTable'
import { UploadModal } from '@/components/datasets/UploadModal'
import { Button } from '@/components/ui/button'
import { api } from '@/lib/api'
import { useAuthStore } from '@/store/useAuthStore'
import type { Dataset, DatasetDetail } from '@/types'

export default function DatasetsPage() {
  const [datasets, setDatasets] = useState<Dataset[]>([])
  const [loading, setLoading]   = useState(true)
  const [showUpload, setShowUpload] = useState(false)
  const canEdit = useAuthStore((s) => s.canEdit())
  const canDelete = useAuthStore((s) => s.hasRole(['OWNER', 'ADMIN']))

  useEffect(() => { load() }, [])

  async function load() {
    try {
      const res = await api.datasets.list()
      setDatasets(res.content)
    } catch { /* ignore */ }
    finally { setLoading(false) }
  }

  function handleUploadSuccess(ds: DatasetDetail) {
    setDatasets((prev) => [ds, ...prev])
  }

  async function handleDelete(id: string) {
    if (!confirm('Delete this dataset? This cannot be undone.')) return
    try {
      await api.datasets.delete(id)
      setDatasets((prev) => prev.filter((d) => d.id !== id))
    } catch { /* ignore */ }
  }

  return (
    <>
      <Header
        title="Datasets"
        description="Manage your CSV data files"
        action={
          canEdit ? (
            <Button size="sm" onClick={() => setShowUpload(true)}>
              <Plus className="h-4 w-4 mr-1.5" />
              Upload
            </Button>
          ) : undefined
        }
      />
      <PageContainer>
        {loading ? (
          <p className="text-sm text-gray-400">Loading...</p>
        ) : (
          <DatasetTable
            datasets={datasets}
            canDelete={canDelete}
            onDelete={handleDelete}
          />
        )}
      </PageContainer>

      <UploadModal
        open={showUpload}
        onClose={() => setShowUpload(false)}
        onSuccess={handleUploadSuccess}
      />
    </>
  )
}
