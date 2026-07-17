import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DecisaoProblemaGarcomRequest,
  PedidoProblemaTotemGarcomResponse,
} from '../models/garcom.models';

@Injectable({ providedIn: 'root' })
export class GarcomTotemProblemaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/garcom/pedidos-totem`;

  listarProblemas(): Observable<PedidoProblemaTotemGarcomResponse[]> {
    return this.http.get<PedidoProblemaTotemGarcomResponse[]>(`${this.baseUrl}/problemas`);
  }

  assumir(idPedido: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${idPedido}/problemas/assumir`, {});
  }

  registrarDecisao(idPedido: number, request: DecisaoProblemaGarcomRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${idPedido}/problemas/decisao`, request);
  }
}
