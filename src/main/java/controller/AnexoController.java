package controller;

import dao.DocumentoDao;
import dao.FotoDao;
import java.nio.file.Path;
import java.util.List;
import model.AnexoModel;
import model.DocumentoModel;
import model.FotoModel;
import service.AnexoService;

public class AnexoController {
    private final AnexoService service;
    public AnexoController() { this(new AnexoService(new FotoDao(), new DocumentoDao())); }
    public AnexoController(AnexoService service) { this.service = java.util.Objects.requireNonNull(service); }
    public AnexoModel ler(Path arquivo, boolean foto) { return service.ler(arquivo, foto); }
    public List<FotoModel> listarFotos(Long imovelId) { return service.listarFotos(imovelId); }
    public List<DocumentoModel> listarDocumentos(Long imovelId) { return service.listarDocumentos(imovelId); }
    public void salvarFotos(Long imovelId, List<FotoModel> novos, List<Long> removidos) { service.salvarFotos(imovelId, novos, removidos); }
    public void salvarDocumentos(Long imovelId, List<DocumentoModel> novos, List<Long> removidos) { service.salvarDocumentos(imovelId, novos, removidos); }
}
