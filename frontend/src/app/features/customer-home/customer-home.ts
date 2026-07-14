import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, ChangeDetectionStrategy, Component, DestroyRef, ElementRef, HostListener, ViewChild, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  CardapioPaginadoResponse,
  CategoriaCardapioResponse,
  CategoriaMenuResponse,
  ProdutoCardapioView,
} from '../../core/models/menu.models';
import { CartItem, CustomerContext } from '../../core/models/cart.models';
import { MesaAtendimentoAtualResponse } from '../../core/models/order.models';
import { CartService } from '../../core/services/cart.service';
import { CustomerContextService } from '../../core/services/customer-context.service';
import { MenuService } from '../../core/services/menu.service';
import { OrderService } from '../../core/services/order.service';
import { getBase64ImageSource } from '../../core/utils/product-image.utils';
import { CategoryCarouselComponent } from './components/category-carousel/category-carousel';
import { CustomerFooterComponent } from './components/customer-footer/customer-footer';
import { CustomerHeroComponent } from './components/customer-hero/customer-hero';
import { CustomerHomeHeaderComponent } from './components/customer-home-header/customer-home-header';
import { MenuFilterBarComponent } from './components/menu-filter-bar/menu-filter-bar';
import { ProductDetailsModalComponent } from './components/product-details-modal/product-details-modal';
import { ProductGridComponent } from './components/product-grid/product-grid';
import { MesaHeaderComponent } from '../../shared/components/mesa-header/mesa-header';

type MenuLoadState =
  | { status: 'loading'; data: null; message: string }
  | { status: 'error'; data: null; message: string }
  | { status: 'success'; data: CategoriaCardapioResponse[]; message: string };

type CategoryLoadState =
  | { status: 'loading'; data: null; message: string }
  | { status: 'error'; data: null; message: string }
  | { status: 'success'; data: CategoriaMenuResponse[]; message: string };

const MENU_PAGE_SIZE = 12;
const MENU_SCROLL_THRESHOLD_PX = 900;

