package repository;

import java.util.List;
import model.FotoModel;

public interface FotoRepository {
    List<FotoModel> listar(Long imovelId);
    void salvar(Long imovelId, List<FotoModel> novos, List<Long> removidos);
}
