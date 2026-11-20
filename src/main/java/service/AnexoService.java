package service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import model.AnexoModel;
import model.DocumentoModel;
import model.FotoModel;
import repository.DocumentoRepository;
import repository.FotoRepository;

public class AnexoService {
    private final FotoRepository fotos;
    private final DocumentoRepository documentos;

    public AnexoService(FotoRepository fotos, DocumentoRepository documentos) {
        this.fotos = Objects.requireNonNull(fotos);
        this.documentos = Objects.requireNonNull(documentos);
    }

    public AnexoModel ler(Path arquivo, boolean foto) {
        try {
            if (!Files.isRegularFile(arquivo) || Files.size(arquivo) == 0 || Files.size(arquivo) > 5 * 1024 * 1024)
                throw new IllegalArgumentException("Escolha um arquivo de até 5 MB, não vazio.");
            byte[] conteudo = Files.readAllBytes(arquivo);
            if (foto && javax.imageio.ImageIO.read(new ByteArrayInputStream(conteudo)) == null)
                throw new IllegalArgumentException("Escolha uma imagem JPG, PNG ou GIF válida.");
            AnexoModel anexo = foto ? new FotoModel() : new DocumentoModel();
            anexo.setNome(arquivo.getFileName().toString());
            anexo.setConteudo(conteudo);
            return anexo;
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo.", e);
        }
    }

    public List<FotoModel> listarFotos(Long imovelId) { return fotos.listar(imovelId); }
    public List<DocumentoModel> listarDocumentos(Long imovelId) { return documentos.listar(imovelId); }
    public void salvarFotos(Long imovelId, List<FotoModel> novos, List<Long> removidos) { fotos.salvar(imovelId, novos, removidos); }
    public void salvarDocumentos(Long imovelId, List<DocumentoModel> novos, List<Long> removidos) { documentos.salvar(imovelId, novos, removidos); }
}
