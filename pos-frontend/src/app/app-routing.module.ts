import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard, RoleGuard } from './core/auth/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'pos',
    loadComponent: () => import('./features/pos/pos.component')
      .then(m => m.PosComponent),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['MESERO', 'ADMIN'] }
  },
  {
    path: 'kds',
    loadComponent: () => import('./features/kds/kds.component')
      .then(m => m.KdsComponent),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['COCINA', 'ADMIN'] }
  },
  {
    path: 'bds',
    loadComponent: () => import('./features/bds/bds.component')
      .then(m => m.BdsComponent),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['BARRA', 'ADMIN'] }
  },
  {
    path: 'admin',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: 'productos',
        loadComponent: () => import('./features/admin/productos/admin-productos.component')
          .then(m => m.AdminProductosComponent)
      },
      {
        path: 'inventario',
        loadComponent: () => import('./features/admin/inventario/admin-inventario.component')
          .then(m => m.AdminInventarioComponent)
      },
      {
        path: 'usuarios',
        loadComponent: () => import('./features/admin/usuarios/admin-usuarios.component')
          .then(m => m.AdminUsuariosComponent)
      },
      {
        path: 'reportes',
        loadComponent: () => import('./features/admin/reportes/admin-reportes.component')
          .then(m => m.AdminReportesComponent)
      },
      {
        path: 'mesas',
        loadComponent: () => import('./features/admin/mesas/admin-mesas.component')
          .then(m => m.AdminMesasComponent)
      }
    ]
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./features/login/login.component')
      .then(m => m.LoginComponent)
  },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
