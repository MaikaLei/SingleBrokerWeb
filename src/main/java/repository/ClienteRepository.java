package repository;

import java.util.List;
import model.ClienteModel;
import model.ClientePfModel;
import model.ClientePjModel;

public interface ClienteRepository {
    void salvar(ClienteModel cliente);
    void atualizar(ClienteModel cliente);
    ClienteModel buscarPorId(Long id);
    List<ClientePfModel> listarPf();
    List<ClientePjModel> listarPj();
    List<ClientePfModel> buscarPf(String nome, String cpf, String telefone);
    List<ClientePjModel> buscarPj(String nome, String cnpj, String telefone);
}
