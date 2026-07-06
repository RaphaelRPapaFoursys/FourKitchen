import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { NotificacaoResponse } from '../models/notificacao.models';

export interface ChamarGarcomRequest {
  codigoSessao: number;
}

@Injectable({
  providedIn: 'root',
})
export class MesaChamadaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/mesa/chamadas-garcom`;

  chamarGarcom(codigoSessao: number): Observable<NotificacaoResponse> {
    const request: ChamarGarcomRequest = { codigoSessao };

    return this.http.post<NotificacaoResponse>(this.baseUrl, request);
  }
}
