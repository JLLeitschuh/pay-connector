package uk.gov.pay.connector.dao;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.connector.model.domain.ChargeEntity;
import uk.gov.pay.connector.model.domain.ChargeEventEntity;
import uk.gov.pay.connector.model.domain.ChargeStatus;
import uk.gov.pay.connector.util.ChargeEventJpaListener;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.pay.connector.model.domain.ChargeStatus.CREATED;

public class ChargeJpaDao extends JpaDao<ChargeEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ChargeJpaDao.class);

    private ChargeEventJpaListener eventListener;

    @Inject
    public ChargeJpaDao(final Provider<EntityManager> entityManager, ChargeEventJpaListener eventListener) {
        super(entityManager);
        this.eventListener = eventListener;
    }

    @Transactional
    public void persist(ChargeEntity charge) {
        super.persist(charge);
        eventListener.notify(ChargeEventEntity.from(charge, CREATED, charge.getCreatedDate().toLocalDateTime()));
    }

    @Transactional
    public ChargeEntity merge(final ChargeEntity charge) {
        ChargeEntity updated = super.merge(charge);
        eventListener.notify(ChargeEventEntity.from(
                charge,
                ChargeStatus.chargeStatusFrom(charge.getStatus()),
                LocalDateTime.now()));
        return updated;
    }

    public <ID> Optional<ChargeEntity> findById(final ID id) {
        return super.findById(ChargeEntity.class, id);
    }


    public Optional<ChargeEntity> findByGatewayTransactionIdAndProvider(String transactionId, String paymentProvider) {
        TypedQuery<ChargeEntity> query = entityManager.get()
                .createQuery("select c from ChargeEntity c where c.gatewayTransactionId = :gatewayTransactionId and c.gatewayAccount.gatewayName = :paymentProvider", ChargeEntity.class);

        query.setParameter("gatewayTransactionId", transactionId);
        query.setParameter("paymentProvider", paymentProvider);

        return Optional.ofNullable(query.getSingleResult());
    }

    public List<ChargeEntity> findAllBy(ChargeSearchQueryBuilder queryBuilder) {
        TypedQuery<ChargeEntity> query = queryBuilder.buildWith(entityManager);
        return query.getResultList();
    }

    // updates the new status only if the charge is in one of the old statuses and returns num of rows affected
    // very specific transition happening here so check for a valid state before transitioning
    @Transactional
    public void updateNewStatusWhereOldStatusIn(Long chargeId, ChargeStatus newStatus, List<ChargeStatus> oldStatuses) {

        ChargeEntity chargeEntity = findById(chargeId).get();
        String status = chargeEntity.getStatus();

        if(oldStatuses.contains(ChargeStatus.chargeStatusFrom(status))){
            chargeEntity.setStatus(newStatus);
            eventListener.notify(ChargeEventEntity.from(chargeEntity, newStatus, LocalDateTime.now()));
        }
    }

    @Transactional
    public void updateStatus(Long chargeId, ChargeStatus newStatus) {

        ChargeEntity chargeEntity = findById(chargeId).get();
        chargeEntity.setStatus(newStatus);
        eventListener.notify(ChargeEventEntity.from(chargeEntity, newStatus, LocalDateTime.now()));

        int updateCount = entityManager.get()
                .createQuery("UPDATE ChargeEntity c SET c.status=:newStatus WHERE c.id=:chargeId", ChargeEntity.class)
                .setParameter("chargeId", chargeId)
                .setParameter("newStatus", newStatus.getValue())
                .executeUpdate();
        if (updateCount != 1) {
            throw new PayDBIException(format("Could not update charge '%s' with status %s, updated %d rows.", chargeId, newStatus, updateCount));
        }
        entityManager.get().flush();
        entityManager.get().clear();

    }

    private String getStringFromStatusList(List<ChargeStatus> oldStatuses) {
        return oldStatuses
                .stream()
                .map(t -> "'" + t.getValue() + "'")
                .collect(Collectors.joining(","));
    }

}


