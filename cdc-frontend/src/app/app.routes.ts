import { Routes } from '@angular/router';
import { authGuard } from './core/auth-guard';

export const routes: Routes = [
  {
    path: 'connexion',
    title: 'Connexion',
    loadComponent: () => import('./login/login').then((m) => m.Login),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'tableau-de-bord' },
      {
        path: 'tableau-de-bord',
        title: 'Tableau de bord',
        loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'import-csv',
        title: 'Importer un CSV',
        loadComponent: () => import('./csv-import/csv-import').then((m) => m.CsvImport),
      },
      {
        path: 'operations',
        title: 'Opérations',
        loadComponent: () => import('./operations/operations').then((m) => m.Operations),
      },
      {
        path: 'categories',
        title: 'Catégories',
        loadComponent: () => import('./categories/categories').then((m) => m.Categories),
      },
      {
        path: 'recurrents',
        title: 'Récurrents',
        loadComponent: () => import('./recurring/recurring').then((m) => m.Recurring),
      },
    ],
  },
  { path: '**', redirectTo: 'tableau-de-bord' },
];
