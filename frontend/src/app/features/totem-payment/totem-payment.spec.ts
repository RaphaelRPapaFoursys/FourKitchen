import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';

import { CartItem } from '../../core/models/cart.models';
import { PagamentoResponse } from '../../core/models/payment.models';
import { PedidoTotemResponse } from '../../core/models/order.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerOrderCacheService } from '../../core/services/customer-order-cache.service';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';
import { TotemPayment } from './totem-payment';

describe('TotemPayment', () => {
  let component: TotemPayment;
  let fixture: ComponentFixture<TotemPayment>;
  let cartService: {
    getCart: ReturnType<typeof vi.fn>;
    clearCart: ReturnType<typeof vi.fn>;
  };
  let orderService: {
    createTotemOrder: ReturnType<typeof vi.fn>;
  };
  let paymentService: {
    processPayment: ReturnType<typeof vi.fn>;
  };
  let orderCacheService: {
    addOrder: ReturnType<typeof vi.fn>;
  };
  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  const item: CartItem = {
    cartItemId: 'item-10',
    productId: 10,
    name: 'X-Burger',
    description: 'Lanche',
    unitPrice: 29.9,
    quantity: 1,
  };
  const payment: PagamentoResponse = {
    status: 'APROVADO',
    mensagem: 'Pagamento aprovado',
    codigoAutorizacao: 'AUTH-123',
  };
  const order: PedidoTotemResponse = {
    id: 25,
    codigo: 100025,
    canal: 'TOTEM',
    status: 'ENVIADO_COZINHA',
  };

  beforeEach(async () => {
    cartService = {
      getCart: vi.fn().mockReturnValue([item]),
      clearCart: vi.fn(),
    };
    orderService = { createTotemOrder: vi.fn() };
    paymentService = { processPayment: vi.fn() };
    orderCacheService = { addOrder: vi.fn() };
    router = { navigate: vi.fn().mockResolvedValue(true) };

    await TestBed.configureTestingModule({
      imports: [TotemPayment],
      providers: [
        { provide: CartService, useValue: cartService },
        { provide: OrderService, useValue: orderService },
        { provide: PaymentService, useValue: paymentService },
        { provide: CustomerOrderCacheService, useValue: orderCacheService },
        { provide: Router, useValue: router },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TotemPayment);
    component = fixture.componentInstance;
  });

  it('deve navegar com o codigo publico e limpar o carrinho somente apos criar o pedido', () => {
    paymentService.processPayment.mockReturnValue(of(payment));
    orderService.createTotemOrder.mockReturnValue(of(order));

    processPayment();

    expect(orderCacheService.addOrder).toHaveBeenCalledWith('totem', order);
    expect(cartService.clearCart).toHaveBeenCalledWith('totem');
    expect(router.navigate).toHaveBeenCalledWith(
      ['/totem/pedido-criado'],
      { state: { order } },
    );
  });

  it('deve manter o carrinho quando a criacao do pedido falhar', () => {
    paymentService.processPayment.mockReturnValue(of(payment));
    orderService.createTotemOrder.mockReturnValue(
      throwError(() => new Error('ms-pedidos indisponivel')),
    );

    processPayment();

    expect(cartService.clearCart).not.toHaveBeenCalled();
    expect(orderCacheService.addOrder).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('deve rejeitar resposta sem codigo publico sem apagar o carrinho', () => {
    paymentService.processPayment.mockReturnValue(of(payment));
    orderService.createTotemOrder.mockReturnValue(of({ ...order, codigo: 0 }));

    processPayment();

    expect(cartService.clearCart).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('deve ignorar cliques duplicados enquanto o pagamento estiver em andamento', () => {
    const paymentRequest = new Subject<PagamentoResponse>();
    paymentService.processPayment.mockReturnValue(paymentRequest.asObservable());
    orderService.createTotemOrder.mockReturnValue(of(order));

    processPayment();
    processPayment();

    expect(paymentService.processPayment).toHaveBeenCalledTimes(1);

    paymentRequest.next(payment);
    paymentRequest.complete();
  });

  function processPayment(): void {
    (component as unknown as { processPayment(): void }).processPayment();
  }
});
