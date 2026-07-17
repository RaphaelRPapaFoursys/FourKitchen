import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import {
  getBase64ImageSource,
  validateProductImageDimensions,
  validateProductImageFile,
} from '../../../core/utils/product-image.utils';

@Component({
  selector: 'fk-product-image-upload',
  templateUrl: './product-image-upload.html',
  styleUrl: './product-image-upload.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [TranslatePipe],
})
export class ProductImageUpload {
  readonly currentImage = input<string | null>(null);
  readonly disabled = input(false);
  readonly fieldLabel = input('Imagem do produto');
  readonly previewAlt = input('Pré-visualização da imagem do produto');
  readonly imageChanged = output<string | null>();

  private readonly fileInput = viewChild<ElementRef<HTMLInputElement>>('fileInput');
  private selectionSequence = 0;
  private readonly translate = inject(TranslateService);

  protected readonly selectedImage = signal<string | null>(null);
  protected readonly selectedFileName = signal('');
  protected readonly errorMessage = signal('');
  protected readonly reading = signal(false);
  protected readonly dragging = signal(false);
  protected readonly previewSource = computed(() =>
    this.selectedImage() ?? getBase64ImageSource(this.currentImage()),
  );
  protected readonly hasNewSelection = computed(() => this.selectedImage() !== null);

  protected onFileSelected(event: Event): void {
    const inputElement = event.target as HTMLInputElement;
    const file = inputElement.files?.item(0);

    if (file) {
      void this.processFile(file);
    }
  }

  protected onDragOver(event: DragEvent): void {
    event.preventDefault();
    if (!this.disabled()) {
      this.dragging.set(true);
    }
  }

  protected onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);
  }

  protected onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);

    if (this.disabled()) {
      return;
    }

    const file = event.dataTransfer?.files.item(0);
    if (file) {
      void this.processFile(file);
    }
  }

  protected cancelSelection(): void {
    this.selectionSequence += 1;
    this.selectedImage.set(null);
    this.selectedFileName.set('');
    this.errorMessage.set('');
    this.reading.set(false);
    this.resetNativeInput();
    this.imageChanged.emit(null);
  }

  private async processFile(file: File): Promise<void> {
    const sequence = ++this.selectionSequence;
    this.errorMessage.set('');

    const fileError = validateProductImageFile(file);
    if (fileError) {
      this.rejectSelection(fileError);
      return;
    }

    this.reading.set(true);

    try {
      const dataUrl = await this.readAsDataUrl(file);
      const dimensions = await this.readImageDimensions(dataUrl);

      if (sequence !== this.selectionSequence) {
        return;
      }

      const dimensionError = validateProductImageDimensions(dimensions.width, dimensions.height);
      if (dimensionError) {
        this.rejectSelection(dimensionError);
        return;
      }

      this.selectedImage.set(dataUrl);
      this.selectedFileName.set(file.name);
      this.imageChanged.emit(dataUrl);
    } catch {
      if (sequence === this.selectionSequence) {
        this.rejectSelection(this.translate.instant('ERROR.IMAGE_READ'));
      }
    } finally {
      if (sequence === this.selectionSequence) {
        this.reading.set(false);
      }
    }
  }

  private rejectSelection(message: string): void {
    this.selectedImage.set(null);
    this.selectedFileName.set('');
    this.errorMessage.set(message);
    this.reading.set(false);
    this.resetNativeInput();
    this.imageChanged.emit(null);
  }

  private readAsDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          resolve(reader.result);
          return;
        }

        reject(new Error('Resultado de leitura inválido.'));
      };
      reader.onerror = () => reject(reader.error ?? new Error('Falha ao ler o arquivo.'));
      reader.readAsDataURL(file);
    });
  }

  private readImageDimensions(source: string): Promise<{ width: number; height: number }> {
    return new Promise((resolve, reject) => {
      const image = new Image();
      image.onload = () => resolve({ width: image.naturalWidth, height: image.naturalHeight });
      image.onerror = () => reject(new Error('Imagem inválida.'));
      image.src = source;
    });
  }

  private resetNativeInput(): void {
    const input = this.fileInput()?.nativeElement;
    if (input) {
      input.value = '';
    }
  }
}
