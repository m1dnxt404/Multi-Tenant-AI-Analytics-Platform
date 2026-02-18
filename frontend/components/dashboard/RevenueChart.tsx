'use client'

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'

const data = [
  { month: 'Oct', datasets: 3, insights: 2 },
  { month: 'Nov', datasets: 5, insights: 4 },
  { month: 'Dec', datasets: 4, insights: 5 },
  { month: 'Jan', datasets: 7, insights: 6 },
  { month: 'Feb', datasets: 9, insights: 8 },
]

export function RevenueChart() {
  return (
    <div className="bg-white rounded-lg border border-gray-200 p-5">
      <h3 className="text-sm font-medium text-gray-900 mb-4">Activity Overview</h3>
      <ResponsiveContainer width="100%" height={200}>
        <LineChart data={data} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
          <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
          <YAxis tick={{ fontSize: 12, fill: '#9ca3af' }} axisLine={false} tickLine={false} />
          <Tooltip
            contentStyle={{
              border: '1px solid #e5e7eb',
              borderRadius: '6px',
              fontSize: '12px',
              boxShadow: 'none',
            }}
          />
          <Line
            type="monotone"
            dataKey="datasets"
            stroke="#374151"
            strokeWidth={1.5}
            dot={false}
            name="Datasets"
          />
          <Line
            type="monotone"
            dataKey="insights"
            stroke="#9ca3af"
            strokeWidth={1.5}
            dot={false}
            name="Insights"
            strokeDasharray="4 2"
          />
        </LineChart>
      </ResponsiveContainer>
      <div className="flex items-center gap-4 mt-3">
        <div className="flex items-center gap-1.5">
          <div className="h-px w-4 bg-gray-700" />
          <span className="text-xs text-gray-500">Datasets</span>
        </div>
        <div className="flex items-center gap-1.5">
          <div className="h-px w-4 bg-gray-400 border-dashed border-t" />
          <span className="text-xs text-gray-500">Insights</span>
        </div>
      </div>
    </div>
  )
}
