import { Injectable } from '@angular/core';
import { signal } from '@angular/core';

import { CartItem, CartSummary, CustomerContext } from '../models/cart.models';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private readonly storagePrefix = 'fourkitchen-cart';
  private readonly cartVersionSignal = signal(0);

  readonly cartVersion = this.cartVersionSignal.asReadonly();

  getCart(context: CustomerContext): CartItem[] {
    const storedCart = localStorage.getItem(this.getStorageKey(context));

    if (!storedCart) {
      return [];
    }

    try {
      const parsedCart = JSON.parse(storedCart) as CartItem[];

      return Array.isArray(parsedCart) ? parsedCart : [];
    } catch {
      return [];
    }
  }

  getSummary(context: CustomerContext): CartSummary {
    const items = this.getCart(context);
    const subtotal = items.reduce(
      (total, item) => total + item.unitPrice * item.quantity,
      0,
    );

    return {
      items,
      subtotal,
      total: subtotal,
      totalItems: items.reduce((total, item) => total + item.quantity, 0),
    };
  }

  addItem(context: CustomerContext, item: CartItem): CartItem[] {
    const items = this.getCart(context);
    const normalizedObservation = this.normalizeObservation(item.observation);
    const existingItem = items.find(
      cartItem =>
        cartItem.productId === item.productId
        && this.normalizeObservation(cartItem.observation) === normalizedObservation,
    );

    if (existingItem) {
      existingItem.quantity += item.quantity;
    } else {
      items.push({
        ...item,
        observation: normalizedObservation || undefined,
      });
    }

    this.saveCart(context, items);

    return items;
  }

  updateQuantity(context: CustomerContext, cartItemId: string, quantity: number): CartItem[] {
    const nextQuantity = Math.max(1, quantity);
    const items = this.getCart(context).map(item =>
      item.cartItemId === cartItemId
        ? { ...item, quantity: nextQuantity }
        : item,
    );

    this.saveCart(context, items);

    return items;
  }

  removeItem(context: CustomerContext, cartItemId: string): CartItem[] {
    const items = this.getCart(context).filter(item => item.cartItemId !== cartItemId);

    this.saveCart(context, items);

    return items;
  }

  clearCart(context: CustomerContext): void {
    localStorage.removeItem(this.getStorageKey(context));
    this.bumpCartVersion();
  }

  getTotal(context: CustomerContext): number {
    return this.getSummary(context).total;
  }

  private saveCart(context: CustomerContext, items: CartItem[]): void {
    localStorage.setItem(this.getStorageKey(context), JSON.stringify(items));
    this.bumpCartVersion();
  }

  private getStorageKey(context: CustomerContext): string {
    return `${this.storagePrefix}-${context}`;
  }

  private normalizeObservation(observation?: string): string {
    return observation?.trim() ?? '';
  }

  private bumpCartVersion(): void {
    this.cartVersionSignal.update(version => version + 1);
  }
}
