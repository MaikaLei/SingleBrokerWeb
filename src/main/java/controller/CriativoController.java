package controller;

import java.util.List;
import model.ImovelModel;
import repository.ImovelRepository;
import dao.ImovelDao;
import service.CriativoService;

public class CriativoController {
    private final CriativoService service;
    private final ImovelRepository imoveis;
    public CriativoController() { this(new CriativoService(), new ImovelDao()); }
    public CriativoController(CriativoService service, ImovelRepository imoveis) {
        this.service = java.util.Objects.requireNonNull(service);
        this.imoveis = java.util.Objects.requireNonNull(imoveis);
    }
    public String gerar(ImovelModel imovel, String tipo) { return service.gerar(imovel, tipo); }
    public List<ImovelModel> listarImoveis() { return imoveis.listar(); }
}
