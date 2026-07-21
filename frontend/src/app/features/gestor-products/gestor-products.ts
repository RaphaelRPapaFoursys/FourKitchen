import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize, forkJoin } from 'rxjs';

import {
  CategoriaOpcaoResponse,
  ProdutoGestorRequest,
  ProdutoGestorResponse,
} from '../../core/models/catalog.models';
import { AuthService } from '../../core/services/auth';
import { CatalogService } from '../../core/services/catalog.service';
import { getBase64ImageSource } from '../../core/utils/product-image.utils';
import { Topbar } from '../../shared/components/header/header';
import { Icon } from '../../shared/components/icon/icon';
import { ProductImageUpload } from '../../shared/components/product-image-upload/product-image-upload';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-gestor-products',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink, Sidebar, Topbar, Icon, ProductImageUpload],
  templateUrl: './gestor-products.html',
  styleUrl: './gestor-products.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GestorProducts {
  private readonly authService = inject(AuthService);
  private readonly catalogService = inject(CatalogService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly products = signal<ProdutoGestorResponse[]>([]);
  protected readonly categories = signal<CategoriaOpcaoResponse[]>([]);
  protected readonly searchTerm = signal('');
  protected readonly currentPage = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly pageSize = 10;
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly actionProductId = signal<number | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly dialogOpen = signal(false);
  protected readonly editingProduct = signal<ProdutoGestorResponse | null>(null);
  protected readonly pendingImage = signal<string | null>(null);
  private readonly searchChanges = new Subject<string>();

  protected readonly productForm = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(150)],
    }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(255)],
    }),
    preco: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
    categoriaId: new FormControl<number | null>(null, {
      validators: [Validators.required],
    }),
  });

  protected readonly categoryOptions = computed(() => {
    const currentCategoryId = this.editingProduct()?.categoriaId;
    return this.categories().filter(category => category.ativo || category.id === currentCategoryId);
  });
  protected readonly hasActiveCategory = computed(() => this.categories().some(category => category.ativo));

  constructor() {
    this.searchChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadProducts();
      });
    this.loadCatalog();
  }

  protected onSearchChange(value: string): void {
    this.searchTerm.set(value);
    this.searchChanges.next(value.trim());
  }

  protected previousPage(): void {
    if (this.currentPage() > 0 && !this.loading()) {
      this.currentPage.update(page => page - 1);
      this.loadProducts();
    }
  }

  protected nextPage(): void {
    if (this.currentPage() + 1 < this.totalPages() && !this.loading()) {
      this.currentPage.update(page => page + 1);
      this.loadProducts();
    }
  }

  protected openCreateDialog(): void {
    this.clearMessages();

    if (!this.hasActiveCategory()) {
      this.errorMessage.set('Cadastre ou ative uma categoria antes de criar um produto.');
      return;
    }

    this.editingProduct.set(null);
    this.pendingImage.set(null);
    this.productForm.reset({
      nome: '',
      descricao: '',
      preco: null,
      categoriaId: null,
    });
    this.dialogOpen.set(true);
  }

  protected openEditDialog(product: ProdutoGestorResponse): void {
    this.clearMessages();
    this.editingProduct.set(product);
    this.pendingImage.set(null);
    this.productForm.reset({
      nome: product.nome,
      descricao: product.descricao ?? '',
      preco: product.preco,
      categoriaId: product.categoriaId,
    });
    this.dialogOpen.set(true);
  }

  protected closeDialog(): void {
    if (this.saving()) {
      return;
    }

    this.dialogOpen.set(false);
    this.editingProduct.set(null);
    this.pendingImage.set(null);
  }

  protected onImageChanged(image: string | null): void {
    this.pendingImage.set(image);
  }

  protected saveProduct(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.productForm.markAllAsTouched();

    if (this.productForm.invalid || this.saving()) {
      return;
    }

    const formValue = this.productForm.getRawValue();
    if (formValue.preco === null || formValue.categoriaId === null) {
      return;
    }

    const request: ProdutoGestorRequest = {
      nome: formValue.nome.trim(),
      descricao: formValue.descricao.trim() || null,
      imagem: this.pendingImage(),
      preco: formValue.preco,
      categoriaId: formValue.categoriaId,
    };
    const currentProduct = this.editingProduct();
    const operation = currentProduct
      ? this.catalogService.updateProduct(currentProduct.id, request)
      : this.catalogService.createProduct(request);

    this.saving.set(true);
    operation
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: () => {
          this.dialogOpen.set(false);
          this.editingProduct.set(null);
          this.pendingImage.set(null);
          this.successMessage.set(currentProduct ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.');
          this.loadProducts();
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected toggleAvailability(product: ProdutoGestorResponse): void {
    if (this.actionProductId() !== null) {
      return;
    }

    this.clearMessages();
    this.actionProductId.set(product.id);
    const operation = product.disponivel
      ? this.catalogService.deactivateProduct(product.id)
      : this.catalogService.activateProduct(product.id);

    operation
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.actionProductId.set(null)),
      )
      .subscribe({
        next: updatedProduct => {
          this.successMessage.set(
            updatedProduct.disponivel ? 'Produto ativado com sucesso.' : 'Produto desativado com sucesso.',
          );
          this.loadProducts();
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected imageSource(imageUrl: string | null): string {
    return getBase64ImageSource(imageUrl, environment.apiUrl) ?? 'assets/images/product-placeholder.svg';
  }

  protected currentImageSource(imageUrl: string | null | undefined): string | null {
    return getBase64ImageSource(imageUrl, environment.apiUrl);
  }

  protected trackProduct(_index: number, product: ProdutoGestorResponse): number {
    return product.id;
  }

  protected initials(name: string | null | undefined): string {
    return name?.trim().charAt(0).toUpperCase() || '?';
  }

  protected logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  private loadCatalog(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    forkJoin({
      products: this.catalogService.listProducts(this.currentPage(), this.pageSize, this.searchTerm()),
      categories: this.catalogService.listCategoryOptions(),
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: ({ products, categories }) => {
          this.applyProductPage(products);
          this.categories.set(categories);
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  private loadProducts(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.catalogService
      .listProducts(this.currentPage(), this.pageSize, this.searchTerm())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: page => this.applyProductPage(page),
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  private applyProductPage(page: { content: ProdutoGestorResponse[]; page: number; totalElements: number; totalPages: number }): void {
    this.products.set(page.content);
    this.currentPage.set(page.page);
    this.totalElements.set(page.totalElements);
    this.totalPages.set(page.totalPages);
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { msgError?: unknown } | null;
      if (typeof body?.msgError === 'string' && body.msgError.trim()) {
        return body.msgError;
      }

      if (error.status === 401) return 'Sua sessão expirou. Entre novamente.';
      if (error.status === 403) return 'Você não tem permissão para gerenciar produtos.';
      if (error.status === 404) return 'Produto ou categoria não encontrado.';
      if (error.status === 502) return 'O serviço de produtos está indisponível. Tente novamente.';
    }

    return 'Não foi possível concluir a operação. Tente novamente.';
  }
}
