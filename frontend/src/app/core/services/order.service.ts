import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CriarPedidoMesaRequest,
  CriarPedidoTotemRequest,
} from '../models/menu.models';

interface PedidoResponse {
  id?: number;
  status?: string;
  mensagem?: string;
}

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
}
