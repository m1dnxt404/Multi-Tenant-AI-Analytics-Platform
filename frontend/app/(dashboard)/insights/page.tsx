'use client'

import { useState, useEffect, useCallback } from 'react'
import { Sparkles, Plus } from 'lucide-react'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { InsightCard } from '@/components/insights/InsightCard'
import { GenerateInsightModal } from '@/components/insights/GenerateInsightModal'
import { Button } from '@/components/ui/button'
import { api } from '@/lib/api'
import { useAuthStore } from '@/store/useAuthStore'
import type { AiInsight } from '@/types'

export default function InsightsPage() {
  const { hasRole } = useAuthStore()
  const canGenerate = hasRole(['OWNER', 'ADMIN', 'MEMBER'])

  const [insights, setInsights] = useState<AiInsight[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [showGenerate, setShowGenerate] = useState(false)

  const fetchInsights = useCallback(async (p: number) => {
    setLoading(true)
    try {
      const res = await api.insights.list(p)
      setInsights(res.content)
      setTotalPages(res.totalPages)
    } catch {
      // keep existing data on error
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchInsights(page)
  }, [fetchInsights, page])

  function handleGenerated(insight: AiInsight) {
    setInsights((prev) => [insight, ...prev])
  }

  return (
    <>
      <Header
        title="AI Insights"
        description="AI-generated analysis from your datasets"
        action={
          canGenerate ? (
            <Button onClick={() => setShowGenerate(true)} size="sm">
              <Plus className="h-4 w-4 mr-1.5" />
              Generate Insight
            </Button>
          ) : undefined
        }
      />
      <PageContainer>
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="h-32 bg-gray-100 rounded-lg animate-pulse" />
            ))}
          </div>
        ) : insights.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="h-12 w-12 rounded-full bg-gray-100 flex items-center justify-center mb-3">
              <Sparkles className="h-6 w-6 text-gray-400" />
            </div>
            <p className="text-sm font-medium text-gray-900">No insights yet</p>
            <p className="text-xs text-gray-500 mt-1">
              {canGenerate
                ? 'Generate your first AI insight from a dataset.'
                : 'Insights will appear here once generated.'}
            </p>
            {canGenerate && (
              <Button onClick={() => setShowGenerate(true)} size="sm" className="mt-4">
                Generate Insight
              </Button>
            )}
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {insights.map((insight) => (
                <InsightCard key={insight.id} insight={insight} />
              ))}
            </div>

            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 mt-6">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => p - 1)}
                  disabled={page === 0}
                >
                  Previous
                </Button>
                <span className="text-sm text-gray-500">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= totalPages - 1}
                >
                  Next
                </Button>
              </div>
            )}
          </>
        )}
      </PageContainer>

      <GenerateInsightModal
        open={showGenerate}
        onClose={() => setShowGenerate(false)}
        onGenerated={handleGenerated}
      />
    </>
  )
}
