package br.edu.ifce.biblioteca.autor.repository;

import br.edu.ifce.biblioteca.autor.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
