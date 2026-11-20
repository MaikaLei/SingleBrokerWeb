package service;

import java.util.Objects;
import java.util.function.Consumer;
import model.UsuarioModel;
import repository.UsuarioRepository;
import util.Validador;

public class LoginService {
    private final UsuarioRepository usuarios;
    private final Consumer<UsuarioModel> iniciarSessao;

    public LoginService(UsuarioRepository usuarios, Consumer<UsuarioModel> iniciarSessao) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.iniciarSessao = Objects.requireNonNull(iniciarSessao);
    }

    public UsuarioModel entrar(String email, String senha) {
        Validador.obrigatorio(email, "o e-mail");
        Validador.obrigatorio(senha, "a senha");
        UsuarioModel usuario = usuarios.autenticar(email, senha);
        if (usuario != null) iniciarSessao.accept(usuario);
        return usuario;
    }
}
