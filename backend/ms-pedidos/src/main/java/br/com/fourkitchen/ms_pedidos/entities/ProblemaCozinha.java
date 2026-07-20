package br.com.fourkitchen.ms_pedidos.entities;

import br.com.fourkitchen.ms_pedidos.enums.StatusProdutoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "problemas_cozinha")
public class ProblemaCozinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    @Column(name = "id_produto_pedido", nullable = false)
    private Integer idProdutoPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 50)
    private StatusProdutoPedido motivo;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_resolucao")
    private LocalDateTime dataResolucao;
}
