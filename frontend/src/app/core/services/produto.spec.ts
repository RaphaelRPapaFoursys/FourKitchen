import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { environment } from '../../../environments/environment';
import { CriarProdutoRequest } from '../models/produto.models';
import { ProdutoService } from './produto';

const BASE_URL = `${environment.apiUrl}/api/gestor`;

describe('ProdutoService', () => {
  let service: ProdutoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProdutoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('listarCategorias faz GET em /api/gestor/categorias', () => {
    service.listarCategorias().subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/categorias`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('criarProduto faz POST com o corpo enviado', () => {
    const body: CriarProdutoRequest = {
      nome: 'Água',
      descricao: null,
      imagem: null,
      preco: 6.9,
      categoriaId: 3,
      disponivel: true,
    };
    service.criarProduto(body).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/produtos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('desativarProduto faz PATCH em /produtos/:id/desativar', () => {
    service.desativarProduto(7).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/produtos/7/desativar`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('criarCategoria faz POST em /api/gestor/categorias', () => {
    service.criarCategoria({ nome: 'Massas' }).subscribe();
    const req = httpMock.expectOne(`${BASE_URL}/categorias`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nome: 'Massas' });
    req.flush({});
  });
});
