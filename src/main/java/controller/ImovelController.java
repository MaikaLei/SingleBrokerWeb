package controller;

import dao.ClienteDao;
import dao.ImovelDao;
import java.time.Clock;
import java.util.List;
import model.ImovelModel;
import service.ImovelService;

public class ImovelController {
    private final ImovelService service;

    public ImovelController() {
        this(new ImovelService(new ImovelDao(), new ClienteDao(), util.SessaoUsuario::getUsuarioLogado, Clock.systemDefaultZone()));
    }

    public ImovelController(ImovelService service) { this.service = java.util.Objects.requireNonNull(service); }

    public void salvar(ImovelModel imovel) { service.salvar(imovel); }
    public ImovelModel buscarPorId(Long id) { return service.buscarPorId(id); }
    public List<ImovelModel> listar() { return service.listar(); }
    public List<ImovelModel> buscar(String codigo, String status, String transacao, String tipo, String cidade,
            String bairro, String valorMinimo, String valorMaximo) {
        return service.buscar(codigo, status, transacao, tipo, cidade, bairro, valorMinimo, valorMaximo);
    }
}
