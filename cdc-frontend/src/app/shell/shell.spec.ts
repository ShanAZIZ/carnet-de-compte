import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../app.routes';
import { Auth } from '../core/auth';

describe('Shell', () => {
  let harness: RouterTestingHarness;

  beforeEach(async () => {
    harness = await createHarness();
  });

  it('renders every navigation link', () => {
    const labels = links().map((link) => link.textContent?.trim());

    expect(labels).toEqual([
      'Tableau de bord',
      'Importer un CSV',
      'Opérations',
      'Catégories',
      'Récurrents',
    ]);
  });

  it('renders "Référentiels" as a non-clickable section title', () => {
    const title = root().querySelector('.menu-title');

    expect(title?.querySelector('a')).toBeNull();
  });

  it('shows the dashboard by default', () => {
    expect(heading()).toBe('Tableau de bord');
  });

  it.each([
    ['/import-csv', 'Importer un CSV'],
    ['/operations', 'Opérations'],
    ['/categories', 'Catégories'],
    ['/recurrents', 'Récurrents'],
  ])('shows %s with the title %s', async (path, title) => {
    await harness.navigateByUrl(path);

    expect(heading()).toBe(title);
  });

  it('marks the current page link as active', async () => {
    await harness.navigateByUrl('/operations');

    expect(root().querySelector('a.menu-active')?.textContent?.trim()).toBe('Opérations');
  });

  it('closes the drawer when a navigation link is clicked', async () => {
    hamburger().click();
    await harness.fixture.whenStable();

    links()[1].click();
    await harness.fixture.whenStable();

    expect(toggle().checked).toBe(false);
  });

  it('opens the drawer from the hamburger button', async () => {
    hamburger().click();

    await harness.fixture.whenStable();

    expect(toggle().checked).toBe(true);
  });

  it('closes the drawer on Escape', async () => {
    hamburger().click();
    await harness.fixture.whenStable();

    root()
      .querySelector('app-shell')
      ?.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await harness.fixture.whenStable();

    expect(toggle().checked).toBe(false);
  });

  it('is not rendered on the login page', async () => {
    await harness.navigateByUrl('/connexion');

    expect(root().querySelector('nav')).toBeNull();
  });

  function root(): HTMLElement {
    return harness.fixture.nativeElement;
  }

  function links(): HTMLAnchorElement[] {
    return Array.from(root().querySelectorAll('nav a'));
  }

  function heading(): string | null | undefined {
    return root().querySelector('main h1')?.textContent;
  }

  function toggle(): HTMLInputElement {
    return root().querySelector('.drawer-toggle') as HTMLInputElement;
  }

  function hamburger(): HTMLButtonElement {
    return root().querySelector('[aria-controls="app-nav"]') as HTMLButtonElement;
  }
});

async function createHarness() {
  TestBed.configureTestingModule({
    providers: [
      provideRouter(routes),
      {
        provide: Auth,
        useValue: {
          user: signal({ email: 'alice@example.com' }),
          isSignedIn: () => Promise.resolve(true),
        },
      },
    ],
  });

  return RouterTestingHarness.create('/');
}
