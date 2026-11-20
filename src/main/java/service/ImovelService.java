package service;

import java.time.Clock;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import model.ClienteModel;
import model.ImovelModel;
import model.UsuarioModel;
import repository.ClienteRepository;
import repository.ImovelRepository;
import util.Validador;

public class ImovelService {
    private final ImovelRepository imoveis;
    private final ClienteRepository clientes;
    private final Supplier<UsuarioModel> usuarioAtual;
    private final Clock clock;

    public ImovelService(ImovelRepository imoveis, ClienteRepository clientes,
            Supplier<UsuarioModel> usuarioAtual, Clock clock) {
        this.imoveis = Objects.requireNonNull(imoveis);
        this.clientes = Objects.requireNonNull(clientes);
        this.usuarioAtual = Objects.requireNonNull(usuarioAtual);
        this.clock = Objects.requireNonNull(clock);
    }

    public void salvar(ImovelModel imovel) {
        Objects.requireNonNull(imovel, "Informe o imóvel.");
        ClienteModel proprietario = imovel.getProprietario();
        if (proprietario == null || proprietario.getId() == null)
            throw new IllegalArgumentException("Selecione um proprietário cadastrado.");
        ClienteModel proprietarioPersistido = clientes.buscarPorId(proprietario.getId());
        if (proprietarioPersistido == null) throw new IllegalArgumentException("O proprietário selecionado não existe.");
        imovel.setProprietario(proprietarioPersistido);
        imovel.setEndereco(Validador.obrigatorio(imovel.getEndereco(), "o endereço"));
        imovel.setTipoImovel(opcaoObrigatoria(imovel.getTipoImovel(), "o tipo do imóvel", List.of("Apartamento", "Casa", "Terreno", "Sala Comercial", "Loja")));
        imovel.setTransacao(opcaoObrigatoria(imovel.getTransacao(), "a transação", List.of("Venda", "Locação", "Venda e Locação")));
        imovel.setStatusImovel(opcaoObrigatoria(imovel.getStatusImovel(), "o status", List.of("Ativo", "Inativo", "Vendido", "Alugado")));
        imovel.setCidade(Validador.obrigatorio(imovel.getCidade(), "a cidade"));
        imovel.setEstado(Validador.obrigatorio(imovel.getEstado(), "o estado").toUpperCase());
        Validador.decimal(Validador.obrigatorio(imovel.getValor(), "o valor do imóvel"), "valor do imóvel");
        if (Validador.decimal(imovel.getValor(), "valor do imóvel").signum() <= 0)
            throw new IllegalArgumentException("O valor do imóvel deve ser maior que zero.");
        Validador.decimal(imovel.getValorAdministracao(), "administração");
        Validador.decimal(imovel.getValorCondominio(), "condomínio");
        Validador.decimal(imovel.getValorIptu(), "IPTU");
        Validador.data(imovel.getDataEntrada(), "a data de entrada");
        Validador.data(imovel.getValidadeContrato(), "a validade do contrato");
        imovel.setLinkVideoYoutube(validarLinkYoutube(imovel.getLinkVideoYoutube()));

        LocalDate hoje = LocalDate.now(clock);
        boolean novo = imovel.getId() == null;
        String statusAnterior = "";
        if (novo) {
            imovel.setDataCadastro(hoje);
            imovel.setUsuarioCadastro(usuarioAtual.get());
        } else {
            ImovelModel atual = imoveis.buscarPorId(imovel.getId());
            if (atual == null) throw new IllegalArgumentException("O imóvel que deseja editar não foi encontrado.");
            statusAnterior = atual.getStatusImovel();
        }
        if (imoveis.existeEndereco(imovel.getEndereco(), imovel.getNumero(), imovel.getComplemento(),
                imovel.getCidade(), imovel.getEstado(), imovel.getId()))
            throw new IllegalArgumentException("Já existe um imóvel cadastrado neste endereço, número e complemento.");
        if ("Vendido".equals(imovel.getStatusImovel()) && !"Vendido".equals(statusAnterior)) imovel.setDataVenda(hoje);
        if (!"Vendido".equals(imovel.getStatusImovel())) imovel.setDataVenda(null);
        if (novo) imoveis.salvar(imovel); else imoveis.atualizar(imovel);
    }

    public ImovelModel buscarPorId(Long id) { return imoveis.buscarPorId(id); }
    public List<ImovelModel> listar() { return imoveis.listar(); }
    public List<ImovelModel> buscar(String codigo, String status, String transacao, String tipo, String cidade,
            String bairro, String valorMinimo, String valorMaximo) {
        String codigoNormalizado = codigo == null ? "" : codigo.trim();
        if (!codigoNormalizado.isEmpty() && !codigoNormalizado.matches("\\d+"))
            throw new IllegalArgumentException("O código do imóvel deve conter somente números.");
        BigDecimal minimo = Validador.decimal(valorMinimo, "valor mínimo");
        BigDecimal maximo = Validador.decimal(valorMaximo, "valor máximo");
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0)
            throw new IllegalArgumentException("O valor mínimo não pode ser maior que o valor máximo.");
        return imoveis.buscar(codigoNormalizado, status, transacao, tipo, cidade, bairro).stream()
                .filter(imovel -> dentroDaFaixa(imovel.getValor(), minimo, maximo))
                .toList();
    }

    private static String opcaoObrigatoria(String valor, String campo, List<String> opcoes) {
        String normalizado = Validador.obrigatorio(valor, campo);
        if (!opcoes.contains(normalizado)) throw new IllegalArgumentException("Selecione " + campo + " válido.");
        return normalizado;
    }

    private static boolean dentroDaFaixa(String valor, BigDecimal minimo, BigDecimal maximo) {
        if (minimo == null && maximo == null) return true;
        BigDecimal numero;
        try { numero = Validador.decimal(valor, "valor do imóvel"); }
        catch (IllegalArgumentException ex) { return false; }
        if (numero == null) return false;
        return (minimo == null || numero.compareTo(minimo) >= 0)
                && (maximo == null || numero.compareTo(maximo) <= 0);
    }

    private static String validarLinkYoutube(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String link = valor.trim();
        try {
            java.net.URI uri = java.net.URI.create(link);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null
                    || !(host.equalsIgnoreCase("youtube.com") || host.endsWith(".youtube.com")
                    || host.equalsIgnoreCase("youtu.be") || host.equalsIgnoreCase("youtube-nocookie.com")
                    || host.endsWith(".youtube-nocookie.com")))
                throw new IllegalArgumentException();
            return uri.toASCIIString();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Informe um link HTTPS válido do YouTube.");
        }
    }
}
