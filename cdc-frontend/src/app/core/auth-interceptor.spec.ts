import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { Auth } from './auth';
import { authInterceptor } from './auth-interceptor';

describe('authInterceptor', () => {
  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('adds the Firebase ID token to calls to the API', async () => {
    const authorization = await headerSentTo('/operations', 'id-token');

    expect(authorization).toBe('Bearer id-token');
  });

  it('sends no token to another origin', async () => {
    const authorization = await headerSentTo('https://example.com/data', 'id-token');

    expect(authorization).toBeNull();
  });

  it('sends no header when signed out', async () => {
    const authorization = await headerSentTo('/operations', null);

    expect(authorization).toBeNull();
  });
});

async function headerSentTo(url: string, token: string | null): Promise<string | null> {
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([authInterceptor])),
      provideHttpClientTesting(),
      { provide: Auth, useValue: { idToken: () => Promise.resolve(token) } },
    ],
  });
  const response = firstValueFrom(TestBed.inject(HttpClient).get(url));
  await Promise.resolve();

  const request = TestBed.inject(HttpTestingController).expectOne(url);
  request.flush({});
  await response;

  return request.request.headers.get('Authorization');
}
