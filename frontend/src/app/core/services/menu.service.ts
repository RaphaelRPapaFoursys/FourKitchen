import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CardapioPaginadoResponse, CategoriaCardapioResponse } from '../models/menu.models';

export type MenuContext = 'mesa' | 'totem';

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  private readonly http = inject(HttpClient);
  private readonly menuCache = new Map<MenuContext, Observable<CategoriaCardapioResponse[]>>();
  private readonly menuPageCache = new Map<string, Observable<CardapioPaginadoResponse>>();

  getMenu(context: MenuContext): Observable<CategoriaCardapioResponse[]> {
    const cached = this.menuCache.get(context);
    if (cached) {
      return cached;
    }

    const request = this.http.get<CategoriaCardapioResponse[]>(
      `${environment.apiUrl}/api/${context}/cardapio`,
    ).pipe(
      shareReplay({ bufferSize: 1, refCount: false }),
    );

    this.menuCache.set(context, request);
    return request;
  }

  refreshMenu(context: MenuContext): Observable<CategoriaCardapioResponse[]> {
    this.menuCache.delete(context);
    this.clearPageCache(context);
    return this.getMenu(context);
  }

  getMenuPage(context: MenuContext, page: number, size: number): Observable<CardapioPaginadoResponse> {
    const cacheKey = this.getPageCacheKey(context, page, size);
    const cached = this.menuPageCache.get(cacheKey);
    if (cached) {
      return cached;
    }

    const request = this.http.get<CardapioPaginadoResponse>(
      `${environment.apiUrl}/api/${context}/cardapio/paginado`,
      {
        params: {
          page,
          size,
        },
      },
    ).pipe(
      shareReplay({ bufferSize: 1, refCount: false }),
    );

    this.menuPageCache.set(cacheKey, request);
    return request;
  }

  refreshMenuPage(context: MenuContext, page: number, size: number): Observable<CardapioPaginadoResponse> {
    this.clearPageCache(context);
    this.menuCache.delete(context);
    return this.getMenuPage(context, page, size);
  }

  private clearPageCache(context: MenuContext): void {
    for (const key of Array.from(this.menuPageCache.keys())) {
      if (key.startsWith(`${context}|`)) {
        this.menuPageCache.delete(key);
      }
    }
  }

  private getPageCacheKey(context: MenuContext, page: number, size: number): string {
    return `${context}|${page}|${size}`;
  }
}
