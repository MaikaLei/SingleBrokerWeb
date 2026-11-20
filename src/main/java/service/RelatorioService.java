package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import model.ClienteModel;
import model.ClientePfModel;
import model.ClientePjModel;
import model.ImovelModel;
import model.UsuarioModel;
import repository.ClienteRepository;
import repository.ImovelRepository;
import repository.UsuarioRepository;
import util.Validador;

public class RelatorioService {
    public record Resultado(String titulo, String texto, int quantidade) { }
    private final ImovelRepository imoveis;
    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;

    public RelatorioService(ImovelRepository imoveis, ClienteRepository clientes, UsuarioRepository usuarios) {
        this.imoveis = Objects.requireNonNull(imoveis);
        this.clientes = Objects.requireNonNull(clientes);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    public List<UsuarioModel> listarUsuarios() { return usuarios.listar(); }
    public List<ImovelModel> listarImoveis() { return imoveis.listar(); }
    public int totalClientes() { return clientes.listarPf().size() + clientes.listarPj().size(); }

    private boolean periodo(LocalDate data, LocalDate inicio, LocalDate fim) {
        if (inicio == null && fim == null) return true;
        return data != null && (inicio == null || !data.isBefore(inicio)) && (fim == null || !data.isAfter(fim));
    }

    private boolean usuario(UsuarioModel dono, Long id) { return id == null || (dono != null && id.equals(dono.getId())); }

    private LocalDate dataImovel(ImovelModel imovel) {
        if (imovel.getDataCadastro() != null) return imovel.getDataCadastro();
        try { return Validador.data(imovel.getDataEntrada(), "entrada"); }
        catch (IllegalArgumentException e) { return null; }
    }

    public Resultado gerar(String tipo, LocalDate inicio, LocalDate fim, Long usuarioId) {
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à final.");
        List<ImovelModel> listaImoveis = imoveis.listar();
        StringBuilder texto = new StringBuilder("Período: " + (inicio == null ? "sem início" : inicio) + " a " + (fim == null ? "sem fim" : fim) + "\n\n");
        int total = 0;
        if (tipo.equals("Lista de Clientes") || tipo.equals("Lista de Proprietários")) {
            List<ClienteModel> listaClientes = new ArrayList<>();
            listaClientes.addAll(clientes.listarPf());
            listaClientes.addAll(clientes.listarPj());
            Set<Long> proprietarios = new HashSet<>();
            for (ImovelModel imovel : listaImoveis)
                if (imovel.getProprietario() != null && usuario(imovel.getUsuarioCadastro(), usuarioId)) proprietarios.add(imovel.getProprietario().getId());
            for (ClienteModel cliente : listaClientes) {
                boolean somenteProprietarios = tipo.equals("Lista de Proprietários");
                if (somenteProprietarios && !proprietarios.contains(cliente.getId())) continue;
                if (!somenteProprietarios && !usuario(cliente.getUsuarioCadastro(), usuarioId)) continue;
                if (!periodo(cliente.getDataCadastro(), inicio, fim)) continue;
                String nome = cliente instanceof ClientePfModel pf ? pf.getNome() : ((ClientePjModel) cliente).getRazaoSocial();
                texto.append(cliente.getId()).append(" | ").append(nome).append(" | ").append(Objects.toString(cliente.getTelefone(), ""))
                        .append(" | ").append(Objects.toString(cliente.getEmail(), "")).append("\n");
                total++;
            }
        } else {
            for (ImovelModel imovel : listaImoveis) {
                if (!usuario(imovel.getUsuarioCadastro(), usuarioId)) continue;
                if (tipo.equals("Lista de ativos") && !"Ativo".equals(imovel.getStatusImovel())) continue;
                if (tipo.equals("Vendidos") && !"Vendido".equals(imovel.getStatusImovel())) continue;
                LocalDate data = tipo.equals("Vendidos") ? imovel.getDataVenda() : dataImovel(imovel);
                if (!periodo(data, inicio, fim)) continue;
                texto.append("Código ").append(imovel.getId()).append(" | ").append(imovel.getTipoImovel()).append(" | ").append(imovel.getStatusImovel()).append("\n")
                        .append(Objects.toString(imovel.getEndereco(), "")).append(" - ").append(Objects.toString(imovel.getCidade(), "")).append("\n")
                        .append("Transação: ").append(imovel.getTransacao()).append(" | Valor: ").append(Objects.toString(imovel.getValor(), "")).append("\n\n");
                total++;
            }
        }
        texto.append("\nTotal: ").append(total);
        if (inicio != null || fim != null || usuarioId != null) texto.append("\nRegistros antigos sem data ou usuário correspondente não entram no filtro.");
        return new Resultado(tipo, texto.toString(), total);
    }
}
