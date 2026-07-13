import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
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
}
