import { TestBed } from '@angular/core/testing';
import { WebSocketService } from './websocket.service';

describe('WebSocketService', () => {
  let service: WebSocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [WebSocketService] });
    service = TestBed.inject(WebSocketService);
  });

  afterEach(() => service.disconnect());

  it('should create', () => expect(service).toBeTruthy());

  it('initial status is disconnected', (done) => {
    service.connectionStatus$.subscribe(status => {
      expect(status).toBe('disconnected');
      done();
    });
  });

  it('reconnect attempts use exponential backoff delays', () => {
    const delays = [1000, 2000, 4000, 8000, 16000];
    // Verify the backoff array is defined correctly
    expect((service as any).BACKOFF_DELAYS).toEqual(delays);
  });

  it('max attempts is 5', () => {
    expect((service as any).MAX_ATTEMPTS).toBe(5);
  });

  it('disconnect sets destroyed flag', () => {
    service.disconnect();
    expect((service as any).destroyed).toBeTrue();
  });
});
