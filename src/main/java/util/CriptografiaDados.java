package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Protege dados pessoais persistidos no banco. */
public final class CriptografiaDados {
    private static final String PREFIXO = "enc:v1:";
    private static final int TAMANHO_NONCE = 12;
    private static final byte[] PREFIXO_BINARIO = {'S', 'B', 'D', '1'};
    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile byte[] chave;

    private CriptografiaDados() { }

    public static String criptografar(String valor) {
        if (valor == null || valor.isEmpty() || valor.startsWith(PREFIXO)) return valor;
        try {
            byte[] nonce = new byte[TAMANHO_NONCE];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(obterChave(), "AES"), new GCMParameterSpec(128, nonce));
            byte[] cifrado = cipher.doFinal(valor.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] resultado = new byte[nonce.length + cifrado.length];
            System.arraycopy(nonce, 0, resultado, 0, nonce.length);
            System.arraycopy(cifrado, 0, resultado, nonce.length, cifrado.length);
            return PREFIXO + Base64.getEncoder().encodeToString(resultado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger os dados pessoais.", e);
        }
    }

    public static String descriptografar(String valor) {
        if (valor == null || !valor.startsWith(PREFIXO)) return valor;
        try {
            byte[] dados = Base64.getDecoder().decode(valor.substring(PREFIXO.length()));
            if (dados.length <= TAMANHO_NONCE) throw new GeneralSecurityException("Conteúdo criptografado incompleto.");
            byte[] nonce = java.util.Arrays.copyOfRange(dados, 0, TAMANHO_NONCE);
            byte[] cifrado = java.util.Arrays.copyOfRange(dados, TAMANHO_NONCE, dados.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(obterChave(), "AES"), new GCMParameterSpec(128, nonce));
            return new String(cipher.doFinal(cifrado), java.nio.charset.StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível descriptografar um dado pessoal. Confira o arquivo .singlebroker-data.key.", e);
        }
    }

    public static byte[] criptografar(byte[] valor) {
        if (valor == null || valor.length == 0 || ehBinarioCriptografado(valor)) return valor;
        try {
            byte[] nonce = new byte[TAMANHO_NONCE];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(obterChave(), "AES"), new GCMParameterSpec(128, nonce));
            byte[] cifrado = cipher.doFinal(valor);
            byte[] resultado = new byte[PREFIXO_BINARIO.length + nonce.length + cifrado.length];
            System.arraycopy(PREFIXO_BINARIO, 0, resultado, 0, PREFIXO_BINARIO.length);
            System.arraycopy(nonce, 0, resultado, PREFIXO_BINARIO.length, nonce.length);
            System.arraycopy(cifrado, 0, resultado, PREFIXO_BINARIO.length + nonce.length, cifrado.length);
            return resultado;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger um anexo.", e);
        }
    }

    public static byte[] descriptografar(byte[] valor) {
        if (!ehBinarioCriptografado(valor)) return valor;
        try {
            int inicio = PREFIXO_BINARIO.length;
            byte[] nonce = java.util.Arrays.copyOfRange(valor, inicio, inicio + TAMANHO_NONCE);
            byte[] cifrado = java.util.Arrays.copyOfRange(valor, inicio + TAMANHO_NONCE, valor.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(obterChave(), "AES"), new GCMParameterSpec(128, nonce));
            return cipher.doFinal(cifrado);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível descriptografar um anexo. Confira .singlebroker-data.key.", e);
        }
    }

    public static boolean ehBinarioCriptografado(byte[] valor) {
        if (valor == null || valor.length < PREFIXO_BINARIO.length + TAMANHO_NONCE + 16) return false;
        for (int i = 0; i < PREFIXO_BINARIO.length; i++) if (valor[i] != PREFIXO_BINARIO[i]) return false;
        return true;
    }

    private static byte[] obterChave() {
        if (chave != null) return chave;
        synchronized (CriptografiaDados.class) {
            if (chave != null) return chave;
            String configurada = System.getProperty("SINGLEBROKER_DATA_KEY");
            if (configurada == null || configurada.isBlank()) configurada = System.getenv("SINGLEBROKER_DATA_KEY");
            if (configurada != null && !configurada.isBlank()) {
                try { chave = Base64.getDecoder().decode(configurada.trim()); }
                catch (IllegalArgumentException e) { throw new IllegalStateException("SINGLEBROKER_DATA_KEY deve ser uma chave Base64 válida.", e); }
            } else {
                chave = carregarOuCriarChaveLocal();
            }
            if (chave.length != 32) throw new IllegalStateException("A chave de dados deve conter 32 bytes (AES-256).");
            return chave;
        }
    }

    public static String chaveBase64() {
        return Base64.getEncoder().encodeToString(obterChave());
    }

    private static byte[] carregarOuCriarChaveLocal() {
        Path arquivo = Path.of(".singlebroker-data.key");
        try {
            if (Files.exists(arquivo)) return Base64.getDecoder().decode(Files.readString(arquivo).trim());
            byte[] nova = new byte[32];
            RANDOM.nextBytes(nova);
            try {
                Files.writeString(arquivo, Base64.getEncoder().encodeToString(nova), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } catch (java.nio.file.FileAlreadyExistsException corrida) {
                return Base64.getDecoder().decode(Files.readString(arquivo).trim());
            }
            return nova;
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível carregar a chave local .singlebroker-data.key. Preserve esse arquivo para manter acesso aos dados criptografados.", e);
        }
    }
}
