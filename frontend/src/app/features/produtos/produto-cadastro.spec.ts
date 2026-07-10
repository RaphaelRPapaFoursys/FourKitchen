import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth';
import { ProdutoCadastro } from './produto-cadastro';

const BASE_URL = `${environment.apiUrl}/api/gestor`;

/**
 * A compressão real usa canvas (indisponível no jsdom). Stubamos `Image` para
 * que `onload` dispare; o canvas cai no fallback e `comprimirImagem` devolve o
 * Data URL original lido pelo FileReader — sem travar o teste.
 */
class FakeImage {
  onload: (() => void) | null = null;
  onerror: (() => void) | null = null;
  width = 100;
  height = 100;
  set src(_v: string) {
    queueMicrotask(() => this.onload?.());
  }
}

const CATEGORIAS_API = [
  { id: 1, nome: 'Prato principal', descricao: null, ativo: true },
  { id: 2, nome: 'Bebida', descricao: null, ativo: true },
];

function produtoApi(over: Record<string, unknown> = {}) {
  return {
    id: 10,
    nome: 'Filé à Parmegiana',
    descricao: 'Filé empanado.',
    imagem: null,
    preco: 59.9,
    categoriaId: 1,
    categoria: 'Prato principal',
    disponivel: true,
    ...over,
  };
}

