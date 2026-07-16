import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, finalize, forkJoin } from 'rxjs';

import {
  CategoriaGestorResponse,
  ProdutoGestorRequest,
  ProdutoGestorResponse,
} from '../../core/models/catalog.models';
import { AuthService } from '../../core/services/auth';
import { CatalogService } from '../../core/services/catalog.service';
import { RealtimeTopic } from '../../core/models/realtime.models';
import { RealtimeService } from '../../core/services/realtime.service';
import { getBase64ImageSource } from '../../core/utils/product-image.utils';
import { Topbar } from '../../shared/components/header/header';
import { Icon } from '../../shared/components/icon/icon';
import { ProductImageUpload } from '../../shared/components/product-image-upload/product-image-upload';
import { Sidebar } from '../../shared/components/sidebar/sidebar';

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
  private readonly realtimeService = inject(RealtimeService);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly products = signal<ProdutoGestorResponse[]>([]);
  protected readonly categories = signal<CategoriaGestorResponse[]>([]);
  protected readonly searchTerm = signal('');
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly actionProductId = signal<number | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly dialogOpen = signal(false);
  protected readonly editingProduct = signal<ProdutoGestorResponse | null>(null);
  protected readonly pendingImage = signal<string | null>(null);

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

  protected readonly filteredProducts = computed(() => {
    const term = this.normalizeText(this.searchTerm());
    if (!term) {
      return this.products();
    }

    return this.products().filter(product =>
      this.normalizeText(`${product.nome} ${product.descricao ?? ''} ${product.categoria}`).includes(term),
    );
  });
  protected readonly categoryOptions = computed(() => {
    const currentCategoryId = this.editingProduct()?.categoriaId;
    return this.categories().filter(category => category.ativo || category.id === currentCategoryId);
  });
  protected readonly hasActiveCategory = computed(() => this.categories().some(category => category.ativo));

  constructor() {
    this.loadCatalog();
    this.realtimeService
      .watch(RealtimeTopic.gestorCatalogo)
      .pipe(debounceTime(200), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadCatalog());
    this.realtimeService.reconnected$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.loadCatalog());
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
        next: product => {
          this.upsertProduct(product);
          this.dialogOpen.set(false);
          this.editingProduct.set(null);
          this.pendingImage.set(null);
          this.successMessage.set(currentProduct ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.');
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
          this.upsertProduct(updatedProduct);
          this.successMessage.set(
            updatedProduct.disponivel ? 'Produto ativado com sucesso.' : 'Produto desativado com sucesso.',
          );
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected imageSource(image: string | null): string {
    return getBase64ImageSource(image) ?? 'assets/images/product-placeholder.svg';
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
      products: this.catalogService.listProducts(),
      categories: this.catalogService.listCategories(),
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: ({ products, categories }) => {
          this.products.set(this.sortProducts(products));
          this.categories.set(categories);
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  private upsertProduct(product: ProdutoGestorResponse): void {
    this.products.update(products =>
      this.sortProducts([...products.filter(item => item.id !== product.id), product]),
    );
  }

  private sortProducts(products: ProdutoGestorResponse[]): ProdutoGestorResponse[] {
    return products.sort((left, right) => left.nome.localeCompare(right.nome, 'pt-BR'));
  }

  private clearMessages(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
  }

  private normalizeText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
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
