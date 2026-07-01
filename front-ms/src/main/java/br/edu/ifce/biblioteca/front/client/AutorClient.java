package br.edu.ifce.biblioteca.front.client;

import br.edu.ifce.biblioteca.front.model.Autor;
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
public class AutorClient {

    private static final String SERVICO = "autor-ms";

    private final RestClient client;
    private final ObjectMapper objectMapper;

    public AutorClient(RestClient autorRestClient, ObjectMapper objectMapper) {
        this.client = autorRestClient;
        this.objectMapper = objectMapper;
    }

    public List<Autor> listar() {
        try {
            return client.get()
                    .uri("/api/autores")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Autor>>() {});
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public Optional<Autor> buscarPorId(Long id) {
        try {
            Autor autor = client.get()
                    .uri("/api/autores/{id}", id)
                    .retrieve()
                    .body(Autor.class);
            return Optional.ofNullable(autor);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public Autor criar(Autor autor) {
        try {
            return client.post()
                    .uri("/api/autores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(autor)
                    .retrieve()
                    .body(Autor.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw converterValidacao(e);
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public Autor atualizar(Long id, Autor autor) {
        try {
            return client.put()
                    .uri("/api/autores/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(autor)
                    .retrieve()
                    .body(Autor.class);
        } catch (HttpClientErrorException.BadRequest e) {
            throw converterValidacao(e);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNaoEncontradoException("Autor não encontrado: " + id);
        } catch (ResourceAccessException e) {
            throw new ServicoIndisponivelException(SERVICO);
        }
    }

    public void excluir(Long id) {
        try {
            client.delete()
                    .uri("/api/autores/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNaoEncontradoException("Autor não encontrado: " + id);
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
