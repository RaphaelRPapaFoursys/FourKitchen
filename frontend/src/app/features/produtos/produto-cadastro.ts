import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  CategoriaGestorResponse,
  CriarCategoriaRequest,
  CriarProdutoRequest,
} from '../../core/models/produto.models';
import { comprimirImagem } from '../../core/utils/imagem.util';
import { AuthService } from '../../core/services/auth';
import { ProdutoService } from '../../core/services/produto';
import { Badge } from '../../shared/components/badge/badge';
import { Icon } from '../../shared/components/icon/icon';
import { Sidebar } from '../../shared/components/sidebar/sidebar';
import { Topbar } from '../../shared/components/header/header';

const NOME_MIN = 3;
const NOME_MAX = 150;
const DESCRICAO_MAX = 255;
const CATEGORIA_NOME_MIN = 3;
const CATEGORIA_NOME_MAX = 80;
const IMAGEM_MAX_BYTES = 5 * 1024 * 1024;
const TIPOS_IMAGEM = ['image/png', 'image/jpeg', 'image/webp'];

/**
 * Tela "Cadastrar produto" (perfil GESTOR/ADMIN).
 * Consome apenas os endpoints e campos já existentes no BFF gestor:
 * GET /api/gestor/categorias, POST /api/gestor/produtos e PATCH .../desativar.
 * Reaproveita fk-sidebar, fk-topbar, fk-icon e fk-badge.
 */
