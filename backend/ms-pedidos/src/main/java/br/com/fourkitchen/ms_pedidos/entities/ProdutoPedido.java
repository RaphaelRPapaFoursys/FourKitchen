package br.com.fourkitchen.ms_pedidos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "produtos_pedidos")
public class ProdutoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    @Column(name = "id_produto", nullable = false)
    private Integer idProduto;

    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "observacao")
    private String observacao;
}
