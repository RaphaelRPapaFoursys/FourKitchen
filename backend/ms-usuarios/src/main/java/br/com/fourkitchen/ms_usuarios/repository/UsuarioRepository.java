package br.com.fourkitchen.ms_usuarios.repository;

import br.com.fourkitchen.ms_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    List<Usuario> findByAtivoTrue();

    boolean existsByEmail(String email);
}
