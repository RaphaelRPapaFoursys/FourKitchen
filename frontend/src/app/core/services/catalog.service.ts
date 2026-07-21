import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CategoriaGestorRequest,
  CategoriaGestorResponse,
  CategoriaOpcaoResponse,
  CatalogPageResponse,
  ProdutoGestorRequest,
  ProdutoGestorResponse,
} from '../models/catalog.models';

@Injectable({
  providedIn: 'root',
})
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor/catalogo`;

  listProducts(
    page = 0,
    size = 10,
    busca = '',
    categoriaId: number | null = null,
  ): Observable<CatalogPageResponse<ProdutoGestorResponse>> {
    return this.http.get<CatalogPageResponse<ProdutoGestorResponse>>(`${this.baseUrl}/produtos`, {
      params: {
        page,
        size,
        ...(busca.trim() ? { busca: busca.trim() } : {}),
        ...(categoriaId === null ? {} : { categoriaId }),
      },
    });
  }

  createProduct(request: ProdutoGestorRequest): Observable<ProdutoGestorResponse> {
    return this.http.post<ProdutoGestorResponse>(`${this.baseUrl}/produtos`, request);
  }

  updateProduct(id: number, request: ProdutoGestorRequest): Observable<ProdutoGestorResponse> {
    return this.http.put<ProdutoGestorResponse>(`${this.baseUrl}/produtos/${id}`, request);
  }

  activateProduct(id: number): Observable<ProdutoGestorResponse> {
    return this.http.patch<ProdutoGestorResponse>(`${this.baseUrl}/produtos/${id}/ativar`, {});
  }

  deactivateProduct(id: number): Observable<ProdutoGestorResponse> {
    return this.http.patch<ProdutoGestorResponse>(`${this.baseUrl}/produtos/${id}/desativar`, {});
  }

  listCategories(
    page = 0,
    size = 10,
    busca = '',
    ativo: boolean | null = null,
  ): Observable<CatalogPageResponse<CategoriaGestorResponse>> {
    return this.http.get<CatalogPageResponse<CategoriaGestorResponse>>(`${this.baseUrl}/categorias`, {
      params: {
        page,
        size,
        ...(busca.trim() ? { busca: busca.trim() } : {}),
        ...(ativo === null ? {} : { ativo }),
      },
    });
  }

  listCategoryOptions(): Observable<CategoriaOpcaoResponse[]> {
    return this.http.get<CategoriaOpcaoResponse[]>(`${this.baseUrl}/categorias/opcoes`);
  }

  createCategory(request: CategoriaGestorRequest): Observable<CategoriaGestorResponse> {
    return this.http.post<CategoriaGestorResponse>(`${this.baseUrl}/categorias`, request);
  }

  updateCategory(id: number, request: CategoriaGestorRequest): Observable<CategoriaGestorResponse> {
    return this.http.put<CategoriaGestorResponse>(`${this.baseUrl}/categorias/${id}`, request);
  }

  activateCategory(id: number): Observable<CategoriaGestorResponse> {
    return this.http.patch<CategoriaGestorResponse>(`${this.baseUrl}/categorias/${id}/ativar`, {});
  }

  deactivateCategory(id: number): Observable<CategoriaGestorResponse> {
    return this.http.patch<CategoriaGestorResponse>(`${this.baseUrl}/categorias/${id}/desativar`, {});
  }
}
