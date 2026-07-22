import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, Subject, combineLatest, concat, merge, of, timer } from 'rxjs';
import { catchError, map, shareReplay, switchMap } from 'rxjs/operators';

import { environment } from '../../../../environments/environment';
import {
  EstadoGrafico,
  FILTROS_DASHBOARD_INICIAIS,
  FiltrosDashboard,
  PedidosCanalResponse,
  PeriodoRankingProdutos,
  ProblemasCozinhaMotivoResponse,
  RankingProdutosResponse,
  VolumePedidosHorarioResponse,
} from '../models/dashboard-graficos.models';

const INTERVALO_ATUALIZACAO_MS = 15_000;

@Injectable()
export class DashboardGraficosService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/gestor/dashboard`;
  private readonly filtrosVolumeSubject = new BehaviorSubject<FiltrosDashboard>({ ...FILTROS_DASHBOARD_INICIAIS });
  private readonly filtrosProblemasSubject = new BehaviorSubject<FiltrosDashboard>({ ...FILTROS_DASHBOARD_INICIAIS });
  private readonly filtrosCanaisSubject = new BehaviorSubject<FiltrosDashboard>({ ...FILTROS_DASHBOARD_INICIAIS });
  private readonly repetirVolumeSubject = new Subject<void>();
  private readonly repetirProblemasSubject = new Subject<void>();
  private readonly repetirCanaisSubject = new Subject<void>();
  private readonly periodoRankingSubject = new BehaviorSubject<PeriodoRankingProdutos>('ULTIMOS_30_DIAS');
  private readonly repetirRankingSubject = new Subject<void>();

  readonly volume$: Observable<EstadoGrafico<VolumePedidosHorarioResponse>> = this.criarFluxo(
    '/pedidos-por-horario',
    this.filtrosVolumeSubject,
    this.repetirVolumeSubject,
  );
  readonly problemas$: Observable<EstadoGrafico<ProblemasCozinhaMotivoResponse>> = this.criarFluxo(
    '/problemas-por-motivo',
    this.filtrosProblemasSubject,
    this.repetirProblemasSubject,
  );
  readonly canais$: Observable<EstadoGrafico<PedidosCanalResponse>> = this.criarFluxo(
    '/pedidos-por-canal',
    this.filtrosCanaisSubject,
    this.repetirCanaisSubject,
  );
  readonly rankingProdutos$: Observable<EstadoGrafico<RankingProdutosResponse>> = combineLatest([
    this.periodoRankingSubject,
    merge(timer(0, INTERVALO_ATUALIZACAO_MS), this.repetirRankingSubject),
  ]).pipe(
    switchMap(([periodo]) => concat(
      of<EstadoGrafico<RankingProdutosResponse>>({ status: 'carregando', dados: null }),
      this.http.get<RankingProdutosResponse>(`${this.baseUrl}/ranking-produtos`, {
        params: new HttpParams().set('periodo', periodo),
      }).pipe(
        map(dados => ({ status: 'sucesso', dados }) as EstadoGrafico<RankingProdutosResponse>),
        catchError((erro: HttpErrorResponse) => of<EstadoGrafico<RankingProdutosResponse>>({
          status: 'erro', dados: null, mensagem: this.mensagemErro(erro),
        })),
      ),
    )),
    shareReplay({ bufferSize: 1, refCount: true }),
  );

  atualizarFiltrosGraficos(filtros: FiltrosDashboard): void {
    this.atualizarFiltrosVolume(filtros);
    this.atualizarFiltrosProblemas(filtros);
    this.atualizarFiltrosCanais(filtros);
  }

  atualizarFiltrosVolume(filtros: FiltrosDashboard): void { this.atualizar(this.filtrosVolumeSubject, filtros); }
  atualizarFiltrosProblemas(filtros: FiltrosDashboard): void { this.atualizar(this.filtrosProblemasSubject, filtros); }
  atualizarFiltrosCanais(filtros: FiltrosDashboard): void { this.atualizar(this.filtrosCanaisSubject, filtros); }

  atualizarPeriodoRanking(periodo: PeriodoRankingProdutos): void {
    if (periodo !== this.periodoRankingSubject.value) this.periodoRankingSubject.next(periodo);
  }

  repetirVolume(): void { this.repetirVolumeSubject.next(); }
  repetirProblemas(): void { this.repetirProblemasSubject.next(); }
  repetirCanais(): void { this.repetirCanaisSubject.next(); }
  repetirRanking(): void { this.repetirRankingSubject.next(); }

  private criarFluxo<T>(
    caminho: string,
    filtros$: BehaviorSubject<FiltrosDashboard>,
    repetir$: Subject<void>,
  ): Observable<EstadoGrafico<T>> {
    return combineLatest([
      filtros$,
      merge(timer(0, INTERVALO_ATUALIZACAO_MS), repetir$),
    ]).pipe(
      switchMap(([filtros]) => concat(
        of<EstadoGrafico<T>>({ status: 'carregando', dados: null }),
        this.http.get<T>(`${this.baseUrl}${caminho}`, { params: this.parametros(filtros) }).pipe(
          map(dados => ({ status: 'sucesso', dados }) as EstadoGrafico<T>),
          catchError((erro: HttpErrorResponse) => of<EstadoGrafico<T>>({
            status: 'erro',
            dados: null,
            mensagem: this.mensagemErro(erro),
          })),
        ),
      )),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
  }

  private atualizar(subject: BehaviorSubject<FiltrosDashboard>, filtros: FiltrosDashboard): void {
    if (JSON.stringify(filtros) !== JSON.stringify(subject.value)) subject.next({ ...filtros });
  }

  private parametros(filtros: FiltrosDashboard): HttpParams {
    let params = new HttpParams().set('periodo', filtros.periodo);
    if (filtros.periodo === 'PERSONALIZADO') {
      params = params.set('dataInicial', filtros.dataInicial).set('dataFinal', filtros.dataFinal);
    }
    if (filtros.canal) params = params.set('canal', filtros.canal);
    if (filtros.idMesa !== null) params = params.set('idMesa', filtros.idMesa);
    if (filtros.status) params = params.set('status', filtros.status);
    return params;
  }

  private mensagemErro(erro: HttpErrorResponse): string {
    if (erro.status === 401 || erro.status === 403) return 'Você não possui autorização para visualizar estes dados.';
    if (erro.status === 503) return 'Este gráfico está temporariamente indisponível.';
    return 'Não foi possível carregar este gráfico.';
  }
}
