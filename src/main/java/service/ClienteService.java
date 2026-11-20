package service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import model.ClienteModel;
import model.ClientePfModel;
import model.ClientePjModel;
import model.UsuarioModel;
import repository.ClienteRepository;
import util.Validador;

public class ClienteService {
    private final ClienteRepository clientes;
    private final Supplier<UsuarioModel> usuarioAtual;
    private final Clock clock;

    public ClienteService(ClienteRepository clientes, Supplier<UsuarioModel> usuarioAtual, Clock clock) {
        this.clientes = Objects.requireNonNull(clientes);
        this.usuarioAtual = Objects.requireNonNull(usuarioAtual);
        this.clock = Objects.requireNonNull(clock);
    }

    public void salvar(ClienteModel cliente) {
        Objects.requireNonNull(cliente, "Informe o cliente.");
        cliente.setEmail(Validador.email(cliente.getEmail(), false));
        String documento;
        boolean duplicado;
        if (cliente instanceof ClientePfModel pf) {
            pf.setNome(Validador.obrigatorio(pf.getNome(), "o nome"));
            documento = Validador.documento(pf.getCpf(), "CPF");
            pf.setCpf(documento);
            if (pf.getDataNascimento() != null && pf.getDataNascimento().isAfter(LocalDate.now(clock)))
                throw new IllegalArgumentException("A data de nascimento não pode estar no futuro.");
            String documentoNormalizado = somenteDigitos(documento);
            duplicado = clientes.listarPf().stream().anyMatch(outro ->
                    !Objects.equals(outro.getId(), pf.getId()) && outro.getCpf() != null
                    && somenteDigitos(outro.getCpf()).equals(documentoNormalizado));
        } else if (cliente instanceof ClientePjModel pj) {
            pj.setRazaoSocial(Validador.obrigatorio(pj.getRazaoSocial(), "a razão social"));
            documento = Validador.documento(pj.getCnpj(), "CNPJ");
            pj.setCnpj(documento);
            String documentoNormalizado = somenteDigitos(documento);
            duplicado = clientes.listarPj().stream().anyMatch(outro ->
                    !Objects.equals(outro.getId(), pj.getId()) && outro.getCnpj() != null
                    && somenteDigitos(outro.getCnpj()).equals(documentoNormalizado));
        } else {
            throw new IllegalArgumentException("Selecione o tipo de cliente.");
        }
        if (duplicado) throw new IllegalArgumentException("Já existe um cliente com este CPF/CNPJ.");
        if (cliente.getId() == null) {
            cliente.setDataCadastro(LocalDate.now(clock));
            cliente.setUsuarioCadastro(usuarioAtual.get());
            clientes.salvar(cliente);
        } else {
            clientes.atualizar(cliente);
        }
    }

    public ClienteModel buscarPorId(Long id) { return clientes.buscarPorId(id); }
    public List<ClientePfModel> listarPf() { return clientes.listarPf(); }
    public List<ClientePjModel> listarPj() { return clientes.listarPj(); }
    public List<ClientePfModel> buscarPf(String nome, String documento, String telefone) { return clientes.buscarPf(nome, documento, telefone); }
    public List<ClientePjModel> buscarPj(String nome, String documento, String telefone) { return clientes.buscarPj(nome, documento, telefone); }

    private static String somenteDigitos(String valor) { return valor.replaceAll("[^0-9]", ""); }
}
