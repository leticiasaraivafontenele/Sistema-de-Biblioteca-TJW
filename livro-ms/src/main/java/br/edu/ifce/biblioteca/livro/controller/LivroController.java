package br.edu.ifce.biblioteca.livro.controller;

import br.edu.ifce.biblioteca.livro.exception.LivroNaoEncontradoException;
import br.edu.ifce.biblioteca.livro.model.Livro;
import br.edu.ifce.biblioteca.livro.repository.LivroRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroRepository repository;

    public LivroController(LivroRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Livro> listar(@RequestParam(required = false) Boolean disponivel) {
        if (disponivel == null) {
            return repository.findAll();
        }
        return repository.findByDisponivel(disponivel);
    }

    @GetMapping("/{id}")
    public Livro buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(id));
    }

    @PostMapping
    public ResponseEntity<Livro> criar(@Valid @RequestBody Livro livro, UriComponentsBuilder uriBuilder) {
        livro.setId(null);
        Livro salvo = repository.save(livro);
        URI uri = uriBuilder.path("/api/livros/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public Livro atualizar(@PathVariable Long id, @Valid @RequestBody Livro dados) {
        Livro livro = repository.findById(id)
                .orElseThrow(() -> new LivroNaoEncontradoException(id));
        livro.setTitulo(dados.getTitulo());
        livro.setGenero(dados.getGenero());
        livro.setAnoPublicacao(dados.getAnoPublicacao());
        livro.setDisponivel(dados.getDisponivel());
        livro.setAutorId(dados.getAutorId());
        return repository.save(livro);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new LivroNaoEncontradoException(id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
