import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { CategoriaCardapioResponse } from '../../../../core/models/menu.models';

@Component({
  selector: 'app-menu-filter-bar',
  templateUrl: './menu-filter-bar.html',
  styleUrl: './menu-filter-bar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MenuFilterBarComponent {
  readonly categories = input.required<CategoriaCardapioResponse[]>();
  readonly selectedCategoryId = input<number | null>(null);
  readonly trackCategory = input.required<(index: number, category: CategoriaCardapioResponse) => number>();

  readonly categorySelected = output<number | null>();

  protected selectCategory(categoryId: number | null): void {
    this.categorySelected.emit(categoryId);
  }
}
