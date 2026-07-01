package br.edu.ifce.biblioteca.front.controller;

import br.edu.ifce.biblioteca.front.client.ApiValidacaoException;
import br.edu.ifce.biblioteca.front.client.AutorClient;
import br.edu.ifce.biblioteca.front.client.LivroClient;
import br.edu.ifce.biblioteca.front.client.RecursoNaoEncontradoException;
import br.edu.ifce.biblioteca.front.client.ServicoIndisponivelException;
import br.edu.ifce.biblioteca.front.model.Autor;
import br.edu.ifce.biblioteca.front.model.Livro;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LivroWebController {

    private final LivroClient livroClient;
    private final AutorClient autorClient;

    public LivroWebController(LivroClient livroClient, AutorClient autorClient) {
        this.livroClient = livroClient;
        this.autorClient = autorClient;
    }

    @GetMapping("/livros")
    public String listar(@RequestParam(required = false) Boolean disponivel, Model model) {
        model.addAttribute("disponivel", disponivel);
        try {
            List<Livro> livros = livroClient.listar(disponivel);
            Map<Long, String> cache = new HashMap<>();
            for (Livro livro : livros) {
                livro.setNomeAutor(resolverNomeAutor(livro.getAutorId(), cache));
            }
            model.addAttribute("livros", livros);
        } catch (ServicoIndisponivelException e) {
            model.addAttribute("livros", List.of());
            model.addAttribute("erro", e.getMessage());
        }
        return "livros/lista";
    }

    @GetMapping("/livros/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        Livro livro = livroClient.buscarPorId(id).orElse(null);
        if (livro == null) {
            model.addAttribute("mensagem", "Livro não encontrado: " + id);
            return "error";
        }
        livro.setNomeAutor(resolverNomeAutor(livro.getAutorId(), new HashMap<>()));
        model.addAttribute("livro", livro);
        return "livros/detalhe";
    }

    @GetMapping("/livros/novo")
    public String novo(Model model) {
        model.addAttribute("livro", new Livro());
        model.addAttribute("autores", carregarAutores(model));
        return "livros/formulario";
    }

    @GetMapping("/livros/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Livro livro = livroClient.buscarPorId(id).orElse(null);
        if (livro == null) {
            ra.addFlashAttribute("erro", "Livro não encontrado: " + id);
            return "redirect:/livros";
        }
        model.addAttribute("livro", livro);
        model.addAttribute("autores", carregarAutores(model));
        return "livros/formulario";
    }

    @PostMapping("/livros")
    public String criar(@Valid @ModelAttribute("livro") Livro livro, BindingResult result,
                        Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("autores", carregarAutores(model));
            return "livros/formulario";
        }
        try {
            livroClient.criar(livro);
        } catch (ApiValidacaoException e) {
            model.addAttribute("errosApi", e.getErros().values());
            model.addAttribute("autores", carregarAutores(model));
            return "livros/formulario";
        }
        ra.addFlashAttribute("sucesso", "Livro cadastrado com sucesso!");
        return "redirect:/livros";
    }

    @PostMapping("/livros/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("livro") Livro livro,
                           BindingResult result, Model model, RedirectAttributes ra) {
        livro.setId(id);
        if (result.hasErrors()) {
            model.addAttribute("autores", carregarAutores(model));
            return "livros/formulario";
        }
        try {
            livroClient.atualizar(id, livro);
        } catch (ApiValidacaoException e) {
            model.addAttribute("errosApi", e.getErros().values());
            model.addAttribute("autores", carregarAutores(model));
            return "livros/formulario";
        } catch (RecursoNaoEncontradoException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/livros";
        }
        ra.addFlashAttribute("sucesso", "Livro atualizado com sucesso!");
        return "redirect:/livros";
    }

    @PostMapping("/livros/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            livroClient.excluir(id);
            ra.addFlashAttribute("sucesso", "Livro excluído com sucesso!");
        } catch (RecursoNaoEncontradoException | ServicoIndisponivelException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/livros";
    }

    private String resolverNomeAutor(Long autorId, Map<Long, String> cache) {
        if (autorId == null) {
            return "Autor removido";
        }
        return cache.computeIfAbsent(autorId, id -> {
            try {
                return autorClient.buscarPorId(id)
                        .map(Autor::getNome)
                        .orElse("Autor removido");
            } catch (ServicoIndisponivelException e) {
                return "Autor indisponível";
            }
        });
    }

    private List<Autor> carregarAutores(Model model) {
        try {
            return autorClient.listar();
        } catch (ServicoIndisponivelException e) {
            model.addAttribute("erro", e.getMessage());
            return List.of();
        }
    }
}
