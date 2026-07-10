import { ChangeDetectionStrategy, Component, ElementRef, ViewChild, input, output } from '@angular/core';

import { CategoriaCardapioResponse } from '../../../../core/models/menu.models';

@Component({
  selector: 'app-category-carousel',
  templateUrl: './category-carousel.html',
  styleUrl: './category-carousel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryCarouselComponent {
  @ViewChild('categoriesCarousel') private readonly categoriesCarousel?: ElementRef<HTMLElement>;

  readonly categories = input.required<CategoriaCardapioResponse[]>();
  readonly selectedCategoryId = input<number | null>(null);
  readonly canScrollLeft = input(false);
  readonly canScrollRight = input(false);
  readonly categoryImage = input.required<(category: CategoriaCardapioResponse) => string>();
  readonly trackCategory = input.required<(index: number, category: CategoriaCardapioResponse) => number>();

  readonly categorySelected = output<number>();
  readonly scrollRequested = output<'left' | 'right'>();
  readonly scrolled = output<void>();

  protected selectCategory(categoryId: number): void {
    this.categorySelected.emit(categoryId);
  }

  protected requestScroll(direction: 'left' | 'right'): void {
    this.scrollRequested.emit(direction);
  }

  protected notifyScrolled(): void {
    this.scrolled.emit();
  }

  getCarouselElement(): HTMLElement | undefined {
    return this.categoriesCarousel?.nativeElement;
  }
}
