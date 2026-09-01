import { useEffect, useState } from 'react'
import {
  Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography,
} from '@mui/material'
import DynamicParameterForm from './DynamicParameterForm'

export default function ChangeRequestDialog({ open, mode, metadata, selectedRow, loading, onClose, onSubmit }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [value, setValue] = useState(selectedRow || {})

  useEffect(() => {
    setTitle(mode === 'CREATE' ? `Yeni ${metadata?.title}` : `${metadata?.title} güncelleme`)
    setDescription('')
    setValue(selectedRow || {})
  }, [open, mode, metadata, selectedRow])

  if (!metadata) return null

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{mode === 'CREATE' ? 'Yeni kayıt talebi' : 'Güncelleme talebi'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography color="text.secondary">
            Değişiklik canlı tabloya hemen yazılmaz; onaycı kararından sonra uygulanır.
          </Typography>
          <TextField label="Talep başlığı" required value={title} onChange={(e) => setTitle(e.target.value)} />
          <TextField
            label="Açıklama"
            multiline
            minRows={2}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
          <DynamicParameterForm
            metadata={metadata}
            initialValue={selectedRow || {}}
            mode={mode}
            onChange={setValue}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Vazgeç</Button>
        <Button
          variant="contained"
          disabled={!title.trim() || loading}
          onClick={() => onSubmit({ title, description, value })}
        >Onaya Gönder</Button>
      </DialogActions>
    </Dialog>
  )
}

