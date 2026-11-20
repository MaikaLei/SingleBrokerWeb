package dao;

import jakarta.persistence.EntityManager;
import model.ClienteModel;
import util.JPAUtil;
import java.util.List;
import model.ClientePfModel;
import model.ClientePjModel;

public class ClienteDao implements repository.ClienteRepository {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ClienteDao.class.getName());

    public void salvar(ClienteModel cliente) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            em.persist(cliente);

            em.getTransaction().commit();
            LOGGER.log(java.util.logging.Level.INFO, "Cliente: transação confirmada no MySQL, código {0}.", cliente.getId());

        } catch (Exception e) {

            if (em.getTransaction().isActive()) em.getTransaction().rollback();

            throw new IllegalStateException("Não foi possível salvar a alteração no banco de dados.", e);

        } finally {

            em.close();

        }
    }

    public List<ClientePfModel> listarPf() {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery(
                    "FROM ClientePfModel",
                    ClientePfModel.class
            ).getResultList();

        } finally {

            em.close();

        }

    }

    public List<ClientePjModel> listarPj() {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery(
                    "FROM ClientePjModel",
                    ClientePjModel.class
            ).getResultList();

        } finally {

            em.close();

        }

    }

    public List<ClientePfModel> buscarPf(
            String nome,
            String cpf,
            String telefone) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery("FROM ClientePfModel c ORDER BY c.nome", ClientePfModel.class)
                    .getResultList().stream()
                    .filter(c -> contem(c.getNome(), nome))
                    .filter(c -> contemDigitos(c.getCpf(), cpf))
                    .filter(c -> contemDigitos(c.getTelefone(), telefone))
                    .toList();

        } finally {

            em.close();

        }
    }

    public List<ClientePjModel> buscarPj(
            String nome,
            String cnpj,
            String telefone) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.createQuery("FROM ClientePjModel c ORDER BY c.razaoSocial", ClientePjModel.class)
                    .getResultList().stream()
                    .filter(c -> contem(c.getRazaoSocial(), nome))
                    .filter(c -> contemDigitos(c.getCnpj(), cnpj))
                    .filter(c -> contemDigitos(c.getTelefone(), telefone))
                    .toList();

        } finally {

            em.close();

        }
    }

    private static boolean contem(String valor, String filtro) {
        return filtro == null || filtro.isBlank() || (valor != null
                && valor.toLowerCase(java.util.Locale.ROOT).contains(filtro.trim().toLowerCase(java.util.Locale.ROOT)));
    }

    private static boolean contemDigitos(String valor, String filtro) {
        if (filtro == null || filtro.isBlank()) return true;
        String esperado = filtro.replaceAll("\\D", "");
        String atual = valor == null ? "" : valor.replaceAll("\\D", "");
        return !esperado.isEmpty() && atual.contains(esperado);
    }

    public ClienteModel buscarPorId(Long id) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            return em.find(ClienteModel.class, id);

        } finally {

            em.close();

        }

    }

    public void atualizar(ClienteModel cliente) {

        EntityManager em = JPAUtil.getEntityManager();

        try {

            em.getTransaction().begin();

            em.merge(cliente);

            em.getTransaction().commit();
            LOGGER.log(java.util.logging.Level.INFO, "Cliente: transação confirmada no MySQL, código {0}.", cliente.getId());

        } catch (Exception e) {

            if (em.getTransaction().isActive()) em.getTransaction().rollback();

            throw new IllegalStateException("Não foi possível salvar a alteração no banco de dados.", e);

        } finally {

            em.close();

        }
    }

}
