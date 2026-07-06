import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { NotificacaoResponse } from '../models/notificacao.models';

@Injectable({
  providedIn: 'root',
})
export class GarcomChamadaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/garcom/chamadas`;

  concluirChamada(id: number): Observable<NotificacaoResponse> {
    return this.http.patch<NotificacaoResponse>(`${this.baseUrl}/${id}/concluir`, {});
  }
}
