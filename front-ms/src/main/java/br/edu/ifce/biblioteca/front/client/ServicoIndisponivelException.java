package br.edu.ifce.biblioteca.front.client;

public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String servico) {
        super("O serviço " + servico + " está indisponível no momento. Tente novamente mais tarde.");
    }
}
