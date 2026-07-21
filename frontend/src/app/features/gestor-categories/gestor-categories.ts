import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, finalize } from 'rxjs';

import {
  CategoriaGestorRequest,
  CategoriaGestorResponse,
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
  selector: 'app-gestor-categories',
  imports: [ReactiveFormsModule, Sidebar, Topbar, Icon, ProductImageUpload],
  templateUrl: './gestor-categories.html',
  styleUrls: ['../gestor-products/gestor-products.scss', './gestor-categories.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GestorCategories {
  private readonly authService = inject(AuthService);
  private readonly catalogService = inject(CatalogService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });
  protected readonly categories = signal<CategoriaGestorResponse[]>([]);
  protected readonly searchTerm = signal('');
  protected readonly selectedStatus = signal<boolean | null>(null);
  protected readonly currentPage = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly pageSize = 10;
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly actionCategoryId = signal<number | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly successMessage = signal('');
  protected readonly dialogOpen = signal(false);
  protected readonly editingCategory = signal<CategoriaGestorResponse | null>(null);
  protected readonly pendingImage = signal<string | null>(null);
  private readonly searchChanges = new Subject<string>();

  protected readonly categoryForm = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(80)],
    }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(255)],
    }),
  });

  constructor() {
    this.searchChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.currentPage.set(0);
        this.loadCategories();
      });
    this.loadCategories();
  }

  protected onSearchChange(value: string): void {
    this.searchTerm.set(value);
    this.searchChanges.next(value.trim());
  }

  protected previousPage(): void {
    if (this.currentPage() > 0 && !this.loading()) {
      this.currentPage.update(page => page - 1);
      this.loadCategories();
    }
  }

  protected nextPage(): void {
    if (this.currentPage() + 1 < this.totalPages() && !this.loading()) {
      this.currentPage.update(page => page + 1);
      this.loadCategories();
    }
  }

  protected selectStatus(status: boolean | null): void {
    if (this.selectedStatus() === status || this.loading()) {
      return;
    }

    this.selectedStatus.set(status);
    this.currentPage.set(0);
    this.loadCategories();
  }

  protected openCreateDialog(): void {
    this.clearMessages();
    this.editingCategory.set(null);
    this.pendingImage.set(null);
    this.categoryForm.reset({ nome: '', descricao: '' });
    this.dialogOpen.set(true);
  }

  protected openEditDialog(category: CategoriaGestorResponse): void {
    this.clearMessages();
    this.editingCategory.set(category);
    this.pendingImage.set(null);
    this.categoryForm.reset({
      nome: category.nome,
      descricao: category.descricao ?? '',
    });
    this.dialogOpen.set(true);
  }

  protected closeDialog(): void {
    if (this.saving()) {
      return;
    }

    this.dialogOpen.set(false);
    this.editingCategory.set(null);
    this.pendingImage.set(null);
  }

  protected onImageChanged(image: string | null): void {
    this.pendingImage.set(image);
  }

  protected saveCategory(): void {
    this.clearMessages();
    this.categoryForm.markAllAsTouched();

    if (this.categoryForm.invalid || this.saving()) {
      return;
    }

    const formValue = this.categoryForm.getRawValue();
    const request: CategoriaGestorRequest = {
      nome: formValue.nome.trim(),
      descricao: formValue.descricao.trim() || null,
      imagem: this.pendingImage(),
    };
    const currentCategory = this.editingCategory();
    const operation = currentCategory
      ? this.catalogService.updateCategory(currentCategory.id, request)
      : this.catalogService.createCategory(request);

    this.saving.set(true);
    operation
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.saving.set(false)),
      )
      .subscribe({
        next: () => {
          this.dialogOpen.set(false);
          this.editingCategory.set(null);
          this.pendingImage.set(null);
          this.successMessage.set(
            currentCategory ? 'Categoria atualizada com sucesso.' : 'Categoria cadastrada com sucesso.',
          );
          this.loadCategories();
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
  }

  protected toggleStatus(category: CategoriaGestorResponse): void {
    if (this.actionCategoryId() !== null) {
      return;
    }

    if (
      category.ativo
      && !window.confirm(
        `Deseja desativar a categoria ${category.nome}? Os produtos vinculados deixarão de aparecer no cardápio.`,
      )
    ) {
      return;
    }

    this.clearMessages();
    this.actionCategoryId.set(category.id);
    const operation = category.ativo
      ? this.catalogService.deactivateCategory(category.id)
      : this.catalogService.activateCategory(category.id);

    operation
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.actionCategoryId.set(null)),
      )
      .subscribe({
        next: updatedCategory => {
          this.successMessage.set(
            updatedCategory.ativo
              ? 'Categoria ativada com sucesso.'
              : 'Categoria desativada com sucesso.',
          );
          this.loadCategories();
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

  protected trackCategory(_index: number, category: CategoriaGestorResponse): number {
    return category.id;
  }

  protected initials(name: string | null | undefined): string {
    return name?.trim().charAt(0).toUpperCase() || '?';
  }

  protected logout(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  private loadCategories(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.catalogService
      .listCategories(this.currentPage(), this.pageSize, this.searchTerm(), this.selectedStatus())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loading.set(false)),
      )
      .subscribe({
        next: page => {
          this.categories.set(page.content);
          this.currentPage.set(page.page);
          this.totalElements.set(page.totalElements);
          this.totalPages.set(page.totalPages);
        },
        error: error => this.errorMessage.set(this.getErrorMessage(error)),
      });
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

      if (error.status === 400) return 'Confira os dados e as regras da imagem.';
      if (error.status === 401) return 'Sua sessão expirou. Entre novamente.';
      if (error.status === 403) return 'Você não tem permissão para gerenciar categorias.';
      if (error.status === 404) return 'Categoria não encontrada.';
      if (error.status === 409) return 'Já existe uma categoria com este nome.';
      if (error.status === 502) return 'O serviço de produtos está indisponível. Tente novamente.';
    }

    return 'Não foi possível concluir a operação. Tente novamente.';
  }
}
