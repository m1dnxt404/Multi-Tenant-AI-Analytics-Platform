'use client'

import { useEffect, useState } from 'react'
import { Database, Sparkles, Users, Clock } from 'lucide-react'
import { Header } from '@/components/layout/Header'
import { PageContainer } from '@/components/layout/PageContainer'
import { KpiCard } from '@/components/dashboard/KpiCard'
import { RevenueChart } from '@/components/dashboard/RevenueChart'
import { api } from '@/lib/api'
import { formatDateTime } from '@/lib/utils'
import type { AiInsight, Dataset } from '@/types'

export default function DashboardPage() {
  const [datasetCount, setDatasetCount]  = useState(0)
  const [insightCount, setInsightCount]  = useState(0)
  const [recentInsights, setRecentInsights] = useState<AiInsight[]>([])
  const [recentDatasets, setRecentDatasets] = useState<Dataset[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [ds, ins] = await Promise.all([
          api.datasets.list(0, 5),
          api.insights.list(0, 5),
        ])
        setDatasetCount(ds.totalElements)
        setInsightCount(ins.totalElements)
        setRecentDatasets(ds.content)
        setRecentInsights(ins.content)
      } catch { /* show zeros */ }
      finally { setLoading(false) }
    }
    load()
  }, [])

  return (
    <>
      <Header title="Dashboard" description="Overview of your organisation's analytics" />
      <PageContainer>
        {/* KPI Row */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <KpiCard
            title="Total Datasets"
            value={loading ? '—' : datasetCount}
            icon={Database}
            subtitle="CSV files uploaded"
          />
          <KpiCard
            title="AI Insights"
            value={loading ? '—' : insightCount}
            icon={Sparkles}
            subtitle="Generated insights"
          />
          <KpiCard
            title="Recent Dataset"
            value={recentDatasets[0]?.name ?? '—'}
            icon={Clock}
            subtitle={recentDatasets[0] ? formatDateTime(recentDatasets[0].createdAt) : 'No data yet'}
          />
          <KpiCard
            title="Latest Insight"
            value={recentInsights[0] ? 'View →' : '—'}
            icon={Sparkles}
            subtitle={recentInsights[0]?.title ?? 'No insights yet'}
          />
        </div>

        {/* Charts + Recent */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2">
            <RevenueChart />
          </div>

          <div className="bg-white rounded-lg border border-gray-200 p-5">
            <h3 className="text-sm font-medium text-gray-900 mb-3">Recent Insights</h3>
            {loading ? (
              <p className="text-xs text-gray-400">Loading...</p>
            ) : recentInsights.length === 0 ? (
              <p className="text-xs text-gray-400">No insights yet.</p>
            ) : (
              <div className="space-y-3">
                {recentInsights.slice(0, 4).map((ins) => (
                  <div key={ins.id} className="border-b border-gray-100 pb-2 last:border-0">
                    <p className="text-xs font-medium text-gray-800 line-clamp-1">{ins.title}</p>
                    <p className="text-xs text-gray-400">{ins.datasetName}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </PageContainer>
    </>
  )
}
