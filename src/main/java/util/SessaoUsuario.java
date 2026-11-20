package util;

import model.UsuarioModel;
import enums.PerfilUsuario;

/**
 *
 * @author Maikon
 */
public class SessaoUsuario {

    private static UsuarioModel usuarioLogado;

    public static void setUsuarioLogado(UsuarioModel usuario) {
        usuarioLogado = usuario;
    }

    public static UsuarioModel getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void encerrarSessao() {
        usuarioLogado = null;
    }

    public static boolean isAdministrador() {

        return usuarioLogado != null
                && usuarioLogado.getPerfil() == PerfilUsuario.ADMINISTRADOR;
    }

    public static boolean isAssistente() {
        return usuarioLogado != null && usuarioLogado.getPerfil() == PerfilUsuario.ASSISTENTE;
    }

    public static void restringirMenuAssistente(javax.swing.JLabel agenda, javax.swing.JLabel criativos,
            javax.swing.JLabel minhaPagina, javax.swing.JLabel relatorios, javax.swing.JLabel usuarios) {
        if (!isAssistente()) return;
        agenda.setVisible(false);
        criativos.setVisible(false);
        minhaPagina.setVisible(false);
        relatorios.setVisible(false);
        usuarios.setVisible(false);
    }
}
