import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { GestorProducts } from './gestor-products';

describe('GestorProducts', () => {
  const baseUrl = `${environment.apiUrl}/api/gestor/catalogo`;
  let fixture: ComponentFixture<GestorProducts>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [GestorProducts],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(GestorProducts);
    fixture.detectChanges();

    httpMock.expectOne(`${baseUrl}/produtos`).flush([]);
    httpMock.expectOne(`${baseUrl}/categorias`).flush([
      { id: 1, nome: 'Pratos', descricao: null, imagem: null, ativo: true },
    ]);
    await fixture.whenStable();
    fixture.detectChanges();
  });

  afterEach(() => httpMock.verify());

  it('creates the product management page', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Produtos');
  });

  it('opens the product form with the image upload field', () => {
    const createButton = fixture.nativeElement.querySelector('.page-heading .primary-button') as HTMLButtonElement;
    createButton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.product-dialog')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('fk-product-image-upload')).toBeTruthy();
  });
});
