package br.edu.ifce.biblioteca.livro.exception;

public class LivroNaoEncontradoException extends RuntimeException {

    public LivroNaoEncontradoException(Long id) {
        super("Livro não encontrado: " + id);
    }
}
