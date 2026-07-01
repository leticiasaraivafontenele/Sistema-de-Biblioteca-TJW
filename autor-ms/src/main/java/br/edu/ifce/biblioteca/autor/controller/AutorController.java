package br.edu.ifce.biblioteca.autor.controller;

import br.edu.ifce.biblioteca.autor.exception.AutorNaoEncontradoException;
import br.edu.ifce.biblioteca.autor.model.Autor;
import br.edu.ifce.biblioteca.autor.repository.AutorRepository;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorRepository repository;

    public AutorController(AutorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Autor> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Autor buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AutorNaoEncontradoException(id));
    }

    @PostMapping
    public ResponseEntity<Autor> criar(@Valid @RequestBody Autor autor, UriComponentsBuilder uriBuilder) {
        autor.setId(null);
        Autor salvo = repository.save(autor);
        URI uri = uriBuilder.path("/api/autores/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public Autor atualizar(@PathVariable Long id, @Valid @RequestBody Autor dados) {
        Autor autor = repository.findById(id)
                .orElseThrow(() -> new AutorNaoEncontradoException(id));
        autor.setNome(dados.getNome());
        autor.setNacionalidade(dados.getNacionalidade());
        autor.setAnoNascimento(dados.getAnoNascimento());
        return repository.save(autor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new AutorNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
