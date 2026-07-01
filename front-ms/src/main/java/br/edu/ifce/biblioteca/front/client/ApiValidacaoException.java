package br.edu.ifce.biblioteca.front.client;

import java.util.Map;

public class ApiValidacaoException extends RuntimeException {

    private final transient Map<String, String> erros;

    public ApiValidacaoException(Map<String, String> erros) {
        super("Erro de validação retornado pela API");
        this.erros = erros;
    }

    public Map<String, String> getErros() {
        return erros;
    }
}
