export interface MenuResponse {
  categorias: CategoriaCardapio[];
  produtos: ProdutoCardapio[];
}

export interface CategoriaCardapio {
  id: number;
  nome: string;
  descricao?: string;
  slug?: string;
  imagemUrl?: string;
  disponivel?: boolean;
}

export interface ProdutoCardapio {
  id: number;
  nome: string;
  descricao: string;
  preco: number;
  categoriaId: number;
  categoriaNome?: string;
  categoriaSlug?: string;
  imagemUrl?: string;
  disponivel: boolean;
  tempoEstimadoPreparo?: number;
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
  idMesa: number;
  codigoSessao: number;
  itens: ItemPedidoMesaRequest[];
}

export interface ItemPedidoMesaRequest {
  idProduto: number;
  quantidade: number;
  precoUnitario: number;
  observacao?: string;
}
