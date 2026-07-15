import { correspondeBuscaFilaCozinha } from './operational-search';

describe('correspondeBuscaFilaCozinha', () => {
  it('encontra a mesa pelo prefixo, com ou sem zero à esquerda', () => {
    expect(correspondeBuscaFilaCozinha(701366, 'Mesa 02', 'mesa 02')).toBe(true);
    expect(correspondeBuscaFilaCozinha(701366, 'Mesa 02', 'mesa2')).toBe(true);
    expect(correspondeBuscaFilaCozinha(701366, 'Mesa 02', '02')).toBe(true);
  });

  it('encontra o totem pelo nome operacional', () => {
    expect(correspondeBuscaFilaCozinha(701366, 'Totem 01', 'totem1')).toBe(true);
    expect(correspondeBuscaFilaCozinha(701366, 'Totem 01', 'totem 02')).toBe(false);
  });

  it('encontra o código visível do pedido inteiro ou parcialmente', () => {
    expect(correspondeBuscaFilaCozinha(701366, 'Mesa 02', '#701366')).toBe(true);
    expect(correspondeBuscaFilaCozinha(701366, 'Mesa 02', '136')).toBe(true);
    expect(correspondeBuscaFilaCozinha(701366, 'Mesa 02', 'pedido 999')).toBe(false);
  });
});
