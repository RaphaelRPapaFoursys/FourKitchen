import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { CategoriaMenuResponse } from '../../../../core/models/menu.models';

@Component({
  selector: 'app-menu-filter-bar',
  templateUrl: './menu-filter-bar.html',
  styleUrl: './menu-filter-bar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class MenuFilterBarComponent {
  readonly categories = input.required<CategoriaMenuResponse[]>();
  readonly selectedCategoryId = input<number | null>(null);
  readonly trackCategory = input.required<(index: number, category: CategoriaMenuResponse) => number>();

  readonly categorySelected = output<number | null>();

  protected selectCategory(categoryId: number | null): void {
    this.categorySelected.emit(categoryId);
  }
}
