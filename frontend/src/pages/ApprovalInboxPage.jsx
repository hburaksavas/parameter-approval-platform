import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Box, Card, FormControl, InputLabel, MenuItem, Select } from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import { requestApi } from '../api/parameterApi'
import PageHeader from '../components/PageHeader'
import ErrorAlert from '../components/ErrorAlert'
import StatusChip from '../components/StatusChip'

const statuses = [
  ['', 'Tümü'],
  ['WAITING_APPROVAL', 'Onay Bekliyor'],
  ['APPROVED', 'Onaylandı'],
  ['REJECTED', 'Reddedildi'],
  ['FAILED_CONFLICT', 'Çakışma'],
  ['WITHDRAWN', 'Geri Çekildi'],
]

export default function ApprovalInboxPage() {
  const navigate = useNavigate()
  const [status, setStatus] = useState('WAITING_APPROVAL')
  const [pageModel, setPageModel] = useState({ page: 0, pageSize: 20 })
  const query = useQuery({
    queryKey: ['requests', status, pageModel],
    queryFn: () => requestApi.list(status, pageModel.page, pageModel.pageSize),
  })
  const columns = useMemo(() => [
    { field: 'requestNo', headerName: 'Talep No', minWidth: 190 },
    { field: 'title', headerName: 'Başlık', flex: 1, minWidth: 220 },
    { field: 'status', headerName: 'Durum', minWidth: 150, renderCell: (params) => <StatusChip status={params.value} /> },
    { field: 'createdByName', headerName: 'Talep Eden', minWidth: 180 },
    { field: 'itemCount', headerName: 'Kayıt', width: 90, type: 'number' },
    { field: 'createdAt', headerName: 'Tarih', minWidth: 180, valueFormatter: (value) => new Date(value).toLocaleString('tr-TR') },
  ], [])

  return (
    <Box>
      <PageHeader
        title="Değişiklik Talepleri"
        subtitle="Bir talepteki tüm satırlar tek onay kararı ve tek transaction ile uygulanır."
        actions={
          <FormControl size="small" sx={{ minWidth: 180 }}>
            <InputLabel>Durum</InputLabel>
            <Select value={status} label="Durum" onChange={(event) => setStatus(event.target.value)}>
              {statuses.map(([value, label]) => <MenuItem key={value} value={value}>{label}</MenuItem>)}
            </Select>
          </FormControl>
        }
      />
      <ErrorAlert error={query.error} />
      <Card sx={{ height: 620 }}>
        <DataGrid
          rows={query.data?.content || []}
          columns={columns}
          rowCount={query.data?.totalElements || 0}
          loading={query.isLoading}
          paginationMode="server"
          paginationModel={pageModel}
          onPaginationModelChange={setPageModel}
          pageSizeOptions={[10, 20, 50]}
          onRowDoubleClick={(params) => navigate(`/requests/${params.id}`)}
          onRowClick={(params) => navigate(`/requests/${params.id}`)}
        />
      </Card>
    </Box>
  )
}

