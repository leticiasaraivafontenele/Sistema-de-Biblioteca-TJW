package br.edu.ifce.biblioteca.autor.exception;

public class AutorNaoEncontradoException extends RuntimeException {

    public AutorNaoEncontradoException(Long id) {
        super("Autor não encontrado: " + id);
    }
}
