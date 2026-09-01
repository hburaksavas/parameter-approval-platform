import { useMemo, useState } from 'react'
import { useQueries } from '@tanstack/react-query'
import { Box, Button, Card, CardContent, FormControl, Grid, InputLabel, MenuItem, Select } from '@mui/material'
import { parameterApi } from '../api/parameterApi'
import DynamicField from './DynamicField'

export default function DynamicFilterPanel({ metadata, onSearch }) {
  const fields = useMemo(() => metadata.fields.filter((field) => field.filterable), [metadata])
  const [values, setValues] = useState({})
  const [operators, setOperators] = useState(() => Object.fromEntries(fields.map((f) => [f.name, f.operators[0]])))
  const optionQueries = useQueries({
    queries: fields.map((field) => ({
      queryKey: ['options', metadata.code, field.name],
      queryFn: () => parameterApi.options(metadata.code, field.name),
      enabled: field.filterInput === 'SELECT' || field.dataType === 'ENUM' || Boolean(field.reference),
      staleTime: 300_000,
    })),
  })

  const search = () => {
    const filters = fields
      .filter((field) => values[field.name] !== undefined && values[field.name] !== null && values[field.name] !== '')
      .map((field) => ({ field: field.name, operator: operators[field.name], value: values[field.name] }))
    onSearch(filters)
  }

  const clear = () => {
    setValues({})
    onSearch([])
  }

  return (
    <Card sx={{ mb: 2 }}>
      <CardContent>
        <Grid container spacing={2}>
          {fields.map((field, index) => (
            <Grid item xs={12} lg={6} key={field.name}>
              <Box sx={{ display: 'grid', gridTemplateColumns: '150px 1fr', gap: 1 }}>
                <FormControl size="small">
                  <InputLabel>Operatör</InputLabel>
                  <Select
                    value={operators[field.name] || field.operators[0]}
                    label="Operatör"
                    onChange={(event) => setOperators({ ...operators, [field.name]: event.target.value })}
                  >
                    {field.operators.map((operator) => <MenuItem key={operator} value={operator}>{operator}</MenuItem>)}
                  </Select>
                </FormControl>
                <DynamicField
                  field={field}
                  value={values[field.name]}
                  onChange={(value) => setValues({ ...values, [field.name]: value })}
                  options={optionQueries[index]?.data || []}
                />
              </Box>
            </Grid>
          ))}
        </Grid>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
          <Button onClick={clear}>Temizle</Button>
          <Button variant="contained" onClick={search}>Sorgula</Button>
        </Box>
      </CardContent>
    </Card>
  )
}

