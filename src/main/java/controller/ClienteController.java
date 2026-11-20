package controller;

import dao.ClienteDao;
import java.time.Clock;
import java.util.List;
import model.ClienteModel;
import model.ClientePfModel;
import model.ClientePjModel;
import service.ClienteService;

public class ClienteController {
    private final ClienteService service;

    public ClienteController() {
        this(new ClienteService(new ClienteDao(), util.SessaoUsuario::getUsuarioLogado, Clock.systemDefaultZone()));
    }

    public ClienteController(ClienteService service) { this.service = java.util.Objects.requireNonNull(service); }

    public void salvar(ClienteModel cliente) { service.salvar(cliente); }
    public ClienteModel buscarPorId(Long id) { return service.buscarPorId(id); }
    public List<ClientePfModel> listarPf() { return service.listarPf(); }
    public List<ClientePjModel> listarPj() { return service.listarPj(); }
    public List<ClientePfModel> buscarPf(String nome, String cpf, String telefone) { return service.buscarPf(nome, cpf, telefone); }
    public List<ClientePjModel> buscarPj(String nome, String cnpj, String telefone) { return service.buscarPj(nome, cnpj, telefone); }
}