@Component({
  selector: 'app-customer-home',
  imports: [
    CommonModule,
    FormsModule,
    CustomerHomeHeaderComponent,
    MesaHeaderComponent,
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
  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private scrollAnimationFrameId: number | null = null;
  private cartFeedbackTimeoutId: number | null = null;
  private nextMenuPage = 0;
  private reachedLastMenuPage = false;

  protected readonly selectedCategoryId = signal<number | null>(null);
  protected readonly selectedProduct = signal<ProdutoCardapioView | null>(null);
  protected readonly selectedQuantity = signal(1);
  protected readonly selectedObservation = signal('');
  protected readonly cartItemsCount = signal(0);
  protected readonly cartFeedback = signal('');
  protected readonly atendimentoAtual = signal<MesaAtendimentoAtualResponse | null>(null);
  protected readonly carregandoAtendimento = signal(false);
  protected readonly erroAtendimento = signal('');
  protected readonly loadingMoreMenu = signal(false);
  protected readonly canScrollCategoriesLeft = signal(false);
  protected readonly canScrollCategoriesRight = signal(false);
  protected readonly cartRoute = computed(() =>
    this.customerContextService.getCartRoute(this.getCurrentContext()),
  );
  protected readonly ordersRoute = computed(() =>
    this.customerContextService.getOrdersRoute(this.getCurrentContext()),
  );
  protected readonly showOrdersLink = computed(() => this.getCurrentContext() === 'mesa');
  protected readonly showMesaActions = computed(() => this.getCurrentContext() === 'mesa');
  protected readonly podeAdicionarAoCarrinho = computed(() =>
    this.getCurrentContext() !== 'mesa'
      || (!this.carregandoAtendimento() && this.isAtendimentoAtivo(this.atendimentoAtual())),
  );
  protected readonly atendimentoFeedback = computed(() => {
    if (!this.showMesaActions() || this.carregandoAtendimento()) {
      return '';
    }

    return this.erroAtendimento() || (
      this.atendimentoAtual() === null
        ? 'Esta mesa ainda não possui um atendimento iniciado.'
        : ''
    );
  });

  protected readonly menuState = signal<MenuLoadState>({
    status: 'loading',
    data: null,
    message: 'Carregando cardapio...',
  });

  protected readonly categoryState = signal<CategoryLoadState>({
    status: 'loading',
    data: null,
    message: 'Carregando categorias...',
  });

  protected readonly categories = computed(() => this.categoryState().data ?? []);
  protected readonly menuCategories = computed(() => this.menuState().data ?? []);
  protected readonly products = computed(() => this.buildProductViews(this.menuCategories()));

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
    if (this.getCurrentContext() === 'mesa') {
      this.carregarAtendimentoAtual();
    }
    void this.loadCategories(false);
    void this.loadFirstMenuPage(false);

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

  @HostListener('window:scroll')
  protected loadMoreMenuOnScroll(): void {
    const distanceToBottom = document.documentElement.scrollHeight - (window.scrollY + window.innerHeight);

    if (distanceToBottom <= MENU_SCROLL_THRESHOLD_PX) {
      void this.loadNextMenuPage();
    }
  }

  ngAfterViewInit(): void {
    window.setTimeout(() => this.updateCategoriesScrollState());
  }

  protected loadMenu(): void {
    void this.loadFirstMenuPage(true);
    void this.loadCategories(true);
  }

  protected retryLoadMenu(): void {
    this.loadMenu();
  }

  private async loadCategories(forceRefresh: boolean): Promise<void> {
    this.categoryState.set({
      status: 'loading',
      data: null,
      message: 'Carregando categorias...',
    });

    try {
      const request = forceRefresh
        ? this.menuService.refreshMenuCategories(this.getCurrentContext())
        : this.menuService.getMenuCategories(this.getCurrentContext());
      const categories = await firstValueFrom(request);

      this.categoryState.set({ status: 'success', data: categories, message: '' });
    } catch {
      this.categoryState.set({
        status: 'error',
        data: null,
        message: 'Nao foi possivel carregar as categorias.',
      });
    }
  }

  private async loadFirstMenuPage(forceRefresh: boolean): Promise<void> {
    this.nextMenuPage = 0;
    this.reachedLastMenuPage = false;
    this.loadingMoreMenu.set(false);
    this.menuState.set({
      status: 'loading',
      data: null,
      message: 'Carregando cardapio...',
    });

    try {
      const page = await this.fetchMenuPage(0, forceRefresh, this.selectedCategoryId());
      this.applyMenuPage(page, false);
    } catch {
      this.menuState.set({
        status: 'error',
        data: null,
        message: 'Nao foi possivel carregar o cardapio. Tente novamente.',
      });
    }
  }

  private async loadNextMenuPage(): Promise<void> {
    if (
      this.menuState().status !== 'success' ||
      this.loadingMoreMenu() ||
      this.reachedLastMenuPage
    ) {
      return;
    }

    this.loadingMoreMenu.set(true);

    try {
      const page = await this.fetchMenuPage(this.nextMenuPage, false, this.selectedCategoryId());
      this.applyMenuPage(page, true);
    } catch {
      // Mantem o cardapio ja carregado; o proximo scroll tenta novamente.
    } finally {
      this.loadingMoreMenu.set(false);
    }
  }

  private fetchMenuPage(
    page: number,
    forceRefresh: boolean,
    categoriaId: number | null,
  ): Promise<CardapioPaginadoResponse> {
    const request = forceRefresh
      ? this.menuService.refreshMenuPage(this.getCurrentContext(), page, MENU_PAGE_SIZE, categoriaId)
      : this.menuService.getMenuPage(this.getCurrentContext(), page, MENU_PAGE_SIZE, categoriaId);

    return firstValueFrom(request);
  }

  private applyMenuPage(page: CardapioPaginadoResponse, append: boolean): void {
    const currentData = append && this.menuState().status === 'success'
      ? this.menuState().data
      : [];
    const data = append ? this.mergeMenuCategories(currentData ?? [], page.content) : page.content;

    this.menuState.set({
      status: 'success',
      data,
      message: '',
    });
    this.nextMenuPage = page.page + 1;
    this.reachedLastMenuPage = page.last || this.nextMenuPage >= page.totalPages;

    window.setTimeout(() => this.loadMoreMenuOnScroll());
  }

  private mergeMenuCategories(
    currentCategories: CategoriaCardapioResponse[],
    nextCategories: CategoriaCardapioResponse[],
  ): CategoriaCardapioResponse[] {
    const byCategory = new Map<number, CategoriaCardapioResponse>();

    for (const category of currentCategories) {
      byCategory.set(category.categoriaId, {
        ...category,
        produtos: [...category.produtos],
      });
    }

    for (const category of nextCategories) {
      const existing = byCategory.get(category.categoriaId);
      if (!existing) {
        byCategory.set(category.categoriaId, {
          ...category,
          produtos: [...category.produtos],
        });
        continue;
      }

      const productIds = new Set(existing.produtos.map(product => product.id));
      existing.produtos.push(...category.produtos.filter(product => !productIds.has(product.id)));
    }

    return Array.from(byCategory.values());
  }

  protected selectCategory(categoryId: number | null): void {
    if (this.selectedCategoryId() === categoryId) {
      return;
    }

    this.selectedCategoryId.set(categoryId);
    void this.loadFirstMenuPage(false);
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

    if (this.addProductToCart(product, this.selectedQuantity(), this.selectedObservation())) {
      this.closeProductDetails();
    }
  }

  protected addProductFromCard(product: ProdutoCardapioView, event: Event): void {
    event.stopPropagation();
    this.addProductToCart(product, 1);
  }

  protected trackByCategoryId(_index: number, category: CategoriaMenuResponse): number {
    return category.categoriaId;
  }

  protected trackByProductId(_index: number, product: ProdutoCardapioView): number {
    return product.id;
  }

  protected getCategoryImage(category: CategoriaMenuResponse): string {
    if (category.imagemUrl) {
      return this.resolveApiUrl(category.imagemUrl);
    }

    const image = category.imagem?.trim();

    if (image && this.isValidCategoryImage(image)) {
      return getBase64ImageSource(image) ?? 'assets/images/category-placeholder.svg';
    }

    const normalizedName = this.normalizeCategoryName(category.categoriaNome);

    if (normalizedName.includes('japones')) {
      return 'assets/images/japonesa.png';
    }

    if (normalizedName.includes('veget') || normalizedName.includes('vegan')) {
      return 'assets/images/vegetariana.png';
    }

    if (normalizedName.includes('prato') || normalizedName.includes('pronto')) {
      return 'assets/images/prontos.png';
    }

    if (normalizedName.includes('entrada') || normalizedName.includes('lanche')) {
      return 'assets/images/entradas.png';
    }

    return 'assets/images/category-placeholder.svg';
  }

  protected getProductImage(product: ProdutoCardapioView): string {
    if (product.imagemUrl) {
      return this.resolveApiUrl(product.imagemUrl);
    }

    if (!product.imagem) {
      return 'assets/images/product-placeholder.svg';
    }

    return getBase64ImageSource(product.imagem) ?? 'assets/images/product-placeholder.svg';
  }

  protected formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price);
  }

  protected getCurrentContext(): CustomerContext {
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
  ): boolean {
    if (!this.podeAdicionarAoCarrinho()) {
      if (!this.carregandoAtendimento() && this.atendimentoFeedback()) {
        this.showCartFeedback(this.atendimentoFeedback());
      }
      return false;
    }

    const cartItem: CartItem = {
      cartItemId: this.createCartItemId(product.id),
      productId: product.id,
      name: product.nome,
      description: product.descricao,
      image: product.imagemUrl ? this.resolveApiUrl(product.imagemUrl) : product.imagem,
      unitPrice: product.preco,
      quantity,
      observation,
      categoryId: product.categoriaId,
      categoryName: product.categoriaNome,
    };

    this.cartService.addItem(this.getCurrentContext(), cartItem);
    this.refreshCartCount();
    this.showCartFeedback('Item adicionado ao carrinho.');
    return true;
  }

  protected carregarAtendimentoAtual(): void {
    if (this.getCurrentContext() !== 'mesa') {
      return;
    }

    this.carregandoAtendimento.set(true);
    this.atendimentoAtual.set(null);
    this.erroAtendimento.set('');
    this.orderService
      .getCurrentTableAttendance()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.carregandoAtendimento.set(false)),
      )
      .subscribe({
        next: atendimento => {
          if (this.isAtendimentoAtivo(atendimento)) {
            this.atendimentoAtual.set(atendimento);
            return;
          }

          this.atendimentoAtual.set(null);
        },
        error: error => {
          this.atendimentoAtual.set(null);
          if (!(error instanceof HttpErrorResponse && error.status === 400)) {
            this.erroAtendimento.set(
              this.getApiErrorMessage(error, 'Não foi possível verificar o atendimento atual.'),
            );
          }
        },
      });
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

  private isAtendimentoAtivo(atendimento: MesaAtendimentoAtualResponse | null): atendimento is MesaAtendimentoAtualResponse {
    return atendimento !== null
      && atendimento.status.toUpperCase() === 'OCUPADA'
      && atendimento.idAtendimento > 0
      && atendimento.codigoAtendimento > 0;
  }

  private getApiErrorMessage(error: unknown, fallback: string): string {
    if (
      error instanceof HttpErrorResponse
      && error.error
      && typeof error.error === 'object'
      && 'msgError' in error.error
      && typeof error.error.msgError === 'string'
      && error.error.msgError.trim()
    ) {
      return error.error.msgError;
    }

    return fallback;
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
      .replace(/[^a-z0-9]+/g, '-');
  }

  private isValidCategoryImage(image: string): boolean {
    const base64 = /^data:/i.test(image)
      ? image.match(/^data:image\/[a-z0-9.+-]+;base64,(.+)$/i)?.[1]
      : image;

    if (!base64) {
      return false;
    }

    const normalizedBase64 = base64.replace(/\s/g, '');

    return normalizedBase64.length % 4 !== 1
      && /^[a-z0-9+/]*={0,2}$/i.test(normalizedBase64);
  }

  private resolveApiUrl(url: string): string {
    if (/^https?:\/\//i.test(url) || url.startsWith('data:') || url.startsWith('assets/')) {
      return url;
    }

    return `${environment.apiUrl}${url.startsWith('/') ? url : `/${url}`}`;
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
