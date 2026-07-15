import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { CustomerOrders } from './customer-orders';

describe('CustomerOrders', () => {
  let fixture: ComponentFixture<CustomerOrders>;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [CustomerOrders],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture = TestBed.createComponent(CustomerOrders);
    fixture.detectChanges();

    httpMock.expectOne(`${environment.apiUrl}/api/mesa/atendimento-atual`).flush({
      idMesa: 10,
      idAtendimento: 20,
      codigoAtendimento: 827764,
      status: 'OCUPADA',
    });
    httpMock.expectOne(request =>
      request.url === `${environment.apiUrl}/api/mesa/pedidos`
      && request.params.get('codigoAtendimento') === '827764',
    ).flush([{
      id: 25,
      codigo: 843011,
      canal: 'MESA',
      status: 'EM_PREPARO',
      idMesa: 10,
      idAtendimento: 20,
      codigoAtendimento: 827764,
      dataCriacao: '2026-07-15T09:26:00',
      valorTotal: 59.8,
      itens: [{
        idProduto: 10,
        nome: 'X-Burger',
        quantidade: 2,
        precoUnitario: 29.9,
        valorTotal: 59.8,
        observacao: 'Sem cebola',
      }],
    }]);
    httpMock.expectOne(request =>
      request.url === `${environment.apiUrl}/api/mesa/pedidos/resumo-conta`
      && request.params.get('codigoAtendimento') === '827764',
    ).flush({
      idAtendimento: 20,
      codigoAtendimento: 827764,
      valorFinal: 149.7,
      totalPedidos: 3,
      totalItens: 7,
    });
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('shows the action to create another order', () => {
    const button = fixture.nativeElement.querySelector('.orders-panel__new-order') as HTMLButtonElement;

    expect(button).toBeTruthy();
    expect(button.textContent).toContain('Fazer novo pedido');
  });

  it('returns to the table menu when the new order action is selected', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const button = fixture.nativeElement.querySelector('.orders-panel__new-order') as HTMLButtonElement;

    button.click();

    expect(navigateSpy).toHaveBeenCalledWith(['/mesa']);
  });

  it('shows item, order and account totals', () => {
    const item = fixture.nativeElement.querySelector('.order-card__item-line') as HTMLElement;
    const orderTotal = fixture.nativeElement.querySelector('.order-card__total') as HTMLElement;
    const accountTotal = fixture.nativeElement.querySelector('.orders-summary') as HTMLElement;

    expect(item.textContent).toContain('2x X-Burger');
    expect(item.textContent).toContain('R$ 59,80');
    expect(orderTotal.textContent).toContain('Total do pedido');
    expect(orderTotal.textContent).toContain('R$ 59,80');
    expect(accountTotal.textContent).toContain('Total da conta');
    expect(accountTotal.textContent).toContain('R$ 149,70');
  });

  it('shows the complete preparation status', () => {
    const status = fixture.nativeElement.querySelector('.orders-status') as HTMLElement;

    expect(status.textContent?.trim()).toBe('Em preparo');
  });
});
