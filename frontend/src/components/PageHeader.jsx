import { Box, Typography } from '@mui/material'

export default function PageHeader({ title, subtitle, actions }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3, gap: 2 }}>
      <Box>
        <Typography variant="h4" gutterBottom>{title}</Typography>
        {subtitle && <Typography color="text.secondary">{subtitle}</Typography>}
      </Box>
      <Box sx={{ display: 'flex', gap: 1 }}>{actions}</Box>
    </Box>
  )
}

