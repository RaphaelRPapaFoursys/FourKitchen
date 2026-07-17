import { ChangeDetectionStrategy, Component, ElementRef, ViewChild, input, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';

import { CategoriaMenuResponse } from '../../../../core/models/menu.models';

@Component({
  selector: 'app-category-carousel',
  templateUrl: './category-carousel.html',
  styleUrl: './category-carousel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class CategoryCarouselComponent {
  @ViewChild('categoriesCarousel') private readonly categoriesCarousel?: ElementRef<HTMLElement>;

  readonly categories = input.required<CategoriaMenuResponse[]>();
  readonly selectedCategoryId = input<number | null>(null);
  readonly canScrollLeft = input(false);
  readonly canScrollRight = input(false);
  readonly categoryImage = input.required<(category: CategoriaMenuResponse) => string>();
  readonly trackCategory = input.required<(index: number, category: CategoriaMenuResponse) => number>();

  readonly categorySelected = output<number>();
  readonly scrollRequested = output<'left' | 'right'>();
  readonly scrolled = output<void>();

  protected isDragging = false;

  private startX = 0;
  private startScrollLeft = 0;
  private didDrag = false;
  private isMouseDown = false;
  private readonly dragThreshold = 8;

  protected onCategoryClick(event: MouseEvent, categoryId: number): void {
    if (this.didDrag) {
      event.preventDefault();
      event.stopPropagation();
      this.didDrag = false;
      return;
    }

    this.categorySelected.emit(categoryId);
  }

  protected requestScroll(direction: 'left' | 'right'): void {
    this.scrollRequested.emit(direction);
  }

  protected notifyScrolled(): void {
    this.scrolled.emit();
  }

  protected useCategoryFallback(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.onerror = null;
    image.src = 'assets/images/category-placeholder.svg';
  }

  protected onPointerDown(event: PointerEvent): void {
    if (event.pointerType !== 'mouse' || event.button !== 0) {
      return;
    }

    const carousel = event.currentTarget as HTMLElement;
    this.startX = event.clientX;
    this.startScrollLeft = carousel.scrollLeft;
    this.didDrag = false;
    this.isMouseDown = true;
    this.isDragging = false;
  }

  protected onPointerMove(event: PointerEvent): void {
    if (event.pointerType !== 'mouse' || !this.isMouseDown || !(event.buttons & 1)) {
      return;
    }

    const carousel = event.currentTarget as HTMLElement;
    const distance = event.clientX - this.startX;

    if (Math.abs(distance) < this.dragThreshold) {
      return;
    }

    this.didDrag = true;
    this.isDragging = true;
    event.preventDefault();
    carousel.scrollLeft = this.startScrollLeft - distance;
  }

  protected onPointerUp(event: PointerEvent): void {
    if (event.pointerType !== 'mouse') {
      return;
    }

    this.isMouseDown = false;
    this.isDragging = false;
  }

  protected onPointerCancel(event: PointerEvent): void {
    if (event.pointerType !== 'mouse') {
      return;
    }

    this.isMouseDown = false;
    this.isDragging = false;
  }

  protected onPointerLeave(event: PointerEvent): void {
    if (event.pointerType === 'mouse') {
      this.isDragging = false;
    }
  }

  getCarouselElement(): HTMLElement | undefined {
    return this.categoriesCarousel?.nativeElement;
  }
}
