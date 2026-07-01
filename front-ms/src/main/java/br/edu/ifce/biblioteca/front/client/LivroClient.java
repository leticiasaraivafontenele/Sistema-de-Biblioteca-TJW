package br.edu.ifce.biblioteca.front.client;

import br.edu.ifce.biblioteca.front.model.Livro;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class LivroClient {

    private static final String SERVICO = "livro-ms";

    private final RestClient client;
    private final ObjectMapper objectMapper;

    public LivroClient(RestClient livroRestClient, ObjectMapper objectMapper) {
        this.client = livroRestClient;
        this.objectMapper = objectMapper;
    }

    public List<Livro> listar(Boolean disponivel) {
        try {
            return client.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/livros");
                        if (disponivel != null) {
                            uriBuilder.queryParam("disponivel", disponivel);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Livro>>() {});
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public Optional<Livro> buscarPorId(Long id) {
        try {
            Livro livro = client.get()
                    .uri("/api/livros/{id}", id)
                    .retrieve()
                    .body(Livro.class);
            return Optional.ofNullable(livro);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public Livro criar(Livro livro) {
        try {
            return client.post()
                    .uri("/api/livros")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(livro)
                    .retrieve()
                    .body(Livro.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw converterValidacao(e);
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public Livro atualizar(Long id, Livro livro) {
        try {
            return client.put()
                    .uri("/api/livros/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(livro)
                    .retrieve()
                    .body(Livro.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw converterValidacao(e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNaoEncontradoException("Livro não encontrado: " + id);
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public void excluir(Long id) {
        try {
            client.delete()
                    .uri("/api/livros/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNaoEncontradoException("Livro não encontrado: " + id);
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    private ApiValidacaoException converterValidacao(HttpClientErrorException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        try {
            JsonNode raiz = objectMapper.readTree(e.getResponseBodyAsString());
            JsonNode noErros = raiz.get("erros");
            if (noErros != null && noErros.isObject()) {
                noErros.fields().forEachRemaining(campo -> erros.put(campo.getKey(), campo.getValue().asText()));
            } else if (raiz.get("erro") != null) {
                erros.put("erro", raiz.get("erro").asText());
            }
        } catch (Exception ignore) {
        }
        if (erros.isEmpty()) {
            erros.put("erro", "Dados inválidos");
        }
        return new ApiValidacaoException(erros);
    }
}
