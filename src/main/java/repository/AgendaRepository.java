package repository;

import java.time.LocalDate;
import java.util.List;
import model.AgendaModel;

public interface AgendaRepository {
    List<AgendaModel> listar(LocalDate inicio, LocalDate fim);
    void salvar(AgendaModel tarefa);
    void excluir(Long id);
}
