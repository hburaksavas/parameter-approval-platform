import { createContext, useContext, useMemo, useState } from 'react'

const USERS = {
  editor: { id: 'maker01', name: 'Talep Oluşturan', roles: ['PARAMETER_EDITOR', 'PARAMETER_VIEWER'] },
  approver: { id: 'approver01', name: 'Onaycı Kullanıcı', roles: ['PARAMETER_APPROVER', 'PARAMETER_VIEWER'] },
  viewer: { id: 'viewer01', name: 'Görüntüleyici', roles: ['PARAMETER_VIEWER'] },
}

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [mode, setModeState] = useState(() => localStorage.getItem('parameter-demo-mode') || 'editor')
  const user = USERS[mode]
  localStorage.setItem('parameter-demo-user', JSON.stringify(user))

  const setMode = (nextMode) => {
    setModeState(nextMode)
    localStorage.setItem('parameter-demo-mode', nextMode)
    localStorage.setItem('parameter-demo-user', JSON.stringify(USERS[nextMode]))
    window.location.reload()
  }

  const value = useMemo(() => ({ mode, setMode, user, users: USERS }), [mode, user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext)
