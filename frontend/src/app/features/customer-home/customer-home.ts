import { CommonModule } from '@angular/common';
import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, HostListener, ViewChild, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, catchError, map, startWith, switchMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

import {
  CategoriaCardapioResponse,
  ProdutoCardapioView,
} from '../../core/models/menu.models';
import { CartItem } from '../../core/models/cart.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { MenuContext, MenuService } from '../../core/services/menu.service';
import { CategoryCarouselComponent } from './components/category-carousel/category-carousel';
import { CustomerFooterComponent } from './components/customer-footer/customer-footer';
import { CustomerHeroComponent } from './components/customer-hero/customer-hero';
import { CustomerHomeHeaderComponent } from './components/customer-home-header/customer-home-header';
import { MenuFilterBarComponent } from './components/menu-filter-bar/menu-filter-bar';
import { ProductDetailsModalComponent } from './components/product-details-modal/product-details-modal';
import { ProductGridComponent } from './components/product-grid/product-grid';

type MenuLoadState =
  | { status: 'loading'; data: null; message: string }
  | { status: 'error'; data: null; message: string }
  | { status: 'success'; data: CategoriaCardapioResponse[]; message: string };

@Component({
  selector: 'app-customer-home',
  imports: [
    CommonModule,
    FormsModule,
    CustomerHomeHeaderComponent,
    CustomerHeroComponent,
    CategoryCarouselComponent,
    MenuFilterBarComponent,
    ProductGridComponent,
    ProductDetailsModalComponent,
    CustomerFooterComponent,
  ],
  templateUrl: './customer-home.html',
  styleUrl: './customer-home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CustomerHome implements AfterViewInit {
  @ViewChild('menuSection') private menuSection?: ElementRef<HTMLElement>;
  @ViewChild(CategoryCarouselComponent) private categoryCarouselComponent?: CategoryCarouselComponent;

  private readonly menuService = inject(MenuService);
  private readonly cartService = inject(CartService);
  private readonly customerContextService = inject(CustomerContextService);
  private readonly router = inject(Router);
  private readonly reloadMenuSubject = new Subject<void>();
  private scrollAnimationFrameId: number | null = null;
  private cartFeedbackTimeoutId: number | null = null;

  protected readonly selectedCategoryId = signal<number | null>(null);
  protected readonly selectedProduct = signal<ProdutoCardapioView | null>(null);
  protected readonly selectedQuantity = signal(1);
  protected readonly selectedObservation = signal('');
  protected readonly cartItemsCount = signal(0);
  protected readonly cartFeedback = signal('');
  protected readonly canScrollCategoriesLeft = signal(false);
  protected readonly canScrollCategoriesRight = signal(false);
  protected readonly cartRoute = computed(() =>
    this.customerContextService.getCartRoute(this.getCurrentContext()),
  );
  protected readonly ordersRoute = computed(() =>
    this.customerContextService.getOrdersRoute(this.getCurrentContext()),
  );
  protected readonly showOrdersLink = computed(() => this.getCurrentContext() === 'mesa');

  protected readonly menuState = toSignal(
    this.reloadMenuSubject.pipe(
      startWith({ status: 'loading', data: null, message: 'Carregando cardapio...' } satisfies MenuLoadState),
      switchMap(() =>
        this.menuService.getMenu(this.getCurrentContext()).pipe(
          map(data => ({ status: 'success', data, message: '' }) satisfies MenuLoadState),
          startWith({ status: 'loading', data: null, message: 'Carregando cardapio...' } satisfies MenuLoadState),
          catchError(() => [
            {
              status: 'error',
              data: null,
              message: 'Nao foi possivel carregar o cardapio. Tente novamente.',
            } satisfies MenuLoadState,
          ]),
        ),
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

  protected readonly categories = computed(() => this.menuState().data ?? []);
  protected readonly products = computed(() => this.buildProductViews(this.categories()));

  protected readonly filteredProducts = computed(() => {
    const categoryId = this.selectedCategoryId();

    if (categoryId === null) {
      return this.products();
    }

    return this.products().filter(product => product.categoriaId === categoryId);
  });

  protected readonly hasMenuContent = computed(() => this.products().length > 0);

  constructor() {
    this.refreshCartCount();
    effect(() => {
      this.categories();
      window.setTimeout(() => this.updateCategoriesScrollState());
    });
  }

  @HostListener('document:keydown.escape')
  protected closeProductDetailsOnEscape(): void {
    this.closeProductDetails();
  }

  @HostListener('window:resize')
  protected updateCategoriesScrollOnResize(): void {
    this.updateCategoriesScrollState();
  }

  ngAfterViewInit(): void {
    window.setTimeout(() => this.updateCategoriesScrollState());
  }

  protected loadMenu(): void {
    this.reloadMenuSubject.next();
  }

  protected retryLoadMenu(): void {
    this.loadMenu();
  }

  protected selectCategory(categoryId: number | null): void {
    this.selectedCategoryId.set(categoryId);
  }

  protected selectCategoryFromCard(categoryId: number): void {
    this.selectedCategoryId.set(categoryId);
    this.scrollToMenuSection();
  }

  protected selectCategoryFromFilter(categoryId: number | null): void {
    this.selectedCategoryId.set(categoryId);
  }

  protected scrollCategories(direction: 'left' | 'right'): void {
    const carousel = this.categoryCarouselComponent?.getCarouselElement();

    if (!carousel) {
      return;
    }

    const destination = this.getCategoryScrollDestination(carousel, direction);

    carousel.classList.remove('categories__grid--animating');
    carousel.classList.add('categories__grid--animating');

    this.animateScroll(
      carousel,
      destination,
      'left',
      760,
      () => {
        carousel.classList.remove('categories__grid--animating');
        this.updateCategoriesScrollState();
      },
    );
  }

  protected updateCategoriesScrollState(): void {
    const carousel = this.categoryCarouselComponent?.getCarouselElement();

    if (!carousel) {
      this.canScrollCategoriesLeft.set(false);
      this.canScrollCategoriesRight.set(false);

      return;
    }

    const maxScrollLeft = Math.max(carousel.scrollWidth - carousel.clientWidth, 0);
    const currentScroll = Math.max(carousel.scrollLeft, 0);
    const threshold = 16;

    this.canScrollCategoriesLeft.set(currentScroll > threshold);
    this.canScrollCategoriesRight.set(currentScroll < maxScrollLeft - threshold);
  }

  protected scrollToSection(sectionId: string, event: Event): void {
    event.preventDefault();
    const section = document.getElementById(sectionId);

    if (section) {
      this.animateWindowScroll(section.getBoundingClientRect().top + window.scrollY, 680);
    }
  }

  protected goToCart(event: Event): void {
    event.preventDefault();
    this.router.navigate([this.customerContextService.getCartRoute(this.getCurrentContext())]);
  }

  protected goToOrders(event: Event): void {
    event.preventDefault();

    if (this.getCurrentContext() === 'mesa') {
      this.router.navigate([this.customerContextService.getOrdersRoute('mesa')]);
    }
  }

  private scrollToMenuSection(): void {
    const menuSection = this.menuSection?.nativeElement;

    if (menuSection) {
      this.animateWindowScroll(menuSection.getBoundingClientRect().top + window.scrollY, 680);
    }
  }

  protected openProductDetails(product: ProdutoCardapioView): void {
    this.selectedProduct.set(product);
    this.selectedQuantity.set(1);
    this.selectedObservation.set('');
    this.cartFeedback.set('');
  }

  protected closeProductDetails(): void {
    this.selectedProduct.set(null);
    this.selectedQuantity.set(1);
    this.selectedObservation.set('');
  }

  protected increaseQuantity(): void {
    this.selectedQuantity.update(quantity => quantity + 1);
  }

  protected decreaseQuantity(): void {
    this.selectedQuantity.update(quantity => Math.max(1, quantity - 1));
  }

  protected updateObservation(observation: string): void {
    this.selectedObservation.set(observation);
  }

  protected addSelectedProductToCart(): void {
    const product = this.selectedProduct();

    if (!product) {
      return;
    }

    this.addProductToCart(product, this.selectedQuantity(), this.selectedObservation());
    this.closeProductDetails();
  }

  protected addProductFromCard(product: ProdutoCardapioView, event: Event): void {
    event.stopPropagation();
    this.addProductToCart(product, 1);
  }

  protected trackByCategoryId(_index: number, category: CategoriaCardapioResponse): number {
    return category.categoriaId;
  }

  protected trackByProductId(_index: number, product: ProdutoCardapioView): number {
    return product.id;
  }

  protected getCategoryImage(category: CategoriaCardapioResponse): string {
    const imageMap: Record<string, string> = {
      entradas: 'assets/images/entradas.png',
      lanches: 'assets/images/entradas.png',
      'pratos-prontos': 'assets/images/prontos.png',
      pratos: 'assets/images/prontos.png',
      japonesa: 'assets/images/japonesa.png',
      vegetariana: 'assets/images/vegetariana.png',
    };

    return imageMap[this.normalizeCategoryName(category.categoriaNome)]
      ?? 'assets/images/category-placeholder.svg';
  }

  protected getProductImage(product: ProdutoCardapioView): string {
    if (!product.imagem) {
      return 'assets/images/product-placeholder.svg';
    }

    return product.imagem.startsWith('data:image')
      ? product.imagem
      : `data:image/png;base64,${product.imagem}`;
  }

  protected formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price);
  }

  protected getCurrentContext(): MenuContext {
    return this.customerContextService.getCurrentContext(this.router.url);
  }

  private refreshCartCount(): void {
    this.cartItemsCount.set(this.cartService.getSummary(this.getCurrentContext()).totalItems);
  }

  private createCartItemId(productId: number): string {
    const randomId = typeof crypto !== 'undefined' && 'randomUUID' in crypto
      ? crypto.randomUUID()
      : Math.random().toString(36).slice(2);

    return `${productId}-${Date.now()}-${randomId}`;
  }

  private addProductToCart(
    product: ProdutoCardapioView,
    quantity: number,
    observation = '',
  ): void {
    const cartItem: CartItem = {
      cartItemId: this.createCartItemId(product.id),
      productId: product.id,
      name: product.nome,
      description: product.descricao,
      image: product.imagem,
      unitPrice: product.preco,
      quantity,
      observation,
      categoryId: product.categoriaId,
      categoryName: product.categoriaNome,
    };

    this.cartService.addItem(this.getCurrentContext(), cartItem);
    this.refreshCartCount();
    this.showCartFeedback('Item adicionado ao carrinho.');
  }

  private showCartFeedback(message: string): void {
    if (this.cartFeedbackTimeoutId !== null) {
      window.clearTimeout(this.cartFeedbackTimeoutId);
    }

    this.cartFeedback.set(message);
    this.cartFeedbackTimeoutId = window.setTimeout(() => {
      this.cartFeedback.set('');
      this.cartFeedbackTimeoutId = null;
    }, 2400);
  }

  private buildProductViews(categories: CategoriaCardapioResponse[]): ProdutoCardapioView[] {
    return categories.flatMap(category =>
      category.produtos.map(product => ({
        ...product,
        categoriaId: category.categoriaId,
        categoriaNome: category.categoriaNome,
      })),
    );
  }

  private normalizeCategoryName(categoryName: string): string {
    return categoryName
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-|-$/g, '');
  }

  private animateWindowScroll(targetTop: number, duration: number): void {
    this.animateScroll(window, targetTop, 'top', duration);
  }

  private animateScroll(
    target: Window | HTMLElement,
    destination: number,
    axis: 'top' | 'left',
    duration: number,
    onComplete?: () => void,
  ): void {
    if (this.scrollAnimationFrameId !== null) {
      cancelAnimationFrame(this.scrollAnimationFrameId);
    }

    const start = axis === 'top'
      ? target instanceof Window ? target.scrollY : target.scrollTop
      : target instanceof Window ? target.scrollX : target.scrollLeft;
    const change = destination - start;
    const startTime = performance.now();

    const step = (currentTime: number): void => {
      const elapsed = Math.min((currentTime - startTime) / duration, 1);
      const eased = elapsed < 0.5
        ? 4 * elapsed * elapsed * elapsed
        : 1 - Math.pow(-2 * elapsed + 2, 3) / 2;
      const nextPosition = start + change * eased;

      if (target instanceof Window) {
        target.scrollTo({
          top: axis === 'top' ? nextPosition : target.scrollY,
          left: axis === 'left' ? nextPosition : target.scrollX,
        });
      } else if (axis === 'top') {
        target.scrollTop = nextPosition;
      } else {
        target.scrollLeft = nextPosition;
      }

      if (elapsed < 1) {
        this.scrollAnimationFrameId = requestAnimationFrame(step);
      } else {
        this.scrollAnimationFrameId = null;
        onComplete?.();
      }
    };

    this.scrollAnimationFrameId = requestAnimationFrame(step);
  }

  private getCategoryScrollDestination(
    carousel: HTMLElement,
    direction: 'left' | 'right',
  ): number {
    const maxScrollLeft = carousel.scrollWidth - carousel.clientWidth;
    const firstCard = carousel.querySelector<HTMLElement>('.category-card');
    const firstCardOffset = firstCard?.offsetLeft ?? 0;
    const cardOffsets = Array.from(carousel.querySelectorAll<HTMLElement>('.category-card'))
      .map(card => Math.min(Math.max(card.offsetLeft - firstCardOffset, 0), maxScrollLeft));
    const currentScroll = carousel.scrollLeft;
    const threshold = 24;

    if (direction === 'right') {
      return cardOffsets.find(offset => offset > currentScroll + threshold) ?? maxScrollLeft;
    }

    for (let index = cardOffsets.length - 1; index >= 0; index -= 1) {
      if (cardOffsets[index] < currentScroll - threshold) {
        return cardOffsets[index];
      }
    }

    return 0;
  }
}
