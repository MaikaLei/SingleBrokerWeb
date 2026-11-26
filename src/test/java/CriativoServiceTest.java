import model.ImovelModel;
import org.junit.jupiter.api.Test;
import service.CriativoService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriativoServiceTest {

    private final CriativoService service = new CriativoService();

    @Test
    void geraFichaComDadosDoImovel() {
        ImovelModel imovel = new ImovelModel();
        imovel.setId(42L);
        imovel.setTipoImovel("Apartamento");
        imovel.setTransacao("Venda");
        imovel.setCidade("Campo Bom");
        imovel.setEndereco("Rua das Flores");
        imovel.setNumero("120");
        imovel.setBairro("Centro");
        imovel.setEstado("RS");
        imovel.setValor("R$ 450.000,00");
        imovel.setAreaPrivativa(new BigDecimal("80.00"));
        imovel.setApresentacao("Apartamento ensolarado.");
        imovel.setLinkVideoYoutube("https://youtu.be/exemplo");

        String ficha = service.gerar(imovel, "Ficha resumida do imóvel");

        assertTrue(ficha.contains("Apartamento para venda em Campo Bom"));
        assertTrue(ficha.contains("Código: 42"));
        assertTrue(ficha.contains("Rua das Flores, 120"));
        assertTrue(ficha.contains("Área privativa: 80.00 m²"));
        assertTrue(ficha.contains("Apartamento ensolarado."));
        assertTrue(ficha.contains("https://youtu.be/exemplo"));
    }

    @Test
    void rejeitaGeracaoSemImovel() {
        assertThrows(IllegalArgumentException.class,
                () -> service.gerar(null, "Ficha resumida do imóvel"));
    }

    @Test
    void tipoDeCriativoEscolheFormatoDeMensagem() {
        ImovelModel imovel = new ImovelModel();
        imovel.setTipoImovel("Casa");
        imovel.setTransacao("Aluguel");
        imovel.setCidade("Novo Hamburgo");

        String mensagem = service.gerar(imovel, "Texto para mensagem");

        assertTrue(mensagem.startsWith("Olá! Confira este imóvel: Casa para aluguel em Novo Hamburgo."));
        assertTrue(mensagem.contains("Entre em contato para saber mais e agendar uma visita."));
    }
}
