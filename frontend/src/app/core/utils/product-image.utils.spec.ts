import { describe, expect, it } from 'vitest';

import {
  PRODUCT_IMAGE_MAX_SIZE_BYTES,
  getBase64ImageSource,
  validateProductImageDimensions,
  validateProductImageFile,
} from './product-image.utils';

describe('product image utilities', () => {
  it('accepts JPG, JPEG and PNG files', () => {
    expect(validateProductImageFile(new File(['image'], 'produto.jpg', { type: 'image/jpeg' }))).toBeNull();
    expect(validateProductImageFile(new File(['image'], 'produto.jpeg', { type: 'image/jpeg' }))).toBeNull();
    expect(validateProductImageFile(new File(['image'], 'produto.png', { type: 'image/png' }))).toBeNull();
  });

  it('rejects an unsupported format', () => {
    const error = validateProductImageFile(new File(['image'], 'produto.webp', { type: 'image/webp' }));
    expect(error).toContain('Formato inválido');
  });

  it('rejects a file larger than 1 MB', () => {
    const content = new Uint8Array(PRODUCT_IMAGE_MAX_SIZE_BYTES + 1);
    const error = validateProductImageFile(new File([content], 'produto.png', { type: 'image/png' }));
    expect(error).toContain('1 MB');
  });

  it('accepts 4:3 dimensions within the backend limit', () => {
    expect(validateProductImageDimensions(800, 600)).toBeNull();
    expect(validateProductImageDimensions(1200, 900)).toBeNull();
  });

  it('rejects dimensions outside 4:3 or above the backend limit', () => {
    expect(validateProductImageDimensions(800, 800)).toContain('proporção 4:3');
    expect(validateProductImageDimensions(1600, 1200)).toContain('1200 × 900');
  });

  it('creates the correct data URL for raw PNG and JPEG Base64', () => {
    expect(getBase64ImageSource('iVBORw0KGgo')).toBe('data:image/png;base64,iVBORw0KGgo');
    expect(getBase64ImageSource('/9j/4AAQSkZJRg')).toBe('data:image/jpeg;base64,/9j/4AAQSkZJRg');
  });

  it('preserves an existing image data URL', () => {
    const source = 'data:image/jpeg;base64,/9j/example';
    expect(getBase64ImageSource(source)).toBe(source);
  });
});
