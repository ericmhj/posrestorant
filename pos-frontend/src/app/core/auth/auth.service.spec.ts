import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('should create', () => expect(service).toBeTruthy());

  it('login stores token and emits user', () => {
    const mockResponse = {
      token: 'test-token',
      usuario: { id: '1', nombre: 'Admin', rol: 'ADMIN' },
      expiresAt: new Date().toISOString()
    };

    service.login({ username: 'admin', password: 'admin123' }).subscribe(res => {
      expect(res.token).toBe('test-token');
      expect(service.getToken()).toBe('test-token');
      expect(service.isAuthenticated()).toBeTrue();
      expect(service.currentUser$.value?.rol).toBe('ADMIN');
    });

    http.expectOne(`${environment.apiUrl}/api/v1/auth/login`)
      .flush(mockResponse);
  });

  it('logout clears session', () => {
    sessionStorage.setItem('pos_token', 'test-token');
    sessionStorage.setItem('pos_user', JSON.stringify({ id: '1', nombre: 'Admin', rol: 'ADMIN' }));

    service.logout().subscribe(() => {
      expect(service.getToken()).toBeNull();
      expect(service.isAuthenticated()).toBeFalse();
      expect(service.currentUser$.value).toBeNull();
    });

    http.expectOne(`${environment.apiUrl}/api/v1/auth/logout`).flush({});
  });

  it('isAuthenticated returns false when no token', () => {
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('hasRole returns true for matching role', () => {
    (service as any).currentUser$.next({ id: '1', nombre: 'Admin', rol: 'ADMIN' });
    expect(service.hasRole('ADMIN')).toBeTrue();
    expect(service.hasRole('MESERO')).toBeFalse();
  });

  it('clearSession removes token and user', () => {
    sessionStorage.setItem('pos_token', 'abc');
    service.clearSession();
    expect(service.getToken()).toBeNull();
    expect(service.currentUser$.value).toBeNull();
  });
});
