import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CategoriaGestorRequest, ProdutoGestorRequest } from '../models/catalog.models';
import { CatalogService } from './catalog.service';

describe('CatalogService', () => {
  const baseUrl = `${environment.apiUrl}/api/gestor/catalogo`;
  let service: CatalogService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CatalogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists products through the BFF', () => {
    const page = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true };
    service.listProducts().subscribe(products => expect(products).toEqual(page));
    const request = httpMock.expectOne(req => req.url === `${baseUrl}/produtos`);
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    request.flush(page);
  });

  it('lists lightweight category options', () => {
    service.listCategoryOptions().subscribe(categories => expect(categories[0].nome).toBe('Pratos'));
    httpMock.expectOne(`${baseUrl}/categorias/opcoes`).flush([{ id: 1, nome: 'Pratos', ativo: true }]);
  });

  it('sends the category when filtering products', () => {
    service.listProducts(0, 10, '', 3).subscribe();
    const request = httpMock.expectOne(req => req.url === `${baseUrl}/produtos`);
    expect(request.request.params.get('categoriaId')).toBe('3');
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('sends the status when filtering categories', () => {
    service.listCategories(0, 10, '', false).subscribe();
    const request = httpMock.expectOne(req => req.url === `${baseUrl}/categorias`);
    expect(request.request.params.get('ativo')).toBe('false');
    request.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, first: true, last: true });
  });

  it('sends the Base64 image when creating a product', () => {
    const request: ProdutoGestorRequest = {
      nome: 'Risoto',
      descricao: null,
      imagem: 'data:image/png;base64,iVBORw0KGgo',
      preco: 49.9,
      categoriaId: 1,
    };

    service.createProduct(request).subscribe();
    const httpRequest = httpMock.expectOne(`${baseUrl}/produtos`);
    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);
    httpRequest.flush({});
  });

  it('sends image as null to preserve it when editing without a new selection', () => {
    const request: ProdutoGestorRequest = {
      nome: 'Risoto atualizado',
      descricao: 'Nova descrição',
      imagem: null,
      preco: 55,
      categoriaId: 1,
    };

    service.updateProduct(10, request).subscribe();
    const httpRequest = httpMock.expectOne(`${baseUrl}/produtos/10`);
    expect(httpRequest.request.method).toBe('PUT');
    expect(httpRequest.request.body.imagem).toBeNull();
    httpRequest.flush({});
  });

  it('creates a category with an optional Base64 image', () => {
    const request: CategoriaGestorRequest = {
      nome: 'Pratos principais',
      descricao: 'Refeições servidas no almoço',
      imagem: 'data:image/png;base64,iVBORw0KGgo',
    };

    service.createCategory(request).subscribe();
    const httpRequest = httpMock.expectOne(`${baseUrl}/categorias`);
    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);
    httpRequest.flush({});
  });

  it('updates a category while preserving its current image', () => {
    const request: CategoriaGestorRequest = {
      nome: 'Pratos especiais',
      descricao: null,
      imagem: null,
    };

    service.updateCategory(3, request).subscribe();
    const httpRequest = httpMock.expectOne(`${baseUrl}/categorias/3`);
    expect(httpRequest.request.method).toBe('PUT');
    expect(httpRequest.request.body.imagem).toBeNull();
    httpRequest.flush({});
  });

  it('activates and deactivates a category', () => {
    service.activateCategory(3).subscribe();
    const activateRequest = httpMock.expectOne(`${baseUrl}/categorias/3/ativar`);
    expect(activateRequest.request.method).toBe('PATCH');
    activateRequest.flush({});

    service.deactivateCategory(3).subscribe();
    const deactivateRequest = httpMock.expectOne(`${baseUrl}/categorias/3/desativar`);
    expect(deactivateRequest.request.method).toBe('PATCH');
    deactivateRequest.flush({});
  });
});
