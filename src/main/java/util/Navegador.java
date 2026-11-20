package util;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/** Centraliza a navegação para manter uma única tela ativa e voltar ao contexto correto. */
public final class Navegador {
    private static final Map<JFrame, JFrame> telasAnteriores = new IdentityHashMap<>();
    private static final Set<JFrame> telasConfiguradas = Collections.newSetFromMap(new IdentityHashMap<>());
    private static JFrame telaPrincipal;

    private Navegador() { }

    public static void iniciarAplicacao(JFrame principal) {
        telaPrincipal = principal;
        principal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        principal.setLocationRelativeTo(null);
        principal.setVisible(true);
    }

    public static boolean podeSair(JFrame tela, boolean alterado) {
        boolean mudou = tela instanceof FormularioEditavel f ? f.temAlteracoes() : alterado;
        if (!mudou) return true;
        if (tela instanceof FormularioEditavel f) {
            int resposta = JOptionPane.showConfirmDialog(tela,
                    "Deseja salvar as alterações antes de sair?", "Alterações não salvas",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (resposta == JOptionPane.YES_OPTION) return f.salvarAlteracoes();
            return resposta == JOptionPane.NO_OPTION;
        }
        return JOptionPane.showConfirmDialog(tela, "Descartar as alterações e sair?",
                "Alterações não salvas", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Abre cadastro, detalhe ou seletor como filho da tela atual. */
    public static void abrirTela(JFrame atual, JFrame proxima, boolean alterado) {
        if (!permitida(proxima, atual)) return;
        if (atual.getClass().equals(proxima.getClass())) {
            proxima.dispose();
            return;
        }
        if (!podeSair(atual, alterado)) {
            proxima.dispose();
            return;
        }

        JFrame anterior = telasAnteriores.get(atual);
        if (anterior != null && anterior.isDisplayable()
                && anterior.getClass().equals(proxima.getClass())) {
            proxima.dispose();
            fecharAprovado(atual, anterior);
            return;
        }
        abrirFilha(atual, proxima);
    }

    /** Troca o módulo atual e descarta telas intermediárias, mantendo a tela principal como base. */
    public static void abrirModulo(JFrame atual, JFrame proximo, boolean alterado) {
        if (!permitida(proximo, atual)) return;
        if (atual.getClass().equals(proximo.getClass())) {
            proximo.dispose();
            return;
        }
        if (!podeSairDaCadeia(atual, alterado)) {
            proximo.dispose();
            return;
        }

        JFrame principal = obterTelaPrincipal(atual);
        boolean voltarParaPrincipal = principal != null
                && principal.isDisplayable()
                && proximo.getClass().equals(principal.getClass());
        if (voltarParaPrincipal) proximo.dispose();

        descartarCadeia(atual, principal);
        JFrame destino = voltarParaPrincipal ? principal : proximo;
        if (destino == null) return;
        if (destino instanceof view.ListaImovelView lista) lista.atualizarTabela();
        if (destino != principal) {
            if (principal != null) telasAnteriores.put(destino, principal);
            configurarFechamento(destino);
            destino.setLocationRelativeTo(null);
        }
        destino.setVisible(true);
        destino.toFront();
    }

    /** Abre um auxiliar sem pedir confirmação do formulário que continuará em edição. */
    public static void abrirAuxiliar(JFrame atual, JFrame proxima) {
        if (!permitida(proxima, atual)) return;
        abrirFilha(atual, proxima);
    }

    public static void voltarTela(JFrame tela, boolean alterado) {
        fecharTela(tela, alterado);
    }

    public static void fecharTela(JFrame tela, boolean alterado) {
        if (tela == null || !tela.isDisplayable() || !podeSair(tela, alterado)) return;
        JFrame anterior = telasAnteriores.remove(tela);
        telasConfiguradas.remove(tela);
        tela.dispose();
        restaurar(anterior);
    }

    private static void abrirFilha(JFrame atual, JFrame proxima) {
        telasAnteriores.put(proxima, atual);
        configurarFechamento(proxima);
        proxima.setLocationRelativeTo(atual);
        atual.setVisible(false);
        proxima.setVisible(true);
    }

    private static boolean podeSairDaCadeia(JFrame atual, boolean alterado) {
        JFrame cursor = atual;
        Set<JFrame> visitadas = Collections.newSetFromMap(new IdentityHashMap<>());
        boolean primeira = true;
        while (cursor != null && visitadas.add(cursor)) {
            if (!podeSair(cursor, primeira ? alterado : false)) return false;
            if (cursor == telaPrincipal) break;
            cursor = telasAnteriores.get(cursor);
            primeira = false;
        }
        return true;
    }

    private static JFrame obterTelaPrincipal(JFrame atual) {
        if (telaPrincipal != null && telaPrincipal.isDisplayable()) return telaPrincipal;
        JFrame cursor = atual;
        Set<JFrame> visitadas = Collections.newSetFromMap(new IdentityHashMap<>());
        while (cursor != null && visitadas.add(cursor)) {
            JFrame anterior = telasAnteriores.get(cursor);
            if (anterior == null) break;
            cursor = anterior;
        }
        telaPrincipal = cursor;
        return cursor;
    }

    private static void descartarCadeia(JFrame atual, JFrame principal) {
        JFrame cursor = atual;
        Set<JFrame> visitadas = Collections.newSetFromMap(new IdentityHashMap<>());
        while (cursor != null && cursor != principal && visitadas.add(cursor)) {
            JFrame anterior = telasAnteriores.remove(cursor);
            telasConfiguradas.remove(cursor);
            cursor.dispose();
            cursor = anterior;
        }
    }

    private static void fecharAprovado(JFrame tela, JFrame anterior) {
        telasAnteriores.remove(tela);
        telasConfiguradas.remove(tela);
        tela.dispose();
        restaurar(anterior);
    }

    private static void restaurar(JFrame anterior) {
        if (anterior == null || !anterior.isDisplayable()) return;
        try {
            if (anterior instanceof view.ListaImovelView lista) lista.atualizarTabela();
            if (anterior instanceof view.ImovelView imovel) imovel.atualizarDetalhes();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(anterior, "A tela foi reaberta, mas não foi possível atualizar os dados: "
                    + e.getMessage(), "Atualização", JOptionPane.WARNING_MESSAGE);
        }
        anterior.setVisible(true);
        anterior.toFront();
    }

    private static boolean permitida(JFrame proxima, JFrame atual) {
        if (!SessaoUsuario.isAssistente()) return true;
        boolean permitida = !(proxima instanceof view.AgendaView || proxima instanceof view.CriativosView
                || proxima instanceof view.MinhaPaginaView || proxima instanceof view.RelatoriosView
                || proxima instanceof view.ListaUsuarioModal);
        if (!permitida) {
            proxima.dispose();
            JOptionPane.showMessageDialog(atual,
                    "O perfil Assistente pode acessar somente imóveis e clientes.",
                    "Acesso não permitido", JOptionPane.WARNING_MESSAGE);
        }
        return permitida;
    }

    private static void configurarFechamento(JFrame tela) {
        if (!telasConfiguradas.add(tela)) return;
        tela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        tela.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                fecharTela(tela, false);
            }

            @Override
            public void windowClosed(WindowEvent evento) {
                JFrame anterior = telasAnteriores.remove(tela);
                telasConfiguradas.remove(tela);
                if (anterior != null) restaurar(anterior);
            }
        });
    }
}
