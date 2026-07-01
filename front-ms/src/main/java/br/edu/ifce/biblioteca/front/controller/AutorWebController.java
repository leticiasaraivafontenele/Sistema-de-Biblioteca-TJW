package br.edu.ifce.biblioteca.front.controller;

import br.edu.ifce.biblioteca.front.client.ApiValidacaoException;
import br.edu.ifce.biblioteca.front.client.AutorClient;
import br.edu.ifce.biblioteca.front.client.RecursoNaoEncontradoException;
import br.edu.ifce.biblioteca.front.client.ServicoIndisponivelException;
import br.edu.ifce.biblioteca.front.model.Autor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutorWebController {

    private final AutorClient autorClient;

    public AutorWebController(AutorClient autorClient) {
        this.autorClient = autorClient;
    }

    @GetMapping("/autores")
    public String listar(Model model) {
        try {
            model.addAttribute("autores", autorClient.listar());
        } catch (ServicoIndisponivelException e) {
            model.addAttribute("autores", List.of());
            model.addAttribute("erro", e.getMessage());
        }
        return "autores/lista";
    }

    @GetMapping("/autores/novo")
    public String novo(Model model) {
        model.addAttribute("autor", new Autor());
        return "autores/formulario";
    }

    @GetMapping("/autores/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return autorClient.buscarPorId(id)
                .map(autor -> {
                    model.addAttribute("autor", autor);
                    return "autores/formulario";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("erro", "Autor não encontrado: " + id);
                    return "redirect:/autores";
                });
    }

    @PostMapping("/autores")
    public String criar(@Valid @ModelAttribute("autor") Autor autor, BindingResult result,
                        Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "autores/formulario";
        }
        try {
            autorClient.criar(autor);
        } catch (ApiValidacaoException e) {
            model.addAttribute("errosApi", e.getErros().values());
            return "autores/formulario";
        }
        ra.addFlashAttribute("sucesso", "Autor cadastrado com sucesso!");
        return "redirect:/autores";
    }

    @PostMapping("/autores/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("autor") Autor autor,
                           BindingResult result, Model model, RedirectAttributes ra) {
        autor.setId(id);
        if (result.hasErrors()) {
            return "autores/formulario";
        }
        try {
            autorClient.atualizar(id, autor);
        } catch (ApiValidacaoException e) {
            model.addAttribute("errosApi", e.getErros().values());
            return "autores/formulario";
        } catch (RecursoNaoEncontradoException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/autores";
        }
        ra.addFlashAttribute("sucesso", "Autor atualizado com sucesso!");
        return "redirect:/autores";
    }

    @PostMapping("/autores/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            autorClient.excluir(id);
            ra.addFlashAttribute("sucesso", "Autor excluído com sucesso!");
        } catch (RecursoNaoEncontradoException | ServicoIndisponivelException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/autores";
    }
}
