import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AtualizarUsuarioGestorRequest,
  CriarUsuarioGestorRequest,
  UsuarioGestorResponse,
} from '../models/user-management.models';

@Injectable({
  providedIn: 'root',
})
export class UserManagementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor/usuarios`;

  listUsers(): Observable<UsuarioGestorResponse[]> {
    return this.http.get<UsuarioGestorResponse[]>(this.baseUrl);
  }

  createUser(request: CriarUsuarioGestorRequest): Observable<UsuarioGestorResponse> {
    return this.http.post<UsuarioGestorResponse>(this.baseUrl, request);
  }

  updateUser(
    id: number,
    request: AtualizarUsuarioGestorRequest,
  ): Observable<UsuarioGestorResponse> {
    return this.http.put<UsuarioGestorResponse>(`${this.baseUrl}/${id}`, request);
  }

  deactivateUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  activateUser(id: number): Observable<UsuarioGestorResponse> {
    return this.http.patch<UsuarioGestorResponse>(`${this.baseUrl}/${id}/ativar`, {});
  }
}
