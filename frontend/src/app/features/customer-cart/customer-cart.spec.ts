import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';

import { CartItem } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerOrderCacheService } from '../../core/services/customer-order-cache.service';
import { OrderService } from '../../core/services/order.service';
import { CustomerCart } from './customer-cart';

describe('CustomerCart', () => {
  let fixture: ComponentFixture<CustomerCart>;
  let cartService: {
    getCart: ReturnType<typeof vi.fn>;
    updateObservation: ReturnType<typeof vi.fn>;
  };

  const item: CartItem = {
    cartItemId: 'item-10',
    productId: 10,
    name: 'Coca-Cola Zero',
    description: 'Lata 350 ml',
    unitPrice: 8.9,
    quantity: 1,
  };

  beforeEach(async () => {
    cartService = {
      getCart: vi.fn().mockReturnValue([item]),
      updateObservation: vi.fn().mockImplementation(
        (_context: string, _cartItemId: string, observation: string) => [
          { ...item, observation },
        ],
      ),
    };

    await TestBed.configureTestingModule({
      imports: [CustomerCart],
      providers: [
        { provide: CartService, useValue: cartService },
        { provide: OrderService, useValue: {} },
        { provide: CustomerOrderCacheService, useValue: {} },
        {
          provide: Router,
          useValue: {
            url: '/totem/carrinho',
            navigate: vi.fn().mockResolvedValue(true),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomerCart);
    fixture.detectChanges();
  });

  it('deve exibir e persistir a observacao no carrinho do Totem', () => {
    const input = fixture.nativeElement.querySelector(
      '.cart-item__observation input',
    ) as HTMLInputElement;

    expect(input).toBeTruthy();

    input.value = 'Sem gelo';
    input.dispatchEvent(new Event('input'));

    expect(cartService.updateObservation).toHaveBeenCalledWith(
      'totem',
      'item-10',
      'Sem gelo',
    );
  });
});
