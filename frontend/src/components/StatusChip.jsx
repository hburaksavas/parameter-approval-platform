import { Chip } from '@mui/material'

const colors = {
  WAITING_APPROVAL: 'warning',
  APPROVED: 'success',
  REJECTED: 'error',
  WITHDRAWN: 'default',
  FAILED_CONFLICT: 'error',
}

const labels = {
  WAITING_APPROVAL: 'Onay Bekliyor',
  APPROVED: 'Onaylandı',
  REJECTED: 'Reddedildi',
  WITHDRAWN: 'Geri Çekildi',
  FAILED_CONFLICT: 'Çakışma',
}

export default function StatusChip({ status }) {
  return <Chip size="small" color={colors[status] || 'default'} label={labels[status] || status} />
}

