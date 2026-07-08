import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CriarPedidoMesaRequest,
  CriarPedidoTotemRequest,
} from '../models/menu.models';
import {
  MesaAtendimentoAtualResponse,
  PedidoMesaResponse,
  PedidoMesaStatusResponse,
  PedidoTotemResponse,
} from '../models/order.models';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly http = inject(HttpClient);

  createMesaOrder(request: CriarPedidoMesaRequest): Observable<PedidoMesaResponse> {
    return this.http.post<PedidoMesaResponse>(`${environment.apiUrl}/api/mesa/pedidos`, request);
  }

  createTotemOrder(request: CriarPedidoTotemRequest): Observable<PedidoTotemResponse> {
    return this.http.post<PedidoTotemResponse>(`${environment.apiUrl}/api/totem/pedidos`, request);
  }

  getCurrentTableAttendance(): Observable<MesaAtendimentoAtualResponse> {
    return this.http.get<MesaAtendimentoAtualResponse>(
      `${environment.apiUrl}/api/mesa/atendimento-atual`,
    );
  }

  getMesaOrders(codigoAtendimento: number): Observable<PedidoMesaStatusResponse[]> {
    return this.http.get<PedidoMesaStatusResponse[]>(
      `${environment.apiUrl}/api/mesa/pedidos`,
      { params: { codigoAtendimento } },
    );
  }
}
