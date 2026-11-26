import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ConexaoMySqlTest {

    @Test
    void consultaServidorMySqlQuandoDisponivel() {
        String url = valorConfigurado("SINGLEBROKER_DB_URL", "db.url",
                "jdbc:mysql://localhost:3306/singlebrokerdb");
        String usuario = valorConfigurado("SINGLEBROKER_DB_USER", "db.user", "root");
        String senha = valorConfigurado("SINGLEBROKER_DB_PASSWORD", "db.password", "");

        Integer resultado = null;
        String falhaConexao = null;

        try (Connection conexao = DriverManager.getConnection(url, usuario, senha);
                var comando = conexao.createStatement();
                var resultadoConsulta = comando.executeQuery("SELECT 1")) {
            if (resultadoConsulta.next()) {
                resultado = resultadoConsulta.getInt(1);
            }
        } catch (SQLException | IllegalStateException e) {
            falhaConexao = e.getMessage();
        }

        assumeTrue(falhaConexao == null,
                "Verificação MySQL ignorada porque o banco não está acessível: " + falhaConexao);
        assertEquals(1, resultado);
    }

    private String valorConfigurado(String ambiente, String chaveLocal, String valorPadrao) {
        String valor = util.JPAUtil.obterConfiguracao(ambiente, chaveLocal);
        return valor != null ? valor : valorPadrao;
    }
}
