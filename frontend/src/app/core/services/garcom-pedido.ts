import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CriarPedidoGarcomRequest, PedidoGarcomResponse } from '../models/garcom.models';

@Injectable({ providedIn: 'root' })
export class GarcomPedidoService {
  private readonly http = inject(HttpClient);

  criarPedido(request: CriarPedidoGarcomRequest): Observable<PedidoGarcomResponse> {
    return this.http.post<PedidoGarcomResponse>(`${environment.apiUrl}/api/garcom/pedidos`, request);
  }
}
