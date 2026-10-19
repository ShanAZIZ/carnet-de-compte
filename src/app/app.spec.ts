import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  let fixture: ComponentFixture<App>;

  beforeEach(async () => {
    fixture = await createApp();
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
    const title = fixture.nativeElement.querySelector('.menu-title');

    expect(title?.querySelector('a')).toBeNull();
  });

  it('shows the dashboard by default', async () => {
    await navigateTo('/');

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe('Tableau de bord');
  });

  it.each([
    ['/import-csv', 'Importer un CSV'],
    ['/operations', 'Opérations'],
    ['/categories', 'Catégories'],
    ['/recurrents', 'Récurrents'],
  ])('shows %s with the title %s', async (path, title) => {
    await navigateTo(path);

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toBe(title);
  });

  it('marks the current page link as active', async () => {
    await navigateTo('/operations');

    expect(fixture.nativeElement.querySelector('a.menu-active')?.textContent?.trim()).toBe(
      'Opérations',
    );
  });

  it('closes the drawer when a navigation link is clicked', async () => {
    hamburger().click();
    await fixture.whenStable();

    links()[1].click();
    await fixture.whenStable();

    expect(toggle().checked).toBe(false);
  });

  it('opens the drawer from the hamburger button', async () => {
    hamburger().click();

    await fixture.whenStable();

    expect(toggle().checked).toBe(true);
  });

  it('closes the drawer on Escape', async () => {
    hamburger().click();
    await fixture.whenStable();

    fixture.nativeElement.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    await fixture.whenStable();

    expect(toggle().checked).toBe(false);
  });

  function links(): HTMLAnchorElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('nav a'));
  }

  function toggle(): HTMLInputElement {
    return fixture.nativeElement.querySelector('.drawer-toggle');
  }

  function hamburger(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('[aria-controls="app-nav"]');
  }

  async function navigateTo(path: string): Promise<void> {
    await TestBed.inject(Router).navigateByUrl(path);
    await fixture.whenStable();
  }
});

async function createApp() {
  TestBed.configureTestingModule({
    imports: [App],
    providers: [provideRouter(routes)],
  });

  const fixture = TestBed.createComponent(App);
  await fixture.whenStable();

  return fixture;
}
