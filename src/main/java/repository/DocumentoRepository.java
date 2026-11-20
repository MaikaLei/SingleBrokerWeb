package repository;

import java.util.List;
import model.DocumentoModel;

public interface DocumentoRepository {
    List<DocumentoModel> listar(Long imovelId);
    void salvar(Long imovelId, List<DocumentoModel> novos, List<Long> removidos);
}
