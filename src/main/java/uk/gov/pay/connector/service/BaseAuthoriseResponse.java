package uk.gov.pay.connector.service;

import uk.gov.pay.connector.model.GatewayParamsFor3ds;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import java.util.Optional;

public interface BaseAuthoriseResponse extends BaseResponse {

    String getTransactionId();

    AuthoriseStatus authoriseStatus();

    Optional<? extends GatewayParamsFor3ds> getGatewayParamsFor3ds();

    enum AuthoriseStatus {
        SUBMITTED(ChargeStatus.AUTHORISATION_SUBMITTED),
        AUTHORISED(ChargeStatus.AUTHORISATION_SUCCESS),
        REJECTED(ChargeStatus.AUTHORISATION_REJECTED),
        REQUIRES_3DS(ChargeStatus.AUTHORISATION_3DS_REQUIRED),
        CANCELLED(ChargeStatus.AUTHORISATION_CANCELLED),
        ERROR(ChargeStatus.AUTHORISATION_ERROR);

        ChargeStatus mappedChargeStatus;

        AuthoriseStatus(ChargeStatus status) {
            mappedChargeStatus = status;
        }

        public ChargeStatus getMappedChargeStatus() {
            return mappedChargeStatus;
        }
    }

}
