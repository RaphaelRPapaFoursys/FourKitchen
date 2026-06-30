import { Routes } from '@angular/router';

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
  {
    path: 'home',
    loadComponent: () =>
      import('./features/home/home').then(m => m.Home)
  },
  {
    path: 'totem',
    loadComponent: () =>
      import('./features/totem/totem').then(m => m.Totem)
  },
  {
    path: 'mesa',
    loadComponent: () =>
      import('./features/mesa/mesa').then(m => m.Mesa)
  },
  {
    path: 'garcom',
    loadComponent: () =>
      import('./features/garcom/garcom').then(m => m.Garcom)
  },
  {
    path: 'cozinha',
    loadComponent: () =>
      import('./features/cozinha/cozinha').then(m => m.Cozinha)
  },
  {
    path: 'gestor',
    loadComponent: () =>
      import('./features/gestor/gestor').then(m => m.Gestor)
  }
];
