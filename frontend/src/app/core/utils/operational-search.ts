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
