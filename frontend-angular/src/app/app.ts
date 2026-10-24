import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface Pedido {
  id: number;
  descripcion: string;
  estado: string;
  comentario?: string | null;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {

  titulo = 'Pedidos360';

  apiUrl = 'http://localhost:8080';

  // Modo local para probar el sistema sin Cognito
  modoLocal = true;

  rolLocal: 'SOLICITANTE' | 'APROBADOR' =
    'SOLICITANTE';

  cognitoDomain =
    'https://dsy1107-bryant-001.auth.us-east-1.amazoncognito.com';

  clientId =
    '6t8gda2gsqh5h61jk5rgclc55s';

  redirectUri =
    'http://localhost:4200/';

  accessToken =
    sessionStorage.getItem('access_token') || '';

  descripcion = '';
  comentario = '';

  pedidos: Pedido[] = [];

  mensaje = '';
  error = '';

  constructor(
    private http: HttpClient
  ) {}

  ngOnInit(): void {

    if (this.modoLocal) {

      this.cargarPedidos();

      return;
    }

    const params =
      new URLSearchParams(
        window.location.search
      );

    const code =
      params.get('code');

    if (
      code &&
      !this.accessToken
    ) {

      this.intercambiarCodigo(code);
    }

    if (this.accessToken) {

      this.cargarPedidos();
    }
  }

  get autenticado(): boolean {

    if (this.modoLocal) {
      return true;
    }

    return !!this.accessToken;
  }

  get grupos(): string[] {

    if (this.modoLocal) {

      return [
        this.rolLocal
      ];
    }

    const payload =
      this.decodificarToken(
        this.accessToken
      );

    return payload?.['cognito:groups']
      || [];
  }

  get esAprobador(): boolean {

    return this.grupos.includes(
      'APROBADOR'
    );
  }

  get esSolicitante(): boolean {

    return this.grupos.includes(
      'SOLICITANTE'
    );
  }

  cambiarRol(): void {

    if (
      this.rolLocal ===
      'SOLICITANTE'
    ) {

      this.rolLocal =
        'APROBADOR';

    } else {

      this.rolLocal =
        'SOLICITANTE';
    }

    this.mensaje =
      `Modo local: ${this.rolLocal}`;

    this.error = '';
  }

  async login(): Promise<void> {

    if (this.modoLocal) {
      return;
    }

    const verifier =
      this.generarVerifier();

    sessionStorage.setItem(
      'code_verifier',
      verifier
    );

    const challenge =
      await this.generarChallenge(
        verifier
      );

    const url =
      `${this.cognitoDomain}/login`
      + `?client_id=${encodeURIComponent(this.clientId)}`
      + `&response_type=code`
      + `&scope=openid+email+profile`
      + `&redirect_uri=${encodeURIComponent(this.redirectUri)}`
      + `&code_challenge_method=S256`
      + `&code_challenge=${challenge}`;

    window.location.href = url;
  }

  logout(): void {

    if (this.modoLocal) {
      return;
    }

    sessionStorage.clear();

    this.accessToken = '';

    window.location.href =
      `${this.cognitoDomain}/logout`
      + `?client_id=${encodeURIComponent(this.clientId)}`
      + `&logout_uri=${encodeURIComponent(this.redirectUri)}`;
  }

  cargarPedidos(): void {

    this.http.get<Pedido[]>(
      `${this.apiUrl}/pedidos`,
      {
        headers:
          this.authHeaders()
      }
    ).subscribe({

      next: data => {

        this.pedidos =
          data;

        this.error =
          '';
      },

      error: err => {

        this.error =
          `Error cargando pedidos: ${err.status}`;
      }
    });
  }

  crearPedido(): void {

    if (
      !this.descripcion.trim()
    ) {

      this.error =
        'La descripción es obligatoria';

      return;
    }

    this.http.post<Pedido>(
      `${this.apiUrl}/pedidos`,
      {
        descripcion:
          this.descripcion.trim()
      },
      {
        headers:
          this.authHeaders()
      }
    ).subscribe({

      next: () => {

        this.descripcion =
          '';

        this.mensaje =
          'Pedido creado correctamente';

        this.error =
          '';

        this.cargarPedidos();
      },

      error: err => {

        this.error =
          `Error creando pedido: ${err.status}`;
      }
    });
  }

  aprobar(
    pedido: Pedido
  ): void {

    this.http.put(
      `${this.apiUrl}/pedidos/${pedido.id}/aprobar`,
      {
        comentario:
          this.comentario
      },
      {
        headers:
          this.authHeaders()
      }
    ).subscribe({

      next: () => {

        this.mensaje =
          'Pedido aprobado';

        this.comentario =
          '';

        this.error =
          '';

        this.cargarPedidos();
      },

      error: err => {

        this.error =
          `Error aprobando pedido: ${err.status}`;
      }
    });
  }

  rechazar(
    pedido: Pedido
  ): void {

    if (
      !this.comentario.trim()
    ) {

      this.error =
        'Debes ingresar un comentario para rechazar';

      return;
    }

    this.http.put(
      `${this.apiUrl}/pedidos/${pedido.id}/rechazar`,
      {
        comentario:
          this.comentario.trim()
      },
      {
        headers:
          this.authHeaders()
      }
    ).subscribe({

      next: () => {

        this.mensaje =
          'Pedido rechazado';

        this.comentario =
          '';

        this.error =
          '';

        this.cargarPedidos();
      },

      error: err => {

        this.error =
          `Error rechazando pedido: ${err.status}`;
      }
    });
  }

  editar(
    pedido: Pedido
  ): void {

    const nuevaDescripcion =
      window.prompt(
        'Nueva descripción',
        pedido.descripcion
      );

    if (
      !nuevaDescripcion?.trim()
    ) {

      return;
    }

    this.http.put(
      `${this.apiUrl}/pedidos/${pedido.id}`,
      {
        descripcion:
          nuevaDescripcion.trim()
      },
      {
        headers:
          this.authHeaders()
      }
    ).subscribe({

      next: () => {

        this.mensaje =
          'Pedido actualizado correctamente';

        this.error =
          '';

        this.cargarPedidos();
      },

      error: err => {

        this.error =
          `Error editando pedido: ${err.status}`;
      }
    });
  }

  eliminar(
    pedido: Pedido
  ): void {

    const confirmar =
      window.confirm(
        `¿Eliminar pedido #${pedido.id}?`
      );

    if (!confirmar) {
      return;
    }

    this.http.delete(
      `${this.apiUrl}/pedidos/${pedido.id}`,
      {
        headers:
          this.authHeaders(),
        responseType:
          'text'
      }
    ).subscribe({

      next: () => {

        this.mensaje =
          'Pedido eliminado correctamente';

        this.error =
          '';

        this.cargarPedidos();
      },

      error: err => {

        this.error =
          `Error eliminando pedido: ${err.status}`;
      }
    });
  }

  private authHeaders():
    HttpHeaders {

    let headers =
      new HttpHeaders({
        'Content-Type':
          'application/json'
      });

    if (
      !this.modoLocal &&
      this.accessToken
    ) {

      headers =
        headers.set(
          'Authorization',
          `Bearer ${this.accessToken}`
        );
    }

    return headers;
  }

  private async intercambiarCodigo(
    code: string
  ): Promise<void> {

    const verifier =
      sessionStorage.getItem(
        'code_verifier'
      );

    if (!verifier) {

      this.error =
        'No se encontró code_verifier';

      return;
    }

    const body =
      new URLSearchParams();

    body.set(
      'grant_type',
      'authorization_code'
    );

    body.set(
      'client_id',
      this.clientId
    );

    body.set(
      'code',
      code
    );

    body.set(
      'redirect_uri',
      this.redirectUri
    );

    body.set(
      'code_verifier',
      verifier
    );

    try {

      const response =
        await fetch(
          `${this.cognitoDomain}/oauth2/token`,
          {
            method:
              'POST',

            headers: {
              'Content-Type':
                'application/x-www-form-urlencoded'
            },

            body
          }
        );

      const data =
        await response.json();

      if (
        !response.ok
      ) {

        throw new Error(
          data.error_description
          || data.error
        );
      }

      sessionStorage.setItem(
        'access_token',
        data.access_token
      );

      if (
        data.id_token
      ) {

        sessionStorage.setItem(
          'id_token',
          data.id_token
        );
      }

      if (
        data.refresh_token
      ) {

        sessionStorage.setItem(
          'refresh_token',
          data.refresh_token
        );
      }

      this.accessToken =
        data.access_token;

      window.history.replaceState(
        {},
        document.title,
        window.location.pathname
      );

      this.cargarPedidos();

    } catch (error) {

      this.error =
        `Error Cognito: ${error}`;
    }
  }

  private decodificarToken(
    token: string
  ): any {

    try {

      const payload =
        token
          .split('.')[1]
          .replace(
            /-/g,
            '+'
          )
          .replace(
            /_/g,
            '/'
          );

      return JSON.parse(
        decodeURIComponent(
          atob(payload)
            .split('')
            .map(
              c =>
                '%' +
                c.charCodeAt(0)
                  .toString(16)
                  .padStart(
                    2,
                    '0'
                  )
            )
            .join('')
        )
      );

    } catch {

      return null;
    }
  }

  private generarVerifier():
    string {

    const bytes =
      crypto.getRandomValues(
        new Uint8Array(32)
      );

    return Array
      .from(bytes)
      .map(
        value =>
          value
            .toString(16)
            .padStart(
              2,
              '0'
            )
      )
      .join('');
  }

  private async generarChallenge(
    verifier: string
  ): Promise<string> {

    const data =
      new TextEncoder()
        .encode(
          verifier
        );

    const digest =
      await crypto.subtle.digest(
        'SHA-256',
        data
      );

    return btoa(
      String.fromCharCode(
        ...new Uint8Array(
          digest
        )
      )
    )
      .replace(
        /\+/g,
        '-'
      )
      .replace(
        /\//g,
        '_'
      )
      .replace(
        /=+$/,
        ''
      );
  }
}