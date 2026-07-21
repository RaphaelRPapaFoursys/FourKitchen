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

    httpMock.expectOne(req => req.url === `${baseUrl}/produtos`).flush({
      content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true,
    });
    httpMock.expectOne(`${baseUrl}/categorias/opcoes`).flush([
      { id: 1, nome: 'Pratos', ativo: true },
      { id: 2, nome: 'Desativada', ativo: false },
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

  it('filters products by a category loaded from the backend', () => {
    const categoryButtons = [...fixture.nativeElement.querySelectorAll('.category-filters button')] as HTMLButtonElement[];
    expect(categoryButtons.some(button => button.textContent?.trim() === 'Desativada')).toBe(false);
    categoryButtons.find(button => button.textContent?.trim() === 'Pratos')?.click();

    const request = httpMock.expectOne(req =>
      req.url === `${baseUrl}/produtos` && req.params.get('categoriaId') === '1',
    );
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
    fixture.detectChanges();

    expect(request.request.params.get('page')).toBe('0');
  });
});