describe('ProdutoCadastro', () => {
  let component: ProdutoCadastro;
  let fixture: ComponentFixture<ProdutoCadastro>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    vi.stubGlobal('Image', FakeImage);
    await TestBed.configureTestingModule({
      imports: [ProdutoCadastro],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    vi.unstubAllGlobals();
  });

  /** Cria o componente e resolve (ou falha) o GET inicial de categorias. */
  function criar(opts: { erroCategorias?: boolean } = {}): void {
    fixture = TestBed.createComponent(ProdutoCadastro);
    component = fixture.componentInstance;
    const req = httpMock.expectOne(`${BASE_URL}/categorias`);
    if (opts.erroCategorias) {
      req.flush(null, { status: 500, statusText: 'Server Error' });
    } else {
      req.flush(CATEGORIAS_API);
    }
  }

  function preencherValido(): void {
    component['nome'].set('Filé à Parmegiana');
    component['descricao'].set('Filé empanado.');
    component['preco'].set(59.9);
    component['categoriaId'].set(1);
  }

  it('should create e carrega categorias', () => {
    criar();
    expect(component).toBeTruthy();
    expect(component['categorias']().length).toBe(2);
    expect(component['carregandoCategorias']()).toBe(false);
  });

  it('exibe erro quando o carregamento de categorias falha', () => {
    criar({ erroCategorias: true });
    expect(component['erro']()).toContain('categorias');
    expect(component['carregandoCategorias']()).toBe(false);
  });

  it('formulário é inválido sem os campos obrigatórios', () => {
    criar();
    expect(component['formValido']()).toBe(false);
  });

  it('valida nome, preço, categoria e tamanho da descrição', () => {
    criar();
    // nome curto
    component['nome'].set('ab');
    expect(component['nomeValido']()).toBe(false);
    component['nome'].set('Água');
    expect(component['nomeValido']()).toBe(true);
    // preço deve ser > 0
    component['preco'].set(0);
    expect(component['precoValido']()).toBe(false);
    component['preco'].set(0.01);
    expect(component['precoValido']()).toBe(true);
    // categoria obrigatória
    expect(component['categoriaValida']()).toBe(false);
    component['categoriaId'].set(2);
    expect(component['categoriaValida']()).toBe(true);
    // descrição no limite
    component['descricao'].set('x'.repeat(256));
    expect(component['descricaoValida']()).toBe(false);
    component['descricao'].set('x'.repeat(255));
    expect(component['formValido']()).toBe(true);
  });

  it('categoriaNome reflete a categoria selecionada', () => {
    criar();
    expect(component['categoriaNome']()).toBeNull();
    component['categoriaId'].set(1);
    expect(component['categoriaNome']()).toBe('Prato principal');
  });

  it('cria categoria inline, adiciona ordenada e já seleciona', () => {
    criar();
    component['abrirNovaCategoria']();
    expect(component['criandoCategoria']()).toBe(true);

    // validação do nome (3–80)
    component['novaCategoriaNome'].set('ab');
    expect(component['novaCategoriaValida']()).toBe(false);
    component['novaCategoriaNome'].set('Aperitivos');
    expect(component['novaCategoriaValida']()).toBe(true);

    component['salvarCategoria']();
    const req = httpMock.expectOne(`${BASE_URL}/categorias`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nome: 'Aperitivos' });
    req.flush({ id: 9, nome: 'Aperitivos', descricao: null, ativo: true });

    // adicionada e ordenada (Aperitivos vem antes de Bebida/Prato principal)
    expect(component['categorias']().map(c => c.nome)).toEqual([
      'Aperitivos',
      'Prato principal',
      'Bebida',
    ].sort((a, b) => a.localeCompare(b)));
    expect(component['categoriaId']()).toBe(9); // já selecionada
    expect(component['criandoCategoria']()).toBe(false);
    expect(component['novaCategoriaNome']()).toBe('');
  });

  it('não cria categoria quando o nome é inválido', () => {
    criar();
    component['abrirNovaCategoria']();
    component['novaCategoriaNome'].set('ab');
    component['salvarCategoria']();
    httpMock.expectNone(`${BASE_URL}/categorias`);
  });

  it('mostra erro do backend ao falhar a criação de categoria', () => {
    criar();
    component['abrirNovaCategoria']();
    component['novaCategoriaNome'].set('Duplicada');
    component['salvarCategoria']();
    httpMock
      .expectOne(`${BASE_URL}/categorias`)
      .flush({ msgError: 'Categoria já existe.' }, { status: 409, statusText: 'Conflict' });
    expect(component['erro']()).toBe('Categoria já existe.');
    expect(component['salvandoCategoria']()).toBe(false);
  });

  it('cancelarNovaCategoria fecha e limpa o campo', () => {
    criar();
    component['abrirNovaCategoria']();
    component['novaCategoriaNome'].set('x');
    component['cancelarNovaCategoria']();
    expect(component['criandoCategoria']()).toBe(false);
    expect(component['novaCategoriaNome']()).toBe('');
  });

  it('cria o produto com o payload esperado (apenas campos existentes)', () => {
    criar();
    preencherValido();
    expect(component['formValido']()).toBe(true);

    component['salvar']();

    const req = httpMock.expectOne(`${BASE_URL}/produtos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      nome: 'Filé à Parmegiana',
      descricao: 'Filé empanado.',
      imagem: null,
      preco: 59.9,
      categoriaId: 1,
      disponivel: true,
    });

    req.flush(produtoApi());

    expect(component['sucesso']()).toContain('cadastrado com sucesso');
    expect(component['salvando']()).toBe(false);
    // Limpa o formulário após sucesso.
    expect(component['nome']()).toBe('');
    expect(component['disponivel']()).toBe(true);
  });

  it('envia descricao null quando o campo está vazio', () => {
    criar();
    component['nome'].set('Água com gás');
    component['preco'].set(6.9);
    component['categoriaId'].set(2);

    component['salvar']();
    const req = httpMock.expectOne(`${BASE_URL}/produtos`);
    expect(req.request.body.descricao).toBeNull();
    req.flush(produtoApi({ id: 30 }));
  });

  it('envia disponivel=false no POST e NÃO faz PATCH desativar (atômico)', () => {
    criar();
    component['nome'].set('Suco Natural');
    component['preco'].set(12);
    component['categoriaId'].set(2);
    component['disponivel'].set(false);

    component['salvar']();

    const req = httpMock.expectOne(`${BASE_URL}/produtos`);
    expect(req.request.body.disponivel).toBe(false);
    req.flush(produtoApi({ id: 20, nome: 'Suco Natural', disponivel: false }));

    // Não deve haver chamada de desativar: a criação já persiste a disponibilidade.
    httpMock.expectNone(`${BASE_URL}/produtos/20/desativar`);
    expect(component['sucesso']()).toContain('cadastrado com sucesso');
  });

  it('mostra a mensagem de erro do backend quando o POST falha', () => {
    criar();
    preencherValido();
    component['salvar']();
    httpMock
      .expectOne(`${BASE_URL}/produtos`)
      .flush({ msgError: 'Nome já cadastrado.' }, { status: 400, statusText: 'Bad Request' });

    expect(component['erro']()).toBe('Nome já cadastrado.');
    expect(component['salvando']()).toBe(false);
  });

  it('usa mensagem padrão quando o erro não traz msgError', () => {
    criar();
    preencherValido();
    component['salvar']();
    httpMock.expectOne(`${BASE_URL}/produtos`).flush(null, { status: 500, statusText: 'Server Error' });

    expect(component['erro']()).toBe('Não foi possível cadastrar o produto.');
  });

  it('não faz POST quando o formulário é inválido', () => {
    criar();
    component['salvar']();
    httpMock.expectNone(`${BASE_URL}/produtos`);
    expect(component['tentouSalvar']()).toBe(true);
  });

  it('atualizarPreco interpreta número, vírgula, vazio e valor inválido', () => {
    criar();
    component['atualizarPreco'](30);
    expect(component['preco']()).toBe(30);
    component['atualizarPreco']('12,50');
    expect(component['preco']()).toBe(12.5);
    component['atualizarPreco']('');
    expect(component['preco']()).toBeNull();
    component['atualizarPreco'](null);
    expect(component['preco']()).toBeNull();
    component['atualizarPreco']('abc');
    expect(component['preco']()).toBeNull();
  });

  it('aceita imagem válida (comprimida) e permite removê-la', async () => {
    criar();
    const file = new File(['conteudo'], 'foto.png', { type: 'image/png' });
    component['aoSelecionarImagem']({ target: { files: [file], value: '' } } as unknown as Event);

    await vi.waitFor(() => expect(component['imagem']()).toContain('data:image'));
    expect(component['erro']()).toBeNull();

    component['removerImagem']();
    expect(component['imagem']()).toBeNull();
  });

  it('exibe erro quando o processamento da imagem falha', async () => {
    criar();
    // FileReader que falha faz comprimirImagem rejeitar.
    class FailingReader {
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;
      readAsDataURL(): void {
        queueMicrotask(() => this.onerror?.());
      }
    }
    vi.stubGlobal('FileReader', FailingReader);
    const file = new File(['x'], 'a.png', { type: 'image/png' });
    component['aoSelecionarImagem']({ target: { files: [file], value: '' } } as unknown as Event);

    await vi.waitFor(() => expect(component['erro']()).toContain('Não foi possível ler a imagem'));
  });

  it('ignora quando nenhum arquivo é selecionado', () => {
    criar();
    component['aoSelecionarImagem']({ target: { files: [] } } as unknown as Event);
    expect(component['imagem']()).toBeNull();
    expect(component['erro']()).toBeNull();
  });

  it('aceita imagem solta via drag-and-drop', async () => {
    criar();
    const file = new File(['x'], 'foto.png', { type: 'image/png' });
    const evento = {
      preventDefault: vi.fn(),
      dataTransfer: { files: [file] },
    } as unknown as DragEvent;
    component['aoSoltarImagem'](evento);

    await vi.waitFor(() => expect(component['imagem']()).toContain('data:image'));
    expect(evento.preventDefault).toHaveBeenCalled();
    expect(component['arrastando']()).toBe(false);
  });

  it('marca e desmarca o estado de arraste', () => {
    criar();
    const over = { preventDefault: vi.fn() } as unknown as DragEvent;
    component['aoArrastarSobre'](over);
    expect(component['arrastando']()).toBe(true);
    const leave = { preventDefault: vi.fn() } as unknown as DragEvent;
    component['aoSairDaArea'](leave);
    expect(component['arrastando']()).toBe(false);
  });

  it('rejeita imagem de formato inválido', () => {
    criar();
    const input = { files: [new File(['x'], 'a.gif', { type: 'image/gif' })], value: 'manter' };
    component['aoSelecionarImagem']({ target: input } as unknown as Event);
    expect(component['erro']()).toContain('Formato inválido');
    expect(input.value).toBe('');
    expect(component['imagem']()).toBeNull();
  });

  it('rejeita imagem acima de 5MB', () => {
    criar();
    const file = new File(['x'], 'grande.png', { type: 'image/png' });
    Object.defineProperty(file, 'size', { value: 5 * 1024 * 1024 + 1 });
    const input = { files: [file], value: 'manter' };
    component['aoSelecionarImagem']({ target: input } as unknown as Event);
    expect(component['erro']()).toContain('5MB');
    expect(input.value).toBe('');
  });

  it('cancelar navega para /gestor', () => {
    criar();
    const router = TestBed.inject(Router);
    const nav = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component['cancelar']();
    expect(nav).toHaveBeenCalledWith('/gestor');
  });

  it('sair faz logout e navega para /login', () => {
    criar();
    const router = TestBed.inject(Router);
    const auth = TestBed.inject(AuthService);
    const logout = vi.spyOn(auth, 'logout').mockImplementation(() => {});
    const nav = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component['sair']();
    expect(logout).toHaveBeenCalledOnce();
    expect(nav).toHaveBeenCalledWith('/login');
  });

  it('iniciais retorna a primeira letra ou ? quando vazio', () => {
    criar();
    expect(component['iniciais']('Carlos')).toBe('C');
    expect(component['iniciais'](null)).toBe('?');
    expect(component['iniciais'](undefined)).toBe('?');
  });
});

describe('ProdutoCadastro (DOM)', () => {
  let component: ProdutoCadastro;
  let fixture: ComponentFixture<ProdutoCadastro>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    vi.stubGlobal('Image', FakeImage);
    await TestBed.configureTestingModule({
      imports: [ProdutoCadastro],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ProdutoCadastro);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
    vi.unstubAllGlobals();
  });

  const el = (sel: string): HTMLElement => fixture.nativeElement.querySelector(sel) as HTMLElement;

  it('mostra placeholder de carregamento e depois as categorias', () => {
    fixture.detectChanges(); // render com carregandoCategorias = true
    const req = httpMock.expectOne(`${BASE_URL}/categorias`);
    expect(el('#categoria').textContent).toContain('Carregando categorias');
    // só o placeholder enquanto carrega
    expect(fixture.nativeElement.querySelectorAll('#categoria option').length).toBe(1);

    req.flush(CATEGORIAS_API);
    fixture.detectChanges();
    expect(el('#categoria').textContent).toContain('Selecione uma categoria');
    // placeholder + 2 categorias
    expect(fixture.nativeElement.querySelectorAll('#categoria option').length).toBe(3);
  });

  it('renderiza a prévia com imagem e remove pelo botão', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);

    component['imagem'].set('data:image/png;base64,AAAA');
    component['categoriaId'].set(1);
    fixture.detectChanges();
    expect(el('.dropzone__preview')).toBeTruthy();
    expect(el('.previa-card__imagem img')).toBeTruthy();

    (el('.dropzone__remover') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el('.dropzone__preview')).toBeNull();
    expect(el('.dropzone__titulo')).toBeTruthy(); // volta ao placeholder
  });

  it('renderiza alertas de sucesso e de erro', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);

    component['sucesso'].set('Produto "X" cadastrado com sucesso.');
    fixture.detectChanges();
    expect(el('.alerta--sucesso')?.textContent).toContain('sucesso');

    component['sucesso'].set(null);
    component['erro'].set('Falhou.');
    fixture.detectChanges();
    expect(el('.alerta--erro')?.textContent).toContain('Falhou.');
  });

  it('exibe mensagens de validação ao submeter inválido (via DOM)', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);

    el('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    const erros = fixture.nativeElement.querySelectorAll('.campo__erro');
    expect(erros.length).toBeGreaterThanOrEqual(3); // nome, categoria, preço
    httpMock.expectNone(`${BASE_URL}/produtos`);
  });

  it('dispara o (change) do input de arquivo e submete pelo botão', async () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);
    fixture.detectChanges();

    // dispara o handler (change) do input de arquivo pelo DOM
    const file = new File(['x'], 'foto.png', { type: 'image/png' });
    const fileInput = el('.dropzone__input') as HTMLInputElement;
    Object.defineProperty(fileInput, 'files', { value: [file], configurable: true });
    fileInput.dispatchEvent(new Event('change'));
    await vi.waitFor(() => expect(component['imagem']()).toContain('data:image'));

    // preenche o resto via signals e submete pelo botão (aciona o ngSubmit)
    component['nome'].set('Filé à Parmegiana');
    component['preco'].set(59.9);
    component['categoriaId'].set(1);
    fixture.detectChanges();

    (el('.btn--primario') as HTMLButtonElement).click();
    const req = httpMock.expectOne(`${BASE_URL}/produtos`);
    expect(req.request.body.nome).toBe('Filé à Parmegiana');
    expect(req.request.body.imagem).toContain('data:image');
    req.flush(produtoApi());
    fixture.detectChanges();
    expect(el('.alerta--sucesso')).toBeTruthy();
  });

  it('reflete usuário logado, estado "Salvando" e preço válido na prévia', () => {
    const auth = TestBed.inject(AuthService) as unknown as {
      usuarioSubject: { next: (u: unknown) => void };
    };
    auth.usuarioSubject.next({ id: 1, nome: 'Carlos', email: 'carlos@fk.com', perfil: 'ADMIN' });

    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);

    component['preco'].set(59.9);
    component['categoriaId'].set(1);
    component['salvando'].set(true);
    fixture.detectChanges();

    expect(el('.topbar__usuario-nome')?.textContent).toContain('Carlos');
    expect(el('.btn--primario')?.textContent).toContain('Salvando');
    expect(el('.previa-card__preco')?.textContent).toContain('59');
  });

  it('cria categoria inline pelo DOM (botão Criar)', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);
    fixture.detectChanges();

    (el('.campo__acao-link') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el('.nova-categoria')).toBeTruthy();
    expect(el('#categoria')).toBeNull(); // select some enquanto cria

    component['novaCategoriaNome'].set('Aperitivos');
    fixture.detectChanges();
    (fixture.nativeElement.querySelector('.nova-categoria .btn--primario') as HTMLButtonElement).click();

    httpMock
      .expectOne(`${BASE_URL}/categorias`)
      .flush({ id: 9, nome: 'Aperitivos', descricao: null, ativo: true });
    fixture.detectChanges();

    expect(el('.nova-categoria')).toBeNull(); // fechou
    expect(el('#categoria')).toBeTruthy(); // select voltou
    expect(component['categoriaId']()).toBe(9);
  });

  it('cancela a criação de categoria pelo DOM', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);
    fixture.detectChanges();

    (el('.campo__acao-link') as HTMLButtonElement).click();
    fixture.detectChanges();
    const cancelar = fixture.nativeElement.querySelector('.nova-categoria .btn--ghost') as HTMLButtonElement;
    cancelar.click();
    fixture.detectChanges();
    expect(el('.nova-categoria')).toBeNull();
    expect(el('#categoria')).toBeTruthy();
  });

  it('reage aos eventos de drag-and-drop no dropzone (DOM)', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);
    fixture.detectChanges();

    const zone = el('.dropzone');
    zone.dispatchEvent(new Event('dragover'));
    fixture.detectChanges();
    expect(zone.classList.contains('dropzone--arrastando')).toBe(true);

    zone.dispatchEvent(new Event('dragleave'));
    fixture.detectChanges();
    expect(zone.classList.contains('dropzone--arrastando')).toBe(false);

    const drop = new Event('drop');
    Object.defineProperty(drop, 'dataTransfer', { value: { files: [] } });
    zone.dispatchEvent(drop);
    expect(component['imagem']()).toBeNull(); // drop sem arquivo não altera
  });

  it('alterna disponibilidade e aciona cancelar pelos botões', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${BASE_URL}/categorias`).flush(CATEGORIAS_API);

    const router = TestBed.inject(Router);
    const nav = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    const [disp, indisp] = fixture.nativeElement.querySelectorAll('.toggle__opcao') as NodeListOf<HTMLButtonElement>;
    indisp.click();
    fixture.detectChanges();
    expect(component['disponivel']()).toBe(false);
    expect(el('.previa-card__badge--status')?.textContent).toContain('Indisponível');
    disp.click();
    expect(component['disponivel']()).toBe(true);

    (el('.btn--ghost') as HTMLButtonElement).click();
    expect(nav).toHaveBeenCalledWith('/gestor');
  });
});
