package service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.BooleanSupplier;
import model.UsuarioModel;
import repository.UsuarioRepository;
import util.Senhas;
import util.Validador;

public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final Supplier<UsuarioModel> usuarioAtual;
    private final BooleanSupplier administrador;

    public UsuarioService(UsuarioRepository usuarios, Supplier<UsuarioModel> usuarioAtual, BooleanSupplier administrador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.usuarioAtual = Objects.requireNonNull(usuarioAtual);
        this.administrador = Objects.requireNonNull(administrador);
    }

    public void salvar(UsuarioModel usuario, String novaSenha) {
        if (!administrador.getAsBoolean()) throw new SecurityException("Somente o administrador pode alterar usuários.");
        usuario.setNome(Validador.obrigatorio(usuario.getNome(), "o nome"));
        usuario.setEmail(Validador.email(usuario.getEmail(), true));
        if (usuario.getPerfil() == null) throw new IllegalArgumentException("Selecione o perfil.");
        UsuarioModel atual = usuarioAtual.get();
        if (usuario.getId() != null && atual != null && usuario.getId().equals(atual.getId())
                && (!Boolean.TRUE.equals(usuario.getAtivo()) || usuario.getPerfil() != enums.PerfilUsuario.ADMINISTRADOR))
            throw new IllegalArgumentException("Você não pode desativar ou remover seu próprio acesso de administrador.");
        if (novaSenha != null && !novaSenha.isBlank()) usuario.setSenha(Senhas.gerar(novaSenha));
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) throw new IllegalArgumentException("Informe a senha do novo usuário.");
        if (usuario.getId() == null) usuarios.salvar(usuario); else usuarios.atualizar(usuario);
    }

    public UsuarioModel buscarPorId(Long id) { exigirAdministrador(); return usuarios.buscarPorId(id); }
    public List<UsuarioModel> listar() { exigirAdministrador(); return usuarios.listar(); }
    public List<UsuarioModel> buscar(String perfil, String status) { exigirAdministrador(); return usuarios.buscar(perfil, status); }

    private void exigirAdministrador() {
        if (!administrador.getAsBoolean()) throw new SecurityException("Somente o administrador pode acessar usuários.");
    }
}
