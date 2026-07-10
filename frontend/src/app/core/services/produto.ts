import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CategoriaGestorResponse,
  CriarCategoriaRequest,
  CriarProdutoRequest,
  ProdutoGestorResponse,
} from '../models/produto.models';

/**
 * Consome os endpoints já existentes do BFF gestor (`/api/gestor`).
 * Não cria nenhum endpoint/campo novo no backend.
 */
@Injectable({
  providedIn: 'root',
})
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor`;

  listarCategorias(): Observable<CategoriaGestorResponse[]> {
    return this.http.get<CategoriaGestorResponse[]>(`${this.baseUrl}/categorias`);
  }

  criarCategoria(request: CriarCategoriaRequest): Observable<CategoriaGestorResponse> {
    return this.http.post<CategoriaGestorResponse>(`${this.baseUrl}/categorias`, request);
  }

  criarProduto(request: CriarProdutoRequest): Observable<ProdutoGestorResponse> {
    return this.http.post<ProdutoGestorResponse>(`${this.baseUrl}/produtos`, request);
  }

  desativarProduto(id: number): Observable<ProdutoGestorResponse> {
    return this.http.patch<ProdutoGestorResponse>(`${this.baseUrl}/produtos/${id}/desativar`, {});
  }
}
