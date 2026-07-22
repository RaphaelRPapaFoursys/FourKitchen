import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesaPainel } from '../../../core/models/painel.models';
import { MesaCard } from './mesa-card';

function mesaOcupada(): MesaPainel {
  return {
    id: 1,
    numero: 5,
    status: 'OCUPADA',
    garcomId: 10,
    garcom: 'Ana',
    abertaEm: null,
    statusPedido: 'PRONTO_ENTREGA',
    tempoLabel: 'Aberta há',
    tempoMinutos: 12,
    etapaAtual: 4,
    totalEtapas: 4,
    pedidos: [
      { id: 1, status: 'PRONTO', valor: 50, criadoMinutosAtras: 12, tempoPreparoMinutos: null, totalItens: 3 },
    ],
  };
}

describe('MesaCard', () => {
  let component: MesaCard;
  let fixture: ComponentFixture<MesaCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesaCard],
    }).compileComponents();

    fixture = TestBed.createComponent(MesaCard);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('mesa', mesaOcupada());
    fixture.componentRef.setInput('criticidade', 'critico');
    fixture.componentRef.setInput('acaoPrimaria', { tipo: 'FECHAR_CONTA', label: 'Fechar conta' });
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('exibe o número da mesa com dois dígitos', () => {
    const titulo: HTMLElement = fixture.nativeElement.querySelector('.mesa-card__header h3');
    expect(titulo.textContent).toContain('Mesa 05');
  });

  it('emite acao ao clicar na ação primária', () => {
    const emitido = vi.fn();
    component.acao.subscribe(emitido);

    const botao: HTMLButtonElement = fixture.nativeElement.querySelector('.mesa-card__acao');
    botao.click();

    expect(emitido).toHaveBeenCalledOnce();
  });

  it('emite trocar ao clicar em Trocar', () => {
    const emitido = vi.fn();
    component.trocar.subscribe(emitido);

    const botao: HTMLButtonElement = fixture.nativeElement.querySelector('.mesa-card__trocar-garcom');
    botao.click();

    expect(emitido).toHaveBeenCalledOnce();
  });

  it('não exibe quantidade de itens nem valor da conta diretamente no card', () => {
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(texto).not.toContain('3 itens');
    expect(texto).not.toContain('R$ 50,00');
  });

  it('emite verPedido ao clicar na ação secundária de detalhes', () => {
    const emitido = vi.fn();
    component.verPedido.subscribe(emitido);

    const botoes = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll<HTMLButtonElement>('.mesa-card__acao'));
    const botaoDetalhes = botoes.find(botao => botao.textContent?.includes('Ver pedido'));
    botaoDetalhes?.click();

    expect(emitido).toHaveBeenCalledOnce();
  });
});
