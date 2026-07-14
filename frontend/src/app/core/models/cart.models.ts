export type CustomerContext = 'mesa' | 'totem';

export const CART_OBSERVATION_MAX_LENGTH = 255;

export interface CartItem {
  cartItemId: string;
  productId: number;
  name: string;
  description: string;
  image?: string | null;
  unitPrice: number;
  quantity: number;
  observation?: string;
  categoryId?: number;
  categoryName?: string;
}

export interface CartSummary {
  items: CartItem[];
  subtotal: number;
  total: number;
  totalItems: number;
}
