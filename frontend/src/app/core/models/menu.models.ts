export interface CategoriaCardapioResponse {
  categoriaId: number;
  categoriaNome: string;
  categoriaDescricao?: string;
  categoriaImagem?: string | null;
  produtos: ProdutoCardapioResponse[];
}

export interface ProdutoCardapioResponse {
  id: number;
  nome: string;
  descricao: string;
  imagem?: string | null;
  preco: number;
}

export interface ProdutoCardapioView {
  id: number;
  nome: string;
  descricao: string;
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
