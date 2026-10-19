import { Routes } from '@angular/router';

export const routes: Routes = [
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
  { path: '**', redirectTo: 'tableau-de-bord' },
];
