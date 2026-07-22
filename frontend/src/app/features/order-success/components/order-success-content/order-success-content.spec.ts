import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderSuccessContentComponent } from './order-success-content';

describe('OrderSuccessContentComponent', () => {
  let fixture: ComponentFixture<OrderSuccessContentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderSuccessContentComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(OrderSuccessContentComponent);
  });

  it('deve destacar o codigo publico retornado para um pedido do totem', () => {
    fixture.componentRef.setInput('isTotem', true);
    fixture.componentRef.setInput('orderCode', 100025);
    fixture.detectChanges();

    const numberCard = fixture.nativeElement.querySelector('.order-success__number') as HTMLElement;

    expect(numberCard.textContent).toContain('100025');
    expect(numberCard.textContent).toContain('Acompanhe seu pedido no painel.');
  });

  it('nao deve exibir numero quando nao houver codigo confirmado', () => {
    fixture.componentRef.setInput('isTotem', true);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.order-success__number')).toBeNull();
  });
});
