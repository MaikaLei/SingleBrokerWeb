package repository;

import model.MinhaPaginaModel;

public interface MinhaPaginaRepository {
    MinhaPaginaModel carregar();
    void salvar(MinhaPaginaModel pagina);
}
