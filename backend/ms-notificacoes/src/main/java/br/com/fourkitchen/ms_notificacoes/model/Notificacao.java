package br.com.fourkitchen.ms_notificacoes.model;

import br.com.fourkitchen.ms_notificacoes.enums.DestinoNotificacao;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class
Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column(name = "mensagem", nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "destino", nullable = false, length = 100)
    private DestinoNotificacao destino;

    @Column(name = "lida", nullable = false)
    private Boolean lida;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;
}
