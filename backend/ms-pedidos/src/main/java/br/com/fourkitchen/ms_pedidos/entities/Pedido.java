package br.com.fourkitchen.ms_pedidos.entities;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codigo", nullable = false)
    private Integer codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", length = 50, nullable = false)
    private CanaisPedido canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private StatusPedido status = StatusPedido.ENVIADO_COZINHA;

    @Column(name = "id_mesa")
    private Integer idMesa;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_atendimento")
    private Integer idAtendimento;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_inicio_preparo")
    private LocalDateTime dataInicioPreparo;

    @Column(name = "data_pronto")
    private LocalDateTime dataPronto;
}
