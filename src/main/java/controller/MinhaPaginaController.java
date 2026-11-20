package controller;

import model.MinhaPaginaModel;
import service.MinhaPaginaService;

public class MinhaPaginaController {
    private final MinhaPaginaService service;
    public MinhaPaginaController() { this(new MinhaPaginaService(new dao.MinhaPaginaDao())); }
    public MinhaPaginaController(MinhaPaginaService service) { this.service = java.util.Objects.requireNonNull(service); }
    public MinhaPaginaModel carregar() { return service.carregar(); }
    public void salvar(MinhaPaginaModel pagina) { service.salvar(pagina); }
}
