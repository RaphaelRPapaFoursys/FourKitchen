import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CategoriaGestorRequest,
  CategoriaGestorResponse,
  ProdutoGestorRequest,
  ProdutoGestorResponse,
} from '../models/catalog.models';

@Injectable({
  providedIn: 'root',
})
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor/catalogo`;

  listProducts(): Observable<ProdutoGestorResponse[]> {
    return this.http.get<ProdutoGestorResponse[]>(`${this.baseUrl}/produtos`);
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

  listCategories(): Observable<CategoriaGestorResponse[]> {
    return this.http.get<CategoriaGestorResponse[]>(`${this.baseUrl}/categorias`);
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
