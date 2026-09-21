import { useEffect, useState } from "react";
import "./App.css";

const CLIENT_ID = "7sd8t3kru1jqackf0mmfiunii9";

const COGNITO_DOMAIN =
  "https://dsy1107-grupoxx-001.auth.us-east-1.amazoncognito.com";

const REDIRECT_URI = "http://localhost:5173/";

const API_URL =
  "https://mpva2w11r2.execute-api.us-east-1.amazonaws.com/Desarrollo/datos";

function base64UrlEncode(buffer) {
  return btoa(String.fromCharCode(...new Uint8Array(buffer)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

// crea el challenge usando el verifier
async function generateCodeChallenge(codeVerifier) {
  const encoder = new TextEncoder();
  const data = encoder.encode(codeVerifier);

  // transforma el verifier para crear el challenge
  const digest = await crypto.subtle.digest("SHA-256", data);

  return base64UrlEncode(digest);
}

// genera el codigo original que usa PKCE
function generateCodeVerifier() {
  const array = new Uint8Array(32);

  crypto.getRandomValues(array);

  return Array.from(array)
    .map((b) => ("0" + b.toString(16)).slice(-2))
    .join("");
}

function App() {
  const [accessToken, setAccessToken] = useState(
    sessionStorage.getItem("access_token") || ""
  );

  const [message, setMessage] = useState("");

  const [apiData, setApiData] = useState(null);

  const [apiError, setApiError] = useState("");

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);

    const code = params.get("code");

    if (code && !accessToken) {
      exchangeCodeForToken(code);
    }
  }, []);

  // aqui empieza el login
  const login = async () => {
    const codeVerifier = generateCodeVerifier();

    const codeChallenge = await generateCodeChallenge(codeVerifier);

    // guardo el verifier para ocuparlo cuando vuelva de Cognito
    sessionStorage.setItem("code_verifier", codeVerifier);

    const loginUrl =
      `${COGNITO_DOMAIN}/login?` +
      `client_id=${CLIENT_ID}` +
      // Cognito primero devuelve un codigo temporal
      `&response_type=code` +
      `&scope=openid+email+profile` +
      `&redirect_uri=${encodeURIComponent(REDIRECT_URI)}` +
      // le indico a Cognito que el challenge se hizo con SHA-256
      `&code_challenge_method=S256` +
      `&code_challenge=${codeChallenge}`;

    window.location.href = loginUrl;
  };

  // cambia el codigo temporal por los tokens
  const exchangeCodeForToken = async (code) => {
    const codeVerifier = sessionStorage.getItem("code_verifier");

    if (!codeVerifier) {
      setMessage("No se encontró el code_verifier.");
      return;
    }

    const body = new URLSearchParams({
      grant_type: "authorization_code",
      client_id: CLIENT_ID,
      code: code,
      redirect_uri: REDIRECT_URI,
      code_verifier: codeVerifier,
    });

    try {
      const response = await fetch(`${COGNITO_DOMAIN}/oauth2/token`, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body,
      });

      const data = await response.json();

      if (!response.ok) {
        console.error(data);

        setMessage("Error obteniendo el token.");

        return;
      }

      // token que voy a usar para entrar a la API
      sessionStorage.setItem("access_token", data.access_token);

      // token que trae informacion del usuario
      sessionStorage.setItem("id_token", data.id_token);

      setAccessToken(data.access_token);

      setMessage("JWT obtenido correctamente.");

      window.history.replaceState({}, document.title, "/");
    } catch (error) {
      console.error(error);

      setMessage("Error conectando con Cognito.");
    }
  };

  const getApiData = async () => {
    try {
      setLoading(true);

      setApiError("");

      setApiData(null);

      const response = await fetch(API_URL, {
        method: "GET",

        headers: {
          // mando el access token para poder entrar a la API
          Authorization: accessToken,
        },
      });

      const data = await response.json();

      if (!response.ok) {
        console.error(data);

        setApiError(
          `Error consumiendo API Gateway. Código: ${response.status}`
        );

        return;
      }

      setApiData(data);
    } catch (error) {
      console.error(error);

      setApiError("Error conectando con API Gateway.");
    } finally {
      setLoading(false);
    }
  };

  // borra la sesion y cierra sesion en Cognito
  const logout = () => {
    sessionStorage.clear();

    const logoutUrl =
      `${COGNITO_DOMAIN}/logout?` +
      `client_id=${CLIENT_ID}` +
      `&logout_uri=${encodeURIComponent(REDIRECT_URI)}`;

    window.location.href = logoutUrl;
  };

  return (
    <div>
      <h1>Cognito + React</h1>

      {!accessToken ? (
        <>
          <p>No has iniciado sesión.</p>

          <button onClick={login}>
            Iniciar sesión con Cognito
          </button>

          {message && <p>{message}</p>}
        </>
      ) : (
        <>
          <p>Sesión iniciada correctamente.</p>

          <p>{message}</p>

          <button onClick={getApiData}>
            Obtener datos de API
          </button>

          {loading && <p>Cargando datos...</p>}

          {apiError && (
            <p>
              {apiError}
            </p>
          )}

          {apiData && (
            <>
              <h2>Respuesta de API Gateway</h2>

              <pre
                style={{
                  textAlign: "left",
                  maxHeight: "400px",
                  overflow: "auto",
                  background: "#eeeeee",
                  padding: "15px",
                }}
              >
                {JSON.stringify(apiData, null, 2)}
              </pre>
            </>
          )}

          <br />
          <br />

          <button onClick={logout}>
            Cerrar sesión
          </button>
        </>
      )}
    </div>
  );
}

export default App;