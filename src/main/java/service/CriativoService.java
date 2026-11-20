package service;

import model.ImovelModel;

public class CriativoService {
    private String texto(Object valor) { return valor == null ? "Não informado" : valor.toString(); }

    public String gerar(ImovelModel imovel, String tipo) {
        if (imovel == null) throw new IllegalArgumentException("Selecione um imóvel.");
        String resumo = texto(imovel.getTipoImovel()) + " para " + texto(imovel.getTransacao()).toLowerCase() + " em " + texto(imovel.getCidade());
        String dados = "Código: " + imovel.getId() + "\nValor: " + texto(imovel.getValor()) + "\nBairro: " + texto(imovel.getBairro())
                + "\nÁrea privativa: " + texto(imovel.getAreaPrivativa()) + " m²\nDormitórios: " + texto(imovel.getDormitorios())
                + " | Banheiros: " + texto(imovel.getBanheiros()) + " | Vagas: " + texto(imovel.getVagasGaragem());
        String descricao = imovel.getApresentacao() == null ? "" : imovel.getApresentacao();
        return switch (tipo) {
            case "Texto para mensagem" -> "Olá! Confira este imóvel: " + resumo + ".\n\n" + dados + "\n\n" + descricao + "\n\nEntre em contato para saber mais e agendar uma visita.";
            case "Ficha resumida do imóvel" -> ficha(imovel, resumo, dados);
            case "Legenda Redes Sociais" -> resumo + "\n\n" + dados + "\n\n" + descricao + "\n\nSaiba mais: entre em contato.\n#Imóveis #CorretorDeImóveis";
            default -> resumo + "\n\n" + descricao + "\n\n" + dados + "\n\nAgende uma visita para conhecer o imóvel.";
        };
    }

    private String ficha(ImovelModel imovel, String resumo, String dados) {
        String endereco = texto(imovel.getEndereco()) + ", " + texto(imovel.getNumero())
                + (imovel.getComplemento() == null || imovel.getComplemento().isBlank() ? "" : " - " + imovel.getComplemento())
                + " | " + texto(imovel.getBairro()) + " | " + texto(imovel.getCidade()) + "/" + texto(imovel.getEstado());
        String areas = "Área privativa: " + texto(imovel.getAreaPrivativa()) + " m²"
                + " | Área total: " + texto(imovel.getAreaTotal()) + " m²"
                + " | Terreno: " + texto(imovel.getAreaTerreno()) + " m²";
        String caracteristicas = "Salas: " + texto(imovel.getSalas()) + " | Suítes: " + texto(imovel.getSuites())
                + " | Garagens: " + texto(imovel.getVagasGaragem());
        return resumo + "\n\nENDEREÇO\n" + endereco + "\n\nVALORES E SITUAÇÃO\n" + dados
                + "\nCondomínio: " + texto(imovel.getValorCondominio()) + "\nIPTU: " + texto(imovel.getValorIptu())
                + "\nStatus: " + texto(imovel.getStatusImovel()) + "\n\nCARACTERÍSTICAS\n" + areas + "\n" + caracteristicas
                + "\n\nDESCRIÇÃO\n" + texto(imovel.getApresentacao())
                + (imovel.getLinkVideoYoutube() == null ? "" : "\n\nVídeo: " + imovel.getLinkVideoYoutube());
    }
}
