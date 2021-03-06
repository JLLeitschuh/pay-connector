package uk.gov.pay.connector.tasks;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.pay.connector.charge.model.domain.ChargeEntity;
import uk.gov.pay.connector.charge.model.domain.ChargeStatus;
import uk.gov.pay.connector.charge.model.domain.ParityCheckStatus;
import uk.gov.pay.connector.charge.service.ChargeService;
import uk.gov.pay.connector.paritycheck.LedgerService;
import uk.gov.pay.connector.paritycheck.LedgerTransaction;
import uk.gov.pay.connector.refund.dao.RefundDao;
import uk.gov.pay.connector.refund.model.domain.RefundEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static uk.gov.pay.connector.charge.model.domain.ParityCheckStatus.DATA_MISMATCH;
import static uk.gov.pay.connector.charge.model.domain.ParityCheckStatus.EXISTS_IN_LEDGER;
import static uk.gov.pay.connector.charge.model.domain.ParityCheckStatus.MISSING_IN_LEDGER;
import static uk.gov.pay.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;

public class ParityCheckService {

    public static final String FIELD_NAME = "field_name";
    private static final Logger logger = LoggerFactory.getLogger(ParityCheckService.class);
    private LedgerService ledgerService;
    private ChargeService chargeService;
    private RefundDao refundDao;
    private HistoricalEventEmitter historicalEventEmitter;

    @Inject
    public ParityCheckService(LedgerService ledgerService, ChargeService chargeService,
                              RefundDao refundDao,
                              HistoricalEventEmitter historicalEventEmitter) {
        this.ledgerService = ledgerService;
        this.chargeService = chargeService;
        this.refundDao = refundDao;
        this.historicalEventEmitter = historicalEventEmitter;
    }

    public ParityCheckStatus getChargeParityCheckStatus(ChargeEntity chargeEntity) {
        Optional<LedgerTransaction> transaction = ledgerService.getTransaction(chargeEntity.getExternalId());

        return checkParity(chargeEntity, transaction.orElse(null));
    }

    public ParityCheckStatus getChargeAndRefundsParityCheckStatus(ChargeEntity charge) {
        ParityCheckStatus parityCheckStatus = getChargeParityCheckStatus(charge);
        if (parityCheckStatus.equals(EXISTS_IN_LEDGER)) {
            return getRefundsParityCheckStatus(refundDao.findRefundsByChargeExternalId(charge.getExternalId()));
        }

        return parityCheckStatus;
    }

    public ParityCheckStatus getParityCheckStatus(Optional<LedgerTransaction> transaction, String externalChargeState) {
        if (transaction.isEmpty()) {
            return MISSING_IN_LEDGER;
        }

        if (externalChargeState.equalsIgnoreCase(transaction.get().getState().getStatus())) {
            return EXISTS_IN_LEDGER;
        }

        return DATA_MISMATCH;
    }


    public ParityCheckStatus getRefundsParityCheckStatus(List<RefundEntity> refunds) {
        for (var refund : refunds) {
            var transaction = ledgerService.getTransaction(refund.getExternalId());
            ParityCheckStatus parityCheckStatus = getParityCheckStatus(transaction, refund.getStatus().toExternal().getStatus());
            if (!parityCheckStatus.equals(EXISTS_IN_LEDGER)) {
                logger.info("refund transaction does not exist in ledger or is in a different state [externalId={},status={}] -",
                        refund.getExternalId(), parityCheckStatus);
                return parityCheckStatus;
            }
        }

        return EXISTS_IN_LEDGER;
    }

    @Transactional
    public boolean parityCheckChargeForExpunger(ChargeEntity chargeEntity) {
        ParityCheckStatus parityCheckStatus = getChargeParityCheckStatus(chargeEntity);

        //TODO (kbottla) PP-6098: to be replaced by `MATCHES_WITH_LEDGER`
        if (EXISTS_IN_LEDGER.equals(parityCheckStatus)) {
            return true;
        }

        // force emit and update charge status
        historicalEventEmitter.processPaymentEvents(chargeEntity, true);
        chargeService.updateChargeParityStatus(chargeEntity.getExternalId(), parityCheckStatus);

        return false;
    }

    private ParityCheckStatus checkParity(ChargeEntity chargeEntity, LedgerTransaction transaction) {
        String externalId = chargeEntity.getExternalId();
        ParityCheckStatus parityCheckStatus;

        MDC.put(PAYMENT_EXTERNAL_ID, externalId);

        if (transaction == null) {
            logger.info("Transaction missing in Ledger for Charge");
            parityCheckStatus = MISSING_IN_LEDGER;
        } else {
            boolean fieldsMatch;

            fieldsMatch = matchCommonFields(chargeEntity, transaction);

            if (fieldsMatch) {
                parityCheckStatus = EXISTS_IN_LEDGER;
            } else {
                parityCheckStatus = DATA_MISMATCH;
            }
        }
        MDC.remove(PAYMENT_EXTERNAL_ID);
        return parityCheckStatus;
    }

    private boolean matchCommonFields(ChargeEntity chargeEntity, LedgerTransaction transaction) {
        boolean fieldsMatch = isEquals(chargeEntity.getExternalId(), transaction.getTransactionId(), "external_id");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getAmount(), transaction.getAmount(), "amount");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getDescription(), transaction.getDescription(), "description");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getReference().toString(), transaction.getReference(), "reference");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getLanguage(), transaction.getLanguage(), "language");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getEmail(), transaction.getEmail(), "email");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getReturnUrl(), transaction.getReturnUrl(), "return_url");
        fieldsMatch = fieldsMatch && isEquals(chargeEntity.getGatewayTransactionId(), transaction.getGatewayTransactionId(), "gateway_transaction_id");

        String chargeExternalStatus = ChargeStatus.fromString(chargeEntity.getStatus()).toExternal().getStatusV2();
        fieldsMatch = fieldsMatch && isEquals(chargeExternalStatus, transaction.getState().getStatus(), "status");

        return fieldsMatch;
    }

    private boolean isEquals(Object value1, Object value2, String fieldName) {
        if (Objects.equals(value1, value2)) {
            return true;
        } else {
            logger.info("Field value does not match between ledger and connector",
                    kv(FIELD_NAME, fieldName));
            return false;
        }
    }
}
