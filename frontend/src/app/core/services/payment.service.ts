import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { PagamentoResponse } from '../models/payment.models';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private readonly http = inject(HttpClient);

  processPayment(): Observable<PagamentoResponse> {
    return this.http.post<PagamentoResponse>(`${environment.apiUrl}/api/pagamentos`, null);
  }
}
