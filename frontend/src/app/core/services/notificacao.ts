import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { DestinoNotificacao, NotificacaoResponse } from '../models/notificacao.models';

@Injectable({
  providedIn: 'root',
})
export class NotificacaoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/notificacoes`;

  listarPendentes(destino?: DestinoNotificacao): Observable<NotificacaoResponse[]> {
    const url = destino ? `${this.baseUrl}/pendentes?destino=${destino}` : `${this.baseUrl}/pendentes`;

    return this.http.get<NotificacaoResponse[]>(url);
  }

  marcarComoLida(id: number): Observable<NotificacaoResponse> {
    return this.http.patch<NotificacaoResponse>(`${this.baseUrl}/${id}/lida`, {});
  }
}
