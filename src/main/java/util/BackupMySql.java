package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Gera uma cópia SQL diária sem iniciar processos externos. */
public final class BackupMySql {
    private static final Path DIRETORIO = Path.of("backups");
    private static final DateTimeFormatter NOME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static ScheduledExecutorService agendador;

    private BackupMySql() { }

    public static synchronized void iniciar() {
        if (agendador != null) return;
        agendador = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "singlebroker-backup");
            thread.setDaemon(true);
            return thread;
        });
        agendador.scheduleAtFixedRate(() -> {
            try { criarBackupDiario(); }
            catch (Exception e) { registrarFalha(e); }
        }, 0, 1440, TimeUnit.MINUTES);
    }

    public static synchronized void encerrar() {
        if (agendador != null) agendador.shutdownNow();
        agendador = null;
    }

    public static Path criarBackupDiario() throws SQLException, IOException {
        Files.createDirectories(DIRETORIO);
        String hoje = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        try (var existentes = Files.list(DIRETORIO)) {
            var anterior = existentes.filter(p -> p.getFileName().toString().startsWith("SingleBroker-" + hoje + "-")
                    && p.toString().endsWith(".sql")).findFirst();
            if (anterior.isPresent()) {
                Path chaveBackup = anterior.get().resolveSibling(anterior.get().getFileName().toString().replace(".sql", ".key"));
                if (Files.exists(chaveBackup)) return anterior.get();
            }
        }
        return criarBackup();
    }

    public static Path criarBackup() throws SQLException, IOException {
        Files.createDirectories(DIRETORIO);
        String url = JPAUtil.obterConfiguracao("SINGLEBROKER_DB_URL", "db.url");
        String usuario = JPAUtil.obterConfiguracao("SINGLEBROKER_DB_USER", "db.user");
        String senha = JPAUtil.obterConfiguracao("SINGLEBROKER_DB_PASSWORD", "db.password");
        if (url == null) url = "jdbc:mysql://localhost:3306/singlebrokerdb";
        if (usuario == null) usuario = "root";
        if (senha == null) senha = "";

        Path destino = DIRETORIO.resolve("SingleBroker-" + LocalDateTime.now().format(NOME) + ".sql");
        Path temporario = Files.createTempFile(DIRETORIO, "singlebroker-", ".tmp");
        try {
            try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
                    BufferedWriter saida = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                conexao.setAutoCommit(false);
                conexao.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                saida.write("SET FOREIGN_KEY_CHECKS=0;\n");
                List<String> tabelas = listarTabelas(conexao);
                for (String tabela : tabelas) exportarTabela(conexao, tabela, saida);
                saida.write("SET FOREIGN_KEY_CHECKS=1;\n");
                saida.flush();
                conexao.rollback();
            }
            try { Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) { Files.move(temporario, destino); }
            Path chaveBackup = destino.resolveSibling(destino.getFileName().toString().replace(".sql", ".key"));
            Files.writeString(chaveBackup, CriptografiaDados.chaveBase64(), StandardCharsets.UTF_8);
            return destino;
        } catch (SQLException | IOException | RuntimeException e) {
            Files.deleteIfExists(temporario);
            throw e;
        }
    }

    private static List<String> listarTabelas(Connection conexao) throws SQLException {
        List<String> tabelas = new ArrayList<>();
        try (Statement comando = conexao.createStatement(); ResultSet resultado = comando.executeQuery("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'")) {
            while (resultado.next()) tabelas.add(resultado.getString(1));
        }
        return tabelas;
    }

    private static void exportarTabela(Connection conexao, String tabela, BufferedWriter saida) throws SQLException, IOException {
        String identificador = "`" + tabela.replace("`", "``") + "`";
        try (Statement comando = conexao.createStatement(); ResultSet ddl = comando.executeQuery("SHOW CREATE TABLE " + identificador)) {
            if (ddl.next()) {
                saida.write("DROP TABLE IF EXISTS " + identificador + ";\n");
                saida.write(ddl.getString(2) + ";\n");
            }
        }
        int offset = 0;
        int tamanhoLote = tabela.equalsIgnoreCase("foto") || tabela.equalsIgnoreCase("documento") ? 10 : 200;
        while (true) {
            int quantidade = 0;
            try (Statement comando = conexao.createStatement();
                    ResultSet linhas = comando.executeQuery("SELECT * FROM " + identificador + " LIMIT " + tamanhoLote + " OFFSET " + offset)) {
                ResultSetMetaData meta = linhas.getMetaData();
                int colunas = meta.getColumnCount();
                StringBuilder prefixo = new StringBuilder("INSERT INTO ").append(identificador).append(" (");
                for (int i = 1; i <= colunas; i++) {
                    if (i > 1) prefixo.append(", ");
                    prefixo.append("`").append(meta.getColumnName(i).replace("`", "``")).append("`");
                }
                prefixo.append(") VALUES (");
                while (linhas.next()) {
                    quantidade++;
                    saida.write(prefixo.toString());
                    for (int i = 1; i <= colunas; i++) {
                        if (i > 1) saida.write(", ");
                        saida.write(sql(linhas.getObject(i), meta.getColumnType(i)));
                    }
                    saida.write(");\n");
                }
            }
            offset += quantidade;
            if (quantidade < tamanhoLote) break;
        }
    }

    private static String sql(Object valor, int tipo) throws SQLException {
        if (valor == null) return "NULL";
        if (valor instanceof byte[] bytes) return "X'" + java.util.HexFormat.of().formatHex(bytes) + "'";
        if (valor instanceof java.sql.Blob blob) {
            try (var entrada = blob.getBinaryStream(); var bytes = new java.io.ByteArrayOutputStream()) {
                entrada.transferTo(bytes);
                return "X'" + java.util.HexFormat.of().formatHex(bytes.toByteArray()) + "'";
            } catch (IOException e) { throw new SQLException("Não foi possível exportar um anexo do banco.", e); }
        }
        if (tipo == java.sql.Types.BIT || tipo == java.sql.Types.BOOLEAN
                || tipo == java.sql.Types.TINYINT || tipo == java.sql.Types.SMALLINT
                || tipo == java.sql.Types.INTEGER || tipo == java.sql.Types.BIGINT
                || tipo == java.sql.Types.FLOAT || tipo == java.sql.Types.REAL
                || tipo == java.sql.Types.DOUBLE || tipo == java.sql.Types.NUMERIC
                || tipo == java.sql.Types.DECIMAL) return valor.toString();
        String texto = valor.toString().replace("\\", "\\\\").replace("'", "\\'")
                .replace("\0", "\\0").replace("\n", "\\n").replace("\r", "\\r");
        return "'" + texto + "'";
    }

    private static void registrarFalha(Exception erro) {
        try {
            Files.createDirectories(DIRETORIO);
            Files.writeString(DIRETORIO.resolve("backup-error.log"), LocalDateTime.now() + " - "
                    + erro.getClass().getSimpleName() + ": " + erro.getMessage() + System.lineSeparator(),
                    StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignorado) { }
    }
}
