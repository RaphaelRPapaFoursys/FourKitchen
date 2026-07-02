import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PainelService } from '../../core/services/painel';
import { Gestor } from './gestor';

describe('Gestor', () => {
  let component: Gestor;
  let fixture: ComponentFixture<Gestor>;
  let painelService: PainelService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Gestor],
    }).compileComponents();

    painelService = TestBed.inject(PainelService);
    fixture = TestBed.createComponent(Gestor);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  function fecharTodasAsContas(): void {
    for (const mesa of painelService.mesas()) {
      if (mesa.status === 'OCUPADA') {
        painelService.fecharConta(mesa.numero);
      }
    }
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('lista os últimos pedidos do mais recente para o mais antigo', () => {
    const pedidos = painelService.ultimosPedidos();

    expect(pedidos.length).toBeGreaterThan(1);
    for (let i = 1; i < pedidos.length; i++) {
      expect(pedidos[i - 1].minutosAtras).toBeLessThanOrEqual(pedidos[i].minutosAtras);
    }
  });

  it('mantém o total da conta no card ao fechar a conta', () => {
    const mesaAntes = painelService.mesas().find(mesa => mesa.numero === 3)!;
    const totalAntes = painelService.valorContaMesa(mesaAntes);

    painelService.fecharConta(3);

    const mesaDepois = painelService.mesas().find(mesa => mesa.numero === 3)!;
    expect(mesaDepois.status).toBe('FECHADA');
    expect(painelService.valorContaMesa(mesaDepois)).toBe(totalAntes);
  });

  it('preserva a receita no resumo do expediente após fechar conta e liberar a mesa', () => {
    const valorTotalAntes = painelService.resumoExpediente().valorTotal;

    painelService.fecharConta(3);
    painelService.liberarMesa(3);

    expect(painelService.resumoExpediente().valorTotal).toBe(valorTotalAntes);
  });

  it('abre mesa já atribuída a um garçom', () => {
    painelService.abrirMesa(8, 'Julia');

    const mesa = painelService.mesas().find(item => item.numero === 8)!;
    expect(mesa.status).toBe('OCUPADA');
    expect(mesa.garcom).toBe('Julia');
  });

  it('bloqueia o fechamento do expediente enquanto houver contas abertas', () => {
    expect(painelService.mesasComContaAberta()).toBeGreaterThan(0);
    expect(painelService.podeFecharExpediente()).toBeFalse();

    painelService.fecharExpediente();
    expect(painelService.expedienteFechado()).toBeFalse();
  });

  it('permite fechar e reabrir o expediente quando não há pendências', () => {
    fecharTodasAsContas();
    expect(painelService.podeFecharExpediente()).toBeTrue();

    painelService.fecharExpediente();
    expect(painelService.expedienteFechado()).toBeTrue();

    painelService.reabrirExpediente();
    expect(painelService.expedienteFechado()).toBeFalse();
  });

  it('não permite mutações nas mesas com o expediente fechado', () => {
    fecharTodasAsContas();
    painelService.fecharExpediente();

    painelService.liberarMesa(4);
    expect(painelService.mesas().find(mesa => mesa.numero === 4)!.status).toBe('FECHADA');

    painelService.reabrirExpediente();
    painelService.liberarMesa(4);
    expect(painelService.mesas().find(mesa => mesa.numero === 4)!.status).toBe('LIVRE');
  });

  it('deriva os garçons disponíveis da carga real', () => {
    const resumo = painelService.resumo();
    const disponiveis = painelService.cargaGarcons().filter(garcom => garcom.mesasAtivas <= 2).length;

    expect(resumo.garconsDisponiveis).toBe(disponiveis);
  });
});
