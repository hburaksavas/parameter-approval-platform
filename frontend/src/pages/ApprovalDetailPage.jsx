import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Card, CardContent,
  Chip, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Grid, Stack, TextField, Typography,
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline'
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined'
import { requestApi } from '../api/parameterApi'
import { useAuth } from '../context/AuthContext'
import PageHeader from '../components/PageHeader'
import ErrorAlert from '../components/ErrorAlert'
import StatusChip from '../components/StatusChip'
import OldNewComparison from '../components/OldNewComparison'

export default function ApprovalDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user } = useAuth()
  const [decision, setDecision] = useState(null)
  const [note, setNote] = useState('')
  const query = useQuery({ queryKey: ['request', id], queryFn: () => requestApi.detail(id) })
  const mutation = useMutation({
    mutationFn: () => decision === 'approve' ? requestApi.approve(id, note) : requestApi.reject(id, note),
    onSuccess: () => {
      setDecision(null)
      queryClient.invalidateQueries({ queryKey: ['request', id] })
      queryClient.invalidateQueries({ queryKey: ['requests'] })
    },
  })
  const request = query.data
  const canDecide = request?.status === 'WAITING_APPROVAL'
    && user.roles.includes('PARAMETER_APPROVER')
    && request.createdBy !== user.id

  return (
    <Box>
      <PageHeader
        title={request?.title || 'Talep detayı'}
        subtitle={request?.requestNo}
        actions={<>
          <Button onClick={() => navigate('/requests')}>Listeye dön</Button>
          {canDecide && <Button color="error" startIcon={<CancelOutlinedIcon />} onClick={() => setDecision('reject')}>Reddet</Button>}
          {canDecide && <Button variant="contained" color="success" startIcon={<CheckCircleOutlineIcon />}
            onClick={() => setDecision('approve')}>Onayla</Button>}
        </>}
      />
      <ErrorAlert error={query.error || mutation.error} />
      {request && (
        <>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Grid container spacing={2}>
                <Grid item xs={12} md={3}><Typography variant="caption">Durum</Typography><Box><StatusChip status={request.status} /></Box></Grid>
                <Grid item xs={12} md={3}><Typography variant="caption">Talep Eden</Typography><Typography>{request.createdByName}</Typography></Grid>
                <Grid item xs={12} md={3}><Typography variant="caption">Tarih</Typography><Typography>{new Date(request.createdAt).toLocaleString('tr-TR')}</Typography></Grid>
                <Grid item xs={12} md={3}><Typography variant="caption">Değişiklik Sayısı</Typography><Typography>{request.itemCount}</Typography></Grid>
                {request.description && <Grid item xs={12}><Divider sx={{ mb: 2 }} /><Typography>{request.description}</Typography></Grid>}
                {request.decisionNote && <Grid item xs={12}><Alert severity={request.status === 'APPROVED' ? 'success' : 'warning'}>{request.decisionNote}</Alert></Grid>}
              </Grid>
            </CardContent>
          </Card>
          <Stack spacing={1.5}>
            {request.items?.map((item, index) => (
              <Accordion key={item.id} defaultExpanded={index === 0}>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Stack direction="row" spacing={1} alignItems="center">
                    <Chip size="small" label={item.operation} color={item.operation === 'DELETE' ? 'error' : item.operation === 'CREATE' ? 'success' : 'info'} />
                    <Typography fontWeight={600}>{item.resourceCode}</Typography>
                    <Typography color="text.secondary">{item.recordId || item.clientReference || 'Yeni kayıt'}</Typography>
                  </Stack>
                </AccordionSummary>
                <AccordionDetails sx={{ p: 0 }}>
                  <OldNewComparison oldValue={item.oldValue} newValue={item.newValue} />
                </AccordionDetails>
              </Accordion>
            ))}
          </Stack>
        </>
      )}
      <Dialog open={Boolean(decision)} onClose={() => setDecision(null)} maxWidth="sm" fullWidth>
        <DialogTitle>{decision === 'approve' ? 'Talebi onayla' : 'Talebi reddet'}</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2 }}>
            {decision === 'approve'
              ? 'Tüm değişiklikler tek Oracle transaction’ında uygulanacaktır.'
              : 'Talep canlı tablolara uygulanmadan reddedilecektir.'}
          </Typography>
          <TextField
            fullWidth
            multiline
            minRows={3}
            required={decision === 'reject'}
            label={decision === 'approve' ? 'Onay notu' : 'Red gerekçesi'}
            value={note}
            onChange={(event) => setNote(event.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDecision(null)}>Vazgeç</Button>
          <Button
            variant="contained"
            color={decision === 'approve' ? 'success' : 'error'}
            disabled={mutation.isPending || (decision === 'reject' && !note.trim())}
            onClick={() => mutation.mutate()}
          >Kararı Uygula</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

