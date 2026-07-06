import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { MesaGarcomResponse } from '../models/garcom.models';

@Injectable({
  providedIn: 'root',
})
export class GarcomMesaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/garcom/mesas`;

  listarMesas(): Observable<MesaGarcomResponse[]> {
    return this.http.get<MesaGarcomResponse[]>(this.baseUrl);
  }
}
