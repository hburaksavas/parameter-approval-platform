import { useState } from 'react'
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from '@mui/material'

export default function DeleteRequestDialog({ open, row, loading, onClose, onSubmit }) {
  const [description, setDescription] = useState('')
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Silme talebi</DialogTitle>
      <DialogContent>
        <Typography sx={{ mb: 2 }}>Seçili kayıt onay sonrasında silinecek.</Typography>
        <TextField
          fullWidth
          required
          label="Silme gerekçesi"
          multiline
          minRows={3}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
        <Typography component="pre" variant="caption" sx={{ whiteSpace: 'pre-wrap', mt: 2 }}>
          {JSON.stringify(row, null, 2)}
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Vazgeç</Button>
        <Button color="error" variant="contained" disabled={!description.trim() || loading}
          onClick={() => onSubmit(description)}>Onaya Gönder</Button>
      </DialogActions>
    </Dialog>
  )
}

