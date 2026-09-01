import { FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'

export default function DynamicField({ field, value, onChange, options = [], disabled = false }) {
  const common = { fullWidth: true, required: field.required, disabled }
  const select = field.filterInput === 'SELECT' || field.dataType === 'ENUM' || field.reference

  if (select && options.length > 0) {
    return (
      <FormControl {...common}>
        <InputLabel>{field.label}</InputLabel>
        <Select value={value ?? ''} label={field.label} onChange={(e) => onChange(e.target.value)}>
          {!field.required && <MenuItem value=""><em>Boş</em></MenuItem>}
          {options.map((option) => (
            <MenuItem key={String(option.value)} value={option.value}>{option.label}</MenuItem>
          ))}
        </Select>
      </FormControl>
    )
  }

  const type = field.dataType === 'NUMBER' ? 'number' : field.dataType === 'DATE' ? 'date' : 'text'
  return (
    <TextField
      {...common}
      type={type}
      label={field.label}
      value={value ?? ''}
      onChange={(event) => onChange(event.target.value)}
      slotProps={type === 'date' ? { inputLabel: { shrink: true } } : undefined}
    />
  )
}

