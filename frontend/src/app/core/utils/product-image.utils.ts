export const PRODUCT_IMAGE_MAX_SIZE_BYTES = 1024 * 1024;
export const PRODUCT_IMAGE_MAX_WIDTH = 1200;
export const PRODUCT_IMAGE_MAX_HEIGHT = 900;

const ALLOWED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png']);
const ALLOWED_IMAGE_EXTENSION = /\.(jpe?g|png)$/i;

export function validateProductImageFile(file: File): string | null {
  if (!ALLOWED_IMAGE_TYPES.has(file.type) || !ALLOWED_IMAGE_EXTENSION.test(file.name)) {
    return 'Formato inválido. Selecione uma imagem JPG, JPEG ou PNG.';
  }

  if (file.size > PRODUCT_IMAGE_MAX_SIZE_BYTES) {
    return 'A imagem deve ter no máximo 1 MB.';
  }

  return null;
}

export function validateProductImageDimensions(width: number, height: number): string | null {
  if (width > PRODUCT_IMAGE_MAX_WIDTH || height > PRODUCT_IMAGE_MAX_HEIGHT) {
    return 'A imagem deve ter no máximo 1200 × 900 pixels.';
  }

  if (width * 3 !== height * 4) {
    return 'A imagem deve estar na proporção 4:3, como 800 × 600 ou 1200 × 900.';
  }

  return null;
}

export function getBase64ImageSource(image: string | null | undefined, apiUrl = ''): string | null {
  if (!image?.trim()) {
    return null;
  }

  const normalized = image.trim();

  if (normalized.startsWith('data:image/')) {
    return normalized;
  }

  if (normalized.startsWith('/9j/')) {
    return `data:image/jpeg;base64,${normalized}`;
  }

  if (/^https?:\/\//i.test(normalized) || normalized.startsWith('assets/')) {
    return normalized;
  }

  if (normalized.startsWith('/')) {
    return `${apiUrl.replace(/\/$/, '')}${normalized}`;
  }

  return `data:image/png;base64,${normalized}`;
}
