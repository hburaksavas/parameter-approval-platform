import { useEffect, useMemo, useState } from 'react'
import { useQueries } from '@tanstack/react-query'
import { Grid } from '@mui/material'
import { parameterApi } from '../api/parameterApi'
import DynamicField from './DynamicField'

export default function DynamicParameterForm({ metadata, initialValue = {}, mode, onChange }) {
  const [values, setValues] = useState(initialValue)
  const fields = useMemo(
    () => metadata.fields.filter((field) => field.visible && !(mode === 'CREATE' && field.generated)),
    [metadata, mode],
  )
  const optionQueries = useQueries({
    queries: fields.map((field) => ({
      queryKey: ['options', metadata.code, field.name],
      queryFn: () => parameterApi.options(metadata.code, field.name),
      enabled: field.filterInput === 'SELECT' || field.dataType === 'ENUM' || Boolean(field.reference),
      staleTime: 300_000,
    })),
  })

  useEffect(() => {
    setValues(initialValue || {})
  }, [initialValue])

  const update = (name, value) => {
    const next = { ...values, [name]: value === '' ? null : value }
    setValues(next)
    onChange(next)
  }

  return (
    <Grid container spacing={2}>
      {fields.map((field, index) => (
        <Grid item xs={12} md={6} key={field.name}>
          <DynamicField
            field={field}
            value={values[field.name]}
            onChange={(value) => update(field.name, value)}
            options={optionQueries[index]?.data || []}
            disabled={mode === 'UPDATE' && (!field.editable || field.id)}
          />
        </Grid>
      ))}
    </Grid>
  )
}

