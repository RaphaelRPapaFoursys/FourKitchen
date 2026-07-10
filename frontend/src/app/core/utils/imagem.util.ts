/**
 * Utilitários de imagem para o cadastro de produtos.
 *
 * O backend persiste a imagem como bytes e devolve **base64 puro** (sem o
 * prefixo `data:...;base64,`). Estas funções centralizam a normalização para
 * exibição e a compressão no cliente antes do upload.
 */

const MAGIC_PARA_MIME: ReadonlyArray<readonly [string, string]> = [
  ['iVBORw0KGgo', 'image/png'], // PNG
  ['/9j/', 'image/jpeg'], // JPEG
  ['UklGR', 'image/webp'], // WEBP (RIFF)
  ['R0lGOD', 'image/gif'], // GIF
];

/**
 * Converte um base64 (com ou sem prefixo) em um Data URL exibível em `<img>`.
 * Detecta o tipo pelos primeiros bytes; usa `image/*` como último recurso.
 */
export function base64ParaDataUrl(valor: string | null | undefined): string | null {
  if (!valor) {
    return null;
  }
  if (valor.startsWith('data:')) {
    return valor;
  }
  const mime = MAGIC_PARA_MIME.find(([assinatura]) => valor.startsWith(assinatura))?.[1] ?? 'image/*';
  return `data:${mime};base64,${valor}`;
}

export interface CompressaoOpcoes {
  /** Maior dimensão (largura ou altura) permitida, em px. */
  maxDimensao?: number;
  /** Qualidade do JPEG resultante (0–1). */
  qualidade?: number;
}

/**
 * Lê um arquivo de imagem e retorna um Data URL, redimensionando/comprimindo
 * via canvas quando disponível. Se o canvas não estiver disponível (ex.: em
 * ambiente de teste sem suporte), devolve o Data URL original sem alterar.
 */
export async function comprimirImagem(
  arquivo: File,
  opcoes: CompressaoOpcoes = {},
): Promise<string> {
  const { maxDimensao = 1280, qualidade = 0.8 } = opcoes;
  const dataUrlOriginal = await lerComoDataUrl(arquivo);

  try {
    const imagem = await carregarImagem(dataUrlOriginal);
    const { largura, altura } = dimensionar(imagem.width, imagem.height, maxDimensao);

    const canvas = document.createElement('canvas');
    canvas.width = largura;
    canvas.height = altura;

    const contexto = canvas.getContext('2d');
    if (!contexto) {
      return dataUrlOriginal; // ambiente sem canvas (ex.: jsdom)
    }

    contexto.drawImage(imagem, 0, 0, largura, altura);
    return canvas.toDataURL('image/jpeg', qualidade);
  } catch {
    return dataUrlOriginal;
  }
}

/** Calcula as dimensões finais mantendo a proporção, limitadas por `maxDimensao`. */
export function dimensionar(
  largura: number,
  altura: number,
  maxDimensao: number,
): { largura: number; altura: number } {
  const maior = Math.max(largura, altura);
  if (maior <= maxDimensao) {
    return { largura, altura };
  }
  const escala = maxDimensao / maior;
  return {
    largura: Math.round(largura * escala),
    altura: Math.round(altura * escala),
  };
}

function lerComoDataUrl(arquivo: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const leitor = new FileReader();
    leitor.onload = () => resolve(leitor.result as string);
    leitor.onerror = () => reject(new Error('Falha ao ler a imagem.'));
    leitor.readAsDataURL(arquivo);
  });
}

function carregarImagem(dataUrl: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const imagem = new Image();
    imagem.onload = () => resolve(imagem);
    imagem.onerror = () => reject(new Error('Falha ao decodificar a imagem.'));
    imagem.src = dataUrl;
  });
}
