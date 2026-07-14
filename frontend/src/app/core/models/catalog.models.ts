export interface ProdutoGestorResponse {
  id: number;
  nome: string;
  descricao: string | null;
  imagem: string | null;
  preco: number;
  categoriaId: number;
  categoria: string;
  disponivel: boolean;
}

export interface CategoriaGestorResponse {
  id: number;
  nome: string;
  descricao: string | null;
  imagem: string | null;
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
