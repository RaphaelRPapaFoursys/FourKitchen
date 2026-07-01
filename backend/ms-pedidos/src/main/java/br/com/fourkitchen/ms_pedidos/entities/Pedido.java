package br.com.fourkitchen.ms_pedidos.entities;

import br.com.fourkitchen.ms_pedidos.enums.CanaisPedido;
import br.com.fourkitchen.ms_pedidos.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "id_mesa", nullable = false)
    private Integer idMesa;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_atendimento")
    private Integer idAtendimento;
}
