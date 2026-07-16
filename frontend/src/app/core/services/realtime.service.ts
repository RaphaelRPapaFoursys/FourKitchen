import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  RealtimeConnectionState,
  RealtimeEvent,
} from '../models/realtime.models';

const RECONNECT_DELAYS_MS = [1_000, 2_000, 5_000, 10_000, 30_000];
const TOKEN_STORAGE_KEY = 'fourkitchen_access_token';

@Injectable({
  providedIn: 'root',
})
export class RealtimeService implements OnDestroy {
  private readonly subjects = new Map<string, Subject<RealtimeEvent>>();
  private readonly subscriptions = new Map<string, StompSubscription>();
  private readonly reconnectSubject = new Subject<void>();
  private readonly stateSubject = new BehaviorSubject<RealtimeConnectionState>('disconnected');
  private client: Client | null = null;
  private reconnectTimerId: number | null = null;
  private reconnectAttempt = 0;
  private hasConnected = false;
  private manuallyDisconnected = false;

  readonly reconnected$ = this.reconnectSubject.asObservable();
  readonly state$ = this.stateSubject.asObservable();

  constructor() {
    if (typeof window !== 'undefined') {
      window.addEventListener('online', this.handleOnline);
      window.addEventListener('offline', this.handleOffline);
    }
  }

  watch(destination: string): Observable<RealtimeEvent> {
    let subject = this.subjects.get(destination);
    if (!subject) {
      subject = new Subject<RealtimeEvent>();
      this.subjects.set(destination, subject);
    }

    this.ensureConnected();
    if (this.client?.connected) {
      this.subscribeDestination(destination);
    }

    return subject.asObservable();
  }

  disconnect(): void {
    this.manuallyDisconnected = true;
    this.clearReconnectTimer();
    this.subscriptions.clear();
    const client = this.client;
    this.client = null;
    this.stateSubject.next('disconnected');

    if (client?.active) {
      void client.deactivate();
    }
  }

  ngOnDestroy(): void {
    this.disconnect();
    if (typeof window !== 'undefined') {
      window.removeEventListener('online', this.handleOnline);
      window.removeEventListener('offline', this.handleOffline);
    }
  }

  private ensureConnected(): void {
    if (this.client?.active || !this.getToken()) {
      return;
    }

    if (typeof navigator !== 'undefined' && !navigator.onLine) {
      this.stateSubject.next('offline');
      return;
    }

    this.manuallyDisconnected = false;
    this.stateSubject.next('connecting');
    const client = new Client({
      brokerURL: this.buildWebSocketUrl(),
      reconnectDelay: 0,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,
      connectionTimeout: 8_000,
      beforeConnect: async () => {
        const token = this.getToken();
        if (!token) {
          throw new Error('Token ausente para conexao WebSocket.');
        }
        client.connectHeaders = { Authorization: `Bearer ${token}` };
      },
      onConnect: () => this.handleConnect(),
      onStompError: () => this.scheduleReconnect(),
      onWebSocketClose: () => this.scheduleReconnect(),
    });

    this.client = client;
    client.activate();
  }

  private handleConnect(): void {
    const reconnect = this.hasConnected;
    this.hasConnected = true;
    this.reconnectAttempt = 0;
    this.clearReconnectTimer();
    this.stateSubject.next('connected');
    this.subscriptions.clear();
    this.subjects.forEach((_subject, destination) => this.subscribeDestination(destination));

    if (reconnect) {
      this.reconnectSubject.next();
    }
  }

  private subscribeDestination(destination: string): void {
    if (!this.client?.connected || this.subscriptions.has(destination)) {
      return;
    }

    const subscription = this.client.subscribe(
      destination,
      message => this.handleMessage(destination, message),
    );
    this.subscriptions.set(destination, subscription);
  }

  private handleMessage(destination: string, message: IMessage): void {
    try {
      const event = JSON.parse(message.body) as RealtimeEvent;
      this.subjects.get(destination)?.next(event);
    } catch {
      // Ignora mensagens fora do contrato para manter a conexao ativa.
    }
  }

  private scheduleReconnect(): void {
    this.subscriptions.clear();
    if (this.manuallyDisconnected || !this.getToken()) {
      this.stateSubject.next('disconnected');
      return;
    }

    if (typeof navigator !== 'undefined' && !navigator.onLine) {
      this.stateSubject.next('offline');
      return;
    }

    if (this.reconnectTimerId !== null) {
      return;
    }

    const client = this.client;
    this.client = null;
    if (client?.active) {
      void client.deactivate();
    }

    this.stateSubject.next('connecting');
    const delay = RECONNECT_DELAYS_MS[
      Math.min(this.reconnectAttempt, RECONNECT_DELAYS_MS.length - 1)
    ];
    this.reconnectAttempt++;
    this.reconnectTimerId = window.setTimeout(() => {
      this.reconnectTimerId = null;
      this.ensureConnected();
    }, delay);
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimerId !== null) {
      window.clearTimeout(this.reconnectTimerId);
      this.reconnectTimerId = null;
    }
  }

  private readonly handleOnline = (): void => {
    this.clearReconnectTimer();
    this.client = null;
    this.ensureConnected();
  };

  private readonly handleOffline = (): void => {
    this.clearReconnectTimer();
    const client = this.client;
    this.client = null;
    if (client?.active) {
      void client.deactivate();
    }
    this.stateSubject.next('offline');
  };

  private getToken(): string | null {
    return typeof localStorage === 'undefined'
      ? null
      : localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  private buildWebSocketUrl(): string {
    return `${environment.apiUrl.replace(/^http/, 'ws')}/ws`;
  }
}
