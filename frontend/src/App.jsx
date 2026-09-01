import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, Navigate, Route, Routes } from 'react-router-dom'
import {
  AppBar, Box, Divider, Drawer, FormControl, InputLabel, List, ListItemButton,
  ListItemText, MenuItem, Select, Toolbar, Typography,
} from '@mui/material'
import FactCheckOutlinedIcon from '@mui/icons-material/FactCheckOutlined'
import TuneOutlinedIcon from '@mui/icons-material/TuneOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import { customQueryApi, parameterApi } from './api/parameterApi'
import { useAuth } from './context/AuthContext'
import ParameterListPage from './pages/ParameterListPage'
import ApprovalInboxPage from './pages/ApprovalInboxPage'
import ApprovalDetailPage from './pages/ApprovalDetailPage'
import BulkRequestPage from './pages/BulkRequestPage'
import CustomQueryPage from './pages/CustomQueryPage'

const drawerWidth = 280

export default function App() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const { mode, setMode, user, users } = useAuth()
  const { data: resources = [] } = useQuery({ queryKey: ['resources'], queryFn: parameterApi.resources })
  const { data: customQueries = [] } = useQuery({ queryKey: ['custom-queries'], queryFn: customQueryApi.all })

  const drawer = (
    <Box>
      <Toolbar>
        <TuneOutlinedIcon sx={{ mr: 1 }} />
        <Typography variant="h6">Parametre Yönetimi</Typography>
      </Toolbar>
      <Divider />
      <List dense>
        <ListItemButton component={Link} to="/requests">
          <FactCheckOutlinedIcon sx={{ mr: 2 }} />
          <ListItemText primary="Değişiklik Talepleri" />
        </ListItemButton>
        <ListItemButton component={Link} to="/bulk">
          <UploadFileOutlinedIcon sx={{ mr: 2 }} />
          <ListItemText primary="Bulk / Aggregate Talep" />
        </ListItemButton>
      </List>
      <Divider />
      <Typography variant="overline" sx={{ px: 2, pt: 2, display: 'block' }}>Parametreler</Typography>
      <List dense>
        {resources.map((resource) => (
          <ListItemButton key={resource.code} component={Link} to={`/resources/${resource.code}`}>
            <ListItemText primary={resource.title} secondary={resource.code} />
          </ListItemButton>
        ))}
      </List>
      {customQueries.length > 0 && <Divider />}
      {customQueries.length > 0 && (
        <Typography variant="overline" sx={{ px: 2, pt: 2, display: 'block' }}>Özel Sorgular</Typography>
      )}
      <List dense>
        {customQueries.map((query) => (
          <ListItemButton key={query.code} component={Link} to={`/custom/${query.code}`}>
            <SearchOutlinedIcon sx={{ mr: 2, fontSize: 19 }} />
            <ListItemText primary={query.title} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Ürün Parametre Onay Platformu</Typography>
          {import.meta.env.VITE_DEMO_AUTH !== 'false' && (
            <FormControl size="small" sx={{ minWidth: 190, bgcolor: 'white', borderRadius: 1 }}>
              <InputLabel>Demo kullanıcı</InputLabel>
              <Select value={mode} label="Demo kullanıcı" onChange={(event) => setMode(event.target.value)}>
                {Object.entries(users).map(([key, value]) => (
                  <MenuItem key={key} value={key}>{value.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
          )}
          <Typography variant="caption" sx={{ ml: 2 }}>{user.id}</Typography>
        </Toolbar>
      </AppBar>
      <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: drawerWidth } }}
        >{drawer}</Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', md: 'block' }, '& .MuiDrawer-paper': { width: drawerWidth } }}
          open
        >{drawer}</Drawer>
      </Box>
      <Box component="main" sx={{ flexGrow: 1, p: 3, width: { md: `calc(100% - ${drawerWidth}px)` } }}>
        <Toolbar />
        <Routes>
          <Route path="/" element={<Navigate to="/requests" replace />} />
          <Route path="/resources/:code" element={<ParameterListPage />} />
          <Route path="/requests" element={<ApprovalInboxPage />} />
          <Route path="/requests/:id" element={<ApprovalDetailPage />} />
          <Route path="/bulk" element={<BulkRequestPage />} />
          <Route path="/custom/:code" element={<CustomQueryPage />} />
        </Routes>
      </Box>
    </Box>
  )
}

