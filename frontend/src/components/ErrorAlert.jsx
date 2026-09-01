import { Alert } from '@mui/material'

export default function ErrorAlert({ error }) {
  return error ? <Alert severity="error" sx={{ mb: 2 }}>{error.message || String(error)}</Alert> : null
}

