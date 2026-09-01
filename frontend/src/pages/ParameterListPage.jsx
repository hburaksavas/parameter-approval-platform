import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useParams } from 'react-router-dom'
import { Alert, Box, Button, Card, Snackbar } from '@mui/material'
import { DataGrid } from '@mui/x-data-grid'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { parameterApi, requestApi } from '../api/parameterApi'
import { useAuth } from '../context/AuthContext'
import PageHeader from '../components/PageHeader'
import ErrorAlert from '../components/ErrorAlert'
import DynamicFilterPanel from '../components/DynamicFilterPanel'
import ChangeRequestDialog from '../components/ChangeRequestDialog'
import DeleteRequestDialog from '../components/DeleteRequestDialog'

export default function ParameterListPage() {
  const { code } = useParams()
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const canEdit = user.roles.includes('PARAMETER_EDITOR')
  const [filters, setFilters] = useState([])
  const [pageModel, setPageModel] = useState({ page: 0, pageSize: 20 })
  const [selectedRow, setSelectedRow] = useState(null)
  const [dialogMode, setDialogMode] = useState(null)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [notice, setNotice] = useState('')

  const metadataQuery = useQuery({ queryKey: ['metadata', code], queryFn: () => parameterApi.metadata(code) })
  const dataQuery = useQuery({
    queryKey: ['parameter-search', code, filters, pageModel],
    queryFn: () => parameterApi.search(code, {
      filters,
      page: pageModel.page,
      size: pageModel.pageSize,
      sort: [],
    }),
    enabled: Boolean(metadataQuery.data),
  })
  const mutation = useMutation({
    mutationFn: requestApi.create,
    onSuccess: (request) => {
      setDialogMode(null)
      setDeleteOpen(false)
      setNotice(`${request.requestNo} numaralı talep onaya gönderildi.`)
      queryClient.invalidateQueries({ queryKey: ['requests'] })
    },
  })

  const metadata = metadataQuery.data
  const columns = useMemo(() => (metadata?.fields || [])
    .filter((field) => field.visible)
    .map((field) => ({ field: field.name, headerName: field.label, minWidth: 140, flex: 1 })), [metadata])

  const submitChange = ({ title, description, value }) => {
    const id = dialogMode === 'UPDATE' ? String(selectedRow[metadata.idField]) : undefined
    mutation.mutate({
      title,
      description,
      items: [{
        resourceCode: code,
        operation: dialogMode,
        recordId: id,
        newValue: value,
        executionOrder: 0,
      }],
    })
  }

  const submitDelete = (description) => mutation.mutate({
    title: `${metadata.title} kayıt silme`,
    description,
    items: [{
      resourceCode: code,
      operation: 'DELETE',
      recordId: String(selectedRow[metadata.idField]),
      executionOrder: 0,
    }],
  })

  return (
    <Box>
      <PageHeader
        title={metadata?.title || code}
        subtitle="Filtreler entity anotasyonlarından dinamik olarak oluşturulur."
        actions={canEdit && <>
          <Button startIcon={<AddIcon />} variant="contained" onClick={() => { setSelectedRow(null); setDialogMode('CREATE') }}>
            Yeni
          </Button>
          <Button startIcon={<EditIcon />} disabled={!selectedRow} onClick={() => setDialogMode('UPDATE')}>Güncelle</Button>
          <Button startIcon={<DeleteIcon />} color="error" disabled={!selectedRow} onClick={() => setDeleteOpen(true)}>Sil</Button>
        </>}
      />
      <ErrorAlert error={metadataQuery.error || dataQuery.error || mutation.error} />
      {metadata && <DynamicFilterPanel key={metadata.code} metadata={metadata} onSearch={(next) => { setFilters(next); setPageModel({ ...pageModel, page: 0 }) }} />}
      <Card sx={{ height: 560 }}>
        <DataGrid
          rows={dataQuery.data?.content || []}
          columns={columns}
          getRowId={(row) => row[metadata?.idField]}
          rowCount={dataQuery.data?.totalElements || 0}
          loading={dataQuery.isLoading}
          paginationMode="server"
          paginationModel={pageModel}
          onPaginationModelChange={setPageModel}
          pageSizeOptions={[10, 20, 50, 100]}
          onRowClick={(params) => setSelectedRow(params.row)}
          disableRowSelectionOnClick
        />
      </Card>
      <ChangeRequestDialog
        open={Boolean(dialogMode)}
        mode={dialogMode}
        metadata={metadata}
        selectedRow={dialogMode === 'UPDATE' ? selectedRow : null}
        loading={mutation.isPending}
        onClose={() => setDialogMode(null)}
        onSubmit={submitChange}
      />
      <DeleteRequestDialog
        open={deleteOpen}
        row={selectedRow}
        loading={mutation.isPending}
        onClose={() => setDeleteOpen(false)}
        onSubmit={submitDelete}
      />
      <Snackbar open={Boolean(notice)} autoHideDuration={5000} onClose={() => setNotice('')}>
        <Alert severity="success" onClose={() => setNotice('')}>{notice}</Alert>
      </Snackbar>
    </Box>
  )
}
