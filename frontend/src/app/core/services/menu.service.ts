import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CardapioPaginadoResponse, CategoriaCardapioResponse, CategoriaMenuResponse } from '../models/menu.models';

export type MenuContext = 'mesa' | 'totem' | 'garcom';

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  private readonly http = inject(HttpClient);
  private readonly menuCache = new Map<MenuContext, Observable<CategoriaCardapioResponse[]>>();
  private readonly categoryCache = new Map<MenuContext, Observable<CategoriaMenuResponse[]>>();
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

  getMenuCategories(context: MenuContext): Observable<CategoriaMenuResponse[]> {
    const cached = this.categoryCache.get(context);
    if (cached) {
      return cached;
    }

    const request = this.http.get<CategoriaMenuResponse[]>(
      `${environment.apiUrl}/api/${context}/categorias`,
    ).pipe(
      shareReplay({ bufferSize: 1, refCount: false }),
    );

    this.categoryCache.set(context, request);
    return request;
  }

  refreshMenuCategories(context: MenuContext): Observable<CategoriaMenuResponse[]> {
    this.categoryCache.delete(context);
    return this.getMenuCategories(context);
  }

  getMenuPage(
    context: MenuContext,
    page: number,
    size: number,
    categoriaId: number | null = null,
  ): Observable<CardapioPaginadoResponse> {
    const cacheKey = this.getPageCacheKey(context, page, size, categoriaId);
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
          ...(categoriaId === null ? {} : { categoriaId }),
        },
      },
    ).pipe(
      shareReplay({ bufferSize: 1, refCount: false }),
    );

    this.menuPageCache.set(cacheKey, request);
    return request;
  }

  refreshMenuPage(
    context: MenuContext,
    page: number,
    size: number,
    categoriaId: number | null = null,
  ): Observable<CardapioPaginadoResponse> {
    this.clearPageCache(context);
    this.menuCache.delete(context);
    return this.getMenuPage(context, page, size, categoriaId);
  }

  private clearPageCache(context: MenuContext): void {
    for (const key of Array.from(this.menuPageCache.keys())) {
      if (key.startsWith(`${context}|`)) {
        this.menuPageCache.delete(key);
      }
    }
  }

  private getPageCacheKey(context: MenuContext, page: number, size: number, categoriaId: number | null): string {
    return `${context}|${page}|${size}|${categoriaId ?? 'todos'}`;
  }
}
