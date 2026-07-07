package br.com.fourkitchen.ms_mesas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_atendimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_atendimento", nullable = false, unique = true)
    private Integer idAtendimento;

    @Column(name = "codigo_sessao", nullable = false)
    private Integer codigoSessao;

    @Column(name = "id_mesa", nullable = false)
    private Integer idMesa;

    @Column(name = "numero_mesa", nullable = false)
    private Integer numeroMesa;

    @Column(name = "id_garcom")
    private Integer idGarcom;

    @Column(name = "nome_garcom")
    private String nomeGarcom;

    @Column(name = "valor_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "total_pedidos", nullable = false)
    private Integer totalPedidos;

    @Column(name = "total_itens", nullable = false)
    private Integer totalItens;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento", nullable = false)
    private LocalDateTime dataFechamento;

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
