package br.edu.ifce.biblioteca.livro.config;

import br.edu.ifce.biblioteca.livro.model.Livro;
import br.edu.ifce.biblioteca.livro.repository.LivroRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final LivroRepository repository;

    public DataSeeder(LivroRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Livro("Dom Casmurro", "Romance", 1899, true, 1L),
                new Livro("Memórias Póstumas de Brás Cubas", "Romance", 1881, true, 1L),
                new Livro("A Hora da Estrela", "Romance", 1977, false, 2L),
                new Livro("1984", "Distopia", 1949, true, 3L),
                new Livro("A Revolução dos Bichos", "Fábula", 1945, true, 3L)
        ));
    }
}
