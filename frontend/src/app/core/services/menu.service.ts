import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CategoriaCardapioResponse } from '../models/menu.models';

export type MenuContext = 'mesa' | 'totem' | 'garcom';

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  private readonly http = inject(HttpClient);

  getMenu(context: MenuContext): Observable<CategoriaCardapioResponse[]> {
    return this.http.get<CategoriaCardapioResponse[]>(
      `${environment.apiUrl}/api/${context}/cardapio`,
    );
  }
}
