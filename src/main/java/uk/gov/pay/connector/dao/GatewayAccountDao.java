package uk.gov.pay.connector.dao;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import uk.gov.pay.connector.model.domain.EmailNotificationEntity;
import uk.gov.pay.connector.model.domain.GatewayAccountEntity;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Optional;

@Transactional
public class GatewayAccountDao extends JpaDao<GatewayAccountEntity> {

    @Inject
    public GatewayAccountDao(final Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public Optional<GatewayAccountEntity> findById(Long gatewayAccountId) {
        return super.findById(GatewayAccountEntity.class, gatewayAccountId);
    }

    @Override
    public void persist(final GatewayAccountEntity account) {
        entityManager.get().persist(account);                                                                                                                                                                                                                                                                                                                                                                                                           
        account.setEmailNotification(new EmailNotificationEntity(account));
    }

    public Optional<GatewayAccountEntity> findByNotificationCredentialsUsername(String username) {
        String query = "SELECT gae FROM GatewayAccountEntity gae " +
                "WHERE gae.notificationCredentials.userName = :username";


        return entityManager.get()
                .createQuery(query, GatewayAccountEntity.class)
                .setParameter("username", username)
                .getResultList().stream().findFirst();
    }
}
