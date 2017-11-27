package uk.gov.pay.connector.resources;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.connector.dao.ChargeDao;
import uk.gov.pay.connector.dao.GatewayAccountDao;
import uk.gov.pay.connector.dao.RefundDao;
import uk.gov.pay.connector.model.TransactionsSummaryResponse;
import uk.gov.pay.connector.model.api.ExternalChargeState;
import uk.gov.pay.connector.model.api.ExternalRefundStatus;
import uk.gov.pay.connector.model.domain.ChargeEntity;
import uk.gov.pay.connector.model.domain.ChargeStatus;
import uk.gov.pay.connector.model.domain.RefundEntity;
import uk.gov.pay.connector.model.domain.RefundStatus;
import uk.gov.pay.connector.util.ResponseUtil;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static fj.data.Either.reduce;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.connector.resources.ApiPaths.TRANSACTIONS_SUMMARY_API_PATH;
import static uk.gov.pay.connector.resources.ApiValidators.validateFromDateIsBeforeToDate;
import static uk.gov.pay.connector.resources.ApiValidators.validateGatewayAccountReference;

@Path("/")
public class TransactionsSummaryResource {

    private static final String ACCOUNT_ID = "accountId";
    private static final String FROM_DATE = "from_date";
    private static final String TO_DATE = "to_date";

    private static final List<ChargeStatus> CHARGE_SUCCESS_STATUSES = Arrays.stream(ChargeStatus.values())
            .filter(status -> status.toExternal() == ExternalChargeState.EXTERNAL_SUCCESS)
            .collect(toList());

    private static final List<RefundStatus> REFUND_SUCCESS_STATUSES = Arrays.stream(RefundStatus.values())
            .filter(status -> status.toExternal() == ExternalRefundStatus.EXTERNAL_SUCCESS)
            .collect(toList());

    private final GatewayAccountDao gatewayAccountDao;
    private final ChargeDao chargeDao;
    private final RefundDao refundDao;

    @Inject
    public TransactionsSummaryResource(GatewayAccountDao gatewayAccountDao, ChargeDao chargeDao, RefundDao refundDao) {
        this.gatewayAccountDao = gatewayAccountDao;
        this.chargeDao = chargeDao;
        this.refundDao = refundDao;
    }

    @GET
    @Path(TRANSACTIONS_SUMMARY_API_PATH)
    @Produces(APPLICATION_JSON)
    public Response getPaymentsSummary(@PathParam(ACCOUNT_ID) Long gatewayAccountId,
                                       @QueryParam(FROM_DATE) String fromDate,
                                       @QueryParam(TO_DATE) String toDate) {
        return reduce(validateGatewayAccountReference(gatewayAccountDao, gatewayAccountId)
                .bimap(ResponseUtil::notFoundResponse,
                        success -> reduce(validateFromDateIsBeforeToDate(FROM_DATE, fromDate, TO_DATE, toDate)
                                .bimap(ResponseUtil::badRequestResponse,
                                        fromDateAndToDate -> summarisePaymentsAndRefunds(gatewayAccountId, fromDateAndToDate)))));
    }

    private Response summarisePaymentsAndRefunds(Long gatewayAccountId, Pair<ZonedDateTime, ZonedDateTime> fromDateAndToDate) {
        List<ChargeEntity> successfulPayments = chargeDao.findByAccountBetweenDatesWithStatusIn(
                gatewayAccountId,
                fromDateAndToDate.getLeft(),
                fromDateAndToDate.getRight(),
                CHARGE_SUCCESS_STATUSES);

        int successfulPaymentsCount = successfulPayments.size();
        long successfulPaymentsTotalInPence = successfulPayments.stream().mapToLong(ChargeEntity::getAmount).sum();

        List<RefundEntity> successfulRefunds = refundDao.findByAccountBetweenDatesWithStatusIn(
                gatewayAccountId,
                fromDateAndToDate.getLeft(),
                fromDateAndToDate.getRight(),
                REFUND_SUCCESS_STATUSES);

        int successfulRefundsCount = successfulRefunds.size();
        long successfulRefundsTotalInPence = successfulRefunds.stream().mapToLong(RefundEntity::getAmount).sum();

        long netIncome = successfulPaymentsTotalInPence - successfulRefundsTotalInPence;

        TransactionsSummaryResponse response = new TransactionsSummaryResponse(successfulPaymentsCount, successfulPaymentsTotalInPence,
                successfulRefundsCount, successfulRefundsTotalInPence, netIncome);

        return Response.ok(response).build();
    }

}
