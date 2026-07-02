import { Routes } from '@angular/router';

import { defaultRedirectGuard } from './core/guards/auth.guard';
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
  {
    path: '**',
    canActivate: [defaultRedirectGuard],
    loadComponent: () =>
      import('./features/login/login').then(m => m.Login),
  },
];

