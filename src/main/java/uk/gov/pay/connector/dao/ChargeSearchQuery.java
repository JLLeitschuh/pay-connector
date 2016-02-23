package uk.gov.pay.connector.dao;

import com.google.common.base.Preconditions;
import uk.gov.pay.connector.model.domain.ChargeEntity;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChargeSearchQuery {

    private static final String REFERENCE = "reference";
    private static final String STATUSES = "statuses";
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final Map<String, String> QUERY_DEFINITIONS = new HashMap<String, String>() {{
        put(REFERENCE, " AND c.reference LIKE :reference");
        put(FROM_DATE, " AND c.createdDate >= :fromDate");
        put(STATUSES, " AND c.status IN :statuses");
        put(TO_DATE, " AND c.createdDate <= :toDate");
    }};

    private Long gatewayAccountId;
    private Map<String, Object> queryParameters = new HashMap<>();

    public ChargeSearchQuery(Long gatewayAccountId) {
        Preconditions.checkNotNull(gatewayAccountId);
        this.gatewayAccountId = gatewayAccountId;
    }

    public ChargeSearchQuery withReferenceLike(String reference) {
        queryParameters.put(REFERENCE, "%" + reference + "%");
        return this;
    }

    public ChargeSearchQuery withStatusIn(ChargeStatus... statuses) {
        queryParameters.put(STATUSES, Arrays.asList(statuses));
        return this;
    }

    public ChargeSearchQuery withCreatedDateFrom(ZonedDateTime fromDate) {
        queryParameters.put(FROM_DATE, fromDate);
        return this;
    }

    public ChargeSearchQuery withCreatedDateTo(ZonedDateTime toDate) {
        queryParameters.put(TO_DATE, toDate);
        return this;
    }

    public TypedQuery<ChargeEntity> apply(EntityManager entityManager) {
        TypedQuery<ChargeEntity> typedQuery = entityManager.createQuery(buildQueryDefinition(), ChargeEntity.class);
        applyParametersTo(typedQuery);
        return typedQuery;
    }

    private void applyParametersTo(TypedQuery<ChargeEntity> typedQuery) {
        typedQuery.setParameter("gatewayAccountId", gatewayAccountId);
        queryParameters.forEach(typedQuery::setParameter);
    }

    private String buildQueryDefinition() {
        StringBuilder query = new StringBuilder("SELECT c " +
                "FROM ChargeEntity c " +
                "WHERE c.gatewayAccount.id = :gatewayAccountId");
        queryParameters.forEach((parameterName, parameterValue) -> query.append(QUERY_DEFINITIONS.get(parameterName)));
        query.append(" ORDER BY c.id DESC");
        return query.toString();
    }
}
