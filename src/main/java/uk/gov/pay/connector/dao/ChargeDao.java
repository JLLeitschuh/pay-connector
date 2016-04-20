package uk.gov.pay.connector.dao;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import uk.gov.pay.connector.model.domain.ChargeEntity;
import uk.gov.pay.connector.model.domain.ChargeEventEntity;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public class ChargeDao extends JpaDao<ChargeEntity> {

    private EventDao eventDao;
    public static final String STATUS = "status";
    public static final String CREATED_DATE = "createdDate";

    @Inject
    public ChargeDao(final Provider<EntityManager> entityManager, EventDao eventDao) {
        super(entityManager);
        this.eventDao = eventDao;
    }

    public List<ChargeEntity> findAllBy(ChargeSearch searchQuery) {
        TypedQuery<ChargeEntity> query = searchQuery.apply(entityManager.get());
        return query.getResultList();
    }

    public Optional<ChargeEntity> findById(Long chargeId) {
        return super.findById(ChargeEntity.class, chargeId);
    }

    public Optional<ChargeEntity> findByExternalId(String externalId) {

        String query = "SELECT c FROM ChargeEntity c " +
                "WHERE c.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, ChargeEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }

    public Optional<ChargeEntity> findByTokenId(String tokenId) {
        String query = "SELECT te.chargeEntity FROM TokenEntity te WHERE te.token=:tokenId";

        return entityManager.get()
                .createQuery(query, ChargeEntity.class)
                .setParameter("tokenId", tokenId)
                .getResultList()
                .stream()
                .findFirst();
    }

    public Optional<ChargeEntity> findByExternalIdAndGatewayAccount(String externalId, Long accountId) {
        return findByExternalId(externalId).filter(charge -> charge.isAssociatedTo(accountId));
    }

    public Optional<ChargeEntity> findByProviderAndTransactionId(String provider, String transactionId) {

        String query = "SELECT c FROM ChargeEntity c " +
                "WHERE c.gatewayTransactionId = :gatewayTransactionId " +
                "AND c.gatewayAccount.gatewayName = :provider";

        return entityManager.get()
                .createQuery(query, ChargeEntity.class)
                .setParameter("gatewayTransactionId", transactionId)
                .setParameter("provider", provider).getResultList().stream().findFirst();
    }

    // Temporary methods until notification listeners are in place
    public void persist(ChargeEntity chargeEntity) {
        super.persist(chargeEntity);
        eventDao.persist(ChargeEventEntity.from(chargeEntity, ChargeStatus.CREATED, chargeEntity.getCreatedDate()));
    }

    public List<ChargeEntity> findBeforeDateWithStatusIn(ZonedDateTime date, List<ChargeStatus> statuses) {
        CriteriaBuilder cb = entityManager.get().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root entity = cq.from(ChargeEntity.class);

        Expression expression = entity.get(STATUS).in(statuses);
        cq.where(cb.lessThan(entity.get(CREATED_DATE), date),
                entity.get(STATUS).in(statuses));

        Query query = entityManager.get().createQuery(cq);
        return query.getResultList();
    }

    public void mergeAndNotifyStatusHasChanged(ChargeEntity chargeEntity) {
        super.merge(chargeEntity);
        eventDao.persist(ChargeEventEntity.from(chargeEntity, ChargeStatus.chargeStatusFrom(chargeEntity.getStatus()), ZonedDateTime.now()));
    }
}