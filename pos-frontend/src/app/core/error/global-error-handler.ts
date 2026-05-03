import { ErrorHandler, Injectable } from '@angular/core';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: Error): void {
    console.error('[POS Error]', {
      message: error.message,
      stack: error.stack,
      timestamp: new Date().toISOString()
    });
    // Do not expose technical details to the user
    // Notification is handled by individual components
  }
}
