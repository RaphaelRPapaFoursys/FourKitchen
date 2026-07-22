import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { GestorTotens } from './gestor-totens';

describe('GestorTotens', () => {
  let fixture: ComponentFixture<GestorTotens>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [GestorTotens],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GestorTotens);
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/api/gestor/totens`).flush([{
      id: 9,
      nome: 'Totem 01',
      email: 'totem01@fourkitchen.com',
      ativo: true,
      pedidosHoje: 7,
      valorHoje: 245.5,
      ultimaAtividade: '2026-07-22T14:30:00',
      problemasAbertos: 1,
    }]);
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('shows operational indicators and registered totems', () => {
    expect(fixture.nativeElement.textContent).toContain('Totens');
    expect(fixture.nativeElement.textContent).toContain('Totem 01');
    expect(fixture.nativeElement.textContent).toContain('Pedidos hoje');
    expect(fixture.nativeElement.textContent).toContain('7');
  });

  it('opens the create totem form', () => {
    const button = fixture.nativeElement.querySelector('.heading-actions .primary-button') as HTMLButtonElement;
    button.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.totem-dialog')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Cadastrar dispositivo');
  });
});
