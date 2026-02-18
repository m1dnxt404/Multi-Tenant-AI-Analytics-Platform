'use client'

import Link from 'next/link'
import { Trash2 } from 'lucide-react'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { formatDate, formatNumber, statusColor } from '@/lib/utils'
import type { Dataset } from '@/types'

interface DatasetTableProps {
  datasets: Dataset[]
  canDelete: boolean
  onDelete: (id: string) => void
}

export function DatasetTable({ datasets, canDelete, onDelete }: DatasetTableProps) {
  if (datasets.length === 0) {
    return (
      <div className="text-center py-12 text-sm text-gray-400">
        No datasets yet. Upload your first CSV file.
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg border border-gray-200">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Rows</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Uploaded</TableHead>
            {canDelete && <TableHead className="w-10" />}
          </TableRow>
        </TableHeader>
        <TableBody>
          {datasets.map((ds) => (
            <TableRow key={ds.id}>
              <TableCell>
                <Link
                  href={`/datasets/${ds.id}`}
                  className="font-medium text-gray-900 hover:underline"
                >
                  {ds.name}
                </Link>
                {ds.description && (
                  <p className="text-xs text-gray-400 mt-0.5 line-clamp-1">{ds.description}</p>
                )}
              </TableCell>
              <TableCell className="text-sm text-gray-600">
                {ds.rowCount != null ? formatNumber(ds.rowCount) : '—'}
              </TableCell>
              <TableCell>
                <span className={`text-xs font-medium px-2 py-0.5 rounded border ${statusColor(ds.status)}`}>
                  {ds.status}
                </span>
              </TableCell>
              <TableCell className="text-sm text-gray-500">{formatDate(ds.createdAt)}</TableCell>
              {canDelete && (
                <TableCell>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-gray-400 hover:text-red-600"
                    onClick={() => onDelete(ds.id)}
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </TableCell>
              )}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
