package service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import model.AgendaModel;
import repository.AgendaRepository;
import util.Validador;

public class AgendaService {
    private final AgendaRepository tarefas;

    public AgendaService(AgendaRepository tarefas) { this.tarefas = Objects.requireNonNull(tarefas); }

    public void salvar(AgendaModel tarefa) {
        Objects.requireNonNull(tarefa, "Informe a tarefa.");
        if (tarefa.getData() == null || tarefa.getHorario() == null)
            throw new IllegalArgumentException("Informe a data e o horário.");
        tarefa.setTipo(Validador.obrigatorio(tarefa.getTipo(), "o tipo"));
        tarefa.setDescricao(Validador.obrigatorio(tarefa.getDescricao(), "a descrição"));
        if (tarefa.getDescricao().length() > 2000)
            throw new IllegalArgumentException("A descrição deve ter até 2000 caracteres.");
        tarefas.salvar(tarefa);
    }

    public List<AgendaModel> listar(LocalDate inicio, LocalDate fim) { return tarefas.listar(inicio, fim); }
    public void excluir(Long id) { tarefas.excluir(id); }
}
