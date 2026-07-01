package br.edu.ifce.biblioteca.front.controller;

import br.edu.ifce.biblioteca.front.client.ServicoIndisponivelException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(ServicoIndisponivelException.class)
    public String tratarServicoIndisponivel(ServicoIndisponivelException e, Model model) {
        model.addAttribute("mensagem", e.getMessage());
        return "error";
    }
}
