package repository;

import java.util.List;
import model.UsuarioModel;

public interface UsuarioRepository {
    void salvar(UsuarioModel usuario);
    void atualizar(UsuarioModel usuario);
    UsuarioModel buscarPorId(Long id);
    List<UsuarioModel> listar();
    List<UsuarioModel> buscar(String perfil, String status);
    UsuarioModel autenticar(String email, String senha);
}
