import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { catchError, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

import { CategoriaCardapio, MenuResponse, ProdutoCardapio } from '../../core/models/menu.models';
import { MenuService } from '../../core/services/menu.service';

type MenuLoadState =
  | { status: 'loading'; data: null; message: string }
  | { status: 'error'; data: null; message: string }
  | { status: 'success'; data: MenuResponse; message: string };

interface StaticCategoryCard {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
}

@Component({
  selector: 'app-customer-home',
  imports: [CommonModule],
  templateUrl: './customer-home.html',
  styleUrl: './customer-home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerHome {
  private readonly menuService = inject(MenuService);

  protected readonly selectedCategoryId = signal<number | null>(null);
  protected readonly staticCategories: StaticCategoryCard[] = [
    {
      id: 1,
      title: 'Entradas',
      description: 'Lorem ipsum dolor sit, consectetur adipiscing elit, sed do eiusmod tempor',
      imageUrl: 'assets/images/entradas.png',
    },
    {
      id: 2,
      title: 'Pratos Prontos',
      description: 'Lorem ipsum dolor sit, consectetur adipiscing elit, sed do eiusmod tempor',
      imageUrl: 'assets/images/prontos.png',
    },
    {
      id: 3,
      title: 'Japonesa',
      description: 'Lorem ipsum dolor sit, consectetur adipiscing elit, sed do eiusmod tempor',
      imageUrl: 'assets/images/japonesa.png',
    },
    {
      id: 4,
      title: 'Vegetariana',
      description: 'Lorem ipsum dolor sit, consectetur adipiscing elit, sed do eiusmod tempor',
      imageUrl: 'assets/images/vegetariana.png',
    },
  ];

  protected readonly menuState = toSignal(
    this.menuService.getMenu().pipe(
      map(data => ({ status: 'success', data, message: '' }) satisfies MenuLoadState),
      startWith({ status: 'loading', data: null, message: 'Carregando cardapio...' } satisfies MenuLoadState),
      catchError(() =>
        [
          {
          status: 'error',
          data: null,
          message: 'Nao foi possivel carregar o cardapio no momento.',
          } satisfies MenuLoadState,
        ],
      ),
    ),
    {
      initialValue: {
        status: 'loading',
        data: null,
        message: 'Carregando cardapio...',
      } satisfies MenuLoadState,
    },
  );

  protected readonly categories = computed(() => this.menuState().data?.categorias ?? []);
  protected readonly products = computed(() => this.menuState().data?.produtos ?? []);

  protected readonly filteredProducts = computed(() => {
    const categoryId = this.selectedCategoryId();
    const products = this.products().filter(product => product.disponivel);

    if (categoryId === null) {
      return products;
    }

    return products.filter(product => product.categoriaId === categoryId);
  });

  protected readonly hasMenuContent = computed(
    () => this.categories().length > 0 || this.products().length > 0,
  );

  protected selectCategory(categoryId: number | null): void {
    this.selectedCategoryId.set(categoryId);
  }

  protected scrollToSection(sectionId: string, event: Event): void {
    event.preventDefault();
    document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  protected addToCart(_product: ProdutoCardapio): void {
    // TODO: integrar com carrinho futuramente.
  }

  protected trackByCategoryId(_index: number, category: CategoriaCardapio): number {
    return category.id;
  }

  protected trackByStaticCategoryId(_index: number, category: StaticCategoryCard): number {
    return category.id;
  }

  protected trackByProductId(_index: number, product: ProdutoCardapio): number {
    return product.id;
  }

  protected getCategoryImage(category: CategoriaCardapio): string | null {
    if (category.imagemUrl) {
      return category.imagemUrl;
    }

    const slug = category.slug?.trim();
    return slug ? `assets/images/${slug}.png` : null;
  }

  protected getProductImage(product: ProdutoCardapio): string | null {
    if (product.imagemUrl) {
      return product.imagemUrl;
    }

    const slug = product.categoriaSlug?.trim();
    return slug ? `assets/images/${slug}.png` : null;
  }

  protected formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price);
  }
}
