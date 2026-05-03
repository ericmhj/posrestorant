import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>POS Restaurante</h1>
        <p *ngIf="sessionExpired" class="warning">Tu sesión expiró. Inicia sesión nuevamente.</p>
        <form (ngSubmit)="onLogin()" #loginForm="ngForm">
          <div class="field">
            <label for="username">Usuario</label>
            <input id="username" type="text" [(ngModel)]="username"
                   name="username" required autocomplete="username" />
          </div>
          <div class="field">
            <label for="password">Contraseña</label>
            <input id="password" type="password" [(ngModel)]="password"
                   name="password" required autocomplete="current-password" />
          </div>
          <p *ngIf="errorMessage" class="error" role="alert">{{ errorMessage }}</p>
          <button type="submit" [disabled]="loading || !loginForm.valid">
            {{ loading ? 'Iniciando...' : 'Iniciar Sesión' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container { display:flex; align-items:center; justify-content:center; min-height:100vh; background:#f5f5f5; }
    .login-card { background:#fff; padding:2rem; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,.1); width:100%; max-width:360px; }
    h1 { text-align:center; margin-bottom:1.5rem; color:#2c3e50; }
    .field { margin-bottom:1rem; }
    label { display:block; margin-bottom:.25rem; font-weight:500; }
    input { width:100%; padding:.5rem; border:1px solid #ccc; border-radius:4px; box-sizing:border-box; }
    button { width:100%; padding:.75rem; background:#2980b9; color:#fff; border:none; border-radius:4px; cursor:pointer; font-size:1rem; }
    button:disabled { opacity:.6; cursor:not-allowed; }
    .error { color:#e74c3c; font-size:.875rem; }
    .warning { color:#e67e22; font-size:.875rem; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;
  errorMessage = '';
  sessionExpired = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.sessionExpired = route.snapshot.queryParams['reason'] === 'session_expired';
  }

  onLogin(): void {
    this.loading = true;
    this.errorMessage = '';
    this.auth.login({ username: this.username, password: this.password }).subscribe({
      next: (res) => {
        const rol = res.usuario.rol;
        const redirect = rol === 'COCINA' ? '/kds'
          : rol === 'BARRA' ? '/bds'
          : rol === 'ADMIN' ? '/pos'
          : '/pos';
        this.router.navigate([redirect]);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Usuario o contraseña incorrectos';
      }
    });
  }
}
