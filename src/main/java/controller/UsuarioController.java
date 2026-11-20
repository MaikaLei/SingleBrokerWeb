package controller;

import dao.UsuarioDao;
import java.util.List;
import model.UsuarioModel;
import service.UsuarioService;

public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController() { this(new UsuarioService(new UsuarioDao(), util.SessaoUsuario::getUsuarioLogado, util.SessaoUsuario::isAdministrador)); }
    public UsuarioController(UsuarioService service) { this.service = java.util.Objects.requireNonNull(service); }
    public void salvar(UsuarioModel usuario, String novaSenha) { service.salvar(usuario, novaSenha); }
    public UsuarioModel buscarPorId(Long id) { return service.buscarPorId(id); }
    public List<UsuarioModel> listar() { return service.listar(); }
    public List<UsuarioModel> buscar(String perfil, String status) { return service.buscar(perfil, status); }
}
