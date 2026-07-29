import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { EventoRealtime } from '../models/realtime.models';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class RealtimeService {
  private readonly authService = inject(AuthService);
  private readonly endpoint = `${environment.apiUrl}/api/realtime/eventos`;
  private readonly retryDelayMillis = 3000;

  eventos<T = unknown>(): Observable<EventoRealtime<T>> {
    return new Observable(observer => {
      const abortController = new AbortController();
      let retryTimer: ReturnType<typeof setTimeout> | undefined;

      const agendarReconexao = (): void => {
        if (!abortController.signal.aborted) {
          retryTimer = setTimeout(() => void conectar(), this.retryDelayMillis);
        }
      };

      const conectar = async (): Promise<void> => {
        const token = this.authService.getToken();
        if (!token) {
          observer.complete();
          return;
        }

        try {
          const response = await fetch(this.endpoint, {
            headers: {
              Accept: 'text/event-stream',
              Authorization: `Bearer ${token}`,
            },
            signal: abortController.signal,
          });

          if (!response.ok || !response.body) {
            throw new Error(`Falha ao conectar ao SSE: HTTP ${response.status}`);
          }

          await this.lerEventos(response.body, evento => observer.next(evento as EventoRealtime<T>));
          agendarReconexao();
        } catch {
          agendarReconexao();
        }
      };

      void conectar();

      return () => {
        abortController.abort();
        if (retryTimer) {
          clearTimeout(retryTimer);
        }
      };
    });
  }

  private async lerEventos(
    stream: ReadableStream<Uint8Array>,
    aoReceber: (evento: EventoRealtime) => void,
  ): Promise<void> {
    const reader = stream.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true }).replaceAll('\r\n', '\n');
      const blocos = buffer.split('\n\n');
      buffer = blocos.pop() ?? '';

      for (const bloco of blocos) {
        const dados = bloco
          .split('\n')
          .filter(linha => linha.startsWith('data:'))
          .map(linha => linha.slice(5).trimStart())
          .join('\n');

        if (dados) {
          aoReceber(JSON.parse(dados) as EventoRealtime);
        }
      }
    }
  }
}
