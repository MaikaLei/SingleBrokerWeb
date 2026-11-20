package dao;
public class DocumentoDao extends AnexoDao<model.DocumentoModel> implements repository.DocumentoRepository { public DocumentoDao() { super(model.DocumentoModel.class); } }
