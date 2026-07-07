import { Route, Routes } from '@angular/router';

import { authChildGuard, authGuard } from '../guards/auth.guard';

export type AppProfile = 'ADMIN' | 'GESTOR' | 'COZINHA' | 'GARCOM' | 'MESA' | 'TOTEM';

function protectedRoute(route: Route, allowedProfiles?: AppProfile[]): Route {
  return {
    ...route,
    canActivate: [authGuard, ...(route.canActivate ?? [])],
    canActivateChild: route.children
      ? [authChildGuard, ...(route.canActivateChild ?? [])]
      : route.canActivateChild,
    data: {
      ...route.data,
      ...(allowedProfiles ? { allowedProfiles } : {}),
    },
  };
}

export const protectedRoutes: Routes = [
  protectedRoute({
    path: 'home',
    loadComponent: () =>
      import('../../features/home/home').then(m => m.Home),
  }),
  protectedRoute(
    {
      path: 'totem',
      loadComponent: () =>
        import('../../features/customer-home/customer-home').then(m => m.CustomerHome),
    },
    ['ADMIN', 'TOTEM'],
  ),
  protectedRoute(
    {
      path: 'mesa',
      loadComponent: () =>
        import('../../features/customer-home/customer-home').then(m => m.CustomerHome),
    },
    ['ADMIN', 'MESA'],
  ),
  protectedRoute(
    {
      path: 'garcom',
      loadComponent: () =>
        import('../../features/garcom/garcom').then(m => m.Garcom),
    },
    ['ADMIN', 'GARCOM'],
  ),
  protectedRoute(
    {
      path: 'cozinha',
      loadComponent: () =>
        import('../../features/cozinha/cozinha').then(m => m.Cozinha),
    },
    ['ADMIN', 'COZINHA'],
  ),
  protectedRoute(
    {
      path: 'gestor',
      loadComponent: () =>
        import('../../features/gestor/gestor').then(m => m.Gestor),
    },
    ['ADMIN', 'GESTOR'],
  ),
];
