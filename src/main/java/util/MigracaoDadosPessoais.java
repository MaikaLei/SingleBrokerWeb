package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Criptografa os dados pessoais antigos depois de criar uma cópia restaurável. */
public final class MigracaoDadosPessoais {
    private static final String[][] TABELAS = {
        {"cliente", "telefone", "email", "cep", "rua", "numero", "complemento", "bairro", "cidade", "estado"},
        {"cliente_pf", "cpf", "rg"},
        {"cliente_pj", "cnpj", "inscricao_estadual", "nome_responsavel"}
    };

    private MigracaoDadosPessoais() { }

    public static void migrarSeNecessario() throws Exception {
        if (contarLegados() + contarAnexosLegados() == 0) return;
        BackupMySql.criarBackup();
        migrarTabelaPorTabela();
        migrarAnexos();
        BackupMySql.criarBackup();
    }

    private static long contarAnexosLegados() throws SQLException {
        long total = 0;
        try (Connection conexao = conexao(); StatementAux comando = new StatementAux(conexao)) {
            for (String tabela : List.of("foto", "documento"))
                total += comando.contar("SELECT COUNT(*) FROM `" + tabela + "` WHERE `conteudo` IS NOT NULL AND OCTET_LENGTH(`conteudo`) > 0 AND LEFT(`conteudo`, 4) <> X'53424431'");
        }
        return total;
    }

    private static void migrarAnexos() throws SQLException {
        try (Connection conexao = conexao()) {
            conexao.setAutoCommit(false);
            for (String tabela : List.of("foto", "documento")) {
                String select = "SELECT `id`, `conteudo` FROM `" + tabela + "` WHERE `conteudo` IS NOT NULL AND OCTET_LENGTH(`conteudo`) > 0 AND LEFT(`conteudo`, 4) <> X'53424431' LIMIT 50";
                String update = "UPDATE `" + tabela + "` SET `conteudo` = ? WHERE `id` = ?";
                try (PreparedStatement leitura = conexao.prepareStatement(select);
                        PreparedStatement escrita = conexao.prepareStatement(update)) {
                    while (true) {
                        List<Object[]> lote = new ArrayList<>(50);
                        try (ResultSet resultado = leitura.executeQuery()) {
                            while (resultado.next()) lote.add(new Object[]{resultado.getLong(1), resultado.getBytes(2)});
                        }
                        if (lote.isEmpty()) break;
                        for (Object[] linha : lote) {
                            escrita.setBytes(1, CriptografiaDados.criptografar((byte[]) linha[1]));
                            escrita.setLong(2, (Long) linha[0]);
                            escrita.addBatch();
                        }
                        escrita.executeBatch();
                        conexao.commit();
                    }
                }
            }
        }
    }

    private static long contarLegados() throws SQLException {
        long total = 0;
        try (Connection conexao = conexao(); StatementAux comando = new StatementAux(conexao)) {
            for (String[] tabela : TABELAS) {
                String[] fisicas = resolverColunas(conexao, tabela);
                String predicado = String.join(" OR ", java.util.Arrays.stream(fisicas, 1, fisicas.length)
                        .map(coluna -> "(`" + coluna + "` IS NOT NULL AND `" + coluna + "` <> '' AND `" + coluna + "` NOT LIKE 'enc:v1:%')")
                        .toList());
                total += comando.contar("SELECT COUNT(*) FROM `" + fisicas[0] + "` WHERE " + predicado);
            }
        }
        return total;
    }

    private static void migrarTabelaPorTabela() throws SQLException {
        try (Connection conexao = conexao()) {
            conexao.setAutoCommit(false);
            for (String[] tabela : TABELAS) migrarTabela(conexao, resolverColunas(conexao, tabela));
        }
    }

    private static void migrarTabela(Connection conexao, String[] tabela) throws SQLException {
        String nomeTabela = tabela[0];
        String colunas = String.join(", ", java.util.Arrays.stream(tabela, 1, tabela.length).map(c -> "`" + c + "`").toList());
        String predicado = String.join(" OR ", java.util.Arrays.stream(tabela, 1, tabela.length)
                .map(c -> "(`" + c + "` IS NOT NULL AND `" + c + "` <> '' AND `" + c + "` NOT LIKE 'enc:v1:%')")
                .toList());
        String selecionar = "SELECT `id`, " + colunas + " FROM `" + nomeTabela + "` WHERE " + predicado + " LIMIT 100";
        String atualizacao = "UPDATE `" + nomeTabela + "` SET " + String.join(", ", java.util.Arrays.stream(tabela, 1, tabela.length)
                .map(c -> "`" + c + "` = ?").toList()) + " WHERE `id` = ?";
        try (PreparedStatement leitura = conexao.prepareStatement(selecionar);
                PreparedStatement escrita = conexao.prepareStatement(atualizacao)) {
            while (true) {
                List<Object[]> lote = new ArrayList<>(100);
                try (ResultSet resultado = leitura.executeQuery()) {
                    while (resultado.next()) {
                        Object[] linha = new Object[tabela.length];
                        linha[0] = resultado.getLong(1);
                        for (int i = 1; i < tabela.length; i++) linha[i] = resultado.getString(i + 1);
                        lote.add(linha);
                    }
                }
                if (lote.isEmpty()) break;
                for (Object[] linha : lote) {
                    for (int i = 1; i < tabela.length; i++) {
                        String valor = (String) linha[i];
                        escrita.setString(i, CriptografiaDados.criptografar(valor));
                    }
                    escrita.setLong(tabela.length, (Long) linha[0]);
                    escrita.addBatch();
                }
                escrita.executeBatch();
                conexao.commit();
            }
        } catch (SQLException | RuntimeException e) {
            conexao.rollback();
            throw e;
        }
    }

    private static String[] resolverColunas(Connection conexao, String[] tabela) throws SQLException {
        String[] resolvida = tabela.clone();
        for (int i = 1; i < tabela.length; i++) {
            String esperada = tabela[i].replace("_", "").toLowerCase(java.util.Locale.ROOT);
            try (ResultSet colunas = conexao.getMetaData().getColumns(conexao.getCatalog(), null, tabela[0], null)) {
                boolean encontrada = false;
                while (colunas.next()) {
                    String nome = colunas.getString("COLUMN_NAME");
                    if (nome.replace("_", "").toLowerCase(java.util.Locale.ROOT).equals(esperada)) {
                        resolvida[i] = nome;
                        encontrada = true;
                        break;
                    }
                }
                if (!encontrada) throw new SQLException("Coluna de dados pessoais ausente na tabela " + tabela[0] + ".");
            }
        }
        return resolvida;
    }

    private static Connection conexao() throws SQLException {
        String url = JPAUtil.obterConfiguracao("SINGLEBROKER_DB_URL", "db.url");
        String usuario = JPAUtil.obterConfiguracao("SINGLEBROKER_DB_USER", "db.user");
        String senha = JPAUtil.obterConfiguracao("SINGLEBROKER_DB_PASSWORD", "db.password");
        return DriverManager.getConnection(url == null ? "jdbc:mysql://localhost:3306/singlebrokerdb" : url,
                usuario == null ? "root" : usuario, senha == null ? "" : senha);
    }

    private static final class StatementAux implements AutoCloseable {
        private final java.sql.Statement statement;
        StatementAux(Connection conexao) throws SQLException { statement = conexao.createStatement(); }
        long contar(String sql) throws SQLException {
            try (ResultSet resultado = statement.executeQuery(sql)) { return resultado.next() ? resultado.getLong(1) : 0; }
        }
        @Override public void close() throws SQLException { statement.close(); }
    }
}