@Component({
  selector: 'app-produto-cadastro',
  imports: [FormsModule, CurrencyPipe, Sidebar, Topbar, Icon, Badge],
  templateUrl: './produto-cadastro.html',
  styleUrl: './produto-cadastro.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProdutoCadastro {
  private readonly produtoService = inject(ProdutoService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly usuario = toSignal(this.authService.usuario$, {
    initialValue: this.authService.getCurrentUser(),
  });

  protected readonly descricaoMax = DESCRICAO_MAX;

  // ---- Campos do formulário (só o que o backend aceita) -------------------
  protected readonly nome = signal('');
  protected readonly categoriaId = signal<number | null>(null);
  protected readonly preco = signal<number | null>(null);
  protected readonly descricao = signal('');
  protected readonly imagem = signal<string | null>(null);
  protected readonly disponivel = signal(true);

  // ---- Categorias (dropdown) ----------------------------------------------
  protected readonly categorias = signal<CategoriaGestorResponse[]>([]);
  protected readonly carregandoCategorias = signal(true);

  // ---- Criação de categoria inline ----------------------------------------
  protected readonly criandoCategoria = signal(false);
  protected readonly novaCategoriaNome = signal('');
  protected readonly salvandoCategoria = signal(false);
  protected readonly novaCategoriaValida = computed(() => {
    const tamanho = this.novaCategoriaNome().trim().length;
    return tamanho >= CATEGORIA_NOME_MIN && tamanho <= CATEGORIA_NOME_MAX;
  });

  // ---- Estado da submissão ------------------------------------------------
  protected readonly salvando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly sucesso = signal<string | null>(null);
  protected readonly tentouSalvar = signal(false);

  // ---- Validações (espelham as regras do backend) -------------------------
  protected readonly nomeValido = computed(() => {
    const valor = this.nome().trim().length;
    return valor >= NOME_MIN && valor <= NOME_MAX;
  });
  protected readonly precoValido = computed(() => {
    const valor = this.preco();
    return valor !== null && valor >= 0.01;
  });
  protected readonly categoriaValida = computed(() => this.categoriaId() !== null);
  protected readonly descricaoValida = computed(() => this.descricao().length <= DESCRICAO_MAX);
  protected readonly formValido = computed(
    () =>
      this.nomeValido() &&
      this.precoValido() &&
      this.categoriaValida() &&
      this.descricaoValida(),
  );

  // ---- Prévia (derivada dos campos existentes) ----------------------------
  protected readonly categoriaNome = computed(() => {
    const id = this.categoriaId();
    return this.categorias().find(categoria => categoria.id === id)?.nome ?? null;
  });

  constructor() {
    this.carregarCategorias();
  }

  private carregarCategorias(): void {
    this.carregandoCategorias.set(true);
    this.produtoService.listarCategorias().subscribe({
      next: categorias => {
        this.categorias.set(categorias);
        this.carregandoCategorias.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível carregar as categorias.');
        this.carregandoCategorias.set(false);
      },
    });
  }

  protected abrirNovaCategoria(): void {
    this.criandoCategoria.set(true);
  }

  protected cancelarNovaCategoria(): void {
    this.criandoCategoria.set(false);
    this.novaCategoriaNome.set('');
  }

  protected salvarCategoria(): void {
    if (!this.novaCategoriaValida() || this.salvandoCategoria()) return;

    const request: CriarCategoriaRequest = { nome: this.novaCategoriaNome().trim() };

    this.salvandoCategoria.set(true);
    this.produtoService.criarCategoria(request).subscribe({
      next: categoria => {
        this.categorias.update(atuais =>
          [...atuais, categoria].sort((a, b) => a.nome.localeCompare(b.nome)),
        );
        this.categoriaId.set(categoria.id); // já seleciona a nova categoria
        this.salvandoCategoria.set(false);
        this.cancelarNovaCategoria();
      },
      error: erro => {
        this.salvandoCategoria.set(false);
        this.erro.set(this.mensagemErro(erro));
      },
    });
  }

  protected readonly arrastando = signal(false);

  protected aoSelecionarImagem(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    const arquivo = input.files?.[0];
    input.value = ''; // permite reenviar o mesmo arquivo
    if (arquivo) void this.processarArquivo(arquivo);
  }

  protected aoArrastarSobre(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastando.set(true);
  }

  protected aoSairDaArea(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastando.set(false);
  }

  protected aoSoltarImagem(evento: DragEvent): void {
    evento.preventDefault();
    this.arrastando.set(false);
    const arquivo = evento.dataTransfer?.files?.[0];
    if (arquivo) void this.processarArquivo(arquivo);
  }

  private async processarArquivo(arquivo: File): Promise<void> {
    if (!TIPOS_IMAGEM.includes(arquivo.type)) {
      this.erro.set('Formato inválido. Use PNG, JPG ou WEBP.');
      return;
    }
    if (arquivo.size > IMAGEM_MAX_BYTES) {
      this.erro.set('Imagem muito grande. O limite é 5MB.');
      return;
    }

    try {
      this.imagem.set(await comprimirImagem(arquivo));
      this.erro.set(null);
    } catch {
      this.erro.set('Não foi possível ler a imagem.');
    }
  }

  protected removerImagem(): void {
    this.imagem.set(null);
  }

  protected atualizarPreco(valor: number | string | null): void {
    if (valor === null || valor === '') {
      this.preco.set(null);
      return;
    }
    const numero = typeof valor === 'number' ? valor : Number.parseFloat(valor.replace(',', '.'));
    this.preco.set(Number.isNaN(numero) ? null : numero);
  }

  protected salvar(): void {
    this.tentouSalvar.set(true);
    this.erro.set(null);
    this.sucesso.set(null);

    if (!this.formValido() || this.salvando()) return;

    const request: CriarProdutoRequest = {
      nome: this.nome().trim(),
      descricao: this.descricao().trim() || null,
      imagem: this.imagem(),
      preco: this.preco() as number,
      categoriaId: this.categoriaId() as number,
      disponivel: this.disponivel(),
    };

    this.salvando.set(true);
    this.produtoService.criarProduto(request).subscribe({
      next: produto => this.finalizarSucesso(produto.nome),
      error: erro => {
        this.salvando.set(false);
        this.erro.set(this.mensagemErro(erro));
      },
    });
  }

  private finalizarSucesso(nomeProduto: string): void {
    this.salvando.set(false);
    this.sucesso.set(`Produto "${nomeProduto}" cadastrado com sucesso.`);
    this.limparFormulario();
  }

  private limparFormulario(): void {
    this.nome.set('');
    this.categoriaId.set(null);
    this.preco.set(null);
    this.descricao.set('');
    this.imagem.set(null);
    this.disponivel.set(true);
    this.tentouSalvar.set(false);
  }

  private mensagemErro(erro: unknown): string {
    const payload = (erro as { error?: { msgError?: string; message?: string } })?.error;
    return payload?.msgError ?? payload?.message ?? 'Não foi possível cadastrar o produto.';
  }

  protected cancelar(): void {
    void this.router.navigateByUrl('/gestor');
  }

  protected sair(): void {
    this.authService.logout();
    void this.router.navigateByUrl('/login');
  }

  protected iniciais(nome: string | null | undefined): string {
    return nome ? nome.charAt(0).toUpperCase() : '?';
  }
}
