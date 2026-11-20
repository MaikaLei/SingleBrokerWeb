package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import model.ImovelModel;
import util.JPAUtil;

/**
 *
 * @author Maikon
 */
public class ImovelDao implements repository.ImovelRepository {

    public void salvar(ImovelModel imovel) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            em.persist(imovel);

            em.getTransaction().commit();

        } catch (Exception e) {

            if (em.getTransaction().isActive()) em.getTransaction().rollback();

            throw e;

        } finally {

            em.close();

        }
    }

    public List<ImovelModel> listar() {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            TypedQuery<ImovelModel> query = em.createQuery(
                    "FROM ImovelModel ORDER BY id DESC",
                    ImovelModel.class);

            return query.getResultList();

        } finally {

            em.close();

        }
    }

    public ImovelModel buscarPorId(Long id) {

        EntityManager em = JPAUtil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT i FROM ImovelModel i "
                    + "LEFT JOIN FETCH i.proprietario "
                    + "WHERE i.id = :id",
                    ImovelModel.class
            )
                    .setParameter("id", id)
                    .getSingleResult();

        } finally {
            em.close();
        }
    }

    public void atualizar(ImovelModel imovel) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            em.merge(imovel);

            em.getTransaction().commit();

        } catch (Exception e) {

            if (em.getTransaction().isActive()) em.getTransaction().rollback();

            throw e;

        } finally {

            em.close();

        }
    }

    public List<ImovelModel> buscar(
            String codigo,
            String status,
            String transacao,
            String tipo,
            String cidade,
            String bairro) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            String jpql
                    = "SELECT i FROM ImovelModel i "
                    + "WHERE 1=1 ";

            if (!codigo.isBlank()) {

                jpql += "AND i.id = :id ";
            }

            if (!status.equals("Status")
                    && !status.equals("Todos")) {

                jpql += "AND i.statusImovel = :status ";
            }

            if (!transacao.equals("Transação")) {

                jpql += "AND i.transacao = :transacao ";
            }

            if (!tipo.equals("Tipo")) {

                jpql += "AND i.tipoImovel = :tipo ";
            }

            if (!cidade.equals("Cidade")) {

                jpql += "AND i.cidade = :cidade ";
            }

            if (bairro != null && !bairro.isBlank() && !bairro.equals("Bairro")) {
                jpql += "AND UPPER(i.bairro) LIKE :bairro ";
            }

            jpql += "ORDER BY i.id DESC";

            TypedQuery<ImovelModel> query
                    = em.createQuery(
                            jpql,
                            ImovelModel.class);

            if (!codigo.isBlank()) {

                query.setParameter(
                        "id",
                        Long.valueOf(codigo));
            }

            if (!status.equals("Status")
                    && !status.equals("Todos")) {

                query.setParameter(
                        "status",
                        status);
            }

            if (!transacao.equals("Transação")) {

                query.setParameter(
                        "transacao",
                        transacao);
            }

            if (!tipo.equals("Tipo")) {

                query.setParameter(
                        "tipo",
                        tipo);
            }

            if (!cidade.equals("Cidade")) {

                query.setParameter(
                        "cidade",
                        cidade);
            }

            if (bairro != null && !bairro.isBlank() && !bairro.equals("Bairro")) {
                query.setParameter("bairro", "%" + bairro.trim().toUpperCase(java.util.Locale.ROOT) + "%");
            }

            return query.getResultList();

        } finally {

            em.close();

        }
    }

    @Override
    public boolean existeEndereco(String endereco, String numero, String complemento,
            String cidade, String estado, Long ignorarId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(i) FROM ImovelModel i WHERE "
                    + "UPPER(TRIM(i.endereco)) = :endereco "
                    + "AND COALESCE(UPPER(TRIM(i.numero)), '') = :numero "
                    + "AND COALESCE(UPPER(TRIM(i.complemento)), '') = :complemento "
                    + "AND UPPER(TRIM(i.cidade)) = :cidade "
                    + "AND UPPER(TRIM(i.estado)) = :estado";
            if (ignorarId != null) jpql += " AND i.id <> :ignorarId";
            var query = em.createQuery(jpql, Long.class)
                    .setParameter("endereco", endereco.trim().toUpperCase(java.util.Locale.ROOT))
                    .setParameter("numero", normalizar(numero))
                    .setParameter("complemento", normalizar(complemento))
                    .setParameter("cidade", cidade.trim().toUpperCase(java.util.Locale.ROOT))
                    .setParameter("estado", estado.trim().toUpperCase(java.util.Locale.ROOT));
            if (ignorarId != null) query.setParameter("ignorarId", ignorarId);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }
}
