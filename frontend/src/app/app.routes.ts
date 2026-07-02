import { Routes } from '@angular/router';

import { protectedRoutes } from './core/routing/protected-routes';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login').then(m => m.Login)
  },
  ...protectedRoutes,
];
