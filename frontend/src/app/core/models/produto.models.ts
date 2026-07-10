/**
 * Espelham os DTOs já existentes no BFF (`/api/gestor`). Nenhum campo novo:
 * - CategoriaGestorResponse: { id, nome, descricao, ativo }
 * - ProdutoGestorResponse:   { id, nome, descricao, imagem, preco, categoriaId, categoria, disponivel }
 * - CriarProdutoRequest:     { nome, descricao, imagem, preco, categoriaId }
 */

export interface CategoriaGestorResponse {
  id: number;
  nome: string;
  descricao?: string | null;
  ativo: boolean;
}

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

export interface CriarProdutoRequest {
  nome: string;
  descricao?: string | null;
  imagem?: string | null;
  preco: number;
  categoriaId: number;
  disponivel: boolean;
}

export interface CriarCategoriaRequest {
  nome: string;
  descricao?: string | null;
}
