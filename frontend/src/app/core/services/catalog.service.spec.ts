import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { ProdutoGestorRequest } from '../models/catalog.models';
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
    service.listProducts().subscribe(products => expect(products).toEqual([]));
    httpMock.expectOne(`${baseUrl}/produtos`).flush([]);
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
});
