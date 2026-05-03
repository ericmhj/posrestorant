import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, interval, Subscription } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { of } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BackendHealthService implements OnDestroy {

  private readonly POLL_INTERVAL_MS = 30000;
  private readonly TIMEOUT_MS = 10000;

  backendUnavailable$ = new BehaviorSubject<boolean>(false);

  private pollSub?: Subscription;

  constructor(private http: HttpClient) {
    this.startPolling();
  }

  private startPolling(): void {
    this.pollSub = interval(this.POLL_INTERVAL_MS).subscribe(() => {
      this.checkHealth();
    });
  }

  private checkHealth(): void {
    this.http.get(`${environment.apiUrl}/q/health/live`).pipe(
      timeout(this.TIMEOUT_MS),
      catchError(() => {
        this.backendUnavailable$.next(true);
        return of(null);
      })
    ).subscribe(result => {
      if (result !== null) {
        this.backendUnavailable$.next(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }
}
