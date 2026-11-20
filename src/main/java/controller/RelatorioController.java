package controller;

import dao.ClienteDao;
import dao.ImovelDao;
import dao.UsuarioDao;
import java.time.LocalDate;
import java.util.List;
import model.ImovelModel;
import model.UsuarioModel;
import service.RelatorioService;

public class RelatorioController {
    public record Resultado(String titulo, String texto, int quantidade) { }
    private final RelatorioService service;

    public RelatorioController() { this(new RelatorioService(new ImovelDao(), new ClienteDao(), new UsuarioDao())); }
    public RelatorioController(RelatorioService service) { this.service = java.util.Objects.requireNonNull(service); }

    public Resultado gerar(String tipo, LocalDate inicio, LocalDate fim, Long usuarioId) {
        RelatorioService.Resultado resultado = service.gerar(tipo, inicio, fim, usuarioId);
        return new Resultado(resultado.titulo(), resultado.texto(), resultado.quantidade());
    }
    public List<UsuarioModel> listarUsuarios() { return service.listarUsuarios(); }
    public List<ImovelModel> listarImoveis() { return service.listarImoveis(); }
    public int totalClientes() { return service.totalClientes(); }
}
