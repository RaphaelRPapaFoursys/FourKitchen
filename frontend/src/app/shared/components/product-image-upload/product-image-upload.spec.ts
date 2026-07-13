import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductImageUpload } from './product-image-upload';

describe('ProductImageUpload', () => {
  let fixture: ComponentFixture<ProductImageUpload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductImageUpload],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductImageUpload);
    fixture.detectChanges();
  });

  it('creates the upload field with the supported formats', () => {
    const input = fixture.nativeElement.querySelector('input[type="file"]') as HTMLInputElement;
    expect(input).toBeTruthy();
    expect(input.accept).toContain('.jpg');
    expect(input.accept).toContain('.jpeg');
    expect(input.accept).toContain('.png');
  });

  it('shows the existing image in edit mode', () => {
    fixture.componentRef.setInput('currentImage', '/9j/example');
    fixture.detectChanges();

    const preview = fixture.nativeElement.querySelector('.image-upload__preview img') as HTMLImageElement;
    expect(preview.src).toContain('data:image/jpeg;base64,/9j/example');
    expect(fixture.nativeElement.textContent).toContain('imagem atual será mantida');
  });
});
