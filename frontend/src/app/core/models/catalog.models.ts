export interface ProdutoGestorResponse {
  id: number;
  nome: string;
  descricao: string | null;
  imagemUrl: string | null;
  preco: number;
  categoriaId: number;
  categoria: string;
  disponivel: boolean;
}

export interface CategoriaGestorResponse {
  id: number;
  nome: string;
  descricao: string | null;
  imagemUrl: string | null;
  ativo: boolean;
}

export interface ProdutoGestorRequest {
  nome: string;
  descricao: string | null;
  imagem: string | null;
  preco: number;
  categoriaId: number;
}

export interface CategoriaGestorRequest {
  nome: string;
  descricao: string | null;
  imagem: string | null;
}

export interface CategoriaOpcaoResponse {
  id: number;
  nome: string;
  ativo: boolean;
}

export interface CatalogPageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
