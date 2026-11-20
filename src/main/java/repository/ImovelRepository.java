package repository;

import java.util.List;
import model.ImovelModel;

public interface ImovelRepository {
    void salvar(ImovelModel imovel);
    void atualizar(ImovelModel imovel);
    ImovelModel buscarPorId(Long id);
    List<ImovelModel> listar();
    List<ImovelModel> buscar(String codigo, String status, String transacao, String tipo, String cidade, String bairro);
    boolean existeEndereco(String endereco, String numero, String complemento, String cidade, String estado, Long ignorarId);
}
