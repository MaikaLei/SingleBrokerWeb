package controller;

import dao.AgendaDao;
import java.time.LocalDate;
import java.util.List;
import model.AgendaModel;
import service.AgendaService;

public class AgendaController {
    private final AgendaService service;

    public AgendaController() { this(new AgendaService(new AgendaDao())); }
    public AgendaController(AgendaService service) { this.service = java.util.Objects.requireNonNull(service); }

    public void salvar(AgendaModel tarefa) { service.salvar(tarefa); }
    public List<AgendaModel> listar(LocalDate inicio, LocalDate fim) { return service.listar(inicio, fim); }
    public void excluir(Long id) { service.excluir(id); }
}
