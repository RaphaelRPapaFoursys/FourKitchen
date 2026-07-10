import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CustomerContext } from '../models/cart.models';

@Injectable({
  providedIn: 'root',
})
export class CustomerContextService {
  private readonly router = inject(Router);

  getCurrentContext(url = this.router.url): CustomerContext {
    return url.startsWith('/totem') ? 'totem' : 'mesa';
  }

  isCustomerRoute(url = this.router.url): boolean {
    return url.startsWith('/mesa') || url.startsWith('/totem');
  }

  getHomeRoute(context: CustomerContext): string {
    return `/${context}`;
  }

  getCartRoute(context: CustomerContext): string {
    return `/${context}/carrinho`;
  }

  getSuccessRoute(context: CustomerContext): string {
    return `/${context}/pedido-criado`;
  }

  getErrorRoute(context: CustomerContext): string {
    return `/${context}/pedido-erro`;
  }

  getOrdersRoute(context: CustomerContext): string {
    return context === 'mesa' ? '/mesa/pedidos' : '/totem';
  }

  isHiddenFloatingCartRoute(url = this.router.url): boolean {
    const normalizedUrl = url.split('?')[0];

    return [
      '/mesa/carrinho',
      '/totem/carrinho',
      '/mesa/pedido-criado',
      '/totem/pedido-criado',
      '/mesa/pedido-erro',
      '/totem/pedido-erro',
      '/mesa/pedidos',
    ].includes(normalizedUrl);
  }
}
