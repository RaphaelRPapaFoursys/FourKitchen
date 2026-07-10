import { base64ParaDataUrl, comprimirImagem, dimensionar } from './imagem.util';

describe('base64ParaDataUrl', () => {
  it('retorna null para valores vazios', () => {
    expect(base64ParaDataUrl(null)).toBeNull();
    expect(base64ParaDataUrl(undefined)).toBeNull();
    expect(base64ParaDataUrl('')).toBeNull();
  });

  it('mantém um Data URL já formado', () => {
    expect(base64ParaDataUrl('data:image/png;base64,AAAA')).toBe('data:image/png;base64,AAAA');
  });

  it('detecta o mime pelos magic bytes', () => {
    expect(base64ParaDataUrl('iVBORw0KGgoXXXX')).toBe('data:image/png;base64,iVBORw0KGgoXXXX');
    expect(base64ParaDataUrl('/9j/4AAQ')).toBe('data:image/jpeg;base64,/9j/4AAQ');
    expect(base64ParaDataUrl('UklGRxxx')).toBe('data:image/webp;base64,UklGRxxx');
    expect(base64ParaDataUrl('R0lGODxxx')).toBe('data:image/gif;base64,R0lGODxxx');
  });

  it('usa image/* como fallback para conteúdo desconhecido', () => {
    expect(base64ParaDataUrl('ZZZZ')).toBe('data:image/*;base64,ZZZZ');
  });
});

describe('dimensionar', () => {
  it('mantém as dimensões quando dentro do limite', () => {
    expect(dimensionar(800, 600, 1280)).toEqual({ largura: 800, altura: 600 });
  });

  it('não divide por zero', () => {
    expect(dimensionar(0, 0, 1280)).toEqual({ largura: 0, altura: 0 });
  });

  it('reduz mantendo proporção (paisagem)', () => {
    expect(dimensionar(2560, 1280, 1280)).toEqual({ largura: 1280, altura: 640 });
  });

  it('reduz mantendo proporção (retrato)', () => {
    expect(dimensionar(1000, 2000, 1000)).toEqual({ largura: 500, altura: 1000 });
  });
});

describe('comprimirImagem', () => {
  class FakeFileReader {
    onload: (() => void) | null = null;
    onerror: (() => void) | null = null;
    result: string | null = null;
    readAsDataURL(): void {
      this.result = 'data:image/png;base64,ORIGINAL';
      queueMicrotask(() => this.onload?.());
    }
  }

  class FakeImage {
    onload: (() => void) | null = null;
    onerror: (() => void) | null = null;
    width = 2000;
    height = 1000;
    set src(_v: string) {
      queueMicrotask(() => this.onload?.());
    }
  }

  const arquivo = new File(['x'], 'a.png', { type: 'image/png' });

  afterEach(() => vi.unstubAllGlobals());

  it('redimensiona e reencoda via canvas quando disponível', async () => {
    vi.stubGlobal('FileReader', FakeFileReader);
    vi.stubGlobal('Image', FakeImage);
    const ctx = { drawImage: vi.fn() };
    const canvas = {
      width: 0,
      height: 0,
      getContext: vi.fn().mockReturnValue(ctx),
      toDataURL: vi.fn().mockReturnValue('data:image/jpeg;base64,COMPRIMIDA'),
    };
    vi.spyOn(document, 'createElement').mockReturnValue(canvas as unknown as HTMLElement);

    const saida = await comprimirImagem(arquivo, { maxDimensao: 1000, qualidade: 0.7 });

    expect(saida).toBe('data:image/jpeg;base64,COMPRIMIDA');
    expect(canvas.width).toBe(1000);
    expect(canvas.height).toBe(500);
    expect(ctx.drawImage).toHaveBeenCalled();
    expect(canvas.toDataURL).toHaveBeenCalledWith('image/jpeg', 0.7);
  });

  it('devolve o original quando o canvas não está disponível', async () => {
    vi.stubGlobal('FileReader', FakeFileReader);
    vi.stubGlobal('Image', FakeImage);
    const canvas = { getContext: vi.fn().mockReturnValue(null) };
    vi.spyOn(document, 'createElement').mockReturnValue(canvas as unknown as HTMLElement);

    const saida = await comprimirImagem(arquivo);
    expect(saida).toBe('data:image/png;base64,ORIGINAL');
  });

  it('devolve o original quando a imagem não decodifica', async () => {
    vi.stubGlobal('FileReader', FakeFileReader);
    class BrokenImage {
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;
      set src(_v: string) {
        queueMicrotask(() => this.onerror?.());
      }
    }
    vi.stubGlobal('Image', BrokenImage);

    const saida = await comprimirImagem(arquivo);
    expect(saida).toBe('data:image/png;base64,ORIGINAL');
  });

  it('rejeita quando a leitura do arquivo falha', async () => {
    class FailingReader {
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;
      readAsDataURL(): void {
        queueMicrotask(() => this.onerror?.());
      }
    }
    vi.stubGlobal('FileReader', FailingReader);

    await expect(comprimirImagem(arquivo)).rejects.toThrow('Falha ao ler a imagem.');
  });
});
