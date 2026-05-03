import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthLoginRequest, AuthLoginResponse, UsuarioInfo } from './auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'pos_token';
  private readonly USER_KEY = 'pos_user';

  currentUser$ = new BehaviorSubject<UsuarioInfo | null>(this.loadUser());

  constructor(private http: HttpClient) {}

  login(request: AuthLoginRequest): Observable<AuthLoginResponse> {
    return this.http.post<AuthLoginResponse>(
      `${environment.apiUrl}/api/v1/auth/login`, request
    ).pipe(
      tap(response => {
        sessionStorage.setItem(this.TOKEN_KEY, response.token);
        sessionStorage.setItem(this.USER_KEY, JSON.stringify(response.usuario));
        this.currentUser$.next(response.usuario);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/v1/auth/logout`, {}).pipe(
      tap(() => this.clearSession())
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/api/v1/auth/password`, {
      currentPassword, newPassword
    });
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(rol: string): boolean {
    const user = this.currentUser$.value;
    return user?.rol === rol;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUser$.value;
    return !!user && roles.includes(user.rol);
  }

  clearSession(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    sessionStorage.removeItem(this.USER_KEY);
    this.currentUser$.next(null);
  }

  private loadUser(): UsuarioInfo | null {
    const raw = sessionStorage.getItem(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
