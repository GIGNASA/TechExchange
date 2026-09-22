import { createContext, useContext, useEffect, useState } from 'react'
import { fetchCurrentUser } from '../api/auth'
import { TOKEN_KEY } from '../api/token'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [authReady, setAuthReady] = useState(false)

  useEffect(() => {
    let ignore = false

    async function syncUser() {
      if (!token) {
        if (!ignore) {
          setUser(null)
          setAuthReady(true)
        }

        return
      }

      try {
        const profile = await fetchCurrentUser()
        if (!ignore) {
          setUser(profile)
        }
      } catch {
        if (!ignore) {
          logout()
        }
      } finally {
        if (!ignore) {
          setAuthReady(true)
        }
      }
    }

    setAuthReady(false)
    syncUser()

    return () => {
      ignore = true
    }
  }, [token])

  useEffect(() => {
    function handleUnauthorized() {
      logout()
    }

    window.addEventListener('auth:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized)
  }, [])

  function applyAuth(authResponse) {
    localStorage.setItem(TOKEN_KEY, authResponse.token)
    setToken(authResponse.token)
    setUser({
      email: authResponse.email,
      fullName: authResponse.fullName,
      role: authResponse.role,
    })
    setAuthReady(true)
  }

  function setUserProfile(profile) {
    setUser(profile)
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
    setAuthReady(true)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        authReady,
        isAuthenticated: Boolean(token),
        login: applyAuth,
        setUserProfile,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
