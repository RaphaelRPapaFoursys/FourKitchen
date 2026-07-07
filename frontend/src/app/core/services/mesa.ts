import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AtribuirGarcomRequest,
  CriarMesaRequest,
  MesaResponse,
} from '../models/mesa.models';

@Injectable({
  providedIn: 'root',
})
export class MesaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/mesas`;

  listarMesas(): Observable<MesaResponse[]> {
    return this.http.get<MesaResponse[]>(this.baseUrl);
  }

  criarMesa(numero: number): Observable<MesaResponse> {
    const request: CriarMesaRequest = { numero };

    return this.http.post<MesaResponse>(this.baseUrl, request);
  }

  abrirMesa(id: number): Observable<MesaResponse> {
    return this.http.patch<MesaResponse>(`${this.baseUrl}/${id}/abrir`, {});
  }

  fecharMesa(id: number): Observable<MesaResponse> {
    return this.http.patch<MesaResponse>(`${this.baseUrl}/${id}/fechar`, {});
  }

  atribuirGarcom(id: number, garcomId: number): Observable<MesaResponse> {
    const request: AtribuirGarcomRequest = { garcomId };

    return this.http.patch<MesaResponse>(`${this.baseUrl}/${id}/atribuir-garcom`, request);
  }
}
