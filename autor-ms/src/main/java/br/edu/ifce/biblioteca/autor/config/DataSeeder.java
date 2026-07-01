package br.edu.ifce.biblioteca.autor.config;

import br.edu.ifce.biblioteca.autor.model.Autor;
import br.edu.ifce.biblioteca.autor.repository.AutorRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AutorRepository repository;

    public DataSeeder(AutorRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new Autor("Machado de Assis", "Brasileira", 1839),
                new Autor("Clarice Lispector", "Brasileira", 1920),
                new Autor("George Orwell", "Britânica", 1903)
        ));
    }
}
