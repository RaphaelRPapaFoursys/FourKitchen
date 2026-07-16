import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, of, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { NotificacaoResponse } from '../models/notificacao.models';
import { NotificacaoService } from './notificacao';

export interface ChamarGarcomRequest {
  codigoSessao: number;
}

interface ChamadaGarcomPendente {
  codigoAtendimento: number;
  notificacaoId: number;
}

@Injectable({
  providedIn: 'root',
})
export class MesaChamadaService {
  private readonly http = inject(HttpClient);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly baseUrl = `${environment.apiUrl}/api/mesa/chamadas-garcom`;
  private readonly storageKey = 'fourkitchen_mesa_waiter_call';
  private readonly chamadaPendente = signal<ChamadaGarcomPendente | null>(
    this.readStoredCall(),
  );

  readonly chamadaAtendimentoCodigoAtual = computed(
    () => this.chamadaPendente()?.codigoAtendimento ?? null,
  );

  readonly chamadaNotificacaoIdAtual = computed(
    () => this.chamadaPendente()?.notificacaoId ?? null,
  );

  chamarGarcom(codigoSessao: number): Observable<NotificacaoResponse> {
    const request: ChamarGarcomRequest = { codigoSessao };

    return this.http.post<NotificacaoResponse>(this.baseUrl, request);
  }

  marcarChamadaEmAndamento(codigoAtendimento: number, notificacaoId: number): void {
    const chamada = { codigoAtendimento, notificacaoId };
    this.chamadaPendente.set(chamada);
    this.storeCall(chamada);
  }

  sincronizarChamadaPendente(codigoAtendimento: number): Observable<boolean> {
    const chamada = this.chamadaPendente();

    if (!chamada || chamada.codigoAtendimento !== codigoAtendimento) {
      return of(false);
    }

    return this.notificacaoService.listarPendentes('GARCOM').pipe(
      map(notificacoes => notificacoes.some(notificacao => notificacao.id === chamada.notificacaoId)),
      tap(pendente => {
        if (!pendente) {
          this.clearStoredCall();
        }
      }),
    );
  }

  concluirChamadaEmAndamento(notificacaoId: number): void {
    if (this.chamadaPendente()?.notificacaoId === notificacaoId) {
      this.clearStoredCall();
    }
  }

  private readStoredCall(): ChamadaGarcomPendente | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }

    const stored = localStorage.getItem(this.storageKey);
    if (!stored) {
      return null;
    }

    try {
      const parsed = JSON.parse(stored) as Partial<ChamadaGarcomPendente>;
      return Number.isInteger(parsed.codigoAtendimento) && Number.isInteger(parsed.notificacaoId)
        ? parsed as ChamadaGarcomPendente
        : null;
    } catch {
      return null;
    }
  }

  private storeCall(chamada: ChamadaGarcomPendente): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(this.storageKey, JSON.stringify(chamada));
    }
  }

  private clearStoredCall(): void {
    this.chamadaPendente.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(this.storageKey);
    }
  }
}
