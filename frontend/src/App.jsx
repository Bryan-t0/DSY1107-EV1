import { useEffect, useMemo, useState } from 'react'

const localConfig = {
  region: import.meta.env.VITE_AWS_REGION || 'us-east-1',
  cognitoDomain: import.meta.env.VITE_COGNITO_DOMAIN || 'https://dsy1107-bryant-001.auth.us-east-1.amazoncognito.com',
  clientId: import.meta.env.VITE_COGNITO_CLIENT_ID || '6t8gda2gsqh5h61jk5rgclc55s',
  redirectUri: import.meta.env.VITE_REDIRECT_URI || 'http://localhost:5173/',
  apiUrl: import.meta.env.VITE_API_URL || 'https://xxcvnolhf8.execute-api.us-east-1.amazonaws.com/datos'
}

const b64url = (bytes) => btoa(String.fromCharCode(...new Uint8Array(bytes))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
const verifier = () => Array.from(crypto.getRandomValues(new Uint8Array(32))).map(b => b.toString(16).padStart(2, '0')).join('')
const challenge = async (value) => b64url(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(value)))

function decodeJwt(token) {
  try {
    const part = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(decodeURIComponent(atob(part).split('').map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0')).join('')))
  } catch { return null }
}

async function loadConfig() {
  try {
    const r = await fetch('/config.json', { cache: 'no-store' })
    if (r.ok) return { ...localConfig, ...(await r.json()) }
  } catch {}
  return localConfig
}

export default function App() {
  const [config, setConfig] = useState(localConfig)
  const [accessToken, setAccessToken] = useState(sessionStorage.getItem('access_token') || '')
  const [idToken, setIdToken] = useState(sessionStorage.getItem('id_token') || '')
  const [message, setMessage] = useState('')
  const [apiData, setApiData] = useState(null)
  const [apiError, setApiError] = useState('')
  const [loading, setLoading] = useState(false)
  const [pedidos, setPedidos] = useState([])
  const [descripcion, setDescripcion] = useState('')
  const [pedidoError, setPedidoError] = useState('')
  const claims = useMemo(() => decodeJwt(accessToken), [accessToken])
  const grupos = claims?.['cognito:groups'] || []
  const esAprobador = grupos.includes('APROBADOR')
  const apiBase = config.apiUrl.replace(/\/datos$/, '')

  useEffect(() => { loadConfig().then(setConfig) }, [])

  useEffect(() => {
    const code = new URLSearchParams(window.location.search).get('code')
    if (code && !accessToken) exchangeCodeForToken(code)
  }, [config.cognitoDomain])

  useEffect(() => {
    if (accessToken) cargarPedidos()
  }, [accessToken, config.apiUrl])

  const login = async () => {
    const v = verifier()
    sessionStorage.setItem('code_verifier', v)
    const c = await challenge(v)
    const url = `${config.cognitoDomain}/login?client_id=${encodeURIComponent(config.clientId)}&response_type=code&scope=openid+email+profile&redirect_uri=${encodeURIComponent(config.redirectUri)}&code_challenge_method=S256&code_challenge=${c}`
    window.location.href = url
  }

  const exchangeCodeForToken = async (code) => {
    const codeVerifier = sessionStorage.getItem('code_verifier')
    if (!codeVerifier) return setMessage('No se encontró code_verifier.')
    const body = new URLSearchParams({ grant_type: 'authorization_code', client_id: config.clientId, code, redirect_uri: config.redirectUri, code_verifier: codeVerifier })

    try {
      const r = await fetch(`${config.cognitoDomain}/oauth2/token`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body })
      const data = await r.json()
      if (!r.ok) throw new Error(data.error_description || data.error || 'No fue posible obtener el token')

      for (const key of ['access_token','id_token','refresh_token']) {
        if (data[key]) sessionStorage.setItem(key, data[key])
      }

      setAccessToken(data.access_token || '')
      setIdToken(data.id_token || '')
      setMessage('Sesión iniciada correctamente.')
      window.history.replaceState({}, document.title, window.location.pathname)
    } catch (e) {
      setMessage(`Error Cognito: ${e.message}`)
    }
  }

  const refreshAccessToken = async () => {
    const refresh = sessionStorage.getItem('refresh_token')
    if (!refresh) return setMessage('No hay refresh token disponible.')

    const body = new URLSearchParams({ grant_type: 'refresh_token', client_id: config.clientId, refresh_token: refresh })
    const r = await fetch(`${config.cognitoDomain}/oauth2/token`, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body })
    const data = await r.json()

    if (!r.ok) return setMessage('No se pudo renovar el token.')

    sessionStorage.setItem('access_token', data.access_token)
    if (data.id_token) sessionStorage.setItem('id_token', data.id_token)
    setAccessToken(data.access_token)
    if (data.id_token) setIdToken(data.id_token)
    setMessage('Token renovado.')
  }

  const authFetch = (url, options = {}) =>
    fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accessToken}`,
        ...(options.headers || {})
      }
    })

  const getApiData = async () => {
    setLoading(true)
    setApiError('')
    setApiData(null)

    try {
      const r = await authFetch(config.apiUrl)
      const text = await r.text()
      let data
      try { data = JSON.parse(text) } catch { data = text }

      if (!r.ok) throw new Error(`HTTP ${r.status}: ${typeof data === 'string' ? data : JSON.stringify(data)}`)
      setApiData(data)
    } catch (e) {
      setApiError(e.message)
    } finally {
      setLoading(false)
    }
  }

  const cargarPedidos = async () => {
    try {
      const r = await authFetch(`${apiBase}/pedidos`)
      if (!r.ok) throw new Error(`HTTP ${r.status}`)
      setPedidos(await r.json())
      setPedidoError('')
    } catch (e) {
      setPedidoError(`No se pudieron cargar los pedidos: ${e.message}`)
    }
  }

  const crearPedido = async (e) => {
    e.preventDefault()
    if (!descripcion.trim()) return

    try {
      const r = await authFetch(`${apiBase}/pedidos`, {
        method: 'POST',
        body: JSON.stringify({ descripcion })
      })

      if (!r.ok) throw new Error(`HTTP ${r.status}`)
      setDescripcion('')
      await cargarPedidos()
    } catch (e) {
      setPedidoError(`No se pudo crear el pedido: ${e.message}`)
    }
  }

  const cambiarEstado = async (id, accion) => {
    try {
      const r = await authFetch(`${apiBase}/pedidos/${id}/${accion}`, { method: 'PUT' })
      if (!r.ok) throw new Error(`HTTP ${r.status}`)
      await cargarPedidos()
    } catch (e) {
      setPedidoError(`No se pudo actualizar el pedido: ${e.message}`)
    }
  }

  const logout = () => {
    sessionStorage.clear()
    setAccessToken('')
    setIdToken('')
    window.location.href = `${config.cognitoDomain}/logout?client_id=${encodeURIComponent(config.clientId)}&logout_uri=${encodeURIComponent(config.redirectUri)}`
  }

  return <main className="container">
    <section className="hero">
      <div>
        <p className="eyebrow">GESTIÓN DE PEDIDOS</p>
        <h1>Pedidos360</h1>
        <p className="subtitle">Registro, seguimiento y aprobación de pedidos en una arquitectura cloud segura.</p>
      </div>

      {!accessToken
        ? <button onClick={login}>Iniciar sesión</button>
        : <div className="actions">
            <span className="role">{esAprobador ? 'APROBADOR' : 'SOLICITANTE'}</span>
            <button className="secondary" onClick={refreshAccessToken}>Renovar token</button>
            <button className="secondary" onClick={logout}>Cerrar sesión</button>
          </div>
      }
    </section>

    {message && <p className="notice">{message}</p>}

    {accessToken && <>
      <section className="grid">
        <div className="card">
          <h2>Nuevo pedido</h2>
          <form onSubmit={crearPedido}>
            <label>Descripción</label>
            <textarea
              value={descripcion}
              onChange={e => setDescripcion(e.target.value)}
              placeholder="Ej: 10 notebooks para el área comercial"
              rows="4"
            />
            <button type="submit">Crear pedido</button>
          </form>
        </div>

        <div className="card summary">
          <h2>Resumen</h2>
          <div className="summary-number">{pedidos.length}</div>
          <p>Pedidos registrados</p>
          <p><strong>Rol:</strong> {esAprobador ? 'APROBADOR' : 'SOLICITANTE'}</p>
          <button className="secondary" onClick={getApiData}>Verificar API</button>
        </div>
      </section>

      <section className="card">
        <div className="section-head">
          <h2>Pedidos</h2>
          <button className="secondary" onClick={cargarPedidos}>Actualizar</button>
        </div>

        {pedidoError && <p className="error">{pedidoError}</p>}

        {pedidos.length === 0
          ? <p className="empty">Todavía no hay pedidos registrados.</p>
          : <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Descripción</th>
                    <th>Estado</th>
                    {esAprobador && <th>Acciones</th>}
                  </tr>
                </thead>
                <tbody>
                  {pedidos.map(pedido => <tr key={pedido.id}>
                    <td>#{pedido.id}</td>
                    <td>{pedido.descripcion}</td>
                    <td><span className={`badge ${pedido.estado?.toLowerCase()}`}>{pedido.estado}</span></td>
                    {esAprobador && <td className="row-actions">
                      <button onClick={() => cambiarEstado(pedido.id, 'aprobar')}>Aprobar</button>
                      <button className="danger" onClick={() => cambiarEstado(pedido.id, 'rechazar')}>Rechazar</button>
                    </td>}
                  </tr>)}
                </tbody>
              </table>
            </div>
        }
      </section>

      {apiData && <section className="card"><h2>Verificación técnica</h2><pre>{JSON.stringify(apiData, null, 2)}</pre></section>}
      {apiError && <p className="error">{apiError}</p>}
      {loading && <p>Cargando…</p>}
    </>}
  </main>
}
