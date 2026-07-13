export interface CategoriaCardapioResponse {
  categoriaId: number;
  categoriaNome: string;
  categoriaDescricao?: string;
  produtos: ProdutoCardapioResponse[];
}

export interface CategoriaMenuResponse {
  categoriaId: number;
  categoriaNome: string;
  categoriaDescricao?: string;
  imagemUrl?: string | null;
}

export interface CardapioPaginadoResponse {
  content: CategoriaCardapioResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ProdutoCardapioResponse {
  id: number;
  nome: string;
  descricao: string;
  imagemUrl?: string | null;
  imagem?: string | null;
  preco: number;
}

export interface ProdutoCardapioView {
  id: number;
  nome: string;
  descricao: string;
  imagemUrl?: string | null;
  imagem?: string | null;
  preco: number;
  categoriaId: number;
  categoriaNome: string;
}

export interface ErrorResponse {
  codError: string;
  msgError: string;
}

export interface CriarPedidoTotemRequest {
  itens: ItemPedidoTotemRequest[];
}

export interface ItemPedidoTotemRequest {
  idProduto: number;
  quantidade: number;
  observacao?: string;
}

export interface CriarPedidoMesaRequest {
  codigoAtendimento: number;
  itens: ItemPedidoMesaRequest[];
}

export interface ItemPedidoMesaRequest {
  idProduto: number;
  quantidade: number;
  observacao?: string;
}
