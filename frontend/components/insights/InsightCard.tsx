import Link from 'next/link'
import { Sparkles } from 'lucide-react'
import { formatDateTime } from '@/lib/utils'
import type { AiInsight } from '@/types'

interface InsightCardProps {
  insight: AiInsight
}

export function InsightCard({ insight }: InsightCardProps) {
  return (
    <Link href={`/insights/${insight.id}`} className="block">
      <div className="bg-white rounded-lg border border-gray-200 p-5 hover:border-gray-300 transition-colors">
        <div className="flex items-start gap-3">
          <div className="h-8 w-8 rounded-md bg-gray-100 flex items-center justify-center shrink-0">
            <Sparkles className="h-4 w-4 text-gray-600" />
          </div>
          <div className="min-w-0">
            <p className="text-sm font-medium text-gray-900 line-clamp-1">{insight.title}</p>
            <p className="text-xs text-gray-500 mt-0.5">{insight.datasetName}</p>
            <p className="text-xs text-gray-600 mt-2 line-clamp-2">{insight.summary}</p>
            <div className="flex items-center gap-3 mt-3">
              <span className="text-xs text-gray-400">{formatDateTime(insight.createdAt)}</span>
              <span className="text-xs bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded font-mono">
                {insight.modelUsed}
              </span>
            </div>
          </div>
        </div>
      </div>
    </Link>
  )
}
