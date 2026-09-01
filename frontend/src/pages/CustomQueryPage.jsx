import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { Box, Button, Card, CardContent, Grid } from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import { customQueryApi } from '../api/parameterApi'
import PageHeader from '../components/PageHeader'
import ErrorAlert from '../components/ErrorAlert'
import DynamicField from '../components/DynamicField'

export default function CustomQueryPage() {
  const { code } = useParams()
  const [filters, setFilters] = useState({})
  const [submittedFilters, setSubmittedFilters] = useState({})
  const [pageModel, setPageModel] = useState({ page: 0, pageSize: 20 })
  const metadataQuery = useQuery({ queryKey: ['custom-metadata', code], queryFn: () => customQueryApi.metadata(code) })
  const dataQuery = useQuery({
    queryKey: ['custom-search', code, submittedFilters, pageModel],
    queryFn: () => customQueryApi.search(code, {
      filters: submittedFilters,
      page: pageModel.page,
      size: pageModel.pageSize,
    }),
    enabled: Boolean(metadataQuery.data),
  })
  const columns = useMemo(() => (metadataQuery.data?.columns || []).map((column) => ({
    field: column.name,
    headerName: column.label,
    minWidth: 150,
    flex: 1,
  })), [metadataQuery.data])

  return (
    <Box>
      <PageHeader
        title={metadataQuery.data?.title || code}
        subtitle="Bu ekran kayıtlı CustomQueryProvider üzerinden join/projection sorgusu çalıştırır."
      />
      <ErrorAlert error={metadataQuery.error || dataQuery.error} />
      {metadataQuery.data && (
        <Card sx={{ mb: 2 }}>
          <CardContent>
            <Grid container spacing={2}>
              {metadataQuery.data.filters.map((field) => (
                <Grid item xs={12} md={6} key={field.name}>
                  <DynamicField
                    field={field}
                    value={filters[field.name]}
                    onChange={(value) => setFilters({ ...filters, [field.name]: value })}
                  />
                </Grid>
              ))}
            </Grid>
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
              <Button onClick={() => { setFilters({}); setSubmittedFilters({}) }}>Temizle</Button>
              <Button variant="contained" onClick={() => { setSubmittedFilters(filters); setPageModel({ ...pageModel, page: 0 }) }}>Sorgula</Button>
            </Box>
          </CardContent>
        </Card>
      )}
      <Card sx={{ height: 560 }}>
        <DataGrid
          rows={(dataQuery.data?.content || []).map((row, index) => ({ id: `${pageModel.page}-${index}`, ...row }))}
          columns={columns}
          rowCount={dataQuery.data?.totalElements || 0}
          loading={dataQuery.isLoading}
          paginationMode="server"
          paginationModel={pageModel}
          onPaginationModelChange={setPageModel}
          pageSizeOptions={[10, 20, 50, 100]}
        />
      </Card>
    </Box>
  )
}

