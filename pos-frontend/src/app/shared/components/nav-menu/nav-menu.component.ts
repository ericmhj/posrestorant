import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-nav-menu',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="nav-bar" *ngIf="auth.isAuthenticated()">
      <div class="nav-brand">POS Restaurante</div>
      <div class="nav-links">
        <a routerLink="/pos" routerLinkActive="active" *ngIf="isAdmin() || isMesero()">Mesas</a>
        <a routerLink="/kds" routerLinkActive="active" *ngIf="isAdmin() || isCocina()">Cocina</a>
        <a routerLink="/bds" routerLinkActive="active" *ngIf="isAdmin() || isBarra()">Barra</a>
        <ng-container *ngIf="isAdmin()">
          <a routerLink="/admin/usuarios" routerLinkActive="active">Usuarios</a>
          <a routerLink="/admin/productos" routerLinkActive="active">Productos</a>
          <a routerLink="/admin/inventario" routerLinkActive="active">Inventario</a>
          <a routerLink="/admin/mesas" routerLinkActive="active">Mesas Admin</a>
          <a routerLink="/admin/reportes" routerLinkActive="active">Reportes</a>
        </ng-container>
      </div>
      <button class="btn-logout" (click)="logout()">Salir</button>
    </nav>
  `,
  styles: [`
    .nav-bar { display:flex; align-items:center; background:#2c3e50; color:#fff; padding:.5rem 1rem; gap:1rem; flex-wrap:wrap; }
    .nav-brand { font-weight:700; font-size:1.1rem; margin-right:1rem; }
    .nav-links { display:flex; gap:.5rem; flex-wrap:wrap; flex:1; }
    .nav-links a { color:#ecf0f1; text-decoration:none; padding:.4rem .75rem; border-radius:4px; font-size:.9rem; }
    .nav-links a:hover, .nav-links a.active { background:#2980b9; }
    .btn-logout { margin-left:auto; padding:.4rem .75rem; background:#e74c3c; color:#fff; border:none; border-radius:4px; cursor:pointer; font-size:.9rem; }
  `]
})
export class NavMenuComponent {
  constructor(public auth: AuthService, private router: Router) {}

  isAdmin(): boolean { return this.auth.hasRole('ADMIN'); }
  isMesero(): boolean { return this.auth.hasRole('MESERO'); }
  isCocina(): boolean { return this.auth.hasRole('COCINA'); }
  isBarra(): boolean { return this.auth.hasRole('BARRA'); }

  logout(): void {
    this.auth.logout().subscribe();
    this.router.navigate(['/login']);
  }
}
