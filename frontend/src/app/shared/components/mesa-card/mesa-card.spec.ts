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
    fixture.componentRef.setInput('acaoPrimaria', { tipo: 'MARCAR_ENTREGUE', label: 'Marcar entregue' });
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
});
