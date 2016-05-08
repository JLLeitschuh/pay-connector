package uk.gov.pay.connector.dao;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import uk.gov.pay.connector.model.domain.ChargeEntity;
import uk.gov.pay.connector.model.domain.ChargeEventEntity;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.time.ZonedDateTime;
import java.util.*;

@Transactional
public class ChargeDao extends JpaDao<ChargeEntity> {

    private static final String STATUS = "status";
    private static final String CREATED_DATE = "createdDate";
    private static final String GATEWAY_ACCOUNT = "gatewayAccount";
    private static final String REFERENCE = "reference";

    private EventDao eventDao;

    @Inject
    public ChargeDao(final Provider<EntityManager> entityManager, EventDao eventDao) {
        super(entityManager);
        this.eventDao = eventDao;
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
        ChargeSearchParams params = new ChargeSearchParams()
                .withToDate(date)
                .withInternalChargeStatuses(statuses);
        return findAllBy(params);
    }

    public List<ChargeEntity> findAllBy(ChargeSearchParams params) {
        CriteriaBuilder cb = entityManager.get().getCriteriaBuilder();
        CriteriaQuery<ChargeEntity> cq = cb.createQuery(ChargeEntity.class);
        Root<ChargeEntity> charge = cq.from(ChargeEntity.class);

        List<Predicate> predicates = buildParamPredicates(params, cb, charge);

        cq.select(charge)
                .where(predicates.toArray(new Predicate[]{}))
                .orderBy(cb.desc(charge.get(CREATED_DATE)));

        Query query = entityManager.get().createQuery(cq);
        Long firstResult = params.getPage() * params.getDisplaySize();
        query.setFirstResult(firstResult.intValue());
        query.setMaxResults(params.getDisplaySize().intValue());

        return query.getResultList();
    }

    public ChargeEntity mergeAndNotifyStatusHasChanged(ChargeEntity chargeEntity) {
        ChargeEntity mergedCharge = super.merge(chargeEntity);
        eventDao.persist(ChargeEventEntity.from(chargeEntity, ChargeStatus.fromString(chargeEntity.getStatus()), ZonedDateTime.now()));
        return mergedCharge;
    }

    private List<Predicate> buildParamPredicates(ChargeSearchParams params, CriteriaBuilder cb, Root<ChargeEntity> charge) {
        List<Predicate> predicates = new ArrayList<>();
        if (params.getGatewayAccountId() != null)
            predicates.add(cb.equal(charge.get(GATEWAY_ACCOUNT).get("id"), params.getGatewayAccountId()));
        if (params.getReference() != null)
            predicates.add(cb.like(charge.get(REFERENCE), '%'+params.getReference()+'%'));
        if (params.getChargeStatuses() != null && !params.getChargeStatuses().isEmpty())
            predicates.add(charge.get(STATUS).in(params.getChargeStatuses()));
        if (params.getFromDate() != null)
            predicates.add(cb.greaterThanOrEqualTo(charge.get(CREATED_DATE), params.getFromDate()));
        if (params.getToDate() != null)
            predicates.add(cb.lessThan(charge.get(CREATED_DATE), params.getToDate()));

        return predicates;
    }
}
