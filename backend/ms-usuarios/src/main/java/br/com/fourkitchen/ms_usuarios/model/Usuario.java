package br.com.fourkitchen.ms_usuarios.model;

import br.com.fourkitchen.ms_usuarios.enums.PerfilUsuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    @Column(name = "perfil", nullable = false)
    @Enumerated(EnumType.STRING)
    private PerfilUsuario perfilUsuario;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "ativo")
    private Boolean ativo;

}