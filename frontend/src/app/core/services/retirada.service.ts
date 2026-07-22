import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  PedidoBalcaoResponse,
  PedidoPainelRetiradaResponse,
} from '../models/retirada.models';

@Injectable({ providedIn: 'root' })
export class RetiradaService {
  private readonly http = inject(HttpClient);

  listarPainelPublico(): Observable<PedidoPainelRetiradaResponse[]> {
    return this.http.get<PedidoPainelRetiradaResponse[]>(
      `${environment.apiUrl}/api/painel-retirada/pedidos`,
    );
  }

  listarFilaBalcao(): Observable<PedidoBalcaoResponse[]> {
    return this.http.get<PedidoBalcaoResponse[]>(`${environment.apiUrl}/api/balcao/pedidos`);
  }

  entregar(id: number): Observable<PedidoBalcaoResponse> {
    return this.http.patch<PedidoBalcaoResponse>(
      `${environment.apiUrl}/api/balcao/pedidos/${id}/entregar`,
      {},
    );
  }
}
