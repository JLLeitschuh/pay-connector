package uk.gov.pay.connector.service.epdq;

import com.google.common.base.Strings;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import uk.gov.pay.connector.model.ChargeStatusRequest;
import uk.gov.pay.connector.model.domain.ChargeStatus;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public class EpdqNotification implements ChargeStatusRequest {

    public static final String SHASIGN = "SHASIGN";
    public static final String PAYID = "PAYID";
    public static final String PAYIDSUB = "PAYIDSUB";
    public static final String STATUS = "STATUS";

    private final List<NameValuePair> paramsList;

    private final String status;
    private final String payId;
    private final String payIdSub;
    private final String shaSign;

    public enum StatusCode {
        EPDQ_AUTHORISATION_REFUSED("2"),
        EPDQ_AUTHORISED("5"),
        EPDQ_AUTHORISED_CANCELLED("6"),
        EPDQ_PAYMENT_DELETED("7"),
        EPDQ_DELETION_REFUSED("73"),
        EPDQ_REFUND("8"),
        EPDQ_REFUND_REFUSED("83"),
        EPDQ_PAYMENT_REQUESTED("9"),
        EPDQ_REFUND_DECLINED_BY_ACQUIRER("94"),
        UNKNOWN("");

        public String getCode() {
            return code;
        }

        public final String code;

        StatusCode(final String code) {
            this.code = code;
        }

        public static StatusCode byCode(String code) {
            return Arrays.stream(StatusCode.values()).filter(c -> c.getCode().equals(code)).findFirst().orElse(UNKNOWN);
        }

        public String toString() {
            return this.name();
        }
    }

    private Optional<ChargeStatus> chargeStatus = Optional.empty();

    public class EpdqParseException extends Exception {
        public EpdqParseException(String message) {
            super(message);
        }
    }

    public EpdqNotification(String payload) throws EpdqParseException {
        try {
            paramsList = URLEncodedUtils.parse(payload, StandardCharsets.UTF_8);

            Map<String, String> params = paramsList.stream()
                    .collect(toMap(NameValuePair::getName, NameValuePair::getValue));

            status = params.get(STATUS);
            payId = params.get(PAYID);
            payIdSub = params.get(PAYIDSUB);
            shaSign = params.get(SHASIGN);
        } catch (Exception e) {
            throw new EpdqParseException(
                    "Could not decode ePDQ notification payload as UTF-8 application/x-www-form-urlencoded");
        }
    }

    public List<NameValuePair> getParams() {
        return paramsList;
    }

    public String getShaSign() {
        return shaSign;
    }

    public String getPayId() {
        return payId;
    }

    public String getPayIdSub() {
        return payIdSub;
    }

    public String getReference() {
        if (Strings.isNullOrEmpty(payId) || Strings.isNullOrEmpty(payIdSub)) {
            return "";
        }
        return payId + "/" + payIdSub;
    }

    public String getStatus() {
        return status;
    }

    public StatusCode getStatusCode() {
        return StatusCode.byCode(status);
    }

    public String describeStatusCode() {
        final String description = getStatusCode().name();
        if (getStatusCode() == StatusCode.UNKNOWN) {
            return description + " (" + status + ")";
        };
        return description;
    }

    @Override
    public String getTransactionId() {
        return getPayId();
    }

    @Override
    public Optional<ChargeStatus> getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(Optional<ChargeStatus> chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    @Override
    public String toString() {
        return "EpdqNotification{" +
                "status='" + status + "' " +
                "(" + describeStatusCode() + ")" +
                ", payId='" + payId + '\'' +
                ", payIdSub='" + payIdSub + '\'' +
                ", chargeStatus=" + chargeStatus +
                '}';
    }
}
