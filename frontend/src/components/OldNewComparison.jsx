import { Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'

const display = (value) => {
  if (value === null || value === undefined) return '—'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

export default function OldNewComparison({ oldValue, newValue }) {
  const keys = [...new Set([...Object.keys(oldValue || {}), ...Object.keys(newValue || {})])]
  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell>Alan</TableCell>
          <TableCell>Eski</TableCell>
          <TableCell>Yeni</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {keys.map((key) => {
          const changed = JSON.stringify(oldValue?.[key]) !== JSON.stringify(newValue?.[key])
          return (
            <TableRow key={key} sx={changed ? { bgcolor: 'warning.50' } : undefined}>
              <TableCell><Typography fontWeight={changed ? 700 : 400}>{key}</Typography></TableCell>
              <TableCell sx={{ color: changed ? 'error.main' : 'text.primary' }}>{display(oldValue?.[key])}</TableCell>
              <TableCell sx={{ color: changed ? 'success.main' : 'text.primary' }}>{display(newValue?.[key])}</TableCell>
            </TableRow>
          )
        })}
      </TableBody>
    </Table>
  )
}

