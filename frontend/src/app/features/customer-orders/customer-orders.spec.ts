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
    ).flush([]);
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
});
