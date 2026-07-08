import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CriarPedidoMesaRequest,
  CriarPedidoTotemRequest,
} from '../models/menu.models';
import { PedidoMesaStatusResponse, PedidoResponse } from '../models/order.models';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly http = inject(HttpClient);

  createMesaOrder(request: CriarPedidoMesaRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${environment.apiUrl}/api/mesa/pedidos`, request);
  }

  createTotemOrder(request: CriarPedidoTotemRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(`${environment.apiUrl}/api/totem/pedidos`, request);
  }

  getMesaOrders(codigoAtendimento: number): Observable<PedidoMesaStatusResponse[]> {
    // TODO: ajustar o nome do parametro se o BFF oficializar outro contrato.
    return this.http.get<PedidoMesaStatusResponse[]>(
      `${environment.apiUrl}/api/mesa/pedidos`,
      { params: { codigoAtendimento } },
    );
  }
}
