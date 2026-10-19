import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

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
  selector: 'app-root',
  templateUrl: './app.html',
  host: { '(keydown.escape)': 'toggleDrawer(false)' },
})
export class App {
  protected readonly sections = NAV_SECTIONS;
  protected readonly isDrawerOpen = signal(false);

  protected toggleDrawer(open: boolean): void {
    this.isDrawerOpen.set(open);
  }
}
