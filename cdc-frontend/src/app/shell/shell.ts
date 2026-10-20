import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Auth } from '../core/auth';

const NAV_SECTIONS = [
  {
    title: null,
    links: [
      { path: '/tableau-de-bord', label: 'Tableau de bord' },
      { path: '/import-csv', label: 'Importer un CSV' },
      { path: '/operations', label: 'Opérations' },
    ],
  },
  {
    title: 'Référentiels',
    links: [
      { path: '/categories', label: 'Catégories' },
      { path: '/recurrents', label: 'Récurrents' },
    ],
  },
];

@Component({
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  selector: 'app-shell',
  templateUrl: './shell.html',
  host: { '(keydown.escape)': 'toggleDrawer(false)' },
})
export class Shell {
  protected readonly auth = inject(Auth);
  readonly #router = inject(Router);
  protected readonly sections = NAV_SECTIONS;
  protected readonly isDrawerOpen = signal(false);

  protected toggleDrawer(open: boolean): void {
    this.isDrawerOpen.set(open);
  }

  protected async signOut(): Promise<void> {
    await this.auth.signOut();
    await this.#router.navigateByUrl('/connexion');
  }
}
