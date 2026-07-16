import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  DecisaoProblemaGarcomRequest,
  FechamentoContaGarcomResponse,
  MesaGarcomDetalheResponse,
  MesaGarcomResponse,
  MesaProblemasGarcomResponse,
} from '../models/garcom.models';

@Injectable({
  providedIn: 'root',
})
export class GarcomMesaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/garcom/mesas`;

  listarMesas(): Observable<MesaGarcomResponse[]> {
    return this.http.get<MesaGarcomResponse[]>(this.baseUrl);
  }

  detalharMesa(idMesa: number): Observable<MesaGarcomDetalheResponse> {
    return this.http.get<MesaGarcomDetalheResponse>(`${this.baseUrl}/${idMesa}/detalhe`);
  }

  listarProblemas(idMesa: number): Observable<MesaProblemasGarcomResponse> {
    return this.http.get<MesaProblemasGarcomResponse>(`${this.baseUrl}/${idMesa}/problemas`);
  }

  registrarDecisao(
    idMesa: number,
    request: DecisaoProblemaGarcomRequest,
  ): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${idMesa}/problemas/decisao`, request);
  }

  fecharConta(idMesa: number): Observable<FechamentoContaGarcomResponse> {
    return this.http.patch<FechamentoContaGarcomResponse>(`${this.baseUrl}/${idMesa}/fechar-conta`, {});
  }

  marcarPedidoComoEntregue(idMesa: number, idPedido: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${idMesa}/pedidos/${idPedido}/entregar`, {});
  }

  cancelarPedidoAntesDoPreparo(idMesa: number, idPedido: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${idMesa}/pedidos/${idPedido}/cancelar`, {});
  }
}
