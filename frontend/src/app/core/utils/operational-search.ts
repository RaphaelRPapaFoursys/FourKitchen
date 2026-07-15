/** Normaliza texto de busca sem diferenciar maiúsculas, acentos ou espaços. */
export function normalizarBuscaOperacional(valor: unknown): string {
  return String(valor ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase()
    .replace(/\s+/g, '')
    .trim();
}

/** Aceita prefixos operacionais como "Mesa 01", "Pedido 0007" e "#8". */
export function numeroBuscaOperacional(valor: unknown): string | null {
  const busca = normalizarBuscaOperacional(valor);
  const semPrefixo = busca.replace(/^(?:mesa|pedido|atendimento|atend|numero|n|#)/, '');

  if (!/^\d+$/.test(semPrefixo)) {
    return null;
  }

  return semPrefixo.replace(/^0+(?=\d)/, '');
}

export function numeroContemBusca(numero: number | null | undefined, busca: unknown): boolean {
  const numeroBusca = numeroBuscaOperacional(busca);
  return numero !== null
    && numero !== undefined
    && numeroBusca !== null
    && String(numero).includes(numeroBusca);
}

/** Retorna somente os dígitos usados na busca pelo número da mesa. */
export function numeroMesaBusca(valor: unknown): string | null {
  const busca = normalizarBuscaOperacional(valor).replace(/^mesa/, '').replace(/\D/g, '');

  return busca === '' ? null : busca;
}

/** Um dígito busca parcialmente; dois ou mais dígitos identificam a mesa exatamente. */
export function mesaCorrespondeBuscaParcial(numero: number | null | undefined, busca: unknown): boolean {
  if (numero === null || numero === undefined) {
    return false;
  }

  const termo = numeroMesaBusca(busca);
  if (termo === null) {
    return false;
  }

  const numeroFormatado = String(numero).padStart(2, '0');
  return termo.length === 1
    ? numeroFormatado.includes(termo)
    : numeroFormatado === termo;
}

/** Busca operacional usada na fila da cozinha, sem depender de IDs internos. */
export function correspondeBuscaFilaCozinha(
  codigoPedido: number | null | undefined,
  origem: string | null | undefined,
  valor: unknown,
): boolean {
  const busca = normalizarBuscaOperacional(valor);

  if (!busca) {
    return true;
  }

  const origemNormalizada = normalizarBuscaOperacional(origem);
  const buscaIdentificada = busca.match(/^(mesa|totem|pedido|#)(\d*)$/);

  if (buscaIdentificada) {
    const [, tipo, numero] = buscaIdentificada;

    if (tipo === 'pedido' || tipo === '#') {
      return numero !== '' && codigoPedidoCorresponde(codigoPedido, numero);
    }

    return origemCorresponde(tipo, numero, origemNormalizada);
  }

  if (/^\d+$/.test(busca)) {
    return codigoPedidoCorresponde(codigoPedido, busca)
      || origemCorrespondeNumero(busca, origemNormalizada);
  }

  return origemNormalizada.includes(busca);
}

function codigoPedidoCorresponde(codigoPedido: number | null | undefined, busca: string): boolean {
  return codigoPedido !== null
    && codigoPedido !== undefined
    && String(codigoPedido).includes(busca);
}

function origemCorresponde(tipo: string, numero: string, origem: string): boolean {
  if (!origem.startsWith(tipo)) {
    return false;
  }

  return numero === '' || origemCorrespondeNumero(numero, origem);
}

function origemCorrespondeNumero(busca: string, origem: string): boolean {
  const numeroOrigem = origem.match(/\d+$/)?.[0];

  if (!numeroOrigem) {
    return false;
  }

  return normalizarNumeroOperacional(numeroOrigem) === normalizarNumeroOperacional(busca);
}

function normalizarNumeroOperacional(numero: string): string {
  return numero.replace(/^0+(?=\d)/, '');
}
