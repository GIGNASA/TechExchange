import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import DeviceDetails from './pages/DeviceDetails'
import Login from './pages/Login'
import Marketplace from './pages/Marketplace'
import Profile from './pages/Profile'
import Register from './pages/Register'
import Requests from './pages/Requests'

import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'
import Navbar from './components/Navbar'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="app-shell">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route
              path="/dashboard"
              element={
                <PrivateRoute>
                  <Marketplace />
                </PrivateRoute>
              }
            />
            <Route
              path="/requests"
              element={
                <PrivateRoute>
                  <Requests />
                </PrivateRoute>
              }
            />
            <Route
              path="/devices/:id"
              element={
                <PrivateRoute>
                  <DeviceDetails />
                </PrivateRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <PrivateRoute>
                  <Profile />
                </PrivateRoute>
              }
            />
          </Routes>
        </main>
      </BrowserRouter>
    </AuthProvider>
  )
}
