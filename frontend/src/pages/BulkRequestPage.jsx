import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Alert, Box, Button, Card, CardContent, TextField, Typography } from '@mui/material'
import { requestApi } from '../api/parameterApi'
import PageHeader from '../components/PageHeader'
import ErrorAlert from '../components/ErrorAlert'

const example = {
  title: 'Yeni ürün ve oran tanımı',
  description: 'İlişkili tablolar aynı talep ve transaction içinde oluşturulur.',
  items: [
    {
      resourceCode: 'LOAN_PRODUCT',
      operation: 'CREATE',
      clientReference: 'product-1',
      executionOrder: 10,
      newValue: { code: 'TL_TAKSITLI', name: 'TL Taksitli Ticari Kredi', currency: 'TRY', status: 'ACTIVE' },
    },
    {
      resourceCode: 'LOAN_RATE',
      operation: 'CREATE',
      clientReference: 'rate-1',
      referenceBindings: { productCode: 'product-1' },
      executionOrder: 20,
      newValue: { termMonth: 12, minAmount: 10000, interestRate: 4.35, effectiveFrom: '2026-09-01' },
    },
  ],
}

export default function BulkRequestPage() {
  const [json, setJson] = useState(JSON.stringify(example, null, 2))
  const [parseError, setParseError] = useState('')
  const mutation = useMutation({ mutationFn: requestApi.create })

  const submit = () => {
    try {
      setParseError('')
      mutation.mutate(JSON.parse(json))
    } catch (error) {
      setParseError(error.message)
    }
  }

  return (
    <Box>
      <PageHeader
        title="Bulk / Aggregate Talep"
        subtitle="CREATE, UPDATE ve DELETE item’ları tek talepte karıştırılabilir; clientReference ile ilişkiler kurulabilir."
      />
      <ErrorAlert error={mutation.error || (parseError ? new Error(parseError) : null)} />
      {mutation.data && <Alert severity="success" sx={{ mb: 2 }}>
        {mutation.data.requestNo} numaralı talep onaya gönderildi.
      </Alert>}
      <Card>
        <CardContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Bu demo JSON editörü API’nin tüm bulk kabiliyetini gösterir. Kurumsal kullanımda CSV/Excel import adaptörü aynı endpoint’e talep üretir.
          </Typography>
          <TextField
            fullWidth
            multiline
            minRows={24}
            value={json}
            onChange={(event) => setJson(event.target.value)}
            sx={{ '& textarea': { fontFamily: 'monospace', fontSize: 13 } }}
          />
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
            <Button variant="contained" disabled={mutation.isPending} onClick={submit}>Onaya Gönder</Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}

