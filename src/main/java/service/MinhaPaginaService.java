package service;

import java.util.Objects;
import model.MinhaPaginaModel;
import repository.MinhaPaginaRepository;
import util.Validador;

public class MinhaPaginaService {
    private final MinhaPaginaRepository paginas;

    public MinhaPaginaService(MinhaPaginaRepository paginas) { this.paginas = Objects.requireNonNull(paginas); }

    public MinhaPaginaModel carregar() { return paginas.carregar(); }

    public void salvar(MinhaPaginaModel pagina) {
        pagina.setEmail(Validador.email(pagina.getEmail(), false));
        if (pagina.getTitulo().length() > 500 || pagina.getDescricao().length() > 2000 || pagina.getBibliografia().length() > 5000)
            throw new IllegalArgumentException("Limites: título 500, descrição 2000 e bibliografia 5000 caracteres.");
        paginas.salvar(pagina);
    }
}
