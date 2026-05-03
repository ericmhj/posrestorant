import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ConnectionStatusBannerComponent } from './shared/components/connection-status-banner/connection-status-banner.component';

@Component({
  selector: 'app-root',
  standalone: false,
  template: `
    <app-connection-status-banner></app-connection-status-banner>
    <router-outlet></router-outlet>
  `
})
export class AppComponent {
  title = 'POS Restaurant';
}
