import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TotemGestorResponse } from '../models/totem-management.models';

@Injectable({ providedIn: 'root' })
export class TotemManagementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor/totens`;

  listTotems(): Observable<TotemGestorResponse[]> {
    return this.http.get<TotemGestorResponse[]>(this.baseUrl);
  }
}
