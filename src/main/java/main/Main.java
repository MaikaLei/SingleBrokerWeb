package main;
import dao.UsuarioDao;
import enums.PerfilUsuario;
import model.UsuarioModel;
import util.*;
import javax.swing.*;
import java.math.BigDecimal;
import model.ImovelModel;
import service.CriativoService;

public class Main {
    public static void main(String[] args) {
        if (args.length == 1 && "--verificar-regras".equals(args[0])) {
            verificarRegras();
            return;
        }
        Tema.instalar();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            BackupMySql.encerrar();
            JPAUtil.close();
        }));
        SwingUtilities.invokeLater(() -> {
            try {
                UsuarioDao dao = new UsuarioDao();
                java.util.List<UsuarioModel> usuarios = dao.listar();
                MigracaoDadosPessoais.migrarSeNecessario();
                if (usuarios.isEmpty() && !criarAdministrador(dao)) return;
                BackupMySql.iniciar();
                new view.LoginView().setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null, "Não foi possível iniciar. Verifique o MySQL, o backup e a proteção local dos dados.\n" + e.getMessage(), "SingleBroker", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Não foi possível preparar a proteção dos dados. Confira o MySQL e a pasta backups.\n" + e.getMessage(), "SingleBroker", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /** Verificações rápidas das regras puras, sem abrir a interface ou acessar o banco. */
    private static void verificarRegras() {
        if (Validador.decimal("1.250,50", "valor").compareTo(new BigDecimal("1250.50")) != 0)
            throw new AssertionError("A conversão de valor em formato brasileiro falhou.");
        ImovelModel imovel = new ImovelModel();
        imovel.setTipoImovel("Apartamento");
        imovel.setTransacao("Venda");
        imovel.setCidade("Porto Alegre");
        imovel.setValor("450000");
        imovel.setAreaPrivativa(new BigDecimal("80"));
        String texto = new CriativoService().gerar(imovel, "Ficha resumida do imóvel");
        if (!texto.contains("Apartamento para venda em Porto Alegre") || !texto.contains("Área privativa: 80 m²"))
            throw new AssertionError("A geração da ficha resumida falhou.");
        try {
            new CriativoService().gerar(null, "Ficha resumida do imóvel");
            throw new AssertionError("A geração deve rejeitar imóvel ausente.");
        } catch (IllegalArgumentException esperado) {
            // O caso inválido foi rejeitado como esperado.
        }
        System.out.println("Verificações concluídas.");
    }

    private static boolean criarAdministrador(UsuarioDao dao) {
        JTextField nome = new JTextField("Administrador");
        JTextField email = new JTextField();
        JPasswordField senha = new JPasswordField();
        while (JOptionPane.showConfirmDialog(null, new Object[]{"Crie o primeiro administrador", "Nome", nome, "E-mail", email, "Senha (mínimo 6 caracteres)", senha}, "Primeiro acesso", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                UsuarioModel admin = new UsuarioModel();
                admin.setNome(Validador.obrigatorio(nome.getText(), "o nome"));
                admin.setEmail(Validador.email(email.getText(), true));
                admin.setSenha(Senhas.gerar(new String(senha.getPassword())));
                admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
                admin.setAtivo(true);
                admin.setDataCadastro(java.time.LocalDateTime.now());
                dao.salvar(admin);
                return true;
            } catch (RuntimeException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        }
        return false;
    }
}
