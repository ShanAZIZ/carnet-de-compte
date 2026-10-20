import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { Auth } from './auth';
import { authGuard } from './auth-guard';

describe('authGuard', () => {
  it('lets a signed-in user through', async () => {
    const result = await runGuard(true);

    expect(result).toBe(true);
  });

  it('redirects a signed-out visitor to the login page', async () => {
    const result = await runGuard(false);

    expect(TestBed.inject(Router).serializeUrl(result as UrlTree)).toBe('/connexion');
  });
});

function runGuard(isSignedIn: boolean) {
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      { provide: Auth, useValue: { isSignedIn: () => Promise.resolve(isSignedIn) } },
    ],
  });
  return TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
}
