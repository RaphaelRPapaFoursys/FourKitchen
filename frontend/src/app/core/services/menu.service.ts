import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { MenuResponse } from '../models/menu.models';

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  private readonly http = inject(HttpClient);

  getMenu(): Observable<MenuResponse> {
    // TODO: ajustar endpoint quando o BFF disponibilizar a rota oficial de cardapio.
    return this.http.get<MenuResponse>(`${environment.apiUrl}/api/cardapio`);
  }
}
