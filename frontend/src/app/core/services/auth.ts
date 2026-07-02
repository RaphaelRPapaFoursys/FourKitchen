import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  LoginFormValue,
  LoginRequest,
  LoginResponse,
  UsuarioAutenticadoResponse,
} from '../models/auth.models';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorageKey = 'fourkitchen_access_token';
  private readonly legacyUserStorageKey = 'fourkitchen_user';
  private readonly usuarioSubject = new BehaviorSubject<UsuarioAutenticadoResponse | null>(null);

  readonly usuario$ = this.usuarioSubject.asObservable();

  constructor() {
    this.removeStoredValue(this.legacyUserStorageKey);
  }

  login(credentials: LoginFormValue): Observable<LoginResponse> {
    const request: LoginRequest = {
      email: credentials.email,
      senha: credentials.password,
    };

    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/api/auth/login`, request)
      .pipe(tap(response => this.persistSession(response)));
  }

  me(): Observable<UsuarioAutenticadoResponse> {
    return this.http
      .get<UsuarioAutenticadoResponse>(`${environment.apiUrl}/api/auth/me`)
      .pipe(tap(usuario => this.usuarioSubject.next(usuario)));
  }

  logout(): void {
    this.removeStoredValue(this.tokenStorageKey);
    this.removeStoredValue(this.legacyUserStorageKey);
    this.usuarioSubject.next(null);
  }

  getToken(): string | null {
    return this.readStoredValue(this.tokenStorageKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getCurrentUser(): UsuarioAutenticadoResponse | null {
    return this.usuarioSubject.value;
  }

  private persistSession(response: LoginResponse): void {
    this.storeValue(this.tokenStorageKey, response.accessToken);
    this.usuarioSubject.next(response.usuario);
  }

  private readStoredValue(key: string): string | null {
    if (!this.hasStorage()) {
      return null;
    }

    return localStorage.getItem(key);
  }

  private storeValue(key: string, value: string): void {
    if (!this.hasStorage()) {
      return;
    }

    localStorage.setItem(key, value);
  }

  private removeStoredValue(key: string): void {
    if (!this.hasStorage()) {
      return;
    }

    localStorage.removeItem(key);
  }

  private hasStorage(): boolean {
    return typeof localStorage !== 'undefined';
  }
}
