import { Directive, ElementRef, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { BackendHealthService } from '../../core/http/backend-health.service';

/**
 * Disables the host element when the backend is unavailable.
 * Usage: <button appDisabledWhenOffline>Save</button>
 */
@Directive({
  selector: '[appDisabledWhenOffline]',
  standalone: true
})
export class DisabledWhenOfflineDirective implements OnInit, OnDestroy {

  private sub?: Subscription;

  constructor(
    private el: ElementRef<HTMLElement>,
    private healthService: BackendHealthService
  ) {}

  ngOnInit(): void {
    this.sub = this.healthService.backendUnavailable$.subscribe(unavailable => {
      const el = this.el.nativeElement;
      if (unavailable) {
        el.setAttribute('disabled', 'true');
        el.setAttribute('aria-disabled', 'true');
        el.style.opacity = '0.5';
        el.style.cursor = 'not-allowed';
      } else {
        el.removeAttribute('disabled');
        el.removeAttribute('aria-disabled');
        el.style.opacity = '';
        el.style.cursor = '';
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
