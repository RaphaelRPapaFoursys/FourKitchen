import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  PedidoFilaCozinhaResponse,
  StatusPedidoCozinha,
} from '../models/cozinha.models';

@Injectable({
  providedIn: 'root',
})
export class CozinhaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/cozinha`;

  listarFila(): Observable<PedidoFilaCozinhaResponse[]> {
    return this.http.get<PedidoFilaCozinhaResponse[]>(`${this.baseUrl}/fila`);
  }

  alterarStatus(id: number, status: StatusPedidoCozinha): Observable<void> {
    const acao = status === 'EM_PREPARO' ? 'iniciar' : 'finalizar';

    return this.http.patch<void>(`${this.baseUrl}/pedidos/${id}/${acao}`, {});
  }
}
