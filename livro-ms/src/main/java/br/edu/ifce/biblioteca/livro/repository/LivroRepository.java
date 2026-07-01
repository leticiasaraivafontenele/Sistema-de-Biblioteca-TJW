package br.edu.ifce.biblioteca.livro.repository;

import br.edu.ifce.biblioteca.livro.model.Livro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LivroRepository extends JpaRepository<Livro, Long> {

    List<Livro> findByDisponivel(Boolean disponivel);
}
