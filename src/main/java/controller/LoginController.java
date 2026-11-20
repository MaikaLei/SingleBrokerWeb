package controller;

import dao.UsuarioDao;
import model.UsuarioModel;
import service.LoginService;

public class LoginController {
    private final LoginService service;
    public LoginController() { this(new LoginService(new UsuarioDao(), util.SessaoUsuario::setUsuarioLogado)); }
    public LoginController(LoginService service) { this.service = java.util.Objects.requireNonNull(service); }
    public UsuarioModel entrar(String email, String senha) { return service.entrar(email, senha); }
}
