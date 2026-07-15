import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

import { CustomerContext } from '../../../core/models/cart.models';
import { CartService } from '../../../core/services/cart.service';
import { CustomerContextService } from '../../../core/services/customer-context.service';

@Component({
  selector: 'app-floating-cart-button',
  imports: [CommonModule],
  templateUrl: './floating-cart-button.html',
  styleUrl: './floating-cart-button.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FloatingCartButton {
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);

  protected readonly currentUrl = signal(this.router.url);

  protected readonly context = computed<CustomerContext>(() =>
    this.customerContextService.getCurrentContext(this.currentUrl()),
  );

  protected readonly totalItems = computed(() => {
    this.cartService.cartVersion();

    return this.cartService.getSummary(this.context()).totalItems;
  });

  protected readonly shouldShow = computed(() =>
    this.totalItems() > 0
    && this.customerContextService.isCustomerRoute(this.currentUrl())
    && !this.customerContextService.isHiddenFloatingCartRoute(this.currentUrl()),
  );

  constructor() {
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(event => this.currentUrl.set(event.urlAfterRedirects));
  }

  protected goToCart(): void {
    this.router.navigate([this.customerContextService.getCartRoute(this.context())]);
  }

  
}
