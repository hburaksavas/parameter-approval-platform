import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import OldNewComparison from './OldNewComparison'

describe('OldNewComparison', () => {
  it('shows old and new field values', () => {
    render(<OldNewComparison oldValue={{ status: 'PASSIVE' }} newValue={{ status: 'ACTIVE' }} />)
    expect(screen.getByText('status')).toBeInTheDocument()
    expect(screen.getByText('PASSIVE')).toBeInTheDocument()
    expect(screen.getByText('ACTIVE')).toBeInTheDocument()
  })
})
