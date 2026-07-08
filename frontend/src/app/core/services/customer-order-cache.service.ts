import { Injectable } from '@angular/core';

import { CustomerContext } from '../models/cart.models';
import { PedidoMesaStatusResponse, PedidoResponse } from '../models/order.models';

@Injectable({
  providedIn: 'root',
})
export class CustomerOrderCacheService {
  private readonly storagePrefix = 'fourkitchen-orders';

  getMesaOrders(): PedidoMesaStatusResponse[] {
    return this.getCachedOrders('mesa')
      .filter(order => order.canal === 'MESA')
      .map(order => order as PedidoMesaStatusResponse);
  }

  addOrder(context: CustomerContext, order: PedidoResponse): void {
    if (!order.id || !order.codigo || !order.status || !order.canal) {
      return;
    }

    const orders = this.getCachedOrders(context);
    const existingOrderIndex = orders.findIndex(cachedOrder => cachedOrder.id === order.id);

    if (existingOrderIndex >= 0) {
      orders[existingOrderIndex] = order;
    } else {
      orders.unshift(order);
    }

    localStorage.setItem(this.getStorageKey(context), JSON.stringify(orders));
  }

  private getCachedOrders(context: CustomerContext): PedidoResponse[] {
    const storedOrders = localStorage.getItem(this.getStorageKey(context));

    if (!storedOrders) {
      return [];
    }

    try {
      const parsedOrders = JSON.parse(storedOrders) as PedidoResponse[];

      return Array.isArray(parsedOrders) ? parsedOrders : [];
    } catch {
      return [];
    }
  }

  private getStorageKey(context: CustomerContext): string {
    return `${this.storagePrefix}-${context}`;
  }
}
