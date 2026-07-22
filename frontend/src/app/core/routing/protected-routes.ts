import { Route, Routes } from '@angular/router';

import { authChildGuard, authGuard } from '../guards/auth.guard';

export type AppProfile = 'ADMIN' | 'GESTOR' | 'COZINHA' | 'GARCOM' | 'MESA' | 'TOTEM' | 'BALCAO';

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
  protectedRoute(
    {
      path: 'balcao',
      loadComponent: () =>
        import('../../features/balcao/balcao').then(m => m.Balcao),
    },
    ['ADMIN', 'BALCAO'],
  ),
  protectedRoute({
    path: 'home',
    loadComponent: () =>
      import('../../features/home/home').then(m => m.Home),
  }),
  {
    path: 'totem/pedidos',
    pathMatch: 'full',
    redirectTo: 'totem',
  },
  protectedRoute(
    {
      path: 'totem/carrinho',
      loadComponent: () =>
        import('../../features/customer-cart/customer-cart').then(m => m.CustomerCart),
    },
    ['ADMIN', 'TOTEM'],
  ),
  protectedRoute(
    {
      path: 'totem/pagamento',
      loadComponent: () =>
        import('../../features/totem-payment/totem-payment').then(m => m.TotemPayment),
    },
    ['ADMIN', 'TOTEM'],
  ),
  protectedRoute(
    {
      path: 'totem/pedido-criado',
      loadComponent: () =>
        import('../../features/order-success/order-success').then(m => m.OrderSuccess),
    },
    ['ADMIN', 'TOTEM'],
  ),
  protectedRoute(
    {
      path: 'totem/pedido-erro',
      loadComponent: () =>
        import('../../features/order-error/order-error').then(m => m.OrderError),
    },
    ['ADMIN', 'TOTEM'],
  ),
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
      path: 'mesa/pedidos',
      loadComponent: () =>
        import('../../features/customer-orders/customer-orders').then(m => m.CustomerOrders),
    },
    ['ADMIN', 'MESA'],
  ),
  protectedRoute(
    {
      path: 'mesa/carrinho',
      loadComponent: () =>
        import('../../features/customer-cart/customer-cart').then(m => m.CustomerCart),
    },
    ['ADMIN', 'MESA'],
  ),
  protectedRoute(
    {
      path: 'mesa/pedido-criado',
      loadComponent: () =>
        import('../../features/order-success/order-success').then(m => m.OrderSuccess),
    },
    ['ADMIN', 'MESA'],
  ),
  protectedRoute(
    {
      path: 'mesa/pedido-erro',
      loadComponent: () =>
        import('../../features/order-error/order-error').then(m => m.OrderError),
    },
    ['ADMIN', 'MESA'],
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
      path: 'garcom/mesas/:id/pedido',
      loadComponent: () =>
        import('../../features/garcom-pedido/garcom-pedido').then(m => m.GarcomPedido),
    },
    ['ADMIN', 'GARCOM'],
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
      path: 'gestor/totens',
      loadComponent: () =>
        import('../../features/gestor-totens/gestor-totens').then(m => m.GestorTotens),
    },
    ['ADMIN', 'GESTOR'],
  ),

  protectedRoute(
    {
      path: 'gestor/produtos',
      loadComponent: () =>
        import('../../features/gestor-products/gestor-products').then(m => m.GestorProducts),
    },
    ['ADMIN', 'GESTOR'],
  ),
  
  protectedRoute(
    {
      path: 'gestor/categorias',
      loadComponent: () =>
        import('../../features/gestor-categories/gestor-categories').then(m => m.GestorCategories),
    },
    ['ADMIN', 'GESTOR'],
  ),
  
  protectedRoute(
    {
      path: 'gestor/usuarios',
      loadComponent: () =>
        import('../../features/gestor-users/gestor-users').then(m => m.GestorUsers),
    },
    ['ADMIN', 'GESTOR'],
  ),
  
  protectedRoute(
    {
      path: 'gestor/mesas',
      loadComponent: () =>
        import('../../features/gestor/gestor').then(m => m.Gestor),
    },
    ['ADMIN', 'GESTOR'],
  ),

  protectedRoute(
    {
      path: 'gestor',
      loadComponent: () =>
        import('../../features/gestor-dashboard/gestor-dashboard').then(m => m.GestorDashboard),
    },
    ['ADMIN', 'GESTOR'],
  ),
];
